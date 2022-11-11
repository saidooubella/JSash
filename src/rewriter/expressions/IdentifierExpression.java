package rewriter.expressions;

import rewriter.symbols.Symbol;
import span.TextSpan;
import types.Type;

public final class IdentifierExpression implements Expression {

	private final TextSpan span;
	
	public final Symbol symbol;

	public IdentifierExpression(Symbol symbol, TextSpan span) {
		this.symbol = symbol;
		this.span = span;
	}

	@Override
	public TextSpan span() {
		return span;
	}

	@Override
	public Type type() {
		return symbol.type();
	}
}
