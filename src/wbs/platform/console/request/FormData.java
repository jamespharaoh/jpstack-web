package wbs.platform.console.request;

import java.util.Iterator;

public
interface FormData
		extends Iterable<FormData.Entry> {

	public interface
	Entry {

		public
		String getName ();

		public
		String getValue ();

	}

	public
	class SimpleEntry
		implements Entry {

		private final
		String name, value;

		public
		SimpleEntry (
				String newName,
				String newValue) {

			name = newName;
			value = newValue;

		}

		@Override
		public
		String getName () {

			return name;

		}

		@Override
		public
		String getValue () {

			return value;

		}

	}

	public
	boolean contains (
			String name);

	public
	String get (
			String name);

	@Override
	public
	Iterator<Entry> iterator ();

}
