package parser.expressions;

import span.TextSpan;
import tokens.Token;

public final class UnaryExpressionNode implements ExpressionNode {

	private final TextSpan span;

	public final Token operator;
	public final ExpressionNode operand;
	
	public UnaryExpressionNode(Token operator, ExpressionNode operand) {
		this.span = operator.span().plus(operand.span());
		this.operator = operator;
		this.operand = operand;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
