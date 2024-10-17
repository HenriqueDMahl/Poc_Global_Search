package com.poc.global.search.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TokenUtils {

	/**
	 * Extrai tokens de uma string.
	 *
	 * @param str A string a ser tokenizada.
	 * @return Uma lista de tokens extraídos da string.
	 */
	public List<String> getTokens(String str) {
		// Remove pontuação e caracteres especiais
		str = str.replaceAll("[\\p{Punct}\\t\\n\\r]", " ").toLowerCase();

		CharsetEncoder encoder = Charset.forName("ISO_8859_1").newEncoder();

		// Tokeniza a string
		return Arrays.stream(str.split(" "))
				.filter(token -> token.length() > 2 && !StringUtils.isNumeric(token)  && encoder.canEncode(token))
				.collect(Collectors.toList());
	}
}
