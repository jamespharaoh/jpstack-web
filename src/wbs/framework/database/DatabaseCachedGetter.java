package wbs.framework.database;

import java.util.function.Function;

import lombok.NonNull;
import lombok.experimental.Accessors;

import org.joda.time.Duration;

import wbs.framework.logging.LogContext;

import wbs.utils.cache.CachedGetter;
import wbs.utils.cache.GenericCachedGetter;

@Accessors (fluent = true)
public
class DatabaseCachedGetter <Type>
	implements CachedGetter <Transaction, Type> {

	// state

	private final
	LogContext logContext;

	private final
	CachedGetter <Transaction, Type> delegate;

	// constructors

	public
	DatabaseCachedGetter (
			@NonNull LogContext logContext,
			@NonNull Function <Transaction, Type> refresh,
			@NonNull Duration reloadFrequency) {

		this.logContext =
			logContext;

		this.delegate =
			new GenericCachedGetter <Transaction, Type> (
				this::getWrapper,
				refresh,
				reloadFrequency);

	}

	// implementation

	@Override
	public
	Type get (
			@NonNull Transaction context) {

		return delegate.get (
			context);

	}

	// private implementation

	private
	Type getWrapper (
			@NonNull Transaction parentTransaction,
			@NonNull Function <Transaction, Type> provider) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"get");

		) {

			return provider.apply (
				transaction);

		}

	}

}
