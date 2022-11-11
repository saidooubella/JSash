package tokens.cases;

import input.Input;
import span.Position;
import span.TextSpan;
import tokens.BuildContext;
import tokens.Token;
import tokens.TokenType;

import java.util.List;

public final class EndCase extends TokenCase {

	@Override
	public List<Token> apply(BuildContext context, Object extra) {
		final Position position = context.position();
		final TextSpan span = position.plus(position);
		return tokens(new Token("End of file", TokenType.EOF, null, span));
	}

	@Override
	public CheckResult check(Input<Character> source) {
		return CheckResult.from(source.isDone());
	}
}
