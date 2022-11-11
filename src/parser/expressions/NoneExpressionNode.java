package parser.expressions;

import span.TextSpan;
import tokens.Token;

public final class NoneExpressionNode implements ExpressionNode {

	public final Token noneKeyword;

	public NoneExpressionNode(Token noneKeyword) {
		this.noneKeyword = noneKeyword;
	}
	
	@Override
	public TextSpan span() {
		return noneKeyword.span();
	}
}
