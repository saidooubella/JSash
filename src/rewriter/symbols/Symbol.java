package rewriter.symbols;

import types.Typed;

public abstract class Symbol implements Typed {

	public abstract String name();

	@Override
	public abstract boolean equals(Object obj);
	
	@Override
	public abstract int hashCode();
}
