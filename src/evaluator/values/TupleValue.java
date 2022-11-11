package evaluator.values;

import java.util.List;

public final class TupleValue {
	
	private final List<Object> values;

	public TupleValue(List<Object> values) {
		this.values = values;
	}
	
	public Object get(int index) {
		return values.get(index);
	}

	@Override
	public String toString() {
		return values.toString();
	}
}
