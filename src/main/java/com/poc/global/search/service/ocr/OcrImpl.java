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
		if (size <= MAX_SIZE) {
			processFile(file.getInputStream(), fileId);
		} else {
			final byte[] buffer = new byte[MAX_SIZE];
			InputStream in = file.getInputStream();
			int dataRead = in.read(buffer);
			while (dataRead > -1) {
				processFile(new ByteArrayInputStream(buffer, 0, dataRead), fileId);
				dataRead = in.read(buffer);
			}
		}
	}

	private void processFile(InputStream file, int fileId) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		String line;

		try (BufferedReader br = new BufferedReader(new InputStreamReader(file, StandardCharsets.UTF_8))) {
			while ((line = br.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(System.lineSeparator());
			}
		}

		process(stringBuilder.toString(), fileId);
	}

	/**
	 * Processa um objeto OcrVO e extrai os tokens.
	 *
	 * @param ocrVO O objeto que contém o arquivo e o ID do arquivo.
	 */
	public void process(OcrVO ocrVO) {
		process(ocrVO.getFile(), ocrVO.getFileId());
	}

	/**
	 * Processa uma string e extrai os tokens.
	 *
	 * @param file A string a ser processada.
	 * @param fileId O ID do arquivo.
	 */
	public void process(String file, int fileId) {

		long startTime = System.currentTimeMillis();

		TokenUtils tokenUtils = new TokenUtils();
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
