package wbs.framework.object;

import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

public
interface ObjectHooks <RecordType extends Record <RecordType>> {

	default
	void createSingletons (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ObjectHelper <RecordType> objectHelper,
			@NonNull ObjectHelper <?> parentHelper,
			@NonNull Record <?> parentObject) {

		doNothing ();

	}

	default
	void beforeInsert (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull RecordType object) {

		doNothing ();

	}

	default
	void afterInsert (
			@NonNull TaskLogger parentTaskLogger,
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
			@NonNull TaskLogger parentTaskLogger,
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
