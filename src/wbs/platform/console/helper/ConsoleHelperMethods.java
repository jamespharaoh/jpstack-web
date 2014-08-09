package wbs.platform.console.helper;

import wbs.framework.record.Record;

public
interface ConsoleHelperMethods<ObjectType extends Record<ObjectType>> {

	String idKey ();

	String getPathId (
			Record<?> object);

	String getPathId (
			Integer objectId);

	String getDefaultContextPath (
			Record<?> object);

	String getDefaultLocalPath (
			Record<?> object);

	boolean canView (
			Record<?> object);

}
