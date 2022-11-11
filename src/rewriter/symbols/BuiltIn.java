package rewriter.symbols;

import types.FunctionType;
import types.PrimitiveType;
import types.Type;
import utils.Types;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class BuiltIn {

	public static final FunctionSymbol Println = new FunctionSymbol(
		"println", params(new ParameterSymbol("value", Types.AnyNone)),
		new FunctionType(types(Types.AnyNone), PrimitiveType.Unit)
	);

	public static final FunctionSymbol Print = new FunctionSymbol(
		"print", params(new ParameterSymbol("value", Types.AnyNone)),
		new FunctionType(types(Types.AnyNone), PrimitiveType.Unit)
	);

	public static final FunctionSymbol Input = new FunctionSymbol(
		"input", params(), new FunctionType(types(), PrimitiveType.String)
	);

	public static final ExtensionSymbol StringLen = new ExtensionSymbol(
		new ParameterSymbol("self", PrimitiveType.String), "length", params(),
		new FunctionType(types(), PrimitiveType.Integer)
	);

	private BuiltIn() {}

	private static List<ParameterSymbol> params() {
		return Collections.emptyList();
	}

	private static List<ParameterSymbol> params(ParameterSymbol... params) {
		return Arrays.asList(params);
	}

	private static List<Type> types() {
		return Collections.emptyList();
	}

	private static List<Type> types(Type... types) {
		return Arrays.asList(types);
	}
}
