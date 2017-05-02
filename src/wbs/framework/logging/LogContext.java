package wbs.framework.logging;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormatArray;

import com.google.common.base.Optional;

import lombok.NonNull;

public
interface LogContext {

	OwnedTaskLogger createTaskLogger (
			CharSequence dynamicContext,
			Optional <Boolean> debugEnabled);

	default
	OwnedTaskLogger createTaskLogger (
			@NonNull CharSequence dynamicContext) {

		return createTaskLogger (
			dynamicContext,
			optionalAbsent ());

	}

	default
	OwnedTaskLogger createTaskLoggerFormat (
			@NonNull CharSequence ... arguments) {

		return createTaskLogger (
			stringFormatArray (
				arguments),
			optionalAbsent ());

	}

	default
	OwnedTaskLogger createTaskLogger (
			@NonNull CharSequence dynamicContext,
			@NonNull Boolean debugEnabled) {

		return createTaskLogger (
			dynamicContext,
			optionalOf (
				debugEnabled));

	}

	OwnedTaskLogger nestTaskLogger (
			Optional <TaskLogger> parent,
			CharSequence dynamicContext,
			Optional <Boolean> debugEnabled);

	default
	OwnedTaskLogger nestTaskLogger (
			@NonNull Optional <TaskLogger> parent,
			@NonNull CharSequence dynamicContext,
			@NonNull Boolean debugEnabled) {

		return nestTaskLogger (
			parent,
			dynamicContext,
			optionalOf (
				debugEnabled));

	}

	default
	OwnedTaskLogger nestTaskLogger (
			@NonNull TaskLogger parent,
			@NonNull CharSequence dynamicContext,
			@NonNull Boolean debugEnabled) {

		return nestTaskLogger (
			optionalOf (
				parent),
			dynamicContext,
			optionalOf (
				debugEnabled));

	}

	default
	OwnedTaskLogger nestTaskLogger (
			Optional <TaskLogger> parent,
			CharSequence dynamicContext) {

		return nestTaskLogger (
			parent,
			dynamicContext,
			optionalAbsent ());

	}

	default
	OwnedTaskLogger nestTaskLogger (
			TaskLogger parent,
			CharSequence dynamicContext) {

		return nestTaskLogger (
			optionalOf (
				parent),
			dynamicContext,
			optionalAbsent ());

	}

	default
	OwnedTaskLogger nestTaskLoggerFormat (
			@NonNull TaskLogger parent,
			@NonNull CharSequence ... dynamicContextArguments) {

		return nestTaskLogger (
			optionalOf (
				parent),
			stringFormatArray (
				dynamicContextArguments),
			optionalAbsent ());

	}

}
