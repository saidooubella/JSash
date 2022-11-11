package rewriter;

import errors.ErrorsReporter;
import parser.expressions.*;
import parser.expressions.strings.ExpressionStringElement;
import parser.expressions.strings.IdentifierStringElement;
import parser.expressions.strings.RawStringElement;
import parser.expressions.strings.StringElement;
import parser.statements.*;
import parser.wrappers.*;
import rewriter.expressions.*;
import rewriter.operators.BinaryOperator;
import rewriter.operators.UnaryOperator;
import rewriter.statements.*;
import rewriter.symbols.*;
import span.TextSpan;
import types.*;
import utils.SeparatedList;
import utils.Types;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Rewriter {

	private final ScopeTracker scopeTracker;
	private final SymbolsTable symbolsTable;

	private final ErrorsReporter reporter;
	private final FileRootNode root;

	public Rewriter(ErrorsReporter reporter, FileRootNode root) {
		this.symbolsTable = new SymbolsTable();
		this.scopeTracker = new ScopeTracker();
		this.reporter = reporter;
		this.root = root;
	}

	public RewriterResult rewrite() {

		final List<Statement> statements = new ArrayList<>();

		final List<Type> recordTypes = defineRecords(root.records);
		final List<ExtensionSymbol> extensionSymbols = defineExtensions(root.extensions);
		final List<VariableSymbol> variableSymbols = defineVariables(root.variables);
		final List<FunctionSymbol> functionSymbols = defineFunctions(root.functions);

		final List<Statement> variables = new ArrayList<>();

		for (int i = 0; i < recordTypes.size(); i++) {
			statements.add(resolveRecord(root.records.get(i), recordTypes.get(i)));
		}

		for (int i = 0; i < variableSymbols.size(); i++) {
			final VariableStatementNode statement = root.variables.get(i);
			final Expression value = rewriteExpression(statement.value.value);
			final VariableSymbol symbol = variableSymbols.get(i);
			variables.add(resolveVariable(value, symbol, statement.span()));
		}

		for (int i = 0; i < functionSymbols.size(); i++) {
			statements.add(resolveFunction(root.functions.get(i), functionSymbols.get(i)));
		}

		for (int i = 0; i < extensionSymbols.size(); i++) {
			statements.add(resolveExtension(root.extensions.get(i), extensionSymbols.get(i)));
		}

		statements.addAll(variables);

		return new RewriterResult(statements, mainEntryPoint());
	}

	private static final Predicate<Symbol> MainEntryFilter = symbol -> {
		if (!(symbol instanceof FunctionSymbol)) return false;
		final FunctionType type = ((FunctionSymbol) symbol).type();
		return type.parameters.isEmpty() && type.returnType == PrimitiveType.Unit;
	};

	private Optional<Symbol> mainEntryPoint() {
		return symbolsTable.getSymbol("main").filter(MainEntryFilter);
	}

	private List<Type> defineRecords(List<RecordStatementNode> records) {

		final List<Type> symbols = new ArrayList<>(records.size());

		for (final RecordStatementNode statement : records) {
			symbols.add(defineRecord(statement));
		}

		return symbols;
	}

	private Type defineRecord(RecordStatementNode statement) {

		final Type recordType = new RecordType(statement.identifier.text);

		if (!statement.identifier.text.isEmpty() && !symbolsTable.putType(recordType)) {
			reporter.reportAlreadyExistentSymbol(statement.identifier.span(), statement.identifier.text);
		}

		return recordType;
	}

	private Statement resolveRecord(RecordStatementNode statement, Type type) {

		final List<ParameterSymbol> parameters = rewriteParameters(statement.fields.params, false);
		final FunctionType function = new FunctionType(Types.fromTyped(parameters), type);
		final RecordSymbol record = new RecordSymbol(type.name(), parameters, function);

		if (!statement.identifier.text.isEmpty()) {
			symbolsTable.putSymbol(record);
		}

		return new RecordStatement(record, statement.span());
	}

	private List<ExtensionSymbol> defineExtensions(List<ExtensionStatementNode> extensions) {

		final List<ExtensionSymbol> symbols = new ArrayList<>(extensions.size());

		for (final ExtensionStatementNode statement : extensions) {
			symbols.add(defineExtension(statement));
		}

		return symbols;
	}

	private ExtensionSymbol defineExtension(ExtensionStatementNode statement) {

		final Type receiverType = TypesResolver.resolve(symbolsTable, reporter, statement.receiver.type);
		final ParameterSymbol receiver = new ParameterSymbol("self", receiverType);
		final List<ParameterSymbol> parameters = rewriteParameters(statement.parameters.params, false);
		final Type returnType = TypesResolver.resolve(symbolsTable, reporter, statement.returnType.type);
		final FunctionType type = new FunctionType(Types.fromTyped(parameters), returnType);
		final ExtensionSymbol extension = new ExtensionSymbol(receiver, statement.identifier.text, parameters, type);

		if (!statement.identifier.text.isEmpty() && receiverType.isNotError() && !symbolsTable.putExtension(extension)) {
			reporter.reportAlreadyExistentSymbol(statement.identifier.span(), statement.identifier.text);
		}

		return extension;
	}

	private Statement resolveExtension(ExtensionStatementNode statement, ExtensionSymbol extension) {
		final Optional<ParameterSymbol> receiver = Optional.of(extension.receiver);
		final Type returnType = extension.type().returnType;
		final List<Statement> body = rewriteFunctionBody(receiver, extension.parameters, returnType, statement.body);
		return new ExtensionStatement(extension, body, statement.span());
	}

	private List<FunctionSymbol> defineFunctions(List<FunctionStatementNode> functions) {

		final List<FunctionSymbol> symbols = new ArrayList<>(functions.size());

		for (final FunctionStatementNode statement : functions) {
			symbols.add(defineFunction(statement));
		}

		return symbols;
	}

	private FunctionSymbol defineFunction(FunctionStatementNode statement) {

		final List<ParameterSymbol> parameters = rewriteParameters(statement.parameters.params, false);
		final Type returnType = TypesResolver.resolve(symbolsTable, reporter, statement.returnType.type);
		final FunctionType type = new FunctionType(Types.fromTyped(parameters), returnType);
		final FunctionSymbol function = new FunctionSymbol(statement.identifier.text, parameters, type);

		if (!statement.identifier.text.isEmpty() && !symbolsTable.putSymbol(function)) {
			reporter.reportAlreadyExistentSymbol(statement.identifier.span(), statement.identifier.text);
		}

		return function;
	}

	private Statement resolveFunction(FunctionStatementNode statement, FunctionSymbol function) {
		final Type returnType = function.type().returnType;
		final List<Statement> body = rewriteFunctionBody(Optional.empty(), function.parameters, returnType, statement.body);
		return new FunctionStatement(function, body, statement.span());
	}

	private List<VariableSymbol> defineVariables(List<VariableStatementNode> variables) {

		final List<VariableSymbol> symbols = new ArrayList<>(variables.size());

		for (final VariableStatementNode statement : variables) {

			if (!(statement.declaration instanceof DeclarationNode.Simple)) {
				reporter.report(statement.declaration.span(), "Top level destructuring is not allowed.");
				continue;
			}

			symbols.add(defineVariable((DeclarationNode.Simple) statement.declaration, statement.isReadOnly));
		}

		return symbols;
	}

	private VariableSymbol defineVariable(DeclarationNode.Simple declaration, boolean isReadOnly) {

		final Type type = TypesResolver.resolve(symbolsTable, reporter, declaration.type);
		final VariableSymbol variable = new VariableSymbol(declaration.label.identifier.text, type, isReadOnly, false);

		if (!declaration.label.identifier.text.isEmpty() && !symbolsTable.putSymbol(variable)) {
			reporter.reportAlreadyExistentSymbol(declaration.label.identifier.span(), declaration.label.identifier.text + "'");
		}

		return variable;
	}

	private Statement resolveVariable(Expression value, VariableSymbol variable, TextSpan span) {

		variable.isInitialized = true;

		if (!value.type().assignableTo(variable.type())) {
			reporter.reportMismatchedVariableValueType(value.span(), value.type(), variable.type());
		}

		return new VariableStatement(variable, value, span);
	}

	private List<Statement> rewriteFunctionBody(Optional<ParameterSymbol> receiver, List<ParameterSymbol> parameters, Type returnType, FunctionBodyNode body) {

		final boolean hasExpressionBody = body instanceof FunctionBodyNode.Expression;
		scopeTracker.pushScope(new Scope.Function(returnType, hasExpressionBody));
		symbolsTable.pushScope();

		try {

			receiver.ifPresent(symbolsTable::putSymbol);

			for (final ParameterSymbol parameter : parameters) {
				symbolsTable.putSymbol(parameter);
			}

			if (body instanceof FunctionBodyNode.Expression) {
				return rewriteFunctionExpressionBody((FunctionBodyNode.Expression) body, returnType);
			} else if (body instanceof FunctionBodyNode.Block) {
				return rewriteFunctionBlockBody((FunctionBodyNode.Block) body, returnType);
			} else {
				throw new IllegalStateException(body.getClass().getSimpleName());
			}

		} finally {
			symbolsTable.popScope();
			scopeTracker.popScope();
		}
	}

	private List<Statement> rewriteFunctionBlockBody(FunctionBodyNode.Block body, Type returnType) {

		final List<Statement> statements = rewriteStatements(body.statements);

		if (ReturnsResolver.resolve(reporter, statements, returnType) == ReturnsResolver.State.Missing) {
			reporter.reportMissingReturn(body.closeCurly.span(), returnType);
		}

		return statements;
	}

	private List<Statement> rewriteFunctionExpressionBody(FunctionBodyNode.Expression body, Type returnType) {

		final List<Statement> statements = new ArrayList<>(1);
		final Expression expression = rewriteExpression(body.expression);
		statements.add(new ExpressionStatement(new ReturnExpression(Optional.of(expression), expression.span())));

		if (!expression.type().assignableTo(returnType)) {
			reporter.reportMismatchedReturnValueType(expression.span(), expression.type(), returnType);
		}

		return statements;
	}

	private List<ParameterSymbol> rewriteParameters(SeparatedList<ParameterNode, ?> params, boolean isFunctionExpression) {

		final List<ParameterSymbol> parameters = new ArrayList<>(params.elementsSize());
		final Set<String> namesLookup = new HashSet<>();

		for (final ParameterNode parameterNode : params) {

			final String identifier = parameterNode.identifier.text;
			final Type type = TypesResolver.resolve(symbolsTable, reporter, parameterNode.type.type);
			final Optional<Expression> defaultValue = rewriteOptionalValue(parameterNode.value);
			final ParameterSymbol parameter = new ParameterSymbol(identifier, type, defaultValue);

			if (defaultValue.isPresent()) {

				final Expression defaultVal = defaultValue.get();

				if (isFunctionExpression) {
					reporter.reportDefaultArgsInFunctionExpression(defaultVal.span());
				}

				if (!defaultVal.type().assignableTo(type)) {
					reporter.reportMismatchedDefaultValueType(defaultVal.span(), defaultVal.type(), parameter.type());
				}
			}

			if (!identifier.isEmpty() && !namesLookup.add(identifier)) {
				reporter.reportAlreadyExistentParameter(parameterNode.span(), identifier);
			}

			parameters.add(parameter);
		}

		return parameters;
	}

	////////////////////////////////////////////////////////////////////////////////

	private List<Statement> rewriteStatements(List<StatementNode> statementsNode) {

		final List<Statement> statements = new ArrayList<>(statementsNode.size());

		for (final StatementNode statement : statementsNode) {
			statements.add(rewriteStatement(statement));
		}

		return statements;
	}

	private Statement rewriteStatement(StatementNode statement) {
		if (statement instanceof VariableStatementNode) {
			return rewriteVariableStatement((VariableStatementNode) statement);
		} else if (statement instanceof FunctionStatementNode) {
			return rewriteFunctionStatement((FunctionStatementNode) statement);
		} else if (statement instanceof ExpressionStatementNode) {
			return rewriteExpressionStatement((ExpressionStatementNode) statement);
		} else if (statement instanceof ExtensionStatementNode) {
			return rewriteExtensionStatement((ExtensionStatementNode) statement);
		} else if (statement instanceof RecordStatementNode) {
			return rewriteRecordStatement((RecordStatementNode) statement);
		} else {
			throw new IllegalStateException(statement.getClass().getSimpleName());
		}
	}

	private Statement rewriteRecordStatement(RecordStatementNode statement) {
		return resolveRecord(statement, defineRecord(statement));
	}

	private static final AtomicInteger ids = new AtomicInteger();

	private static final Supplier<String> LabelSupplier = () -> ids.getAndIncrement() + "Label";

	private static final Function<LabelNode, String> LabelMapper = label -> label.identifier.text;

	private StatementSet rewriteDeclaration(DeclarationNode rootDeclaration, Expression rootValue, boolean readOnly, TextSpan span) {

		final Deque<Pair<DeclarationNode, Expression>> stack = new ArrayDeque<>();
		stack.push(Pair.of(rootDeclaration, rootValue));

		final List<Statement> statements = new ArrayList<>();

		while (!stack.isEmpty()) {

			final Pair<DeclarationNode, Expression> element = stack.pop();
			final DeclarationNode declarationNode = element.first;
			final Expression declarationValue = element.second;

			if (declarationNode instanceof DeclarationNode.Distructure) {

				final DeclarationNode.Distructure distructure = (DeclarationNode.Distructure) declarationNode;

				if (declarationValue.type() instanceof TupleType) {

					final List<Type> types = ((TupleType) declarationValue.type()).types;
					final String label = distructure.label.map(LabelMapper).orElseGet(LabelSupplier);
					final VariableSymbol symbol = new VariableSymbol(label, declarationValue.type(), true, true);
					final IdentifierExpression getter = new IdentifierExpression(symbol, distructure.span());

					if (distructure.label.isPresent() && !label.isEmpty() && !symbolsTable.putSymbol(symbol)) {
						reporter.reportUnknownSymbol(distructure.label.get().identifier.span(), label);
					}

					for (int i = distructure.declarations.elementsSize() - 1; i >= 0; i--) {

						if (i >= types.size()) {
							reporter.report(declarationValue.span(), "There is no element for this declaration");
							continue;
						}

						final DeclarationNode declaration = distructure.declarations.getElement(i);
						final Expression index = new LiteralExpression(i, PrimitiveType.Integer, declaration.span());
						final Expression value = new IndexingExpression(getter, index, types.get(i), declaration.span());
						stack.push(Pair.of(declaration, value));
					}

					statements.add(new VariableStatement(symbol, declarationValue, declarationValue.span()));

				} else if (declarationValue.type() instanceof RecordType) {

					final RecordSymbol record = (RecordSymbol) symbolsTable.getSymbol(declarationValue.type().name()).get();
					final String label = distructure.label.map(LabelMapper).orElseGet(LabelSupplier);
					final VariableSymbol symbol = new VariableSymbol(label, declarationValue.type(), true, true);
					final IdentifierExpression getter = new IdentifierExpression(symbol, distructure.span());

					if (distructure.label.isPresent() && !label.isEmpty() && !symbolsTable.putSymbol(symbol)) {
						reporter.reportUnknownSymbol(distructure.label.get().identifier.span(), label);
					}

					for (int i = distructure.declarations.elementsSize() - 1; i >= 0; i--) {

						if (i >= record.parameters.size()) {
							reporter.report(declarationValue.span(), "There is no element for this declaration");
							continue;
						}

						final DeclarationNode declaration = distructure.declarations.getElement(i);
						final Expression value = new FieldGetExpression(getter, record.parameters.get(i), declaration.span());
						stack.push(Pair.of(declaration, value));
					}

					statements.add(new VariableStatement(symbol, declarationValue, declarationValue.span()));

				} else {
					reporter.report(declarationNode.span(), "This value cannot be distructured '" + declarationValue.type().name() + "'");
				}

			} else if (declarationNode instanceof DeclarationNode.Simple) {

				final DeclarationNode.Simple simple = (DeclarationNode.Simple) declarationNode;
				final VariableSymbol variable = defineVariable(simple, readOnly);
				statements.add(resolveVariable(declarationValue, variable, declarationValue.span()));

			} else {
				throw new IllegalStateException(declarationNode.getClass().getSimpleName());
			}
		}

		return new StatementSet(statements, span);
	}

	private Statement rewriteVariableStatement(VariableStatementNode statement) {
		return rewriteDeclaration(statement.declaration, rewriteExpression(statement.value.value), statement.isReadOnly, statement.span());
	}

	private Statement rewriteExtensionStatement(ExtensionStatementNode statement) {
		return resolveExtension(statement, defineExtension(statement));
	}

	private Statement rewriteFunctionStatement(FunctionStatementNode statement) {
		return resolveFunction(statement, defineFunction(statement));
	}

	private Statement rewriteExpressionStatement(ExpressionStatementNode statement) {
		return new ExpressionStatement(rewriteExpression(statement.expression));
	}

	private Optional<Expression> rewriteOptionalValue(Optional<ValueNode> expression) {
		return expression.map(valueNode -> rewriteExpression(valueNode.value));
	}

	private Optional<Expression> rewriteOptionalExpression(Optional<ExpressionNode> expression) {
		return expression.map(this::rewriteExpression);
	}

	private List<Expression> rewriteExpressions(SeparatedList<ExpressionNode, ?> expressionsNodes) {
		final List<Expression> expressions = new ArrayList<>(expressionsNodes.elementsSize());
		for (final ExpressionNode expressionNode : expressionsNodes) {
			expressions.add(rewriteExpression(expressionNode));
		}
		return expressions;
	}

	private Expression rewriteExpression(ExpressionNode expression) {
		if (expression instanceof LiteralExpressionNode) {
			return rewriteLiteralExpression((LiteralExpressionNode) expression);
		} else if (expression instanceof FunctionExpressionNode) {
			return rewriteFunctionExpression((FunctionExpressionNode) expression);
		} else if (expression instanceof ParenthesizedExpressionNode) {
			return rewriteParenthesizedExpression((ParenthesizedExpressionNode) expression);
		} else if (expression instanceof BinaryExpressionNode) {
			return rewriteBinaryExpression((BinaryExpressionNode) expression);
		} else if (expression instanceof UnaryExpressionNode) {
			return rewriteUnaryExpression((UnaryExpressionNode) expression);
		} else if (expression instanceof CallExpressionNode) {
			return rewriteCallExpression((CallExpressionNode) expression);
		} else if (expression instanceof IdentifierExpressionNode) {
			return rewriteIdentifierExpression((IdentifierExpressionNode) expression);
		} else if (expression instanceof ReturnExpressionNode) {
			return rewriteReturnExpression((ReturnExpressionNode) expression);
		} else if (expression instanceof AssignmentExpressionNode) {
			return rewriteAssignmentExpression((AssignmentExpressionNode) expression);
		} else if (expression instanceof NoneExpressionNode) {
			return rewriteNoneExpression((NoneExpressionNode) expression);
		} else if (expression instanceof StringExpressionNode) {
			return rewriteStringExpression((StringExpressionNode) expression);
		} else if (expression instanceof TupleExpressionNode) {
			return rewriteTupleExpression((TupleExpressionNode) expression);
		} else if (expression instanceof TernaryExpressionNode) {
			return rewriteTernaryExpression((TernaryExpressionNode) expression);
		} else if (expression instanceof SelfExpressionNode) {
			return rewriteSelfExpression((SelfExpressionNode) expression);
		} else if (expression instanceof GetExpressionNode) {
			return rewriteGetExpression((GetExpressionNode) expression);
		} else {
			throw new IllegalStateException(expression.getClass().getSimpleName());
		}
	}

	private Expression rewriteGetExpression(GetExpressionNode expression) {

		final Expression target = rewriteExpression(expression.target);
		final String identifier = expression.identifier.text;

		out: if (target.type().isNotError() && !identifier.isEmpty()) {

			if (target.type() instanceof RecordType) {
				final RecordSymbol recordSymbol = (RecordSymbol) symbolsTable.getSymbol(target.type().name()).get();
				final Optional<ParameterSymbol> field = getParameterWithName(recordSymbol.parameters, identifier);
				if (field.isPresent()) return new FieldGetExpression(target, field.get(), expression.span());
			}

			final Optional<ExtensionSymbol> extension = symbolsTable.getExtension(target.type(), identifier);
			if (extension.isEmpty()) {
				reporter.reportUnknownSymbol(expression.identifier.span(), identifier);
				break out;
			}

			return new ExtensionGetExpression(target, extension.get(), expression.span());
		}

		return new ErrorExpression(expression.span());
	}

	private Optional<ParameterSymbol> getParameterWithName(List<ParameterSymbol> parameters, String name) {
		for (final ParameterSymbol param : parameters) {
			if (param.name().equals(name)) {
				return Optional.of(param);
			}
		}
		return Optional.empty();
	}

	private Expression rewriteSelfExpression(SelfExpressionNode expression) {
		return resolveIdentifier("self", expression.span());
	}

	private Expression rewriteTernaryExpression(TernaryExpressionNode expression) {

		final Expression condition = rewriteExpression(expression.condition);
		final Expression thenExpr = rewriteExpression(expression.thenExpr);
		final Expression elseExpr = rewriteExpression(expression.elseExpr);

		if (!condition.type().assignableTo(PrimitiveType.Boolean)) {
			reporter.report(condition.span(), "The condition must be of type Boolean");
		}

		final Type type = thenExpr.type().isNotError() && elseExpr.type().isNotError()
			? Types.dominator(thenExpr.type(), elseExpr.type()).orElse(Types.anyNone(thenExpr.type(), elseExpr.type()) ? Types.AnyNone : PrimitiveType.Any)
			: Types.any(thenExpr.type(), elseExpr.type());

		return new TernaryExpression(condition, thenExpr, elseExpr, type, expression.span());
	}

	private Expression rewriteTupleExpression(TupleExpressionNode expression) {
		final List<Expression> values = rewriteExpressions(expression.values);
		final Type type = new TupleType(Types.fromTyped(values));
		return new TupleExpression(values, type, expression.span());
	}

	private Expression rewriteStringExpression(StringExpressionNode expression) {

		Expression left = null;

		for (final StringElement element : expression.elements) {

			final Expression right;

			if (element instanceof RawStringElement) {
				final RawStringElement string = (RawStringElement) element;
				right = new LiteralExpression(string.string.text, PrimitiveType.String, string.string.span());
			} else if (element instanceof IdentifierStringElement) {
				final IdentifierStringElement string = (IdentifierStringElement) element;
				right = resolveIdentifier(string.identifier.text, string.identifier.span());
			} else if (element instanceof ExpressionStringElement) {
				final ExpressionStringElement string = (ExpressionStringElement) element;
				right = rewriteExpression(string.expression);
			} else {
				throw new IllegalStateException(element.getClass().getSimpleName());
			}

			left = left == null ? right : new BinaryExpression(left, BinaryOperator.Addition, right, PrimitiveType.String, left.span().plus(right.span()));
		}

		return left != null ? left : new LiteralExpression("", PrimitiveType.String, expression.span());
	}

	private Expression rewriteNoneExpression(NoneExpressionNode expression) {
		return new NoneExpression(expression.span());
	}

	private Expression rewriteAssignmentExpression(AssignmentExpressionNode expression) {

		final Expression target = rewriteExpression(expression.target);
		final Expression value = rewriteExpression(expression.value.value);

		if (target instanceof IdentifierExpression && ((IdentifierExpression) target).symbol instanceof VariableSymbol) {

			final VariableSymbol variable = (VariableSymbol) ((IdentifierExpression) target).symbol;

			if (variable.isInitialized && variable.isReadOnly) {
				reporter.reportMutatingReadOnlyVariable(expression.value.equal.span());
			}

			if (!value.type().assignableTo(variable.type())) {
				reporter.reportMismatchedVariableValueType(value.span(), value.type(), variable.type());
			}

			return new AssignmentExpression(variable, value, expression.span());
		}

		if (target instanceof FieldGetExpression) {

			final FieldGetExpression fieldGet = (FieldGetExpression) target;

			if (!value.type().assignableTo(fieldGet.field.type())) {
				reporter.reportMismatchedVariableValueType(value.span(), value.type(), fieldGet.field.type());
			}

			return new FieldSetExpression(fieldGet.target, fieldGet.field, value, expression.span());
		}

		if (target.type().isNotError()) {
			reporter.reportInvalidAssignmentTarget(target.span());
		}

		return new ErrorExpression(expression.span());
	}

	private Expression rewriteReturnExpression(ReturnExpressionNode expression) {

		final Optional<Expression> value = rewriteOptionalExpression(expression.value);
		final Optional<Scope.Function> optionalFunction = scopeTracker.nearestFunction();

		out: if (optionalFunction.isPresent()) {
			final Scope.Function function = optionalFunction.get();
			if (function.hasExpressionBody) {
				reporter.reportReturnUsageInExpressionBody(expression.span());
			} else if (value.isPresent()) {
				if (value.get().type().assignableTo(function.returnType)) break out;
				reporter.reportMismatchedReturnValueType(value.get().span(), value.get().type(), function.returnType);
			} else if (function.returnType != PrimitiveType.Unit) {
				reporter.reportMissingReturnValue(expression.returnKeyword.span(), function.returnType);
			}
		} else {
			reporter.reportReturnOutOfFunction(expression.span());
		}

		return new ReturnExpression(value, expression.span());
	}

	private Expression rewriteCallExpression(CallExpressionNode expression) {

		final Expression target = rewriteExpression(expression.target);

		if (target instanceof IdentifierExpression && ((IdentifierExpression) target).symbol instanceof FunctionSymbol) {
			final FunctionSymbol function = (FunctionSymbol) ((IdentifierExpression) target).symbol;
			return resolveCallExpression(expression, target, function.parameters, function.type());
		}

		if (target instanceof IdentifierExpression && ((IdentifierExpression) target).symbol instanceof RecordSymbol) {
			final RecordSymbol record = (RecordSymbol) ((IdentifierExpression) target).symbol;
			return resolveCallExpression(expression, target, record.parameters, record.type());
		}

		if (target instanceof ExtensionGetExpression && ((ExtensionGetExpression) target).symbol != null) {
			final ExtensionSymbol extension = ((ExtensionGetExpression) target).symbol;
			return resolveCallExpression(expression, target, extension.parameters, extension.type());
		}

		if (target.type() instanceof FunctionType) {
			final FunctionType function = (FunctionType) target.type();
			final List<Expression> arguments = rewriteArguments(expression.arguments, true);
			validateCallArguments(arguments, function.parameters, expression);
			return new CallExpression(target, arguments, function.returnType, expression.span());
		}

		rewriteArguments(expression.arguments, false);

		if (target.type().isNotError()) {
			reporter.reportInvalidCallingTarget(target.span());
		}

		return new ErrorExpression(expression.span());
	}

	private CallExpression resolveCallExpression(CallExpressionNode expression, Expression target, List<ParameterSymbol> parameters, FunctionType type) {

		final List<Expression> arguments = rewriteArguments(expression.arguments, false);

		final Expression[] args = new Expression[Math.max(arguments.size(), parameters.size())];

		boolean mixedArguments = true;

		for (int i = 0; i < arguments.size(); i++) {
			final ArgumentNode argument = expression.arguments.getElement(i);
			if (argument.label.isPresent()) {
				final String label = argument.label.get().identifier.text;
				final int index = parameterIndex(parameters, label);
				if (index == -1) {
					reporter.reportUnknownParameterName(argument.label.get().span(), label);
				} else if (args[index] != null) {
					reporter.reportAlreadyPassedArgument(argument.label.get().span(), label);
					mixedArguments = false;
				} else {
					args[index] = arguments.get(i);
					if (mixedArguments && index != i) {
						mixedArguments = false;
					}
				}
			} else if (mixedArguments) {
				args[i] = arguments.get(i);
			} else {
				final TextSpan span = expression.openParent.span().plus(expression.closeParent.span());
				reporter.reportMixedKeyedAndPositionalArgs(span);
				break;
			}
		}

		// TODO: To be changed with a bridge function call instead of
		//       inlining the default value to the calling site.
		//       After doing so allow previous arguments to be used
		//       as defaults for the ones following.
		int i = 0;
		for (; i < parameters.size(); i++) {
			final ParameterSymbol param = parameters.get(i);
			if (args[i] != null) continue;
			if (param.defaultValue.isPresent()) {
				args[i] = param.defaultValue.get();
			} else {
				if (mixedArguments) break;
				reporter.reportMissingArgument(expression.closeParent.span(), param.name());
				args[i] = new ErrorExpression(expression.span());
			}
		}

		final List<Expression> actualArgs = Arrays.asList(args).subList(0, i);
		validateCallArguments(actualArgs, type.parameters, expression);

		return new CallExpression(target, actualArgs, type.returnType, expression.span());
	}

	private void validateCallArguments(List<Expression> arguments, List<Type> parameters, CallExpressionNode expression) {

		if (arguments.size() != parameters.size()) {
			final TextSpan span = expression.openParent.span().plus(expression.closeParent.span());
			reporter.reportUnexpectedArgsCount(span, parameters.size(), arguments.size());
		}

		final int length = Math.min(arguments.size(), parameters.size());
		for (int i = 0; i < length; i++) {
			final Type parameter = parameters.get(i);
			final Expression argument = arguments.get(i);
			if (!argument.type().assignableTo(parameter)) {
				reporter.reportMismatchedArgumentType(argument.span(), argument.type(), parameter);
			}
		}
	}

	private static int parameterIndex(List<ParameterSymbol> parameters, String label) {
		for (int i = 0; i < parameters.size(); i++)
			if (parameters.get(i).name().equals(label))
				return i;
		return -1;
	}

	private List<Expression> rewriteArguments(SeparatedList<ArgumentNode, ?> args, boolean isFunctionExpression) {

		final List<Expression> arguments = new ArrayList<>(args.elementsSize());

		for (final ArgumentNode argument : args) {

			arguments.add(rewriteExpression(argument.value));

			if (isFunctionExpression && argument.label.isPresent()) {
				reporter.reportLabeledArgsForFuncExpressionCall(argument.span());
			}
		}

		return arguments;
	}

	private Expression rewriteFunctionExpression(FunctionExpressionNode expression) {

		final List<ParameterSymbol> parameters = rewriteParameters(expression.parameters.params, true);
		final Type returnType = TypesResolver.resolve(symbolsTable, reporter, expression.returnType.type);
		final FunctionType type = new FunctionType(Types.fromTyped(parameters), returnType);
		final List<Statement> statements = rewriteFunctionBody(Optional.empty(), parameters, returnType, expression.body);

		return new FunctionExpression(parameters, statements, type, expression.span());
	}

	private Expression rewriteBinaryExpression(BinaryExpressionNode expression) {

		final Expression left = rewriteExpression(expression.left);
		final Expression right = rewriteExpression(expression.right);

		if (left.type().isError() || right.type().isError())
			return new ErrorExpression(expression.span());

		final BinaryOperator operator = BinaryOperator.from(expression.operator.type);
		final Optional<Type> type = Types.fromBinaryOperation(left.type(), operator, right.type());

		if (type.isPresent()) return new BinaryExpression(left, operator, right, type.get(), expression.span());

		reporter.reportInvalidTypesForBinaryOperator(expression.operator.span(), left.type(), operator, right.type());
		return new ErrorExpression(expression.span());
	}

	private Expression rewriteUnaryExpression(UnaryExpressionNode expression) {

		final Expression operand = rewriteExpression(expression.operand);
		if (operand.type().isError()) return new ErrorExpression(expression.span());

		final UnaryOperator operator = UnaryOperator.from(expression.operator.type);
		final Optional<Type> type = Types.fromUnaryOperation(operator, operand.type());

		if (type.isPresent()) return new UnaryExpression(operator, operand, type.get(), expression.span());

		reporter.reportInvalidTypesForUnaryOperator(expression.operator.span(), operator, operand.type());
		return new ErrorExpression(expression.span());
	}

	private Expression rewriteIdentifierExpression(IdentifierExpressionNode expression) {
		final String identifier = expression.identifier.text;
		if (identifier.isEmpty()) return new ErrorExpression(expression.span());
		return resolveIdentifier(identifier, expression.span());
	}

	private Expression resolveIdentifier(String identifier, TextSpan span) {

		final Optional<Symbol> result = symbolsTable.getSymbol(identifier);

		if (result.isPresent()) {
			final Symbol symbol = result.get();
			if (symbol instanceof VariableSymbol && !((VariableSymbol) symbol).isInitialized) {
				reporter.reportUninitializedVariable(span);
			}
			return new IdentifierExpression(symbol, span);
		}

		reporter.reportUnknownSymbol(span, identifier);
		return new ErrorExpression(span);
	}

	private Expression rewriteParenthesizedExpression(ParenthesizedExpressionNode expression) {
		return new ParenthesizedExpression(rewriteExpression(expression.expression), expression.span());
	}

	private Expression rewriteLiteralExpression(LiteralExpressionNode expression) {
		final Type type = Types.fromValue(expression.value);
		return new LiteralExpression(expression.value, type, expression.span());
	}
}

final class ReturnsResolver {

	private ReturnsResolver() {}

	public static State resolve(ErrorsReporter reporter, List<Statement> statements, Type returnType) {
		if (resolveStatements(reporter, statements) || returnType.isError()) return State.Valid;
		return returnType == PrimitiveType.Unit ? State.UnitFunction : State.Missing;
	}

	private static boolean resolveStatements(ErrorsReporter reporter, List<Statement> statements) {

		boolean result = false;

		for (Statement statement : statements) {
			if (result) {
				reporter.reportUnreachedStatement(statement.span());
			} else {
				result = resolveStatement(reporter, statement);
			}
		}

		return result;
	}

	private static boolean resolveStatement(ErrorsReporter reporter, Statement statement) {
		if (statement instanceof VariableStatement) {
			return resolveVariableStatement(reporter, (VariableStatement) statement);
		} else if (statement instanceof ExpressionStatement) {
			return resolveExpression(reporter, ((ExpressionStatement) statement).expression);
		} else {
			return false;
		}
	}

	private static boolean resolveVariableStatement(ErrorsReporter reporter, VariableStatement statement) {
		if (resolveExpression(reporter, statement.value)) {
			final TextSpan span = statement.span().start.plus(statement.value.span().start);
			reporter.reportUnreachedStatement(span);
			return true;
		}
		return false;
	}

	private static boolean resolveExpression(ErrorsReporter reporter, Expression expression) {
		if (expression instanceof CallExpression) {
			return resolveCallExpression(reporter, (CallExpression) expression);
		} else if (expression instanceof ReturnExpression) {
			return resolveReturnExpression(reporter, (ReturnExpression) expression);
		} else {
			return expression.type() == PrimitiveType.Nothing;
		}
	}

	private static boolean resolveReturnExpression(ErrorsReporter reporter, ReturnExpression expression) {
		if (expression.value.isPresent() && resolveExpression(reporter, expression.value.get())) {
			final TextSpan span = expression.span().start.plus(expression.value.get().span().start);
			reporter.reportUnreachedStatement(span);
		}
		return true;
	}

	private static boolean resolveCallExpression(ErrorsReporter reporter, CallExpression expression) {
		for (final Expression argument : expression.arguments) {
			if (resolveExpression(reporter, argument)) {
				final TextSpan exprSpan = expression.span();
				final TextSpan argSpan = argument.span();
				reporter.reportUnreachedStatement(exprSpan.start.plus(argSpan.start));
				reporter.reportUnreachedStatement(argSpan.end.plus(exprSpan.end));
				return true;
			}
		}
		return expression.type() == PrimitiveType.Nothing;
	}

	public enum State { Valid, UnitFunction, Missing }
}

final class TypesResolver {

	private TypesResolver() {}

	private static List<Type> resolveAll(SymbolsTable symbolsTable, ErrorsReporter reporter, SeparatedList<TypeNode, ?> typeNodes) {
		final List<Type> types = new ArrayList<>(typeNodes.elementsSize());
		for (final TypeNode typeNode : typeNodes) {
			types.add(resolve(symbolsTable, reporter, typeNode));
		}
		return types;
	}

	public static Type resolve(SymbolsTable symbolsTable, ErrorsReporter reporter, TypeNode type) {
		if (type instanceof TypeNode.Basic) {
			return resolveBasicType(symbolsTable, reporter, (TypeNode.Basic) type);
		} else if (type instanceof TypeNode.Function) {
			return resolveFunctionType(symbolsTable, reporter, (TypeNode.Function) type);
		} else if (type instanceof TypeNode.Union) {
			return resolveUnionType(symbolsTable, reporter, (TypeNode.Union) type);
		} else if (type instanceof TypeNode.Tuple) {
			return resolveTupleType(symbolsTable, reporter, (TypeNode.Tuple) type);
		} else if (type instanceof TypeNode.Parenthesized) {
			return resolveParenthesizedType(symbolsTable, reporter, (TypeNode.Parenthesized) type);
		} else {
			throw new IllegalStateException(type.getClass().getSimpleName());
		}
	}

	private static Type resolveTupleType(SymbolsTable symbolsTable, ErrorsReporter reporter, TypeNode.Tuple type) {
		return new TupleType(resolveAll(symbolsTable, reporter, type.types));
	}

	private static Type resolveParenthesizedType(SymbolsTable symbolsTable, ErrorsReporter reporter, TypeNode.Parenthesized type) {
		return resolve(symbolsTable, reporter, type.type);
	}

	private static Type resolveUnionType(SymbolsTable symbolsTable, ErrorsReporter reporter, TypeNode.Union type) {
		return new UnionType(resolve(symbolsTable, reporter, type.left), resolve(symbolsTable, reporter, type.right));
	}

	private static Type resolveFunctionType(SymbolsTable symbolsTable, ErrorsReporter reporter, TypeNode.Function type) {
		return new FunctionType(resolveAll(symbolsTable, reporter, type.parameters), resolve(symbolsTable, reporter, type.returnType));
	}

	private static Type resolveBasicType(SymbolsTable symbolsTable, ErrorsReporter reporter, TypeNode.Basic type) {

		final Optional<Type> result = symbolsTable.getType(type.name.text);

		if (!type.name.text.isEmpty() && result.isEmpty()) {
			reporter.reportUnknownType(type.span(), type.name.text);
		}

		return result.orElse(PrimitiveType.Error);
	}
}

final class ScopeTracker {

	private final ArrayDeque<Scope> scopes = new ArrayDeque<>();

	public void pushScope(Scope scope) {
		scopes.push(scope);
	}

	public void popScope() {
		scopes.pop();
	}

	public Optional<Scope.Function> nearestFunction() {
		for (final Scope scope : scopes)
			if (scope instanceof Scope.Function)
				return Optional.of((Scope.Function) scope);
		return Optional.empty();
	}
}

abstract class Scope {

	private Scope() {}

	public static final class Function extends Scope {

		public final boolean hasExpressionBody;
		public final Type returnType;

		public Function(Type returnType, boolean hasExpressionBody) {
			this.hasExpressionBody = hasExpressionBody;
			this.returnType = returnType;
		}
	}
}

final class Pair<K, V> {

	public final K first;
	public final V second;

	private Pair(K first, V second) {
		this.first = first;
		this.second = second;
	}

	public static <K, V> Pair<K, V> of(K first, V second) {
		return new Pair<>(first, second);
	}
}
