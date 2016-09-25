package com.cnpc.framework.tags;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.cnpc.framework.utils.SessionUtil;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class CSRFTag implements TemplateDirectiveModel {

    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException,
            IOException {

        StringBuffer html = new StringBuffer();
        html.append("<input type=\"hidden\" ");
        html.append("id=\"csrfToken\" name=\"csrfToken\" ");
        if (SessionUtil.getSession().getAttribute("csrfToken") != null) {
            html.append("value=\"" + SessionUtil.getSession().getAttribute("csrfToken") + "\"");
        }
        html.append("/>");
        Writer out = env.getOut();
        out.write(html.toString());
    }
}
