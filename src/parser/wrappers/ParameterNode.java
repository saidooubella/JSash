package parser.wrappers;

import span.Spannable;
import span.TextSpan;
import tokens.Token;

import java.util.Optional;

public final class ParameterNode implements Spannable {

	private final TextSpan span;

	public final Token identifier;
	public final TypeAnnotationNode type;
	public final Optional<ValueNode> value;

	public ParameterNode(Token identifier, TypeAnnotationNode type, Optional<ValueNode> value) {
		this.span = value.map(valueNode -> identifier.span().plus(valueNode.span())).orElseGet(() -> identifier.span().plus(type.span()));
		this.identifier = identifier;
		this.type = type;
		this.value = value;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
