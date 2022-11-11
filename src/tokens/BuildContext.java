package tokens;

import errors.ErrorsReporter;
import input.Input;
import span.Position;

public final class BuildContext {
	
	private final Input<Character> source;
	private final Position.Builder positionBuilder;
	private final ErrorsReporter reporter;

	public BuildContext(Input<Character> source, Position.Builder positionBuilder, ErrorsReporter reporter) {
		this.positionBuilder = positionBuilder;
		this.reporter = reporter;
		this.source = source;
	}

	public Input<Character> source() {
		return source;
	}

	public ErrorsReporter reporter() {
		return reporter;
	}
	
	public Position position() {
		return positionBuilder.build();
	}
}
