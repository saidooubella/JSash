package parser.expressions;

import parser.expressions.strings.StringElement;
import span.TextSpan;
import tokens.Token;

import java.util.List;

public final class StringExpressionNode implements ExpressionNode {

	public final Token leftQuote;
	public final List<StringElement> elements;
	public final Token rightQuote;

	private final TextSpan span;

	public StringExpressionNode(Token leftQuote, List<StringElement> elements, Token rightQuote) {
		this.leftQuote = leftQuote;
		this.elements = elements;
		this.rightQuote = rightQuote;
		this.span = leftQuote.span().plus(rightQuote.span());
	}

	@Override
	public TextSpan span() {
		return span;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(leftQuote.text);
		for (final StringElement element : elements) {
			builder.append(element.toString());
		}
		builder.append(rightQuote.text);
		return builder.toString();
	}
}
