package wbs.framework.object;

import static wbs.utils.etc.TypeUtils.dynamicCast;

import com.google.common.base.Optional;

import wbs.framework.codegen.DoNotDelegate;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;

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

	Record <?> getParentType (
			RecordType object);

	default
	Record <?> getParentTypeGeneric (
			Record <?> object) {

		return getParentTypeGeneric (
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

	Record <?> getParent (
			RecordType object);

	default
	Record <?> getParentGeneric (
			Record <?> object) {

		return getParent (
			dynamicCast (
				objectHelper ().objectClass (),
				object));

	}

	Boolean getDeleted (
			RecordType object,
			boolean checkParents);

	default
	Boolean getDeletedGeneric (
			Record <?> object,
			boolean checkParents) {

		return getDeleted (
			dynamicCast (
				objectHelper ().objectClass (),
				object),
			checkParents);

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
