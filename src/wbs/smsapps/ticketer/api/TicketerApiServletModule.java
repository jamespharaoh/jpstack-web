package wbs.smsapps.ticketer.api;

import static wbs.framework.utils.etc.Misc.emptyStringIfNull;
import static wbs.framework.utils.etc.Misc.equal;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
import wbs.framework.web.Action;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;
import wbs.platform.api.ApiFile;
import wbs.platform.api.StringMapResponderFactory;
import wbs.platform.exception.logic.ExceptionLogic;
import wbs.platform.rpc.php.PhpStringMapResponderFactory;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.ticketer.model.TicketerObjectHelper;
import wbs.smsapps.ticketer.model.TicketerRec;
import wbs.smsapps.ticketer.model.TicketerTicketObjectHelper;
import wbs.smsapps.ticketer.model.TicketerTicketRec;

import com.google.common.collect.ImmutableMap;

@SingletonComponent ("ticketerApiServletModule")
public
class TicketerApiServletModule
	implements ServletModule {

	@Inject
	RequestContext requestContext;

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	Database database;

	@Inject
	NumberObjectHelper numberHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	TicketerObjectHelper ticketerHelper;

	@Inject
	TicketerTicketObjectHelper ticketerTicketHelper;

	@Inject
	Provider<ApiFile> apiFile;

	// ================================= servlet module

	@Override
	public
	Map<String,PathHandler> paths () {
		return null;
	}

	@Override
	public
	Map<String,WebFile> files () {

		return ImmutableMap.<String,WebFile>builder ()

			.put ("/ticketer/query/php",
				apiFile.get ()
					.getAction (queryPhpAction)
					.postAction (queryPhpAction))

			.build ();

	}

	// ================================= actions

	Action queryPhpAction =
		new QueryAction (
			new PhpStringMapResponderFactory ());

	// ================================= query action

	public static final
	int stRequestInvalid = 0x01;

	public static final
	int stTicketerInvalid = 0x02;

	public static final
	int stTicketInvalid = 0x10;

	public static final
	int stTicketExpired = 0x11;

	public static final
	int stTicketValidPermanently = 0x20;

	public static final
	int stTicketValidTemporary = 0x21;

	public static final
	int stInternalError = 0xff;

	class QueryAction
		implements Action {

		StringMapResponderFactory responderFactory;

		QueryAction (
				StringMapResponderFactory newResponderFactory) {

			responderFactory =
				newResponderFactory;

		}

		Map<String,Object> makeError (
				int status,
				String statusCode,
				String message) {

			return ImmutableMap.<String,Object>builder ()
				.put ("status", status)
				.put ("status-code", statusCode)
				.put ("valid", false)
				.put ("message", message)
				.build ();

		}

		public
		Map<String,Object> myGo (
				RequestContext requestContext) {

			String sliceParam =
				requestContext.parameter ("slice");

			String codeParam =
				requestContext.parameter ("code");

			String numberParam =
				requestContext.parameter ("number");

			String ticketParam =
				requestContext.parameter ("ticket");

			if (
				equal (
					emptyStringIfNull (sliceParam),
					"")
			) {

				return makeError (
					stRequestInvalid,
					"request-invalid",
					"Param slice must be supplied");

			}

			if (
				equal (
					emptyStringIfNull (codeParam),
					"")
			) {

				return makeError (
					stRequestInvalid,
					"request-invalid",
					"Param code must be supplied");

			}

			if (
				equal (
					emptyStringIfNull (numberParam),
					"")
			) {

				return makeError (
					stRequestInvalid,
					"request-invalid",
					"Param number must be supplied");

			}

			if (
				equal (
					emptyStringIfNull (ticketParam),
					"")
			) {

				return makeError (
					stRequestInvalid,
					"request-invalid",
					"Param ticket must be supplied");

			}

			@Cleanup
			Transaction transaction =
				database.beginReadWrite ();

			SliceRec slice =
				sliceHelper.findByCode (
					GlobalId.root,
					sliceParam);

			if (slice == null) {

				return makeError (
					stTicketerInvalid,
					"ticketer-invalid",
					"Params slice/code are not valid\n");

			}

			TicketerRec ticketer =
				ticketerHelper.findByCode (
					slice,
					codeParam);

			if (ticketer == null) {

				return makeError (
					stTicketerInvalid,
					"ticketer-invalid",
					"Params slice/code are not valid\n");

			}

			NumberRec number =
				numberHelper.findByCode (
					GlobalId.root,
					numberParam);

			if (number == null) {

				return makeError (
					stTicketInvalid,
					"ticket-invalid",
					"Ticket is not valid");

			}

			TicketerTicketRec ticket =
				ticketerTicketHelper.findByTicket (
					ticketer,
					number,
					ticketParam);

			if (ticket == null) {

				return makeError (
					stTicketInvalid,
					"ticket-invalid",
					"Ticket is not valid");

			}

			if (ticket.getRetrievedTime () == null) {

				// new ticket

				Calendar calendar =
					Calendar.getInstance ();

				Date now = calendar.getTime();
				calendar.add(Calendar.SECOND, ticketer.getDuration());
				Date then = calendar.getTime();
				ticket.setRetrievedTime(now);
				ticket.setExpiresTime(then);
				transaction.commit();

				return ImmutableMap.<String,Object>builder ()
					.put ("status", stTicketValidTemporary)
					.put ("status-code", "ticket-valid-temporary")
					.put ("valid", true)
					.put ("message", "Ticket is valid for "
							+ ticketer.getDuration() + " seconds")
					.put ("time-left", ticketer.getDuration ())
					.build ();

			} else {

				// old ticket

				Date now = new Date ();

				long remaining = (
						ticket.getExpiresTime ().getTime ()
							- now.getTime ()
					) / 1000;

				transaction.commit ();

				if (remaining <= 0) {

					return ImmutableMap.<String,Object>builder ()
						.put ("status", stTicketExpired)
						.put ("status-code", "ticket-expired")
						.put ("valid", false)
						.put ("message", "Ticket has expired")
						.build ();

				}

				return ImmutableMap.<String,Object>builder ()
					.put ("status", stTicketValidPermanently)
					.put ("status-code", "ticket-valid-permanently")
					.put ("valid", true)
					.put ("message", "Ticket is valid permanently")
					.build ();

			}

		}

		@Override
		public
		Responder go () {

			try {

				Map<String,Object> map =
					myGo (requestContext);

				return responderFactory.makeResponder (
					map);

			} catch (RuntimeException exception) {

				exceptionLogic.logThrowable (
					"webapi",
					requestContext.requestUri (),
					exception,
					null,
					false);

				requestContext.status (500);

				Map<String,Object> map =
					ImmutableMap.<String,Object>builder ()
						.put ("status", stInternalError)
						.put ("status-code", "internal-error")
						.put ("valid", false)
						.put ("message", "An internal error has occurred")
						.build ();

				return responderFactory.makeResponder (map);

			}

		}

	}

}
