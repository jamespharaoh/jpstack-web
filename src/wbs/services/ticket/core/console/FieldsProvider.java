package wbs.services.ticket.core.console;

import wbs.console.forms.FormFieldSet;
import wbs.framework.record.Record;

public
interface FieldsProvider {

	FormFieldSet getFields (
			Record<?> parent);

	FieldsProvider setFields (
			FormFieldSet fields);

	FieldsProvider setMode (
			String modeSet);

}
