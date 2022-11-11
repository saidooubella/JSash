package parser.wrappers;

import parser.expressions.ExpressionNode;
import span.Spannable;
import span.TextSpan;
import tokens.Token;

public final class ValueNode implements Spannable {
	
	private final TextSpan span;

	public final Token equal;
	public final ExpressionNode value;

	public ValueNode(Token equal, ExpressionNode value) {
		this.span = equal.span().plus(value.span());
		this.equal = equal;
		this.value = value;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
