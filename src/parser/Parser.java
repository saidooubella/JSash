package parser;

import errors.ErrorsReporter;
import input.Input;
import parser.expressions.*;
import parser.expressions.strings.ExpressionStringElement;
import parser.expressions.strings.IdentifierStringElement;
import parser.expressions.strings.RawStringElement;
import parser.expressions.strings.StringElement;
import parser.statements.*;
import parser.wrappers.*;
import span.Spannable;
import span.TextSpan;
import tokens.Token;
import tokens.TokenType;
import utils.MutableSeparatedList;
import utils.SeparatedList;
import utils.TokenTypes;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Parser implements Closeable {

	private final ErrorsReporter reporter;
	private final Input<Token> tokens;

	public Parser(ErrorsReporter reporter, Input<Token> tokens) {
		this.reporter = reporter;
		this.tokens = tokens;
	}

	public FileRootNode parse() {
		
		final List<ExtensionStatementNode> extensions = new ArrayList<>();
		final List<FunctionStatementNode> functions = new ArrayList<>();
		final List<VariableStatementNode> variables = new ArrayList<>();
		final List<RecordStatementNode> records = new ArrayList<>();
		
		while (tokens.isNotDone()) {
			
			final StatementNode declaration = declaration(/*declarationsOnly: */ true);
			
			if (declaration instanceof ExtensionStatementNode) {
				extensions.add((ExtensionStatementNode) declaration);
			} else if (declaration instanceof FunctionStatementNode) {
				functions.add((FunctionStatementNode) declaration);
			} else if (declaration instanceof VariableStatementNode) {
				variables.add((VariableStatementNode) declaration);
			} else if (declaration instanceof RecordStatementNode) {
				records.add((RecordStatementNode) declaration);
			} else {
				throw new IllegalStateException(declaration.getClass().getSimpleName());
			}
		}
		
		return new FileRootNode(extensions, functions, variables, records);
	}
	
	private StatementNode declaration(boolean declarationsOnly) {

		if (match(TokenType.FunKeyword)) {
			return functionStatement();
		}
		
		if (match(TokenType.RecordKeyword)) {
			return recordStatement();
		}

		if (match(TokenType.DefKeyword)) {
			return variableStatement(false);
		}

		if (declarationsOnly || match(TokenType.LetKeyword)) {
			return variableStatement(true);
		}
		
		return statement();
	}

	private StatementNode recordStatement() {
		final Token recordKeyword = tokens.consume();
		final Token identifier = consume(TokenType.Identifier);
		final ParametersNode fields = parameters();
		return new RecordStatementNode(recordKeyword, identifier, fields);
	}

	private StatementNode functionStatement() {
		final Token funKeyword = tokens.consume();
		final Optional<ReceiverNode> receiver = receiver();
		final Token identifier = consume(TokenType.Identifier);
		final ParametersNode params = parameters();
		final TypeAnnotationNode returnType = typeAnnotation();
		final FunctionBodyNode body = functionBody();
		if (receiver.isPresent()) {
			return new ExtensionStatementNode(funKeyword, receiver.get(), identifier, params, returnType, body);
		}
		return new FunctionStatementNode(funKeyword, identifier, params, returnType, body);
	}
	
	private Optional<ReceiverNode> receiver() {
		return Optional.ofNullable(hasReceiver() ? new ReceiverNode(type(), consume(TokenType.Dot)) : null);
	}

	private StatementNode variableStatement(boolean isReadOnly) {
		final Token keyword = tokens.consume();
		final DeclarationNode dec = declaration();
		final ValueNode value = value();
		expectEndOfExpression(value);
		return new VariableStatementNode(keyword, dec, value, isReadOnly);
	}
	
	private boolean hasReceiver() {
		return matchAll(TokenType.Identifier, TokenType.Dot) || match(TokenType.OpenParent);
	}

	private StatementNode statement() {
		return expressionStatement();
	}

	private StatementNode expressionStatement() {
		final StatementNode statement = new ExpressionStatementNode(expression());
		expectEndOfExpression(statement);
		return statement;
	}

	private void expectEndOfExpression(Spannable last) {
		if (!match(TokenType.EOF) && onSameLine(last, tokens.current())) {
			reporter.reportExpectedEndOfStatement(tokens.current().span());
		}
	}

	private ExpressionNode expression() {
		return assignment();
	}

	private ExpressionNode assignment() {

		final ExpressionNode left = ternary();

		return match(TokenType.Equal) ? new AssignmentExpressionNode(left, value()) : left;
	}
	
	private ExpressionNode ternary() {
		
		final ExpressionNode condition = disjunction();
	
		if (match(TokenType.Question)) {
			final Token question = tokens.consume();
			final ExpressionNode thenExpr = expression();
			final Token colon = consume(TokenType.Colon);
			final ExpressionNode elseExpr = expression();
			return new TernaryExpressionNode(condition, question, thenExpr, colon, elseExpr);
		}
		
		return condition;
	}

	private ExpressionNode disjunction() {

		ExpressionNode left = conjunction();

		while (tokens.isNotDone() && match(TokenType.PipePipe)) {
			final Token operator = tokens.consume();
			final ExpressionNode right = conjunction();
			left = new BinaryExpressionNode(left, operator, right);
		}

		return left;
	}

	private ExpressionNode conjunction() {

		ExpressionNode left = equality();

		while (tokens.isNotDone() && match(TokenType.AmpersandAmpersand)) {
			final Token operator = tokens.consume();
			final ExpressionNode right = equality();
			left = new BinaryExpressionNode(left, operator, right);
		}

		return left;
	}

	private ExpressionNode equality() {

		ExpressionNode left = comparison();

		while (tokens.isNotDone() && matchAny(TokenType.EqualEqual, TokenType.BangEqual)) {
			final Token operator = tokens.consume();
			final ExpressionNode right = comparison();
			left = new BinaryExpressionNode(left, operator, right);
		}

		return left;
	}

	private ExpressionNode comparison() {

		ExpressionNode left = additive();

		while (tokens.isNotDone() && matchAny(TokenType.OpenAngle, TokenType.OpenAngleEqual, TokenType.CloseAngle, TokenType.CloseAngleEqual)) {
			final Token operator = tokens.consume();
			final ExpressionNode right = additive();
			left = new BinaryExpressionNode(left, operator, right);
		}

		return left;
	}

	private ExpressionNode additive() {

		ExpressionNode left = multiplicative();

		while (tokens.isNotDone() && matchAny(TokenType.Plus, TokenType.Minus)) {
			final Token operator = tokens.consume();
			final ExpressionNode right = multiplicative();
			left = new BinaryExpressionNode(left, operator, right);
		}

		return left;
	}

	private ExpressionNode multiplicative() {

		ExpressionNode left = prefix();

		while (tokens.isNotDone() && matchAny(TokenType.Slash, TokenType.Star)) {
			final Token operator = tokens.consume();
			final ExpressionNode right = prefix();
			left = new BinaryExpressionNode(left, operator, right);
		}

		return left;
	}

	private ExpressionNode prefix() {

		if (matchAny(TokenType.Plus, TokenType.Minus, TokenType.Bang)) {
			final Token operator = tokens.consume();
			final ExpressionNode operand = prefix();
			return new UnaryExpressionNode(operator, operand);
		}

		return postfix();
	}

	private ExpressionNode postfix() {

		ExpressionNode left = primary();

		while (tokens.isNotDone()) {
			if (match(TokenType.OpenParent) && left.span().end.equals(tokens.current().span().start)) {
				final Token openParent = tokens.consume();
				final SeparatedList<ArgumentNode, Token> arguments = arguments();
				final Token closeParent = consume(TokenType.CloseParent);
				left = new CallExpressionNode(left, openParent, arguments, closeParent);
			} else if (match(TokenType.Dot)) {
				final Token dot = tokens.consume();
				final Token identifier = consume(TokenType.Identifier);
				left = new GetExpressionNode(left, dot, identifier);
			} else break;
		}

		return left;
	}

	private ExpressionNode primary() {

		if (matchAny(TokenType.TrueKeyword, TokenType.FalseKeyword)) {
			final Token token = tokens.consume();
			final boolean value = token.type == TokenType.TrueKeyword;
			return new LiteralExpressionNode(value, token.span());
		}

		if (match(TokenType.Number)) {
			final Token literal = tokens.consume();
			return new LiteralExpressionNode(literal.value, literal.span());
		}

		if (match(TokenType.DoubleQuote)) {

			final Token leftQuote = consume(TokenType.DoubleQuote);
			final List<StringElement> elements = new ArrayList<>();

			while (tokens.isNotDone()) {
				if (match(TokenType.String)) {
					elements.add(new RawStringElement(consume(TokenType.String)));
				} else if (match(TokenType.DollarSign)) {
					final Token dollarSign = consume(TokenType.DollarSign);
					if (match(TokenType.OpenCurly)) {
						final Token openCurly = consume(TokenType.OpenCurly);
						final ExpressionNode expression = expression();
						final Token closeCurly = consume(TokenType.CloseCurly);
						elements.add(new ExpressionStringElement(dollarSign, openCurly, expression, closeCurly));
					} else {
						elements.add(new IdentifierStringElement(consume(TokenType.Identifier)));
					}
				} else {
					break;
				}
			}

			final Token rightQuote = consume(TokenType.DoubleQuote);
			return new StringExpressionNode(leftQuote, elements, rightQuote);
		}

		if (match(TokenType.OpenParent)) {
			
			final Token openParent = tokens.consume();
			final ExpressionNode expression = expression();

			if (match(TokenType.CloseParent)) {
				final Token closeParent = consume(TokenType.CloseParent);
				return new ParenthesizedExpressionNode(openParent, expression, closeParent);
			}

			final MutableSeparatedList<ExpressionNode, Token> values = MutableSeparatedList.create(ExpressionNode.class);
			values.addElement(expression);
			values.addSeparator(consume(TokenType.Comma));

			while (tokens.isNotDone() && !match(TokenType.CloseParent)) {

				final Token start = tokens.current();

				values.addElement(expression());

				if (!match(TokenType.CloseParent)) {
					values.addSeparator(consume(TokenType.Comma));
				}

				if (start == tokens.current()) tokens.advance();
			}

			final Token closeParent = consume(TokenType.CloseParent);

			return new TupleExpressionNode(openParent, values, closeParent);
		}

		if (match(TokenType.ReturnKeyword)) {
			final Token returnKeyword = tokens.consume();
			final Optional<ExpressionNode> value = returnValue(returnKeyword);
			return new ReturnExpressionNode(returnKeyword, value);
		}

		if (match(TokenType.NoneKeyword)) {
			final Token noneKeyword = tokens.consume();
			return new NoneExpressionNode(noneKeyword);
		}
		
		if (match(TokenType.SelfKeyword)) {
			final Token selfKeyword = tokens.consume();
			return new SelfExpressionNode(selfKeyword);
		}
		
		if (match(TokenType.FunKeyword)) {
			final Token funKeyword = tokens.consume();
			final ParametersNode parameters = parameters();
			final TypeAnnotationNode returnType = typeAnnotation();
			final FunctionBodyNode body = functionBody();
			return new FunctionExpressionNode(funKeyword, parameters, returnType, body);
		}

		return new IdentifierExpressionNode(consume(TokenType.Identifier));
	}

	private Optional<ExpressionNode> returnValue(Token returnToken) {
		if (onSameLine(returnToken, tokens.current()) && TokenTypes.isStartOfExpression(tokens.current().type))
			return Optional.of(expression());
		return Optional.empty();
	}

	private FunctionBodyNode functionBody() {

		if (match(TokenType.OpenCurly)) {

			final Token openCurly = tokens.consume();
			final List<StatementNode> statements = new ArrayList<>();

			while (tokens.isNotDone() && !match(TokenType.CloseCurly)) {
				final Token start = tokens.current();
				statements.add(declaration(/*declarationsOnly: */ false));
				if (start == tokens.current()) tokens.advance();
			}

			final Token closeCurly = consume(TokenType.CloseCurly);
			return new FunctionBodyNode.Block(openCurly, statements, closeCurly);
		}

		final Token arrow = consume(TokenType.Arrow);
		return new FunctionBodyNode.Expression(arrow, expression());
	}

	private ParametersNode parameters() {

		final Token openParent = consume(TokenType.OpenParent);
		
		final MutableSeparatedList<ParameterNode, Token> parameters = MutableSeparatedList.create(ParameterNode.class);

		while (tokens.isNotDone() && !match(TokenType.CloseParent)) {

			final Token start = tokens.current();

			final Token identifier = consume(TokenType.Identifier);
			final TypeAnnotationNode type = typeAnnotation();
			final Optional<ValueNode> value = optionalValue();
			parameters.addElement(new ParameterNode(identifier, type, value));

			if (!match(TokenType.CloseParent)) {
				parameters.addSeparator(consume(TokenType.Comma));
			}

			if (start == tokens.current()) tokens.advance();
		}

		final Token closeParent = consume(TokenType.CloseParent);
		
		return new ParametersNode(openParent, parameters, closeParent);
	}

	private Optional<ValueNode> optionalValue() {
		return match(TokenType.Equal) ? Optional.of(value()) : Optional.empty();
	}

	private ValueNode value() {
		final Token equal = consume(TokenType.Equal);
		final ExpressionNode value = expression();
		return new ValueNode(equal, value);
	}

	private TypeAnnotationNode typeAnnotation() {
		final Token colon = consume(TokenType.Colon);
		final TypeNode type = type();
		return new TypeAnnotationNode(colon, type);
	}

	private TypeNode type() {
		return unionType();
	}

	private TypeNode unionType() {

		TypeNode left = primaryType();

		while (tokens.isNotDone() && match(TokenType.Pipe)) {
			final Token pipe = tokens.consume();
			left = new TypeNode.Union(left, pipe, primaryType());
		}

		return left;
	}

	private TypeNode primaryType() {

		if (match(TokenType.OpenParent)) {

			final Token openParent = tokens.consume();
			final SeparatedList<TypeNode, Token> parameters = types(TokenType.CloseParent);
			final Token closeParent = consume(TokenType.CloseParent);

			if (match(TokenType.Arrow) || parameters.fullSize() == 0) {
				final Token arrow = consume(TokenType.Arrow);
				final TypeNode returnType = type();
				return new TypeNode.Function(openParent, parameters, closeParent, arrow, returnType);
			}

			if (parameters.fullSize() == 1) {
				return new TypeNode.Parenthesized(openParent, parameters.getElement(0), closeParent);
			}

			return new TypeNode.Tuple(openParent, parameters, closeParent);
		}

		return new TypeNode.Basic(consume(TokenType.Identifier));
	}

	private SeparatedList<ArgumentNode, Token> arguments() {

		final MutableSeparatedList<ArgumentNode, Token> arguments = MutableSeparatedList.create(ArgumentNode.class);

		while (tokens.isNotDone() && !match(TokenType.CloseParent)) {
			final Token start = tokens.current();

			arguments.addElement(argument());
			if (!match(TokenType.CloseParent)) {
				arguments.addSeparator(consume(TokenType.Comma));
			}

			if (start == tokens.current()) tokens.advance();
		}

		return arguments;
	}

	private ArgumentNode argument() {
		return new ArgumentNode(label(), expression());
	}

	private Optional<LabelNode> label() {
		if (matchAll(TokenType.Identifier, TokenType.Colon)) {
			final Token identifier = tokens.consume();
			final Token colon = tokens.consume();
			return Optional.of(new LabelNode(identifier, colon));
		}
		return Optional.empty();
	}

	private SeparatedList<TypeNode, Token> types(TokenType endToken) {

		final MutableSeparatedList<TypeNode, Token> types = MutableSeparatedList.create(TypeNode.class);

		while (tokens.isNotDone() && !match(endToken)) {

			final Token start = tokens.current();

			types.addElement(type());
			if (!match(endToken)) {
				types.addSeparator(consume(TokenType.Comma));
			}

			if (start == tokens.current()) tokens.advance();
		}

		return types;
	}

	private DeclarationNode declaration() {

		final Optional<LabelNode> label = label();
		
		if (label.isEmpty()) {

			final Token openParent = consume(TokenType.OpenParent);

			final MutableSeparatedList<DeclarationNode, Token> declarations = MutableSeparatedList.create(DeclarationNode.class);

			while (tokens.isNotDone() && !match(TokenType.CloseParent)) {
				final Token start = tokens.current();

				declarations.addElement(declaration());
				if (!match(TokenType.CloseParent)) {
					declarations.addSeparator(consume(TokenType.Comma));
				}

				if (start == tokens.current()) tokens.advance();
			}

			final Token closeParent = consume(TokenType.CloseParent);

			return new DeclarationNode.Distructure(label, openParent, declarations, closeParent);
		}
		
		return new DeclarationNode.Simple(label.get(), type());
	}

	private boolean reportErrors = true;

	private Token consume(TokenType target) {

		if (match(target)) {
			reportErrors = true;
			return tokens.consume();
		}

		if (reportErrors) {
			final Token current = tokens.current();
			reporter.reportUnexpectedToken(current.span(), current.type);
			reportErrors = false;
		}

		return new Token("", target, null, TextSpan.EMPTY);
	}

	private boolean match(TokenType type) {
		return tokens.current().type == type;
	}

	private boolean matchAll(TokenType... types) {
		for (int i = 0; i < types.length; i++)
			if (tokens.peek(i).type != types[i])
				return false;
		return true;
	}

	private boolean matchAny(TokenType... types) {
		for (final TokenType type : types)
			if (tokens.current().type == type)
				return true;
		return false;
	}

	@Override
	public void close() throws IOException {
		tokens.close();
	}

	private static boolean onSameLine(Spannable left, Spannable right) {
		return left.span().end.line == right.span().start.line;
	}
}

