package wbs.framework.database;

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
	OwnedTransaction beginReadWrite (
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
	OwnedTransaction beginReadOnly (
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

}
