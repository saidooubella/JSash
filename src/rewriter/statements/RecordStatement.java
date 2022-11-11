package rewriter.statements;

import rewriter.symbols.RecordSymbol;
import span.TextSpan;

public final class RecordStatement implements Statement {

	private final TextSpan span;
	
	public final RecordSymbol record;

	public RecordStatement(RecordSymbol record, TextSpan span) {
		this.record = record;
		this.span = span;
	}
	
	@Override
	public TextSpan span() {
		return span;
	}
}
