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

<hst:defineObjects/>

<hst:link var="productSearchUrl" path="/topproducts" mount="restapi" fullyQualified="true">
  <hst:param name="_type" value="json" />
</hst:link>

<hst:link var="json" path="/js/json2.js"/>
<script src="${json}" type="text/javascript"></script>

<hst:link var="sh" path="/js/syntaxhighlighter_3.0.83" />
<script type="text/javascript" src="${sh}/scripts/shCore.js"></script> 
<script type="text/javascript" src="${sh}/scripts/shBrushJScript.js"></script>

<hst:headContribution category="css">
  <link type="text/css" rel="stylesheet" href="${sh}/styles/shCoreDefault.css"/>
</hst:headContribution>

<hst:headContribution>
  <title><fmt:message key="standard.header.title"/> - <c:out value="${document.title}"/></title>
</hst:headContribution>

<script type="text/javascript">
  //<![CDATA[
$(document).ready(function() {
  var state = {
    shown: "top-product"
  };

  var showSelectedForm = function() {
    var current = $("#rest-call-uri option:selected").val();
    if (current != state.shown) {
      $("#" + state.shown).hide();
      $("#" + current).show();
      state.shown = current;
    }
  };

  showSelectedForm();

  $("#rest-call-uri").change(function() {
    showSelectedForm();

    $('#rest-url').empty();
    $('#rest-output').empty();
  });

  $("#rest-clear").click(function() {
    $("#rest-output").empty();
  });

  $("#rest-submit").click(function() {
    $("#" + state.shown + " form").submit();
  });

  var formatXml = function(xml) {
    var formatted = '';
    var reg = /(>)(<)(\/*)/g;
    xml = xml.replace(reg, '$1\r\n$2$3');
    var pad = 0;
    $.each(xml.split('\r\n'), function(index, node) {
        var indent = 0;
        if (node.match( /.+<\/\w[^>]*>$/ )) {
            indent = 0;
        } else if (node.match( /^<\/\w/ )) {
            if (pad != 0) {
                pad -= 1;
            }
        } else if (node.match( /^<\w[^>]*[^\/]>.*$/ )) {
            indent = 1;
        } else {
            indent = 0;
        }

        var padding = '';
        for (var i = 0; i < pad; i++) {
            padding += '  ';
        }

        formatted += padding + node + '\r\n';
        pad += indent;
    });

    return formatted;
  };

  var updateOutput = function(url) {
    $.ajax({ 
      url: url, 
      success:
        function(data, textStatus, xmlHttp) {
          $('#rest-url').empty();
          $('#rest-url').append('<a href="' + url + '">' + url + '</a>');

          var text = xmlHttp.responseText;
          var contentType = xmlHttp.getResponseHeader("Content-Type");
          if (contentType.match(/application\/json/)) {
            text = JSON.stringify(JSON.parse(xmlHttp.responseText), null, " ").replace(/\\n/g, '\n');
          } else if (contentType.match(/application\/xml/)) {
            text = formatXml(xmlHttp.responseText).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
          }
          $('#rest-output').empty();
          $('#rest-output').append('<pre class="brush: js">' + text + '\n</pre>');
          SyntaxHighlighter.highlight();
        }
    });
  };

  var encodeParams = function(restUrl, form) {
    var params = $(form).serializeArray();
    $.each(params, function(idx, param) {
      if (!idx && restUrl.indexOf('?') == -1) {
        restUrl += '?';
      } else {
        restUrl += '&';
      }
      restUrl += encodeURIComponent(param.name) + '=' + encodeURIComponent(param.value);
    });
    return restUrl;
  };

  $("#top-product form").submit(function() {
    var restUrl = encodeParams('${productSearchUrl}', this);
    updateOutput(restUrl);
    return false;
  });

  $("#product-detail form").submit(function() {
    var restUrl = encodeParams($("#productDetailDropdownList option:selected").val(), this);
    updateOutput(restUrl);
    return false;
  });

  $.ajax({
    url: '${productSearchUrl}',
    success:
      function(data, textStatus, xmlHttp) {
        $.each(data, function(i, item) {
          $("#productDetailDropdownList").append(
            "<option value='" + item.productLink + "'>" + item.localizedName + "</option>\n");
        });
        $("#productDetailDropdownList select:first").select();
      }
  });
});
  //]]>
</script>

<div class="yui-main">
  <div id="content" class="yui-b">
    <div class="rest-api">
    <h2 class="title"><c:out value="${document.title}"/></h2>
    <hst:html hippohtml="${document.description}" />

    <h3>REST Call URI</h3>
    <select name="rest-call-uri" id="rest-call-uri">
      <option value="top-product" selected="selected">Top Products</option>
      <option value="product-detail">Product detail</option>
    </select>

    <div id="top-product">
      <p>Retrieve the top products through the REST API.</p>
      <form action="">
        <ul class="parameters">
          <li class="show">
            <h4>Required Parameters</h4>
            <div class="body">None</div>
          </li>
          <li class="last">
            <h4 >Optional Parameters</h4>
            <div class="body">
              <div class="param">
                <p class="field-description">Order products by rating or price.</p>
                <%--<label for="sortby">Sort By</label>--%>
                <select name="sortby" id="sortby">
                  <option value="hippogogreen:rating" selected="selected">Rating</option>
                  <option value="hippogogreen:price">Price</option>
                </select>
              </div>
              <div class="param">
                <%--<label for="sortdir">Sort Direction</label>--%>
                <p class="field-description">Sort ascending (low to high) or descending.</p>
                <select name="sortdir" id="sortdir">
                  <option value="descending" selected="selected">Descending</option>
                  <option value="ascending">Ascending</option>
                </select>
                
              </div>
              <div class="param">
                <%--<label for="max">Max</label>--%>
                <p class="field-description">Maximum number of results.</p>
                <input type="text" id="max" name="max" value="10" />
              </div>  
            </div>
          </li>
        </ul>
      </form>
    </div>

    <div id="product-detail" style="display: none;">
      <p>Retrieve product details through the REST API.</p>
      <form action="">
        <ul class="parameters">
          <li class="show">
            <h4>Required Parameters</h4>
            <div class="body">
              <div class="param">
                <%--<label>Product</label>--%>
                <p>Show details of the selected product type.</p>
                <select id="productDetailDropdownList"></select>
               </div> 
            </div>
          </li>
          <li class="last">
            <h4>Optional Parameters</h4>
            <div class="body">
              <div class="param">
                <%--<label for="_type">Response Type</label>--%>
                <p>Format the response as JSON or XML.</p>
                <select id="_type" name="_type">
                  <option value="json" selected="selected">JSON</option>
                  <option value="xml">XML</option>
                </select>
              </div>  
            </div>
          </li>
        </ul>  
      </form>
    </div>

    <button  class="submit-button" id="rest-submit">Execute REST Call</button>
    <input type="reset" class="reset-button" value="Clear REST Call" id="rest-clear" />

    <h3 class="rest-url">REST URL</h3>
    <p id="rest-url"></p>

    <h3 class="rest-output">REST Call Output</h3>
    <div id="rest-output">
    </div>

    </div>
  </div>
</div>
