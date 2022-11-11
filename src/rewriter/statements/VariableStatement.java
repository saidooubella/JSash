package rewriter.statements;

import rewriter.expressions.Expression;
import rewriter.symbols.VariableSymbol;
import span.TextSpan;

public final class VariableStatement implements Statement {

	private final TextSpan span;
	
	public final VariableSymbol symbol;
	public final Expression value;
	
	public VariableStatement(VariableSymbol symbol, Expression value, TextSpan span) {
		this.symbol = symbol;
		this.value = value;
		this.span = span;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
