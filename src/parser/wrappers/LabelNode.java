package parser.wrappers;

import span.Spannable;
import span.TextSpan;
import tokens.Token;

public final class LabelNode implements Spannable {
	
	private final TextSpan span;

	public final Token identifier;
	public final Token colon;

	public LabelNode(Token identifier, Token colon) {
		this.span = identifier.span().plus(colon.span());
		this.identifier = identifier;
		this.colon = colon;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
