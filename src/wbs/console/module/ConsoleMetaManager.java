package wbs.console.module;

import java.util.List;

import wbs.console.context.ResolvedConsoleContextExtensionPoint;

import wbs.framework.logging.TaskLogger;

public
interface ConsoleMetaManager {

	List <ResolvedConsoleContextExtensionPoint> resolveExtensionPoint (
			TaskLogger parentTaskLogger,
			String extensionPointName);

	List <ResolvedConsoleContextLink> resolveContextLink (
			TaskLogger parentTaskLogger,
			String contextLinkName);

}
