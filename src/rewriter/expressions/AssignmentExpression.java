package rewriter.expressions;

import rewriter.symbols.VariableSymbol;
import span.TextSpan;
import types.Type;

public final class AssignmentExpression implements Expression {

	private final TextSpan span;

	public final VariableSymbol variable;
	public final Expression value;
	
	public AssignmentExpression(VariableSymbol variable, Expression value, TextSpan span) {
		this.variable = variable;
		this.value = value;
		this.span = span;
	}

	@Override
	public Type type() {
		return variable.type();
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
