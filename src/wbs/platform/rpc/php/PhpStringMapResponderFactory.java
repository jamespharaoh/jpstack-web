package wbs.platform.rpc.php;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.web.Responder;
import wbs.platform.api.StringMapResponderFactory;

@SingletonComponent ("phpStringMapResponderFactory")
public
class PhpStringMapResponderFactory
	implements StringMapResponderFactory {

	@Inject
	Provider<PhpMapResponder> phpMapResponder;

	@Override
	public
	Responder makeResponder (
			Map<String,?> map) {

		return phpMapResponder.get ()

			.map (
				map);

	}

}
