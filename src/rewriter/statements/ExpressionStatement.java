package rewriter.statements;

import rewriter.expressions.Expression;
import span.TextSpan;

public final class ExpressionStatement implements Statement {

	public final Expression expression;

	public ExpressionStatement(Expression expression) {
		this.expression = expression;
	}

	@Override
	public TextSpan span() {
		return expression.span();
	}
}
