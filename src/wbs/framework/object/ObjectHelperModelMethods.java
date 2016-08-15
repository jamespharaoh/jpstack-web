package wbs.framework.object;

import wbs.framework.record.Record;

public
interface ObjectHelperModelMethods<RecordType extends Record<RecordType>> {

	Class<RecordType> objectClass ();
	String objectName ();
	String objectTypeCode ();
	Long objectTypeId ();

	String friendlyName ();
	String friendlyNamePlural ();

	String shortName ();
	String shortNamePlural ();

	Class<?> parentClass ();
	String parentFieldName ();
	String parentLabel ();
	Boolean parentExists ();

	String typeCodeFieldName ();
	String typeCodeLabel ();
	Boolean typeCodeExists ();

	String codeFieldName ();
	String codeLabel ();
	Boolean codeExists ();

	String indexFieldName ();
	String indexLabel ();
	Boolean indexExists ();
	String indexCounterFieldName ();

	String deletedFieldName ();
	String deletedLabel ();
	Boolean deletedExists ();

	String descriptionFieldName ();
	String descriptionLabel ();
	Boolean descriptionExists ();

	String nameFieldName ();
	String nameLabel ();
	Boolean nameExists ();
	Boolean nameIsCode ();

	boolean common ();
	boolean ephemeral ();
	boolean event ();
	boolean major ();
	boolean minor ();
	boolean type ();

}
