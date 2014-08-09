package wbs.platform.console.request;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public
class HideFormData
	implements FormData {

	private
	FormData delegate;

	private
	Set<String> hideKeys;

	public
	HideFormData (
			FormData delegate,
			Set<String> hideKeys) {

		this.delegate =
			delegate;

		this.hideKeys =
			hideKeys;

	}

	@Override
	public
	boolean contains (
			String name) {

		if (hideKeys.contains (
				name))
			return false;

		return delegate.contains (
			name);

	}

	@Override
	public
	String get (
			String name) {

		if (hideKeys.contains (
				name))
			return null;

		return delegate.get (
			name);

	}

	@Override
	public
	Iterator<Entry> iterator () {

		final
		Iterator<Entry> delegateIterator =
			delegate.iterator ();

		return new Iterator<Entry> () {

			Entry next = null;

			@Override
			public
			boolean hasNext () {

				if (next != null)
					return true;

				for (;;) {

					next = delegateIterator.next ();

					if (next == null)
						return false;

					if (hideKeys.contains (next.getName ())) {
						next = null;
						continue;
					}

					return true;

				}

			}

			@Override
			public
			Entry next () {

				if (! hasNext ())
					throw new NoSuchElementException ();

				return next;
			}

			@Override
			public
			void remove () {
				throw new UnsupportedOperationException ();
			}

		};

	}

}
