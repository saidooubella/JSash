package rewriter.symbols;

import types.PrimitiveType;
import types.Type;

import java.util.*;

public final class SymbolsTable {

	private Scope current;

	public SymbolsTable() {
		this.current = new Scope(null);
		this.putExtension(BuiltIn.StringLen);
		this.putSymbol(BuiltIn.Println);
		this.putSymbol(BuiltIn.Input);
		this.putSymbol(BuiltIn.Print);
		this.putType(PrimitiveType.Boolean);
		this.putType(PrimitiveType.Integer);
		this.putType(PrimitiveType.Nothing);
		this.putType(PrimitiveType.String);
		this.putType(PrimitiveType.Double);
		this.putType(PrimitiveType.Float);
		this.putType(PrimitiveType.Long);
		this.putType(PrimitiveType.None);
		this.putType(PrimitiveType.Unit);
		this.putType(PrimitiveType.Any);
		this.pushScope();
	}

	public void pushScope() {
		current = new Scope(current);
	}

	public void popScope() {
		current = current.requireParent();
	}
	
	public boolean putExtension(ExtensionSymbol symbol) {
		return current.putExtension(symbol.receiver.type(), symbol);
	}

	public boolean putSymbol(Symbol symbol) {
		return current.putSymbol(symbol);
	}

	public Optional<ExtensionSymbol> getExtension(Type receiver, String name) {
		return current.getExtension(receiver, name);
	}

	public Optional<Symbol> getSymbol(String name) {
		return current.getSymbol(name);
	}

	public boolean putType(Type type) {
		return current.putType(type);
	}

	public Optional<Type> getType(String name) {
		return current.getType(name);
	}
	
	private static final class Scope implements Iterable<Scope> {

		private final Map<String, Symbol> symbols;
		private final Map<String, Type> types;
		private final Map<Type, Map<String, ExtensionSymbol>> extensions;
		private final Scope parent;

		public Scope(Scope parent) {
			this.extensions = new HashMap<>();
			this.symbols = new HashMap<>();
			this.types = new HashMap<>();
			this.parent = parent;
		}
		
		public boolean putExtension(Type receiver, ExtensionSymbol symbol) {
			
			Map<String, ExtensionSymbol> exts = extensions.get(receiver);
			if (exts == null) {
				exts = new HashMap<>();
				extensions.put(receiver, exts);
			}
		
			if (exts.containsKey(symbol.name()))
				return false;

			exts.put(symbol.name(), symbol);
			return true;
		}
		
		public Optional<ExtensionSymbol> getExtension(Type receiver, String name) {
			for (final Scope scope : this) {
				final Map<String, ExtensionSymbol> exts = scope.extensions.get(receiver);
				if (exts != null && exts.containsKey(name)) return Optional.of(exts.get(name));
			}
			return receiver == PrimitiveType.Any ? Optional.empty() : getExtension(PrimitiveType.Any, name);
		}

		public boolean putSymbol(Symbol symbol) {

			if (symbols.containsKey(symbol.name()))
				return false;

			symbols.put(symbol.name(), symbol);
			return true;
		}

		public Optional<Symbol> getSymbol(String name) {
			for (final Scope scope : this) {
				final Symbol symbol = scope.symbols.get(name);
				if (symbol != null) return Optional.of(symbol);
			}
			return Optional.empty();
		}

		public boolean putType(Type type) {

			if (types.containsKey(type.name()))
				return false;

			types.put(type.name(), type);
			return true;
		}

		public Optional<Type> getType(String name) {
			for (final Scope scope : this) {
				final Type type = scope.types.get(name);
				if (type != null) return Optional.of(type);
			}
			return Optional.empty();
		}

		public Scope requireParent() {
			return Objects.requireNonNull(parent);
		}

		@Override
		public Iterator<Scope> iterator() {
			return new ScopeIterator(this);
		}

		@Override
		public Spliterator<Scope> spliterator() {
			throw new UnsupportedOperationException();
		}

		private static final class ScopeIterator implements Iterator<Scope> {

			private Scope current;

			public ScopeIterator(Scope scope) {
				this.current = scope;
			}

			@Override
			public boolean hasNext() {
				return current != null;
			}

			@Override
			public Scope next() {
				final Scope temp = current;
				current = current.parent;
				return temp;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		}
	}
}
