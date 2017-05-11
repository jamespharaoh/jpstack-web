package wbs.console.forms.types;

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
