package utils;

public final class Todo extends RuntimeException {
	
	public Todo() {
		this("Not implemented");
	}
	
	public Todo(String message) {
		super("TODO: " + message);
	}
}
