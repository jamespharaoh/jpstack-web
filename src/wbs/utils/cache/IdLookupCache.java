package wbs.utils.cache;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapOptional;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Duration;

@Accessors (fluent = true)
public
class IdLookupCache <Key, Id, Value>
	implements AdvancedCache <Key, Value> {

	// constants

	public final static
	Duration keepDuration =
		Duration.standardMinutes (
			60);

	public final static
	Long maximumCacheSize =
		1024l * 1024l;

	// properties

	@Getter @Setter
	Boolean assumeNegatives;

	@Getter @Setter
	Boolean cacheNegatives;

	@Getter @Setter
	Function <Key, Optional <Value>> lookupByKeyFunction;

	@Getter @Setter
	Function <Id, Optional <Value>> lookupByIdFunction;

	@Getter @Setter
	Function <Value, Id> getIdFunction;

	@Getter @Setter
	Function <Key, Value> createFunction;

	// state

	@SuppressWarnings ("unchecked")
	Cache <Key, Optional <Id>> idCache =

		(Cache <Key, Optional <Id>>)

		(Cache <?, ?>)

		CacheBuilder.newBuilder ()

		.maximumSize (
			maximumCacheSize)

		.expireAfterAccess (
			keepDuration.getMillis (),
			TimeUnit.MILLISECONDS)

		.build ();

	// public implementation

	@Override
	public
	Optional <Value> find (
			@NonNull Key key) {

		// first try the cache

		Optional <Id> cachedId =
			idCache.getIfPresent (
				key);

		if (
			isNotNull (
				cachedId)
		) {

			if (
				optionalIsPresent (
					cachedId)
			) {

				return lookupByIdFunction.apply (
					cachedId.get ());

			} else {

				return Optional.absent ();

			}

		} else if (assumeNegatives) {

			return optionalAbsent ();

		}

		// lookup by key and store in cache

		Optional <Value> valueOptional =
			lookupByKeyFunction.apply (
				key);

		if (cacheNegatives) {

			idCache.put (
				key,
				optionalMapOptional (
					valueOptional,
					value ->
						Optional.of (
							getIdFunction.apply (
								value))));

		} else if (
			optionalIsPresent (
				valueOptional)
		) {

			idCache.put (
				key,
				Optional.of (
					getIdFunction.apply (
						valueOptional.get ())));

		}

		return valueOptional;

	}

	@Override
	public
	Value create (
			@NonNull Key key) {

		Value value =
			createFunction.apply (
				key);

		Id id =
			getIdFunction.apply (
				value);

		idCache.put (
			key,
			optionalOf (
				id));

		return value;

	}

	@Override
	public
	Value findOrCreate (
			@NonNull Key key) {

		Optional <Value> valueOptional =
			find (
				key);

		if (
			optionalIsPresent (
				valueOptional)
		) {

			return optionalGetRequired (
				valueOptional);

		} else {

			return create (
				key);

		}

	}

}
