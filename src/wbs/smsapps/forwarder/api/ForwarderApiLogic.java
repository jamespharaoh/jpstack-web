package wbs.smsapps.forwarder.api;

import java.util.List;
import java.util.Map;

import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.rpc.core.RpcSource;
import wbs.smsapps.forwarder.logic.ForwarderNotFoundException;
import wbs.smsapps.forwarder.logic.IncorrectPasswordException;
import wbs.smsapps.forwarder.logic.ReportableException;
import wbs.smsapps.forwarder.model.ForwarderRec;

public interface ForwarderApiLogic {

	ForwarderRec lookupForwarder (
			RequestContext requestContext,
			String sliceCode,
			String code,
			String password)
		throws
			ForwarderNotFoundException,
			IncorrectPasswordException;

	Responder controlActionGet (
			RequestContext requestContext,
			ForwarderRec forwarder);

	Responder controlActionBorrow (
			RequestContext requestContext,
			ForwarderRec forwarder);

	Responder controlActionUnqueue (
			RequestContext requestContext,
			ForwarderRec forwarder)
		throws ReportableException;

	ForwarderRec rpcAuth (
			RpcSource source);

	Map<String,Object> unsafeMapStringObject (
			Object input);

	List<Map<String,Object>> unsafeListMapStringObject (
			Object input);

}
