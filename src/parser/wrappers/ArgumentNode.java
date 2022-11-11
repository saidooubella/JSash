package parser.wrappers;

import parser.expressions.ExpressionNode;
import span.Spannable;
import span.TextSpan;

import java.util.Optional;

public final class ArgumentNode implements Spannable {
	
	private final TextSpan span;

	public final Optional<LabelNode> label;
	public final ExpressionNode value;

	public ArgumentNode(Optional<LabelNode> label, ExpressionNode value) {
		this.span = label.map(labelNode -> labelNode.span().plus(value.span())).orElseGet(value::span);
		this.label = label;
		this.value = value;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
