package com.poc.global.search.rest.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchVO {
	private String termToSearch;
	private String searchType;
}
