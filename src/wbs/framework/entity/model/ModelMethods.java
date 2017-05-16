package wbs.framework.entity.model;

import java.util.List;
import java.util.Map;

import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectHelper;

public
interface ModelMethods <RecordType extends Record <RecordType>> {

	// identity

	String objectName ();
	Class <RecordType> objectClass ();
	String objectTypeCode ();

	String oldObjectName ();
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

	Class <? extends Record <?>> parentClassRequired ();
	Class <? extends ObjectHelper <?>> helperClass ();

	// property accessors

	Record <?> getParentOrNull (
			RecordType object);

	default
	Record <?> getParentOrNullGeneric (
			Record <?> object) {

		return getParentOrNull (
			objectClass ().cast (
				object));

	}

	Record <?> getParentType (
			RecordType object);

	default
	Record <?> getParentTypeGeneric (
			Record <?> object) {

		return objectClass ().cast (
			object);

	}

	Long getParentId (
			RecordType object);

	String getTypeCode (
			RecordType object);

	default
	String getTypeCodeGeneric (
			Record <?> object) {

		return getTypeCode (
			objectClass ().cast (
				object));

	}

	String getCode (
			RecordType record);

	default
	String getCodeGeneric (
			Record <?> record) {

		return getCode (
			objectClass ().cast (
				record));

	}

	String getName (
			RecordType record);

	String getDescription (
			RecordType record);

}
