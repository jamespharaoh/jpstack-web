package wbs.framework.object;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.Misc.successOrElse;
import static wbs.utils.etc.Misc.successOrThrowRuntimeException;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.OptionalUtils.optionalOrThrow;
import static wbs.utils.etc.ResultUtils.mapSuccess;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.EphemeralRecord;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

import wbs.utils.data.Pair;

import fj.data.Either;

public
interface ObjectManagerMethods {

	// navigation

	Either <Optional <Record <?>>, String> getParentOrError (
			Transaction parentTransaction,
			Record <?> object);

	default
	Optional <Record <?>> getParent (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		return successOrThrowRuntimeException (
			getParentOrError (
				parentTransaction,
				object));

	}

	default
	Either <Record <?>, String> getParentRequiredOrError (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		return mapSuccess (
			getParentOrError (
				parentTransaction,
				object),
			optional ->
				optionalGetRequired (
					optional));

	}

	default
	Record <?> getParentRequired (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		return optionalGetRequired (
			successOrThrowRuntimeException (
				getParentOrError (
					parentTransaction,
					object)));

	}

	@Deprecated
	default
	Record <?> getParentOrNull (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		return optionalOrNull (
			successOrThrowRuntimeException (
				getParentOrError (
					parentTransaction,
					object)));

	}

	GlobalId getGlobalId (
			Transaction parentTransaction,
			Record <?> object);

	GlobalId getParentGlobalId (
			Transaction parentTransaction,
			Record <?> object);

	public abstract <ObjectType extends Record <ObjectType>>
	List <ObjectType> getChildren (
			Transaction parentTransaction,
			Record <?> object,
			Class <ObjectType> childClass);

	Long getObjectTypeId (
			Transaction parentTransaction,
			Record <?> object);

	String getCode (
			Transaction parentTransaction,
			Record <?> object);

	List<Record<?>> getMinorChildren (
			Transaction parentTransaction,
			Record <?> object);

	String getObjectTypeCode (
			Transaction parentTransaction,
			Record <?> object);

	<ObjectType extends Record <ObjectType>>
	Optional <ObjectType> getAncestor (
			Transaction parentTransaction,
			Class <ObjectType> ancestorClass,
			Record <?> object);

	default <ObjectType extends Record <ObjectType>>
	ObjectType getAncestorRequired (
			@NonNull Transaction parentTransaction,
			@NonNull Class <ObjectType> ancestorClass,
			@NonNull Record <?> object) {

		return optionalGetRequired (
			getAncestor (
				parentTransaction,
				ancestorClass,
				object));

	}

	// data access

	Optional <Record <?>> findObject (
			Transaction parentTransaction,
			GlobalId objectGlobalId);

	default
	Record <?> findObjectRequired (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId objectGlobalId) {

		return optionalOrThrow (
			findObject (
				parentTransaction,
				objectGlobalId),
			() -> new NoSuchElementException (
				stringFormat (
					"No such object with type %s and id %s",
					integerToDecimalString (
						objectGlobalId.typeId ()),
					integerToDecimalString (
						objectGlobalId.objectId ()))));

	}

	<ObjectType extends Record <?>>
	ObjectType update (
			Transaction parentTransaction,
			ObjectType object);

	<ObjectType extends EphemeralRecord <?>>
	ObjectType remove (
			Transaction parentTransaction,
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

	boolean isAncestor (
			Transaction parentTransaction,
			Record <?> object,
			Record <?> parent);

	<ObjectType extends Record <?>>
	Optional <ObjectType> firstAncestor (
			Transaction parentTransaction,
			Record <?> object,
			Set <ObjectType> parents);

	List <Pair <Record <?>, String>> verifyData (
			Transaction parentTransaction,
			Record <?> object,
			Boolean recurse);

	// object paths

	String objectPath (
			Transaction parentTransaction,
			Record <?> dataObject,
			Optional <Record <?>> assumedRoot,
			boolean mini,
			boolean preload);

	default
	String objectPath (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> dataObject) {

		return objectPath (
			parentTransaction,
			dataObject,
			optionalAbsent (),
			false,
			false);

	}

	default
	String objectPathMini (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> dataObject) {

		return objectPath (
			parentTransaction,
			dataObject,
			optionalAbsent (),
			true,
			false);

	}

	default
	String objectPathMini (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object,
			@NonNull Optional <Record <?>> root) {

		return objectPath (
			parentTransaction,
			object,
			root,
			true,
			false);

	}

	default
	String objectPathMini (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object,
			@NonNull Record <?> root) {

		return objectPath (
			parentTransaction,
			object,
			optionalOf (
				root),
			true,
			false);

	}

	default
	String objectPathMiniPreload (
			@NonNull Transaction parentTransaction,
			@NonNull Record<?> object,
			@NonNull Optional<Record<?>> root) {

		return objectPath (
			parentTransaction,
			object,
			root,
			true,
			true);

	}

	default
	String objectPathMiniPreload (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object,
			@NonNull Record <?> root) {

		return objectPath (
			parentTransaction,
			object,
			optionalOf (
				root),
			true,
			true);

	}

	default
	String objectPath (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> dataObject,
			@NonNull Optional <Record <?>> root) {

		return objectPath (
			parentTransaction,
			dataObject,
			root,
			false,
			false);

	}

	default
	String objectPath (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> dataObject,
			@NonNull Record <?> root) {

		return objectPath (
			parentTransaction,
			dataObject,
			optionalOf (
				root),
			false,
			false);

	}

	// object path utilities

	String objectIdString (
			Record <?> dataObject);

	<ObjectType extends Record <?>>
	SortedMap <String, ObjectType> pathMap (
			Transaction parentTransaction,
			Collection <ObjectType> objects,
			Record <?> root,
			boolean mini);

	// structured dereferncing

	Either <Optional <Object>, String> dereferenceOrError (
			Transaction parentTransaction,
			Object object,
			String path,
			Map <String, Object> hints);

	default
	Either <Optional <Object>, String> dereferenceOrError (
			@NonNull Transaction parentTransaction,
			@NonNull Object object,
			@NonNull String path) {

		return dereferenceOrError (
			parentTransaction,
			object,
			path,
			emptyMap ());

	}

	default
	Optional <Object> dereference (
			@NonNull Transaction parentTransaction,
			@NonNull Object object,
			@NonNull String path,
			@NonNull Map <String, Object> hints) {

		return successOrThrowRuntimeException (
			dereferenceOrError (
				parentTransaction,
				object,
				path,
				hints));

	}

	default
	Optional <Object> dereference (
			@NonNull Transaction parentTransaction,
			@NonNull Object object,
			@NonNull String path) {

		return successOrThrowRuntimeException (
			dereferenceOrError (
				parentTransaction,
				object,
				path,
				emptyMap ()));

	}

	default
	Object dereferenceRequired (
			@NonNull Transaction parentTransaction,
			@NonNull Object object,
			@NonNull String path,
			@NonNull Map <String, Object> hints) {

		return optionalGetRequired (
			successOrThrowRuntimeException (
				dereferenceOrError (
					parentTransaction,
					object,
					path,
					hints)));

	}

	default
	Object dereferenceRequired (
			@NonNull Transaction parentTransaction,
			@NonNull Object object,
			@NonNull String path) {

		return optionalGetRequired (
			successOrThrowRuntimeException (
				dereferenceOrError (
					parentTransaction,
					object,
					path,
					emptyMap ())));

	}

	@Deprecated
	default
	Object dereferenceObsolete (
			@NonNull Transaction parentTransaction,
			@NonNull Object object,
			@NonNull String path,
			@NonNull Map <String, Object> hints) {

		return optionalOrNull (
			successOrElse (
				dereferenceOrError (
					parentTransaction,
					object,
					path,
					hints),
				error ->
					optionalAbsent ()));

	}

	@Deprecated
	default
	Object dereferenceObsolete (
			@NonNull Transaction parentTransaction,
			@NonNull Object object,
			@NonNull String path) {

		return optionalOrNull (
			successOrElse (
				dereferenceOrError (
					parentTransaction,
					object,
					path,
					emptyMap ()),
				error ->
					optionalAbsent ()));

	}

	Optional <Class <?>> dereferenceType (
			TaskLogger taskLogger,
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
