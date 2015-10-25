package wbs.framework.entity.model;

import java.util.List;
import java.util.Map;

public
interface ModelMethods {

	ModelField codeField ();
	ModelField deletedField ();
	ModelField descriptionField ();
	ModelField idField ();
	ModelField indexField ();
	ModelField nameField ();
	ModelField parentField ();
	ModelField parentIdField ();
	ModelField parentTypeField ();
	ModelField timestampField ();
	ModelField typeCodeField ();

	List<ModelField> fields ();
	Map<String,ModelField> fieldsByName ();

	ModelField field (
			String name);

	Boolean isRoot ();
	Boolean isRooted ();
	Boolean canGetParent ();
	Boolean parentTypeIsFixed ();

}
