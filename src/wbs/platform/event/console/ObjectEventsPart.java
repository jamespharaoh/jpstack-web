package wbs.platform.event.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Calendar;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.misc.TimeFormatter;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.GlobalId;
import wbs.framework.utils.etc.Html;
import wbs.platform.event.model.EventLinkObjectHelper;
import wbs.platform.event.model.EventLinkRec;
import wbs.platform.event.model.EventRec;

@Accessors (fluent = true)
@PrototypeComponent ("objectEventsPart")
@Log4j
public
class ObjectEventsPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	EventConsoleLogic eventConsoleLogic;

	@Inject
	EventLinkObjectHelper eventLinkHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	TimeFormatter timeFormatter;

	// properties

	@Getter @Setter
	Collection<GlobalId> dataObjectIds;

	// state

	int dayNumber = 0;
	Set<EventRec> events;

	// implementation

	@Override
	public
	void prepare () {

		events =
			new TreeSet<EventRec> ();

		for (
			GlobalId dataObjectId
				: dataObjectIds
		) {

			Collection<EventLinkRec> eventLinks =
				eventLinkHelper.findByTypeAndRef (
					dataObjectId.typeId (),
					dataObjectId.objectId ());

			for (
				EventLinkRec eventLink
					: eventLinks
			) {

				events.add (
					eventLink.getEvent ());

			}

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"list\">");

		printFormat (
			"<tr>\n",
			"<th>Time</th>\n",
			"<th>Details</th>\n",
			"</tr>");

		for (
			EventRec event
				: events
		) {

			doEvent (
				event);

		}

		printFormat (
			"</table>\n");

	}

	void doEvent (
			EventRec event) {

		Calendar calendar =
			Calendar.getInstance ();

		calendar.setTime (
			event.getTimestamp ());

		int newDayNumber =
			(calendar.get (Calendar.YEAR) << 9)
				+ calendar.get (Calendar.DAY_OF_YEAR);

		if (newDayNumber != dayNumber) {

			printFormat (
				"<tr class=\"sep\">\n");

			printFormat (
				"<tr style=\"font-weight: bold\">\n",

				"<td colspan=\"2\">%h</td>\n",
				timeFormatter.instantToDateStringLong (
					timeFormatter.defaultTimezone (),
					dateToInstant (
						event.getTimestamp ())),

				"</tr>\n");

			dayNumber =
				newDayNumber;

		}

		String text;

		try {

			text =
				eventConsoleLogic.eventText (
					event);

		} catch (Exception exception) {

			log.error (
				stringFormat (
					"Error displaying event %s",
					event.getId ()),
				exception);

			text =
				"(error displaying this event)";

		}

		printFormat (
			"<tr>\n",

			"<td>%s</td>\n",
			Html.encodeNonBreakingWhitespace (
				timeFormatter.instantToTimeString (
					timeFormatter.defaultTimezone (),
					dateToInstant (
						event.getTimestamp ()))),

			"<td>%s</td>\n",
			text,

			"</tr>\n");

	}

}
