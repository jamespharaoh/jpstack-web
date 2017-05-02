package wbs.smsapps.forwarder.api;

import wbs.framework.database.Transaction;

import wbs.platform.rpc.core.RpcSource;

import wbs.smsapps.forwarder.logic.ForwarderNotFoundException;
import wbs.smsapps.forwarder.logic.IncorrectPasswordException;
import wbs.smsapps.forwarder.logic.ReportableException;
import wbs.smsapps.forwarder.model.ForwarderRec;

import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

public
interface ForwarderApiLogic {

	ForwarderRec lookupForwarder (
			Transaction parentTransaction,
			RequestContext requestContext,
			String sliceCode,
			String code,
			String password)
		throws
			ForwarderNotFoundException,
			IncorrectPasswordException;

	Responder controlActionGet (
			Transaction parentTransaction,
			RequestContext requestContext,
			ForwarderRec forwarder);

	Responder controlActionBorrow (
			Transaction parentTransaction,
			RequestContext requestContext,
			ForwarderRec forwarder);

	Responder controlActionUnqueue (
			Transaction parentTransaction,
			RequestContext requestContext,
			ForwarderRec forwarder)
		throws ReportableException;

	ForwarderRec rpcAuth (
			Transaction parentTransaction,
			RpcSource source);

}
