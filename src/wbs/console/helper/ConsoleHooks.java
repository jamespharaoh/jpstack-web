package wbs.console.helper;

import com.google.common.base.Optional;

import wbs.framework.record.Record;

public
interface ConsoleHooks<RecordType extends Record<RecordType>> {

	Optional<String> getHtml (
			RecordType object);

	Optional<String> getListClass (
			RecordType object);

	void applySearchFilter (
			Object searchObject);

}
