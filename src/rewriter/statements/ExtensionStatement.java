package rewriter.statements;

import rewriter.symbols.ExtensionSymbol;
import span.TextSpan;

import java.util.List;

public final class ExtensionStatement implements Statement {
	
	private final TextSpan span;

	public final ExtensionSymbol extension;
	public final List<Statement> body;

	public ExtensionStatement(ExtensionSymbol extension, List<Statement> body, TextSpan span) {
		this.extension = extension;
		this.body = body;
		this.span = span;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
