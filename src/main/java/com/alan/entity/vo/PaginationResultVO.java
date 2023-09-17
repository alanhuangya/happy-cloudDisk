package com.alan.entity.vo;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页结果集
 * @param <T>
 */
public class PaginationResultVO<T> {
	/**
	 * 总条数
	 */
	private Integer totalCount;

	/**
	 * 每页显示条数
	 */
	private Integer pageSize;
	private Integer pageNo;
	private Integer pageTotal;
	private List<T> list = new ArrayList<T>();

	/**
	 * 分页结果集
	 * @param totalCount
	 * @param pageSize
	 * @param pageNo
	 * @param list
	 */
	public PaginationResultVO(Integer totalCount, Integer pageSize, Integer pageNo, List<T> list) {
		this.totalCount = totalCount;
		this.pageSize = pageSize;
		this.pageNo = pageNo;
		this.list = list;
	}

    public PaginationResultVO(Integer totalCount, Integer pageSize, Integer pageNo, Integer pageTotal, List<T> list) {
        if (pageNo == 0) {
            pageNo = 1;
        }
        this.totalCount = totalCount;
        this.pageSize = pageSize;
        this.pageNo = pageNo;
        this.pageTotal = pageTotal;
        this.list = list;
    }

	public PaginationResultVO(List<T> list) {
		this.list = list;
	}

	public PaginationResultVO() {

	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getPageNo() {
		return pageNo;
	}

	public void setPageNo(Integer pageNo) {
		this.pageNo = pageNo;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public Integer getPageTotal() {
        return pageTotal;
    }

    public void setPageTotal(Integer pageTotal) {
        this.pageTotal = pageTotal;
    }
}
