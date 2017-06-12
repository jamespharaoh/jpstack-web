package wbs.platform.rpc.php;

import java.util.Map;

import javax.inject.Provider;

import wbs.api.mvc.StringMapResponderFactory;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;

import wbs.web.responder.WebResponder;

@SingletonComponent ("phpStringMapResponderFactory")
public
class PhpStringMapResponderFactory
	implements StringMapResponderFactory {

	// prototype dependencies

	@PrototypeDependency
	Provider <PhpMapResponder> phpMapResponder;

	// implementation

	@Override
	public
	WebResponder makeResponder (
			Map<String,?> map) {

		return phpMapResponder.get ()

			.map (
				map);

	}

}
