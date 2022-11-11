package utils;

import input.Input;

import java.util.function.Function;

public final class Inputs {
	
	private Inputs() { throw new UnsupportedOperationException(); }

	public static final Function<Integer, Character> IntToChar = codePoint -> (char) codePoint.intValue();
	
	public static boolean isNotNewLine(Input<Character> source) {
		return source.current() != '\n' && (source.current() != '\r' || source.peek(1) != '\n');
	}
}
