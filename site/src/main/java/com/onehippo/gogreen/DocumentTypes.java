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
package com.onehippo.gogreen;


/**
 * Contains constants for all names of node types and properties.
 */
public class DocumentTypes {

    private DocumentTypes() {
        // prevent instantiation
    }

    public static final String IMAGE_SET = "hippogogreengallery:imageset";

    public static final class ImageSet {
        public static final String ALT = "hippogogreengallery:alt";
        public static final String COPYRIGHT = "hippogogreengallery:copyright";
        public static final String EXTRA_LARGE_THUMBNAIL = "hippogogreengallery:extralargethumbnail";
        public static final String LARGE_THUMBNAIL = "hippogogreengallery:largethumbnail";
        public static final String MOBILE_LOGO = "hippogogreengallery:mobilelogo";
        public static final String MOBILE_THUMBNAIL = "hippogogreengallery:mobilethumbnail";
        public static final String SMALL_THUMBNAIL = "hippogogreengallery:smallthumbnail";
    }

}
