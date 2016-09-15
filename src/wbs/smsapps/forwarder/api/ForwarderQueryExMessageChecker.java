package wbs.smsapps.forwarder.api;

import java.util.List;
import java.util.Map;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.rpc.core.RpcChecker;
import wbs.platform.rpc.core.RpcDefinition;

@SingletonComponent ("forwarderQueryExMessageChecker")
public
class ForwarderQueryExMessageChecker
	implements RpcChecker {

	// singleton dependencies

	@SingletonDependency
	ForwarderApiLogic forwarderApiLogic;

	// implementation

	@Override
	public
	Object check (
			RpcDefinition rpcDefinition,
			Object value,
			List<String> errors) {

		Map<String,Object> map =
			forwarderApiLogic.unsafeMapStringObject (
				value);

		if (
			! map.containsKey ("server-id")
			&& ! map.containsKey ("client-id")
		) {

			errors.add (
				"Must provide at least one of: server-id, client-id");

			return null;

		}

		return map;

	}

}
