package wbs.services.ticket.core.console;

import wbs.framework.record.Record;
import wbs.platform.console.forms.FormFieldSet;

public interface FieldsProvider {

	FormFieldSet getFields (
			Record<?> parent);
	
	FieldsProvider setFields (
			FormFieldSet fields);

	FieldsProvider setMode(
			String modeSet);
	
}
