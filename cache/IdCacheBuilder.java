package wbs.utils.cache;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsSpecialConfig;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@Data
@PrototypeComponent ("idCacheBuilder")
public
class IdCacheBuilder <Key, Id, Value> {

	// singleton dependencies

	@SingletonDependency
	WbsSpecialConfig wbsSpecialConfig;

	// properties

	Boolean dummy = false;
	Boolean cacheNegatives = false;

	Function <Key, Optional <Value>> lookupByKeyFunction;
	Function <Id, Optional <Value>> lookupByIdFunction;
	Function <Value, Id> getIdFunction;
	BiFunction <TaskLogger, Key, Value> createFunction;

	// implementation

	public
	AdvancedCache <Key, Value> build () {

		if (dummy) {

			return new DummyCache <Key, Value> ()

				.lookupByKeyFunction (
					lookupByKeyFunction);

		} else {

			return new IdLookupCache <Key, Id, Value> ()

				.assumeNegatives (
					wbsSpecialConfig.assumeNegativeCache ())

				.cacheNegatives (
					cacheNegatives)

				.lookupByIdFunction (
					lookupByIdFunction)

				.lookupByKeyFunction (
					lookupByKeyFunction)

				.getIdFunction (
					getIdFunction)

				.createFunction (
					createFunction);

		}

	}

}
