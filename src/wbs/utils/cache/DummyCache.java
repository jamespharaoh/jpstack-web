package wbs.utils.cache;

import java.util.function.Function;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class DummyCache <Key, Value>
	implements AdvancedCache <Key, Value> {

	@Getter @Setter
	Function <Key, Optional <Value>> lookupByKeyFunction;

	@Override
	public
	Optional <Value> find (
			@NonNull Key key) {

		return lookupByKeyFunction.apply (
			key);

	}

	@Override
	public
	Value create (
			@NonNull Key key) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Value findOrCreate (
			@NonNull Key key) {

		throw new UnsupportedOperationException ();

	}

}
