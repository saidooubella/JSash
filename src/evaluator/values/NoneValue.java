package evaluator.values;

public final class NoneValue {

	private static final NoneValue INSTANCE = new NoneValue();

	private NoneValue() {}

	public static NoneValue instance() {
		return INSTANCE;
	}

	@Override
	public String toString() {
		return "sash.None";
	}
}
