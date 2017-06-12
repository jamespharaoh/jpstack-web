package wbs.smsapps.forwarder.api;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerNotEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

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
import wbs.platform.rpc.core.RpcException;
import wbs.platform.rpc.core.RpcHandler;
import wbs.platform.rpc.core.RpcList;
import wbs.platform.rpc.core.RpcResult;
import wbs.platform.rpc.core.RpcSource;
import wbs.platform.rpc.core.RpcType;

import wbs.sms.message.core.model.MessageStatus;

import wbs.smsapps.forwarder.model.ForwarderMessageOutObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderRec;

@PrototypeComponent ("forwarderQueryExRpcHandler")
public
class ForwarderQueryExRpcHandler
	implements RpcHandler {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ForwarderApiLogic forwarderApiLogic;

	@SingletonDependency
	ForwarderMessageOutObjectHelper forwarderMessageOutHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	@NamedDependency ("forwarderQueryExRequestDef")
	RpcDefinition queryExRequestDef;

	// state

	ForwarderRec forwarder;

	List <String> errors =
		new ArrayList<> ();

	List <ForwarderQueryExMessage> messages =
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
					"QueryExRpcHandler.handle");

		) {

			// authenticate

			forwarder =
				forwarderApiLogic.rpcAuth (
					transaction,
					source);

			// get params

			getParams (
				transaction,
				source);

			// bail on any request-invalid classErrors

			if (errors.iterator ().hasNext ()) {

				return Rpc.rpcError (
					"forwarder-query-ex-response",
					Rpc.stRequestInvalid,
					"request-invalid",
					errors);

			}

			// do the stuff

			findMessages (
				transaction);

			checkIdsMatch (
				transaction);

			// return

			transaction.commit ();

			return makeSuccess (
				transaction);

		}

	}

	private
	void getParams (
			@NonNull Transaction parentTransaction,
			@NonNull RpcSource source) {

		Map <String, Object> params =
			genericCastUnchecked (
				source.obtain (
					queryExRequestDef,
					errors,
					true));

		List <Map <String, Object>> mpList =
			genericCastUnchecked (
				params.get (
					"unqueueExMessages"));

		if (mpList == null) {
			return;
		}

		for (
			Map<String,Object> mp
				: mpList
		) {

			if (mp == null)
				continue;

			ForwarderQueryExMessage queryExMessage =
				new ForwarderQueryExMessage ();

			queryExMessage.clientId =
				(String)
				mp.get (
					"client-id");

			queryExMessage.serverId =
				(Long)
				mp.get (
					"server-id");

			messages.add (
				queryExMessage);

		}

	}

	private
	void findMessages (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findMessages");

		) {

			for (
				int i = 0;
				i < messages.size ();
				i ++
			) {

				ForwarderQueryExMessage queryExMessage =
					messages.get (i);

				if (queryExMessage.serverId != null) {

					queryExMessage.fmOut =
						optionalOrNull (
							forwarderMessageOutHelper.find (
								transaction,
								queryExMessage.serverId));

				}

				if (

					isNotNull (
						queryExMessage.clientId)

					&& isNull (
						queryExMessage.fmOut)

				) {

					queryExMessage.fmOut =
						forwarderMessageOutHelper.findByOtherId (
							transaction,
							forwarder,
							queryExMessage.clientId);

				}

				if (queryExMessage.fmOut == null) {

					errors.add (
						stringFormat (
							"Message %s not found",
							integerToDecimalString (
								i)));

				}

			}

			if (errors.size() > 0) {

				throw new RpcException (
					"forwarder-query-ex-response",
					ForwarderApiConstants.stMessageNotFound,
					"message-not-found",
					errors);

			}

		}

	}

	private
	void checkIdsMatch (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"checkIdsMatch");

		) {

			for (
				int index = 0;
				index < messages.size ();
				index ++
			) {

				ForwarderQueryExMessage queryExMessage =
					messages.get (index);

				if (queryExMessage.serverId == null
						|| queryExMessage.clientId == null)
					continue;

				if (

					integerNotEqualSafe (
						queryExMessage.serverId,
						queryExMessage.fmOut.getId ())

					|| stringNotEqualSafe (
						queryExMessage.clientId,
						queryExMessage.fmOut.getOtherId ())

				) {

					errors.add ("Message " + index + " id mismatch");

				}

			}

			if (errors.size () > 0) {

				throw new RpcException (
					"forwarder-query-ex-response",
					ForwarderApiConstants.stMessageIdMismatch,
					"message-id-mismatch",
					errors);

			}

		}

	}

	private
	RpcResult makeSuccess (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"makeSuccess");

		) {

			RpcList messagesPart =
				Rpc.rpcList (
					"unqueueExMessages",
					"message",
					RpcType.rStructure);

			for (
				ForwarderQueryExMessage queryExMessage
					: messages
			) {

				MessageStatus messageStatus =
					queryExMessage.fmOut.getMessage ().getStatus ();

				ForwarderMessageStatus forwarderMessageStatus =
					forwarderApiLogic.messageStatusMap (
						messageStatus);

				messagesPart.add (
					Rpc.rpcStruct (
						"message",

						Rpc.rpcElem (
							"server-id",
							queryExMessage.fmOut.getId ()),

						Rpc.rpcElem (
							"client-id",
							queryExMessage.fmOut.getOtherId ()),

						Rpc.rpcElem (
							"message-status",
							forwarderMessageStatus.status ()),

						Rpc.rpcElem (
							"message-status-code",
							forwarderMessageStatus.statusCode ())));

			}

			return Rpc.rpcSuccess (
				"Success",
				"forwarder-query-ex-response",
				messagesPart);

		}

	}

}
