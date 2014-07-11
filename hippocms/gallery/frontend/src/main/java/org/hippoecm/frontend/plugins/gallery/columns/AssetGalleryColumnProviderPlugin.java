/*
 * Copyright 2010-2013 Hippo B.V. (http://www.onehippo.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.frontend.plugins.gallery.columns;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.gallery.columns.compare.CalendarComparator;
import org.hippoecm.frontend.plugins.gallery.columns.compare.MimeTypeComparator;
import org.hippoecm.frontend.plugins.gallery.columns.compare.SizeComparator;
import org.hippoecm.frontend.plugins.gallery.columns.render.DatePropertyRenderer;
import org.hippoecm.frontend.plugins.gallery.columns.render.MimeTypeIconRenderer;
import org.hippoecm.frontend.plugins.gallery.columns.render.SizeRenderer;
import org.hippoecm.frontend.plugins.gallery.columns.render.StringPropertyRenderer;
import org.hippoecm.frontend.plugins.standards.ClassResourceModel;
import org.hippoecm.frontend.plugins.standards.list.AbstractListColumnProviderPlugin;
import org.hippoecm.frontend.plugins.standards.list.ListColumn;
import org.hippoecm.frontend.plugins.standards.list.comparators.NameComparator;
import org.hippoecm.frontend.plugins.standards.list.resolvers.IconAttributeModifier;

public class AssetGalleryColumnProviderPlugin extends AbstractListColumnProviderPlugin {
    private static final long serialVersionUID = 1L;

    private static final CssResourceReference CSS_RESOURCE_REFERENCE = new CssResourceReference(AssetGalleryColumnProviderPlugin.class, "AssetGalleryStyle.css");

    private String primaryItemName;

    public AssetGalleryColumnProviderPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        primaryItemName = config.getString("primaryItemName", "hippogallery:asset");
    }

    @Override
    public IHeaderContributor getHeaderContributor() {
        return new IHeaderContributor() {
            @Override
            public void renderHead(final IHeaderResponse response) {
                response.render(CssHeaderItem.forReference(CSS_RESOURCE_REFERENCE));
            }
        };
    }

    @Override
    public List<ListColumn<Node>> getColumns() {
        List<ListColumn<Node>> columns = new ArrayList<ListColumn<Node>>();

        ListColumn<Node> column = new ListColumn<Node>(new Model<String>(""), null);
        column.setRenderer(new MimeTypeIconRenderer());
        column.setAttributeModifier(new IconAttributeModifier());
        column.setComparator(new MimeTypeComparator("jcr:mimeType", primaryItemName));
        column.setCssClass("assetgallery-type");
        columns.add(column);

        column = new ListColumn<Node>(new ClassResourceModel("assetgallery-name", Translations.class), "name");
        column.setComparator(new NameComparator());
        column.setCssClass("assetgallery-name");
        columns.add(column);

        return columns;
    }

    @Override
    public List<ListColumn<Node>> getExpandedColumns() {
        List<ListColumn<Node>> columns = getColumns();

        //Filesize
        ListColumn<Node> column = new ListColumn<Node>(new ClassResourceModel("assetgallery-size", Translations.class),
                "size");
        column.setRenderer(new SizeRenderer("jcr:data", primaryItemName));
        column.setComparator(new SizeComparator("jcr:data", primaryItemName));
        column.setCssClass("assetgallery-size");
        columns.add(column);

        //Mimetype
        column = new ListColumn<Node>(new ClassResourceModel("assetgallery-mimetype", Translations.class), "mimetype");
        column.setRenderer(new StringPropertyRenderer("jcr:mimeType", primaryItemName));
        column.setComparator(new MimeTypeComparator("jcr:mimeType", primaryItemName));
        column.setCssClass("assetgallery-mimetype");
        columns.add(column);

        //Last modified date
        column = new ListColumn<Node>(new ClassResourceModel("assetgallery-lastmodified", Translations.class),
                "lastmodified");
        column.setRenderer(new DatePropertyRenderer("jcr:lastModified", primaryItemName));
        column.setComparator(new CalendarComparator("jcr:lastModified", primaryItemName));
        column.setCssClass("assetgallery-lastmodified");
        columns.add(column);

        return columns;
    }
}
