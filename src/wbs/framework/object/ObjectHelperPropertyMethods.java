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

	String getCode (
			RecordType object);

	String getDescription (
			RecordType object);

	Record <?> getParentType (
			RecordType object);

	Long getParentTypeId (
			RecordType object);

	Long getParentId (
			RecordType object);

	GlobalId getParentGlobalId (
			RecordType object);

	Either <Optional <Record <?>>, String> getParentOrError (
			RecordType object);

	default
	Optional <Record <?>> getParent (
			RecordType object) {

		return successOrThrowRuntimeException (
			getParentOrError (
				object));

	}

	default
	Record <?> getParentRequired (
			RecordType object) {

		return optionalGetRequired (
			successOrThrowRuntimeException (
				getParentOrError (
					object)));

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

	// hooks

	void setParent (
			RecordType object,
			Record <?> parent);

	Object getDynamic (
			RecordType object,
			String name);


	void setDynamic (
			RecordType object,
			String name,
			Optional <?> value);

}
