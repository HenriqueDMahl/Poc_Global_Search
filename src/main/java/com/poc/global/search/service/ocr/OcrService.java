package com.poc.global.search.service.ocr;

import com.poc.global.search.rest.vo.OcrVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface OcrService {

	void process(OcrVO ocrVO) throws IOException;

	void process(MultipartFile file, int fileId) throws IOException;
}
