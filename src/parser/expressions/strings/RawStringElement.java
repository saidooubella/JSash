package parser.expressions.strings;

import tokens.Token;

public final class RawStringElement extends StringElement {

	public final Token string;

	public RawStringElement(Token string) {
		this.string = string;
	}

	@Override
	public String toString() {
		return string.text;
	}
}
