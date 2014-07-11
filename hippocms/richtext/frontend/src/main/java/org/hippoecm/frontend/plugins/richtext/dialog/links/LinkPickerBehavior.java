/*
 *  Copyright 2008-2013 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.plugins.richtext.dialog.links;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.richtext.dialog.AbstractAjaxDialogBehavior;
import org.hippoecm.frontend.plugins.richtext.dialog.AbstractRichTextEditorDialog;
import org.hippoecm.frontend.plugins.richtext.model.RichTextEditorInternalLink;

public class LinkPickerBehavior extends AbstractAjaxDialogBehavior {

    private static final long serialVersionUID = 1L;

    private final RichTextEditorLinkService linkService;

    public LinkPickerBehavior(final IPluginContext context,
                              final IPluginConfig config,
                              final RichTextEditorLinkService linkService) {
        super(context, config);
        this.linkService = linkService;
    }

    @Override
    protected AbstractRichTextEditorDialog createDialog() {
        final RichTextEditorInternalLink internalLink = linkService.create(getParameters());
        final IModel<RichTextEditorInternalLink> model = new Model<RichTextEditorInternalLink>(internalLink);
        return new DocumentBrowserDialog<RichTextEditorInternalLink>(getPluginContext(), getPluginConfig(), model);
    }

    @Override
    public void detach(final Component component) {
        super.detach(component);
        if (linkService != null) {
            linkService.detach();
        }
    }

}
