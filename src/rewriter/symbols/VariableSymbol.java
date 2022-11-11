package rewriter.symbols;

import types.Type;

import java.util.Objects;

public final class VariableSymbol extends Symbol {

	private final String name;
	private final Type type;
	
	public final boolean isReadOnly;

	public boolean isInitialized;

	public VariableSymbol(String name, Type type, boolean isReadOnly, boolean isInitialized) {
		this.isInitialized = isInitialized;
		this.isReadOnly = isReadOnly;
		this.name = name;
		this.type = type;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Type type() {
		return type;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name, type, isReadOnly);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof VariableSymbol)) return false;
		final VariableSymbol that = (VariableSymbol) object;
		return this.isReadOnly == that.isReadOnly &&
			this.name.equals(that.name) &&
			this.type.equals(that.type);
	}
}
