package wbs.console.helper.core;

import wbs.console.forms.EntityFinder;
import wbs.console.lookup.ObjectLookup;

import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectHelperMethods;
import wbs.framework.object.ObjectModelMethods;

public
interface ConsoleHelper <RecordType extends Record <RecordType>>
	extends
		ConsoleHelperMethods <RecordType>,
		EntityFinder <RecordType>,
		ObjectHelperMethods <RecordType>,
		ObjectLookup <RecordType>,
		ObjectModelMethods <RecordType> {

}
