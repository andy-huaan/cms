package com.jeecms.cms.action.admin.main;

import static com.jeecms.common.page.SimplePage.cpn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.common.page.Pagination;
import com.jeecms.common.web.CookieUtils;
import com.jeecms.core.entity.CmsRole;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.CmsWorkflow;
import com.jeecms.core.manager.CmsRoleMng;
import com.jeecms.core.manager.CmsWorkflowMng;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;

@Controller
public class CmsWorkflowAct {
	private static final Logger log = LoggerFactory.getLogger(CmsWorkflowAct.class);
	
	@RequiresPermissions("workflow:v_list")
	@RequestMapping("/workflow/v_list.do")
	public String list(Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		Pagination pagination = manager.getPage(CmsUtils.getSiteId(request),cpn(pageNo), CookieUtils.getPageSize(request));
		model.addAttribute("pagination", pagination);
		model.addAttribute("pageNo", pagination.getPageNo());
		return "workflow/list";
	}

	@RequiresPermissions("workflow:v_add")
	@RequestMapping("/workflow/v_add.do")
	public String add(HttpServletRequest request,ModelMap model) {
		List<CmsRole>roles=roleMng.getList();
		model.addAttribute("roles", roles);
		return "workflow/add";
	}

	@RequiresPermissions("workflow:v_edit")
	@RequestMapping("/workflow/v_edit.do")
	public String edit(Integer id, Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateEdit(id, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		List<CmsRole>roles=roleMng.getList();
		model.addAttribute("roles", roles);
		model.addAttribute("workflow", manager.findById(id));
		model.addAttribute("pageNo", pageNo);
		return "workflow/edit";
	}

	@RequiresPermissions("workflow:o_save")
	@RequestMapping("/workflow/o_save.do")
	public String save(CmsWorkflow bean, Integer[] roleIds,Integer[] countersigns, 
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateSave(bean, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		countersigns=getCountersignsParam(request);
		bean = manager.save(bean, roleIds, countersigns);
		log.info("save CmsWorkflow id={}", bean.getId());
		return "redirect:v_list.do";
	}
	
	@SuppressWarnings("unchecked")
	private Integer[] getCountersignsParam(HttpServletRequest request){
		Enumeration paramNames=request.getParameterNames();
		List<Integer>countersigns=new ArrayList<Integer>();
		String paramName;
		while(paramNames.hasMoreElements()){
			paramName=(String) paramNames.nextElement();
			if(paramName.startsWith("countersign")){
				countersigns.add(Integer.parseInt(request.getParameter(paramName)));
			}
		}
		Collections.reverse(countersigns);
		Integer[]countersignsArray=new Integer[countersigns.size()];
		return  countersigns.toArray(countersignsArray);
	}

	@RequiresPermissions("workflow:o_update")
	@RequestMapping("/workflow/o_update.do")
	public String update(CmsWorkflow bean, Integer[] roleIds, Integer[] countersigns, 
			Integer pageNo,HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateUpdate(bean.getId(), request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		countersigns=getCountersignsParam(request);
		bean = manager.update(bean, roleIds, countersigns);
		log.info("update CmsWorkflow id={}.", bean.getId());
		return list(pageNo, request, model);
	}

	@RequiresPermissions("workflow:o_delete")
	@RequestMapping("/workflow/o_delete.do")
	public String delete(Integer[] ids, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		CmsWorkflow[] beans = manager.deleteByIds(ids);
		for (CmsWorkflow bean : beans) {
			log.info("delete CmsWorkflow id={}", bean.getId());
		}
		return list(pageNo, request, model);
	}
	
	@RequiresPermissions("workflow:o_priority")
	@RequestMapping("/workflow/o_priority.do")
	public String priority(Integer[] wids, Integer[] priority,
			Integer queryCtgId, HttpServletRequest request, ModelMap model) {
		manager.updatePriority(wids, priority);
		log.info("update CmsWorkflow priority.");
		return list(queryCtgId, request, model);
	}

	private WebErrors validateSave(CmsWorkflow bean, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		bean.setSite(site);
		return errors;
	}

	private WebErrors validateEdit(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (vldExist(id, site.getId(), errors)) {
			return errors;
		}
		return errors;
	}

	private WebErrors validateUpdate(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (vldExist(id, site.getId(), errors)) {
			return errors;
		}
		return errors;
	}

	private WebErrors validateDelete(Integer[] ids, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (errors.ifEmpty(ids, "ids")) {
			return errors;
		}
		for (Integer id : ids) {
			vldExist(id, site.getId(), errors);
		}
		return errors;
	}

	private boolean vldExist(Integer id, Integer siteId, WebErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		CmsWorkflow entity = manager.findById(id);
		if (errors.ifNotExist(entity, CmsWorkflow.class, id)) {
			return true;
		}
		if (!entity.getSite().getId().equals(siteId)) {
			errors.notInSite(CmsWorkflow.class, id);
			return true;
		}
		return false;
	}

	@Autowired
	private CmsWorkflowMng manager;
	@Autowired
	private CmsRoleMng roleMng;
}