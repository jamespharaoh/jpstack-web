package wbs.framework.object;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.Misc.successOrElse;
import static wbs.utils.etc.Misc.successOrThrowRuntimeException;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.ResultUtils.mapSuccess;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.entity.record.EphemeralRecord;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

import fj.data.Either;

public
interface ObjectManagerMethods {

	// navigation

	Either <Optional <Record <?>>, String> getParentOrError (
			Record <?> object);

	default
	Optional <Record <?>> getParent (
			@NonNull Record <?> object) {

		return successOrThrowRuntimeException (
			getParentOrError (
				object));

	}

	default
	Either <Record <?>, String> getParentRequiredOrError (
			@NonNull Record <?> object) {

		return mapSuccess (
			getParentOrError (
				object),
			optional ->
				optionalGetRequired (
					optional));

	}

	default
	Record <?> getParentRequired (
			@NonNull Record <?> object) {

		return optionalGetRequired (
			successOrThrowRuntimeException (
				getParentOrError (
					object)));

	}

	@Deprecated
	default
	Record <?> getParentOrNull (
			@NonNull Record <?> object) {

		return optionalOrNull (
			successOrThrowRuntimeException (
				getParentOrError (
					object)));

	}

	GlobalId getGlobalId (
			Record <?> object);

	GlobalId getParentGlobalId (
			Record <?> object);

	public abstract <ObjectType extends Record <ObjectType>>
	List <ObjectType> getChildren (
			Record <?> object,
			Class <ObjectType> childClass);

	Long getObjectTypeId (
			Record <?> object);

	String getCode (
			Record <?> object);

	List<Record<?>> getMinorChildren (
			Record <?> object);

	String getObjectTypeCode (
			Record <?> object);

	public
	abstract <ObjectType extends Record <ObjectType>>
	Optional <ObjectType> getAncestor (
			Class <ObjectType> ancestorClass,
			Record <?> object);

	// data access

	Record <?> findObject (
			GlobalId objectGlobalId);

	<ObjectType extends Record <?>>
	ObjectType update (
			TaskLogger parentTaskLogger,
			ObjectType object);

	<ObjectType extends EphemeralRecord <?>>
	ObjectType remove (
			ObjectType object);

	// utilities

	Optional <Class <?>> objectClassForTypeCode (
			String typeCode);

	Class <?> objectClassForTypeCodeRequired (
			String typeCode);

	ObjectHelper <?> objectHelperForTypeCodeRequired (
			String typeCode);

	Optional <ObjectHelper <?>> objectHelperForTypeId (
			Long typeId);

	ObjectHelper <?> objectHelperForTypeIdRequired (
			Long typeId);

	@Deprecated
	ObjectHelper <?> objectHelperForTypeIdOrNull (
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
			@NonNull Record <?> dataObject) {

		return objectPath (
			dataObject,
			optionalAbsent (),
			false,
			false);

	}

	default
	String objectPathMini (
			@NonNull Record <?> dataObject) {

		return objectPath (
			dataObject,
			optionalAbsent (),
			true,
			false);

	}

	default
	String objectPathMini (
			@NonNull Record <?> object,
			@NonNull Optional <Record <?>> root) {

		return objectPath (
			object,
			root,
			true,
			false);

	}

	default
	String objectPathMini (
			@NonNull Record <?> object,
			@NonNull Record <?> root) {

		return objectPath (
			object,
			optionalOf (
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
	SortedMap <String, ObjectType> pathMap (
			Collection <ObjectType> objects,
			Record <?> root,
			boolean mini);

	// structured dereferncing

	Either <Optional <Object>, String> dereferenceOrError (
			Object object,
			String path,
			Map <String, Object> hints);

	default
	Either <Optional <Object>, String> dereferenceOrError (
			@NonNull Object object,
			@NonNull String path) {

		return dereferenceOrError (
			object,
			path,
			emptyMap ());

	}

	default
	Optional <Object> dereference (
			@NonNull Object object,
			@NonNull String path,
			@NonNull Map <String, Object> hints) {

		return successOrThrowRuntimeException (
			dereferenceOrError (
				object,
				path,
				hints));

	}

	default
	Optional <Object> dereference (
			@NonNull Object object,
			@NonNull String path) {

		return successOrThrowRuntimeException (
			dereferenceOrError (
				object,
				path,
				emptyMap ()));

	}

	default
	Object dereferenceRequired (
			@NonNull Object object,
			@NonNull String path,
			@NonNull Map <String, Object> hints) {

		return optionalGetRequired (
			successOrThrowRuntimeException (
				dereferenceOrError (
					object,
					path,
					hints)));

	}

	default
	Object dereferenceRequired (
			@NonNull Object object,
			@NonNull String path) {

		return optionalGetRequired (
			successOrThrowRuntimeException (
				dereferenceOrError (
					object,
					path,
					emptyMap ())));

	}

	@Deprecated
	default
	Object dereferenceObsolete (
			@NonNull Object object,
			@NonNull String path,
			@NonNull Map <String, Object> hints) {

		return optionalOrNull (
			successOrElse (
				dereferenceOrError (
					object,
					path,
					hints),
				error ->
					optionalAbsent ()));

	}

	@Deprecated
	default
	Object dereferenceObsolete (
			@NonNull Object object,
			@NonNull String path) {

		return optionalOrNull (
			successOrElse (
				dereferenceOrError (
					object,
					path,
					emptyMap ()),
				error ->
					optionalAbsent ()));

	}

	Optional <Class <?>> dereferenceType (
			Optional <Class <?>> objectClass,
			Optional <String> path);

	// object helpers

	List <ObjectHelper <?>> objectHelpers ();

	ObjectHelper <?> objectHelperForObjectNameRequired (
			String objectName);

	ObjectHelper <?> objectHelperForObjectRequired (
			Record <?> object);

	ObjectHelper <?> objectHelperForClassRequired (
			Class <?> objectClass);

}
