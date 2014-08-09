package wbs.framework.object;

import java.util.List;

import wbs.framework.record.Record;

public
class AbstractObjectHooks<RecordType extends Record<RecordType>>
	implements ObjectHooks<RecordType> {

	@Override
	public
	void createSingletons (
			ObjectHelper<RecordType> objectHelper,
			ObjectHelper<?> parentHelper,
			Record<?> parentObject) {

	}

	@Override
	public
	List<Integer> searchIds (
			Object search) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	void beforeInsert (
			RecordType object) {

	}

	@Override
	public
	void afterInsert (
			RecordType object) {

	}

}
