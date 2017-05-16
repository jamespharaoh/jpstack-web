package wbs.console.forms.types;

import wbs.framework.entity.record.Record;

public
interface ObjectFieldsProvider <
	Container extends Record <Container>,
	Parent extends Record <Parent>
>
	extends FieldsProvider <Container, Parent> {

}
