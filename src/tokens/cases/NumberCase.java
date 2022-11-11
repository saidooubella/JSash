package tokens.cases;

import input.Input;
import span.Position;
import span.TextSpan;
import tokens.BuildContext;
import tokens.Token;
import tokens.TokenType;

import java.util.List;

public final class NumberCase extends TokenCase {

	@Override
	public List<Token> apply(BuildContext context, Object extra) {

		final StringBuilder numberBuilder = new StringBuilder();
		final StringBuilder textBuilder = new StringBuilder();
		final Position start = context.position();

		NumberType type = NumberType.Integer;

		while (context.source().isNotDone() && isNumberFull(context.source().current())) {

			final char current = context.source().current();

			if (isLongSuffix(current)) {
				if (type == NumberType.Integer) {
					textBuilder.append(context.source().consume());
					type = NumberType.Long;
				}
				break;
			} else if (isFloatSuffix(current)) {
				textBuilder.append(context.source().consume());
				type = NumberType.Float;
				break;
			} else if (isDoubleSuffix(current)) {
				textBuilder.append(context.source().consume());
				type = NumberType.Double;
				break;
			} else if (current == '.') {
				if (isNumber(context.source().peek(1))) {
					type = NumberType.Double;
				} else {
					break;
				}
			}

			numberBuilder.append(context.source().consume());
			textBuilder.append(current);
		}

		final String number = numberBuilder.toString();
		final Number literal = parseOrNull(type, number);

		final String text = textBuilder.toString();
		final TextSpan span = start.plus(context.position());

		if (literal == null) {
			context.reporter().reportInvalidLiteral(span, text);
		}

		final Number value = literal != null ? literal : zeroValue(type);
		return tokens(new Token(text, TokenType.Number, value, span));
	}

	@Override
	public CheckResult check(Input<Character> source) {
		return CheckResult.from(isNumber(source.current()));
	}

	private static boolean isNumberFull(char character) {
		return isNumber(character) || isDoubleSuffix(character) ||
			isFloatSuffix(character) || isLongSuffix(character) ||
			character == '.';
	}

	private static boolean isDoubleSuffix(char character) {
		return character == 'd' || character == 'D';
	}

	private static boolean isFloatSuffix(char character) {
		return character == 'f' || character == 'F';
	}

	private static boolean isLongSuffix(char character) {
		return character == 'l' || character == 'L';
	}

	private static boolean isNumber(char character) {
		return '0' <= character && character <= '9';
	}

	private static Number parseOrNull(NumberType type, String input) {
		try {
			switch (type) {
				default: throw new IllegalArgumentException(type.name());
				case Integer: return Integer.valueOf(input);
				case Double: return Double.valueOf(input);
				case Float: return Float.valueOf(input);
				case Long: return Long.valueOf(input);
			}
		} catch (NumberFormatException ignore) {
			return null;
		}
	}

	private static Number zeroValue(NumberType type) {
		switch (type) {
			default: throw new IllegalArgumentException(type.name());
			case Integer: return 0; case Double: return 0D;
			case Float: return 0F; case Long: return 0L;
		}
	}

	private enum NumberType { Integer, Long, Float, Double }
}
