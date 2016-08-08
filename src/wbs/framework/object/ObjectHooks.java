package wbs.framework.object;

import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import wbs.framework.record.Record;

public
interface ObjectHooks<RecordType extends Record<RecordType>> {

	default
	void createSingletons (
			ObjectHelper<RecordType> objectHelper,
			ObjectHelper<?> parentHelper,
			Record<?> parentObject) {

		doNothing ();

	}

	default
	void beforeInsert (
			RecordType object) {

		doNothing ();

	}

	default
	void afterInsert (
			RecordType object) {

		doNothing ();

	}

	default
	void beforeUpdate (
			RecordType object) {

		doNothing ();

	}

	default
	Object getDynamic (
			RecordType object,
			String name) {

		throw new UnsupportedOperationException (
			stringFormat (
				"%s.getDynamic (...)",
				getClass ().getSimpleName ()));

	}

	default
	void setDynamic (
			RecordType object,
			String name,
			Object value) {

		throw new UnsupportedOperationException (
			stringFormat (
				"%s.setDynamic (...)",
				getClass ().getSimpleName ()));

	}

	public
	class DefaultImplementation<RecordType extends Record<RecordType>>
		implements ObjectHooks<RecordType> {

	}

}
