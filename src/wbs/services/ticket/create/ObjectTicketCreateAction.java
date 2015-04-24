package wbs.services.ticket.create;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.PermanentRecord;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.services.ticket.core.console.FieldsProvider;
import wbs.services.ticket.core.model.TicketFieldTypeRec;
import wbs.services.ticket.core.model.TicketFieldValueRec;
import wbs.services.ticket.core.model.TicketManagerRec;
import wbs.services.ticket.core.model.TicketRec;
import wbs.services.ticket.core.model.TicketFieldTypeObjectHelper;

@Accessors (fluent = true)
@PrototypeComponent ("objectTicketCreateAction")
public 
class ObjectTicketCreateAction 
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
	PrivChecker privChecker;
	
	@Inject
	ConsoleRequestContext requestContext;
	
	@Inject
	RootObjectHelper rootHelper;
	
	@Inject
	TextObjectHelper textHelper;
	
	@Inject
	UserObjectHelper userHelper;
	
	@Inject
	TicketFieldTypeObjectHelper ticketFieldTypeHelper;
	
	@Inject
	QueueLogic queueLogic;
	
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
	FieldsProvider formFieldsProvider;
	
	@Getter @Setter
	List<ObjectTicketCreateSetFieldSpec> ticketFieldSpecs;
	
	// state
	
	ConsoleHelper<?> parentHelper;
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
	
		parentHelper =
			objectManager.getConsoleObjectHelper (
				consoleHelper.parentClass ());
	
		// begin transaction
	
		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);
	
		UserRec myUser =
			userHelper.find (
				requestContext.userId ());
	
		// find context object

		Record<?> contextObject =
			consoleHelper.find (
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
	
			if (! privChecker.can (
					createDelegate,
					createPrivCode)) {
	
				requestContext.addError (
					"Permission denied");
	
				return null;
	
			}
	
		}
	
		// create new record and set parent
	
		TicketRec ticket =
			new TicketRec ()			
				.setTicketManager(ticketManager);
		
		for (ObjectTicketCreateSetFieldSpec ticketFieldSpec
				: ticketFieldSpecs) {

			TicketFieldTypeRec ticketFieldType
				= ticketFieldTypeHelper.findByCode (
					ticketManager, 
					ticketFieldSpec.fieldTypeCode());
			
			if (ticketFieldType == null) {
				throw new RuntimeException ("Field type does not exist");
			}
					
			TicketFieldValueRec ticketFieldValue =
				new TicketFieldValueRec ()				
			
					.setTicket(ticket)
					.setTicketFieldType(ticketFieldType);
					
			switch( ticketFieldType.getType() ) {
				case string:					
					ticketFieldValue.setStringValue (
						(String)objectManager.dereference (
							contextObject,
							ticketFieldSpec.valuePath()));
					break;
					
				case number:
					ticketFieldValue.setIntegerValue(
						(Integer)objectManager.dereference (
							contextObject,
							ticketFieldSpec.valuePath()));
					break;
					
				case bool:
					ticketFieldValue.setBooleanValue(
						(Boolean)objectManager.dereference (
							contextObject,
							ticketFieldSpec.valuePath()));
					break;
					
				case object:
					
					Integer objectId = 
						((Record<?>) objectManager.dereference (
							contextObject,
							ticketFieldSpec.valuePath())).getId();
							
					ticketFieldValue.setIntegerValue(objectId);
					break;
					
				default:
					throw new RuntimeException ();
			
			}		
			
			ticket.setNumFields (
				ticket.getNumFields() + 1);
					
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
				formFieldSet,
				ticket);
	
		if (updateResultSet.errorCount () > 0) {
	
			formFieldLogic.reportErrors (
				updateResultSet);
	
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
				myUser);
	
		}
	
		// insert
	
		objectManager.insert (
			ticket);
	
		// create event
	
		Object objectRef =
			consoleHelper.codeExists ()
				? consoleHelper.getCode (ticket)
				: ticket.getId ();
	
		if (consoleHelper.ephemeral ()) {
	
			eventLogic.createEvent (
				"object_created_in",
				myUser,
				objectRef,
				consoleHelper.shortName (),
				ticketManager);
	
		} else {
	
			eventLogic.createEvent (
				"object_created",
				myUser,
				ticket,
				ticketManager);
	
		}
	
		// update events
	
		if (ticket instanceof PermanentRecord) {
	
			formFieldLogic.runUpdateHooks (
				updateResultSet,
				ticket,
				(PermanentRecord<?>) ticket,
				null,
				null);
	
		} else {
	
			formFieldLogic.runUpdateHooks (
				updateResultSet,
				ticket,
				(PermanentRecord<?>) ticketManager,
				objectRef,
				consoleHelper.shortName ());
	
		}
		
		// commit transaction
	
		transaction.commit ();
	
		// prepare next page
	
		requestContext.addNotice (
			stringFormat (
				"%s created",
				capitalise (consoleHelper.shortName ())));
	
		requestContext.setEmptyFormData ();

		return null;
	}
	
	void prepareFieldSet () {
		
		formFieldSet = formFieldsProvider.getFields(
			ticketManager);
	
	}

}
