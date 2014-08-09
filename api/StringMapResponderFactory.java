package wbs.platform.api;

import java.util.Map;

import wbs.framework.web.Responder;

public
interface StringMapResponderFactory {

	Responder makeResponder (
		Map<String,?> map);

}
