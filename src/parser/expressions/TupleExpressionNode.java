package parser.expressions;

import span.TextSpan;
import tokens.Token;
import utils.SeparatedList;

public final class TupleExpressionNode implements ExpressionNode {

	private final TextSpan span;

	public final Token openParent;
	public final SeparatedList<ExpressionNode, Token> values;
	public final Token closeParent;
	
	public TupleExpressionNode(Token openParent, SeparatedList<ExpressionNode, Token> values, Token closeParent) {
		this.span = openParent.span().plus(closeParent.span());
		this.closeParent = closeParent;
		this.openParent = openParent;
		this.values = values;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
