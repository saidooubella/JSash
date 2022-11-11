package input;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Input<T> implements Closeable {

	public final void advanceBy(int times) {

		if (times <= 0) {
			throw new IllegalArgumentException("times <= 0");
		}

		for (int i = 0; i < times; i++) {
			advance();
		}
	}

	public final T consume() {
		final T temp = current();
		advance();
		return temp;
	}

	public final boolean isNotDone() {
		return !isDone();
	}

	public final <U> Input<U> map(Function<T, U> mapper) {
		return new Mapper<>(this, mapper);
	}

	public final Input<T> doOnPreNext(Consumer<T> callback) {
		return new Watcher<>(this, callback);
	}

	public abstract void advance();

	public abstract boolean isDone();

	public abstract T current();

	public abstract T peek(int offset);

	public static <T> Input<T> of(Provider<T> provider) {
		return new Impl<>(provider);
	}

	private static final class Impl<T> extends Input<T> {

		private final Provider<T> provider;
		private final List<T> cache;

		private boolean isDone;
		private T current;

		public Impl(Provider<T> provider) {
			this.cache = new ArrayList<>();
			this.provider = provider;
			this.current = nextElement();
		}

		@Override
		public void advance() {

			if (isDone) return;
			
			current = cache.isEmpty()
				? nextElement()
				: cache.remove(0);
		}

		private T nextElement() {
			final T next = provider.nextElement();
			if (provider.isEndReached(next))
				isDone = true;
			return next;
		}

		@Override
		public boolean isDone() {
			return isDone;
		}

		@Override
		public T current() {
			return current;
		}

		@Override
		public T peek(int offset) {

			if (offset < 0) 
				throw new IllegalArgumentException("offset < 0");

			if (offset == 0 || isDone)
				return current;

			if (offset <= cache.size())
				return cache.get(offset - 1);

			final int max = offset - cache.size();

			for (int i = 0; i < max; i++) {
				final T next = provider.nextElement();
				if (provider.isEndReached(next))
					return next;
				cache.add(next);
			}

			return cache.get(cache.size() - 1);
		}

		@Override
		public void close() throws IOException {
			provider.close();
		}
	}

	private static final class Watcher<T> extends Input<T> {

		private final Input<T> source;
		private final Consumer<T> onPreNext;

		public Watcher(Input<T> source, Consumer<T> onPreNext) {
			this.source = source;
			this.onPreNext = onPreNext;
		}

		@Override
		public void advance() {
			if (source.isDone()) return;
			onPreNext.accept(source.current());
			source.advance();
		}

		@Override
		public boolean isDone() {
			return source.isDone();
		}

		@Override
		public T current() {
			return source.current();
		}

		@Override
		public T peek(int offset) {
			return source.peek(offset);
		}

		@Override
		public void close() throws IOException {
			source.close();
		}
	}

	private static final class Mapper<T, U> extends Input<U> {

		private final Function<T, U> mapper;
		private final Input<T> source;

		public Mapper(Input<T> source, Function<T, U> mapper) {
			this.source = source;
			this.mapper = mapper;
		}

		@Override
		public void advance() {
			source.advance();
		}

		@Override
		public boolean isDone() {
			return source.isDone();
		}

		@Override
		public U current() {
			return mapper.apply(source.current());
		}

		@Override
		public U peek(int offset) {
			return mapper.apply(source.peek(offset));
		}

		@Override
		public void close() throws IOException {
			source.close();
		}
	}

	public interface Provider<T> extends Closeable {
		T nextElement();
		boolean isEndReached(T item);
	}
}
