package span;

public final class TextSpan implements Spannable {
	
	public static final TextSpan EMPTY;
	
	static {
		final Position empty = new Position(0, 1, 1);
		EMPTY = empty.plus(empty);
	}

	public final Position start;
	public final Position end;

	public TextSpan(Position start, Position end) {
		this.start = start;
		this.end = end;
	}

	public TextSpan plus(final TextSpan that) {
		return new TextSpan(this.start, that.end);
	}

	@Override
	public TextSpan span() {
		return this;
	}
}
