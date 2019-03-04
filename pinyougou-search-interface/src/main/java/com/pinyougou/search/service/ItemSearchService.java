package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

	/**
	 * 物品搜索
	 * @param searchMap
	 * @return
	 */
	public Map<String,Object> search(Map searchMap);
	
	/**
	 * 导入索引数据
	 * @param list
	 */
	public void importList(List list);
	
	/**
	 * 删除数据
	 * @param goodsIdList
	 */
	public void deleteByGoodsIds(List goodsIdList);
}
