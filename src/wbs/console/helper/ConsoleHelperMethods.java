package wbs.console.helper;

import com.google.common.base.Optional;

import wbs.framework.entity.record.Record;

public
interface ConsoleHelperMethods<ObjectType extends Record<ObjectType>> {

	String idKey ();

	String getPathId (
			Record<?> object);

	String getPathId (
			Long objectId);

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

	ConsoleHooks<ObjectType> consoleHooks ();

}
