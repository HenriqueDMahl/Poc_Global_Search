package com.poc.global.search.service.search;

import com.poc.global.search.entity.Tokens;
import com.poc.global.search.enumerator.SearchTypes;
import com.poc.global.search.repository.OcrRepository;
import com.poc.global.search.rest.response.SearchResponse;
import com.poc.global.search.rest.vo.SearchVO;
import com.poc.global.search.utils.TokenUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.poc.global.search.utils.Constants.LOOK_AHEAD;

@Slf4j
@Service
@AllArgsConstructor
public class SearchImpl implements SearchService {

	private OcrRepository ocrRepository;
	private TokenUtils tokenUtils;

	/**
	 * Processa a busca de um termo em um arquivo.
	 *
	 * @param searchVO O objeto que contém o termo a ser buscado.
	 * @return Um objeto SearchResponse que contém o resultado da busca.
	 */
	@Override
	public SearchResponse find(SearchVO searchVO) {
		long startTime = System.currentTimeMillis();

		List<String> tokens = tokenUtils.getTokens(searchVO.getTermToSearch());
		int[] filesIdsIntersection;

		String searchType = searchVO.getSearchType();

		switch (SearchTypes.valueOf(searchType.toUpperCase())) {
			case ANY -> filesIdsIntersection = anyOrderSearch(tokens);
			case EXACT -> filesIdsIntersection = exactSearch(tokens);
			default -> {
				log.error("Tipo de busca não suportado: " + searchType);
				return SearchResponse.builder().searchResult(new int[0]).build();
			}
		}

		long endTime = System.currentTimeMillis();
		long processingTime = endTime - startTime;

		log.info("Processing time for method find using the " + searchType + " type search: " + processingTime + "ms");

		return SearchResponse.builder().searchResult(filesIdsIntersection).build();
	}

	/**
	 * Realiza uma busca exata de um termo em um arquivo.
	 * Funciona como um search exato.
	 * Retorna os arquivos que contêm o termo buscado na exata ordem informados.
	 *
	 * @param tokens A lista de tokens a serem buscados.
	 * @return Um array de inteiros que representa os IDs dos arquivos que contêm o termo buscado.
	 */
	private int[] exactSearch(List<String> tokens) {

		Map<String, Tokens> tokensMap = ocrRepository.findAllById(tokens)
				.stream()
				.collect(Collectors.toMap(Tokens::getToken, Function.identity()));

		Tokens firstToken = tokensMap.get(tokens.get(0));
		if (firstToken == null)
			return new int[0];

		Set<Integer> filesIdsIntersection = new HashSet<>(firstToken.getFilesIds().keySet());

		for (int i = 1; i < tokens.size(); i++) {
			String token = tokens.get(i);
			Tokens filesIds = tokensMap.get(token);

			if (filesIds == null) {
				// Se não encontrar o token, retorna um array vazio, pois não existe filesIds para a busca
				return new int[0];
			}

			HashMap<Integer, List<String>> filesIdsMap = filesIds.getFilesIds();

			filesIdsIntersection.retainAll(filesIdsMap.keySet());

			// Valida se o lookAheadStringFromSearch está contido no lookAheadStringFromFile
			String lookAheadStringFromSearch = i + LOOK_AHEAD < tokens.size() ? tokens.get(i + LOOK_AHEAD) : "";
			validateWithLookAhead(filesIdsIntersection, lookAheadStringFromSearch, filesIdsMap);

			// Se o resultado da interseção for menor ou igual 1, não é necessário continuar a busca
			if (filesIdsIntersection.size() <= 1)
				break;
		}

		return filesIdsIntersection.stream().mapToInt(i -> i).toArray();
	}

	/**
	 * Realiza uma busca em qualquer ordem de um termo em um arquivo.
	 * Funciona como um wildcard search.
	 * Retorna os arquivos que contêm o termo buscado em qualquer ordem.
	 *
	 * @param tokens A lista de tokens a serem buscados.
	 * @return Um array de inteiros que representa os IDs dos arquivos que contêm o termo buscado.
	 */
	private int[] anyOrderSearch(List<String> tokens) {
		Map<String, Tokens> tokensMap = ocrRepository.findAllById(tokens)
				.stream()
				.collect(Collectors.toMap(Tokens::getToken, Function.identity()));

		Set<Integer> filesIdsIntersection = null;

		for (String token : tokens) {
			Tokens filesIds = tokensMap.get(token);
			if (filesIds != null) {
				Set<Integer> filesIdsKeySet = filesIds.getFilesIds().keySet();
				if (filesIdsIntersection == null) {
					filesIdsIntersection = new HashSet<>(filesIdsKeySet);
				} else {
					filesIdsIntersection.retainAll(filesIdsKeySet);

					// Se o resultado da interseção for menor ou igual 1, não é necessário continuar a busca
					if (filesIdsIntersection.size() <= 1)
						break;
				}
			} else {
				// Se não encontrar o token, retorna um array vazio, pois não existe filesIds para a busca
				return new int[0];
			}
		}

		if (filesIdsIntersection == null)
			return new int[0];

		return filesIdsIntersection.stream().mapToInt(i -> i).toArray();
	}

	/**
	 * Valida se o lookAheadStringFromSearch está contido no lookAheadStringFromFile.
	 * Caso não esteja, remove o fileId da interseção.
	 *
	 * @param filesIdsIntercection A interseção dos IDs dos arquivos.
	 * @param lookAheadStringFromSearch O look-ahead string da busca.
	 * @param filesIdsMap O mapa de IDs dos arquivos.
	 */
	private void validateWithLookAhead(Set<Integer> filesIdsIntercection, String lookAheadStringFromSearch, HashMap<Integer, List<String>> filesIdsMap) {
		// Caso não encontre o lookAheadStringFromSearch em algum dos arquivos, remove o arquivo da interseção
		if (!lookAheadStringFromSearch.isEmpty()) {
			// Se o lookAheadStringFromSearch não estiver contido no lookAheadStringFromFile, remove o fileId da interseção
			filesIdsIntercection.removeIf(fileId -> !filesIdsMap.get(fileId).contains(lookAheadStringFromSearch));
		}
	}
}
