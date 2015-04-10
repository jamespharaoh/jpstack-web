package wbs.framework.database;

import org.joda.time.Instant;

public
interface Transaction {

	class IdGenerator {

		private static
		long nextId = 0;

		public synchronized static
		long nextId () {
			return nextId ++;
		}

	}

	long getId ();

	void commit ();

	void close ();

	Instant now ();

	void flush ();

	void refresh (
			Object object);

	boolean contains (
			Object object);

	void setMeta (
			String key,
			Object value);

	Object getMeta (
			String key);

	void fetch (
			Object... objects);

}
