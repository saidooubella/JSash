package utils;

import java.util.ArrayList;
import java.util.List;

public final class MutableSeparatedList<E, S> extends SeparatedList<E, S> {

	private final List<Object> elements;
	private final Class<E> elementClass;

	private MutableSeparatedList(Class<E> elementClass) {
		this.elements = new ArrayList<>();
		this.elementClass = elementClass;
	}

	public void addElement(E element) {
		if (!lastIsSeparator())
			throw new IllegalStateException("A separator must be added");
		elements.add(element);
	}

	public void addSeparator(S element) {
		if (lastIsSeparator())
			throw new IllegalStateException("An element must be added");
		elements.add(element);
	}

	@Override
	@SuppressWarnings("unchecked")
	public E getElement(int index) {
		return (E) elements.get(index * 2);
	}

	@Override
	@SuppressWarnings("unchecked")
	public S getSeparator(int index) {
		return (S) elements.get((index * 2) + 1);
	}

	@Override
	public int fullSize() {
		return elements.size();
	}

	private boolean lastIsSeparator() {
		return elements.isEmpty() || !elementClass.isAssignableFrom(elements.get(elements.size() - 1).getClass());
	}

	public static <E, S> MutableSeparatedList<E, S> create(Class<E> elementClass) {
		return new MutableSeparatedList<>(elementClass);
	}
}
