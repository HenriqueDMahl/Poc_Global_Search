package com.poc.global.search.utils;

import opennlp.tools.tokenize.SimpleTokenizer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.poc.global.search.utils.Constants.STOP_WORDS;

@Component
public class TokenUtils {

	private static final CharsetEncoder ENCODER = Charset.forName("ISO_8859_1").newEncoder();

	/**
	 * Extrai tokens de uma string.
	 *
	 * @param str A string a ser tokenizada.
	 * @return Uma lista de tokens extraídos da string.
	 */
	public List<String> getTokens(String str) {
		/*
			O método getTokens realiza as seguintes operações:

			1. Tokeniza a string: Utiliza o SimpleTokenizer do OpenNLP para dividir a string em tokens (palavras).
			2. Filtra os tokens:
			   - Remove tokens com menos de 3 caracteres.
			   - Remove tokens que são numéricos.
			   - Remove tokens que estão na lista de stop words (STOP_WORDS).
			   - Remove tokens que não podem ser codificados no charset ISO_8859_1.
			3. Coleta os tokens filtrados: Retorna a lista de tokens resultantes.
		 */

		SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
		String[] tokens = tokenizer.tokenize(str.toLowerCase());

		// Filtra os tokens removendo tokens com menos de 3 caracteres, tokens numéricos, stop words e tokens que não podem ser codificados no charset ISO_8859_1
		return Arrays.stream(tokens)
				.filter(token -> token.length() > 2 && !StringUtils.isNumeric(token) && !STOP_WORDS.contains(token) && ENCODER.canEncode(token))
				.collect(Collectors.toList());
	}
}
