package wbs.framework.entity.model;

import java.util.List;
import java.util.Map;

import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectHelper;

public
interface ModelMethods {

	// identity

	String objectName ();
	Class<? extends Record<?>> objectClass ();
	String objectTypeCode ();

	String tableName ();

	// fields

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

	List <ModelField> fields ();
	Map <String, ModelField> fieldsByName ();

	ModelField field (
			String name);

	// misc parameters

	Boolean isRoot ();
	Boolean isRooted ();
	Boolean canGetParent ();
	Boolean parentTypeIsFixed ();

	Boolean create ();
	Boolean mutable ();

	Class<? extends Record<?>> parentClass ();
	Class<? extends ObjectHelper<?>> helperClass ();

	// property accessors

	Record<?> getParent (
			Record<?> object);

	Record<?> getParentType (
			Record<?> object);

	Long getParentId (
			Record<?> object);

	String getTypeCode (
			Record<?> record);

	String getCode (
			Record<?> record);

	String getName (
			Record<?> record);

	String getDescription (
			Record<?> record);

}
