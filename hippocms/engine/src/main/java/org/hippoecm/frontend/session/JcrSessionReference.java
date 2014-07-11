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
package org.hippoecm.frontend.session;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import org.apache.wicket.model.IModel;

import javax.jcr.Session;

/**
 * Maintains a reference to a JCR session model, based on a Wicket session.
 * When the Wicket session is collected as garbage, the JCR session model
 * is detached.
 */
class JcrSessionReference extends WeakReference<UserSession> {

    static final ReferenceQueue<UserSession> refQueue = new ReferenceQueue<UserSession>();

    static void cleanup() {
        JcrSessionReference ref;
        while ((ref = (JcrSessionReference) refQueue.poll()) != null) {
            if (ref.jcrSession != null) {
                ref.jcrSession.detach();
            }
        }
    }

    IModel<Session> jcrSession;

    JcrSessionReference(UserSession referent) {
        super(referent, refQueue);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JcrSessionReference) && ((JcrSessionReference) obj).get() == get();
    }

    @Override
    public int hashCode() {
        UserSession session = get();
        if (session != null) {
            return session.hashCode() ^ 327;
        }
        return 17;
    }
}
