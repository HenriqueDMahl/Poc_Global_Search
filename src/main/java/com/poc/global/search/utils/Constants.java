package com.poc.global.search.utils;

import java.util.Set;

public class Constants {

	// Número de palavras a serem consideradas no cálculo do score.
	public static int LOOK_AHEAD = 1;

	/**
	 * Stop words em português, inglês e espanhol.
	 * Stop words são palavras que são filtradas antes ou depois do processamento de texto.
	 * Afim de melhorar a qualidade do texto processado.
	 *
	 * Nesse caso removeremos as stop words antes de tokenizar o texto.
	 */
	public static final Set<String> STOP_WORDS = Set.of(
			// Stop Word para Portugues https://www.ranks.nl/stopwords/portuguese
			"último", "acerca", "agora", "algmas", "alguns", "ali", "ambos", "antes", "apontar", "aquela", "aquelas", "aquele",
			"aqueles", "aqui", "atrás", "bem", "bom", "cada", "caminho", "cima", "com", "como", "comprido", "conhecido", "corrente",
			"das", "debaixo", "dentro", "desde", "desligado", "deve", "devem", "deverá", "direita", "diz", "dizer", "dois", "dos",
			"ela", "ele", "eles", "enquanto", "então", "está", "estão", "estado", "estar", "estará", "este", "estes", "esteve",
			"estive", "estivemos", "estiveram", "fará", "faz", "fazer", "fazia", "fez", "fim", "foi", "fora", "horas", "iniciar",
			"inicio", "irá", "ista", "iste", "isto", "ligado", "maioria", "maiorias", "mais", "mas", "mesmo", "meu", "muito",
			"muitos", "nós", "não", "nome", "nosso", "novo", "onde", "outro", "para", "parte", "pegar", "pelo", "pessoas",
			"pode", "poderá", "podia", "por", "porque", "povo", "promeiro", "quê", "qual", "qualquer", "quando", "quem",
			"quieto", "são", "saber", "sem", "ser", "seu", "somente", "têm", "tal", "também", "tem", "tempo", "tenho",
			"tentar", "tentaram", "tente", "tentei", "teu", "teve", "tipo", "tive", "todos", "trabalhar", "trabalho",
			"uma", "umas", "uns", "usa", "usar", "valor", "veja", "ver", "verdade", "verdadeiro", "você",

			// Stop Word para Ingles https://www.ranks.nl/stopwords
			"about", "above", "after", "again", "against", "all", "and", "any", "are", "aren't", "because", "been", "before",
			"being", "below", "between", "both", "but", "can't", "cannot", "could", "couldn't", "did", "didn't", "does", "doesn't",
			"doing", "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have",
			"haven't", "having", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how",
			"how's", "i'd", "i'll", "i'm", "i've", "into", "isn't", "it's", "its", "itself", "let's", "more", "most", "mustn't", "myself",
			"nor", "not", "off", "once", "only", "other", "ought", "our", "ours", "ourselves", "out", "over", "own", "same", "shan't",
			"she", "she'd", "she'll", "she's", "should", "shouldn't", "some", "such", "than", "that", "that's", "the", "their",
			"theirs", "them", "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've",
			"this", "those", "through", "too", "under", "until", "very", "was", "wasn't", "we'd", "we'll", "we're", "we've", "were",
			"weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's",
			"with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves",

			// Stop Word para Espanhol https://www.ranks.nl/stopwords/spanish
			"una", "unas", "unos", "uno", "sobre", "todo", "también", "tras", "otro", "algún", "alguno", "alguna", "algunos", "algunas",
			"soy", "eres", "somos", "sois", "estoy", "esta", "estamos", "estais", "estan", "atras", "por qué", "estaba", "ante", "siendo",
			"pero", "poder", "puede", "puedo", "podemos", "podeis", "pueden", "fui", "fue", "fuimos", "fueron", "hacer", "hago", "hace",
			"hacemos", "haceis", "hacen", "fin", "incluso", "conseguir", "consigo", "consigue", "consigues", "conseguimos", "consiguen",
			"voy", "vamos", "vais", "van", "vaya", "gueno", "tener", "tengo", "tiene", "tenemos", "teneis", "tienen", "las", "los",
			"mio", "tuyo", "ellos", "ellas", "nos", "nosotros", "vosotros", "vosotras", "solo", "solamente", "sabes", "sabe", "sabemos",
			"sabeis", "saben", "ultimo", "largo", "bastante", "haces", "muchos", "aquellos", "aquellas", "sus", "entonces", "tiempo",
			"verdad", "verdadero", "verdadera", "cierto", "ciertos", "cierta", "ciertas", "intentar", "intento", "intenta",	"intentas",
			"intentamos", "intentais", "intentan", "bajo", "arriba", "encima", "uso", "usas", "usamos", "usais", "usan", "emplear",
			"empleo", "empleas", "emplean", "ampleamos", "empleais", "muy", "era", "eras", "eramos", "eran", "modo", "bien", "cual",
			"cuando", "donde", "mientras", "quien", "con", "entre", "sin", "trabajo", "trabajar", "trabajas", "trabaja", "trabajamos",
			"trabajais", "trabajan", "podria", "podrias", "podriamos", "podrian", "podriais", "aquel"
	);
}
