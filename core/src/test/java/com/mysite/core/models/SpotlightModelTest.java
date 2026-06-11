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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.mysite.core.testcontext.AppAemContext;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class SpotlightModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    private SpotlightModel spotlight;

    @BeforeEach
    void setup() {
        Page page = context.create().page("/content/mysite/us/en");
        Resource resource = context.create().resource(page, "spotlight",
            "sling:resourceType", "mysite/components/spotlight",
            "title", "Test Spotlight",
            "description", "Test description",
            "linkURL", "/content/mysite/us/en",
            "linkText", "Learn More",
            "alignment", "center");

        spotlight = resource.adaptTo(SpotlightModel.class);
    }

    @Test
    void testModelNotNull() {
        assertNotNull(spotlight);
    }

    @Test
    void testGetTitle() {
        assertEquals("Test Spotlight", spotlight.getTitle());
    }

    @Test
    void testGetLinkURL() {
        assertEquals("/content/mysite/us/en", spotlight.getLinkURL());
    }

    @Test
    void testGetLinkText() {
        assertEquals("Learn More", spotlight.getLinkText());
    }

    @Test
    void testGetAlignment() {
        assertEquals("center", spotlight.getAlignment());
    }

    @Test
    void testGetDescription() {
        assertEquals("Test description", spotlight.getDescription());
    }

    @Test
    void testImageReferenceIsEmptyByDefault() {
        assertEquals("", spotlight.getImageReference());
    }

}
