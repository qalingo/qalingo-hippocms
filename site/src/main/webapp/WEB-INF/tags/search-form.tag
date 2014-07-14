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

<hst:link siteMapItemRefId="search" var="doSearch" />
<c:set var="searchText"><fmt:message key="searchform.search" /></c:set>
<form id="searchSubmit" action="${doSearch}" method="get" onsubmit="sanitizeRequestParam(document.forms['searchSubmit']['query'], '${searchText}')"/>
  <p>
    <input class="search-field gray" type="text" name="query" value="<fmt:message key="searchform.search" />"/>
    <input type="submit" value="${searchText}" class="search-button"/>
  </p>
</form>