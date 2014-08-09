package wbs.platform.console.request;

import java.util.Iterator;
import java.util.NoSuchElementException;

public
class EmptyFormData
	implements FormData {

	private
	EmptyFormData () {
	}

	@Override
	public
	boolean contains (
			String name) {

		return false;

	}

	@Override
	public
	String get (
			String name) {

		return null;

	}

	@Override
	public
	Iterator<Entry> iterator () {

		return new Iterator<Entry> () {

			@Override
			public
			boolean hasNext () {
				return false;
			}

			@Override
			public
			Entry next () {
				throw new NoSuchElementException ();
			}

			@Override
			public
			void remove () {
				throw new UnsupportedOperationException ();
			}

		};

	}

	public final static
	EmptyFormData instance =
		new EmptyFormData ();
}
