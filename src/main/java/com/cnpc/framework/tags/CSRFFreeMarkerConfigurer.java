package com.cnpc.framework.tags;

import java.io.IOException;

import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import freemarker.template.TemplateException;

public class CSRFFreeMarkerConfigurer extends FreeMarkerConfigurer {

    @Override
    public void afterPropertiesSet() throws IOException, TemplateException {

        super.afterPropertiesSet();
        this.getConfiguration().setSharedVariable("csrf", new CSRFTag());
    }

}
