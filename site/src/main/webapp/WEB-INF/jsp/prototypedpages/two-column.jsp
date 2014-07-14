<%--

    Copyright 2014 Hippo B.V. (http://www.onehippo.com)

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

<hst:defineObjects/>

<hippo-gogreen:title title="${hstRequestContext.resolvedSiteMapItem.pageTitle}"/>

<div id="bd" class="yui-t1 prototyped-page prototyped-page-two-column">

    <div class="yui-main">
        <div id="content" class="yui-b right">
            <hst:include ref="left"/>
        </div>
    </div>

    <div id="right" class="yui-b">
      <hst:include ref="right"/>
    </div>

</div>
