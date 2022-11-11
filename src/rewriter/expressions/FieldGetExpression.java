package rewriter.expressions;

import rewriter.symbols.ParameterSymbol;
import span.TextSpan;
import types.Type;

public final class FieldGetExpression implements Expression {

	private final TextSpan span;
	
	public final Expression target;
	public final ParameterSymbol field;

	public FieldGetExpression(Expression target, ParameterSymbol field, TextSpan span) {
		this.target = target;
		this.field = field;
		this.span = span;
	}
	
	@Override
	public Type type() {
		return field.type();
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
