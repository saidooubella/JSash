package tokens;

import span.Spannable;
import span.TextSpan;

public final class Token implements Spannable {

	private final TextSpan span;
	
	public final Object value;
	public final TokenType type;
	public final String text;

	public Token(String text, TokenType type, Object value, TextSpan span) {
		this.value = value;
		this.type = type;
		this.text = text;
		this.span = span;
	}

	@Override
	public TextSpan span() {
		return span;
	}

	@Override
	public String toString() {
		return "Token(text=" + text + ", type=" + type + ", value=" + value + ")";
	}
}
