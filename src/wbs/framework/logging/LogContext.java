package wbs.framework.logging;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

public
interface LogContext {

	OwnedTaskLogger createTaskLogger (
			String dynamicContextName,
			List <CharSequence> dynamicContextParameters,
			Optional <Boolean> debugEnabled);

	default
	OwnedTaskLogger createTaskLogger (
			@NonNull String dynamicContextName,
			@NonNull CharSequence ... parameters) {

		return createTaskLogger (
			dynamicContextName,
			ImmutableList.copyOf (
				parameters),
			optionalAbsent ());

	}

	default
	OwnedTaskLogger createTaskLogger (
			@NonNull String dynamicContextName,
			@NonNull Boolean debugEnabled) {

		return createTaskLogger (
			dynamicContextName,
			emptyList (),
			optionalOf (
				debugEnabled));

	}

	OwnedTaskLogger nestTaskLogger (
			Optional <TaskLogger> parent,
			String dynamicContextName,
			List <CharSequence> dynamicContextParameters,
			Optional <Boolean> debugEnabled);

	default
	OwnedTaskLogger nestTaskLogger (
			@NonNull Optional <TaskLogger> parent,
			@NonNull String dynamicContextName,
			@NonNull Boolean debugEnabled) {

		return nestTaskLogger (
			parent,
			dynamicContextName,
			emptyList (),
			optionalOf (
				debugEnabled));

	}

	default
	OwnedTaskLogger nestTaskLogger (
			@NonNull TaskLogger parent,
			@NonNull String dynamicContextName,
			@NonNull Boolean debugEnabled) {

		return nestTaskLogger (
			optionalOf (
				parent),
			dynamicContextName,
			emptyList (),
			optionalOf (
				debugEnabled));

	}

	default
	OwnedTaskLogger nestTaskLogger (
			Optional <TaskLogger> parent,
			String dynamicContextName) {

		return nestTaskLogger (
			parent,
			dynamicContextName,
			emptyList (),
			optionalAbsent ());

	}

	default
	OwnedTaskLogger nestTaskLogger (
			TaskLogger parent,
			String dynamicContextName) {

		return nestTaskLogger (
			optionalOf (
				parent),
			dynamicContextName,
			emptyList (),
			optionalAbsent ());

	}

	default
	OwnedTaskLogger nestTaskLogger (
			@NonNull TaskLogger parent,
			@NonNull String dynamicContextName,
			@NonNull CharSequence ... dynamicContextParameters) {

		return nestTaskLogger (
			optionalOf (
				parent),
			dynamicContextName,
			ImmutableList.copyOf (
				dynamicContextParameters),
			optionalAbsent ());

	}

}
