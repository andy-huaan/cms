package com.jeecms.cms.action.admin.main;

import static com.jeecms.common.page.SimplePage.cpn;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.entity.main.Channel;
import com.jeecms.cms.entity.main.Content;
import com.jeecms.cms.entity.main.ContentShareCheck;
import com.jeecms.cms.manager.main.ChannelMng;
import com.jeecms.cms.manager.main.ContentMng;
import com.jeecms.cms.manager.main.ContentShareCheckMng;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.web.CookieUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.manager.CmsSiteMng;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class CmsContentShareAct {
	@RequiresPermissions("content:v_tree_share")
	@RequestMapping(value = "/content/v_tree_share.do")
	public String treeShare(String root, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		boolean isRoot;
		Integer cid = null;
		Integer sid = null;
		// jquery treeview的根请求为root=source
		if (StringUtils.isBlank(root) || "source".equals(root)) {
			isRoot = true;
		} else {
			isRoot = false;
			// 站点id以s_开头
			if (root.startsWith("s_")) {
				sid = Integer.parseInt(root.split("s_")[1]);
			} else {
				cid = Integer.parseInt(root);
			}
		}
		model.addAttribute("isRoot", isRoot);
		WebErrors errors = validateTree(root, request);
		if (errors.hasErrors()) {
			ResponseUtils.renderJson(response, "[]");
			return null;
		}
		List<CmsSite> siteList = cmsSiteMng.getList();
		// 共享针对的是将本站信息共享到其他站点
		siteList.remove(CmsUtils.getSite(request));
		List<Channel> list = null;
		if (isRoot) {
			model.addAttribute("list", siteList);
		} else {
			if (sid != null) {
				list = channelMng.getTopList(sid, true);
			} else {
				list = channelMng.getChildList(cid, true);
			}
			model.addAttribute("list", list);
		}

		model.addAttribute("siteList", siteList);
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType("text/json;charset=UTF-8");
		return "content/tree_share";
	}

	@RequiresPermissions("content:o_share")
	@RequestMapping("/content/o_share.do")
	public void share(Integer contentIds[], Integer channelIds[],
			HttpServletResponse response) throws JSONException {
		JSONObject json = new JSONObject();
		Boolean pass = true;
		if (contentIds != null && channelIds != null) {
			ContentShareCheck shareCheck;
			Content content;
			for (Integer contentId : contentIds) {
				content = manager.findById(contentId);
				for (Integer channelId : channelIds) {
					List<ContentShareCheck> li = contentShareCheckMng.getList(contentId, channelId);
					shareCheck = new ContentShareCheck();
					//共享待审
					shareCheck.setCheckStatus(ContentShareCheck.CHECKING);
					shareCheck.setShareValid(true);
					if (li == null || li.size() <= 0) {
						contentShareCheckMng.save(shareCheck, content,channelMng.findById(channelId));
					}
				}
			}
		}
		json.put("pass", pass);
		ResponseUtils.renderJson(response, json.toString());
	}
	
	@RequiresPermissions("content:o_push")
	@RequestMapping("/content/o_push.do")
	public void push(Integer contentIds[], Integer channelIds[],
			HttpServletResponse response) throws JSONException {
		JSONObject json = new JSONObject();
		Boolean pass = true;
		if (contentIds != null && channelIds != null) {
			ContentShareCheck shareCheck;
			Content content;
			for (Integer contentId : contentIds) {
				content = manager.findById(contentId);
				for (Integer channelId : channelIds) {
					List<ContentShareCheck> li = contentShareCheckMng
							.getList(contentId, channelId);
					shareCheck = new ContentShareCheck();
					shareCheck.setCheckStatus(ContentShareCheck.PUSHED);
					shareCheck.setShareValid(true);
					if (li == null || li.size() <= 0) {
						contentShareCheckMng.save(shareCheck, content,channelMng.findById(channelId));
					}
				}
			}
		}
		json.put("pass", pass);
		ResponseUtils.renderJson(response, json.toString());
	}
	
	@RequiresPermissions("content:o_delete_share")
	@RequestMapping("/content/o_delete_share.do")
	public String delete_share(Integer[] ids,String title, Byte status, Integer siteId,
			Integer targetSiteId, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		contentShareCheckMng.deleteByIds(ids);
		return list(title, status, siteId, targetSiteId, pageNo, request, model);
	}
	
	@RequiresPermissions("content:v_view_share")
	@RequestMapping("/content/v_view_share.do")
	public String view_share(Integer id,String title, Byte status, Integer siteId,
			Integer targetSiteId,  Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		/*
		WebErrors errors = validateView(id, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		*/
		CmsSite site = CmsUtils.getSite(request);
		Content content = manager.findById(id);
		model.addAttribute("content", content);
		model.addAttribute("site", site);
		model.addAttribute("title", title);
		model.addAttribute("status",status);
		model.addAttribute("siteId", siteId);
		model.addAttribute("targetSiteId", targetSiteId);
		model.addAttribute("pageNo", pageNo);
		return "content/view_share";
	}
	
	@RequiresPermissions("content:o_check_share")
	@RequestMapping("/content/o_check_share.do")
	public String check_share(Integer[] ids,String title, Byte status, Integer siteId,
			Integer targetSiteId,  Integer pageNo, HttpServletRequest request, ModelMap model) {
		ContentShareCheck shareCheck;
		if(ids!=null&&ids.length>0){
			for(Integer id:ids){
				shareCheck=contentShareCheckMng.findById(id);
				//非本站源内容 而且是待审核的共享信息
				if(!shareCheck.getContent().getSite().equals(CmsUtils.getSite(request))&&shareCheck.getCheckStatus()==ContentShareCheck.CHECKING){
					shareCheck.setCheckStatus(ContentShareCheck.CHECKED);
					shareCheck.setShareValid(true);
				}
				contentShareCheckMng.update(shareCheck);
			}
		}
		return list(title, status, siteId, targetSiteId, pageNo, request, model);
	}

	@RequiresPermissions("content:share_list")
	@RequestMapping("/content/v_list_share.do")
	public String list(String title, Byte status, Integer siteId,
			Integer targetSiteId, Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		CmsSite site=CmsUtils.getSite(request);
		if(siteId!=null&&siteId.equals(0)){
			siteId=null;
		}
		if(targetSiteId!=null&&targetSiteId.equals(0)){
			targetSiteId=null;
		}
		if(status!=null&&status==-1){
			status=null;
		}
		Pagination p = contentShareCheckMng.getPageForShared(title, status, siteId,
				targetSiteId, site.getId(),cpn(pageNo), CookieUtils.getPageSize(request));
		List<CmsSite>siteList=cmsSiteMng.getList();
		model.addAttribute("pagination", p);
		model.addAttribute("siteList", siteList);
		model.addAttribute("site", site);
		model.addAttribute("title", title);
		model.addAttribute("status", status);
		model.addAttribute("siteId", siteId);
		model.addAttribute("targetSiteId", targetSiteId);
		return "content/list_share";
	}

	private WebErrors validateTree(String path, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		// if (errors.ifBlank(path, "path", 255)) {
		// return errors;
		// }
		return errors;
	}

	@Autowired
	private CmsSiteMng cmsSiteMng;
	@Autowired
	private ChannelMng channelMng;
	@Autowired
	private ContentMng manager;
	@Autowired
	private ContentShareCheckMng contentShareCheckMng;
}
