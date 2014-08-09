package wbs.platform.console.metamodule;

import java.util.List;

import wbs.platform.console.context.ConsoleContextExtensionPoint;
import wbs.platform.console.context.ConsoleContextLink;

public
interface ConsoleMetaModule {

	List<ConsoleContextExtensionPoint> extensionPoints ();

	List<ConsoleContextLink> contextLinks ();

}
