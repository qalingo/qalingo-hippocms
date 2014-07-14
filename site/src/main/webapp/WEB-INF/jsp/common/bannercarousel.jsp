<%--
  Copyright 2011-2014 Hippo B.V. (http://www.onehippo.com)

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

<hst:headContribution keyHint="jqLocalScroll" category="jsInternal">
  <hst:link path="/js/jquery.localscroll-1.2.7-min.js" var="jqLocalScroll"/>
  <script type="text/javascript" src="${jqLocalScroll}"></script>
</hst:headContribution>

<hst:headContribution keyHint="jqScrollTo" category="jsInternal">
  <hst:link path="/js/jquery.scrollTo-1.4.2-min.js" var="jqScrollTo"/>
  <script type="text/javascript" src="${jqScrollTo}"></script>
</hst:headContribution>

<hst:headContribution keyHint="jqSerialScroll" category="jsInternal">
  <hst:link path="/js/jquery.serialScroll-1.2.2-min.js" var="jqSerialScroll"/>
  <script type="text/javascript" src="${jqSerialScroll}"></script>
</hst:headContribution>

<hst:headContribution keyHint="homeSlider" category="jsInternal">
  <hst:link path="/js/home-slider.js" var="homeSlider"/>
  <script type="text/javascript" src="${homeSlider}"></script>
</hst:headContribution>

<hst:headContribution keyHint="customform" category="jsInternal">
  <hst:link path="/js/custom-form-elements.js" var="customform"/>
  <script type="text/javascript" src="${customform}"></script>
</hst:headContribution>

<hst:headContribution keyHint="homeSliderCss" category="css">
  <hst:link path="/css/home-slider.css" var="homeSliderCss"/>
  <link rel="stylesheet" media="screen" type="text/css" href="${homeSliderCss}"/>
</hst:headContribution>

<div id="slider">
    <ul class="navigation" style="z-index:1;">
        <%--@elvariable id="banners" type="java.util.List"--%>
        <c:forEach items="${banners}" var="banner" varStatus="index">
            <%--@elvariable id="banner" type="com.onehippo.gogreen.beans.Banner"--%>
            <li><a href="#banner-${index.count}"><c:out value="${banner.title}"/></a></li>
        </c:forEach>
    </ul>
    <div class="scroll" style="overflow: hidden;">
        <div class="scrollContainer">
            <c:forEach items="${banners}" var="banner" varStatus="index">
                <%--@elvariable id="banner" type="com.onehippo.gogreen.beans.Banner"--%>
                <div class="panel" id="doc-${index.count}"
                     style="background: url(<hst:link hippobean="${banner.image.original}"/>) repeat-y !important; float: left; position: relative;">
                    <div class="title"><a href="<hst:link hippobean="${banner.docLink}"/>"><c:out
                            value="${banner.title}"/></a></div>
                </div>
            </c:forEach>
        </div>
    </div>
</div>
