package com.pinyougou.search.service;

import java.util.Map;

public interface ItemSearchService {

	/**
	 * 物品搜索
	 * @param searchMap
	 * @return
	 */
	public Map<String,Object> search(Map searchMap);
}
