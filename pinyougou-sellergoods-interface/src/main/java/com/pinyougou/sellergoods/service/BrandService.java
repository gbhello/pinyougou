package com.pinyougou.sellergoods.service;

import java.util.List;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

/**
 * 品牌服务层接口
 * 
 * @author gengbin
 *
 */
public interface BrandService {
	/**
	 * 根据除”每页条数“和”当前页数“之外的变量查询的分页方法
	 * 
	 * @param brand
	 * @param pageNum当前页码
	 * @param pageSize每页记录数
	 * @return
	 */
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize);

	/**
	 * 批量删除
	 * 
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	public List<TbBrand> findAll();

	/**
	 * 返回分页列表
	 * 
	 * @param pageNum
	 *            当前页面
	 * @param pageSize
	 *            每页记录数
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);

	/**
	 * 增加
	 * 
	 * @param brand
	 */
	public void add(TbBrand brand);

	/**
	 * 修改
	 * 
	 * @param brand
	 */
	public void update(TbBrand brand);

	/**
	 * 根据id获取实体
	 * 
	 * @param id
	 * @return
	 */
	public TbBrand findOne(Long id);
}
