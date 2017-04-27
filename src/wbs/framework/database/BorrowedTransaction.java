package wbs.framework.database;

import lombok.NonNull;

import org.joda.time.Instant;

public
class BorrowedTransaction
	implements Transaction {

	private
	OwnedTransaction realTransaction;

	public
	BorrowedTransaction (
			@NonNull OwnedTransaction realTransaction) {

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
			Object... objects) {

		realTransaction.refresh (
			objects);

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
			Object... objects) {

		return realTransaction.contains (
			objects);

	}

}
