package com.jeecms.cms.entity.main;

import com.jeecms.cms.entity.main.base.BaseContentShareCheck;



public class ContentShareCheck extends BaseContentShareCheck {
	private static final long serialVersionUID = 1L;
	/**
	 * 待审核
	 */
	public static final byte CHECKING = 0;
	/**
	 * 审核通过
	 */
	public static final byte CHECKED = 1;
	/**
	 * 推送
	 */
	public static final byte PUSHED = 2;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ContentShareCheck () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ContentShareCheck (java.lang.Integer id) {
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public ContentShareCheck (
		java.lang.Integer id,
		com.jeecms.cms.entity.main.Content content,
		com.jeecms.cms.entity.main.Channel channel,
		java.lang.Byte checkStatus,
		java.lang.Boolean shareValid) {

		super (
			id,
			content,
			channel,
			checkStatus,
			shareValid);
	}
	public void init() {
		byte status=0;
		if(getCheckStatus()==null){
			setCheckStatus(status);
		}
		if(getShareValid()==null){
			setShareValid(true);
		}
	}

/*[CONSTRUCTOR MARKER END]*/


}