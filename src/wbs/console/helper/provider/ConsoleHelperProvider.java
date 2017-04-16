package wbs.console.helper.provider;

import wbs.console.context.ConsoleContextStuff;

import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

public
interface ConsoleHelperProvider <
	RecordType extends Record <RecordType>
> {

	String objectName ();

	Class <RecordType> objectClass ();

	String idKey ();

	String getPathId (
			TaskLogger parentTaskLogger,
			Long objectId);

	String getDefaultContextPath (
			TaskLogger parentTaskLogger,
			RecordType object);

	String localPath (
			TaskLogger parentTaskLogger,
			RecordType object);

	boolean canView (
			TaskLogger parentTaskLogger,
			RecordType object);

	RecordType lookupObject (
			ConsoleContextStuff contextStuff);

	void postProcess (
			TaskLogger parentTaskLogger,
			ConsoleContextStuff contextStuff);

}
