package wbs.framework.utils.cache;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalMapOptional;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.joda.time.Duration;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class IdLookupCache<Key,Id,Value>
	implements AdvancedCache<Key,Value> {

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
	Boolean cacheNegatives;

	@Getter @Setter
	Function<Key,Optional<Value>> lookupByKey;

	@Getter @Setter
	Function<Id,Optional<Value>> lookupById;

	@Getter @Setter
	Function<Value,Id> getId;

	// state

	@SuppressWarnings ("unchecked")
	Cache<Key,Optional<Id>> idCache =

		(Cache<Key,Optional<Id>>)

		(Cache<?,?>)

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
	Optional<Value> get (
			@NonNull Key key) {

		// first try the cache

		Optional<Id> cachedId =
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

				return lookupById.apply (
					cachedId.get ());

			} else {

				return Optional.absent ();

			}

		}

		// lookup by key and store in cache

		Optional<Value> valueOptional =
			lookupByKey.apply (
				key);

		if (cacheNegatives) {

			idCache.put (
				key,
				optionalMapOptional (
					valueOptional,
					value ->
						Optional.of (
							getId.apply (
								value))));

		} else if (
			optionalIsPresent (
				valueOptional)
		) {

			idCache.put (
				key,
				Optional.of (
					getId.apply (
						valueOptional.get ())));

		}

		return valueOptional;

	}

}
