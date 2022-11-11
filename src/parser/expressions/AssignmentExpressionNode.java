package parser.expressions;

import parser.wrappers.ValueNode;
import span.TextSpan;

public final class AssignmentExpressionNode implements ExpressionNode {

	private final TextSpan span;
	
	public final ExpressionNode target;
	public final ValueNode value;
	
	public AssignmentExpressionNode(ExpressionNode target, ValueNode value) {
		this.span = target.span().plus(value.span());
		this.target = target;
		this.value = value;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}

