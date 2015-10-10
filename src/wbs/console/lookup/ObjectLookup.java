package wbs.console.lookup;

import wbs.console.context.ConsoleContextStuff;

public
interface ObjectLookup<Type> {

	Type lookupObject (
			ConsoleContextStuff contextStuff);

}
