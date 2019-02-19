package com.pinyougou.shop.controller;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;

/**
 * controller
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll() {
		return goodsService.findAll();
	}

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows) {
		return goodsService.findPage(page, rows);
	}

	/**
	 * 增加
	 * 
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods) {

		// 获取登录名
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		goods.getGoods().setSellerId(sellerId);// 设置商家ID
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}

	/**
	 * 修改
	 * 
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods) {
		//校验是否当前商家ID
		TbGoods goodsSKU = goods.getGoods(); 
		Goods goods2 = goodsService.findOne(goodsSKU.getId());
		//获取当前登录的商家ID
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		//如果床底过来的商家id并不是当前登录的用户id，则属于非法操作
		if(!goods2.getGoods().getSellerId().equals(sellerId)||!goodsSKU.getSellerId().equals(sellerId)){
			return new Result(false,"操作非法");
		}
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}

	/**
	 * 获取实体
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id) {
		return goodsService.findOne(id);
	}

	/**
	 * 批量删除
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long[] ids) {
		try {
			goodsService.delete(ids);
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}

	/**
	 * 查询+分页
	 * 
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
		//获取商家ID
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		//添加查询条件
		goods.setSellerId(sellerId);
		return goodsService.findPage(goods, page, rows);
	}

	/**
	 * 更新商品上架状态
	 * @param ids
	 * @param isMarketable
	 * @return
	 */
	@RequestMapping("/updateIsMarketable")
	public Result updateIsMarketable(Long[] ids,String isMarketable){
		try {
			goodsService.updateIsMarketable(ids,isMarketable);
			return new Result(true,"更新成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"更新失败");
		}
	}
}
