package wbs.framework.object;

import java.util.List;

import wbs.framework.entity.model.Model;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;

public
interface ObjectHelperProvider {

	// getters

	Model model ();

	Class<? extends Record<?>> objectClass ();
	String objectName ();
	String objectTypeCode ();

	Class<? extends ObjectHelper<?>> helperClass ();

	Class<?> parentClass ();
	String parentFieldName ();
	String parentLabel ();
	Boolean parentExists ();

	String codeFieldName ();
	String codeLabel ();
	Boolean codeExists ();

	String typeCodeFieldName ();
	String typeCodeLabel ();
	Boolean typeCodeExists ();

	String indexFieldName ();
	String indexLabel ();
	Boolean indexExists ();
	String indexCounterFieldName ();

	String nameFieldName ();
	String nameLabel ();
	Boolean nameExists ();
	Boolean nameIsCode ();

	String deletedFieldName ();
	String deletedLabel ();
	Boolean deletedExists ();

	String descriptionFieldName ();
	String descriptionLabel ();
	Boolean descriptionExists ();

	boolean root ();
	boolean rooted ();
	boolean canGetParent ();
	boolean parentTypeIsFixed ();

	ObjectHelperProvider parentObjectHelperProvider ();

	// database

	Record<?> find (
			long id);

	Record<?> findByParentAndCode (
			GlobalId parentGlobalId,
			String code);

	Record<?> findByParentAndIndex (
			GlobalId parentGlobalId,
			Integer index);

	Record<?> findByParentAndTypeAndCode (
			GlobalId parentGlobalId,
			String typeCode,
			String code);

	List<Record<?>> findAll ();

	List<Record<?>> findAllByParent (
			GlobalId parentGlobalId);

	List<Record<?>> findAllByParentAndType (
			GlobalId parentGlobalId,
			String typeCode);

	<RecordType extends Record<?>>
	RecordType insert (
			RecordType object);

	List<Integer> searchIds (
			Object search);

	<RecordType extends EphemeralRecord<?>>
	RecordType remove (
			RecordType object);

	<RecordType extends Record<?>>
	RecordType lock (
			RecordType object);

	// object getters

	String getName (
			Record<?> object);

	String getTypeCode (
			Record<?> object);

	String getCode (
			Record<?> object);

	String getDescription (
			Record<?> object);

	Record<?> getParentType (
			Record<?> object);

	Integer getParentId (
			Record<?> object);

	Record<?> getParent (
			Record<?> object);

	boolean getDeleted (
			Record<?> object);
	
	boolean getDynamic (
			Record<?> object,
			String name);
	
	void setDynamic (
			Record<?> object,
			String name,
			Object value);

	// hooks

	void createSingletons (
			ObjectHelper<?> objectHelper,
			ObjectHelper<?> parentHelper,
			Record<?> parentObject);

	void setParent (
			Record<?> object,
			Record<?> parent);

}
