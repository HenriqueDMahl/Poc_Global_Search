package com.poc.global.search.service.ocr;

import com.poc.global.search.entity.Tokens;
import com.poc.global.search.repository.OcrRepository;
import com.poc.global.search.rest.vo.OcrVO;
import com.poc.global.search.utils.TokenUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.poc.global.search.utils.Constants.LOOK_AHEAD;

@Slf4j
@Service
@AllArgsConstructor
public class OcrImpl implements OcrService {

	private OcrRepository ocrRepository;
	private TokenUtils tokenUtils;

	// Tamanho máximo permitido para processamento de arquivos
	private static final int MAX_SIZE = 5 * 1024 * 1024; // 5MB

	/**
	 * Processa um arquivo MultipartFile e extrai os tokens.
	 *
	 * @param file O arquivo a ser processado.
	 * @param fileId O ID do arquivo.
	 * @throws IOException Se ocorrer um erro ao ler o arquivo.
	 */
	@Override
	public void process(MultipartFile file, int fileId) throws IOException {
		long size = file.getSize();

		if (size <= MAX_SIZE)
			processWithInputStream(file.getInputStream(), fileId);
		else
			splitProcess(file.getInputStream(), fileId);
	}

	/**
	 * Processa um objeto OcrVO e extrai os tokens.
	 * Caso o arquivo seja maior que o tamanho máximo permitido, divide o arquivo em partes menores e processa cada parte.
	 *
	 * @param ocrVO O objeto que contém o arquivo e o ID do arquivo.
	 */
	public void process(OcrVO ocrVO) throws IOException {
		String file = ocrVO.getFile();
		int fileId = ocrVO.getFileId();
		byte[] bytes = file.getBytes(StandardCharsets.UTF_8);

		if (bytes.length > MAX_SIZE)
			splitProcess(new ByteArrayInputStream(bytes), fileId);
		else
			process(file, fileId);
	}

	/**
	 * Processa uma string e extrai os tokens.
	 *
	 * @param file A string a ser processada.
	 * @param fileId O ID do arquivo.
	 */
	private void process(String file, int fileId) {

		long startTime = System.currentTimeMillis();

		List<String> tokens = tokenUtils.getTokens(file);

		Map<String, Tokens> tokenEntities = ocrRepository.findAllById(tokens)
				.stream()
				.collect(Collectors.toMap(Tokens::getToken, Function.identity()));

		for (int i = 0; i < tokens.size(); i++) {

			String token = tokens.get(i);
			String lookAheadString = "";

			if (i + LOOK_AHEAD < tokens.size())
				lookAheadString = tokens.get(i + LOOK_AHEAD);

			saveOrUpdateToken(token, lookAheadString, fileId, tokenEntities);
		}

		ocrRepository.saveAll(tokenEntities.values());

		long endTime = System.currentTimeMillis();
		long processingTime = endTime - startTime;

		log.info("Processing time for method process: " + processingTime + "ms");
	}

	/**
	 * Caso o arquivo seja maior que o tamanho máximo permitido, divide o arquivo em partes menores e processa cada parte.
	 *
	 * @param in O InputStream a ser processado.
	 * @param fileId O ID do arquivo.
	 * @throws IOException Se ocorrer um erro ao ler o InputStream.
	 */
	private void splitProcess(InputStream in, int fileId) throws IOException {
		final byte[] buffer = new byte[MAX_SIZE];
		int dataRead;

		try (BufferedInputStream bis = new BufferedInputStream(in)) {
			while ((dataRead = bis.read(buffer)) != -1) {
				processWithInputStream(new ByteArrayInputStream(buffer, 0, dataRead), fileId);
			}
		}
	}

	/**
	 * Processa um InputStream e extrai os tokens.
	 *
	 * @param file O InputStream a ser processado.
	 * @param fileId O ID do arquivo.
	 * @throws IOException Se ocorrer um erro ao ler o InputStream.
	 */
	private void processWithInputStream(InputStream file, int fileId) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		String line;

		try (BufferedReader br = new BufferedReader(new InputStreamReader(file, StandardCharsets.UTF_8))) {
			while ((line = br.readLine()) != null) {
				stringBuilder.append(line).append(' ');
			}
		}

		process(stringBuilder.toString(), fileId);
	}

	/**
	 * Salva ou atualiza um token no banco de dados.
	 *
	 * @param token O token a ser salvo ou atualizado.
	 * @param lookAheadString A string de look-ahead associada ao token.
	 * @param fileId O ID do arquivo.
	 * @param tokenEntities O mapa de entidades de token.
	 */
	private void saveOrUpdateToken(String token, String lookAheadString, int fileId, Map<String, Tokens> tokenEntities) {
		Tokens tokenEntity = tokenEntities.getOrDefault(token, Tokens.builder().token(token).filesIds(new HashMap<>()).build());

		HashMap<Integer, List<String>> filesIds = tokenEntity.getFilesIds();
		if (filesIds.containsKey(fileId)) {
			if (!filesIds.get(fileId).contains(lookAheadString))
				filesIds.get(fileId).add(lookAheadString);
		} else {
			filesIds.put(fileId, new ArrayList<>(Collections.singletonList(lookAheadString)));
		}

		tokenEntities.put(token, tokenEntity);
	}

}
