package errors;

import rewriter.operators.BinaryOperator;
import rewriter.operators.UnaryOperator;
import span.TextSpan;
import tokens.TokenType;
import types.Type;

import java.util.List;

public final class ErrorsReporter {

	private final List<ErrorMessage> errors;
	private final String location;

	public ErrorsReporter(List<ErrorMessage> errors, String location) {
		this.location = location;
		this.errors = errors;
	}

	public void reportExpectedEndOfStatement(TextSpan span) {
		report(span, "Expected a new line");
	}

	public void reportUnterminatedBlockComment(TextSpan span) {
		report(span, "Unterminated multi-line comment");
	}

	public void reportUninitializedVariable(TextSpan span) {
		report(span, "Uninitialized variable");
	}

	public void reportUnterminatedStringLiteral(TextSpan span) {
		report(span, "Unterminated string literal");
	}

	public void reportMutatingReadOnlyVariable(TextSpan span) {
		report(span, "Read only variables cannot be reassigned");
	}

	public void reportReturnUsageInExpressionBody(TextSpan span) {
		report(span, "'return' expressions aren't allowed in function's expression body");
	}

	public void reportInvalidAssignmentTarget(TextSpan span) {
		report(span, "Invalid assignment target");
	}

	public void reportMissingReturn(TextSpan span, Type returnType) {
		report(span, "This function must return a value of type '" + returnType + "'");
	}

	public void reportUnreachedStatement(TextSpan span) {
		report(span, "Unreached statement");
	}

	public void reportReturnOutOfFunction(TextSpan span) {
		report(span, "'return' expression cannot be used outside of functions boundaries");
	}

	public void reportMissingReturnValue(TextSpan span, Type returnType) {
		report(span, "A value of type '" + returnType + "' is missing for this 'return' expression");
	}

	public void reportMismatchedReturnValueType(TextSpan span, Type returnValue, Type returnType) {
		report(span, "A value of type '" + returnValue + "' cannot be returned by a function of type '" + returnType + "'");
	}

	public void reportInvalidTypesForUnaryOperator(TextSpan span, UnaryOperator operator, Type operand) {
		report(span, "The unary operator '" + operator + "' cannot be applied to '" + operand + "'");
	}

	public void reportInvalidTypesForBinaryOperator(TextSpan span, Type left, BinaryOperator operator, Type right) {
		report(span, "The binary operator '" + operator + "' cannot be applied to '" + left + "' and '" + right + "'");
	}

	public void reportMissingArgument(TextSpan span, String identifier) {
		report(span, "Missing value for argument '" + identifier + "'");
	}

	public void reportInvalidCallingTarget(TextSpan span) {
		report(span, "Invalid calling target");
	}

	public void reportUnexpectedArgsCount(TextSpan span, int expectedCount, int actualCount) {
		report(span, "Expected '" + expectedCount + "' arguments but got '" + actualCount + "'");
	}

	public void reportMixedKeyedAndPositionalArgs(TextSpan span) {
		report(span, "Mixed keyed and positional arguments");
	}

	public void reportMismatchedDefaultValueType(TextSpan span, Type valueType, Type parameterType) {
		report(span, "A default value of type '" + valueType + "' cannot be assigned to a parameter of type '" + parameterType + "'");
	}

	public void reportMismatchedVariableValueType(TextSpan span, Type valueType, Type variableType) {
		report(span, "A value of type '" + valueType + "' cannot be assigned to a variable of type '" + variableType + "'");
	}

	public void reportMismatchedArgumentType(TextSpan span, Type argumentType, Type parameterType) {
		report(span, "An argument of type '" + argumentType + "' cannot be passed as a parameter of type '" + parameterType + "'");
	}

	public void reportDefaultArgsInFunctionExpression(TextSpan span) {
		report(span, "Default arguments values are not allowed in function expressions");
	}

	public void reportLabeledArgsForFuncExpressionCall(TextSpan span) {
		report(span, "Labeled arguments are not allowed for function expressions");
	}

	public void reportAlreadyExistentParameter(TextSpan span, String identifier) {
		report(span, "Already existent parameter '" + identifier + "'");
	}

	public void reportAlreadyPassedArgument(TextSpan span, String identifier) {
		report(span, "Argument '" + identifier + "' is already passed");
	}

	public void reportAlreadyExistentSymbol(TextSpan span, String identifier) {
		report(span, "Already existent symbol '" + identifier + "'");
	}

	public void reportUnknownParameterName(TextSpan span, String identifier) {
		report(span, "Unknown parameter name '" + identifier + "'");
	}

	public void reportUnknownSymbol(TextSpan span, String identifier) {
		report(span, "Unknown symbol '" + identifier + "'");
	}

	public void reportUnknownType(TextSpan span, String identifier) {
		report(span, "Unknown type '" + identifier + "'");
	}

	public void reportIllegalCharacter(TextSpan span, char character) {
		report(span, "Illegal character '" + character + "'");
	}

	public void reportInvalidLiteral(TextSpan span, String literal) {
		report(span, "Invalid literal '" + literal + "'");
	}

	public void reportUnexpectedToken(TextSpan span, TokenType token) {
		report(span, "Unexpected '" + token + "'");
	}

	public void report(TextSpan span, String message) {
		errors.add(new ErrorMessage(location, span, message));
	}
}
