package parser.statements;

import parser.expressions.ExpressionNode;
import span.TextSpan;

public final class ExpressionStatementNode implements StatementNode {
	
	public final ExpressionNode expression;

	public ExpressionStatementNode(ExpressionNode expression) {
		this.expression = expression;
	}

	@Override
	public TextSpan span() {
		return expression.span();
	}
}
