package evaluator.values;

import evaluator.Evaluator;
import evaluator.environment.Environment;
import evaluator.jumps.ReturnException;
import rewriter.statements.Statement;
import rewriter.symbols.ParameterSymbol;

import java.util.List;

public final class ExtensionValue implements ExtensionBuilder {
	
	private final ParameterSymbol receiver;
	private final List<ParameterSymbol> parameters;
	private final List<Statement> statements;
	private final Environment closure;

	public ExtensionValue(Environment closure, ParameterSymbol receiver, List<ParameterSymbol> parameters, List<Statement> statements) {
		this.parameters = parameters;
		this.statements = statements;
		this.receiver = receiver;
		this.closure = closure;
	}

	@Override
	public CallableValue build(final Object receiverValue) {
		return arguments -> {

			closure.pushScope();

			closure.putSymbol(receiver, receiverValue);

			for (int i = 0; i < parameters.size(); i++) {
				closure.putSymbol(parameters.get(i), arguments.get(i));
			}

			try {
				Evaluator.evaluateStatements(closure, statements);
				return UnitValue.instance();
			} catch (ReturnException exception) {
				return exception.value;
			} finally {
				closure.popScope();
			}
		};
	}

	@Override
	public String toString() {
		return "<function>";
	}
}
