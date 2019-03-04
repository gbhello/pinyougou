package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		//关键字空格处理
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));
		
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
		
		//5.按价格筛选
		if(!"".equals(searchMap.get("price"))){
			String[] price = ((String) searchMap.get("price")).split("-");
			if(!price[0].equals("0")){//如果区间起点不等于0
				Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
				SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(simpleFilterQuery);
			}
			if(!price[1].equals("*")){//如果区间重点不等于*
				Criteria filterCriteriaUpper = new Criteria("item_price").lessThanEqual(price[1]);
				SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(filterCriteriaUpper);
				query.addFilterQuery(simpleFilterQuery);
			}
		}
		
		//6.分页查询
		Integer pageNo = (Integer) searchMap.get("pageNo");//提取页码
		if(pageNo==null){
			pageNo=1;//默认第一页
		}
		
		Integer pageSize = (Integer) searchMap.get("pageSize");//魅夜记录数
		if(pageSize==null){
			pageSize=20;//默认20
		}
		query.setOffset((pageNo-1)*pageSize);//从第几条记录查询
		query.setRows(pageSize);
		
		//7.排序
		String sortValue=(String) searchMap.get("sort");//ASC DESC
		String sortField=(String) searchMap.get("sortField");//排序字段
		if(sortValue!=null && !sortValue.equals("")){
			if(sortValue.equals("ASC")){
				Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
				query.addSort(sort);
			}
			if(sortValue.equals("DESC")){
				Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
				query.addSort(sort);
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
		map.put("totalPages", page.getTotalPages());//返回总页数
		map.put("total", page.getTotalElements());//返回总记录数
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
	
	public void show(){
		System.out.println("test"+System.currentTimeMillis());
	}

	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	@Override
	public void deleteByGoodsIds(List goodsIdList) {
		System.out.println("删除商品ID"+goodsIdList);
		SimpleQuery query = new SimpleQuery();
		Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
	

}
