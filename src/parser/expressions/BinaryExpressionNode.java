package parser.expressions;

import span.TextSpan;
import tokens.Token;

public final class BinaryExpressionNode implements ExpressionNode {

	private final TextSpan span;

	public final ExpressionNode left;
	public final Token operator;
	public final ExpressionNode right;

	public BinaryExpressionNode(ExpressionNode left, Token operator, ExpressionNode right) {
		this.span = left.span().plus(right.span());
		this.operator = operator;
		this.right = right;
		this.left = left;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
