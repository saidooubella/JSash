package types;

public abstract class SimpleType extends Type {
	
	@Override
	public final boolean assignableTo(Type type) {

		if (type instanceof UnionType) {
			final UnionType union = (UnionType) type;
			return assignableTo(union.left) || assignableTo(union.right);
		}

		if (this != PrimitiveType.None && type == PrimitiveType.Any) {
			return true;
		}

		return assignable(type);
	}

	protected abstract boolean assignable(Type type);
}
