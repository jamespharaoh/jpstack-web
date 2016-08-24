package wbs.framework.object;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;
import wbs.framework.entity.record.EphemeralRecord;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;

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

	Long getObjectTypeId (
			Record<?> object);

	String getCode (
			Record<?> object);

	List<Record<?>> getMinorChildren (
			Record<?> object);

	String getObjectTypeCode (
			Record<?> object);

	public
	abstract <ObjectType extends Record<ObjectType>>
	Optional<ObjectType> getAncestor (
			Class<ObjectType> ancestorClass,
			Record<?> object);

	// data access

	Record<?> findObject (
			GlobalId objectGlobalId);

	<ObjectType extends Record<?>>
	ObjectType update (
			ObjectType object);

	<ObjectType extends EphemeralRecord<?>>
	ObjectType remove (
			ObjectType object);

	// utilities

	Class <?> objectTypeCodeToClass (
			String typeCode);

	ObjectHelper <?> objectHelperForTypeCode (
			String typeCode);

	ObjectHelper <?> objectHelperForTypeId (
			Long typeId);

	Long objectClassToTypeId (
			Class <?> objectClass);

	boolean isParent (
			Record <?> object,
			Record <?> parent);

	<ObjectType extends Record <?>>
	ObjectType firstParent (
			Record <?> object,
			Set <ObjectType> parents);

	// object paths

	String objectPath (
			Record <?> dataObject,
			Optional <Record <?>> assumedRoot,
			boolean mini,
			boolean preload);

	default
	String objectPath (
			@NonNull Record<?> dataObject) {

		return objectPath (
			dataObject,
			Optional.<Record<?>>absent (),
			false,
			false);

	}

	default
	String objectPathMini (
			@NonNull Record<?> dataObject) {

		return objectPath (
			dataObject,
			Optional.<Record<?>>absent (),
			true,
			false);

	}

	default
	String objectPathMini (
			@NonNull Record<?> object,
			@NonNull Optional<Record<?>> root) {

		return objectPath (
			object,
			root,
			true,
			false);

	}

	default
	String objectPathMini (
			@NonNull Record<?> object,
			@NonNull Record<?> root) {

		return objectPath (
			object,
			Optional.<Record<?>>of (
				root),
			true,
			false);

	}

	default
	String objectPathMiniPreload (
			@NonNull Record<?> object,
			@NonNull Optional<Record<?>> root) {

		return objectPath (
			object,
			root,
			true,
			true);

	}

	default
	String objectPathMiniPreload (
			@NonNull Record<?> object,
			@NonNull Record<?> root) {

		return objectPath (
			object,
			Optional.<Record<?>>of (
				root),
			true,
			true);

	}

	default
	String objectPath (
			@NonNull Record<?> dataObject,
			@NonNull Optional<Record<?>> root) {

		return objectPath (
			dataObject,
			root,
			false,
			false);

	}

	default
	String objectPath (
			@NonNull Record<?> dataObject,
			@NonNull Record<?> root) {

		return objectPath (
			dataObject,
			Optional.<Record<?>>of (
				root),
			false,
			false);

	}

	// object path utilities

	String objectIdString (
			Record<?> dataObject);

	<ObjectType extends Record<?>>
	SortedMap<String,ObjectType> pathMap (
			Collection<ObjectType> objects,
			Record<?> root,
			boolean mini);

	// structured dereferncing

	Object dereference (
			Object object,
			String path,
			Map<String,Object> hints);

	default
	Object dereference (
			Object object,
			String path) {

		return dereference (
			object,
			path,
			ImmutableMap.of ());

	}

	Optional<Class<?>> dereferenceType (
			Optional<Class<?>> objectClass,
			Optional<String> path);

	// object helpers

	List<ObjectHelper<?>> objectHelpers ();

	ObjectHelper<?> objectHelperForObjectName (
			String objectName);

	ObjectHelper<?> objectHelperForObject (
			Record<?> object);

	ObjectHelper<?> objectHelperForClassRequired (
			Class<?> objectClass);

}
