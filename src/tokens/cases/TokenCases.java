package tokens.cases;

import tokens.BuildContext;
import tokens.Token;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class TokenCases {

	private static final List<TokenCase> tokenCases = cases(
		new NumberCase(),
		new PunctuationCase(),
		IdentifierCase.Instance,
		new StringCase(),
		new EndCase()
	);

	public static List<Token> apply(BuildContext context) {
		for (final TokenCase tokenCase : tokenCases) {
			final CheckResult result = tokenCase.check(context.source());
			if (!result.isSuccess) continue;
			return tokenCase.apply(context, result.extra);
		}
		return Collections.emptyList();
	}

	private static List<TokenCase> cases(TokenCase... cases) {
		return Collections.unmodifiableList(Arrays.asList(cases));
	}
}
