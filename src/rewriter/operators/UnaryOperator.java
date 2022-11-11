package rewriter.operators;

import tokens.TokenType;

public enum UnaryOperator {

	Identity, Negation, LogicalNegation;

	@Override
	public String toString() {
		switch (this) {
			case LogicalNegation: return "!";
			case Identity: return "+";
			case Negation: return "-";
			default: throw new IllegalStateException(name());
		}
	}

	public static UnaryOperator from(TokenType type) {
		switch (type) {
			case Bang: return LogicalNegation;
			case Plus: return Identity;
			case Minus: return Negation;
			default: throw new IllegalStateException(type.name());
		}
	}
}
