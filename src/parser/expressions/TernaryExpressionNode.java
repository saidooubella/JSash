package parser.expressions;

import span.TextSpan;
import tokens.Token;

public final class TernaryExpressionNode implements ExpressionNode {

	private final TextSpan span;
	
	public final ExpressionNode condition;
	public final Token question;
	public final ExpressionNode thenExpr;
	public final Token colon;
	public final ExpressionNode elseExpr;

	public TernaryExpressionNode(ExpressionNode condition, Token question, ExpressionNode thenExpr, Token colon, ExpressionNode elseExpr) {
		this.span = condition.span().plus(elseExpr.span());
		this.condition = condition;
		this.question = question;
		this.thenExpr = thenExpr;
		this.colon = colon;
		this.elseExpr = elseExpr;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
