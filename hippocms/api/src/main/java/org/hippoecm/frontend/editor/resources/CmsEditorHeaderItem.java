/*
 *  Copyright 2010-2013 Hippo B.V. (http://www.onehippo.com)
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.frontend.editor.resources;

import java.util.Arrays;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

public final class CmsEditorHeaderItem extends HeaderItem {

    private static final ResourceReference EDITOR_CSS = new CssResourceReference(CmsEditorHeaderItem.class, "editor.css");

    private static final CmsEditorHeaderItem INSTANCE = new CmsEditorHeaderItem();

    public static CmsEditorHeaderItem get() {
        return INSTANCE;
    }

    private CmsEditorHeaderItem() {
    }

    @Override
    public Iterable<?> getRenderTokens() {
        return Arrays.asList("hippo-cms-editor-header-item");
    }

    @Override
    public void render(final Response response) {
        CssHeaderItem.forReference(EDITOR_CSS).render(response);
    }

}
