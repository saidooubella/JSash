package parser.wrappers;

import span.Spannable;
import span.TextSpan;
import tokens.Token;

public final class ReceiverNode implements Spannable {

	private final TextSpan span;

	public final TypeNode type;
	public final Token dot;

	public ReceiverNode(TypeNode type, Token dot) {
		this.span = type.span().plus(dot.span());
		this.type = type;
		this.dot = dot;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
