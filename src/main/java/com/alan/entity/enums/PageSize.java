package com.alan.entity.enums;


/**
 * 分页大小枚举
 */
public enum PageSize {
	/**
	 * SIZExx(xx)中的xx代表每页显示的条数
	 */
	SIZE15(15), SIZE20(20), SIZE30(30), SIZE40(40), SIZE50(50);

	/**
	 * 每页显示的条数
	 */
	int size;

	private PageSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return this.size;
	}
}
