package wbs.framework.database;

public
interface Database {

	Transaction beginReadWrite ();
	Transaction beginReadOnly ();
	Transaction beginReadOnlyJoin ();

	Transaction beginTransaction (
			boolean readWrite,
			boolean canJoin,
			boolean canCreateNew,
			boolean makeCurrent);

	Transaction currentTransaction ();

	void flush ();
	void clear ();

	void flushAndClear ();

}
