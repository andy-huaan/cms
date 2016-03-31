package com.jeecms.core.entity;

import java.util.ArrayList;
import java.util.List;

import com.jeecms.core.entity.base.BaseCmsWorkflow;



public class CmsWorkflow extends BaseCmsWorkflow {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 通过
	 */
	public static int PASS=1;
	/**
	 * 退回
	 */
	public static int BACK=2;
	/**
	 * 保持
	 */
	public static int KEEP=3;
	
	public boolean getDisabled () {
		return super.isDisabled();
	}
	
	public void addToNodes(CmsRole role, boolean countersign) {
		List<CmsWorkflowNode> list =getNodes();
		if (list == null) {
			list = new ArrayList<CmsWorkflowNode>();
			setNodes(list);
		}
		CmsWorkflowNode node = new CmsWorkflowNode();
		node.setRole(role);
		node.setCountersign(countersign);
		list.add(node);
	}

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CmsWorkflow () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CmsWorkflow (java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public CmsWorkflow (
		java.lang.Integer id,
		com.jeecms.core.entity.CmsSite site,
		java.lang.String name,
		java.lang.Integer priority,
		boolean disabled) {

		super (
			id,
			site,
			name,
			priority,
			disabled);
	}

/*[CONSTRUCTOR MARKER END]*/


}