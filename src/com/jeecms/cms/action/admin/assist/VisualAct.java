package com.jeecms.cms.action.admin.assist;

import static com.jeecms.cms.Constants.TPLDIR_VISUAL;
import static com.jeecms.cms.Constants.TPL_SUFFIX;
import static com.jeecms.common.page.SimplePage.cpn;
import static com.jeecms.common.web.Constants.UTF8;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jeecms.cms.entity.main.Channel;
import com.jeecms.cms.entity.main.CmsTopic;
import com.jeecms.cms.entity.main.Content;
import com.jeecms.cms.entity.main.Content.ContentStatus;
import com.jeecms.cms.manager.main.ChannelMng;
import com.jeecms.cms.manager.main.CmsTopicMng;
import com.jeecms.cms.manager.main.ContentMng;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.web.CookieUtils;
import com.jeecms.common.web.RequestUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.common.web.springmvc.MessageResolver;
import com.jeecms.common.web.springmvc.RealPathResolver;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.web.util.CmsUtils;
import com.jeecms.core.web.util.FrontUtils;

@Controller
public class VisualAct {

	/**
	 * 通用页面设计
	 */
	@RequiresPermissions("visual:index")
	@RequestMapping(value ="/visual/index.do")
	public String index(String html,ModelMap model) {
		model.addAttribute("html", html);
		return "visual/index";
	}
	
	@RequiresPermissions("visual:channelSelect")
	@RequestMapping(value ="/visual/channelSelect.do")
	public String channelSelect() {
		return "visual/channel_select";
	}
	
	@RequiresPermissions("visual:v_tree")
	@RequestMapping(value ="/visual/v_tree.do")
	public String channelTree(String root, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		boolean isRoot;
		// jquery treeview的根请求为root=source
		if (StringUtils.isBlank(root) || "source".equals(root)) {
			isRoot = true;
		} else {
			isRoot = false;
		}
		model.addAttribute("isRoot", isRoot);
		List<Channel> list;
		if (isRoot) {
			CmsSite site = CmsUtils.getSite(request);
			list = channelMng.getTopList(site.getId(), false);
		} else {
			Integer rootId = Integer.valueOf(root);
			list = channelMng.getChildList(rootId, false);
		}
		model.addAttribute("list", list);
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType("text/json;charset=UTF-8");
		return "visual/channel_tree";
	}
	/**
	 * 栏目页设计
	 * @param html 源html
	 * @param model
	 * @return
	 */
	@RequiresPermissions("visual:channel")
	@RequestMapping(value ="/visual/channel.do")
	public String channel(Integer channelId,String html,ModelMap model) {
		Channel channel=channelMng.findById(channelId);
		model.addAttribute("channel", channel);
		model.addAttribute("html", html);
		return "visual/index";
	}

	/**
	 * 选择内容
	 * @param model
	 * @return
	 */
	@RequiresPermissions("visual:contentSelect")
	@RequestMapping(value ="/visual/contentSelect.do")
	public String contentSelect(ModelMap model) {
		return "visual/content_select";
	}
	
	@RequiresPermissions("visual:v_content_left")
	@RequestMapping("/visual/v_content_left.do")
	public String left() {
		return "visual/content_left";
	}
	
	@RequiresPermissions("visual:v_content_tree")
	@RequestMapping(value = "/visual/v_content_tree.do")
	public String tree(String root, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		boolean isRoot;
		// jquery treeview的根请求为root=source
		if (StringUtils.isBlank(root) || "source".equals(root)) {
			isRoot = true;
		} else {
			isRoot = false;
		}
		model.addAttribute("isRoot", isRoot);
		List<Channel> list;
		Integer siteId = CmsUtils.getSiteId(request);
		Integer userId = CmsUtils.getUserId(request);
		CmsUser user=CmsUtils.getUser(request);
		if(user.getUserSite(siteId).getAllChannel()){
			if (isRoot) {
				list = channelMng.getTopListByRigth(userId, siteId, true);
			} else {
				list = channelMng.getChildListByRight(userId, siteId, Integer
						.parseInt(root), true);
			}
		}else{
			Integer departId=CmsUtils.getUser(request).getDepartment().getId();
			if (isRoot) {
				list = channelMng.getTopListForDepartId(departId,userId,siteId,true);
			} else {
				list = channelMng.getChildListByDepartId(departId,siteId, Integer
						.parseInt(root), true);
			}
		}
		
		model.addAttribute("list", list);
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType("text/json;charset=UTF-8");
		return "visual/content_tree";
	}
	
	@RequiresPermissions("visual:v_content_list")
	@RequestMapping("/visual/v_content_list.do")
	public String contentList(Integer cid,Integer pageNo,HttpServletRequest request, ModelMap model) {
		long time = System.currentTimeMillis();
		String queryTitle = RequestUtils.getQueryParam(request, "queryTitle");
		queryTitle = StringUtils.trim(queryTitle);
		String queryInputUsername = RequestUtils.getQueryParam(request,
				"queryInputUsername");
		queryInputUsername = StringUtils.trim(queryInputUsername);
		ContentStatus status = ContentStatus.all;
		Integer queryInputUserId = null;
		CmsSite site = CmsUtils.getSite(request);
		Integer siteId = site.getId();
		CmsUser user = CmsUtils.getUser(request);
		Integer userId = user.getId();
		byte currStep = user.getCheckStep(siteId);
		Pagination p = contentMng.getPageByRight(queryTitle, null,user.getId(),
				queryInputUserId, false, false, status, user
						.getCheckStep(siteId), siteId, cid, userId,
				0, cpn(pageNo), CookieUtils.getPageSize(request));
		model.addAttribute("pagination", p);
		model.addAttribute("cid", cid);
		model.addAttribute("currStep", currStep);
		model.addAttribute("pageNo", pageNo);
		time = System.currentTimeMillis() - time;
		return "visual/content_list";
	}
	
	/**
	 * 内容页设计
	 * @param html 源html
	 * @param model
	 * @return
	 */
	@RequiresPermissions("visual:content")
	@RequestMapping(value ="/visual/content.do")
	public String content(Integer contentId,String html,ModelMap model) {
		Content content=contentMng.findById(contentId);
		model.addAttribute("content", content);
		model.addAttribute("html", html);
		return "visual/index";
	}
	
	/**
	 * 专题选择
	 * @param request
	 * @param model
	 * @return
	 */
	@RequiresPermissions("visual:v_topic_list")
	@RequestMapping(value ="/visual/v_topic_list.do")
	public String topicList(Integer pageNo,HttpServletRequest request,ModelMap model) {
		Pagination pagination = topicMng.getPage(cpn(pageNo), CookieUtils
				.getPageSize(request));
		model.addAttribute("pagination", pagination);
		return "visual/topic_list";
	}
	/**
	 * 专题页设计
	 */
	@RequiresPermissions("visual:topic")
	@RequestMapping(value ="/visual/topic.do")
	public String topic(Integer topicId,String html,ModelMap model) {
		CmsTopic topic= topicMng.findById(topicId);
		model.addAttribute("topic", topic);
		model.addAttribute("html", html);
		return "visual/index";
	}
	
	@RequestMapping(value ="/visual/layout{lyId}.do",method = RequestMethod.GET)
	public String layout(@PathVariable Integer lyId) {
		return "visual/layout/layout_"+lyId;
	}

	/**
	 * 目前只支持直接填写标签，以后版本实现选择标签和参数
	 * @param directive 标签
	 * @param request
	 * @param response
	 * @param model
	 */
	@RequestMapping(value = "/visual/createPage{tempId}.do")
	public void createPage(String directive,@PathVariable Integer tempId,
			HttpServletRequest request, HttpServletResponse response,ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		// 将request中所有参数
		model.putAll(RequestUtils.getQueryParams(request));
		FrontUtils.frontData(request, model, site);
		FrontUtils.frontPageData(request, model);
	//	String tpl= FrontUtils.getTplPath(request, site.getSolutionPath(),TPLDIR_VISUAL, "tpl.directive"+tempId);
		String tpl=site.getSolutionPath() + "/" + TPLDIR_VISUAL + "/"+ "temp"+tempId + TPL_SUFFIX;
		String realPath=realPathResolver.get(tpl);
		File f = new File(realPath);
		try {
			FileUtils.writeStringToFile(f, directive, UTF8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ResponseUtils.renderJson(response, "{\"success\":true}");
	}
	
	@RequestMapping(value = "/visual/createTempPage.do")
	public void createPageJson(String directive,
			HttpServletRequest request, HttpServletResponse response,ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		// 将request中所有参数
		model.putAll(RequestUtils.getQueryParams(request));
		FrontUtils.frontData(request, model, site);
		FrontUtils.frontPageData(request, model);
		String tpl= FrontUtils.getTplPath(request, site.getSolutionPath(),TPLDIR_VISUAL, "tpl.directive0");
		String realPath=realPathResolver.get(tpl);
		File f = new File(realPath);
		try {
			FileUtils.writeStringToFile(f, directive, UTF8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ResponseUtils.renderJson(response, "{\"success\":true}");
	}
	@Autowired
	private RealPathResolver realPathResolver;
	@Autowired
	private ChannelMng channelMng;
	@Autowired
	private ContentMng contentMng;
	@Autowired
	private CmsTopicMng topicMng;
}
