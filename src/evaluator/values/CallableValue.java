package evaluator.values;

import java.util.List;

public interface CallableValue {
	Object invoke(List<Object> arguments);
}
