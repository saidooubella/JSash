package errors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;

public final class ErrorReports implements Iterable<ErrorMessage> {
	
	private final List<ErrorMessage> errors;

	private ErrorReports(List<ErrorMessage> errors) {
		this.errors = errors;
	}

	public boolean isEmpty() {
		return errors.isEmpty();
	}

	@Override
	public Iterator<ErrorMessage> iterator() {
		return errors.iterator();
	}

	@Override
	public Spliterator<ErrorMessage> spliterator() {
		return errors.spliterator();
	}

	public static final class Builder {

		private final List<ErrorMessage> errors;

		public Builder() {
			this.errors = new ArrayList<>();
		}
		
		public ErrorsReporter reporter(String location) {
			return new ErrorsReporter(errors, location);
		}
		
		public ErrorReports build() {
			return new ErrorReports(new ArrayList<>(errors));
		}
	}
}
