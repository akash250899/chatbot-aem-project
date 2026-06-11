/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mysite.core.servlets;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;

@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes = "mysite/components/spotlight",
        methods = HttpConstants.METHOD_GET,
        extensions = "json")
@ServiceDescription("Spotlight Servlet")
public class SpotlightServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
        Resource resource = req.getResource();
        ValueMap props = resource.getValueMap();

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"title\":\"").append(escape(props.get("title", ""))).append("\",");
        json.append("\"description\":\"").append(escape(props.get("description", ""))).append("\",");
        json.append("\"imageReference\":\"").append(escape(props.get("imageReference", ""))).append("\",");
        json.append("\"linkURL\":\"").append(escape(props.get("linkURL", ""))).append("\",");
        json.append("\"linkText\":\"").append(escape(props.get("linkText", "Read More"))).append("\",");
        json.append("\"alignment\":\"").append(escape(props.get("alignment", "left"))).append("\"");
        json.append("}");

        resp.getWriter().write(json.toString());
    }

    private String escape(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

}
