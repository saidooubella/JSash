package span;

public final class Position {

	public final int column;
	public final int index;
	public final int line;

	public Position(int index, int column, int line) {
		this.column = column;
		this.index = index;
		this.line = line;
	}

	public TextSpan plus(Position position) {
		return new TextSpan(this, position);
	}

	public boolean equals(Position that) {
		return this.column == that.column &&
			this.index == that.index &&
			this.line == that.line;
	}

	public static final class Builder {

		private int column = 1;
		private int index = 0;
		private int line = 1;

		public void advance(char current) {
			index += 1;
			if (current == '\n') {
				column = 1;
				line += 1;
			} else {
				column += 1;
			}
		}

		public Position build() {
			return new Position(index, column, line);
		}
	}
}
