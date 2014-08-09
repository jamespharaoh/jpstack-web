package wbs.framework.database;

public
interface TransactionCache<T> {

	T get ();

	void set (
			T value);

}
