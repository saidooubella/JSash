package rewriter.statements;

import span.TextSpan;

import java.util.List;

public final class StatementSet implements Statement {

	private final TextSpan span;
	
	public final List<Statement> statements;

	public StatementSet(List<Statement> statements, TextSpan span) {
		this.statements = statements;
		this.span = span;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
