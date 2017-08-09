package wbs.console.forms.object;

import wbs.console.forms.types.FieldsProvider.FormFieldSetPair;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

public
interface ObjectFieldsProvider <
	Container extends Record <Container>,
	Parent extends Record <Parent>
> {

	Class <Container> containerClass ();

	Class <Parent> parentClass ();

	FormFieldSetPair <Container> getListFields (
			Transaction parentTransaction,
			Container object);

	FormFieldSetPair <Container> getCreateFields (
			Transaction parentTransaction,
			Parent parent);

	FormFieldSetPair <Container> getSummaryFields (
			Transaction parentTransaction,
			Container object);

	FormFieldSetPair <Container> getSettingsFields (
			Transaction parentTransaction,
			Container object);

}
