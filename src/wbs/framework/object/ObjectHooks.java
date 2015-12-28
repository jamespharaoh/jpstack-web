package wbs.framework.object;

import wbs.framework.record.Record;

public
interface ObjectHooks<RecordType extends Record<RecordType>> {

	void createSingletons (
			ObjectHelper<RecordType> objectHelper,
			ObjectHelper<?> parentHelper,
			Record<?> parentObject);

	void beforeInsert (
			RecordType object);

	void afterInsert (
			RecordType object);

	Object getDynamic (
			RecordType object,
			String name);

	void setDynamic (
			RecordType object,
			String name,
			Object value);

	void beforeUpdate (
			RecordType object);

}
