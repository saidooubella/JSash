package tokens.cases;

public final class CheckResult {

	private static final CheckResult FAILURE = new CheckResult(false, null);
	private static final CheckResult SUCCESS = new CheckResult(true, null);

	public final boolean isSuccess;
	public final Object extra;

	private CheckResult(boolean isSuccess, Object extra) {
		this.isSuccess = isSuccess;
		this.extra = extra;
	}

	public static CheckResult from(boolean success) {
		return from(success, null);
	}

	public static CheckResult from(boolean success, Object extra) {
		return success ? extra != null ? new CheckResult(true, extra) : SUCCESS : FAILURE;
	}
}
