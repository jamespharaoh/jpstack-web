package wbs.services.ticket.create;

import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormField.FormType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.services.ticket.core.console.TicketConsoleHelper;
import wbs.services.ticket.core.model.TicketFieldTypeObjectHelper;
import wbs.services.ticket.core.model.TicketFieldTypeRec;
import wbs.services.ticket.core.model.TicketFieldValueObjectHelper;
import wbs.services.ticket.core.model.TicketFieldValueRec;
import wbs.services.ticket.core.model.TicketManagerRec;
import wbs.services.ticket.core.model.TicketRec;

@Accessors (fluent = true)
@PrototypeComponent ("objectTicketCreatePart")
public
class ObjectTicketCreatePart<
	ObjectType extends Record<ObjectType>,
	ParentType extends Record<ParentType>
>
	extends AbstractPagePart {

	// dependencies

	@Inject
	ConsoleManager consoleManager;

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	TicketFieldTypeObjectHelper ticketFieldTypeHelper;

	@Inject
	TicketFieldValueObjectHelper ticketFieldValueHelper;

	@Inject
	TicketConsoleHelper ticketHelper;

	// properties

	@Getter @Setter
	List<ObjectTicketCreateSetFieldSpec> ticketFieldSpecs;

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

	@Getter @Setter
	String localFile;

	@Getter @Setter
	FieldsProvider<TicketRec,TicketManagerRec> fieldsProvider;

	@Getter @Setter
	String ticketManagerPath;

	// state

	ObjectTicketCreateSetFieldSpec currentTicketFieldSpec;
	TicketRec ticket;
	TicketManagerRec ticketManager;
	FormFieldSet formFieldSet;

	// implementation

	@Override
	public
	void prepare () {

		// find context object

		Record<?> contextObject =
			consoleHelper.findRequired (
				requestContext.stuffInteger (
					consoleHelper.idKey ()));

		ticketManager =
			(TicketManagerRec)
			objectManager.dereference (
				contextObject,
				ticketManagerPath);

		prepareFieldSet ();

		// create dummy instance

		ticket =
			ticketHelper.createInstance ()

			.setTicketManager (
				ticketManager);

		for (
			ObjectTicketCreateSetFieldSpec ticketFieldSpec
				: ticketFieldSpecs
		) {

			TicketFieldTypeRec ticketFieldType =
				ticketFieldTypeHelper.findByCodeRequired (
					ticketManager,
					ticketFieldSpec.fieldTypeCode ());

			TicketFieldValueRec ticketFieldValue =
				ticketFieldValueHelper.createInstance ()

				.setTicket (
					ticket)

				.setTicketFieldType (
					ticketFieldType);

			switch (ticketFieldType.getDataType ()) {

			case string:

				ticketFieldValue.setStringValue (
					(String)
					objectManager.dereference (
						contextObject,
						ticketFieldSpec.valuePath ()));

				break;

			case number:

				ticketFieldValue.setIntegerValue (
					(Long)
					objectManager.dereference (
						contextObject,
						ticketFieldSpec.valuePath ()));

				break;

			case bool:

				ticketFieldValue.setBooleanValue (
					(Boolean)
					objectManager.dereference (
						contextObject,
						ticketFieldSpec.valuePath ()));

				break;

			case object:

				Record<?> objectValue =
					(Record<?>)
					objectManager.dereference (
						contextObject,
						ticketFieldSpec.valuePath ());

				// TODO check type

				Long objectId =
					objectValue.getId ();

				ticketFieldValue

					.setIntegerValue (
						objectId);

				break;

			default:

				throw new RuntimeException ();

			}

			ticket.setNumFields (
				ticket.getNumFields () + 1);

			ticket.getTicketFieldValues ().put (
				ticketFieldType.getId (),
				ticketFieldValue);

		}

	}

	void prepareFieldSet () {

		formFieldSet =
			fieldsProvider.getFieldsForParent (
				ticketManager);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<p>Please enter the details for the new ticket</p>\n");

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/" + localFile),
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputFormRows (
			requestContext,
			formatWriter,
			formFieldSet,
			Optional.absent (),
			ticket,
			ImmutableMap.of (),
			FormType.create,
			"create");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"create ticket\"",
			"></p>\n");

		printFormat (
			"</form>\n");

	}

}
