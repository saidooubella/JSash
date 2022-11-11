package tokens;

import input.Input;
import span.Position;
import span.TextSpan;
import tokens.cases.TokenCases;
import utils.Inputs;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public final class TokensProvider implements Input.Provider<Token> {

	private final BuildContext context;
	private final Deque<Token> cache;

	public TokensProvider(BuildContext context) {
		this.cache = new ArrayDeque<>();
		this.context = context;
	}

	@Override
	public Token nextElement() {

		while (cache.isEmpty()) {

			if (isWhitespace()) {
				skipWhitespace();
				continue;
			}

			if (isLineComment()) {
				skipLineComment();
				continue;
			}

			if (isBlockComment()) {
				skipBlockComment();
				continue;
			}

			final List<Token> tokens = TokenCases.apply(context);
			if (!tokens.isEmpty()) {
				cache.addAll(tokens);
				continue;
			}

			final Position start = context.position();
			final char consume = context.source().consume();
			final TextSpan span = start.plus(context.position());
			context.reporter().reportIllegalCharacter(span, consume);
		}
		
		return cache.removeFirst();
	}

	private void skipBlockComment() {

		boolean isCommentNotDone = true;
		int depthCount = 0;

		while (context.source().isNotDone()) {

			if (context.source().current() == '/' && context.source().peek(1) == '*') {
				context.source().advanceBy(2);
				depthCount++;
				continue;
			} else if (context.source().current() == '*' && context.source().peek(1) == '/') {
				context.source().advanceBy(2);
				depthCount--;
				continue;
			}

			if (depthCount == 0) {
				isCommentNotDone = false;
				break;
			}

			context.source().advance();
		}

		if (isCommentNotDone) {
			final Position position = context.position();
			final TextSpan span = position.plus(position);
			context.reporter().reportUnterminatedBlockComment(span);
		}
	}

	private boolean isBlockComment() {
		return context.source().isNotDone() && context.source().current() == '/' && context.source().peek(1) == '*';
	}

	private void skipLineComment() {
		while (context.source().isNotDone() && Inputs.isNotNewLine(context.source())) {
			context.source().advance();
		}
	}

	private boolean isLineComment() {
		return context.source().isNotDone() && context.source().current() == '/' && context.source().peek(1) == '/';
	}

	private void skipWhitespace() {
		while (context.source().isNotDone() && Character.isWhitespace(context.source().current())) {
			context.source().advance();
		}
	}

	private boolean isWhitespace() {
		return context.source().isNotDone() && Character.isWhitespace(context.source().current());
	}

	@Override
	public boolean isEndReached(Token item) {
		return item.type == TokenType.EOF;
	}

	@Override
	public void close() throws IOException {
		context.source().close();
	}
}

