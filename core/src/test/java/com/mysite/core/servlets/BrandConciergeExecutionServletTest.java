package com.mysite.core.servlets;

import com.google.gson.JsonObject;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(AemContextExtension.class)
class BrandConciergeExecutionServletTest {

    private final BrandConciergeExecutionServlet servlet = new BrandConciergeExecutionServlet();

    @Test
    void testDoGetResponse(AemContext context) throws ServletException, IOException {
        MockSlingHttpServletRequest request = context.request();
        request.setQueryString("pagePath=/content/mysite/us/en&prompt=test");

        MockSlingHttpServletResponse response = context.response();

        servlet.doGet(request, response);

        String output = response.getOutputAsString();
        JsonObject responseJson = new com.google.gson.Gson().fromJson(output, JsonObject.class);
        
        assertTrue(responseJson.has("response"));
        assertTrue(responseJson.get("response").getAsString().contains("Thank you"), "Should contain fixed response");
    }
}
