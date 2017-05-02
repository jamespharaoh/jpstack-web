package wbs.framework.logging;

import java.util.function.Function;

import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Duration;

import wbs.utils.cache.CachedGetter;
import wbs.utils.cache.GenericCachedGetter;

@Accessors (fluent = true)
public
class SimpleCachedGetter <Type> {

	// properties

	@Setter
	LogContext logContext;

	@Setter
	Function <TaskLogger, Type> provider;

	@Setter
	Duration reloadFrequency =
		Duration.standardSeconds (1l);

	// implemetation

	CachedGetter <TaskLogger, Type> build () {

		return new GenericCachedGetter <TaskLogger, Type> (
			this::getWrapper,
			provider,
			reloadFrequency);

	}

	// private implementation

	private
	Type getWrapper (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Function <TaskLogger, Type> provider) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"get");

		) {

			return provider.apply (
				taskLogger);

		}

	}

}
