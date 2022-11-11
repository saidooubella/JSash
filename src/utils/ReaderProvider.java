package utils;

import input.Input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public final class ReaderProvider implements Input.Provider<Integer> {

	private final BufferedReader reader;

	public ReaderProvider(Reader reader) {
		this.reader = buffered(reader);
	}

	@Override
	public Integer nextElement() {
		return safeRead(reader);
	}

	@Override
	public boolean isEndReached(Integer item) {
		return item == -1;
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

	private static BufferedReader buffered(Reader reader) {
		return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
	}

	private static int safeRead(Reader reader) {
		try {
			return reader.read();
		} catch (IOException e) {
			return -1;
		}
	}
}
