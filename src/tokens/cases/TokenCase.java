package tokens.cases;

import input.Input;
import tokens.BuildContext;
import tokens.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class TokenCase {
	
	public abstract List<Token> apply(BuildContext context, Object extra);
	public abstract CheckResult check(Input<Character> source);
	
	protected static List<Token> tokens(Token token) {
		final List<Token> tokens = new ArrayList<>(1);
		tokens.add(token);
		return Collections.unmodifiableList(tokens);
	}
}
