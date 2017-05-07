package wbs.framework.database;

import static wbs.utils.etc.Misc.todo;

import lombok.NonNull;

import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLoggerImplementation;

public
class BorrowedTransaction
	implements Transaction {

	private
	OwnedTransaction ownedTransaction;

	public
	BorrowedTransaction (
			@NonNull OwnedTransaction ownedTransaction) {

		this.ownedTransaction =
			ownedTransaction;

	}

	@Override
	public
	long transactionId () {
		return ownedTransaction.transactionId ();
	}

	@Override
	public
	void commit () {
		throw new IllegalAccessError ();
	}

	@Override
	public
	void flush () {
		ownedTransaction.flush ();
	}

	@Override
	public
	void refresh (
			Object... objects) {

		ownedTransaction.refresh (
			objects);

	}

	@Override
	public
	void setMeta (
			String key,
			Object value) {

		ownedTransaction.setMeta (
			key,
			value);

	}

	@Override
	public
	Object getMeta (
			String key) {

		return ownedTransaction.getMeta (
			key);

	}

	@Override
	public
	void fetch (
			Object... objects) {

		ownedTransaction.fetch (
			objects);

	}

	@Override
	public
	boolean contains (
			Object... objects) {

		return ownedTransaction.contains (
			objects);

	}

	@Override
	public
	TaskLoggerImplementation taskLoggerImplementation () {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	OwnedTransaction ownedTransaction () {

		return ownedTransaction;

	}

	@Override
	public
	NestedTransaction nestTransaction (
			@NonNull LogContext logContext,
			@NonNull CharSequence dynamicContext) {

		throw todo ();

	}

}
