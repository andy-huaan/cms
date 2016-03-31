package com.jeecms.core.manager.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeecms.common.hibernate3.Updater;
import com.jeecms.common.page.Pagination;
import com.jeecms.core.dao.CmsWorkflowDao;
import com.jeecms.core.entity.CmsRole;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.entity.CmsWorkflow;
import com.jeecms.core.entity.CmsWorkflowEvent;
import com.jeecms.core.entity.CmsWorkflowNode;
import com.jeecms.core.manager.CmsRoleMng;
import com.jeecms.core.manager.CmsWorkflowEventMng;
import com.jeecms.core.manager.CmsWorkflowEventUserMng;
import com.jeecms.core.manager.CmsWorkflowMng;
import com.jeecms.core.manager.CmsWorkflowRecordMng;
import com.jeecms.cms.entity.main.Content;
import com.jeecms.cms.manager.main.ChannelMng;
import com.jeecms.cms.manager.main.ContentMng;

@Service
@Transactional
public class CmsWorkflowMngImpl implements CmsWorkflowMng {
	public int check(CmsWorkflow workflow, CmsUser owner, CmsUser operator,Integer dateTypeId, Integer dataId, String opinion){
		CmsWorkflowEvent  event = workflowEventMng.find(dateTypeId, dataId);
		if (workflow == null) {
			// 没有工作流，存在流程轨迹 审核通过。
			if (event != null) {
				workflowRecordMng.save(event.getWorkFlow().getSite(), event, operator,  opinion, Calendar.getInstance().getTime(), CmsWorkflow.PASS);
				workflowEventMng.end(event.getId());
			}
			return -1;
		}
		//流程轨迹存在并且已经结束（文章终审结束不能在审核）
		if (event != null && event.getHasFinish()) {
			if(dateTypeId==Content.DATA_CONTENT){
				Content c=contentMng.findById(dataId);
				if(c.getStatus().equals(Content.ContentStatus.checked)){
					return 0;
				}
			}
		}
		//流程轨迹存在并且没有结束
		if (event != null && !event.getHasFinish()) {
			workflow = event.getWorkFlow();
		}
		List<CmsWorkflowNode> nodes = workflow.getNodes();
		int size = nodes.size();
		if (size <= 0) {
			// 工作流没有定义节点，审核通过。
			if (event != null) {
				workflowRecordMng.save(event.getWorkFlow().getSite(), event, operator, opinion, Calendar.getInstance().getTime(), CmsWorkflow.PASS);
				workflowEventMng.end(event.getId());
			}
			return -1;
		}
		//操作人在工作流节点中的最后位置
		int step;
		CmsWorkflowNode lastNode=nodes.get(nodes.size() - 1);
		for (step = size; step > 0; step--) {
			CmsWorkflowNode node = nodes.get(step - 1);
			CmsRole nodeRole=node.getRole();
			Set<CmsRole>roles=new HashSet<CmsRole>();
			roles.add(nodeRole);
			if (CollectionUtils.containsAny(roles,operator.getRoles())) {
				lastNode = node;
				break;
			}
		}
		//是否在工作流节点中
		//流程轨迹存在且没有结束且操作者步骤小于流程步骤，则不能操作，返回0（没有在工作流中）
		if (event != null && !event.getHasFinish() && step < event.getNextStep()) {
			return 0;
		}
		int nextStep;
		Set<CmsUser> nextUsers;
		Set<CmsUser> tempUsers;
		Date endDate;
		Boolean hasFinish;
		if (step == size) {
			if(lastNode.isCountersign()){
				if(event==null){
					nextUsers=nodes.get(step-1).getRole().getUsers();
					tempUsers=new HashSet<CmsUser>();
					tempUsers.addAll(nextUsers);
					tempUsers.remove(operator);
					nextUsers=tempUsers;
					nextStep = step ;
					endDate = null;
					hasFinish = false;
				}else{
					//会签模式 全部同意
					if(event.getPassNum()>=lastNode.getRole().getUsers().size()-1){
						nextUsers = null;
						nextStep = -1;
						endDate = Calendar.getInstance().getTime();
						hasFinish = true;
						event.setPassNum(0);
					}else{
						//会签模式 通过
						nextUsers=nodes.get(step-1).getRole().getUsers();
						tempUsers=new HashSet<CmsUser>();
						tempUsers.addAll(nextUsers);
						tempUsers.remove(operator);
						nextUsers=tempUsers;
						event.setPassNum(event.getPassNum()+1);
						nextStep = step ;
						endDate = null;
						hasFinish = false;
					}
				}
			}else{
				// 终审人员，审核结束。
				nextUsers = null;
				nextStep = -1;
				endDate = Calendar.getInstance().getTime();
				hasFinish = true;
			}
		} else {
			if(lastNode.isCountersign()){
				if(event==null){
					nextUsers=nodes.get(step-1).getRole().getUsers();
					tempUsers=new HashSet<CmsUser>();
					tempUsers.addAll(nextUsers);
					tempUsers.remove(operator);
					nextUsers=tempUsers;
					nextStep = step ;
					endDate = null;
					hasFinish = false;
				}else{
					//会签模式 全部同意 之后流转下个节点
					if(event.getPassNum()>=lastNode.getRole().getUsers().size()-1){
						nextUsers = nodes.get(step).getRole().getUsers();
						nextStep = step + 1;
						endDate = null;
						hasFinish = false;
						//流转下个节点处理当前节点处理用户数
						event.setPassNum(0);
					}else{
						//会签模式 通过
						nextUsers=nodes.get(step-1).getRole().getUsers();
						tempUsers=new HashSet<CmsUser>();
						tempUsers.addAll(nextUsers);
						tempUsers.remove(operator);
						nextUsers=tempUsers;
						event.setPassNum(event.getPassNum()+1);
						nextStep = step ;
						endDate = null;
						hasFinish = false;
					}
				}
			}else{
				// 普通流转 获得下一步审核人员
				nextUsers = nodes.get(step).getRole().getUsers();
				nextStep = step + 1;
				endDate = null;
				hasFinish = false;
			}
		}
		if (event != null) {
			event.setWorkFlow(workflow);
			event.setNextStep(nextStep);
			event.setEndTime(endDate);
			event.setHasFinish(hasFinish);
			//处理工作流下个节点用户
			workflowEventUserMng.update(event, nextUsers);
		} else {
			//保存工作流轨迹
			event=workflowEventMng.save(workflow, owner, nextUsers, dateTypeId, dataId, nextStep, hasFinish);
			//保存工作流下个节点用户
			workflowEventUserMng.save(event, nextUsers);
		}
		//审核记录
		workflowRecordMng.save(event.getWorkFlow().getSite(), event, operator, opinion, Calendar.getInstance().getTime(), CmsWorkflow.PASS);
		return nextStep;
	}

	public int reject(CmsWorkflow workflow, CmsUser owner, CmsUser operator,Integer dateTypeId, Integer dataId, String opinion){
		CmsWorkflowEvent  event = workflowEventMng.find(dateTypeId, dataId);
		if (workflow == null) {
			// 没有工作流，退回则直接打回。
			if (event != null) {
				workflowRecordMng.save(event.getWorkFlow().getSite(), event, operator, opinion, Calendar.getInstance().getTime(), CmsWorkflow.BACK);
				workflowEventMng.end(event.getId());
			}
			return -1;
		}
		//流程轨迹存在且没有结束
		if (event != null && !event.getHasFinish()) {
			workflow = event.getWorkFlow();
		}
		List<CmsWorkflowNode> nodes = workflow.getNodes();
		int size = nodes.size();
		if (size <= 0) {
			//工作流没有定义节点
			if (event != null) {
				workflowRecordMng.save(event.getWorkFlow().getSite(), event, operator, opinion, Calendar.getInstance().getTime(), CmsWorkflow.BACK);
				workflowEventMng.end(event.getId());
			}
			return -1;
		}
		//操作人在工作流节点中的首要位置
		int step;
		for (step = 0; step < size; step++) {
			CmsWorkflowNode node = nodes.get(step);
			CmsRole nodeRole=node.getRole();
			Set<CmsRole>roles=new HashSet<CmsRole>();
			roles.add(nodeRole);
			if (CollectionUtils.containsAny(roles,operator.getRoles())) {
				break;
			}
		}
		//不在审核流程中=0 在审核流程中的步骤step+1
		if (step == size) {
			step = 0;
		} else {
			step++;
		}
		//流程轨迹存在且未结束且非当前步骤人员
		if (event != null && !event.getHasFinish()) {
			if (step != event.getNextStep()) {
				return 0;
			}
		}else if ((event == null || event.getHasFinish()) && step != size) {
			//(流程轨迹不存在或者(流程轨迹已结束))&&非终审人员
			return 0;
		}
		boolean ownerRejected = false;
		if (step > 1) {
			for (int i = step - 2; i < size; i++) {
				CmsWorkflowNode node = nodes.get(i);
				if (node.getRole().getUsers().contains(owner)) {
					ownerRejected = true;
					break;
				}
			}
		}
		int nextStep;
		Set<CmsUser> nextUsers;
		Date endDate;
		Boolean hasFinish;
		// 第一个审核人员，退稿。
		if (step == 1 || ownerRejected) {
			nextUsers = null;
			nextStep = -1;
			endDate = Calendar.getInstance().getTime();
			hasFinish = true;
		} else {
			// 获得上个节点审核人员。
			nextUsers = nodes.get(step - 2).getRole().getUsers();
			nextStep = step - 1;
			endDate = null;
			hasFinish = false;
		}
		if (event != null) {
			event.setWorkFlow(workflow);
			event.setNextStep(nextStep);
			event.setEndTime(endDate);
			event.setHasFinish(hasFinish);
			//工作流下个节点用户数据更新
			workflowEventUserMng.update(event, nextUsers);
		} else {
			//保存工作流轨迹
			event=workflowEventMng.save(workflow, owner, nextUsers, dateTypeId, dataId, step, hasFinish);
			//保存工作流下个节点用户
			workflowEventUserMng.save(event, nextUsers);
		}
		//审核记录
		workflowRecordMng.save(event.getWorkFlow().getSite(), event, operator, opinion, Calendar.getInstance().getTime(), CmsWorkflow.BACK);
		return nextStep;
		
	}
	
	@Transactional(readOnly = true)
	public Pagination getPage(Integer siteId,int pageNo, int pageSize) {
		Pagination page = dao.getPage(siteId,pageNo, pageSize);
		return page;
	}
	
	@Transactional(readOnly = true)
	public List<CmsWorkflow> getList(Integer siteId,Boolean disabled){
		List<CmsWorkflow>workflows = dao.getList(siteId,disabled);
		return workflows;
	}

	@Transactional(readOnly = true)
	public CmsWorkflow findById(Integer id) {
		CmsWorkflow entity = dao.findById(id);
		return entity;
	}

	public CmsWorkflow save(CmsWorkflow bean, Integer[] roleIds, Integer[] countersigns) {
		bean=dao.save(bean);
		// 保存节点
		if (roleIds != null && roleIds.length > 0) {
			for (int i = 0, len = roleIds.length; i < len; i++) {
				if (roleIds[i]!=null) {
					boolean countersign=false;
					if(countersigns!=null&&countersigns[i]!=null&&countersigns[i].equals(1)){
						countersign=true;
					}
					bean.addToNodes(roleMng.findById(roleIds[i]), countersign);
				}
			}
		}
		return bean;
	}

	public CmsWorkflow update(CmsWorkflow bean, Integer[] roleIds, Integer[] countersigns) {
		Updater<CmsWorkflow> updater = new Updater<CmsWorkflow>(bean);
		bean = dao.updateByUpdater(updater);
		bean.getNodes().clear();
		// 保存节点
		if (roleIds != null && roleIds.length > 0) {
			for (int i = 0, len = roleIds.length; i < len; i++) {
				if (roleIds[i]!=null) {
					boolean countersign=false;
					if(countersigns!=null&&countersigns[i]!=null&&countersigns[i].equals(1)){
						countersign=true;
					}
					bean.addToNodes(roleMng.findById(roleIds[i]), countersign);
				}
			}
		}
		return bean;
	}

	public CmsWorkflow deleteById(Integer id) {
		CmsWorkflow bean = findById(id);
		//清除流程轨迹
		List<CmsWorkflowEvent>events=workflowEventMng.getListByWorkFlowId(id);
		for(CmsWorkflowEvent event:events){
			workflowEventMng.deleteById(event.getId());
		}
		//清空栏目设置流程
		channelMng.initWorkFlow(id);
		dao.deleteById(id);
		return bean;
	}

	public CmsWorkflow[] deleteByIds(Integer[] ids) {
		CmsWorkflow[] beans = new CmsWorkflow[ids.length];
		for (int i = 0, len = ids.length; i < len; i++) {
			beans[i] = deleteById(ids[i]);
		}
		return beans;
	}
	
	public void updatePriority(Integer[] ids, Integer[] priorities) {
		if (ids == null || priorities == null || ids.length <= 0
				|| ids.length != priorities.length) {
			return;
		}
		CmsWorkflow workflow;
		for (int i = 0, len = ids.length; i < len; i++) {
			workflow = findById(ids[i]);
			workflow.setPriority(priorities[i]);
		}
	}

	private CmsWorkflowDao dao;
	@Autowired
	private CmsRoleMng roleMng;
	@Autowired
	private ChannelMng channelMng;
	@Autowired
	private ContentMng contentMng;
	@Autowired
	private CmsWorkflowEventMng workflowEventMng;
	@Autowired
	private CmsWorkflowEventUserMng workflowEventUserMng;
	@Autowired
	private CmsWorkflowRecordMng workflowRecordMng;

	@Autowired
	public void setDao(CmsWorkflowDao dao) {
		this.dao = dao;
	}
}