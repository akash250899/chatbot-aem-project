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
package com.mysite.core.services;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(service = DefaultContentService.class)
@Designate(ocd = DefaultContentService.Config.class)
public class DefaultContentService {

    @ObjectClassDefinition(name = "Default Content Service", description = "Generic content processing utilities")
    public @interface Config {
        @AttributeDefinition(name = "Truncation Suffix", description = "Suffix appended to truncated text")
        String truncationSuffix() default "...";
    }

    private String truncationSuffix;

    @Activate
    @Modified
    void activate(Config config) {
        this.truncationSuffix = config.truncationSuffix();
    }

    public String sanitizeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("<[^>]*>", "");
    }

    public String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + truncationSuffix;
    }

}
