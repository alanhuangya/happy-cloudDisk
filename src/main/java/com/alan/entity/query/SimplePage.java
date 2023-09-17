package com.alan.entity.query;
import com.alan.entity.enums.PageSize;

/**
 * 分页
 */
public class SimplePage {
	/**
	 * pageNo：当前页码
	 */
	private int pageNo;

	/**
	 * countTotal：总条数
	 */
	private int countTotal;

	/**
	 * pageSize：每页显示条数
	 */
	private int pageSize;

	/**
	 * pageTotal：总页数
	 */
	private int pageTotal;

	/**
	 * start：开始条数
	 */
	private int start;

	/**
	 * end：结束条数
	 */
	private int end;

	public SimplePage() {
	}

	public SimplePage(Integer pageNo, int countTotal, int pageSize) {
		if (null == pageNo) {
			pageNo = 0;
		}
		this.pageNo = pageNo;
		this.countTotal = countTotal;
		this.pageSize = pageSize;
		action();
	}

	public SimplePage(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public void action() {
		if (this.pageSize <= 0) {
			this.pageSize = PageSize.SIZE20.getSize();
		}
		if (this.countTotal > 0) {
			this.pageTotal = this.countTotal % this.pageSize == 0 ? this.countTotal / this.pageSize
					: this.countTotal / this.pageSize + 1;
		} else {
			pageTotal = 1;
		}

		if (pageNo <= 1) {
			pageNo = 1;
		}
		if (pageNo > pageTotal) {
			pageNo = pageTotal;
		}
		this.start = (pageNo - 1) * pageSize;
		this.end = this.pageSize;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public int getPageTotal() {
		return pageTotal;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public void setPageTotal(int pageTotal) {
		this.pageTotal = pageTotal;
	}

	public int getCountTotal() {
		return countTotal;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public void setCountTotal(int countTotal) {
		this.countTotal = countTotal;
		this.action();
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

}
