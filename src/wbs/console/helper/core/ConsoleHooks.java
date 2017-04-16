package wbs.console.helper.core;

import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

public
interface ConsoleHooks <RecordType extends Record <RecordType>> {

	default
	Optional <String> getHtml (
			@NonNull RecordType object,
			@NonNull Boolean mini) {

		return optionalAbsent ();

	}

	default
	Optional <String> getListClass (
			@NonNull RecordType object) {

		return optionalAbsent ();

	}

	default
	void applySearchFilter (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Object searchObject) {

		doNothing ();

	}

	default
	void beforeCreate (
			@NonNull RecordType object) {

		doNothing ();

	}

	default
	void afterCreate (
			@NonNull RecordType object) {

		doNothing ();

	}

	public static
	class DefaultImplementation <
		RecordType extends Record <RecordType>
	>
		implements ConsoleHooks <RecordType> {

	}

}
