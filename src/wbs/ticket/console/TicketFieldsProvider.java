package wbs.ticket.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.Record;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.forms.IntegerFormFieldSpec;
import wbs.platform.console.forms.ObjectFormFieldSpec;
import wbs.platform.console.forms.TextFormFieldSpec;
import wbs.platform.console.forms.YesNoFormFieldSpec;
import wbs.platform.console.module.ConsoleModuleBuilder;
import wbs.ticket.model.TicketFieldTypeObjectHelper;
import wbs.ticket.model.TicketFieldTypeRec;
import wbs.ticket.model.TicketManagerRec;

@PrototypeComponent ("ticketListFieldsProvider")
public 
class TicketFieldsProvider 
	implements FieldsProvider {
	
	@Inject
	ConsoleModuleBuilder consoleModuleBuilder;
	
	@Inject
	TicketFieldTypeObjectHelper ticketFieldTypeHelper;
	
	@Inject
	TicketFieldTypeConsoleHelper ticketFieldTypeConsoleHelper;
	
	@Inject
	TicketFieldValueConsoleHelper ticketFieldValueConsoleHelper;
	
	FormFieldSet formFields;
	
	String mode;

	@Override
	public 
	FormFieldSet getFields(Record<?> parent) {
		
		// retrieve existing ticket field types
		
		TicketManagerRec ticketManager =
			(TicketManagerRec) parent;
		
		Set<TicketFieldTypeRec> ticketFieldTypes =
				ticketManager.getTicketFieldTypes();
		
		// build form fields
		
		List<Object> formFieldSpecs =
				new ArrayList<Object> ();
		
		for (TicketFieldTypeRec ticketFieldType : ticketFieldTypes) {
			
			
			if (ticketFieldType.getType().equals("string")) {
				
	
				formFieldSpecs
					.add(new TextFormFieldSpec()
				
						.name("value")
					
						.label("value"));
				
			} 
			else if (ticketFieldType.getType().equals("number")) {
				
				formFieldSpecs
					.add(new IntegerFormFieldSpec()
				
						.name("value")
					
						.label("value"));
				
			} 
			else if (ticketFieldType.getType().equals("boolean")) {
				
				formFieldSpecs
					.add(new YesNoFormFieldSpec()
				
						.name("value")
					
						.label("value"));
				
			} 
			else if (ticketFieldType.getType().equals("object")) {
				
				formFieldSpecs
					.add(new ObjectFormFieldSpec()
				
						.name("value")
					
						.label("value"));
				
			} 
			else {
				
			}
		    
		}
		
		String fieldSetName =
			stringFormat (
				"%s.list",
				ticketFieldValueConsoleHelper.objectName());

		return consoleModuleBuilder.buildFormFieldSet (
			ticketFieldValueConsoleHelper,
			fieldSetName,
			formFieldSpecs);
	
	}

	@Override
	public FieldsProvider setFields(FormFieldSet fields) {
		
		formFields = fields;	
		return this;
		
	}
	
	@Override
	public FieldsProvider setMode (String modeSet) {
		
		mode = modeSet;	
		return this;
		
	}

	@SingletonComponent(value = "ticketFieldsProviderConfig")
	public static
	class Config {

		@Inject
		Provider<TicketFieldsProvider> ticketFieldsProvider;
		
		@PrototypeComponent ("ticketListFieldProvider")
		public
		FieldsProvider ticketListFieldProvider () {

			return ticketFieldsProvider.get ()
				.setMode ("list");

		}
		

	}
	
}
