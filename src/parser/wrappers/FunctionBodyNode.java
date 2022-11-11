package parser.wrappers;

import parser.expressions.ExpressionNode;
import parser.statements.StatementNode;
import span.Spannable;
import span.TextSpan;
import tokens.Token;

import java.util.List;

public abstract class FunctionBodyNode implements Spannable {

	private final TextSpan span;

	private FunctionBodyNode(TextSpan span) {
		this.span = span;
	}

	@Override
	public TextSpan span() {
		return span;
	}

	public static final class Block extends FunctionBodyNode {

		public final Token openCurly;
		public final List<StatementNode> statements;
		public final Token closeCurly;

		public Block(Token openCurly, List<StatementNode> statements, Token closeCurly) {
			super(openCurly.span().plus(closeCurly.span()));
			this.openCurly = openCurly;
			this.statements = statements;
			this.closeCurly = closeCurly;
		}
	}

	public static final class Expression extends FunctionBodyNode {

		public final Token arrow;
		public final ExpressionNode expression;

		public Expression(Token arrow, ExpressionNode expression) {
			super(arrow.span().plus(expression.span()));
			this.expression = expression;
			this.arrow = arrow;
		}
	}
}
