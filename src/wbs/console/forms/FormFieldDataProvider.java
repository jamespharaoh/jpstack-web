package wbs.console.forms;

public
interface FormFieldDataProvider<
	ObjectType,
	ParentType
> {

	String getFormFieldDataForParent (
			ParentType parent);

	String getFormFieldDataForObject (
			ObjectType object);

}
