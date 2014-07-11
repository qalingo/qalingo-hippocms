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
package org.hippoecm.frontend.editor.compare;

public class ObjectComparer implements IComparer<Object> {

    private static final long serialVersionUID = 9007990171845097749L;

    public boolean areEqual(Object oldValue, Object newValue) {
        if (oldValue != null && newValue != null) {
            if (!oldValue.equals(newValue)) {
                return false;
            }
        } else if (oldValue != newValue) {
            return false;
        }
        return true;
    }

    public int getHashCode(Object value) {
        if (value != null) {
            return value.hashCode();
        }
        return 0;
    }

}
