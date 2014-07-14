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

<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="hst" uri="http://www.hippoecm.org/jsp/hst/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="image" type="com.onehippo.gogreen.beans.compound.ImageSet" rtexprvalue="true" required="false"%>

<c:set var="copyright" value="${image.copyright}"/>
<c:if test="${copyright ne null and not (empty copyright.description and empty copyright.url)}">
  <p class="copyright">
    &copy; <c:out value="${copyright.description}"/>
    <c:if test="${not (empty copyright.description or empty copyright.url)}"><br/></c:if>
    <c:if test="${not empty copyright.url}">
      <c:url var="link" value="${fn:escapeXml(copyright.url)}"/>
      <a href="${link}"><c:out value="${copyright.url}"/></a>
    </c:if>
  </p>
</c:if>
