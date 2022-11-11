package rewriter.operators;

import tokens.TokenType;

public enum BinaryOperator {

	Addition, Subtraction, Multiplication, Division,
	LogicalOr, LogicalAnd, Equal, NotEqual, GreaterThan,
	GreaterThanOrEqual, LessThan, LessThanOrEqual;

	@Override
	public String toString() {
		switch (this) {
			case Addition: return "+";
			case Subtraction: return "-";
			case Multiplication: return "*";
			case Division: return "/";
			case LogicalOr: return "||";
			case LogicalAnd: return "&&";
			case Equal: return "==";
			case NotEqual: return "!=";
			case GreaterThan: return ">";
			case GreaterThanOrEqual: return ">=";
			case LessThan: return "<";
			case LessThanOrEqual: return "<=";
			default: throw new IllegalStateException(name());
		}
	}

	public static BinaryOperator from(TokenType type) {
		switch (type) {
			case AmpersandAmpersand: return LogicalAnd;
			case CloseAngleEqual: return GreaterThanOrEqual;
			case OpenAngleEqual: return LessThanOrEqual;
			case CloseAngle: return GreaterThan;
			case EqualEqual: return Equal;
			case BangEqual: return NotEqual;
			case OpenAngle: return LessThan;
			case PipePipe: return LogicalOr;
			case Minus: return Subtraction;
			case Slash: return Division;
			case Star: return Multiplication;
			case Plus: return Addition;
			default: throw new IllegalStateException(type.name());
		}
	}
}
