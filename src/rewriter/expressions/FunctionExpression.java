package rewriter.expressions;

import rewriter.statements.Statement;
import rewriter.symbols.ParameterSymbol;
import span.TextSpan;
import types.Type;

import java.util.List;

public final class FunctionExpression implements Expression {

	private final TextSpan span;
	private final Type type;

	public final List<ParameterSymbol> parameters;
	public final List<Statement> body;

	public FunctionExpression(List<ParameterSymbol> parameters, List<Statement> body, Type type, TextSpan span) {
		this.parameters = parameters;
		this.span = span;
		this.body = body;
		this.type = type;
	}

	@Override
	public TextSpan span() {
		return span;
	}

	@Override
	public Type type() {
		return type;
	}
}
