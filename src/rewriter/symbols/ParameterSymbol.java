package rewriter.symbols;

import rewriter.expressions.Expression;
import types.Type;

import java.util.Objects;
import java.util.Optional;

public final class ParameterSymbol extends Symbol {

	private final String name;
	private final Type type;
	
	public final Optional<Expression> defaultValue;

	public ParameterSymbol(String name, Type type) {
		this(name, type, Optional.empty());
	}
	
	public ParameterSymbol(String name, Type type, Optional<Expression> defaultValue) {
		this.defaultValue = defaultValue;
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
		return Objects.hash(name, type, defaultValue);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ParameterSymbol)) return false;
		final ParameterSymbol that = (ParameterSymbol) object;
		return this.defaultValue.equals(that.defaultValue) &&
			this.name.equals(that.name) &&
			this.type.equals(that.type);
	}
}
