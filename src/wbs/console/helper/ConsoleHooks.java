package wbs.console.helper;

import static wbs.framework.utils.etc.Misc.doNothing;
import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.record.Record;

public
interface ConsoleHooks<RecordType extends Record<RecordType>> {

	default
	Optional<String> getHtml (
			@NonNull RecordType object,
			@NonNull Boolean mini) {

		return Optional.<String>absent ();

	}

	default
	Optional<String> getListClass (
			@NonNull RecordType object) {

		return Optional.<String>absent ();

	}

	default
	void applySearchFilter (
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
	class DefaultImplementation<RecordType extends Record<RecordType>>
		implements ConsoleHooks<RecordType> {

	}

}
