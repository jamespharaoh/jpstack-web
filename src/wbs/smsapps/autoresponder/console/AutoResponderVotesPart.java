package wbs.smsapps.autoresponder.console;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.simplify;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenLayout;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageSearch;
import wbs.smsapps.autoresponder.model.AutoResponderObjectHelper;
import wbs.smsapps.autoresponder.model.AutoResponderRec;
import wbs.utils.time.IntervalFormatter;
import wbs.utils.time.TextualInterval;

@Log4j
@PrototypeComponent ("autoResponderVotesPart")
public
class AutoResponderVotesPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	AutoResponderObjectHelper autoResponderHelper;

	@SingletonDependency
	IntervalFormatter intervalFormatter;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	String timePeriodString;
	Map <String, Long> votes;

	// implementation

	@Override
	public
	void prepare () {

		// check units

		timePeriodString =
			requestContext.parameterOrDefault (
				"timePeriod",
				"12 hours");

		Long timePeriodSeconds =
			intervalFormatter.parseIntervalStringSecondsRequired (
				timePeriodString);

		if (timePeriodSeconds == null) {

			requestContext.addError (
				"Invalid time period");

			return;

		}

		// lookup objects

		AutoResponderRec autoResponder =
			autoResponderHelper.findRequired (
				requestContext.stuffInteger (
					"autoResponderId"));

		ServiceRec autoResponderService =
			serviceHelper.findByCodeRequired (
				autoResponder,
				"default");

		// workout start time

		Instant startTime =
			transaction.now ().minus (
				Duration.standardSeconds (
					timePeriodSeconds));

		log.info (
			stringFormat (
				"Searching from %s",
				startTime));

		// retrieve messages

		MessageSearch messageSearch =
			new MessageSearch ()

			.serviceId (
				autoResponderService.getId ())

			.createdTime (
				TextualInterval.after (
					userConsoleLogic.timezone (),
					startTime))

			.direction (
				MessageDirection.in);

		List <MessageRec> messages =
			messageHelper.search (
				messageSearch);

		// now aggregate them

		votes =
			new TreeMap <String, Long> ();

		for (
			MessageRec message
				: messages
		) {

			String body =
				simplify (
					message.getText ().getText ());

			Long oldVal =
				ifNull (
					votes.get (body),
					0l);

			votes.put (
				body,
				oldVal + 1);

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlFormOpenGet ();

		// time period

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"Time period<br>");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"text\"",
			" name=\"timePeriod\"",
			" value=\"%h\"",
			timePeriodString,
			">");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"ok\"",
			">");

		htmlParagraphClose ();

		htmlFormClose ();

		// votes

		if (votes == null)
			return;

		htmlHeadingTwoWrite (
			"Vote summary");

		htmlTableOpenLayout ();

		htmlTableHeaderRowWrite (
			"Content",
			"Votes");

		for (
			Map.Entry <String, Long> entry
				: votes.entrySet ()
		) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				entry.getKey ());

			htmlTableCellWrite (
				integerToDecimalString (
					entry.getValue ()));

		}

		htmlTableClose ();

	}

	private void htmlFormOpenGet () {

		// TODO Auto-generated method stub

	}

}
