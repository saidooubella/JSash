package parser.expressions.strings;

import parser.expressions.ExpressionNode;
import tokens.Token;

public final class ExpressionStringElement extends StringElement {

	public final Token dollarSign;
	public final Token openCurly;
	public final ExpressionNode expression;
	public final Token closeCurly;

	public ExpressionStringElement(Token dollarSign, Token openCurly, ExpressionNode expression, Token closeCurly) {
		this.dollarSign = dollarSign;
		this.openCurly = openCurly;
		this.expression = expression;
		this.closeCurly = closeCurly;
	}

	@Override
	public String toString() {
		return dollarSign.text + openCurly.text + expression.toString() + closeCurly.text;
	}
}
