package wbs.services.ticket.create;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormType;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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
class ObjectTicketCreatePart <
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
>
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	TicketFieldTypeObjectHelper ticketFieldTypeHelper;

	@SingletonDependency
	TicketFieldValueObjectHelper ticketFieldValueHelper;

	@SingletonDependency
	TicketConsoleHelper ticketHelper;

	// properties

	@Getter @Setter
	List <ObjectTicketCreateSetFieldSpec> ticketFieldSpecs;

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

	@Getter @Setter
	String localFile;

	@Getter @Setter
	FieldsProvider <TicketRec, TicketManagerRec> fieldsProvider;

	@Getter @Setter
	String ticketManagerPath;

	// state

	ObjectTicketCreateSetFieldSpec currentTicketFieldSpec;
	TicketRec ticket;
	TicketManagerRec ticketManager;
	FormFieldSet <TicketRec> formFieldSet;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"prepare");

		) {

			// find context object

			Record <?> contextObject =
				consoleHelper.findFromContextRequired ();

			ticketManager =
				genericCastUnchecked (
					objectManager.dereference (
						contextObject,
						ticketManagerPath));

			prepareFieldSet (
				taskLogger);

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
						objectManager.dereferenceObsolete (
							contextObject,
							ticketFieldSpec.valuePath ()));

					break;

				case number:

					ticketFieldValue.setIntegerValue (
						(Long)
						objectManager.dereferenceObsolete (
							contextObject,
							ticketFieldSpec.valuePath ()));

					break;

				case bool:

					ticketFieldValue.setBooleanValue (
						(Boolean)
						objectManager.dereferenceObsolete (
							contextObject,
							ticketFieldSpec.valuePath ()));

					break;

				case object:

					Record<?> objectValue =
						(Record<?>)
						objectManager.dereferenceObsolete (
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

	}

	void prepareFieldSet (
			@NonNull TaskLogger parentTaskLogger) {

		formFieldSet =
			fieldsProvider.getFieldsForParent (
				parentTaskLogger,
				ticketManager);

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"renderHtmlBodyContent");

		) {

			htmlParagraphWriteFormat (
				"Please enter the details for the new ticket");

			htmlFormOpenPostAction (
				requestContext.resolveLocalUrl (
					"/" + localFile));

			htmlTableOpenDetails ();

			formFieldLogic.outputFormRows (
				taskLogger,
				requestContext,
				formatWriter,
				formFieldSet,
				optionalAbsent (),
				ticket,
				emptyMap (),
				FormType.create,
				"create");

			htmlTableClose ();

			htmlParagraphOpen ();

			formatWriter.writeFormat (
				"<input",
				" type=\"submit\"",
				" value=\"create ticket\"",
				">");

			htmlParagraphClose ();

			htmlFormClose ();

		}

	}

}
