package wbs.console.lookup;

import wbs.console.context.ConsoleContextStuff;

public
interface ObjectLookup <ObjectType> {

	ObjectType lookupObject (
			ConsoleContextStuff contextStuff);

}
