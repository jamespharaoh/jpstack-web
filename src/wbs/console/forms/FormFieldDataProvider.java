package wbs.console.forms;

import wbs.framework.record.Record;

public
interface FormFieldDataProvider {

	String getFormFieldData (
		Record<?> parent);

	FormFieldDataProvider setMode (
		String modeSet);

}
