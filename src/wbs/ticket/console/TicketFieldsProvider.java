package wbs.ticket.console;

import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.underscoreToCamel;

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
import wbs.ticket.model.TicketFieldTypeType;
import wbs.ticket.model.TicketManagerRec;

@PrototypeComponent ("ticketFieldsProvider")
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
	TicketConsoleHelper ticketConsoleHelper;
	
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
			
			if (ticketFieldType.getType().equals(TicketFieldTypeType.string)) {
				
	
				formFieldSpecs
					.add(new TextFormFieldSpec()
				
						.name(ticketFieldType.getCode ())					
						.label(ticketFieldType.getName ())
						.dynamic (true));
				
			} 
			else if (ticketFieldType.getType().equals(TicketFieldTypeType.number)) {
				
				formFieldSpecs
					.add(new IntegerFormFieldSpec()
				
						.name(ticketFieldType.getCode ())					
						.label(ticketFieldType.getName ())
						.dynamic (true));
				
			} 
			else if (ticketFieldType.getType().equals(TicketFieldTypeType.bool)) {
				
				formFieldSpecs
					.add(new YesNoFormFieldSpec()
				
						.name(ticketFieldType.getCode ())					
						.label(ticketFieldType.getName ())
						.dynamic (true));
				
			} 
			else if (ticketFieldType.getType().equals(TicketFieldTypeType.object)) {
				
				formFieldSpecs
					.add(new ObjectFormFieldSpec()
				
						.name(ticketFieldType.getCode ())					
						.label(ticketFieldType.getName ())
						.finderName(underscoreToCamel(ticketFieldType.getObjectType().getCode()))
						.dynamic (true));
				
			} 		    
		}
		
		String fieldSetName =
			stringFormat (
				"%s.list",
				ticketConsoleHelper.objectName());

		return consoleModuleBuilder.buildFormFieldSet (
			ticketConsoleHelper,
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

	@SingletonComponent("ticketFieldsProviderConfig")
	public static
	class Config {

		@Inject
		Provider<TicketFieldsProvider> ticketFieldsProvider;
		
		@PrototypeComponent ("ticketListFieldsProvider")
		public
		FieldsProvider ticketListFieldsProvider () {

			return ticketFieldsProvider.get ()
				.setMode ("list");

		}		

	}
	
}
