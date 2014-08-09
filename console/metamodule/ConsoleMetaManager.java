package wbs.platform.console.metamodule;

import java.util.List;

import wbs.platform.console.context.ResolvedConsoleContextExtensionPoint;

public
interface ConsoleMetaManager {

	List<ResolvedConsoleContextExtensionPoint> resolveExtensionPoint (
			String extensionPointName);

	List<ResolvedConsoleContextLink> resolveContextLink (
			String contextLinkName);

}
