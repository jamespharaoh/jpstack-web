package wbs.framework.utils.cache;

import java.util.function.Function;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class DummyCache<Key,Value>
	implements AdvancedCache<Key,Value> {

	@Getter @Setter
	Function<Key,Optional<Value>> lookupByKey;

	@Override
	public
	Optional <Value> get (
			@NonNull Key key) {

		return lookupByKey.apply (
			key);

	}

}
