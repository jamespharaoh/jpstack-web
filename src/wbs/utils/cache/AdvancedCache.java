package wbs.utils.cache;

import com.google.common.base.Optional;

import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("advancedCache")
public
interface AdvancedCache <Key, Value> {

	Optional <Value> find (
			Key key);

	Value findOrCreate (
			Key key);

	Value create (
			Key key);

}
