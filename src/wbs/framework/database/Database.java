package wbs.framework.database;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormatArray;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

public
interface Database {

	OwnedTransaction beginTransaction (
			LogContext parentLogContext,
			Optional <TaskLogger> parentTaskLogger,
			String summary,
			boolean readWrite,
			boolean canJoin,
			boolean canCreateNew,
			boolean makeCurrent);

	default
	OwnedTransaction beginReadWrite (
			@NonNull LogContext parentLogContext,
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String summary) {

		return beginTransaction (
			parentLogContext,
			optionalOf (
				parentTaskLogger),
			summary,
			true,
			false,
			true,
			true);

	}

	default
	OwnedTransaction beginReadWriteFormat (
			@NonNull LogContext parentLogContext,
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String ... summaryArguments) {

		return beginTransaction (
			parentLogContext,
			optionalOf (
				parentTaskLogger),
			stringFormatArray (
				summaryArguments),
			true,
			false,
			true,
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
			true,
			false,
			true,
			true);

	}

	default
	OwnedTransaction beginReadWriteFormat (
			@NonNull LogContext parentLogContext,
			@NonNull String ... summaryArguments) {

		return beginTransaction (
			parentLogContext,
			optionalAbsent (),
			stringFormatArray (
				summaryArguments),
			true,
			false,
			true,
			true);

	}

	default
	OwnedTransaction beginReadOnly (
			@NonNull LogContext parentLogContext,
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String summary) {

		return beginTransaction (
			parentLogContext,
			optionalOf (
				parentTaskLogger),
			summary,
			false,
			true,
			true,
			true);

	}

	default
	OwnedTransaction beginReadOnlyFormat (
			@NonNull LogContext parentLogContext,
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String ... summaryArguments) {

		return beginTransaction (
			parentLogContext,
			optionalOf (
				parentTaskLogger),
			stringFormatArray (
				summaryArguments),
			false,
			true,
			true,
			true);

	}

	default
	OwnedTransaction beginReadOnly (
			@NonNull LogContext parentLogContext,
			@NonNull String summary) {

		return beginTransaction (
			parentLogContext,
			optionalAbsent (),
			summary,
			false,
			true,
			true,
			true);

	}

	default
	OwnedTransaction beginReadOnlyFormat (
			@NonNull LogContext parentLogContext,
			@NonNull String ... summaryArguments) {

		return beginTransaction (
			parentLogContext,
			optionalAbsent (),
			stringFormatArray (
				summaryArguments),
			false,
			true,
			true,
			true);

	}

	default
	OwnedTransaction beginReadOnlyJoin (
			LogContext parentLogContext,
			TaskLogger parentTaskLogger,
			String summary) {

		return beginTransaction (
			parentLogContext,
			optionalOf (
				parentTaskLogger),
			summary,
			false,
			false,
			true,
			true);

	}

	default
	OwnedTransaction beginReadOnlyJoinFormat (
			LogContext parentLogContext,
			TaskLogger parentTaskLogger,
			String ... summaryArguments) {

		return beginTransaction (
			parentLogContext,
			optionalOf (
				parentTaskLogger),
			stringFormatArray (
				summaryArguments),
			false,
			false,
			true,
			true);

	}

	default
	OwnedTransaction beginReadOnlyJoin (
			@NonNull LogContext parentLogContext,
			@NonNull String summary) {

		return beginTransaction (
			parentLogContext,
			optionalAbsent (),
			summary,
			false,
			false,
			true,
			true);

	}

	default
	OwnedTransaction beginReadOnlyJoinFormat (
			@NonNull LogContext parentLogContext,
			@NonNull String ... summaryArguments) {

		return beginTransaction (
			parentLogContext,
			optionalAbsent (),
			stringFormatArray (
				summaryArguments),
			false,
			false,
			true,
			true);

	}

}
