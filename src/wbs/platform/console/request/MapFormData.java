package wbs.platform.console.request;

import java.util.Iterator;
import java.util.Map;

public
class MapFormData
	implements FormData {

	private
	Map<String,String> map;

	public
	MapFormData (
			Map<String,String> newMap) {

		if (newMap == null)
			throw new NullPointerException ();

		map =
			newMap;

	}

	@Override
	public
	boolean contains (
			String name) {

		return map.containsKey (
			name);

	}

	@Override
	public
	String get (
			String name) {

		return map.get (
			name);

	}

	@Override
	public
	Iterator<Entry> iterator () {

		final
		Iterator<Map.Entry<String,String>> mapIterator =
			map.entrySet ().iterator ();

		return new Iterator<Entry> () {

			@Override
			public
			boolean hasNext () {

				return mapIterator.hasNext ();

			}

			@Override
			public
			Entry next () {

				final
				Map.Entry<String,String> mapEntry =
					mapIterator.next ();

				return new Entry () {

					@Override
					public
					String getName () {

						return mapEntry.getKey ();

					}

					@Override
					public
					String getValue () {

						return mapEntry.getValue ();

					}

				};

			}

			@Override
			public
			void remove () {

				throw new UnsupportedOperationException ();

			}

		};

	}

}
