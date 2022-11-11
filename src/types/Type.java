package types;

public abstract class Type {

	public abstract boolean assignableTo(Type type);
	
	public abstract String name();
	
	public final boolean isNotError() {
		return this != PrimitiveType.Error;
	}

	public final boolean isError() {
		return this == PrimitiveType.Error;
	}
	
	public final boolean castableTo(Type type) {
		return this.assignableTo(type) ||
			type.assignableTo(this);
	}

	@Override
	public final boolean equals(Object obj) {
		if (!(obj instanceof Type)) return false;
		return name().equals(((Type) obj).name());
	}

	@Override
	public final int hashCode() {
		return name().hashCode();
	}

	@Override
	public final String toString() {
		return name();
	}
}
