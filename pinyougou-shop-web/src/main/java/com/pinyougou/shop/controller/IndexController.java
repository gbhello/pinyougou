package com.pinyougou.shop.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class IndexController {

	@RequestMapping("/name")
	public Map name(){
		String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
		Map map = new HashMap();
		map.put("loginName", loginName);
		return map;
	}
}
