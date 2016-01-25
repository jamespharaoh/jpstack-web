package wbs.console.module;

import java.util.List;

import wbs.console.context.ConsoleContextExtensionPoint;
import wbs.console.context.ConsoleContextHint;
import wbs.console.context.ConsoleContextLink;

public
interface ConsoleMetaModule {

	List<ConsoleContextExtensionPoint> extensionPoints ();

	List<ConsoleContextLink> contextLinks ();

	List<ConsoleContextHint> contextHints ();

}
