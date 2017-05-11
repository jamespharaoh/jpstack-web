package wbs.platform.event.console;

import static wbs.utils.etc.Misc.doesNotImplement;
import static wbs.utils.string.StringUtils.stringEqualSafe;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.core.FormFieldBuilderContext;
import wbs.console.forms.types.FormFieldPluginProvider;
import wbs.console.helper.core.ConsoleHelper;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.entity.record.Record;

@SingletonComponent ("eventFormFieldPluginProvider")
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class EventFormFieldPluginProvider
	implements FormFieldPluginProvider {

	// prototype dependencies

	@PrototypeDependency
	Provider <NameFormFieldUpdateHook> nameFormFieldUpdateHookProvider;

	@PrototypeDependency
	Provider <SimpleFormFieldUpdateHook> simpleFormFieldUpdateHookProvider;

	// implementation

	@Override
	public
	Optional getUpdateHook (
			@NonNull FormFieldBuilderContext context,
			@NonNull Class<?> containerClass,
			@NonNull String fieldName) {

		// only operate on records

		if (
			doesNotImplement (
				containerClass,
				Record.class)
		) {
			return Optional.absent ();
		}

		// get console helper

		ConsoleHelper consoleHelper =
			context.consoleHelper ();

		// check for name field

		if (

			consoleHelper.nameExists ()

			&& stringEqualSafe (
				consoleHelper.nameFieldName (),
				fieldName)

		) {

			// name field has special update hook

			return Optional.of (
				nameFormFieldUpdateHookProvider.get ());

		} else {

			// other fields use simple update hook

			return Optional.of (
				simpleFormFieldUpdateHookProvider.get ()

				.fieldName (
					fieldName)

			);

		}

	}

}
