package wbs.console.helper.provider;

import wbs.console.context.ConsoleContextStuff;
import wbs.console.priv.UserPrivChecker;

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
			UserPrivChecker privChecker,
			RecordType object);

	RecordType lookupObject (
			Transaction parentTransaction,
			ConsoleContextStuff contextStuff);

	void postProcess (
			Transaction parentTransaction,
			UserPrivChecker privChecker,
			ConsoleContextStuff contextStuff);

	boolean canCreateIn (
			Transaction parentTransaction,
			UserPrivChecker privChecker,
			Record <?> parent);

	boolean canSearch ();

}
