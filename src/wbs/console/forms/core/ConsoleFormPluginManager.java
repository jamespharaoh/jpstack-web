package wbs.console.forms.core;

import com.google.common.base.Optional;

import wbs.console.forms.types.ConsoleFormNativeMapping;
import wbs.console.forms.types.FormFieldUpdateHook;

public
interface ConsoleFormPluginManager {

	Optional <ConsoleFormNativeMapping <?, ?, ?>> getNativeMapping (
			ConsoleFormBuilderContext context,
			Class <?> containerClass,
			String fieldName,
			Class <?> genericClass,
			Class <?> nativeClass);

	ConsoleFormNativeMapping <?, ?, ?> getNativeMappingRequired (
			ConsoleFormBuilderContext context,
			Class <?> containerClass,
			String fieldName,
			Class <?> genericClass,
			Class <?> nativeClass);

	FormFieldUpdateHook <?, ?, ?> getUpdateHook (
			ConsoleFormBuilderContext context,
			Class <?> containerClass,
			String fieldName);

}
