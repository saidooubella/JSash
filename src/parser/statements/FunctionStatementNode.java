package parser.statements;

import parser.wrappers.FunctionBodyNode;
import parser.wrappers.ParametersNode;
import parser.wrappers.TypeAnnotationNode;
import span.TextSpan;
import tokens.Token;

public final class FunctionStatementNode implements StatementNode {

	private final TextSpan span;

	public final Token funKeyword;
	public final Token identifier;
	public final ParametersNode parameters;
	public final TypeAnnotationNode returnType;
	public final FunctionBodyNode body;

	public FunctionStatementNode(Token funKeyword, Token identifier, ParametersNode parameters, TypeAnnotationNode returnType, FunctionBodyNode body) {
		this.span = funKeyword.span().plus(body.span());
		this.funKeyword = funKeyword;
		this.identifier = identifier;
		this.parameters = parameters;
		this.returnType = returnType;
		this.body = body;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
