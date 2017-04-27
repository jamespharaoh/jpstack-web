package wbs.smsapps.forwarder.api;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.Misc.isInt;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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

import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

@PrototypeComponent ("forwarderOutAction")
public
class ForwarderOutAction
	extends ApiAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ForwarderApiLogic forwarderApiLogic;

	@SingletonDependency
	ForwarderLogic forwarderLogic;

	@SingletonDependency
	ForwarderMessageInObjectHelper forwarderMessageInHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <TextResponder> textResponderProvider;

	// implementation

	@Override
	public
	Responder goApi (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"goApi");

		) {

			try (

				OwnedTransaction transaction =
					database.beginReadWrite (
						taskLogger,
						"ForwarderOutAction.goApi ()",
						this);

			) {

				String slice = null, code = null, password = null;
				String message = null, numfrom = null, numto = null;
				String route = null;
				Long inId = null;
				String myId = null;
				Long pri = null;

				for (
					Map.Entry <String, List <String>> parameterEntry
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
								Long.parseLong (
									value);

						} catch (NumberFormatException exception) {

							throw new ReportableException (
								"in_id should be an integer value");

						}

						if (inId < 1) {

							throw new ReportableException (
								"Parameter in_id is invalid");

						}

					} else if (paramName.equals ("my_id")) {

						myId = value;

						if (myId.length () == 0) {

							throw new ReportableException (
								"Parameter my_id should not be empty");

						}

					} else if (paramName.equals ("pri")) {

						try {

							pri = Long.parseLong (value);

						} catch (NumberFormatException e) {

							throw new ReportableException (
								"pri should be an integer value");

						}

					} else {

						throw new ReportableException (
							"Invalid parameter: " + paramName);

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

				Optional <ForwarderMessageInRec> forwarderMessageInOptional;

				if (
					isNotNull (
						inId)
				) {

					forwarderMessageInOptional =
						forwarderMessageInHelper.find (
							inId);

					if (

						optionalIsNotPresent (
							forwarderMessageInOptional)

						|| referenceNotEqualWithClass (
							ForwarderRec.class,
							forwarderMessageInOptional.get ().getForwarder (),
							forwarder)

					) {

						throw new ReportableException (
							"Invalid in_id");

					}

				} else {

					forwarderMessageInOptional =
						Optional.absent ();

				}

				ForwarderMessageOutRec forwarderMessageOut;

				try {

					forwarderMessageOut =
						forwarderLogic.sendMessage (
							taskLogger,
							forwarder,
							forwarderMessageInOptional.orNull (),
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

					return textResponderProvider.get ()
						.text (
							stringFormat (
								"FAIL\n",
								"IDREUSE\n",
								"This message id has already been used for a ",
									"different message"));

				}

				transaction.commit ();

				if (forwarderMessageOut == null) {

					return textResponderProvider.get ()
						.text (
							stringFormat (
								"FAIL\n",
								"TRACKERBLOCK\n",
								"This user's number is being blocked by a report ",
									"tracker"));

				}

				return textResponderProvider.get ()

					.text (
						stringFormat (
							"OK\n",
							"out_id=%s\n",
							integerToDecimalString (
								forwarderMessageOut.getId ())));

			} catch (ReportableException exception) {

				taskLogger.errorFormatException (
					exception,
					"Error doing 'out'");

				for (
					Map.Entry<String,List<String>> entry
						: requestContext.parameterMap ().entrySet ()
				) {

					String name =
						entry.getKey ();

					List <String> values =
						entry.getValue ();

					for (
						String value
							: values
					) {

						taskLogger.debugFormat (
							"Param %s: %s",
							name,
							value);

					}

				}

				return textResponderProvider.get ()
					.text (
						"ERROR\n" + exception.getMessage () + "\n");

			}

		}

	}

}
