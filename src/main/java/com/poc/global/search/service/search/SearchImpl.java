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
		int[] filesIdsIntercection = null;

		String searchType = searchVO.getSearchType();

		if (SearchTypes.ANY.toString().equalsIgnoreCase(searchType))
			filesIdsIntercection = anyOrderSearch(tokens);
		else if (SearchTypes.EXACT.toString().equalsIgnoreCase(searchType))
			filesIdsIntercection = exactSearch(tokens);
		else
			log.error("Tipo de busca não suportado: " + searchType);


		long endTime = System.currentTimeMillis();
		long processingTime = endTime - startTime;

		log.info("Processing time for method find using the " + searchType + " type search: " + processingTime + "ms");

		return SearchResponse.builder().searchResult(filesIdsIntercection).build();
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

		Set<Integer> filesIdsIntersection = new HashSet<>(tokensMap.get(tokens.get(0)).getFilesIds().keySet());

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

	private int[] exactSearchOld(List<String> tokens) {

		Set<Integer> filesIdsIntercection = null;
		String lookAheadStringFromSearch;

		Map<String, Tokens> tokensMap = ocrRepository.findAllById(tokens)
				.stream()
				.collect(Collectors.toMap(Tokens::getToken, Function.identity()));

		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			Tokens filesIds = tokensMap.get(token);

			if (filesIds == null) {
				// Se não encontrar o token, retorna um array vazio, pois não existe filesIds para a busca
				return new int[0];
			}

			HashMap<Integer, List<String>> filesIdsMap = filesIds.getFilesIds();

			if (filesIdsIntercection == null) {
				filesIdsIntercection = filesIdsMap.keySet();
			} else {

				//Se o token atual não for o último, pega o próximo token
				if (i + LOOK_AHEAD < tokens.size())
					lookAheadStringFromSearch = tokens.get(i + LOOK_AHEAD);
				else
					lookAheadStringFromSearch = "";

				filesIdsIntercection = getIntersection(filesIdsIntercection, filesIdsMap.keySet());

				// Valida se o lookAheadStringFromSearch está contido no lookAheadStringFromFile
				validateWithLookAhead(filesIdsIntercection, lookAheadStringFromSearch, filesIdsMap);

				//Se o resultado da interseção for menor ou igual 1, não é necessário continuar a busca
				if (filesIdsIntercection.size() <= 1)
					break;
			}
		}

		if (filesIdsIntercection == null)
			return new int[0];

		return filesIdsIntercection.stream().mapToInt(i -> i).toArray();
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
			}
		}

		if (filesIdsIntersection == null)
			return new int[0];

		return filesIdsIntersection.stream().mapToInt(i -> i).toArray();
	}

	/**
	 * Obtém a interseção de dois conjuntos de IDs de arquivos.
	 *
	 * @param array1 O primeiro conjunto de IDs de arquivos.
	 * @param array2 O segundo conjunto de IDs de arquivos.
	 * @return Um conjunto que representa a interseção dos dois conjuntos de IDs de arquivos.
	 */
	private Set<Integer> getIntersection(Set<Integer> array1, Set<Integer> array2) {
		Set<Integer> intersection = new HashSet<>(array1);
		intersection.retainAll(array2);
		return intersection;
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
