package wbs.framework.object;

import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.NonNull;
import wbs.framework.entity.record.Record;

public
interface ObjectHooks <RecordType extends Record <RecordType>> {

	default
	void createSingletons (
			@NonNull ObjectHelper <RecordType> objectHelper,
			@NonNull ObjectHelper <?> parentHelper,
			@NonNull Record <?> parentObject) {

		doNothing ();

	}

	default
	void beforeInsert (
			@NonNull RecordType object) {

		doNothing ();

	}

	default
	void afterInsert (
			@NonNull RecordType object) {

		doNothing ();

	}

	default
	void beforeUpdate (
			@NonNull RecordType object) {

		doNothing ();

	}

	default
	Object getDynamic (
			@NonNull RecordType object,
			@NonNull String name) {

		throw new UnsupportedOperationException (
			stringFormat (
				"%s.getDynamic (...)",
				getClass ().getSimpleName ()));

	}

	default
	void setDynamic (
			@NonNull RecordType object,
			@NonNull String name,
			@NonNull Optional <?> value) {

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
