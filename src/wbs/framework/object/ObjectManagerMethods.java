package wbs.framework.object;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;

import com.google.common.base.Optional;

public
interface ObjectManagerMethods {

	// navigation

	Record<?> getParent (
			Record<?> dataObject);

	GlobalId getGlobalId (
			Record<?> object);

	GlobalId getParentGlobalId (
			Record<?> object);

	public abstract <ObjectType extends Record<ObjectType>>
	List<ObjectType> getChildren (
			Record<?> object,
			Class<ObjectType> childClass);

	int getObjectTypeId (
			Record<?> object);

	String getCode (
			Record<?> object);

	List<Record<?>> getMinorChildren (
			Record<?> object);

	String getObjectTypeCode (
			Record<?> object);

	// data access

	Record<?> findObject (
			GlobalId objectGlobalId);

	public <ObjectType extends Record<?>>
	ObjectType findChildByCode (
			Class<ObjectType> objectClass,
			GlobalId parentGlobalId,
			String code);

	public <ObjectType extends Record<?>>
	ObjectType findChildByCode (
			Class<ObjectType> objectClass,
			Record<?> parent,
			String code);

	<ObjectType extends Record<?>>
	ObjectType insert (
			ObjectType object);

	<ObjectType extends EphemeralRecord<?>>
	ObjectType remove (
			ObjectType object);

	// utilities

	Class<?> objectTypeCodeToClass (
			String typeCode);

	ObjectHelper<?> objectHelperForTypeCode (
			String typeCode);

	ObjectHelper<?> objectHelperForTypeId (
			Integer typeId);

	int objectClassToTypeId (
			Class<?> objectClass);

	boolean isParent (
			Record<?> object,
			Record<?> parent);

	<ObjectType extends Record<?>>
	ObjectType firstParent (
			Record<?> object,
			Set<ObjectType> parents);

	// object paths

	String objectPath (
			Record<?> dataObject);

	String objectPathMini (
			Record<?> dataObject);

	String objectPath (
			Record<?> dataObject,
			Record<?> root);

	String objectPath (
			Record<?> dataObject,
			Record<?> root,
			boolean mini);

	String objectPath (
			Record<?> dataObject,
			Record<?> assumedRoot,
			boolean mini,
			boolean preload);

	String objectIdString (
			Record<?> dataObject);

	<ObjectType extends Record<?>>
	SortedMap<String,ObjectType> pathMap (
			Collection<ObjectType> objects,
			Record<?> root,
			boolean mini);

	Object dereference (
			Object object,
			String path);

	Optional<Class<?>> dereferenceType (
			Optional<Class<?>> objectClass,
			Optional<String> path);

	// object helpers

	List<ObjectHelper<?>> objectHelpers ();

	ObjectHelper<?> objectHelperForObjectName (
			String objectName);

	ObjectHelper<?> objectHelperForObject (
			Record<?> object);

	ObjectHelper<?> objectHelperForClass (
			Class<?> objectClass);

}
