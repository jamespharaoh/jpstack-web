package wbs.smsapps.forwarder.api;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;

import wbs.platform.rpc.core.Rpc;
import wbs.platform.rpc.core.RpcDefinition;
import wbs.platform.rpc.core.RpcException;
import wbs.platform.rpc.core.RpcSource;
import wbs.platform.rpc.core.RpcType;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.text.web.TextResponder;

import wbs.sms.message.core.model.MessageStatus;

import wbs.smsapps.forwarder.logic.ForwarderNotFoundException;
import wbs.smsapps.forwarder.logic.IncorrectPasswordException;
import wbs.smsapps.forwarder.logic.ReportableException;
import wbs.smsapps.forwarder.model.ForwarderMessageInObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec;
import wbs.smsapps.forwarder.model.ForwarderObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderRec;

import wbs.web.context.RequestContext;
import wbs.web.responder.WebResponder;

@SingletonComponent ("forwarderApiLogic")
public
class ForwarderApiLogicImplementation
	implements ForwarderApiLogic {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ForwarderMessageInObjectHelper forwarderMessageInHelper;

	@SingletonDependency
	ForwarderObjectHelper forwarderHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <TextResponder> textResponderProvider;

	// implementation

	@Override
	public
	ForwarderRec lookupForwarder (
			@NonNull Transaction parentTransaction,
			@NonNull RequestContext requestContext,
			@NonNull String sliceCode,
			@NonNull String code,
			@NonNull String password)
		throws
			ForwarderNotFoundException,
			IncorrectPasswordException {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"lookupForwarder");

		) {

			// find the slice

			Optional <SliceRec> sliceOptional =
				sliceHelper.findByCode (
					transaction,
					GlobalId.root,
					sliceCode);

			if (
				optionalIsNotPresent (
					sliceOptional)
			) {

				throw new ForwarderNotFoundException ();

			}

			SliceRec slice =
				sliceOptional.get ();

			// find the forwarder

			Optional <ForwarderRec> forwarderOptional =
				forwarderHelper.findByCode (
					transaction,
					slice,
					code);

			if (
				optionalIsNotPresent (
					forwarderOptional)
			) {

				throw new ForwarderNotFoundException ();

			}

			ForwarderRec forwarder =
				forwarderOptional.get ();

			// check the password

			if (
				stringNotEqualSafe (
					forwarder.getPassword (),
					password)
			) {

				throw new IncorrectPasswordException ();

			}

			// return

			return forwarder;

		}

	}

	// control action get

	/**
	 * Given a forwarderId, gets and unqueues the next message, and outputs it
	 * appropriately.
	 */
	@Override
	public
	WebResponder controlActionGet (
			@NonNull Transaction parentTransaction,
			@NonNull RequestContext requestContext,
			@NonNull ForwarderRec forwarder) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"controlActionGet");

		) {

			ForwarderMessageInRec forwarderMessageIn =
				forwarderMessageInHelper.findNext (
					transaction,
					transaction.now (),
					forwarder);

			if (forwarderMessageIn == null) {

				return textResponderProvider.get ()

					.text (
						"NONE\n");

			}

			forwarderMessageIn

				.setPending (
					false)

				.setSendQueue (
					false)

				.setRetryTime (
					null)

				.setProcessedTime (
					transaction.now ());

			return textResponderProvider.get ()

				.text (
					printMessageIn (
						requestContext,
						forwarderMessageIn));

		}

	}

	// ================================= control action borrow

	/**
	 * Given a forwarderId, "borrows" the next message in the queue and outputs
	 * it appropriately.
	 */
	@Override
	public
	WebResponder controlActionBorrow (
			@NonNull Transaction parentTransaction,
			@NonNull RequestContext requestContext,
			@NonNull ForwarderRec forwarder) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"controlActionBorrow");

		) {

			ForwarderMessageInRec forwarderMessageIn =
				forwarderMessageInHelper.findNext (
					transaction,
					transaction.now (),
					forwarder);

			if (forwarderMessageIn == null) {

				return textResponderProvider.get ()
					.text ("NONE\n");

			}

			forwarderMessageIn

				.setBorrowedTime (
					transaction.now ().plus (
						Duration.standardMinutes (10)));

			return textResponderProvider.get ()
				.text (
					printMessageIn (
						requestContext,
						forwarderMessageIn));

		}

	}

	// ============================================================ control
	// action unique

	@Override
	public
	WebResponder controlActionUnqueue (
			@NonNull Transaction parentTransaction,
			@NonNull RequestContext requestContext,
			@NonNull ForwarderRec forwarder)
		throws ReportableException {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"controlActionUnqueue");

		) {

			// get the message id

			String tempString =
				requestContext.parameterOrNull (
					"id");

			if (tempString == null) {

				return textResponderProvider.get ()

					.text (
						"ERROR\nNo id supplied\n");

			}

			Long forwarderMessageInId =
				Long.parseLong (
					tempString);

			// find the message

			Optional <ForwarderMessageInRec> forwarderMessageInOptional =
				forwarderMessageInHelper.find (
					transaction,
					forwarderMessageInId);

			if (

				optionalIsNotPresent (
					forwarderMessageInOptional)

				|| referenceNotEqualWithClass (
					ForwarderRec.class,
					forwarderMessageInOptional.get ().getForwarder (),
					forwarder)

			) {

				throw new ReportableException (
					"Message not found");

			}

			ForwarderMessageInRec forwarderMessageIn =
				forwarderMessageInOptional.get ();

			// update it

			if (forwarderMessageIn.getPending ()) {

				forwarderMessageIn

					.setPending (
						false)

					.setSendQueue (
						false)

					.setRetryTime (
						null)

					.setProcessedTime (
						transaction.now ());

			}

			return textResponderProvider.get ()
				.text ("OK\n");

		}

	}

	// ============================================================ print
	// message in

	/**
	 * Retrieves from the database and outputs the specified
	 * forwarder_message_in.
	 */
	private
	String printMessageIn (
			@NonNull RequestContext requestContext,
			@NonNull ForwarderMessageInRec fmi) {

		try {

			return "MESSAGE\n"
					+ "in_id="
					+ URLEncoder.encode(Long.toString(fmi.getId()), "utf-8")
					+ "&"
					+ "numfrom="
					+ URLEncoder.encode(fmi.getMessage().getNumFrom(), "utf-8")
					+ "&"
					+ "numto="
					+ URLEncoder.encode(fmi.getMessage().getNumTo(), "utf-8")
					+ "&"
					+ "message="
					+ URLEncoder.encode(fmi.getMessage().getText().getText(),
							"utf-8") + "\n";

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	private final static
	RpcDefinition authRequestDefinition =
		Rpc.rpcDefinition ("forwarder-send-request", RpcType.rStructure,
			Rpc.rpcDefinition ("slice", RpcType.rString),
			Rpc.rpcDefinition ("forwarder", RpcType.rString),
			Rpc.rpcDefinition ("password", RpcType.rString));

	@Override
	public
	ForwarderRec rpcAuth (
			@NonNull Transaction parentTransaction,
			@NonNull RpcSource source) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"rpcAuth");

		) {

			List <String> errors =
				new ArrayList<> ();

			// check params are present

			Map <String, Object> params =
				genericCastUnchecked (
					source.obtain (
						authRequestDefinition,
						errors,
						false));

			if (errors.size () > 0) {

				throw new RpcException (
					Rpc.rpcError (
						"error-response",
						Rpc.stRequestInvalid,
						"request-invalid",
						errors));

			}

			// get them

			String sliceCode =
				(String) params.get ("slice");

			String forwarderCode =
				(String) params.get ("forwarder");

			String password =
				(String) params.get ("password");

			// lookup slice

			Optional <SliceRec> sliceOptional =
				sliceHelper.findByCode (
					transaction,
					GlobalId.root,
					sliceCode);

			if (
				optionalIsNotPresent (
					sliceOptional)
			) {

				throw new RpcException (
					Rpc.rpcError (
						"error-response",
						Rpc.stAuthError,
						"auth-error",
						"Slice, forwarder and/or password not recognised"));

			}

			SliceRec slice =
				sliceOptional.get ();

			// lookup forwarder

			Optional<ForwarderRec> forwarderOptional =
				forwarderHelper.findByCode (
					transaction,
					slice,
					forwarderCode);

			if (
				optionalIsNotPresent (
					forwarderOptional)
			) {

				throw new RpcException (
					Rpc.rpcError (
						"error-response",
						Rpc.stAuthError,
						"auth-error",
						"Slice, forwarder and/or password not recognised"));

			}

			ForwarderRec forwarder =
				forwarderOptional.get ();

			// check password

			if (
				stringNotEqualSafe (
					forwarder.getPassword (),
					password)
			) {

				throw new RpcException (
					Rpc.rpcError (
						"error-response",
						Rpc.stAuthError,
						"auth-error",
						"Slice, forwarder and/or password not recognised"));

			}

			// return

			return forwarder;

		}

	}

	@Override
	public
	ForwarderMessageStatus messageStatusMap (
			@NonNull MessageStatus messageStatus) {

		return mapItemForKeyRequired (
			messageStatusMap,
			messageStatus);

	}

	// data

	private final static
	Map <MessageStatus, ForwarderMessageStatus> messageStatusMap =
		ImmutableMap.<MessageStatus, ForwarderMessageStatus> builder ()

			.put (
				MessageStatus.blacklisted,
				ForwarderMessageStatus.undelivered)

			.put (
				MessageStatus.held,
				ForwarderMessageStatus.pending)

			.put (
				MessageStatus.pending,
				ForwarderMessageStatus.pending)

			.put (
				MessageStatus.cancelled,
				ForwarderMessageStatus.cancelled)

			.put (
				MessageStatus.failed,
				ForwarderMessageStatus.failed)

			.put (
				MessageStatus.sent,
				ForwarderMessageStatus.sent)

			.put (
				MessageStatus.delivered,
				ForwarderMessageStatus.delivered)

			.put (
				MessageStatus.undelivered,
				ForwarderMessageStatus.undelivered)

			.put (
				MessageStatus.submitted,
				ForwarderMessageStatus.sentUpstream)

			.put (
				MessageStatus.reportTimedOut,
				ForwarderMessageStatus.reportTimedOut)

			.build ();

}
