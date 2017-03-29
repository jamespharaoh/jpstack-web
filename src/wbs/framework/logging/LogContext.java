package wbs.framework.logging;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormatArray;

import com.google.common.base.Optional;

import lombok.NonNull;

public
interface LogContext {

	TaskLogger createTaskLogger (
			String dynamicContext,
			Optional <Boolean> debugEnabled);

	default
	TaskLogger createTaskLogger (
			@NonNull String dynamicContext) {

		return createTaskLogger (
			dynamicContext,
			optionalAbsent ());

	}

	default
	TaskLogger createTaskLoggerFormat (
			@NonNull String ... arguments) {

		return createTaskLogger (
			stringFormatArray (
				arguments),
			optionalAbsent ());

	}

	default
	TaskLogger createTaskLogger (
			@NonNull String dynamicContext,
			@NonNull Boolean debugEnabled) {

		return createTaskLogger (
			dynamicContext,
			optionalOf (
				debugEnabled));

	}

	TaskLogger nestTaskLogger (
			Optional <TaskLogger> parent,
			String dynamicContext,
			Optional <Boolean> debugEnabled);

	default
	TaskLogger nestTaskLogger (
			Optional <TaskLogger> parent,
			String dynamicContext) {

		return nestTaskLogger (
			parent,
			dynamicContext,
			optionalAbsent ());

	}

	default
	TaskLogger nestTaskLogger (
			TaskLogger parent,
			String dynamicContext) {

		return nestTaskLogger (
			optionalOf (
				parent),
			dynamicContext,
			optionalAbsent ());

	}

	default
	TaskLogger nestTaskLoggerFormat (
			@NonNull TaskLogger parent,
			@NonNull String ... dynamicContextArguments) {

		return nestTaskLogger (
			optionalOf (
				parent),
			stringFormatArray (
				dynamicContextArguments),
			optionalAbsent ());

	}

}
