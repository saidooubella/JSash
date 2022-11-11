package utils;

import java.util.Iterator;
import java.util.Spliterator;

public abstract class SeparatedList<E, S> implements Iterable<E> {

	public abstract E getElement(int index);
	public abstract S getSeparator(int index);
	public abstract int fullSize();

	public final int separatorsSize() {
		return fullSize() % 2 == 0 ? elementsSize() : elementsSize() - 1;
	}

	public final int elementsSize() {
		return (fullSize() + 1) / 2;
	}

	public final Iterator<E> elementsIterator() {
		return new ElementsIterator();
	}

	public final Iterator<S> separatorsIterator() {
		return new SeparatorsIterator();
	}

	@Override
	public final Iterator<E> iterator() {
		return elementsIterator();
	}

	@Override
	public Spliterator<E> spliterator() {
		throw new UnsupportedOperationException();
	}

	private final class ElementsIterator implements Iterator<E> {

		private int index = 0;

		@Override
		public boolean hasNext() {
			return index < elementsSize();
		}

		@Override
		public E next() {
			return getElement(index++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private final class SeparatorsIterator implements Iterator<S> {

		private int index = 0;

		@Override
		public boolean hasNext() {
			return index < separatorsSize();
		}

		@Override
		public S next() {
			return getSeparator(index++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
