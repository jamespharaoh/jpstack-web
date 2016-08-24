package wbs.framework.object;

import com.google.common.base.Optional;

import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;

public
interface ObjectHelperPropertyMethods<RecordType extends Record<RecordType>> {

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

	Long getParentTypeId (
			Record<?> object);

	Long getParentId (
			Record<?> object);

	GlobalId getParentGlobalId (
			Record<?> object);

	Record<?> getParent (
			Record<?> object);

	Boolean getDeleted (
			Record<?> object,
			boolean checkParents);

	// hooks

	void setParent (
			Record <?> object,
			Record <?> parent);

	Object getDynamic (
			Record <?> object,
			String name);

	void setDynamic (
			Record <?> object,
			String name,
			Optional <?> value);

}
