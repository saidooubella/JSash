package parser.expressions;

import span.TextSpan;
import tokens.Token;

import java.util.Optional;

public final class ReturnExpressionNode implements ExpressionNode {

	private final TextSpan span;

	public final Optional<ExpressionNode> value;
	public final Token returnKeyword;

	public ReturnExpressionNode(Token returnKeyword, Optional<ExpressionNode> value) {
		this.span = value.map(expressionNode -> returnKeyword.span().plus(expressionNode.span())).orElseGet(returnKeyword::span);
		this.returnKeyword = returnKeyword;
		this.value = value;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
