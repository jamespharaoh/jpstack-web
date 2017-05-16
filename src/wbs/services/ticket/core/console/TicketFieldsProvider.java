package wbs.services.ticket.core.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.underscoreToCamel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.NonNull;

import wbs.console.forms.basic.IntegerFormFieldSpec;
import wbs.console.forms.basic.YesNoFormFieldSpec;
import wbs.console.forms.core.ConsoleFormBuilder;
import wbs.console.forms.core.FormFieldSet;
import wbs.console.forms.object.ObjectFormFieldSpec;
import wbs.console.forms.text.TextFormFieldSpec;
import wbs.console.forms.types.FieldsProvider;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.services.ticket.core.model.TicketFieldTypeObjectHelper;
import wbs.services.ticket.core.model.TicketFieldTypeRec;
import wbs.services.ticket.core.model.TicketManagerRec;
import wbs.services.ticket.core.model.TicketRec;

@PrototypeComponent ("ticketFieldsProvider")
public
class TicketFieldsProvider
	implements FieldsProvider <TicketRec, TicketManagerRec> {

	// singleton dependencies

	@SingletonDependency
	ConsoleFormBuilder consoleFormBuilder;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	TicketFieldTypeObjectHelper ticketFieldTypeHelper;

	@SingletonDependency
	TicketFieldTypeConsoleHelper ticketFieldTypeConsoleHelper;

	@SingletonDependency
	TicketConsoleHelper ticketConsoleHelper;

	// state

	FormFieldSet <TicketRec> formFields;

	String mode;

	// implementation

	@Override
	public
	FormFieldSet <TicketRec> getFieldsForObject (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull TicketRec ticket) {

		return getFieldsForParent (
			parentTaskLogger,
			ticket.getTicketManager ());

	}

	@Override
	public
	FormFieldSet <TicketRec> getFieldsForParent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull TicketManagerRec ticketManager) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getFieldsForParent");

		) {

			// retrieve existing ticket field types

			Set <TicketFieldTypeRec> ticketFieldTypes =
				ticketManager.getTicketFieldTypes ();

			// build form fields

			List <Object> formFieldSpecs =
				new ArrayList<> ();

			for (
				TicketFieldTypeRec ticketFieldType
					: ticketFieldTypes
			) {

				switch (ticketFieldType.getDataType ()) {

				case string:

					formFieldSpecs.add (
						new TextFormFieldSpec ()

						.name (
							ticketFieldType.getCode ())

						.label (
							ticketFieldType.getName ())

						.dynamic (
							true)

					);

					break;

				case number:

					formFieldSpecs.add (
						new IntegerFormFieldSpec ()

						.name (
							ticketFieldType.getCode ())

						.label (
							ticketFieldType.getName ())

						.dynamic (
							true));

					break;

				case bool:

					formFieldSpecs.add (
						new YesNoFormFieldSpec ()

						.name (
							ticketFieldType.getCode ())

						.label (
							ticketFieldType.getName ())

						.dynamic (
							true)

					);

					break;

				case object:

					formFieldSpecs.add (
						new ObjectFormFieldSpec ()

						.name (
							ticketFieldType.getCode ())

						.label (
							ticketFieldType.getName ())

						.objectTypeName (
							underscoreToCamel (
							 	ticketFieldType.getObjectType ().getCode ()))

						.dynamic (
							true)

					);

					break;

				default:

					throw new RuntimeException ();

				}

			}

			// adding the state field

			formFieldSpecs.add (
				new ObjectFormFieldSpec ()

				.name (
					"ticketState")

				.label (
					"State")

				.objectTypeName (
					"ticketState")

				.dynamic (
					false)

			);

			String fieldSetName =
				stringFormat (
					"%s.%s",
					ticketConsoleHelper.objectName(),
					mode);

			return consoleFormBuilder.buildFormFieldSet (
				taskLogger,
				ticketConsoleHelper,
				fieldSetName,
				formFieldSpecs);

		}

	}

	@Override
	public
	FormFieldSet <TicketRec> getStaticFields () {

		throw new UnsupportedOperationException ();

	}

	public
	TicketFieldsProvider setMode (
			@NonNull String modeSet) {

		mode =
			modeSet;

		return this;

	}

}
