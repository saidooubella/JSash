package evaluator.environment;

import evaluator.values.CallableValue;
import evaluator.values.ExtensionBuilder;
import evaluator.values.UnitValue;
import rewriter.symbols.BuiltIn;
import rewriter.symbols.Symbol;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Environment {

	private Scope scope;

	private Environment() {
		this(new Scope(null));
	}

	private Environment(Scope scope) {
		this.scope = scope;
	}

	public void pushScope() {
		scope = new Scope(scope);
	}

	public void popScope() {
		scope = scope.requireParent();
	}

	public void freeze() {
		if (!isGlobal()) {
			scope = scope.copy();
		}
	}

	public void changeSymbol(Symbol key, Object value) {
		scope.changeSymbol(key, value);
	}

	public void putSymbol(Symbol key, Object value) {
		scope.putSymbol(key, value);
	}

	public Object getSymbol(Symbol key) {
		return scope.getSymbol(key);
	}

	public Environment frozenCopy() {
		return isGlobal() ? this : new Environment(scope.copy());
	}

	public Environment copy() {
		return isGlobal() ? this : new Environment(scope);
	}
	
	private boolean isGlobal() {
		return scope.parent == null;
	}
	
	public static Environment create() {
		final Environment env = new Environment();
		env.putSymbol(BuiltIn.Println, (CallableValue) arguments -> {
			Console.println(arguments.get(0));
			return UnitValue.instance();
		});
		env.putSymbol(BuiltIn.Print, (CallableValue) arguments -> {
			Console.print(arguments.get(0));
			return UnitValue.instance();
		});
		env.putSymbol(BuiltIn.Input, (CallableValue) arguments -> Console.readLine());
		env.putSymbol(BuiltIn.StringLen, (ExtensionBuilder) receiverValue ->
				(CallableValue) arguments -> ((String) receiverValue).length());
		return env;
	}

	private static final class Scope {

		private final Scope parent;

		private final Map<Symbol, Box> symbols;

		public Scope(Scope parent) {
			this(parent, new HashMap<>());
		}

		private Scope(Scope parent, Map<Symbol, Box> symbols) {
			this.parent = parent;
			this.symbols = symbols;
		}

		public void changeSymbol(Symbol key, Object value) {
			get(key).value = value;
		}

		public void putSymbol(Symbol key, Object value) {
			symbols.put(key, new Box(value));
		}

		public Object getSymbol(Symbol key) {
			return get(key).value;
		}

		public Box get(Symbol key) {
			Scope current = this;
			while (current != null) {
				final Box value = current.symbols.get(key);
				if (value != null) return value;
				current = current.parent;
			}
			throw new IllegalStateException(key.name() + " not found");
		}

		public Scope requireParent() {
			return Objects.requireNonNull(parent);
		}

		public Scope copy() {
			return new Scope(parent, new HashMap<>(symbols));
		}

		private static final class Box {

			public Object value;

			public Box(Object value) {
				this.value = value;
			}
		}
	}
}
