package utils;

public abstract class Result<T> {

	private Result() {}

	public static <T> Success<T> value(T value) {
		return new Success<>(value);
	}

	public static <T> Failure<T> message(String message) {
		return new Failure<>(message);
	}

	public static final class Success<T> extends Result<T> {

		public final T value;

		private Success(T value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return "value: " + value;
		}
	}

	public static final class Failure<T> extends Result<T> {

		public final String message;

		private Failure(String message) {
			this.message = message;
		}

		@Override
		public String toString() {
			return "error: " + message;
		}
	}
}
