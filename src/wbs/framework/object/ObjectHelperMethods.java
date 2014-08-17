package wbs.framework.object;

import java.util.List;

import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;

public
interface ObjectHelperMethods<RecordType extends Record<RecordType>> {

	// getters

	ObjectHelperProvider objectHelperProvider ();

	Class<RecordType> objectClass ();
	String objectName ();
	String objectTypeCode ();
	Integer objectTypeId ();

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

	boolean major ();
	boolean minor ();
	boolean ephemeral ();
	boolean common ();

	boolean canGetParent ();
	boolean parentTypeIsFixed ();
	boolean root ();
	boolean rooted ();

	// data access

	RecordType find (
			int id);

	RecordType findByCode (
			Record<?> parent,
			String... code);

	RecordType findByCode (
			GlobalId parentGlobalId,
			String... code);

	RecordType findByTypeAndCode (
			Record<?> parent,
			String typeCode,
			String... code);

	RecordType findByTypeAndCode (
			GlobalId parentGlobalId,
			String typeCode,
			String... code);

	List<RecordType> findAll ();

	List<RecordType> findByParent (
			Record<?> parent);

	List<RecordType> findByParent (
			GlobalId parentGlobalId);

	List<RecordType> findByParentAndType (
			Record<?> parent,
			String typeCode);

	List<RecordType> findByParentAndType (
			GlobalId parentGlobalId,
			String typeCode);

	List<RecordType> search (
			Object search);

	List<Integer> searchIds (
			Object search);

	<RecordTypeAgain extends Record<?>>
	RecordTypeAgain insert (
			RecordTypeAgain object);

	<RecordTypeAgain extends EphemeralRecord<?>>
	RecordTypeAgain remove (
			RecordTypeAgain object);

	<RecordTypeAgain extends Record<?>>
	RecordTypeAgain lock (
			RecordTypeAgain object);

	// object access

	RecordType createInstance ();

	GlobalId getGlobalId (
			Record<?> object);

	String getName (
			Record<?> object);

	String getTypeCode (
			Record<?> object);

	String getCode (
			Record<?> object);

	String getDescription (
			Record<?> object);

	Record<?> getParentObjectType (
			Record<?> object);

	Integer getParentTypeId (
			Record<?> object);

	Integer getParentId (
			Record<?> object);

	GlobalId getParentGlobalId (
			Record<?> object);

	Record<?> getParent (
			Record<?> object);

	List<Record<?>> getChildren (
			Record<?> object);

	<ChildType extends Record<?>>
	List<ChildType> getChildren (
			Record<?> object,
			Class<ChildType> childClass);

	List<Record<?>> getMinorChildren (
			Record<?> object);

	boolean getDeleted (
			Record<?> object,
			boolean checkParents);

	// hooks

	void setParent (
			Record<?> object,
			Record<?> parent);

}
