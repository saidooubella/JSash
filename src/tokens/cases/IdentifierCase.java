package tokens.cases;

import input.Input;
import span.Position;
import tokens.BuildContext;
import tokens.Token;
import tokens.TokenType;

import java.util.List;

public final class IdentifierCase extends TokenCase {

	public static final IdentifierCase Instance = new IdentifierCase();
	
	private IdentifierCase() {}
	
	@Override
	public List<Token> apply(BuildContext context, Object extra) {
		
		final StringBuilder builder = new StringBuilder();
		final Position start = context.position();

		while (context.source().isNotDone() && Character.isLetterOrDigit(context.source().current())) {
			builder.append(context.source().consume());
		}
		
		final String identifier = builder.toString();
		final Position end = context.position();
		
		return tokens(new Token(identifier, tokenType(identifier), null, start.plus(end)));
	}

	@Override
	public CheckResult check(Input<Character> source) {
		return CheckResult.from(Character.isLetter(source.current()));
	}
	
	private static TokenType tokenType(String identifier) {
		switch (identifier) {
			case "record": return TokenType.RecordKeyword;
			case "return": return TokenType.ReturnKeyword;
			case "false": return TokenType.FalseKeyword;
			case "none": return TokenType.NoneKeyword;
			case "true": return TokenType.TrueKeyword;
			case "self": return TokenType.SelfKeyword;
			case "def": return TokenType.DefKeyword;
			case "let": return TokenType.LetKeyword;
			case "fun": return TokenType.FunKeyword;
			default: return TokenType.Identifier;
		}
	}
}
