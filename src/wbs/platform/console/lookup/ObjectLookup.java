package wbs.platform.console.lookup;

import wbs.platform.console.context.ConsoleContextStuff;

public
interface ObjectLookup<Type> {

	Type lookupObject (
			ConsoleContextStuff contextStuff);

}
