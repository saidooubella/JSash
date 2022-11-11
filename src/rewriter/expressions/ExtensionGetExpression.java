package rewriter.expressions;

import rewriter.symbols.ExtensionSymbol;
import span.TextSpan;
import types.Type;

public final class ExtensionGetExpression implements Expression {

	private final TextSpan span;
	
	public final Expression target;
	public final ExtensionSymbol symbol;

	public ExtensionGetExpression(Expression target, ExtensionSymbol symbol, TextSpan span) {
		this.target = target;
		this.symbol = symbol;
		this.span = span;
	}

	@Override
	public Type type() {
		return symbol.type();
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
