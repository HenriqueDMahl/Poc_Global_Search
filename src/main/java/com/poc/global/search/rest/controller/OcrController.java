package com.poc.global.search.rest.controller;

import com.poc.global.search.rest.vo.OcrVO;
import com.poc.global.search.service.ocr.OcrService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping(OcrController.BASE_URL)
@AllArgsConstructor
public class OcrController {
	public static final String BASE_URL = "/ocr";

	@Autowired
	private OcrService ocrService;

	@PostMapping("/process")
	@ResponseStatus(code = HttpStatus.OK)
	public void createOcrForDocuments(@Valid @RequestBody OcrVO ocrVO) {
		ocrService.process(ocrVO);
	}

	@PostMapping("/upload")
	@ResponseStatus(code = HttpStatus.OK)
	public void uploadTextFile(@RequestParam("file") MultipartFile file, @RequestParam("fileId") int fileId) {
		if (file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
		}

		if (!Objects.equals(file.getContentType(), "text/plain")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File type is not supported");
		}

		try {
			ocrService.process(file, fileId);
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing file", e);
		}
	}
}
