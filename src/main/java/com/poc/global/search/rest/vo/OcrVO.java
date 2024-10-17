package com.poc.global.search.rest.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OcrVO {
	private String file;
	private int fileId;
}
