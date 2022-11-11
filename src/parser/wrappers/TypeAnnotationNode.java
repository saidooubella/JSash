package parser.wrappers;

import span.Spannable;
import span.TextSpan;
import tokens.Token;

public final class TypeAnnotationNode implements Spannable {
	
	private final TextSpan span;

	public final Token colon;
	public final TypeNode type;

	public TypeAnnotationNode(Token colon, TypeNode type) {
		this.span = colon.span().plus(type.span());
		this.colon = colon;
		this.type = type;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
