package wbs.framework.logging;

import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormatArray;

import com.google.common.base.Optional;

import lombok.NonNull;

public
interface LogContext {

	TaskLogger createTaskLogger (
			String dynamicContext);

	TaskLogger nestTaskLogger (
			Optional <TaskLogger> parent,
			String dynamicContext);

	default
	TaskLogger nestTaskLogger (
			@NonNull TaskLogger parent,
			@NonNull String dynamicContext) {

		return nestTaskLogger (
			optionalOf (
				parent),
			dynamicContext);

	}

	default
	TaskLogger nestTaskLoggerFormat (
			@NonNull TaskLogger parent,
			@NonNull String ... dynamicContextArguments) {

		return nestTaskLogger (
			parent,
			stringFormatArray (
				dynamicContextArguments));

	}

}
