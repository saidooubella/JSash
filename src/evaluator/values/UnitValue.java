package evaluator.values;

public final class UnitValue {

	private static final UnitValue INSTANCE = new UnitValue();

	private UnitValue() {}

	public static UnitValue instance() {
		return INSTANCE;
	}

	@Override
	public String toString() {
		return "sash.Unit";
	}
}
