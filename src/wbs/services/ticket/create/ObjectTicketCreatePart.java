package wbs.services.ticket.create;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.part.AbstractPagePart;
import wbs.services.ticket.core.console.FieldsProvider;
import wbs.services.ticket.core.model.TicketFieldTypeRec;
import wbs.services.ticket.core.model.TicketFieldValueRec;
import wbs.services.ticket.core.model.TicketManagerRec;
import wbs.services.ticket.core.model.TicketRec;
import wbs.services.ticket.core.model.TicketFieldTypeObjectHelper;
import wbs.services.ticket.core.model.TicketFieldValueObjectHelper;

@Accessors (fluent = true)
@PrototypeComponent ("objectTicketCreatePart")
public 
class ObjectTicketCreatePart
	extends AbstractPagePart {
	
	// dependencies

	@Inject
	ConsoleManager consoleManager;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	FormFieldLogic formFieldLogic;
	
	@Inject
	TicketFieldTypeObjectHelper ticketFieldTypeHelper;
	
	@Inject
	TicketFieldValueObjectHelper ticketFieldValueHelper;
	
	@Getter @Setter
	List<ObjectTicketCreateSetFieldSpec> ticketFieldSpecs;

	// properties

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

	@Getter @Setter
	String localFile;
	
	@Getter @Setter
	FieldsProvider fieldsProvider;
	
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
			consoleHelper.find (
				requestContext.stuffInt (
					consoleHelper.idKey ()));

		ticketManager =
			(TicketManagerRec)
			objectManager.dereference (
				contextObject,
				ticketManagerPath);	
				
		prepareFieldSet ();
		
		// create dummy instance

		ticket =
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

	}
	
	void prepareFieldSet () {
		
		formFieldSet = 
			fieldsProvider.getFields(
				ticketManager);
	
	}
	
	@Override
	public
	void goBodyStuff () {
	
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
				out,
				formFieldSet,
				ticket);
		
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
