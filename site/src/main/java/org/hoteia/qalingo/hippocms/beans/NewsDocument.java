package org.hoteia.qalingo.hippocms.beans;

import java.util.Calendar;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSetBean;

@Node(jcrType="qalingohippocms:newsdocument")
public class NewsDocument extends BaseDocument{

    public String getTitle() {
        return getProperty("qalingohippocms:title");
    }
    
    public String getSummary() {
        return getProperty("qalingohippocms:summary");
    }
    
    public Calendar getDate() {
        return getProperty("qalingohippocms:date");
    }

    public HippoHtml getHtml(){
        return getHippoHtml("qalingohippocms:body");    
    }

    /**
     * Get the imageset of the newspage
     *
     * @return the imageset of the newspage
     */
    public HippoGalleryImageSetBean getImage() {
        return getLinkedBean("qalingohippocms:image", HippoGalleryImageSetBean.class);
    }


}
