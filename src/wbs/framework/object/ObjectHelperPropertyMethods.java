package wbs.framework.object;

import static wbs.utils.etc.Misc.successOrThrowRuntimeException;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.dynamicCast;

import javax.annotation.Nonnull;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.codegen.DoNotDelegate;
import wbs.framework.database.Transaction;
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
			Transaction parentTransaction,
			RecordType object);

	GlobalId getParentGlobalId (
			Transaction parentTransaction,
			RecordType object);

	Either <Optional <Record <?>>, String> getParentOrError (
			Transaction parentTransaction,
			RecordType object);

	default
	Optional <Record <?>> getParent (
			Transaction parentTransaction,
			RecordType object) {

		return successOrThrowRuntimeException (
			getParentOrError (
				parentTransaction,
				object));

	}

	default
	Record <?> getParentRequired (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

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
			@NonNull RecordType object) {

		return optionalOrNull (
			successOrThrowRuntimeException (
				getParentOrError (
					parentTransaction,
					object)));

	}

	Either <Boolean, String> getDeletedOrError (
			Transaction parentTransaction,
			RecordType object,
			Boolean checkParents);

	default
	Boolean getDeleted (
			@NonNull Transaction parentTransaction,
			@Nonnull RecordType object,
			@NonNull Boolean checkParents) {

		return successOrThrowRuntimeException (
			getDeletedOrError (
				parentTransaction,
				object,
				checkParents));

	}

	// hooks

	void setParent (
			RecordType object,
			Record <?> parent);

	Object getDynamic (
			Transaction parentTransaction,
			RecordType object,
			String name);

	void setDynamic (
			Transaction parentTransaction,
			RecordType object,
			String name,
			Optional <?> value);

}
