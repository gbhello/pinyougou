package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout=600000)
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 根据商品分类名称查询品牌列表和规格列表
	 * @param category
	 * @return
	 */
	private Map searchBrandAndSpecList(String category){
		HashMap map = new HashMap();
		Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);	//根据商品分类名称查询分类ID
		if(typeId != null){		//如果不为空
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);	//查询商品分类所对应的品牌列表
			map.put("brandList", brandList);	//将查询出来的品牌列表放入map存储
			List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);		//查询商品分类对应的规格列表
			map.put("specList", specList);
		}
		return map;
	}

	@Override
	public Map<String, Object> search(Map searchMap) {
		Map<String, Object> map = new HashMap<>();
		
		//1.查询列表
		map.putAll(searchList(searchMap));
		
		//2.根据关键字查询商品分类
		List categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		
		//3.根据商品分类查询品牌和规格列表
		String categoryName = (String) searchMap.get("category");
		if(!"".equals(categoryName)){//如果呦分类名称
			map.putAll(searchBrandAndSpecList(categoryName));
		}else{//如果没有分类名称，按照第一个查询
			if(categoryList.size()>0){
				String category = (String) categoryList.get(0);
				map.putAll(searchBrandAndSpecList(category));
			}
		}
		return map;
	}

	/**
	 * 根据关键字搜索列表
	 * 
	 * @param searchMap
	 * @return
	 */
	private Map searchList(Map searchMap) {
		Map map = new HashMap();
		HighlightQuery query = new SimpleHighlightQuery();
		HighlightOptions highlightOptions = new HighlightOptions()
				.addField("item_title");// 设置高亮的域
		highlightOptions.setSimplePrefix("<em style='color:red'>");// 高亮前缀
		highlightOptions.setSimplePostfix("</em>");// 高亮后缀
		query.setHighlightOptions(highlightOptions);// 设置高亮选项
		// 1.按照关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap
				.get("keywords"));
		query.addCriteria(criteria);
		
		//2.按分类筛选
		if(!"".equals(searchMap.get("category"))){
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			SimpleFilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		
		//3.按品牌筛选
		if(!"".equals(searchMap.get("brand"))){
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			FilterQuery simpleFilterQuery = new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(simpleFilterQuery);
		}
		
		//4.过滤规格
		if(searchMap.get("spec")!=null){
			Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");
			for (String key : specMap.keySet()) {
				Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
				SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(simpleFilterQuery);
			}
		}
		
		//高亮显示处理
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query,
				TbItem.class);
		// 循环高亮入口集合
		for (HighlightEntry<TbItem> h : page.getHighlighted()) {
			TbItem item = h.getEntity();// 获取原实体类
			if (h.getHighlights().size() > 0 && h.getHighlights().get(0).getSnipplets().size() > 0) {
				item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));// 设置高亮的结果
			}

		}
		map.put("rows", page.getContent());
		return map;
	}
	
	/**
	 * 查询分类列表
	 * @param searchMap
	 * @return
	 */
	private List searchCategoryList(Map searchMap){
		List<String> list = new ArrayList();
		Query query = new SimpleQuery();
		//按照关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//设置分组选项
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions);
		//得到分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		//根据列得到分组结果集
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//得到分组结果入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		//得到分组入口集合
		List<GroupEntry<TbItem>> content = groupEntries.getContent();
		for (GroupEntry<TbItem> entry : content) {
			list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中
		}
		
		return list;
	}
	

}
