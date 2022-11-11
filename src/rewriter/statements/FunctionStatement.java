package rewriter.statements;

import rewriter.symbols.FunctionSymbol;
import span.TextSpan;

import java.util.List;

public final class FunctionStatement implements Statement {

	private final TextSpan span;
	
	public final FunctionSymbol function;
	public final List<Statement> body;

	public FunctionStatement(FunctionSymbol function, List<Statement> body, TextSpan span) {
		this.function = function;
		this.body = body;
		this.span = span;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
