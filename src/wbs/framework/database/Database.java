package wbs.framework.database;

public
interface Database {

	Transaction beginReadWrite (
			Object owner);

	Transaction beginReadOnly (
			Object owner);

	Transaction beginReadOnlyJoin (
			Object owner);

	Transaction beginTransaction (
			Object owner,
			boolean readWrite,
			boolean canJoin,
			boolean canCreateNew,
			boolean makeCurrent);

	Transaction currentTransaction ();

	void flush ();
	void clear ();

	void flushAndClear ();

}
