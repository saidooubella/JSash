package utils;

import input.Input;

import java.util.Iterator;
import java.util.Spliterator;

public final class InputProviders {

	public static <T> Iterable<T> toIterable(final Input.Provider<T> provider) {
		return new ProviderIterable<>(provider);
	}

	private static final class ProviderIterable<T> implements Iterable<T> {

		private final Input.Provider<T> provider;

		public ProviderIterable(Input.Provider<T> provider) {
			this.provider = provider;
		}
		
		@Override
		public Iterator<T> iterator() {
			return new ProviderIterator<>(provider);
		}

		@Override
		public Spliterator<T> spliterator() {
			throw new UnsupportedOperationException();
		}
		
		private static final class ProviderIterator<T> implements Iterator<T> {
			
			private final Input.Provider<T> provider;
			private T current;
			
			public ProviderIterator(Input.Provider<T> provider) {
				this.provider = provider;
			}
			
			@Override
			public boolean hasNext() {
				return !provider.isEndReached(current = provider.nextElement());
			}

			@Override
			public T next() {
				return current;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		}
	}
}
