package parser.wrappers;

import span.Spannable;
import span.TextSpan;
import tokens.Token;
import utils.SeparatedList;

public final class ParametersNode implements Spannable {

	private final TextSpan span;

	public final Token openParent;
	public final SeparatedList<ParameterNode, Token> params;
	public final Token closeParent;

	public ParametersNode(Token openParent, SeparatedList<ParameterNode, Token> params, Token closeParent) {
		this.span = openParent.span().plus(closeParent.span());
		this.closeParent = closeParent;
		this.openParent = openParent;
		this.params = params;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
