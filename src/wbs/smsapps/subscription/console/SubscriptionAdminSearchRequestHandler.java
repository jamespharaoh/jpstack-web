package wbs.smsapps.subscription.console;

import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.nullIfEmptyString;
import static wbs.framework.utils.etc.Misc.parseTimeAfter;
import static wbs.framework.utils.etc.Misc.parseTimeBefore;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleRequestHandler;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.etc.TimeFormatException;
import wbs.framework.web.Responder;
import wbs.smsapps.subscription.model.SubscriptionSubObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionSubRec;

@Accessors (fluent = true)
@PrototypeComponent ("subscriptionAdminSearchRequestHandler")
public
class SubscriptionAdminSearchRequestHandler
	extends ConsoleRequestHandler {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	SubscriptionSubObjectHelper subscriptionSubHelper;

	@Getter @Setter
	Provider<Responder> searchResponder;

	@Getter @Setter
	Provider<Responder> searchResultsResponder;

	@Getter @Setter
	Provider<Responder> searchNumbersResponder;

	@Override
	public
	void handle ()
		throws IOException {

		// save search parameters in session

		String numberString =
			nullIfEmptyString (
				requestContext.parameter ("number"));

		String activeDateString =
			nullIfEmptyString (
				requestContext.parameter ("active_date"));

		String startedFromString =
			nullIfEmptyString (
				requestContext.parameter ("started_from"));

		String startedToString =
			nullIfEmptyString (
				requestContext.parameter ("started_to"));

		String endedFromString =
			nullIfEmptyString (
				requestContext.parameter ("ended_from"));

		String endedToString =
			nullIfEmptyString (
				requestContext.parameter ("ended_to"));

		String formatString =
			nullIfEmptyString (
				requestContext.parameter ("format"));

		requestContext.session (
			"subscription_sub_search_number",
			numberString);

		requestContext.session (
			"subscription_sub_search_active_date",
			activeDateString);

		requestContext.session (
			"subscription_sub_search_started_from",
			startedFromString);

		requestContext.session (
			"subscription_sub_search_started_to",
			startedToString);

		requestContext.session (
			"subscription_sub_search_ended_from",
			endedFromString);

		requestContext.session (
			"subscription_sub_search_ended_to",
			endedToString);

		requestContext.session (
			"subscriptionAdminSearchFormat",
			formatString);

		// work out our dates

		Date activeDateFrom = null;
		Date activeDateTo = null;

		Date startedFrom = null;
		Date startedTo = null;

		Date endedFrom = null;
		Date endedTo = null;

		try {

			if (activeDateString != null) {

				activeDateFrom =
					instantToDate (
						parseTimeAfter (
							activeDateString));

				activeDateTo =
					instantToDate (
						parseTimeBefore (
							activeDateString));

			}

			if (startedFromString != null) {

				startedFrom =
					instantToDate (
						parseTimeAfter (
							startedFromString));

			}

			if (startedToString != null) {

				startedTo =
					instantToDate (
						parseTimeBefore (
							startedToString));

			}

			if (endedFromString != null) {

				endedFrom =
					instantToDate (
						parseTimeAfter (
							endedFromString));

			}

			if (endedToString != null) {

				endedTo =
					instantToDate (
						parseTimeBefore (
							endedToString));

			}

		} catch (TimeFormatException exception) {

			requestContext.addError (
				"Invalid date/time");

			searchResponder
				.get ()
				.execute ();

			return;

		}

		Map<String,Object> searchMap =
			new HashMap<String,Object> ();

		if (numberString != null)
			searchMap.put ("number", numberString);

		if (activeDateFrom != null)
			searchMap.put ("endedAfter", activeDateFrom);

		if (activeDateTo != null)
			searchMap.put ("startedBefore", activeDateTo);

		if (startedFrom != null)
			searchMap.put ("startedAfter", startedFrom);

		if (startedTo != null)
			searchMap.put ("startedBefore", startedTo);

		if (endedFrom != null)
			searchMap.put ("endedAfter", endedFrom);

		if (endedTo != null)
			searchMap.put ("endedBefore", endedTo);

		List<SubscriptionSubRec> subscriptionSubs;

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		searchMap.put (
			"subscriptionId",
			requestContext.stuffInt ("subscriptionId"));

		Integer subscriptionAffiliateId =
			requestContext.stuffInt ("subscriptionAffiliateId");

		if (subscriptionAffiliateId != null) {

			searchMap.put (
				"subscriptionAffiliateId",
				subscriptionAffiliateId);

		}

		subscriptionSubs =
			subscriptionSubHelper.search (
				searchMap);

		if (subscriptionSubs.size () == 0) {

			requestContext.addError (
				"Search produced no results");

			searchResponder
				.get ()
				.execute ();

			return;

		}

		requestContext.request (
			"subscription_search_results",
			subscriptionSubs);

		requestContext.addNotice (
			stringFormat (
				"Found %s subs",
				subscriptionSubs.size ()));

		if (formatString.equals ("downloadNumbers")) {

			searchNumbersResponder
				.get ()
				.execute ();

		} else {

			searchResultsResponder
				.get ()
				.execute ();

		}

	}

}
