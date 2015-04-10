package wbs.framework.database;

import org.joda.time.Instant;

public
class TransactionView
	implements Transaction {

	private
	Transaction realTransaction;

	public
	TransactionView (
			Transaction realTransaction) {

		this.realTransaction =
			realTransaction;

	}

	@Override
	public
	long getId () {
		return realTransaction.getId ();
	}

	@Override
	public
	void commit () {
		throw new IllegalAccessError ();
	}

	@Override
	public
	void close () {
		throw new IllegalAccessError ();
	}

	@Override
	public
	Instant now () {
		return realTransaction.now ();
	}

	@Override
	public
	void flush () {
		realTransaction.flush ();
	}

	@Override
	public
	void refresh (
			Object object) {

		realTransaction.refresh (
			object);

	}

	@Override
	public
	void setMeta (
			String key,
			Object value) {

		realTransaction.setMeta (
			key,
			value);

	}

	@Override
	public
	Object getMeta (
			String key) {

		return realTransaction.getMeta (
			key);

	}

	@Override
	public
	void fetch (
			Object... objects) {

		realTransaction.fetch (
			objects);

	}

	@Override
	public
	boolean contains (
			Object object) {

		return realTransaction.contains (
			object);

	}

}
