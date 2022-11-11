package rewriter.symbols;

import types.FunctionType;

import java.util.List;
import java.util.Objects;

public final class RecordSymbol extends Symbol {
	
	private final FunctionType type;
	private final String name;
	
	public final List<ParameterSymbol> parameters;

	public RecordSymbol(String name, List<ParameterSymbol> parameters, FunctionType type) {
		this.parameters = parameters;
		this.name = name;
		this.type = type;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public FunctionType type() {
		return type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type, parameters);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof RecordSymbol)) return false;
		final RecordSymbol that = (RecordSymbol) object;
		return this.name.equals(that.name) &&
			this.type.equals(that.type) &&
			this.parameters.equals(that.parameters);
	}
}
