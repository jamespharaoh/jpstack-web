package wbs.console.helper.provider;

import wbs.console.context.ConsoleContextStuff;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

public
interface ConsoleHelperProvider <
	RecordType extends Record <RecordType>
> {

	String objectName ();

	Class <RecordType> objectClass ();

	String idKey ();

	String getPathId (
			Transaction parentTransaction,
			Long objectId);

	String getDefaultContextPath (
			Transaction parentTransaction,
			RecordType object);

	String localPath (
			Transaction parentTransaction,
			RecordType object);

	boolean canView (
			Transaction parentTransaction,
			RecordType object);

	RecordType lookupObject (
			Transaction parentTransaction,
			ConsoleContextStuff contextStuff);

	void postProcess (
			Transaction parentTransaction,
			ConsoleContextStuff contextStuff);

	boolean canCreateIn (
			Transaction parentTransaction,
			Record <?> parent);

}
