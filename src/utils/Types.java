package utils;

import rewriter.operators.BinaryOperator;
import rewriter.operators.UnaryOperator;
import types.PrimitiveType;
import types.Type;
import types.Typed;
import types.UnionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static types.PrimitiveType.None;

public final class Types {

	public static final Type AnyNone = new UnionType(PrimitiveType.Any, PrimitiveType.None);
	
	private Types() { throw new UnsupportedOperationException(); }

	public static Type fromValue(final Object value) {
		if (value instanceof Boolean) return PrimitiveType.Boolean;
		if (value instanceof Integer) return PrimitiveType.Integer;
		if (value instanceof Double) return PrimitiveType.Double;
		if (value instanceof String) return PrimitiveType.String;
		if (value instanceof Float) return PrimitiveType.Float;
		if (value instanceof Long) return PrimitiveType.Long;
		throw new IllegalStateException(value.getClass().getSimpleName());
	}

	public static List<Type> fromTyped(List<? extends Typed> parameters) {
		final List<Type> types = new ArrayList<>(parameters.size());
		for (final Typed typed : parameters) {
			types.add(typed.type());
		}
		return types;
	}
	
	public static Optional<Type> dominator(Type a, Type b) {
		return Optional.ofNullable(a.assignableTo(b) ? a : b.assignableTo(a) ? b : null);
	}
	
	public static Type any(Type a, Type b) {
		return a.isError() ? b : a;
	}
	
	public static boolean anyNone(Type a, Type b) {
		return None.assignableTo(a) || None.assignableTo(b);
	}

	public static Optional<Type> fromBinaryOperation(final Type left, final BinaryOperator operator, final Type right) {

		switch (operator) {
			case Addition:
				if (left.assignableTo(PrimitiveType.String) || right.assignableTo(PrimitiveType.String)) {
					return Optional.of(PrimitiveType.String);
				}
			case Subtraction:
			case Multiplication:
			case Division: {
				if (isNumber(left) && isNumber(right)) {
					if (left.assignableTo(PrimitiveType.Double) || right.assignableTo(PrimitiveType.Double))
						return Optional.of(PrimitiveType.Double);
					if (left.assignableTo(PrimitiveType.Float) || right.assignableTo(PrimitiveType.Float))
						return Optional.of(PrimitiveType.Float);
					if (left.assignableTo(PrimitiveType.Long) || right.assignableTo(PrimitiveType.Long))
						return Optional.of(PrimitiveType.Long);
					if (left.assignableTo(PrimitiveType.Integer) || right.assignableTo(PrimitiveType.Integer))
						return Optional.of(PrimitiveType.Integer);
				}
				break;
			}
			case LogicalAnd:
			case LogicalOr: {
				if (left.assignableTo(PrimitiveType.Boolean) && right.assignableTo(PrimitiveType.Boolean)) {
					return Optional.of(PrimitiveType.Boolean);
				}
				break;
			}
			case Equal:
			case NotEqual: {
				if (left == right) {
					return Optional.of(PrimitiveType.Boolean);
				}
				break;
			}
			case GreaterThanOrEqual:
			case LessThanOrEqual:
			case GreaterThan:
			case LessThan: {
				if (isNumber(left) && isNumber(right)) {
					return Optional.of(PrimitiveType.Boolean);
				}
				break;
			}
		}

		return Optional.empty();
	}

	public static Optional<Type> fromUnaryOperation(final UnaryOperator operator, final Type operand) {

		switch (operator) {
			case LogicalNegation: {
				if (operand.assignableTo(PrimitiveType.Boolean)) {
					return Optional.of(operand);
				}
				break;
			}
			case Identity:
			case Negation: {
				if (isNumber(operand)) {
					return Optional.of(operand);
				}
				break;
			}
		}

		return Optional.empty();
	}

	private static boolean isNumber(Type type) {
		return type.assignableTo(PrimitiveType.Integer) ||
			type.assignableTo(PrimitiveType.Double) ||
			type.assignableTo(PrimitiveType.Float) ||
			type.assignableTo(PrimitiveType.Long);
	}
}

