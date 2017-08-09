package wbs.console.lookup;

import wbs.console.context.ConsoleContextStuff;

public
interface BooleanLookup {

	boolean lookup (
			ConsoleContextStuff contextStuff);

	String describe ();

}
