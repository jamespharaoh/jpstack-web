package wbs.framework.entity.model;

import java.util.List;
import java.util.Map;

public
interface ModelMethods {

	ModelField idField ();
	ModelField parentField ();
	ModelField parentTypeField ();
	ModelField parentIdField ();
	ModelField typeCodeField ();
	ModelField codeField ();
	ModelField indexField ();
	ModelField nameField ();
	ModelField descriptionField ();
	ModelField deletedField ();

	List<ModelField> fields ();
	Map<String,ModelField> fieldsByName ();

	ModelField field (
			String name);

}
