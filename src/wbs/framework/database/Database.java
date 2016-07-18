package wbs.framework.database;


public
interface Database {

	Transaction beginReadWrite (
			String summary,
			Object owner);

	Transaction beginReadOnly (
			String summary,
			Object owner);

	Transaction beginReadOnlyJoin (
			String summary,
			Object owner);

	Transaction beginTransaction (
			String summary,
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
