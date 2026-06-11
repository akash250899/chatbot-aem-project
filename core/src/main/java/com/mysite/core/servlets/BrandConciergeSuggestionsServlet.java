package com.mysite.core.servlets;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component(
        service = { Servlet.class },
        property = {
                "sling.servlet.paths=/bin/brandconcierge/suggested-prompts",
                "sling.servlet.methods=GET"
        }
)
@ServiceDescription("Brand Concierge Dynamic Suggestions Servlet")
public class BrandConciergeSuggestionsServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 2L;
    private static final Logger LOG = LoggerFactory.getLogger(BrandConciergeSuggestionsServlet.class);

    private static final String[] FALLBACK_PROMPTS = {
            "Summarize the main requirements of this page",
            "What are the critical dependencies outlined here?",
            "Who are the key stakeholders mentioned on this page?",
            "What are the main goals of this content?"
    };

    @Override
    protected void doGet(final SlingHttpServletRequest request,
                         final SlingHttpServletResponse response) throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pagePath = request.getParameter("pagePath");
        Gson gson = new Gson();
        List<String> suggestions = null;

        if (pagePath != null && !pagePath.isEmpty()) {
            ResourceResolver resolver = request.getResourceResolver();
            PageManager pageManager = resolver.adaptTo(PageManager.class);
            if (pageManager != null) {
                Page page = pageManager.getPage(pagePath);
                if (page != null) {
                    String pageContent = BrandConciergeUtils.extractPageText(page);

                    if (!pageContent.trim().isEmpty()) {
                        String systemPrompt =
                            "[System Role Directive]\n" +
                            "You are a page content analyst. Below is the full visible text content of a webpage as seen by a visitor:\n" +
                            "---\n" +
                            pageContent + "\n" +
                            "---\n\n" +
                            "Based strictly on the above page content, generate exactly 4 specific, insightful questions that a visitor might want to click on to learn more about the topics, details, and information presented on this page.\n" +
                            "Each question must be directly answerable from the page content and reference specific details found in it.\n\n" +
                            "[Output Rules]\n" +
                            "Respond with ONLY a valid JSON array of exactly 4 strings. No markdown, no code blocks, no extra text.\n" +
                            "Example: [\"What are the key features described in this product?\", \"Who is this service designed for?\", \"What are the pricing options mentioned?\", \"How do I get started according to the page?\"]";

                        String llmResponse = BrandConciergeUtils.queryLocalLLM(systemPrompt, true);
                        if (llmResponse != null) {
                            suggestions = parseSuggestions(llmResponse, gson);
                        }
                    }
                }
            }
        }

        // If suggestions are missing or parsing failed, load the default fallback prompts
        if (suggestions == null || suggestions.size() != 4) {
            LOG.warn("LLM suggested prompts call failed to reach LM Studio, falling back to default list.");
            suggestions = new ArrayList<>();
            for (String p : FALLBACK_PROMPTS) {
                suggestions.add(p);
            }
        }

        response.getWriter().write(gson.toJson(suggestions));
    }

    private List<String> parseSuggestions(String jsonResponse, Gson gson) {
        try {
            // Clean up possible markdown wrappers if LLM ignored instructions
            String cleanJson = jsonResponse.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            } else if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();

            JsonArray array = gson.fromJson(cleanJson, JsonArray.class);
            List<String> parsed = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                parsed.add(array.get(i).getAsString());
            }
            return parsed;
        } catch (JsonSyntaxException | IllegalStateException | IndexOutOfBoundsException e) {
            LOG.error("Failed to parse JSON response from Ollama: " + jsonResponse, e);
        }
        return null;
    }
}
