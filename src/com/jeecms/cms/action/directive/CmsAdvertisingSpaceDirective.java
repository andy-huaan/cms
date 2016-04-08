package com.jeecms.cms.action.directive;
import static com.jeecms.common.web.freemarker.DirectiveUtils.OUT_BEAN;
import static freemarker.template.ObjectWrapper.DEFAULT_WRAPPER;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import static com.jeecms.common.web.freemarker.DirectiveUtils.OUT_LIST;
import com.jeecms.cms.entity.assist.CmsAdvertising;
import com.jeecms.cms.manager.assist.CmsAdvertisingMng;
import com.jeecms.common.web.freemarker.DirectiveUtils;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * 广告对象标签
 */
public class CmsAdvertisingSpaceDirective implements TemplateDirectiveModel {
    /**
     * 输入参数，广告ID。
     */
    public static final String PARAM_ID = "id";


    @SuppressWarnings("unchecked")
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
            TemplateDirectiveBody body) throws TemplateException, IOException {
        Integer id = DirectiveUtils.getInt(PARAM_ID, params);
        List<CmsAdvertising> ads = null;
        if (id != null) {
            
            //ad = cmsAdvertisingMng.findById(id);
            ads= cmsAdvertisingMng.getList(id, true);
        }
        Map<String, TemplateModel> paramWrap = new HashMap<String, TemplateModel>(
                params);
        if (ads!=null){
            paramWrap.put(OUT_LIST, DEFAULT_WRAPPER.wrap(ads));
        }
        
        Map<String, TemplateModel> origMap = DirectiveUtils
                .addParamsToVariable(env, paramWrap);
        body.render(env.getOut());
        DirectiveUtils.removeParamsFromVariable(env, paramWrap, origMap);
    }

    @Autowired
    private CmsAdvertisingMng cmsAdvertisingMng;
}