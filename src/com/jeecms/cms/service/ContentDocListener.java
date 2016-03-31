package com.jeecms.cms.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jeecms.cms.entity.main.Content;
import com.jeecms.cms.entity.main.ContentDoc;
import com.jeecms.cms.manager.main.ContentDocMng;
import com.jeecms.cms.service.ContentListenerAbstract;
import com.jeecms.common.web.springmvc.RealPathResolver;

@Component
public class ContentDocListener extends ContentListenerAbstract {
	private static final Logger log = LoggerFactory.getLogger(ContentDocListener.class);
	/**
	 * 文件路径
	 */
	private static final String DOC_PATH = "docPath";
	/**
	 * 是否已审核
	 */
	private static final String IS_CHECKED = "isChecked";

	@Override
	public void afterSave(Content content) {
		if (content.isChecked()&&content.getContentDoc()!=null) {
			contentDocMng.createSwfFile(content.getContentDoc());
		}
	}

	@Override
	public Map<String, Object> preChange(Content content) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(IS_CHECKED, content.isChecked());
		if(content.getContentDoc()!=null){
			map.put(DOC_PATH, content.getDocPath());
		}
		return map;
	}

	@Override
	public void afterChange(Content content, Map<String, Object> map) {
		if(content.getContentDoc()!=null){
			boolean curr = content.isChecked();
			boolean pre = (Boolean) map.get(IS_CHECKED);
			String currPath=content.getDocPath();
			String prePath = (String) map.get(DOC_PATH);
			boolean hasChanged=false;
			if(StringUtils.isNotBlank(currPath)){
				if(StringUtils.isBlank(prePath)){
					hasChanged=true;
				}else if(!prePath.equals(currPath)){
					hasChanged=true;
				}
			}
			if (pre && !curr) {
				deleteSwfFile(content);
			} else if (!pre && curr) {
				contentDocMng.createSwfFile(content.getContentDoc());
			} else if (pre && curr) {
				if(hasChanged){
					contentDocMng.createSwfFile(content.getContentDoc());
				}
			}
		}
	}

	@Override
	public void afterDelete(Content content) {
		String ctx=content.getSite().getContextPath();
		ContentDoc contentDoc=content.getContentDoc();
		String docPath=content.getDocPath();
		String swfPath=content.getSwfPath();
		if(StringUtils.isNotBlank(docPath)&&StringUtils.isNotBlank(ctx)){
			docPath=docPath.substring(ctx.length());
		}
		if(StringUtils.isNotBlank(swfPath)&&StringUtils.isNotBlank(ctx)){
			swfPath=swfPath.substring(ctx.length());
		}
		if(contentDoc!=null){
			Integer swfNum=content.getContentDoc().getSwfNum();
			File doc=new File(realPathResolver.get(docPath));
			doc.delete();
			if(StringUtils.isNotBlank(swfPath)){
				for(Integer i=0;i<swfNum;i++){
					File swfFile=new File(realPathResolver.get(swfPath+"_"+i+".swf"));
					swfFile.delete();
				}
			}
			log.info("delete doc file.."+doc.getName());
		}
	}
	private void deleteSwfFile(Content content){
		String ctx=content.getSite().getContextPath();
		String swfPath=content.getSwfPath();
		Integer swfNum=content.getContentDoc().getSwfNum();
		if(StringUtils.isNotBlank(ctx)&&StringUtils.isNotBlank(swfPath)){
			swfPath=swfPath.substring(ctx.length());
			for(Integer i=0;i<swfNum;i++){
				File swfFile=new File(realPathResolver.get(swfPath+"_"+i+".swf"));
				swfFile.delete();
			}
		}
	}
	@Autowired
	private ContentDocMng contentDocMng;
	@Autowired
	private RealPathResolver realPathResolver;
}
