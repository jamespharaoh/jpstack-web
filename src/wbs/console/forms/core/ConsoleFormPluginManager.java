package wbs.console.forms.core;

import com.google.common.base.Optional;

import wbs.console.forms.types.ConsoleFormNativeMapping;
import wbs.console.forms.types.FormFieldUpdateHook;

import wbs.framework.logging.TaskLogger;

public
interface ConsoleFormPluginManager {

	Optional <ConsoleFormNativeMapping <?, ?, ?>> getNativeMapping (
			TaskLogger parentTaskLogger,
			ConsoleFormBuilderContext context,
			Class <?> containerClass,
			String fieldName,
			Class <?> genericClass,
			Class <?> nativeClass);

	ConsoleFormNativeMapping <?, ?, ?> getNativeMappingRequired (
			TaskLogger parentTaskLogger,
			ConsoleFormBuilderContext context,
			Class <?> containerClass,
			String fieldName,
			Class <?> genericClass,
			Class <?> nativeClass);

	FormFieldUpdateHook <?, ?, ?> getUpdateHook (
			TaskLogger parentTaskLogger,
			ConsoleFormBuilderContext context,
			Class <?> containerClass,
			String fieldName);

}
