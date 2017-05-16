package wbs.smsapps.forwarder.api;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.moreThan;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
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
import wbs.platform.rpc.core.RpcType;

import wbs.smsapps.forwarder.model.ForwarderMessageInObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec;
import wbs.smsapps.forwarder.model.ForwarderMessageOutRec;
import wbs.smsapps.forwarder.model.ForwarderMessageOutReportObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageOutReportRec;
import wbs.smsapps.forwarder.model.ForwarderRec;

@PrototypeComponent ("forwarderUnqueueExRpcHandler")
public
class ForwarderUnqueueExRpcHandler
	implements RpcHandler {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ForwarderApiLogic forwarderApiLogic;

	@SingletonDependency
	ForwarderMessageInObjectHelper forwarderMessageInHelper;

	@SingletonDependency
	ForwarderMessageOutReportObjectHelper forwarderMessageOutReportHelper;

	@SingletonDependency
	@NamedDependency
	RpcDefinition forwarderUnqueueExRequestDef;

	@ClassSingletonDependency
	LogContext logContext;

	// state

	ForwarderRec forwarder;
	Boolean allowPartial;

	List<UnqueueExMessage> unqueueExMessages =
		new ArrayList<UnqueueExMessage>();

	List<UnqueueExReport> reports =
	new ArrayList<UnqueueExReport>();

	int numFailed = 0, numSuccess = 0;
	boolean cancel;

	List<String> statusMessages =
		new ArrayList<String>();

	@Override
	public
	RpcResult handle (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull RpcSource source) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithoutParameters (
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

			getParams (source);

			// bail on any request-invalid classErrors

			if (statusMessages.size () > 0) {

				return Rpc.rpcError (
					"forwarder-unqueue-ex-response",
					Rpc.stRequestInvalid,
					"request-invalid",
					statusMessages);

			}

			// find unqueueExMessages and reports

			findMessages (
				transaction);

			findReports (
				transaction);

			if (numFailed > 0 && ! allowPartial)
				cancel = true;

			// unqueue them (unless we are cancelling)

			if (! cancel) {

				unqueueMessages (
					transaction);

				unqueueReports (
					transaction);

			}

			// return

			if (cancel)
				return makeCancelledResult ();

			transaction.commit ();

			return numFailed == 0
				? makeSuccessResult ()
				: makePartialResult ();

		}

	}

	private
	void getParams (
			RpcSource source) {

		Map <String, Object> params =
			genericCastUnchecked (
				source.obtain (
					forwarderUnqueueExRequestDef,
					statusMessages,
					true));

		allowPartial =
			(Boolean)
			params.get (
				"allow-partial");

		List <Map <String, Object>> messageParamsList =
			genericCastUnchecked (
				params.get (
					"unqueueExMessages"));

		if (messageParamsList != null) {

			for (
				Map <String, Object> messageParams
					: messageParamsList
			) {

				UnqueueExMessage unqueueExMessage =
					new UnqueueExMessage ();

				unqueueExMessage.serverId =
					(Long)
					messageParams.get (
						"server-id");

				unqueueExMessages.add (
					unqueueExMessage);

			}

		}

		List <Map <String, Object>> reportParamsList =
			genericCastUnchecked (
				params.get (
					"reports"));

		if (reportParamsList != null) {

			for (
				Map <String, Object> reportParams
					: reportParamsList
			) {

				UnqueueExReport unqueueExReport =
					new UnqueueExReport ();

				unqueueExReport.reportId =
					(Long)
					reportParams.get (
						"report-id");

				reports.add (
					unqueueExReport);

			}

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
				UnqueueExMessage unqueueExMessage
					: unqueueExMessages
			) {

				// lookup the message

				unqueueExMessage.forwarderMessageIn =
					optionalOrNull (
						forwarderMessageInHelper.find (
							transaction,
							unqueueExMessage.serverId));

				if (unqueueExMessage.forwarderMessageIn != null) {

					// if this report doesn't belong to this forwarder pretend
					// it doesn't exist

					if (
						referenceNotEqualWithClass (
							ForwarderRec.class,
							unqueueExMessage.forwarderMessageIn.getForwarder (),
							forwarder)
					) {
						unqueueExMessage.forwarderMessageIn = null;
					}

				}

				// keep track of whether any failed
				if (unqueueExMessage.forwarderMessageIn != null)
					numSuccess++;
				else
					numFailed++;

			}

		}

	}

	private
	void findReports (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findReports");

		) {

			for (
				UnqueueExReport unqueueExReport
					: reports
			) {

				// lookup the report

				unqueueExReport.fmOutReport =
					forwarderMessageOutReportHelper.findRequired (
						transaction,
						unqueueExReport.reportId);

				if (
					isNotNull (
						unqueueExReport.fmOutReport)
				) {

					// if this report doesn't belong to this forwarder pretend
					// it doesn't exist

					if (
						referenceNotEqualWithClass (
							ForwarderRec.class,
							unqueueExReport.fmOutReport.getForwarderMessageOut ()
								.getForwarder (),
							forwarder)
					) {
						unqueueExReport.fmOutReport = null;
					}

					// if this report is not pending yet then pretend it doesn't
					// exist
					else if (

						isNotNull (
							unqueueExReport.fmOutReport.getForwarderMessageOut ()
								.getReportIndexPending ())

						&& moreThan (
							unqueueExReport.fmOutReport.getIndex (),
							unqueueExReport.fmOutReport.getForwarderMessageOut ()
								.getReportIndexPending ())

					) {
						unqueueExReport.fmOutReport = null;
					}

				}

				// keep track of whether any failed
				if (unqueueExReport.fmOutReport != null)
					numSuccess++;
				else
					numFailed++;

			}

		}

	}

	private
	void unqueueReports (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"unqueueReports");

		) {

			for (
				UnqueueExReport unqueueExReport
					: reports
			) {

				// skip any which weren't found

				if (unqueueExReport.fmOutReport == null)
					continue;

				// skip any which are already processed

				if (unqueueExReport.fmOutReport.getProcessedTime() != null)
					continue;

				unqueueExReport.fmOutReport

					.setProcessedTime (
						transaction.now ());

				ForwarderMessageOutRec forwarderMessageOut =
					unqueueExReport.fmOutReport
						.getForwarderMessageOut ();

				if (
					forwarderMessageOut.getReportIndexPending () + 1
						< forwarderMessageOut.getReportIndexNext ()
				) {

					forwarderMessageOut

						.setReportIndexPending (
							forwarderMessageOut.getReportIndexPending () + 1);

				} else {

					forwarderMessageOut

						.setReportIndexPending (
							null)

						.setReportRetryTime (
							null)

						.setReportTries (
							null);

				}

			}

		}

	}

	private
	void unqueueMessages (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"unqueueMessages");
		) {

			for (
				UnqueueExMessage unqueueExMessage
					: unqueueExMessages
			) {

				// skip any which weren't found

				if (unqueueExMessage.forwarderMessageIn == null)
					continue;

				// skip any which are already processed

				if (unqueueExMessage.forwarderMessageIn.getPending () == false)
					continue;

				// unqueue it

				unqueueExMessage.forwarderMessageIn

					.setProcessedTime (
						transaction.now ())

					.setPending (
						false)

					.setSendQueue (
						false)

					.setRetryTime (
						null);

			}

		}

	}

	private
	RpcResult makePartialResult () {

		statusMessages.add ("Partial success");

		RpcList messagesPart =
			Rpc.rpcList (
				"unqueueExMessages",
				"message",
				RpcType.rStructure);

		for (int i = 0; i < unqueueExMessages.size (); i++) {

			UnqueueExMessage uem =
				unqueueExMessages.get (i);

			if (uem.forwarderMessageIn != null) {

				messagesPart.add (

					Rpc.rpcStruct (
						"message",

						Rpc.rpcElem (
							"server-id",
							uem.serverId),

						Rpc.rpcElem (
							"status",
							Rpc.stSuccess),

						Rpc.rpcElem (
							"status-code",
							"success"),

						Rpc.rpcElem (
							"status-message",
							"Success")));

			} else {

				messagesPart.add (

					Rpc.rpcStruct (
						"message",

						Rpc.rpcElem (
							"server-id",
							uem.serverId),

						Rpc.rpcElem (
							"status",
							ForwarderApiConstants.stMessageNotFound),

						Rpc.rpcElem (
							"status-code",
							"message-not-found"),

						Rpc.rpcElem (
							"status-message",
							"Message not found")));

				statusMessages.add("Message " + i + ": Message not found");
			}
		}

		RpcList reportsPart =
			Rpc.rpcList (
				"reports",
				"report",
				RpcType.rStructure);

		for (int i = 0; i < reports.size (); i++) {

			UnqueueExReport uer =
				reports.get (i);

			if (uer.fmOutReport != null) {

				reportsPart.add (

					Rpc.rpcStruct (
						"report",

						Rpc.rpcElem (
							"report-id",
							uer.reportId),

						Rpc.rpcElem (
							"status",
							Rpc.stSuccess),

						Rpc.rpcElem (
							"status-code",
							"success"),

						Rpc.rpcElem (
							"status-message",
							"Success")));

			} else {

				reportsPart.add (

					Rpc.rpcStruct (
						"report",

						Rpc.rpcElem (
							"report-id",
							uer.reportId),

						Rpc.rpcElem (
							"status",
							ForwarderApiConstants.stReportNotFound),

						Rpc.rpcElem (
							"status-code",
							"report-not-found"),

						Rpc.rpcElem (
							"status-message",
							"Report not found")));

				statusMessages.add (
					"Report " + i + ": Report not found");

			}

		}

		return new RpcResult (
			"forwarder-unqueue-ex-response",
			Rpc.stPartialSuccess,
			"partial-success",
			statusMessages,

			Rpc.rpcElem (
				"unqueueExMessages",
				messagesPart),

			Rpc.rpcElem (
				"reports",
				reportsPart));

	}

	private
	RpcResult makeCancelledResult () {

		statusMessages.add ("Cancelled due to partial failure");

		RpcList messagesPart =
			Rpc.rpcList (
				"unqueueExMessages",
				"message",
				RpcType.rStructure);

		for (int i = 0; i < unqueueExMessages.size (); i++) {

			UnqueueExMessage uem =
				unqueueExMessages.get (i);

			if (uem.forwarderMessageIn != null) {

				messagesPart.add (

					Rpc.rpcStruct (
						"message",

						Rpc.rpcElem (
							"server-id",
							uem.serverId),

						Rpc.rpcElem (
							"status",
							Rpc.stCancelled),

						Rpc.rpcElem (
							"status-code",
							"cancelled"),

						Rpc.rpcElem (
							"status-message",
							"Cancelled due to other failure")));

				statusMessages.add (
					"Message " + i + ": Cancelled due to other failure");

			} else {

				messagesPart.add (

					Rpc.rpcStruct (
						"message",

						Rpc.rpcElem (
							"server-id",
							uem.serverId),

						Rpc.rpcElem (
							"status",
							ForwarderApiConstants.stReportNotFound),

						Rpc.rpcElem (
							"status-code",
							"message-not-found"),

						Rpc.rpcElem (
							"status-message",
							"Message not found")));

				statusMessages.add (
					"Message " + i + ": Message not found");

			}
		}

		RpcList reportsPart =
			Rpc.rpcList (
				"reports",
				"report",
				RpcType.rStructure);

		for (int i = 0; i < reports.size(); i++) {
			UnqueueExReport uer = reports.get(i);

			if (uer.fmOutReport != null) {

				reportsPart.add (

					Rpc.rpcStruct (
						"report",

						Rpc.rpcElem (
							"report-id",
							uer.reportId),

						Rpc.rpcElem (
							"status",
							Rpc.stCancelled),

						Rpc.rpcElem (
							"status-code",
							"cancelled"),

						Rpc.rpcElem (
							"status-message",
							"Cancelled due to other failure")));

				statusMessages.add (
					"Report " + i + ": Cancelled due to other failure");

			} else {

				reportsPart.add (

					Rpc.rpcStruct (
						"report",

						Rpc.rpcElem (
							"report-id",
							uer.reportId),

						Rpc.rpcElem (
							"status",
							ForwarderApiConstants.stReportNotFound),

						Rpc.rpcElem (
							"status-code",
							"report-not-found"),

						Rpc.rpcElem (
							"status-message",
							"Report not found")));

				statusMessages.add (
					"Report " + i + ": Report not found");

			}

		}

		return new RpcResult (
			"forwarder-unqueue-ex-response",
			Rpc.stCancelled,
			"cancelled",
			statusMessages,
			messagesPart,
			reportsPart);

	}

	private
	RpcResult makeSuccessResult () {

		statusMessages.add ("Success");

		RpcList messagesPart =
			Rpc.rpcList (
				"unqueueExMessages",
				"message",
				RpcType.rStructure);

		for (int i = 0; i < unqueueExMessages.size (); i++) {

			UnqueueExMessage uem =
				unqueueExMessages.get (i);

			messagesPart.add (

				Rpc.rpcStruct (
					"message",

					Rpc.rpcElem (
						"server-id",
						uem.serverId),

					Rpc.rpcElem (
						"status",
						Rpc.stSuccess),

					Rpc.rpcElem (
						"status-code",
						"success"),

					Rpc.rpcElem (
						"status-message",
						"Success")));

		}

		RpcList reportsPart =
			Rpc.rpcList (
				"reports",
				"report",
				RpcType.rStructure);

		for (int i = 0; i < reports.size(); i++) {

			UnqueueExReport uer =
				reports.get (i);

			reportsPart.add (

				Rpc.rpcStruct (
					"report",

					Rpc.rpcElem (
						"report-id",
						uer.reportId),

					Rpc.rpcElem (
						"status",
						Rpc.stSuccess),

					Rpc.rpcElem (
						"status-code",
						"success"),

					Rpc.rpcElem (
						"status-message",
						"Success")));
		}

		return new RpcResult (
			"forwarder-unqueue-ex-response",
			Rpc.stSuccess,
			"success",
			statusMessages,
			messagesPart,
			reportsPart);

	}

	static
	class UnqueueExMessage {
		Long serverId;
		ForwarderMessageInRec forwarderMessageIn;
	}

	static
	class UnqueueExReport {
		Long reportId;
		ForwarderMessageOutReportRec fmOutReport;
	}

}
