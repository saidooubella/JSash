package rewriter;

import rewriter.statements.Statement;
import rewriter.symbols.Symbol;

import java.util.List;
import java.util.Optional;

public final class RewriterResult {

	public final Optional<Symbol> mainEntry;
	public final List<Statement> statements;
	
	public RewriterResult(List<Statement> statements, Optional<Symbol> mainEntry) {
		this.statements = statements;
		this.mainEntry = mainEntry;
	}
}
