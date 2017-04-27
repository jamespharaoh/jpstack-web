package wbs.framework.database;

public
interface OwnedTransaction
	extends
		AutoCloseable,
		Transaction {

	@Override
	void close ();

}
