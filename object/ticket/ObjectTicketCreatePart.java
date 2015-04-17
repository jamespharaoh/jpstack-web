package wbs.platform.object.ticket;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.context.ConsoleContext;
import wbs.platform.console.context.ConsoleContextType;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.object.list.ObjectListPart;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.ticket.console.FieldsProvider;
import wbs.ticket.model.TicketFieldTypeObjectHelper;
import wbs.ticket.model.TicketFieldTypeRec;
import wbs.ticket.model.TicketFieldValueObjectHelper;
import wbs.ticket.model.TicketFieldValueRec;
import wbs.ticket.model.TicketManagerRec;
import wbs.ticket.model.TicketRec;

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
			" value=\"create %h\"",
			consoleHelper.shortName (),
			"></p>\n");

		printFormat (
			"</form>\n");			
		
	}
	
}
