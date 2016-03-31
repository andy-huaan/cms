package com.jeecms.core.entity;

import java.util.Calendar;

import com.jeecms.cms.entity.main.Content;
import com.jeecms.core.entity.base.BaseCmsWorkflowEvent;



public class CmsWorkflowEvent extends BaseCmsWorkflowEvent {
	private static final long serialVersionUID = 1L;
	public void init(){
		if(getStartTime()==null){
			setStartTime(Calendar.getInstance().getTime());
		}
		if(getDateType()==null){
			setDateType(Content.DATA_CONTENT);
		}
		if(getHasFinish()==null){
			setHasFinish(false);
		}
		//会签第一人建立流程轨迹 通过数为1
		if(getPassNum()==null){
			setPassNum(0);
		}
	}

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CmsWorkflowEvent () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CmsWorkflowEvent (java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public CmsWorkflowEvent (
		java.lang.Integer id,
		com.jeecms.core.entity.CmsWorkflow workFlow,
		com.jeecms.core.entity.CmsUser initiator,
		java.lang.Integer dateId,
		java.util.Date startTime,
		java.lang.Integer nextStep,
		java.lang.Integer dateType,
		java.lang.Boolean hasFinish) {

		super (
			id,
			workFlow,
			initiator,
			dateId,
			startTime,
			nextStep,
			dateType,
			hasFinish);
	}

/*[CONSTRUCTOR MARKER END]*/


}