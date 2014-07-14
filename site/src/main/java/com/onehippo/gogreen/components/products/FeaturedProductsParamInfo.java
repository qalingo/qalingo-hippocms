/*
 * Copyright 2011-2013 Hippo B.V. (http://www.onehippo.com)
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
package com.onehippo.gogreen.components.products;

import org.hippoecm.hst.core.parameters.FieldGroup;
import org.hippoecm.hst.core.parameters.FieldGroupList;
import org.hippoecm.hst.core.parameters.JcrPath;
import org.hippoecm.hst.core.parameters.Parameter;

@FieldGroupList({
        @FieldGroup({
                "product1", "product2", "product3"
        })
})
public interface FeaturedProductsParamInfo {

    @Parameter(name = "product1", required = true, displayName = "Product 1")
    @JcrPath(isRelative = true, pickerInitialPath = "products", pickerSelectableNodeTypes = {"hippogogreen:product"})
    String getProduct1();

    @Parameter(name = "product2", required = true, displayName = "Product 2")
    @JcrPath(isRelative = true, pickerInitialPath = "products", pickerSelectableNodeTypes = {"hippogogreen:product"})
    String getProduct2();

    @Parameter(name = "product3", required = true, displayName = "Product 3")
    @JcrPath(isRelative = true, pickerInitialPath = "products", pickerSelectableNodeTypes = {"hippogogreen:product"})
    String getProduct3();

}
