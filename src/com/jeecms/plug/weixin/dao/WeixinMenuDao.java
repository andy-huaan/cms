package com.jeecms.plug.weixin.dao;

import java.util.List;

import com.jeecms.common.hibernate3.Updater;
import com.jeecms.common.page.Pagination;
import com.jeecms.plug.weixin.entity.WeixinMenu;

public interface WeixinMenuDao {
	
	public Pagination getPage(Integer siteId,Integer parentId,int pageNo,int pageSize);
	
	public WeixinMenu findById(Integer id);
	
	public List<WeixinMenu> getList(Integer siteId);
	
	public WeixinMenu save(WeixinMenu bean);

	public WeixinMenu updateByUpdater(Updater<WeixinMenu> updater);
	
	public WeixinMenu deleteById(Integer id);
}
