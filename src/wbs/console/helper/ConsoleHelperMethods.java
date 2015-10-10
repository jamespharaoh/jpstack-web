package wbs.console.helper;

import wbs.framework.record.Record;

import com.google.common.base.Optional;

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

	String getHtml (
			Record<?> object,
			Optional<Record<?>> assumedRoot,
			Boolean mini);

}
