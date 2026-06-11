package com.mysite.core.servlets;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.servlet.ServletException;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(AemContextExtension.class)
class BrandConciergeSuggestionsServletTest {

    private final BrandConciergeSuggestionsServlet servlet = new BrandConciergeSuggestionsServlet();

    @BeforeAll
    static void setup() {
        System.setProperty("lm.studio.base.url", "http://localhost:1");
    }

    @Test
    void testDoGetFallback(AemContext context) throws ServletException, IOException {
        // Build mock page
        context.create().page("/content/mysite/us/en");
        context.request().setParameterMap(java.util.Map.of("pagePath", "/content/mysite/us/en"));

        MockSlingHttpServletRequest request = context.request();
        MockSlingHttpServletResponse response = context.response();

        servlet.doGet(request, response);

        String output = response.getOutputAsString();
        // Since local LLM is offline in unit tests, it should return the fallback list of prompts
        assertTrue(output.contains("Summarize the main requirements of this page"), "Should contain fallback prompt 1");
        assertTrue(output.contains("What are the critical dependencies outlined here?"), "Should contain fallback prompt 2");
        assertTrue(output.contains("Who are the key stakeholders mentioned on this page?"), "Should contain fallback prompt 3");
        assertTrue(output.contains("What are the main goals of this content?"), "Should contain fallback prompt 4");
    }
}
