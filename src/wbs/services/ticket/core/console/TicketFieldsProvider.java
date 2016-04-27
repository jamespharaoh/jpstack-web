package wbs.services.ticket.core.console;

import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.underscoreToCamel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.IntegerFormFieldSpec;
import wbs.console.forms.ObjectFormFieldSpec;
import wbs.console.forms.TextFormFieldSpec;
import wbs.console.forms.YesNoFormFieldSpec;
import wbs.console.module.ConsoleModuleBuilder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.services.ticket.core.model.TicketFieldTypeObjectHelper;
import wbs.services.ticket.core.model.TicketFieldTypeRec;
import wbs.services.ticket.core.model.TicketManagerRec;
import wbs.services.ticket.core.model.TicketRec;

@PrototypeComponent ("ticketFieldsProvider")
public
class TicketFieldsProvider
	implements FieldsProvider<TicketRec,TicketManagerRec> {

	// dependencies

	@Inject
	ConsoleModuleBuilder consoleModuleBuilder;

	@Inject
	TicketFieldTypeObjectHelper ticketFieldTypeHelper;

	@Inject
	TicketFieldTypeConsoleHelper ticketFieldTypeConsoleHelper;

	@Inject
	TicketConsoleHelper ticketConsoleHelper;

	// state

	FormFieldSet formFields;

	String mode;

	// implementation

	@Override
	public
	FormFieldSet getFieldsForObject (
			@NonNull TicketRec ticket) {

		return getFieldsForParent (
			ticket.getTicketManager ());

	}

	@Override
	public
	FormFieldSet getFieldsForParent (
			@NonNull TicketManagerRec ticketManager) {

		// retrieve existing ticket field types

		Set<TicketFieldTypeRec> ticketFieldTypes =
			ticketManager.getTicketFieldTypes ();

		// build form fields

		List<Object> formFieldSpecs =
			new ArrayList<Object> ();

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

		return consoleModuleBuilder.buildFormFieldSet (
			ticketConsoleHelper,
			fieldSetName,
			formFieldSpecs);

	}

	@Override
	public
	FormFieldSet getStaticFields () {

		throw new UnsupportedOperationException ();

	}

	public
	TicketFieldsProvider setMode (
			@NonNull String modeSet) {

		mode =
			modeSet;

		return this;

	}

	@SingletonComponent ("ticketFieldsProviderConfig")
	public static
	class Config {

		@PrototypeDependency
		Provider<TicketFieldsProvider> ticketFieldsProvider;

		@PrototypeComponent ("ticketListFieldsProvider")
		@Named
		public
		TicketFieldsProvider ticketListFieldsProvider () {

			return ticketFieldsProvider.get ()

				.setMode (
					"list");

		}

		@PrototypeComponent ("ticketCreateFieldsProvider")
		@Named
		public
		TicketFieldsProvider ticketCreateFieldsProvider () {

			return ticketFieldsProvider.get ()

				.setMode (
					"create");

		}

		@PrototypeComponent ("ticketSettingsFieldsProvider")
		@Named
		public
		TicketFieldsProvider ticketSettingsFieldsProvider () {

			return ticketFieldsProvider.get ()

				.setMode (
					"settings");

		}

		@PrototypeComponent ("ticketSummaryFieldsProvider")
		@Named
		public
		TicketFieldsProvider ticketSummaryFieldsProvider () {

			return ticketFieldsProvider.get ()

				.setMode (
					"summary");

		}

	}

}
