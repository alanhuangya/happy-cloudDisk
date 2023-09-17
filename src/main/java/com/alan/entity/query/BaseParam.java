package com.alan.entity.query;


/**
 * 分页查询参数，包含分页信息和排序信息
 */
public class BaseParam {
	/**
	 * simplePage：分页信息
	 */
	private SimplePage simplePage;

	/**
	 * pageNo：当前页码
	 */
	private Integer pageNo;

	/**
	 * pageSize：每页显示条数
	 */
	private Integer pageSize;

	/**
	 * orderBy：排序字段
	 */
	private String orderBy;
	public SimplePage getSimplePage() {
		return simplePage;
	}

	public void setSimplePage(SimplePage simplePage) {
		this.simplePage = simplePage;
	}

	public Integer getPageNo() {
		return pageNo;
	}

	public void setPageNo(Integer pageNo) {
		this.pageNo = pageNo;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public void setOrderBy(String orderBy){
		this.orderBy = orderBy;
	}

	public String getOrderBy(){
		return this.orderBy;
	}
}
