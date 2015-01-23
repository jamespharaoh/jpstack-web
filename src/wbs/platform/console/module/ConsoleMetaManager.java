package wbs.platform.console.module;

import java.util.List;

import wbs.platform.console.context.ResolvedConsoleContextExtensionPoint;

public
interface ConsoleMetaManager {

	List<ResolvedConsoleContextExtensionPoint> resolveExtensionPoint (
			String extensionPointName);

	List<ResolvedConsoleContextLink> resolveContextLink (
			String contextLinkName);

}
