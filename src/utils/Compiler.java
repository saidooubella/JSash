package utils;

import errors.ErrorReports;
import errors.ErrorsReporter;
import input.Input;
import parser.Parser;
import rewriter.Rewriter;
import rewriter.RewriterResult;
import rewriter.statements.Statement;
import rewriter.symbols.Symbol;
import span.Position;
import tokens.BuildContext;
import tokens.Token;
import tokens.TokensProvider;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class Compiler {

	public static Result compile(SourceFile srcFile) {

		final ErrorReports.Builder reportsBuilder = new ErrorReports.Builder();
		final ErrorsReporter reporter = reportsBuilder.reporter(srcFile.path);
		final Input<Token> tokens = buildTokensSource(srcFile, reporter);

		try (final Parser parser = new Parser(reporter, tokens)) {
			final Rewriter rewriter = new Rewriter(reporter, parser.parse());
			final RewriterResult result = rewriter.rewrite();
			return new Result(result.statements, result.mainEntry, reportsBuilder.build());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Input<Token> buildTokensSource(final SourceFile srcFile, final ErrorsReporter reporter) {

		final Position.Builder positionBuilder = new Position.Builder();
		final Input.Provider<Integer> readerProvider = new ReaderProvider(srcFile.toReader());
		final Consumer<Character> onPreNext = positionBuilder::advance;

		final Input<Character> source = Input.of(readerProvider).map(Inputs.IntToChar).doOnPreNext(onPreNext);
		final BuildContext context = new BuildContext(source, positionBuilder, reporter);

		return Input.of(new TokensProvider(context));
	}

	public static final class Result {

		public final Optional<Symbol> mainEntry;
		public final List<Statement> statements;
		public final ErrorReports reports;

		public Result(List<Statement> statements, Optional<Symbol> mainEntry, ErrorReports reports) {
			this.statements = statements;
			this.mainEntry = mainEntry;
			this.reports = reports;
		}
	}
}
