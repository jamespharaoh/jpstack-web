package wbs.framework.database;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormatLazyArray;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

public
interface Database {

	OwnedTransaction beginTransaction (
			LogContext parentLogContext,
			Optional <TaskLogger> parentTaskLogger,
			CharSequence summary,
			boolean readWrite);

	default
	OwnedTransaction beginReadWrite (
			@NonNull LogContext parentLogContext,
			@NonNull TaskLogger parentTaskLogger,
			@NonNull CharSequence summary) {

		return beginTransaction (
			parentLogContext,
			optionalOf (
				parentTaskLogger),
			summary,
			true);

	}

	default
	OwnedTransaction beginReadWriteFormat (
			@NonNull LogContext parentLogContext,
			@NonNull TaskLogger parentTaskLogger,
			@NonNull CharSequence ... summaryArguments) {

		return beginTransaction (
			parentLogContext,
			optionalOf (
				parentTaskLogger),
			stringFormatLazyArray (
				summaryArguments),
			true);

	}

	default
	OwnedTransaction beginReadWrite (
			@NonNull LogContext parentLogContext,
			@NonNull String summary) {

		return beginTransaction (
			parentLogContext,
			optionalAbsent (),
			summary,
			true);

	}

	default
	OwnedTransaction beginReadWriteFormat (
			@NonNull LogContext parentLogContext,
			@NonNull CharSequence ... summaryArguments) {

		return beginTransaction (
			parentLogContext,
			optionalAbsent (),
			stringFormatLazyArray (
				summaryArguments),
			true);

	}

	default
	OwnedTransaction beginReadOnly (
			@NonNull LogContext parentLogContext,
			@NonNull TaskLogger parentTaskLogger,
			@NonNull CharSequence summary) {

		return beginTransaction (
			parentLogContext,
			optionalOf (
				parentTaskLogger),
			summary,
			false);

	}

	default
	OwnedTransaction beginReadOnlyFormat (
			@NonNull LogContext parentLogContext,
			@NonNull TaskLogger parentTaskLogger,
			@NonNull CharSequence ... summaryArguments) {

		return beginTransaction (
			parentLogContext,
			optionalOf (
				parentTaskLogger),
			stringFormatLazyArray (
				summaryArguments),
			false);

	}

	default
	OwnedTransaction beginReadOnly (
			@NonNull LogContext parentLogContext,
			@NonNull CharSequence summary) {

		return beginTransaction (
			parentLogContext,
			optionalAbsent (),
			summary,
			false);

	}

	default
	OwnedTransaction beginReadOnlyFormat (
			@NonNull LogContext parentLogContext,
			@NonNull CharSequence ... summaryArguments) {

		return beginTransaction (
			parentLogContext,
			optionalAbsent (),
			stringFormatLazyArray (
				summaryArguments),
			false);

	}

	default
	OwnedTransaction beginReadOnlyJoin (
			LogContext parentLogContext,
			TaskLogger parentTaskLogger,
			CharSequence summary) {

		return beginTransaction (
			parentLogContext,
			optionalOf (
				parentTaskLogger),
			summary,
			false);

	}

	default
	OwnedTransaction beginReadOnlyJoinFormat (
			LogContext parentLogContext,
			TaskLogger parentTaskLogger,
			CharSequence ... summaryArguments) {

		return beginTransaction (
			parentLogContext,
			optionalOf (
				parentTaskLogger),
			stringFormatLazyArray (
				summaryArguments),
			false);

	}

	default
	OwnedTransaction beginReadOnlyJoin (
			@NonNull LogContext parentLogContext,
			@NonNull CharSequence summary) {

		return beginTransaction (
			parentLogContext,
			optionalAbsent (),
			summary,
			false);

	}

	default
	OwnedTransaction beginReadOnlyJoinFormat (
			@NonNull LogContext parentLogContext,
			@NonNull CharSequence ... summaryArguments) {

		return beginTransaction (
			parentLogContext,
			optionalAbsent (),
			stringFormatLazyArray (
				summaryArguments),
			false);

	}

}
