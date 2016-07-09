package wbs.services.ticket.create;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import wbs.console.action.ConsoleAction;
import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.PermanentRecord;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.services.ticket.core.console.TicketConsoleHelper;
import wbs.services.ticket.core.console.TicketFieldValueConsoleHelper;
import wbs.services.ticket.core.model.TicketFieldTypeObjectHelper;
import wbs.services.ticket.core.model.TicketFieldTypeRec;
import wbs.services.ticket.core.model.TicketFieldValueRec;
import wbs.services.ticket.core.model.TicketManagerRec;
import wbs.services.ticket.core.model.TicketRec;

@Accessors (fluent = true)
@PrototypeComponent ("objectTicketCreateAction")
public
class ObjectTicketCreateAction<
	ObjectType extends Record<ObjectType>,
	ParentType extends Record<ParentType>
>
	extends ConsoleAction {

	// dependencies

	@Inject
	ConsoleManager consoleManager;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ObjectTypeObjectHelper objectTypeHelper;

	@Inject
	UserPrivChecker privChecker;

	@Inject
	QueueLogic queueLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	RootObjectHelper rootHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	TicketFieldTypeObjectHelper ticketFieldTypeHelper;

	@Inject
	TicketFieldValueConsoleHelper ticketFieldValueHelper;

	@Inject
	TicketConsoleHelper ticketHelper;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// properties

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

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
	FormFieldSet formFieldSet;

	@Getter @Setter
	String createTimeFieldName;

	@Getter @Setter
	String createUserFieldName;

	@Getter @Setter
	String ticketManagerPath;

	@Getter @Setter
	FieldsProvider<TicketRec,TicketManagerRec> formFieldsProvider;

	@Getter @Setter
	List<ObjectTicketCreateSetFieldSpec> ticketFieldSpecs;

	// state

	ConsoleHelper<ParentType> parentHelper;
	TicketManagerRec ticketManager;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			responderName ());

	}

	@Override
	protected
	Responder goReal () {

		@SuppressWarnings ("unchecked")
		ConsoleHelper<ParentType> parentHelperTemp =
			(ConsoleHelper<ParentType>)
			objectManager.findConsoleHelper (
				consoleHelper.parentClass ());

		parentHelper =
			parentHelperTemp;

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		// find context object

		Record<?> contextObject =
			consoleHelper.findRequired (
				requestContext.stuffInt (
					consoleHelper.idKey ()));

		// determine ticket

		ticketManager =
			(TicketManagerRec)
			objectManager.dereference (
				contextObject,
				ticketManagerPath);

		if (ticketManager == null)
			return null;

		// check permissions

		if (createPrivCode != null) {

			Record<?> createDelegate =
				createPrivDelegate != null
					? (Record<?>) objectManager.dereference (
							ticketManager,
						createPrivDelegate)
					: ticketManager;

			if (! privChecker.canRecursive (
					createDelegate,
					createPrivCode)) {

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

				Integer objectId = (
					(Record<?>)
					objectManager.dereference (
						contextObject,
						ticketFieldSpec.valuePath ())
				).getId ();

				ticketFieldValue

					.setIntegerValue (
						(long)
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

			BeanLogic.setProperty (
				ticket,
				consoleHelper.typeCodeFieldName (),
				typeCode);

		}

		// perform updates

		if (formFieldsProvider != null) {
			prepareFieldSet();
		}

		UpdateResultSet updateResultSet =
			formFieldLogic.update (
				requestContext,
				formFieldSet,
				ticket,
				ImmutableMap.of (),
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

			BeanLogic.setProperty (
				ticket,
				createTimeFieldName,
				transaction.now ());

		}

		// set create user

		if (createUserFieldName != null) {

			BeanLogic.setProperty (
				ticket,
				createUserFieldName,
				userConsoleLogic.userRequired ());

		}

		// insert

		ticketHelper.insert (
			ticket);

		// create event

		Object objectRef =
			consoleHelper.codeExists ()
				? consoleHelper.getCode (ticket)
				: ticket.getId ();

		if (consoleHelper.ephemeral ()) {

			eventLogic.createEvent (
				"object_created_in",
				userConsoleLogic.userRequired (),
				objectRef,
				consoleHelper.shortName (),
				ticketManager);

		} else {

			eventLogic.createEvent (
				"object_created",
				userConsoleLogic.userRequired (),
				ticket,
				ticketManager);

		}

		// update events

		if (ticket instanceof PermanentRecord) {

			formFieldLogic.runUpdateHooks (
				formFieldSet,
				updateResultSet,
				ticket,
				(PermanentRecord<?>) ticket,
				Optional.<Object>absent (),
				Optional.<String>absent (),
				"create");

		} else {

			formFieldLogic.runUpdateHooks (
				formFieldSet,
				updateResultSet,
				ticket,
				(PermanentRecord<?>) ticketManager,
				Optional.of (
					objectRef),
				Optional.of (
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

	void prepareFieldSet () {

		formFieldSet =
			formFieldsProvider.getFieldsForParent (
				ticketManager);

	}

}
