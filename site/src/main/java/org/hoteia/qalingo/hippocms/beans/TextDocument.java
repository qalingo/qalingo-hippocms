package org.hoteia.qalingo.hippocms.beans;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoHtml;

@Node(jcrType="qalingohippocms:textdocument")
public class TextDocument extends BaseDocument{
    
    public String getTitle() {
        return getProperty("qalingohippocms:title");
    }

    public String getSummary() {
        return getProperty("qalingohippocms:summary");
    }
    
    public HippoHtml getHtml(){
        return getHippoHtml("qalingohippocms:body");    
    }

}
