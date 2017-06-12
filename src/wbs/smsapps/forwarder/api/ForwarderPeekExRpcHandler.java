package wbs.smsapps.forwarder.api;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
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
import wbs.platform.rpc.core.RpcList;
import wbs.platform.rpc.core.RpcResult;
import wbs.platform.rpc.core.RpcSource;
import wbs.platform.rpc.core.RpcStructure;
import wbs.platform.rpc.core.RpcType;

import wbs.smsapps.forwarder.model.ForwarderMessageInObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec;
import wbs.smsapps.forwarder.model.ForwarderMessageOutObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageOutRec;
import wbs.smsapps.forwarder.model.ForwarderMessageOutReportRec;
import wbs.smsapps.forwarder.model.ForwarderRec;

@PrototypeComponent ("forwarderPeekExRpcHandler")
public
class ForwarderPeekExRpcHandler
	implements RpcHandler {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ForwarderApiLogic forwarderApiLogic;

	@SingletonDependency
	ForwarderMessageInObjectHelper forwarderMessageInHelper;

	@SingletonDependency
	ForwarderMessageOutObjectHelper forwarderMessageOutHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	@NamedDependency ("forwarderPeekExRequestDef")
	RpcDefinition peekExRequestDef;

	// state

	Boolean getMessages;
	Boolean getReports;
	Long maxResults;
	Boolean advancedReporting;

	ForwarderRec forwarder;

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
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"handle");

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

			if (errors.iterator ().hasNext ())
				return Rpc.rpcError (
					"forwarder-peek-ex-ersponse",
					Rpc.stRequestInvalid,
					"request-invalid",
					errors);

			// find stuff

			RpcResult result =
				makeSuccess (
					transaction);

			// commit

			return result;

		}

	}

	private
	void getParams (
			@NonNull Transaction parentTransaction,
			@NonNull RpcSource source) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getParams");

		) {

			Map <String, Object> params =
				genericCastUnchecked (
					source.obtain (
						peekExRequestDef,
						errors,
						true));

			getMessages =
				(Boolean)
				params.get (
					"get-unqueueExMessages");

			getReports =
				(Boolean)
				params.get (
					"get-reports");

			maxResults =
				(Long)
				params.get (
					"max-results");

			advancedReporting =
				(Boolean)
				params.get (
					"advanced-reporting");

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

			RpcList reportsPart =
				Rpc.rpcList (
					"reports",
					"report",
					RpcType.rStructure);

			RpcList advancedReportsPart =
				Rpc.rpcList (
					"advancedreports",
					"advancedreport",
					RpcType.rStructure);

			// get a list of pending unqueueExMessages

			if (getMessages) {

				List <ForwarderMessageInRec> pendingMessageList =
					forwarderMessageInHelper.findPendingLimit (
						transaction,
						forwarder,
						maxResults);

				for (
					ForwarderMessageInRec forwarderMessageIn
						: pendingMessageList
				) {

					RpcStructure message;

					messagesPart.add (message =

						Rpc.rpcStruct (
							"message",

							Rpc.rpcElem (
								"server-id",
								forwarderMessageIn
									.getId ()),

							Rpc.rpcElem (
								"num-from",
								forwarderMessageIn
									.getMessage ()
									.getNumFrom ()),

							Rpc.rpcElem (
								"num-to",
								forwarderMessageIn
									.getMessage ()
									.getNumTo ()),

							Rpc.rpcElem (
								"message",
								forwarderMessageIn
									.getMessage ()
									.getText ()
									.getText ())));

					if (forwarderMessageIn
							.getMessage ()
							.getNetwork ()
							.getId () != 0) {

						message.add (
							Rpc.rpcElem (
								"network-id",
								forwarderMessageIn
									.getMessage ()
									.getNetwork ()
									.getId ()));

					}

				}

			}

			// get a list of pending reports

			if (getReports) {

				List <ForwarderMessageOutRec> pendingReportList =
					forwarderMessageOutHelper.findPendingLimit (
						transaction,
						forwarder,
						maxResults);

				// create the return data list

				for (
					ForwarderMessageOutRec forwarderMessageOut
						: pendingReportList
				) {

					transaction.debugFormat (
						"fmo.id = %s",
						integerToDecimalString (
							forwarderMessageOut.getId ()));

					ForwarderMessageOutReportRec forwarderMessageOutReport =
						forwarderMessageOut.getReports ().get (
							toJavaIntegerRequired (
								forwarderMessageOut.getReportIndexPending ()));

					ForwarderMessageStatus forwarderMessageStatus =
						forwarderApiLogic.messageStatusMap (
							forwarderMessageOutReport.getNewMessageStatus ());

					RpcStructure struct =
						Rpc.rpcStruct (
							"report",

							Rpc.rpcElem (
								"server-id",
								forwarderMessageOut.getId ()),

							Rpc.rpcElem (
								"client-id",
								forwarderMessageOut.getOtherId ()),

							Rpc.rpcElem (
								"report-id",
								forwarderMessageOutReport.getId ()),

							Rpc.rpcElem (
								"message-status",
								forwarderMessageStatus.status ()),

							Rpc.rpcElem (
								"message-status-code",
								forwarderMessageStatus.statusCode ()));

					if (advancedReporting != null && advancedReporting) {

						struct.add (
							Rpc.rpcElem (
								"advanced-message-status",
								forwarderMessageOutReport.getNewMessageStatus ()
									.getOrdinal ()));

					}

					reportsPart.add (struct);

				}

			}

			// and return it in a map

			if (advancedReporting != null && advancedReporting) {

				return Rpc.rpcSuccess (
					"forwarder-peek-ex-response",
					"Success",
					messagesPart,
					reportsPart,
					advancedReportsPart);

			} else {

				return Rpc.rpcSuccess (
					"forwarder-peek-ex-response",
					"Success",
					messagesPart,
					reportsPart);

			}

		}

	}

}
