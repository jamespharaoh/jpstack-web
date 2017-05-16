package wbs.framework.object;

import com.google.common.base.Optional;

import wbs.framework.codegen.DoNotDelegate;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.record.Record;

public
interface ObjectHelperModelMethods <
	RecordType extends Record <RecordType>
> {

	@DoNotDelegate
	ObjectModel <RecordType> objectModel ();

	Class <RecordType> objectClass ();
	String objectName ();
	String objectTypeCode ();
	Long objectTypeId ();

	String friendlyName ();
	String friendlyNamePlural ();

	String shortName ();
	String shortNamePlural ();

	ModelField field (
			String name);

	Optional <Class <? extends Record <?>>> parentClass ();
	Class <? extends Record <?>> parentClassRequired ();
	ModelField parentField ();
	String parentFieldName ();
	String parentLabel ();
	Boolean parentExists ();

	ModelField typeCodeField ();
	String typeCodeFieldName ();
	String typeCodeLabel ();
	Boolean typeCodeExists ();

	ModelField codeField ();
	String codeFieldName ();
	String codeLabel ();
	Boolean codeExists ();

	ModelField indexField ();
	String indexFieldName ();
	String indexLabel ();
	Boolean indexExists ();
	String indexCounterFieldName ();

	ModelField deletedField ();
	String deletedFieldName ();
	String deletedLabel ();
	Boolean deletedExists ();

	ModelField descriptionField ();
	String descriptionFieldName ();
	String descriptionLabel ();
	Boolean descriptionExists ();

	ModelField nameField ();
	String nameFieldName ();
	String nameLabel ();
	Boolean nameExists ();
	Boolean nameIsCode ();

	ModelField timestampField ();
	String timestampFieldName ();

	boolean common ();
	boolean ephemeral ();
	boolean event ();
	boolean major ();
	boolean minor ();
	boolean type ();

	boolean isRoot ();
	boolean isRooted ();
	boolean canGetParent ();
	boolean parentTypeIsFixed ();

}
