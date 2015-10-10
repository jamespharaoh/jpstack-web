package wbs.platform.event.console;

import static wbs.framework.utils.etc.Misc.doesNotImplement;
import static wbs.framework.utils.etc.Misc.equal;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.forms.AbstractFormFieldPluginProvider;
import wbs.console.forms.FormFieldBuilderContext;
import wbs.console.helper.ConsoleHelper;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.Record;

import com.google.common.base.Optional;

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
			FormFieldBuilderContext context,
			Class<?> containerClass,
			String fieldName) {

		if (
			doesNotImplement (
				containerClass,
				Record.class)
		) {
			return Optional.absent ();
		}

		ConsoleHelper consoleHelper =
			context.consoleHelper ();

		if (
			equal (
				consoleHelper.nameFieldName (),
				fieldName)
		) {

			return Optional.of (
				nameFormFieldUpdateHookProvider.get ());

		} else {

			return Optional.of (
				simpleFormFieldUpdateHookProvider.get ());

		}

	}

}
