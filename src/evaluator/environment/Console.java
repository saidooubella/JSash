package evaluator.environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class Console {
	
	private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	
	public static void print(Object object) {
		System.out.print(object);
	}
	
	public static void println(Object object) {
		System.out.println(object);
	}
	
	public static String readLine() {
		try {
			return reader.readLine();
		} catch (IOException e) {
			return "";
		}
	}
}
