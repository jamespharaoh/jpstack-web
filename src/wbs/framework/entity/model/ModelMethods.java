package wbs.framework.entity.model;

import java.util.List;
import java.util.Map;

public
interface ModelMethods {

	Class<?> objectClass ();
	String objectName ();
	String objectTypeCode ();

	String tableName ();
	Boolean create ();
	Boolean mutable ();

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
