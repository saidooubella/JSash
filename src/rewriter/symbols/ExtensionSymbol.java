package rewriter.symbols;

import types.FunctionType;

import java.util.List;
import java.util.Objects;

public final class ExtensionSymbol extends Symbol {

	private final FunctionType type;
	private final String name;
	
	public final ParameterSymbol receiver;
	public final List<ParameterSymbol> parameters;

	public ExtensionSymbol(ParameterSymbol receiver, String name, List<ParameterSymbol> parameters, FunctionType type) {
		this.parameters = parameters;
		this.receiver = receiver;
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
		return Objects.hash(receiver, name, type, parameters);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ExtensionSymbol)) return false;
		final ExtensionSymbol that = (ExtensionSymbol) object;
		return this.receiver.equals(that.receiver) &&
			this.name.equals(that.name) &&
			this.type.equals(that.type) &&
			this.parameters.equals(that.parameters);
	}
}
