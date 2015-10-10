package wbs.console.context;

import java.util.List;

public
interface ConsoleContextExtensionPoint {

	boolean root ();
	boolean nested ();

	String name ();

	// root

	List<String> parentContextNames ();

	List<String> contextTypeNames ();

	List<String> contextLinkNames ();

	// nested

	String parentExtensionPointName ();

}
