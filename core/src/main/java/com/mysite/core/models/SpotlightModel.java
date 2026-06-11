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
package com.mysite.core.models;

import javax.annotation.PostConstruct;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.mysite.core.services.DefaultContentService;
import com.mysite.core.services.SpotlightService;

@Model(adaptables = Resource.class)
public class SpotlightModel {

    @ValueMapValue
    @Default(values = "")
    private String title;

    @ValueMapValue
    @Default(values = "")
    private String description;

    @ValueMapValue
    @Default(values = "")
    private String imageReference;

    @ValueMapValue
    @Default(values = "")
    private String linkURL;

    @ValueMapValue
    @Default(values = "Read More")
    private String linkText;

    @ValueMapValue
    @Default(values = "left")
    private String alignment;

    @OSGiService(injectionStrategy = InjectionStrategy.OPTIONAL)
    private SpotlightService spotlightService;

    @OSGiService(injectionStrategy = InjectionStrategy.OPTIONAL)
    private DefaultContentService contentService;

    @PostConstruct
    void init() {
        if (spotlightService != null) {
            description = spotlightService.enrichHtml(description);
        }
        if (contentService != null && title != null) {
            title = contentService.truncate(title, 100);
        }
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageReference() {
        return imageReference;
    }

    public String getLinkURL() {
        return linkURL;
    }

    public String getLinkText() {
        return linkText;
    }

    public String getAlignment() {
        return alignment;
    }

}
