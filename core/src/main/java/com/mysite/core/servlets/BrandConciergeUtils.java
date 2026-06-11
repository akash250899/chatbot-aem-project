package com.mysite.core.servlets;

import com.day.cq.wcm.api.Page;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class BrandConciergeUtils {

    private static final Logger LOG = LoggerFactory.getLogger(BrandConciergeUtils.class);

    public static String extractPageText(Page page) {
        if (page == null) {
            return "";
        }
        Resource contentResource = page.getContentResource();
        if (contentResource == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();

        String title = page.getTitle();
        if (title != null && !title.isEmpty()) {
            sb.append("Title: ").append(title).append("\n");
        }
        String description = page.getDescription();
        if (description != null && !description.isEmpty()) {
            sb.append("Description: ").append(description).append("\n");
        }

        extractTextFromResource(contentResource, sb);

        String rawContext = sb.toString();
        if (rawContext.length() > 15000) {
            return rawContext.substring(0, 15000);
        }
        return rawContext;
    }

    public static String extractPageJson(Page page) {
        if (page == null) {
            return "{}";
        }
        Gson gson = new Gson();
        JsonObject pageJson = new JsonObject();
        pageJson.addProperty("path", page.getPath());
        if (page.getTitle() != null) pageJson.addProperty("title", page.getTitle());
        if (page.getDescription() != null) pageJson.addProperty("description", page.getDescription());
        if (page.getNavigationTitle() != null) pageJson.addProperty("navTitle", page.getNavigationTitle());
        if (page.getPageTitle() != null) pageJson.addProperty("pageTitle", page.getPageTitle());
        pageJson.addProperty("name", page.getName());
        if (page.getTemplate() != null) pageJson.addProperty("template", page.getTemplate().getPath());

        Resource contentResource = page.getContentResource();
        if (contentResource != null) {
            ValueMap props = contentResource.getValueMap();

            if (props.containsKey("cq:tags")) {
                JsonArray tags = new JsonArray();
                String[] tagValues = props.get("cq:tags", String[].class);
                if (tagValues != null) {
                    for (String tag : tagValues) {
                        tags.add(tag);
                    }
                }
                pageJson.add("tags", tags);
            }

            String[] metaKeys = {"cq:lastModified", "cq:lastModifiedBy", "jcr:created", "jcr:createdBy", "cq:template", "sling:resourceType"};
            for (String key : metaKeys) {
                if (props.containsKey(key)) {
                    Object val = props.get(key);
                    if (val instanceof String) {
                        pageJson.addProperty(key.replace("cq:", "").replace("jcr:", ""), (String) val);
                    }
                }
            }
        }

        String pageContent = extractPageText(page);
        if (!pageContent.isEmpty()) {
            pageJson.addProperty("content", pageContent);
        }

        return gson.toJson(pageJson);
    }

    private static void extractTextFromResource(Resource resource, StringBuilder sb) {
        if (resource == null) {
            return;
        }
        ValueMap properties = resource.getValueMap();

        String[] textProperties = {
            "text",
            "description",
            "jcr:description",
            "title",
            "jcr:title",
            "subtitle",
            "linkText",
            "heading"
        };

        for (String prop : textProperties) {
            if (resource.getName().equals("jcr:content") && 
                (prop.equals("jcr:title") || prop.equals("jcr:description") || prop.equals("title") || prop.equals("description"))) {
                continue;
            }
            if (properties.containsKey(prop)) {
                Object val = properties.get(prop);
                if (val instanceof String) {
                    String text = ((String) val).trim();
                    text = text.replaceAll("<[^>]*>", "").trim();
                    if (!text.isEmpty()) {
                        sb.append(text).append("\n");
                    }
                }
            }
        }

        for (Resource child : resource.getChildren()) {
            extractTextFromResource(child, sb);
        }
    }

    public static String queryLocalLLM(String promptText, boolean expectJson) {
        String baseUrl = System.getProperty("lm.studio.base.url", "http://127.0.0.1:1234");
        return queryLocalLLM(baseUrl, promptText, expectJson);
    }

    static String queryLocalLLM(String baseUrl, String promptText, boolean expectJson) {
        Gson gson = new Gson();

        String systemDirective = "";
        String userMessage = promptText;
        int idx = promptText.indexOf("User Question: ");
        if (idx != -1) {
            systemDirective = promptText.substring(0, idx);
            userMessage = promptText.substring(idx);
        }

        JsonArray messages = new JsonArray();
        if (!systemDirective.isEmpty()) {
            JsonObject sys = new JsonObject();
            sys.addProperty("role", "system");
            sys.addProperty("content", systemDirective.trim());
            messages.add(sys);
        }
        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", userMessage.trim());
        messages.add(user);

        JsonObject requestBody = new JsonObject();
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.2);
        requestBody.addProperty("stream", false);

        String jsonPayload = gson.toJson(requestBody);

        try {
            URL url = new URL(baseUrl + "/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(120000);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int status = conn.getResponseCode();
            LOG.info("LM Studio response status: {}", status);
            if (status == 200) {
                String responseBody = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                if (jsonResponse.has("choices")) {
                    JsonArray choices = jsonResponse.getAsJsonArray("choices");
                    if (choices.size() > 0) {
                        JsonObject choice = choices.get(0).getAsJsonObject();
                        if (choice.has("message")) {
                            JsonObject message = choice.getAsJsonObject("message");
                            return message.get("content").getAsString();
                        }
                    }
                }
            } else {
                String errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                LOG.error("LM Studio returned status {}: {}", status, errorBody);
            }
        } catch (Exception e) {
            LOG.error("LM Studio query failed: {}", e.getMessage(), e);
        }
        return null;
    }
}
