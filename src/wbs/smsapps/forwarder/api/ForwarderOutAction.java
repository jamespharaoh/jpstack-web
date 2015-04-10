package wbs.smsapps.forwarder.api;

import static wbs.framework.utils.etc.Misc.isInt;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.api.mvc.ApiAction;
import wbs.platform.text.web.TextResponder;
import wbs.smsapps.forwarder.logic.ForwarderLogic;
import wbs.smsapps.forwarder.logic.ForwarderNotFoundException;
import wbs.smsapps.forwarder.logic.ForwarderSendClientIdException;
import wbs.smsapps.forwarder.logic.IncorrectPasswordException;
import wbs.smsapps.forwarder.logic.ReportableException;
import wbs.smsapps.forwarder.model.ForwarderMessageInObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec;
import wbs.smsapps.forwarder.model.ForwarderMessageOutRec;
import wbs.smsapps.forwarder.model.ForwarderRec;

@Log4j
@PrototypeComponent ("forwarderOutAction")
public
class ForwarderOutAction
	extends ApiAction {

	// dependencies

	@Inject
	Database database;

	@Inject
	ForwarderApiLogic forwarderApiLogic;

	@Inject
	ForwarderLogic forwarderLogic;

	@Inject
	ForwarderMessageInObjectHelper forwarderMessageInHelper;

	@Inject
	RequestContext requestContext;

	@Inject
	Provider<TextResponder> textResponder;

	// implementation

	@Override
	public
	Responder goApi () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		try {

			String slice = null, code = null, password = null;
			String message = null, numfrom = null, numto = null;
			String route = null;
			Integer inId = null;
			String myId = null;
			Integer pri = null;

			for (
				Map.Entry<String,List<String>> parameterEntry
					: requestContext.parameterMap ().entrySet ()
			) {

				String paramName =
					parameterEntry.getKey ();

				List<String> values =
					parameterEntry.getValue ();

				if (values.size () > 1) {

					throw new ReportableException (
						stringFormat (
							"More than one value provided for %s",
							paramName));

				}

				String value =
					values.get (0);

				if (paramName.equals ("code")) {
					code = value;

				} else if (paramName.equals ("slice")) {
					slice = value;

				} else if (paramName.equals ("password")) {
					password = value;

				} else if (paramName.equals ("route")) {
					route = value;

				} else if (paramName.equals ("message")) {

					message =
						value;

				} else if (paramName.equals ("numfrom")) {

					numfrom =
						value;

				} else if (paramName.equals ("numto")) {

					numto =
						value;

					if (! isInt (numto)) {

						throw new ReportableException (
							"Parameter numto should consist of digits only");

					}

				} else if (paramName.equals ("in_id")) {

					try {

						inId =
							Integer.parseInt (value);

					} catch (NumberFormatException exception) {

						throw new ReportableException (
							"in_id should be an integer value");

					}

					if (inId < 1)
						throw new ReportableException ("Parameter in_id is invalid");

				} else if (paramName.equals ("my_id")) {

					myId = value;

					if (myId.length () == 0)
						throw new ReportableException ("Parameter my_id should not be empty");

				} else if (paramName.equals ("pri")) {
					try {
						pri = Integer.parseInt (value);
					} catch (NumberFormatException e) {
						throw new ReportableException ("pri should be an integer value");
					}

				} else {

					throw new ReportableException ("Invalid parameter: " + paramName);

				}

			}

			if (slice == null) {

				throw new ReportableException (
					"Parameter slice must be supplied");

			}

			if (code == null) {

				throw new ReportableException (
					"Parameter code must be supplied");

			}

			if (password == null) {

				throw new ReportableException (
					"Parameter password must be supplied");

			}

			ForwarderRec forwarder;
			try {

				forwarder =
					forwarderApiLogic.lookupForwarder (
						requestContext,
						slice,
						code,
						password);

			} catch (ForwarderNotFoundException e) {
				throw new ReportableException ("Unknown forwarder code");
			} catch (IncorrectPasswordException e) {
				throw new ReportableException ("Incorrect password");
			}

			if (route == null)
				throw new ReportableException (
					"Parameter route must be supplied");

			if (message == null)
				throw new ReportableException (
					"Parameter message must be supplied");

			if (numfrom == null)
				throw new ReportableException (
					"Parameter numfrom must be supplied");

			if (numto == null)
				throw new ReportableException (
					"Parameter numto must be supplied");

			ForwarderMessageInRec forwarderMessageIn =
				null;

			if (inId != null) {

				forwarderMessageIn =
					forwarderMessageInHelper.find (
						inId);

				if (forwarderMessageIn == null
						|| forwarderMessageIn.getForwarder () != forwarder)

					throw new ReportableException (
						"Invalid in_id");

			}

			ForwarderMessageOutRec forwarderMessageOut;

			try {

				forwarderMessageOut =
					forwarderLogic.sendMessage (
						forwarder,
						forwarderMessageIn,
						message,
						null,
						numfrom,
						numto,
						route,
						myId,
						pri,
						null);

			} catch (ForwarderSendClientIdException exception) {

				// TODO log this, not an exception

				return textResponder.get ()
					.text (
						stringFormat (
							"FAIL\n",
							"IDREUSE\n",
							"This message id has already been used for a ",
								"different message"));

			}

			transaction.commit ();

			if (forwarderMessageOut == null) {

				return textResponder.get ()
					.text (
						stringFormat (
							"FAIL\n",
							"TRACKERBLOCK\n",
							"This user's number is being blocked by a report ",
								"tracker"));

			}

			return textResponder.get ()
				.text (
					stringFormat (
						"OK\n",
						"out_id=%s\n",
						forwarderMessageOut.getId ()));

		} catch (ReportableException exception) {

			log.error (
				"Error doing 'out': " + exception.getMessage ());

			for (Map.Entry<String,List<String>> entry
					: requestContext.parameterMap ().entrySet ()) {

				String name =
					entry.getKey ();

				List<String> values =
					entry.getValue ();

				for (String value : values)
					log.error ("Param " + name + ": " + value);

			}

			return textResponder.get ()
				.text (
					"ERROR\n" + exception.getMessage () + "\n");

		}

	}

}
