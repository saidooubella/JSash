package tokens.cases;

import input.Input;
import span.Position;
import tokens.BuildContext;
import tokens.Token;
import tokens.TokenType;
import tokens.TokensProvider;
import utils.InputProviders;
import utils.Inputs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StringCase extends TokenCase {

	@Override
	public List<Token> apply(BuildContext context, Object extra) {

		final List<Token> tokens = new ArrayList<>();
		
		{
			final Position start = context.position();
			final String text = String.valueOf(context.source().consume());
			final Position end = context.position();
			tokens.add(new Token(text, TokenType.DoubleQuote, null, start.plus(end)));
		}
		
		final StringBuilder builder = new StringBuilder();
		Position startString = null;
		
		while (context.source().isNotDone() && Inputs.isNotNewLine(context.source())) {

			if (context.source().current() == '"') {
				
				createStringIfNotEmpty(tokens, builder, startString, context);
				startString = null;
				
				final Position start = context.position();
				final String text = String.valueOf(context.source().consume());
				final Position end = context.position();
				tokens.add(new Token(text, TokenType.DoubleQuote, null, start.plus(end)));
				break;
			}

			final char current = context.source().current(), next = context.source().peek(1);

			if (current == '$' && Character.isLetter(next)) {

				createStringIfNotEmpty(tokens, builder, startString, context);
				startString = null;
				
				final Position start = context.position();
				final String text = String.valueOf(context.source().consume());
				final Position end = context.position();
				tokens.add(new Token(text, TokenType.DollarSign, null, start.plus(end)));
				tokens.addAll(IdentifierCase.Instance.apply(context, null));
				
			} else if (current == '$' && next == '{') {

				createStringIfNotEmpty(tokens, builder, startString, context);
				startString = null;

				final Position start = context.position();
				final String text = String.valueOf(context.source().consume());
				final Position end = context.position();
				tokens.add(new Token(text, TokenType.DollarSign, null, start.plus(end)));

				int depth = 0;

				final TokensProvider provider = new TokensProvider(context);
				for (final Token token : InputProviders.toIterable(provider)) {
					tokens.add(token);
					switch (token.type) {
						case CloseCurly: depth -= 1; break;
						case OpenCurly: depth += 1; break;
					}
					if (depth == 0) break;
				}
				
			} else {
				if (startString == null) startString = context.position();
				builder.append(context.source().consume());
			}
		}

		createStringIfNotEmpty(tokens, builder, startString, context);

		return Collections.unmodifiableList(tokens);
	}

	@Override
	public CheckResult check(Input<Character> source) {
		return CheckResult.from(source.current() == '"');
	}

	private void createStringIfNotEmpty(List<Token> tokens, StringBuilder builder, Position start, BuildContext context) {
		if (builder.length() > 0) {
			tokens.add(new Token(consume(builder), TokenType.String, null, start.plus(context.position())));
		}
	}

	private static String consume(StringBuilder builder) {
		final String text = builder.toString();
		builder.delete(0, builder.length());
		return text;
	}
}
