package types;

public final class UnionType extends Type {

	public final Type left;
	public final Type right;

	public UnionType(Type left, Type right) {
		this.left = left;
		this.right = right;
	}
	
	@Override
	public boolean assignableTo(Type type) {
		return left.assignableTo(type) && right.assignableTo(type);
	}

	@Override
	public String name() {
		return left + " | " + right;
	}
}
