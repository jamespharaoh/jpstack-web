package wbs.framework.database;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

public
interface Database {

	OwnedTransaction beginTransaction (
			LogContext parentLogContext,
			Optional <TaskLogger> parentTaskLogger,
			String dynamicContextName,
			List <CharSequence> dynamicContextParameters,
			boolean readWrite);

	default
	OwnedTransaction beginReadWriteWithoutParameters (
			@NonNull LogContext parentLogContext,
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String dynamicContextName) {

		return beginTransaction (
			parentLogContext,
			optionalOf (
				parentTaskLogger),
			dynamicContextName,
			emptyList (),
			true);

	}

	default
	OwnedTransaction beginReadWriteWithParameters (
			@NonNull LogContext parentLogContext,
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String dynamicContextName,
			@NonNull CharSequence ... dynamicContextParameters) {

		return beginTransaction (
			parentLogContext,
			optionalOf (
				parentTaskLogger),
			dynamicContextName,
			Arrays.asList (
				dynamicContextParameters),
			true);

	}

	default
	OwnedTransaction beginReadWriteWithoutParameters (
			@NonNull LogContext parentLogContext,
			@NonNull String dynamicContextName) {

		return beginTransaction (
			parentLogContext,
			optionalAbsent (),
			dynamicContextName,
			emptyList (),
			true);

	}

	default
	OwnedTransaction beginReadWriteWithParameters (
			@NonNull LogContext parentLogContext,
			@NonNull String dynamicContextName,
			@NonNull CharSequence ... dynamicContextParameters) {

		return beginTransaction (
			parentLogContext,
			optionalAbsent (),
			dynamicContextName,
			Arrays.asList (
				dynamicContextParameters),
			true);

	}

	default
	OwnedTransaction beginReadOnlyWithoutParameters (
			@NonNull LogContext parentLogContext,
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String dynamicContextName) {

		return beginTransaction (
			parentLogContext,
			optionalOf (
				parentTaskLogger),
			dynamicContextName,
			emptyList (),
			false);

	}

	default
	OwnedTransaction beginReadOnlyWithParameters (
			@NonNull LogContext parentLogContext,
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String dynamicContextName,
			@NonNull CharSequence ... dynamicContextParameters) {

		return beginTransaction (
			parentLogContext,
			optionalOf (
				parentTaskLogger),
			dynamicContextName,
			Arrays.asList (
				dynamicContextParameters),
			false);

	}

	default
	OwnedTransaction beginReadOnlyWithoutParameters (
			@NonNull LogContext parentLogContext,
			@NonNull String dynamicContextName) {

		return beginTransaction (
			parentLogContext,
			optionalAbsent (),
			dynamicContextName,
			emptyList (),
			false);

	}

	default
	OwnedTransaction beginReadOnlyWithParameters (
			@NonNull LogContext parentLogContext,
			@NonNull String dynamicContextName,
			@NonNull CharSequence ... dynamicContextParameters) {

		return beginTransaction (
			parentLogContext,
			optionalAbsent (),
			dynamicContextName,
			Arrays.asList (
				dynamicContextParameters),
			false);

	}

}
