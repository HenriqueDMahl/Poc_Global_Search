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

	/**
	 * Processa um objeto OcrVO e extrai os tokens.
	 * Caso o arquivo seja maior que o tamanho máximo permitido (5 MB), divide o arquivo em partes menores e processa cada parte.
	 *
	 * Essa chamada é feita para clientes Cloud.
	 *
	 * @param ocrVO O objeto que contém o arquivo e o ID do arquivo.
	 */
	@PostMapping("/process")
	@ResponseStatus(code = HttpStatus.OK)
	public void createOcrForDocuments(@Valid @RequestBody OcrVO ocrVO) {
		if (ocrVO.getFile().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
		}

		try {
			ocrService.process(ocrVO);
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing file", e);
		}
	}

	/**
	 * Processa um arquivo MultipartFile e extrai os tokens.
	 * Caso o arquivo seja maior que o tamanho máximo permitido (5 MB), divide o arquivo em partes menores e processa cada parte.
	 *
	 * Essa chamada é feita para clientes On-Premise.
	 *
	 * @param file O arquivo a ser processado.
	 * @param fileId O ID do arquivo.
	 */
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
