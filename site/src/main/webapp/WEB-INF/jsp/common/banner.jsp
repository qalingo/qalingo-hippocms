<%--
  Copyright 2010-2013 Hippo B.V. (http://www.onehippo.com)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--%>
<%@include file="../includes/tags.jspf" %>
<%--@elvariable id="document" type="com.onehippo.gogreen.beans.Banner"--%>
<ul class="banner">
  <c:if test="${not empty document.docLink}">
    <li class="full-link">
      <a href="<hst:link hippobean="${document.docLink}"/>"></a>
    </li>
    <li class="title">
      <a href="<hst:link hippobean="${document.docLink}"/>"><c:out value="${document.title}"/></a>
    </li>
    <li class="image">
        <img src="<hst:link hippobean="${document.image.original}"/>" alt="<fmt:message key="common.banner.image.alt"/>" />
    </li>
  </c:if>
</ul>