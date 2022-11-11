package types;

import java.util.List;

public final class TupleType extends SimpleType {
	
	public final List<Type> types;
	
	public TupleType(List<Type> types) {
		this.types = types;
	}

	@Override
	protected boolean assignable(Type type) {

		if (!(type instanceof TupleType)) return false;

		final TupleType that = (TupleType) type;

		if (this.types.size() != that.types.size())
			return false;

		for (int i = 0; i < types.size(); i++) {
			final Type p1 = this.types.get(i);
			final Type p2 = that.types.get(i);
			if (!p1.assignableTo(p2)) return false;
		}

		return true;
	}

	@Override
	public String name() {
		return types.toString();
	}
}
