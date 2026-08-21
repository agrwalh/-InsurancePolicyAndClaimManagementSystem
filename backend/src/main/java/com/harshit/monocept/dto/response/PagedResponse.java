package com.harshit.monocept.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponse<T> {

	private List<T> records;
	private int currentPage;
	private int pageSize;
	private long totalRecords;
	private int totalPages;
	private boolean lastPage;
	private String sortField;
	private String sortDirection;

	public static <T> PagedResponse<T> from(Page<T> page, String sortField, String sortDirection) {
		return PagedResponse.<T>builder().records(page.getContent()).currentPage(page.getNumber())
				.pageSize(page.getSize()).totalRecords(page.getTotalElements()).totalPages(page.getTotalPages())
				.lastPage(page.isLast()).sortField(sortField).sortDirection(sortDirection.toLowerCase()).build();
	}
}