package wbs.utils.cache;

import com.google.common.base.Optional;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("advancedCache")
public
interface AdvancedCache <Key, Value> {

	Optional <Value> find (
			Key key);

	Value findOrCreate (
			TaskLogger parentTaskLogger,
			Key key);

	Value create (
			TaskLogger parentTaskLogger,
			Key key);

}
