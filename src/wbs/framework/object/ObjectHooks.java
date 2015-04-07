package wbs.framework.object;

import java.util.List;

import wbs.framework.record.Record;

public
interface ObjectHooks<RecordType extends Record<RecordType>> {

	void createSingletons (
			ObjectHelper<RecordType> objectHelper,
			ObjectHelper<?> parentHelper,
			Record<?> parentObject);

	List<Integer> searchIds (
			Object search);

	void beforeInsert (
			RecordType object);

	void afterInsert (
			RecordType object);
	
	boolean getDynamic (
			Record<?> object,
			String name);
	
	void setDynamic (
			Record<?> object,
			String name,
			Object value);

}
