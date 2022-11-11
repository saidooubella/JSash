package parser.expressions.strings;

import tokens.Token;

public final class IdentifierStringElement extends StringElement {

	public final Token identifier;

	public IdentifierStringElement(Token identifier) {
		this.identifier = identifier;
	}

	@Override
	public String toString() {
		return identifier.text;
	}
}
