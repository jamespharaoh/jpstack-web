package wbs.framework.object;

import static wbs.utils.etc.Misc.successOrThrowRuntimeException;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.dynamicCast;

import javax.annotation.Nonnull;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.codegen.DoNotDelegate;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;

import fj.data.Either;

public
interface ObjectHelperPropertyMethods <
	RecordType extends Record <RecordType>
> {

	@DoNotDelegate
	ObjectHelper <RecordType> objectHelper ();

	GlobalId getGlobalId (
			RecordType object);

	default
	GlobalId getGlobalIdGeneric (
			Record <?> object) {

		return getGlobalId (
			dynamicCast (
				objectHelper ().objectClass (),
				object));

	}

	String getName (
			RecordType object);

	default
	String getNameGeneric (
			Record <?> object) {

		return getName (
			dynamicCast (
				objectHelper ().objectClass (),
				object));

	}

	String getTypeCode (
			RecordType object);

	default
	String getTypeCodeGeneric (
			Record <?> object) {

		return getTypeCode (
			dynamicCast (
				objectHelper ().objectClass (),
				object));

	}

	String getCode (
			RecordType object);

	default
	String getCodeGeneric (
			Record <?> object) {

		return getCode (
			dynamicCast (
				objectHelper ().objectClass (),
				object));

	}

	String getDescription (
			RecordType object);

	default
	String getDescriptionGeneric (
			Record <?> object) {

		return getDescription (
			dynamicCast (
				objectHelper ().objectClass (),
				object));

	}

	Record <?> getParentType (
			RecordType object);

	default
	Record <?> getParentTypeGeneric (
			Record <?> object) {

		return getParentType (
			dynamicCast (
				objectHelper ().objectClass (),
				object));

	}

	Long getParentTypeId (
			RecordType object);

	default
	Long getParentTypeIdGeneric (
			Record <?> object) {

		return getParentTypeIdGeneric (
			dynamicCast (
				objectHelper ().objectClass (),
				object));

	}

	Long getParentId (
			RecordType object);

	default
	Long getParentIdGeneric (
			Record <?> object) {

		return getParentId (
			dynamicCast (
				objectHelper ().objectClass (),
				object));

	}

	GlobalId getParentGlobalId (
			RecordType object);

	default
	GlobalId getParentGlobalIdGeneric (
			Record <?> object) {

		return getParentGlobalId (
			dynamicCast (
				objectHelper ().objectClass (),
				object));

	}

	Either <Optional <Record <?>>, String> getParentOrError (
			RecordType object);

	default
	Either <Optional <Record <?>>, String> getParentOrErrorGeneric (
			@NonNull Record <?> object) {

		return getParentOrError (
			dynamicCast (
				objectHelper ().objectClass (),
				object));

	}

	default
	Optional <Record <?>> getParent (
			RecordType object) {

		return successOrThrowRuntimeException (
			getParentOrError (
				object));

	}

	default
	Optional <Record <?>> getParentGeneric (
			@NonNull Record <?> object) {

		return successOrThrowRuntimeException (
			getParentOrError (
				dynamicCast (
					objectHelper ().objectClass (),
					object)));

	}

	default
	Record <?> getParentRequired (
			RecordType object) {

		return optionalGetRequired (
			successOrThrowRuntimeException (
				getParentOrError (
					object)));

	}

	default
	Record <?> getParentRequiredGeneric (
			Record <?> object) {

		return optionalGetRequired (
			successOrThrowRuntimeException (
				getParentOrError (
					dynamicCast (
						objectHelper ().objectClass (),
						object))));

	}

	@Deprecated
	default
	Record <?> getParentOrNull (
			RecordType object) {

		return optionalOrNull (
			successOrThrowRuntimeException (
				getParentOrError (
					object)));

	}

	@Deprecated
	default
	Record <?> getParentOrNullGeneric (
			Record <?> object) {

		return getParentOrNull (
			dynamicCast (
				objectHelper ().objectClass (),
				object));

	}

	Either <Boolean, String> getDeletedOrError (
			RecordType object,
			Boolean checkParents);

	default
	Boolean getDeleted (
			@Nonnull RecordType object,
			@NonNull Boolean checkParents) {

		return successOrThrowRuntimeException (
			getDeletedOrError (
				object,
				checkParents));

	}

	default
	Boolean getDeletedGeneric (
			Record <?> object,
			boolean checkParents) {

		return successOrThrowRuntimeException (
			getDeletedOrError (
				dynamicCast (
					objectHelper ().objectClass (),
					object),
				checkParents));

	}

	// hooks

	void setParent (
			RecordType object,
			Record <?> parent);

	Object getDynamic (
			RecordType object,
			String name);

	default
	Object getDynamicGeneric (
			Record <?> object,
			String name) {

		return getDynamic (
			dynamicCast (
				objectHelper ().objectClass (),
				object),
			name);

	}

	void setDynamic (
			RecordType object,
			String name,
			Optional <?> value);

	default
	void setDynamicGeneric (
			Record <?> object,
			String name,
			Optional <?> value) {

		setDynamic (
			dynamicCast (
				objectHelper ().objectClass (),
				object),
			name,
			value);

	}

}
