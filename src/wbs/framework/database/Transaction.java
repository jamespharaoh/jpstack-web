package wbs.framework.database;

import java.util.Date;

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

	Date timestamp ();

	Instant now ();

	void flush ();

	void refresh (
			Object object);

	void setMeta (
			String key,
			Object value);

	Object getMeta (
			String key);

	void fetch (
			Object... objects);

}
