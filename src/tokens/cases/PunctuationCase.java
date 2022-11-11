package tokens.cases;

import input.Input;
import span.Position;
import tokens.BuildContext;
import tokens.Token;
import tokens.TokenType;

import java.util.List;

public final class PunctuationCase extends TokenCase {

	@Override
	public List<Token> apply(BuildContext context, Object extra) {
		final Punctuation punctuation = (Punctuation) extra;
		final Position start = context.position();
		context.source().advanceBy(punctuation.match.length());
		final Position end = context.position();
		return tokens(new Token(punctuation.match, punctuation.type, null, start.plus(end)));
	}

	@Override
	public CheckResult check(Input<Character> source) {
		final Punctuation result = Punctuation.get(source);
		return CheckResult.from(result != null, result);
	}
	
	private static final class Punctuation {

		private static final Punctuation[] punctuations = {
			new Punctuation("&&", TokenType.AmpersandAmpersand),
			new Punctuation(">=", TokenType.CloseAngleEqual),
			new Punctuation("<=", TokenType.OpenAngleEqual),
			new Punctuation("==", TokenType.EqualEqual),
			new Punctuation("!=", TokenType.BangEqual),
			new Punctuation("||", TokenType.PipePipe),
			new Punctuation("=>", TokenType.Arrow),
			new Punctuation(")", TokenType.CloseParent),
			new Punctuation(">", TokenType.CloseAngle),
			new Punctuation("(", TokenType.OpenParent),
			new Punctuation("}", TokenType.CloseCurly),
			new Punctuation("{", TokenType.OpenCurly),
			new Punctuation("<", TokenType.OpenAngle),
			new Punctuation("?", TokenType.Question),
			new Punctuation("=", TokenType.Equal),
			new Punctuation(":", TokenType.Colon),
			new Punctuation(",", TokenType.Comma),
			new Punctuation("-", TokenType.Minus),
			new Punctuation("/", TokenType.Slash),
			new Punctuation("!", TokenType.Bang),
			new Punctuation("|", TokenType.Pipe),
			new Punctuation("+", TokenType.Plus),
			new Punctuation("*", TokenType.Star),
			new Punctuation(".", TokenType.Dot),
		};
		
		public final String match;
		public final TokenType type;

		private Punctuation(String match, TokenType type) {
			this.match = match;
			this.type = type;
		}

		public static Punctuation get(Input<Character> source) {

			out: for (final Punctuation punctuation : punctuations) {
				for (int i = 0; i < punctuation.match.length(); i++)
					if (source.peek(i) != punctuation.match.charAt(i))
						continue out;
				return punctuation;
			}

			return null;
		}
	}
}
