package wbs.platform.event.console;

import static wbs.framework.utils.etc.Misc.doesNotImplement;
import static wbs.framework.utils.etc.Misc.equal;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.console.forms.AbstractFormFieldPluginProvider;
import wbs.console.forms.FormFieldBuilderContext;
import wbs.console.helper.ConsoleHelper;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.Record;

@SingletonComponent ("eventFormFieldPluginProvider")
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class EventFormFieldPluginProvider
	extends AbstractFormFieldPluginProvider {

	// prototype dependencies

	@Inject
	Provider<NameFormFieldUpdateHook> nameFormFieldUpdateHookProvider;

	@Inject
	Provider<SimpleFormFieldUpdateHook> simpleFormFieldUpdateHookProvider;

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

			&& equal (
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
