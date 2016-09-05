package wbs.framework.utils.cache;

import java.util.function.Function;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.experimental.Accessors;

public
interface AdvancedCache<Key,Value> {

	Optional<Value> get (
			Key key);

	@Accessors (fluent = true)
	@Data
	public static
	class IdBuilder<Key,Id,Value> {

		Boolean dummy;
		Boolean cacheNegatives;

		Function<Key,Optional<Value>> lookupByKey;
		Function<Id,Optional<Value>> lookupById;
		Function<Value,Id> getId;

		public
		AdvancedCache<Key,Value> build () {

			if (dummy) {

				return new DummyCache<Key,Value> ()

					.lookupByKey (
						lookupByKey);

			} else {

				return new IdLookupCache<Key,Id,Value> ()

					.cacheNegatives (
						cacheNegatives)

					.lookupById (
						lookupById)

					.lookupByKey (
						lookupByKey)

					.getId (
						getId);

			}

		}

	}

}
