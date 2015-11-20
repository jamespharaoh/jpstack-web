package wbs.smsapps.forwarder.api;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.instantToDate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.rpc.core.Rpc;
import wbs.platform.rpc.core.RpcDefinition;
import wbs.platform.rpc.core.RpcException;
import wbs.platform.rpc.core.RpcSource;
import wbs.platform.rpc.core.RpcType;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.text.web.TextResponder;
import wbs.smsapps.forwarder.logic.ForwarderNotFoundException;
import wbs.smsapps.forwarder.logic.IncorrectPasswordException;
import wbs.smsapps.forwarder.logic.ReportableException;
import wbs.smsapps.forwarder.model.ForwarderMessageInObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec;
import wbs.smsapps.forwarder.model.ForwarderObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderRec;

@SingletonComponent ("forwarderApiLogic")
public
class ForwarderApiLogicImplementation
	implements ForwarderApiLogic {

	// dependencies

	@Inject
	Database database;

	@Inject
	ForwarderMessageInObjectHelper forwarderMessageInHelper;

	@Inject
	ForwarderObjectHelper forwarderHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	// prototype dependencies

	@Inject
	Provider<TextResponder> textResponder;

	// implementation

	@Override
	public
	ForwarderRec lookupForwarder (
			RequestContext requestContext,
			String sliceCode,
			String code,
			String password)
		throws
			ForwarderNotFoundException,
			IncorrectPasswordException {

		// find the forwarder

		SliceRec slice =
			sliceHelper.findByCode (
				GlobalId.root,
				sliceCode);

		if (slice == null)
			throw new ForwarderNotFoundException ();

		ForwarderRec forwarder =
			forwarderHelper.findByCode (
				slice,
				code);

		if (forwarder == null)
			throw new ForwarderNotFoundException ();

		// check the password

		if (! forwarder.getPassword ().equals (password))
			throw new IncorrectPasswordException ();

		// return

		return forwarder;

	}

	// ============================================================ control action get

	/**
	 * Given a forwarderId, gets and unqueues the next message, and outputs it
	 * appropriately.
	 */
	@Override
	public
	Responder controlActionGet (
			RequestContext requestContext,
			ForwarderRec forwarder) {

		Transaction transaction =
			database.currentTransaction ();

		ForwarderMessageInRec forwarderMessageIn =
			forwarderMessageInHelper.findNext (
				transaction.now (),
				forwarder);

		if (forwarderMessageIn == null) {

			return textResponder.get ()

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
				instantToDate (
					transaction.now ()));

		return textResponder.get ()

			.text (
				printMessageIn (
					requestContext,
					forwarderMessageIn));

	}

	// ================================= control action borrow

	/**
	 * Given a forwarderId, "borrows" the next message in the queue and outputs
	 * it appropriately.
	 */
	@Override
	public
	Responder controlActionBorrow (
			RequestContext requestContext,
			ForwarderRec forwarder) {

		Transaction transaction =
			database.currentTransaction ();

		ForwarderMessageInRec forwarderMessageIn =
			forwarderMessageInHelper.findNext (
				transaction.now (),
				forwarder);

		if (forwarderMessageIn == null) {

			return textResponder.get ()
				.text ("NONE\n");

		}

		Calendar calendar =
			Calendar.getInstance ();

		calendar.add (
			Calendar.MINUTE,
			10);

		forwarderMessageIn.setBorrowedTime (
			calendar.getTime ());

		return textResponder.get ()
			.text (
				printMessageIn (
					requestContext,
					forwarderMessageIn));

	}

	// ============================================================ control
	// action unique

	@Override
	public
	Responder controlActionUnqueue (
			RequestContext requestContext,
			ForwarderRec forwarder)
		throws ReportableException {

		Transaction transaction =
			database.currentTransaction ();

		// get the message id

		String tempString =
			requestContext.parameter (
				"id");

		if (tempString == null) {

			return textResponder.get ()
				.text ("ERROR\nNo id supplied\n");

		}

		int forwarderMessageInId =
			Integer.parseInt (tempString);

		// find the message

		ForwarderMessageInRec forwarderMessageIn =
			forwarderMessageInHelper.find (
				forwarderMessageInId);

		if (forwarderMessageIn.getForwarder () != forwarder)
			throw new ReportableException ("Message not found");

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
					instantToDate (
						transaction.now ()));

		}

		return textResponder.get ()
			.text ("OK\n");

	}

	// ============================================================ print
	// message in

	/**
	 * Retrieves from the database and outputs the specified
	 * forwarder_message_in.
	 */
	private String printMessageIn(RequestContext requestContext, ForwarderMessageInRec fmi) {
		try {

			return "MESSAGE\n"
					+ "in_id="
					+ URLEncoder.encode(Integer.toString(fmi.getId()), "utf-8")
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
			RpcSource source) {

		List<String> errors =
			new ArrayList<String> ();

		// check params are present

		Map<String,Object> params =
			unsafeMapStringObject (
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

		SliceRec slice =
			sliceHelper.findByCode (
				GlobalId.root,
				sliceCode);

		if (slice == null) {

			throw new RpcException (
				Rpc.rpcError (
					"error-response",
					Rpc.stAuthError,
					"auth-error",
					"Slice, forwarder and/or password not recognised"));

		}

		// lookup forwarder

		ForwarderRec forwarder =
			forwarderHelper.findByCode (
				slice,
				forwarderCode);

		if (forwarder == null) {

			throw new RpcException (
				Rpc.rpcError (
					"error-response",
					Rpc.stAuthError,
					"auth-error",
					"Slice, forwarder and/or password not recognised"));

		}

		// check password

		if (! equal (
				forwarder.getPassword (),
				password)) {

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

	@Override
	@SuppressWarnings ("unchecked")
	public
	Map<String,Object> unsafeMapStringObject (
			Object input) {

		return (Map<String,Object>) input;

	}

	@Override
	@SuppressWarnings ("unchecked")
	public
	List<Map<String,Object>> unsafeListMapStringObject (
			Object input) {

		return (List<Map<String,Object>>) input;

	}

}
