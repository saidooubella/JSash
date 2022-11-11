package errors;

import span.TextSpan;

public final class ErrorMessage {
	
	public final String location;
	public final TextSpan span;
	public final String message;

	public ErrorMessage(String location, TextSpan span, String message) {
		this.span = span;
		this.message = message;
		this.location = location;
	}

	@Override
	public String toString() {
		final int columnNumber = span.start.column;
		final int lineNumber = span.start.line;
		return String.format("%s(%d, %d):%n%s", location, lineNumber, columnNumber, indent(message));
	}

	private static String indent(String message) {
		final StringBuilder builder = new StringBuilder();
		final String[] lines = message.split(System.lineSeparator());
		for (int i = 0; i < lines.length; i++) {
			if (i > 0) builder.append(System.lineSeparator());
			builder.append("    ");
			if (i == 0) builder.append("=> ");
			builder.append(lines[i]);
		}
		return builder.toString();
	}
}
