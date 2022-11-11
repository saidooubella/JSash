package utils;

import java.util.regex.Pattern;

public final class Validator {

	private static final String IDENTIFIER = "\\p{L}[\\p{L}\\p{N}]*";
	private static final String EXTENSION = "\\.[Ss][Aa][Ss][Hh]";
	private static final String PARENT_FOLDER = "\\.\\.";
	private static final String THIS_FOLDER = "\\.";
	private static final String SEPARATOR = "/";

	private static final Pattern IMPORT_PATH_PATTERN = Pattern.compile(
		"^((" + THIS_FOLDER + "|" + PARENT_FOLDER + "|" + IDENTIFIER + ")" + SEPARATOR + ")*" + IDENTIFIER + EXTENSION + "$"
	);
}
