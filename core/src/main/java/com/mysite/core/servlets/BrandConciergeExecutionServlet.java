package com.mysite.core.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(
        service = { Servlet.class },
        property = {
                "sling.servlet.paths=/bin/brandconcierge/llm-execute",
                "sling.servlet.methods=GET"
        }
)
@ServiceDescription("Brand Concierge LLM Query Execution Servlet")
public class BrandConciergeExecutionServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 3L;
    private static final Logger LOG = LoggerFactory.getLogger(BrandConciergeExecutionServlet.class);

    @Override
    protected void doGet(final SlingHttpServletRequest request,
                          final SlingHttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();

        String pagePath = request.getParameter("pagePath");
        String prompt = request.getParameter("prompt");

        if (prompt == null || prompt.isEmpty()) {
            responseJson.addProperty("response", "Thank you for your question. The page content has been noted and our team will review it. Please try one of the suggested prompts above for more information about this page.");
            response.getWriter().write(gson.toJson(responseJson));
            return;
        }

        responseJson.addProperty("response", "Thank you for your question. The page content has been noted and our team will review it. Please try one of the suggested prompts above for more information about this page.");

        response.getWriter().write(gson.toJson(responseJson));
    }
}
