package evaluator.values;

import rewriter.symbols.ParameterSymbol;
import rewriter.symbols.RecordSymbol;
import rewriter.symbols.Symbol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RecordValue {
	
	private final Map<ParameterSymbol, Object> fields = new HashMap<>();
	
	private final RecordSymbol symbol;

	public RecordValue(RecordSymbol symbol, List<Object> arguments) {
		this.symbol = symbol;
		for (int i = 0; i < symbol.parameters.size(); i++) {
			fields.put(symbol.parameters.get(i), arguments.get(i));
		}
	}

	public Object set(ParameterSymbol field, Object value) {
		fields.put(field, value);
		return value;
	}

	public Object get(ParameterSymbol field) {
		return fields.get(field);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(symbol.name()).append('(');
		for (int i = 0; i < symbol.parameters.size(); i++) {
			if (i > 0) builder.append(", ");
			final Symbol param = symbol.parameters.get(i);
			builder.append(param.name()).append(": ");
			builder.append(fields.get(param));
		}
		return builder.append(')').toString();
	}
}
