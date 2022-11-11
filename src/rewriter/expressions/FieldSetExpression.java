package rewriter.expressions;

import rewriter.symbols.ParameterSymbol;
import span.TextSpan;
import types.Type;

public final class FieldSetExpression implements Expression {

	private final TextSpan span;
	
	public final Expression target;
	public final ParameterSymbol field;
	public final Expression value;
	
	public FieldSetExpression(Expression target, ParameterSymbol field, Expression value, TextSpan span) {
		this.target = target;
		this.field = field;
		this.value = value;
		this.span = span;
	}

	@Override
	public Type type() {
		return value.type();
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
