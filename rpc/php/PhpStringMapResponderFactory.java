package wbs.platform.rpc.php;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.api.mvc.StringMapResponderFactory;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.web.Responder;

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
