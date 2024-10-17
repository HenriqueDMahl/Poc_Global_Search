package com.poc.global.search.rest.controller;

import com.poc.global.search.rest.response.SearchResponse;
import com.poc.global.search.rest.vo.SearchVO;
import com.poc.global.search.service.search.SearchService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(SearchController.BASE_URL)
@AllArgsConstructor
public class SearchController {
	public static final String BASE_URL = "/search";

	@Autowired
	private SearchService searchService;

	@PostMapping
	@ResponseStatus(code = HttpStatus.OK)
	public SearchResponse searchText(@Valid @RequestBody SearchVO searchVO) {
		return searchService.find(searchVO);
	}
}
