package types;

public final class RecordType extends SimpleType {
	
	private final String name;

	public RecordType(String name) {
		this.name = name;
	}

	@Override
	protected boolean assignable(Type type) {
		return this == type;
	}

	@Override
	public String name() {
		return name;
	}
}
