package parser.expressions;

import parser.wrappers.FunctionBodyNode;
import parser.wrappers.ParametersNode;
import parser.wrappers.TypeAnnotationNode;
import span.TextSpan;
import tokens.Token;

public final class FunctionExpressionNode implements ExpressionNode {

	private final TextSpan span;

	public final Token funKeyword;
	public final ParametersNode parameters;
	public final TypeAnnotationNode returnType;
	public final FunctionBodyNode body;

	public FunctionExpressionNode(Token funKeyword, ParametersNode parameters, TypeAnnotationNode returnType, FunctionBodyNode body) {
		this.span = funKeyword.span().plus(body.span());
		this.parameters = parameters;
		this.funKeyword = funKeyword;
		this.returnType = returnType;
		this.body = body;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
