package wbs.console.lookup;

import wbs.console.context.ConsoleContextStuff;

import wbs.framework.database.Transaction;

public
interface ObjectLookup <ObjectType> {

	ObjectType lookupObject (
			Transaction parentTransaction,
			ConsoleContextStuff contextStuff);

}
