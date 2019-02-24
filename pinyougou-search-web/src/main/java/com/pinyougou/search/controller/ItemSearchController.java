package com.pinyougou.search.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSearchService;

@RestController
@RequestMapping("/itemsearch")
public class ItemSearchController {
	
	@Reference
	private ItemSearchService itemSearchService;
	
	@RequestMapping("/search")
	public Map<String,Object> search(@RequestBody Map searchMap){
		return itemSearchService.search(searchMap);
	}
	
	@RequestMapping("/find")
	public Map<String,Object> find(@RequestBody Map searchMap){
		return itemSearchService.search(searchMap);
	}
	
}
