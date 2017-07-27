package wbs.console.helper.core;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.Ordering;

import lombok.NonNull;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

public
interface ConsoleHooks <RecordType extends Record <RecordType>> {

	default
	Map <String, String> getContextData (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		return emptyMap ();

	}

	default
	Optional <String> getHtml (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object,
			@NonNull Boolean mini) {

		return optionalAbsent ();

	}

	default
	Optional <String> getListClass (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		return optionalAbsent ();

	}

	default
	void applySearchFilter (
			@NonNull Transaction parentTransaction,
			@NonNull Object searchObject) {

		doNothing ();

	}

	default
	void beforeCreate (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		doNothing ();

	}

	default
	void afterCreate (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		doNothing ();

	}

	default
	List <String> verifyData (
			@NonNull Transaction parentTransaction,
			@NonNull RecordType object) {

		return emptyList ();

	}

	default
	Ordering <RecordType> defaultOrdering () {
		return Ordering.natural ();
	}

	public static
	class DefaultImplementation <
		RecordType extends Record <RecordType>
	>
		implements ConsoleHooks <RecordType> {

	}

}
