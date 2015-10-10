package wbs.console.helper;

import wbs.framework.record.Record;

import com.google.common.base.Optional;

public
interface ConsoleHooks<RecordType extends Record<RecordType>> {

	Optional<String> getHtml (
			RecordType object);

}
