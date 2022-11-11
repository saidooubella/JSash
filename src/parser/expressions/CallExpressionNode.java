package parser.expressions;

import parser.wrappers.ArgumentNode;
import span.TextSpan;
import tokens.Token;
import utils.SeparatedList;

public final class CallExpressionNode implements ExpressionNode {

	private final TextSpan span;

	public final ExpressionNode target;
	public final Token openParent;
	public final SeparatedList<ArgumentNode, Token> arguments;
	public final Token closeParent;
	
	public CallExpressionNode(ExpressionNode target, Token openParent, SeparatedList<ArgumentNode, Token> arguments, Token closeParent) {
		this.span = target.span().plus(closeParent.span());
		this.openParent = openParent;
		this.arguments = arguments;
		this.closeParent = closeParent;
		this.target = target;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
