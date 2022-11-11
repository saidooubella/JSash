package types;

import java.util.List;

public final class FunctionType extends SimpleType {
	
	public final List<Type> parameters;
	public final Type returnType;

	public FunctionType(List<Type> parameters, Type returnType) {
		this.parameters = parameters;
		this.returnType = returnType;
	}

	@Override
	protected boolean assignable(Type type) {
		
		if (!(type instanceof FunctionType)) return false;
		
		final FunctionType that = (FunctionType) type;
		
		if (this.parameters.size() != that.parameters.size())
			return false;
		
		for (int i = 0; i < parameters.size(); i++) {
			final Type p1 = this.parameters.get(i);
			final Type p2 = that.parameters.get(i);
			if (!p1.assignableTo(p2)) return false;
		}
		
		return this.returnType.assignableTo(that.returnType);
	}

	@Override
	public String name() {
		return parameters + " => " + returnType;
	}
}
