package com.jeecms.cms.action.admin.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.entity.assist.CmsGuestbookCtg;
import com.jeecms.cms.entity.main.Channel;
import com.jeecms.cms.manager.assist.CmsGuestbookCtgMng;
import com.jeecms.cms.manager.main.ChannelMng;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.web.CookieUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.CmsDepartment;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.manager.CmsDepartmentMng;
import com.jeecms.core.manager.CmsLogMng;
import com.jeecms.core.manager.CmsSiteMng;
import com.jeecms.core.manager.CmsUserMng;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;

import static com.jeecms.common.page.SimplePage.cpn;

@Controller
public class CmsDepartmentAct {
	private static final Logger log = LoggerFactory
			.getLogger(CmsDepartmentAct.class);

	@RequiresPermissions("department:depart_main")
	@RequestMapping("/department/depart_main.do")
	public String departMain(ModelMap model) {
		return "department/depart_main";
	}
	
	@RequiresPermissions("department:v_left")
	@RequestMapping("/department/v_left.do")
	public String left() {
		return "department/left";
	}

	@RequiresPermissions("department:v_list")
	@RequestMapping("/department/v_list.do")
	public String list(Integer  root, HttpServletRequest request,ModelMap model) {
		List<CmsDepartment>list=manager.getList(root, false);
		model.addAttribute("list", list);
		model.addAttribute("parentId", root);
		return "department/list";
	}
	
	@RequiresPermissions("department:v_add")
	@RequestMapping("/department/v_add.do")
	public String add(Integer parentId,ModelMap model, HttpServletRequest request) {
		model.addAttribute("site",CmsUtils.getSite(request));
		List<CmsSite> siteList = cmsSiteMng.getList();
		List<CmsGuestbookCtg>ctgList=guestBookCtgMng.getList(CmsUtils.getSiteId(request));
		CmsDepartment parent=null;
		if(parentId!=null){
			parent=manager.findById(parentId);
		}
		model.addAttribute("siteList", siteList);
		model.addAttribute("ctgList", ctgList);
		model.addAttribute("ctgList", ctgList);
		model.addAttribute("parent", parent);
		return "department/add";
	}

	@RequiresPermissions("department:o_save")
	@RequestMapping("/department/o_save.do")
	public String save(CmsDepartment bean, Integer pid,Integer[] channelIds,
			Integer[] controlChannelIds,Integer[] ctgIds,
			HttpServletRequest request,ModelMap model) {
		WebErrors errors = validateSave(bean, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		if(pid!=null&&!pid.equals(0)){
			CmsDepartment parent=manager.findById(pid);
			bean.setParent(parent);
		}
		bean = manager.save(bean,channelIds,controlChannelIds,ctgIds);
		log.info("save CmsDepartment id={}", bean.getId());
		cmsLogMng.operating(request, "cmsdepartment.log.save", "id="
				+ bean.getId() + ";name=" + bean.getName());
		return list(pid, request, model);
	}

	@RequiresPermissions("department:v_check_name")
	@RequestMapping("/department/v_check_name.do")
	public String checkName(String name, HttpServletRequest request,
			HttpServletResponse response) {
		if (StringUtils.isBlank(name) || manager.nameExist(name)) {
			ResponseUtils.renderJson(response, "false");
		} else {
			ResponseUtils.renderJson(response, "true");
		}
		return null;
	}

	@RequiresPermissions("department:v_edit")
	@RequestMapping("/department/v_edit.do")
	public String edit(Integer id,Integer parentId, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateEdit(id, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		CmsDepartment department= manager.findById(id);
		List<CmsSite> siteList = cmsSiteMng.getList();
		List<CmsGuestbookCtg>ctgList=guestBookCtgMng.getList(CmsUtils.getSiteId(request));
		Map<String, Set<Integer>>departChannelIds=new HashMap<String, Set<Integer>>();
		for(CmsSite site:siteList){
			departChannelIds.put(site.getId().toString(), department.getChannelIds(site.getId()));
		}
		List<CmsDepartment>topDeparts=manager.getList(null, false);
		
		model.addAttribute("department", department);
		model.addAttribute("siteList", siteList);
		model.addAttribute("ctgList", ctgList);
		model.addAttribute("departments", CmsDepartment.getListForSelect(topDeparts));
		model.addAttribute("site",CmsUtils.getSite(request));
		model.addAttribute("departChannelIds", departChannelIds);
		model.addAttribute("ctgIds", department.getGuestBookCtgIds());
		model.addAttribute("parentId", parentId);
		return "department/edit";
	}

	@RequiresPermissions("department:o_update")
	@RequestMapping("/department/o_update.do")
	public String update(CmsDepartment bean, Integer pid,Integer[] channelIds,
			Integer[] controlChannelIds,Integer[] ctgIds,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateUpdate(bean.getId(), request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		if(pid!=null){
			if(pid.equals(0)){
				bean.setParent(null);
			}else{
				CmsDepartment parent=manager.findById(pid);
				bean.setParent(parent);
			}
		}
		bean = manager.update(bean,channelIds,controlChannelIds,ctgIds);
		log.info("update CmsDepartment id={}.", bean.getId());
		cmsLogMng.operating(request, "cmsdepartment.log.update", "id="
				+ bean.getId() + ";name=" + bean.getName());
		return list(pid,request, model);
	}

	@RequiresPermissions("department:o_priority")
	@RequestMapping("/department/o_priority.do")
	public String priority(Integer[] wids, Integer[] priority, Integer parentId, HttpServletRequest request, ModelMap model) {
		WebErrors errors = validatePriority(wids, priority, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		manager.updatePriority(wids, priority);
		log.info("update department priority");
		return list(parentId, request, model);
	}

	@RequiresPermissions("department:o_delete")
	@RequestMapping("/department/o_delete.do")
	public String delete(Integer[] ids, Integer parentId,HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		CmsDepartment[] beans = manager.deleteByIds(ids);
		for (CmsDepartment bean : beans) {
			log.info("delete department id={}", bean.getId());
			cmsLogMng.operating(request, "cmsdepartment.log.delete", "id="
					+ bean.getId() + ";name=" + bean.getName());
		}
		return list(parentId, request, model);
	}
	
	@RequiresPermissions("department:v_tree")
	@RequestMapping(value = "/department/v_tree.do")
	public String selectParent(String root, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		log.debug("tree path={}", root);
		boolean isRoot;
		// jquery treeview的根请求为root=source
		if (StringUtils.isBlank(root) || "source".equals(root)) {
			isRoot = true;
		} else {
			isRoot = false;
		}
		model.addAttribute("isRoot", isRoot);
		List<CmsDepartment> departList;
		if(isRoot){
			departList= manager.getList(null,false);
		}else{
			departList = manager.getList(Integer.parseInt(root),false);
		}
		model.addAttribute("list", departList);
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType("text/json;charset=UTF-8");
		return "department/tree";
	}

	@RequiresPermissions("department:v_channels_list")
	@RequestMapping(value = "/department/v_channels_list.do")
	public String channelsAdd(Integer siteId, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		return channelsListJson(siteId,request, response, model);
	}
	
	@RequiresPermissions("department:v_site_list")
	@RequestMapping(value = "/department/v_site_list.do")
	public String siteAddTree(HttpServletRequest request,HttpServletResponse response, ModelMap model) {
		List<CmsSite> siteList= cmsSiteMng.getTopList();
		model.addAttribute("siteList", siteList);
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType("text/json;charset=UTF-8");
		return "department/sites";
	}
	
	@RequiresPermissions("department:v_control_site_list")
	@RequestMapping(value = "/department/v_control_site_list.do")
	public String siteControlTree(HttpServletRequest request,HttpServletResponse response, ModelMap model) {
		List<CmsSite> siteList= cmsSiteMng.getTopList();
		model.addAttribute("siteList", siteList);
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType("text/json;charset=UTF-8");
		return "department/control_sites";
	}
	
	@RequiresPermissions("department:v_list_members")
	@RequestMapping("/department/v_list_members.do")
	public String list_members(Integer departId,Integer root, Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		Pagination pagination = userMng.getAdminsByDepartId(departId,cpn(pageNo), CookieUtils.getPageSize(request));
		model.addAttribute("pagination", pagination);
		model.addAttribute("departId", departId);
		model.addAttribute("root", root);
		return "department/members";
	}
	

	private String channelsListJson(Integer siteId, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		List<Channel> channelList = channelMng.getTopList(siteId, false);
		model.addAttribute("channelList", channelList);
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType("text/json;charset=UTF-8");
		return "department/channels";
	}


	private WebErrors validatePriority(Integer[] ids, Integer[] priorities,
			HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		return errors;
	}

	private WebErrors validateSave(CmsDepartment bean, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		return errors;
	}

	private WebErrors validateEdit(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (vldExist(id, errors)) {
			return errors;
		}
		return errors;
	}

	private WebErrors validateUpdate(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (vldExist(id, errors)) {
			return errors;
		}
		return errors;
	}

	private WebErrors validateDelete(Integer[] ids, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (errors.ifEmpty(ids, "ids")) {
			return errors;
		}
		for (Integer id : ids) {
			vldExist(id, errors);
		}
		return errors;
	}

	private boolean vldExist(Integer id, WebErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		CmsDepartment entity = manager.findById(id);
		if (errors.ifNotExist(entity, CmsDepartment.class, id)) {
			return true;
		}
		return false;
	}

	@Autowired
	private CmsLogMng cmsLogMng;
	@Autowired
	private CmsDepartmentMng manager;
	@Autowired
	private ChannelMng channelMng;
	@Autowired
	private CmsSiteMng cmsSiteMng;
	@Autowired
	private CmsGuestbookCtgMng guestBookCtgMng;
	@Autowired
	private CmsUserMng userMng;
}