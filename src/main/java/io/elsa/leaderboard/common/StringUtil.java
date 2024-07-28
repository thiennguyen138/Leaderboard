package io.elsa.leaderboard.common;

import java.util.Collection;
import java.util.stream.Collectors;

public class StringUtil {

	public static String parseListStringToString(Collection<String> inputs) {
		return inputs.stream()
				.map(s -> "'" + s + "'")
				.collect(Collectors.joining(","));
	}
}