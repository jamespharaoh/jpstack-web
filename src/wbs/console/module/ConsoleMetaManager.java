package wbs.console.module;

import java.util.List;

import wbs.console.context.ResolvedConsoleContextExtensionPoint;

public
interface ConsoleMetaManager {

	List<ResolvedConsoleContextExtensionPoint> resolveExtensionPoint (
			String extensionPointName);

	List<ResolvedConsoleContextLink> resolveContextLink (
			String contextLinkName);

}
