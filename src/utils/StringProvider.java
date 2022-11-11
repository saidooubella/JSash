package utils;

import input.Input;

public final class StringProvider implements Input.Provider<Integer> {

	private final String input;

	private int index;

	public StringProvider(String input) {
		this.input = input;
	}

	@Override
	public Integer nextElement() {
		if (index < input.length())
			return input.codePointAt(index++);
		return -1;
	}

	@Override
	public boolean isEndReached(Integer item) {
		return item == -1;
	}

	@Override
	public void close() {
		
	}
}
