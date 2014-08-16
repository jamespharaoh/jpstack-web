package wbs.framework.object;

import static wbs.framework.utils.etc.Misc.stringFormat;

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

		// do nothing

	}

	@Override
	public
	List<Integer> searchIds (
			Object search) {

		throw new UnsupportedOperationException (
			stringFormat (
				"%s.searchIds (...)",
				getClass ().getSimpleName ()));

	}

	@Override
	public
	void beforeInsert (
			RecordType object) {

		// do nothing

	}

	@Override
	public
	void afterInsert (
			RecordType object) {

		// do nothing

	}

}
