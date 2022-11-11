package evaluator;

import evaluator.environment.Environment;
import evaluator.jumps.ReturnException;
import evaluator.values.*;
import rewriter.expressions.*;
import rewriter.statements.*;
import rewriter.symbols.Symbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class Evaluator {

	public static void evaluate(List<Statement> statements, Optional<Symbol> mainEntry) {
		final Environment environment = Environment.create();
		evaluateStatements(environment, statements);
		if (mainEntry.isPresent()) {
			final Object callable = environment.getSymbol(mainEntry.get());
			((CallableValue) callable).invoke(Collections.emptyList());
		}
	}

	public static void evaluateStatements(Environment env, List<Statement> statements) {
		for (final Statement statement : statements) {
			evaluateStatement(env, statement);
		}
	}

	private static void evaluateStatement(Environment env, Statement statement) {
		if (statement instanceof VariableStatement) {
			evaluateVariableStatement(env, (VariableStatement) statement);
		} else if (statement instanceof FunctionStatement) {
			evaluateFunctionStatement(env, (FunctionStatement) statement);
		} else if (statement instanceof ExpressionStatement) {
			evaluateExpressionStatement(env, (ExpressionStatement) statement);
		} else if (statement instanceof StatementSet) {
			evaluateStatements(env, ((StatementSet) statement).statements);
		} else if (statement instanceof ExtensionStatement) {
			evaluateExtensionStatement(env, (ExtensionStatement) statement);
		} else if (statement instanceof RecordStatement) {
			evaluateRecordStatement(env, (RecordStatement) statement);
		} else {
			throw new IllegalStateException(statement.getClass().getSimpleName());
		}
	}

	private static void evaluateRecordStatement(Environment env, final RecordStatement statement) {
		env.putSymbol(statement.record, (CallableValue) arguments -> new RecordValue(statement.record, arguments));
	}

	private static void evaluateExtensionStatement(Environment env, ExtensionStatement statement) {
		env.putSymbol(statement.extension, new ExtensionValue(env.copy(), statement.extension.receiver, statement.extension.parameters, statement.body));
		env.freeze();
	}

	private static void evaluateVariableStatement(Environment env, VariableStatement statement) {
		env.putSymbol(statement.symbol, evaluateExpression(env, statement.value));
	}

	private static void evaluateFunctionStatement(Environment env, FunctionStatement statement) {
		env.putSymbol(statement.function, new FunctionValue(env.copy(), statement.function.parameters, statement.body));
		env.freeze();
	}

	private static void evaluateExpressionStatement(Environment env, ExpressionStatement statement) {
		evaluateExpression(env, statement.expression);
	}

	private static List<Object> evaluateExpressions(Environment env, List<Expression> expressions) {
		final List<Object> values = new ArrayList<>(expressions.size());
		for (final Expression expression : expressions) {
			values.add(evaluateExpression(env, expression));
		}
		return values;
	}

	private static Object evaluateExpression(Environment env, Expression expression) {
		if (expression instanceof CallExpression) {
			return evaluateCallExpression(env, (CallExpression) expression);
		} else if (expression instanceof ReturnExpression) {
			return evaluateReturnExpression(env, (ReturnExpression) expression);
		} else if (expression instanceof FunctionExpression) {
			return evaluateFunctionExpression(env, (FunctionExpression) expression);
		} else if (expression instanceof BinaryExpression) {
			return evaluateBinaryExpression(env, (BinaryExpression) expression);
		} else if (expression instanceof UnaryExpression) {
			return evaluateUnaryExpression(env, (UnaryExpression) expression);
		} else if (expression instanceof AssignmentExpression) {
			return evaluateAssignmentExpression(env, (AssignmentExpression) expression);
		} else if (expression instanceof TupleExpression) {
			return evaluateTupleExpression(env, (TupleExpression) expression);
		} else if (expression instanceof IndexingExpression) {
			return evaluateIndexingExpression(env, (IndexingExpression) expression);
		} else if (expression instanceof TernaryExpression) {
			return evaluateTernaryExpression(env, (TernaryExpression) expression);
		} else if (expression instanceof ParenthesizedExpression) {
			return evaluateExpression(env, ((ParenthesizedExpression) expression).expression);
		} else if (expression instanceof ExtensionGetExpression) {
			return evaluateExtensionGetExpression(env, (ExtensionGetExpression) expression);
		} else if (expression instanceof FieldGetExpression) {
			return evaluateFieldGetExpression(env, (FieldGetExpression) expression);
		} else if (expression instanceof FieldSetExpression) {
			return evaluateFieldSetExpression(env, (FieldSetExpression) expression);
		} else if (expression instanceof IdentifierExpression) {
			return env.getSymbol(((IdentifierExpression) expression).symbol);
		} else if (expression instanceof LiteralExpression) {
			return ((LiteralExpression) expression).value;
		} else if (expression instanceof NoneExpression) {
			return NoneValue.instance();
		} else {
			throw new IllegalStateException(expression.getClass().getSimpleName());
		}
	}

	private static Object evaluateFieldSetExpression(Environment env, FieldSetExpression expression) {
		final RecordValue target = (RecordValue) evaluateExpression(env, expression.target);
		return target.set(expression.field, evaluateExpression(env, expression.value));
	}

	private static Object evaluateFieldGetExpression(Environment env, FieldGetExpression expression) {
		return ((RecordValue) evaluateExpression(env, expression.target)).get(expression.field);
	}

	private static Object evaluateExtensionGetExpression(Environment env, ExtensionGetExpression expression) {
		return ((ExtensionBuilder) env.getSymbol(expression.symbol)).build(evaluateExpression(env, expression.target));
	}

	private static Object evaluateTernaryExpression(Environment env, TernaryExpression expression) {
		return ((Boolean) evaluateExpression(env, expression.condition))
			? evaluateExpression(env, expression.thenExpr)
			: evaluateExpression(env, expression.elseExpr);
	}

	private static Object evaluateIndexingExpression(Environment env, IndexingExpression expression) {
		final Object target = evaluateExpression(env, expression.target);
		if (target instanceof TupleValue) {
			final Object index = evaluateExpression(env, expression.index);
			return ((TupleValue) target).get((Integer) index);
		}
		throw new IllegalStateException(target.getClass().getSimpleName());
	}

	private static Object evaluateTupleExpression(Environment env, TupleExpression expression) {
		return new TupleValue(evaluateExpressions(env, expression.values));
	}

	private static Object evaluateAssignmentExpression(Environment env, AssignmentExpression expression) {
		final Object value = evaluateExpression(env, expression.value);
		env.changeSymbol(expression.variable, value);
		return value;
	}

	private static Object evaluateReturnExpression(Environment env, ReturnExpression expression) throws ReturnException {
		throw new ReturnException(expression.value.isPresent() ? evaluateExpression(env, expression.value.get()) : UnitValue.instance());
	}

	private static Object evaluateUnaryExpression(Environment env, UnaryExpression expression) {

		final Object operand = evaluateExpression(env, expression.operand);

		switch (expression.operator) {
			case LogicalNegation: {
				return !(Boolean) operand;
			}
			case Identity: {
				return operand;
			}
			case Negation: {
				if (operand instanceof Integer)
					return -(Integer) operand;
				if (operand instanceof Double)
					return -(Double) operand;
				if (operand instanceof Float)
					return -(Float) operand;
				if (operand instanceof Long)
					return -(Long) operand;
				break;
			}
		}

		throw new IllegalArgumentException();
	}

	private static Object evaluateBinaryExpression(Environment env, BinaryExpression expression) {

		final Object left = evaluateExpression(env, expression.left);
		final Object right = evaluateExpression(env, expression.right);

		switch (expression.operator) {
			case LogicalAnd: {
				return (Boolean) left && (Boolean) right;
			}
			case LogicalOr: {
				return (Boolean) left || (Boolean) right;
			}
			case NotEqual: {
				return !left.equals(right);
			}
			case Equal: {
				return left.equals(right);
			}
			case GreaterThan: {
				if (left instanceof Double || right instanceof Double)
					return ((Number) left).doubleValue() > ((Number) right).doubleValue();
				if (left instanceof Float || right instanceof Float)
					return ((Number) left).floatValue() > ((Number) right).floatValue();
				if (left instanceof Long || right instanceof Long)
					return ((Number) left).longValue() > ((Number) right).longValue();
				if (left instanceof Integer || right instanceof Integer)
					return ((Number) left).intValue() > ((Number) right).intValue();
				break;
			}
			case LessThan: {
				if (left instanceof Double || right instanceof Double)
					return ((Number) left).doubleValue() < ((Number) right).doubleValue();
				if (left instanceof Float || right instanceof Float)
					return ((Number) left).floatValue() < ((Number) right).floatValue();
				if (left instanceof Long || right instanceof Long)
					return ((Number) left).longValue() < ((Number) right).longValue();
				if (left instanceof Integer || right instanceof Integer)
					return ((Number) left).intValue() < ((Number) right).intValue();
				break;
			}
			case GreaterThanOrEqual: {
				if (left instanceof Double || right instanceof Double)
					return ((Number) left).doubleValue() >= ((Number) right).doubleValue();
				if (left instanceof Float || right instanceof Float)
					return ((Number) left).floatValue() >= ((Number) right).floatValue();
				if (left instanceof Long || right instanceof Long)
					return ((Number) left).longValue() >= ((Number) right).longValue();
				if (left instanceof Integer || right instanceof Integer)
					return ((Number) left).intValue() >= ((Number) right).intValue();
				break;
			}
			case LessThanOrEqual: {
				if (left instanceof Double || right instanceof Double)
					return ((Number) left).doubleValue() <= ((Number) right).doubleValue();
				if (left instanceof Float || right instanceof Float)
					return ((Number) left).floatValue() <= ((Number) right).floatValue();
				if (left instanceof Long || right instanceof Long)
					return ((Number) left).longValue() <= ((Number) right).longValue();
				if (left instanceof Integer || right instanceof Integer)
					return ((Number) left).intValue() <= ((Number) right).intValue();
				break;
			}
			case Addition: {
				if (left instanceof String || right instanceof String)
					return left.toString() + right.toString();
				if (left instanceof Double || right instanceof Double)
					return ((Number) left).doubleValue() + ((Number) right).doubleValue();
				if (left instanceof Float || right instanceof Float)
					return ((Number) left).floatValue() + ((Number) right).floatValue();
				if (left instanceof Long || right instanceof Long)
					return ((Number) left).longValue() + ((Number) right).longValue();
				if (left instanceof Integer || right instanceof Integer)
					return ((Number) left).intValue() + ((Number) right).intValue();
				break;
			}
			case Subtraction: {
				if (left instanceof Double || right instanceof Double)
					return ((Number) left).doubleValue() - ((Number) right).doubleValue();
				if (left instanceof Float || right instanceof Float)
					return ((Number) left).floatValue() - ((Number) right).floatValue();
				if (left instanceof Long || right instanceof Long)
					return ((Number) left).longValue() - ((Number) right).longValue();
				if (left instanceof Integer || right instanceof Integer)
					return ((Number) left).intValue() - ((Number) right).intValue();
				break;
			}
			case Multiplication: {
				if (left instanceof Double || right instanceof Double)
					return ((Number) left).doubleValue() * ((Number) right).doubleValue();
				if (left instanceof Float || right instanceof Float)
					return ((Number) left).floatValue() * ((Number) right).floatValue();
				if (left instanceof Long || right instanceof Long)
					return ((Number) left).longValue() * ((Number) right).longValue();
				if (left instanceof Integer || right instanceof Integer)
					return ((Number) left).intValue() * ((Number) right).intValue();
				break;
			}
			case Division: {
				if (left instanceof Double || right instanceof Double)
					return ((Number) left).doubleValue() / ((Number) right).doubleValue();
				if (left instanceof Float || right instanceof Float)
					return ((Number) left).floatValue() / ((Number) right).floatValue();
				if (left instanceof Long || right instanceof Long)
					return ((Number) left).longValue() / ((Number) right).longValue();
				if (left instanceof Integer || right instanceof Integer)
					return ((Number) left).intValue() / ((Number) right).intValue();
				break;
			}
		}

		throw new IllegalArgumentException();
	}

	private static Object evaluateFunctionExpression(Environment env, FunctionExpression expression) {
		return new FunctionValue(env.frozenCopy(), expression.parameters, expression.body);
	}

	private static Object evaluateCallExpression(Environment env, CallExpression expression) {
		final Object target = evaluateExpression(env, expression.target);
		if (target instanceof CallableValue) {
			return ((CallableValue) target).invoke(evaluateExpressions(env, expression.arguments));
		} else {
			throw new IllegalStateException(target.getClass().getSimpleName());
		}
	}
}
