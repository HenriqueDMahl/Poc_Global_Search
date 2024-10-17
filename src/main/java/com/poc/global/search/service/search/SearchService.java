package com.poc.global.search.service.search;

import com.poc.global.search.rest.response.SearchResponse;
import com.poc.global.search.rest.vo.SearchVO;

public interface SearchService {
	SearchResponse find(SearchVO searchVO);
}
