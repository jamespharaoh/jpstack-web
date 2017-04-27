package wbs.services.ticket.create;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.PermanentRecord;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.utils.etc.PropertyUtils;

import wbs.services.ticket.core.console.TicketConsoleHelper;
import wbs.services.ticket.core.console.TicketFieldValueConsoleHelper;
import wbs.services.ticket.core.model.TicketFieldTypeObjectHelper;
import wbs.services.ticket.core.model.TicketFieldTypeRec;
import wbs.services.ticket.core.model.TicketFieldValueRec;
import wbs.services.ticket.core.model.TicketManagerRec;
import wbs.services.ticket.core.model.TicketRec;
import wbs.web.responder.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("objectTicketCreateAction")
public
class ObjectTicketCreateAction <
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
>
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ObjectTypeObjectHelper objectTypeHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	RootObjectHelper rootHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	TicketFieldTypeObjectHelper ticketFieldTypeHelper;

	@SingletonDependency
	TicketFieldValueConsoleHelper ticketFieldValueHelper;

	@SingletonDependency
	TicketConsoleHelper ticketHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// properties

	@Getter @Setter
	ConsoleHelper <TicketRec> consoleHelper;

	@Getter @Setter
	String typeCode;

	@Getter @Setter
	String responderName;

	@Getter @Setter
	String targetContextTypeName;

	@Getter @Setter
	String targetResponderName;

	@Getter @Setter
	String createPrivDelegate;

	@Getter @Setter
	String createPrivCode;

	@Getter @Setter
	FormFieldSet <TicketRec> fields;

	@Getter @Setter
	String createTimeFieldName;

	@Getter @Setter
	String createUserFieldName;

	@Getter @Setter
	String ticketManagerPath;

	@Getter @Setter
	FieldsProvider <TicketRec, TicketManagerRec> formFieldsProvider;

	@Getter @Setter
	List <ObjectTicketCreateSetFieldSpec> ticketFieldSpecs;

	// state

	ConsoleHelper <ParentType> parentHelper;
	TicketManagerRec ticketManager;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			responderName ());

	}

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goReal");

		// begin transaction

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					taskLogger,
					"ObjectTicketCreateAction.goReal ()",
					this);

		) {

			// find context object

			Record <?> contextObject =
				consoleHelper.findFromContextRequired ();

			// determine ticket

			ticketManager =
				genericCastUnchecked (
					objectManager.dereferenceObsolete (
						contextObject,
						ticketManagerPath));

			if (ticketManager == null)
				return null;

			// check permissions

			if (createPrivCode != null) {

				Record<?> createDelegate =
					createPrivDelegate != null
						? (Record<?>) objectManager.dereferenceObsolete (
								ticketManager,
							createPrivDelegate)
						: ticketManager;

				if (
					! privChecker.canRecursive (
						taskLogger,
						createDelegate,
						createPrivCode)
				) {

					requestContext.addError (
						"Permission denied");

					return null;

				}

			}

			// create new record and set parent

			TicketRec ticket =
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

					Long objectId = (
						(Record <?>)
						objectManager.dereferenceObsolete (
							contextObject,
							ticketFieldSpec.valuePath ())
					).getId ();

					ticketFieldValue

						.setIntegerValue (
							objectId);

					break;

				default:

					throw new RuntimeException ();

				}

				ticket

					.setNumFields (
						ticket.getNumFields () + 1);

				ticket.getTicketFieldValues ().put (
					ticketFieldType.getId (),
					ticketFieldValue);

			}

			// set type code

			if (consoleHelper.typeCodeExists ()) {

				PropertyUtils.propertySetAuto (
					ticket,
					consoleHelper.typeCodeFieldName (),
					typeCode);

			}

			// perform updates

			if (formFieldsProvider != null) {

				prepareFieldSet (
					taskLogger);

			}

			UpdateResultSet updateResultSet =
				formFieldLogic.update (
					taskLogger,
					requestContext,
					fields,
					ticket,
					emptyMap (),
					"create");

			if (updateResultSet.errorCount () > 0) {

				formFieldLogic.reportErrors (
					requestContext,
					updateResultSet,
					"create");

				return null;

			}

			// set create time

			if (createTimeFieldName != null) {

				PropertyUtils.propertySetAuto (
					ticket,
					createTimeFieldName,
					transaction.now ());

			}

			// set create user

			if (createUserFieldName != null) {

				PropertyUtils.propertySetAuto (
					ticket,
					createUserFieldName,
					userConsoleLogic.userRequired ());

			}

			// insert

			ticketHelper.insert (
				taskLogger,
				ticket);

			// create event

			Object objectRef =
				consoleHelper.codeExists ()
					? consoleHelper.getCode (ticket)
					: ticket.getId ();

			if (consoleHelper.ephemeral ()) {

				eventLogic.createEvent (
					taskLogger,
					"object_created_in",
					userConsoleLogic.userRequired (),
					objectRef,
					consoleHelper.shortName (),
					ticketManager);

			} else {

				eventLogic.createEvent (
					taskLogger,
					"object_created",
					userConsoleLogic.userRequired (),
					ticket,
					ticketManager);

			}

			// update events

			if (ticket instanceof PermanentRecord) {

				formFieldLogic.runUpdateHooks (
					taskLogger,
					fields,
					updateResultSet,
					ticket,
					(PermanentRecord <?>) ticket,
					optionalAbsent (),
					optionalAbsent (),
					"create");

			} else {

				formFieldLogic.runUpdateHooks (
					taskLogger,
					fields,
					updateResultSet,
					ticket,
					(PermanentRecord <?>) ticketManager,
					optionalOf (
						objectRef),
					optionalOf (
						consoleHelper.shortName ()),
					"create");

			}

			// commit transaction

			transaction.commit ();

			// prepare next page

			requestContext.addNotice (
				stringFormat (
					"%s created",
					capitalise (
						consoleHelper.shortName ())));

			requestContext.setEmptyFormData ();

			return null;

		}

	}

	void prepareFieldSet (
			@NonNull TaskLogger parentTaskLogger) {

		fields =
			formFieldsProvider.getFieldsForParent (
				parentTaskLogger,
				ticketManager);

	}

}
