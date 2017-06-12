package wbs.smsapps.forwarder.api;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.rpc.core.Rpc;
import wbs.platform.rpc.core.RpcDefinition;
import wbs.platform.rpc.core.RpcHandler;
import wbs.platform.rpc.core.RpcResult;
import wbs.platform.rpc.core.RpcSource;

import wbs.smsapps.forwarder.logic.ForwarderLogic;
import wbs.smsapps.forwarder.logic.ForwarderLogicImplementation;
import wbs.smsapps.forwarder.model.ForwarderRec;

@PrototypeComponent ("forwarderSendRpcHandler")
public
class ForwarderSendRpcHandler
	implements RpcHandler {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ForwarderApiLogic forwarderApiLogic;

	@SingletonDependency
	ForwarderLogic forwarderLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	@NamedDependency ("forwarderSendRequestDef")
	RpcDefinition sendRequestDef;

	// state

	String numTo;
	String numFrom;
	String message;
	String clientId;
	String route;
	String service;
	Long replyToServerId;
	Long pri;

	ForwarderRec forwarder;
	ForwarderLogicImplementation.SendTemplate sendTemplate;

	List <String> errors =
		new ArrayList<> ();

	// public implementation

	@Override
	public
	RpcResult handle (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull RpcSource source) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"SendRpcHandler.handle");

		) {

			// authenticate

			forwarder =
				forwarderApiLogic.rpcAuth (
					transaction,
					source);

			// get params

			getParams (source);

			// bail on any request-invalid classErrors

			if (errors.iterator ().hasNext ()) {

				return Rpc.rpcError (
					"forwarder-send-response",
					Rpc.stRequestInvalid,
					"request-invalid",
					errors);

			}

			// create template

			createTemplate ();

			// check template

			if (
				checkTemplate (
					transaction)
			) {

				// and send

				sendTemplate (
					transaction);

				// return success

				transaction.commit ();

				return Rpc.rpcSuccess (
					"Message queued for sending",
					"forwarer-send-response",
					Rpc.rpcElem (
						"server-id",
						sendTemplate.parts.get (0)
							.forwarderMessageOut
							.getId ()));

			} else {

				// return failure

				throw new RuntimeException (
					"Need to elaborate on error here...");

			}

		}

	}

	private
	void getParams (
			RpcSource source) {

		Map <String, Object> params =
			genericCastUnchecked (
				source.obtain (
					sendRequestDef,
					errors,
					true));

		numTo =
			(String)
			params.get ("num-to");

		numFrom =
			(String)
			params.get ("num-from");

		message =
			(String)
			params.get("message");

		clientId = (String) params.get("client-id");
		route = (String) params.get("route");
		service = (String) params.get("service");
		replyToServerId = (Long) params.get("reply-to-server-id");
		pri = (Long) params.get("pri");

	}

	private
	void createTemplate () {

		sendTemplate =
			new ForwarderLogicImplementation.SendTemplate ();

		sendTemplate.forwarder =
			forwarder;

		sendTemplate.fmInId =
			replyToServerId;

		ForwarderLogicImplementation.SendPart part =
			new ForwarderLogicImplementation.SendPart ();

		sendTemplate.parts.add (part);

		part.message = message;
		part.numFrom = numFrom;
		part.numTo = numTo;
		part.routeCode = route;
		part.serviceCode = service;
		part.clientId = clientId;
		part.pri = pri;

	}

	private
	boolean checkTemplate (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"checkTemplate");

		) {

			return forwarderLogic.sendTemplateCheck (
				transaction,
				sendTemplate);

		}

	}

	private
	void sendTemplate (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendTemplate");

		) {

			forwarderLogic.sendTemplateSend (
				transaction,
				sendTemplate);

		}

	}

}
