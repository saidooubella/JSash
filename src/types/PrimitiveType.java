package types;

public final class PrimitiveType extends SimpleType {

	public static final Type Error   = new PrimitiveType("???");

	public static final Type Boolean = new PrimitiveType("Boolean");
	public static final Type Integer = new PrimitiveType("Int");
	public static final Type Double  = new PrimitiveType("Double");
	public static final Type String  = new PrimitiveType("String");
	public static final Type Float   = new PrimitiveType("Float");
	public static final Type Long    = new PrimitiveType("Long");

	public static final Type Nothing = new PrimitiveType("Nothing");
	public static final Type None    = new PrimitiveType("None");
	public static final Type Unit    = new PrimitiveType("Unit");
	public static final Type Any     = new PrimitiveType("Any");

	private final String name;

	private PrimitiveType(String name) {
		this.name = name;
	}

	@Override
	protected boolean assignable(Type type) {
		return this == Nothing || this == Error ||
			type == Error || this == type;
	}

	@Override
	public String name() {
		return name;
	}
}
