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
import wbs.framework.record.Record;
import wbs.framework.utils.etc.Html;
import wbs.platform.event.model.EventLinkObjectHelper;
import wbs.platform.event.model.EventLinkRec;
import wbs.platform.event.model.EventRec;
import wbs.platform.event.model.EventTypeRec;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextRec;

@Accessors (fluent = true)
@PrototypeComponent ("objectEventsPart")
@Log4j
public
class ObjectEventsPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	EventLinkObjectHelper eventLinkHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

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
				eventText (
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
			Html.nonBreakingWhitespace (Html.encode (
				timeFormatter.instantToTimeString (
					timeFormatter.defaultTimezone (),
					dateToInstant (
						event.getTimestamp ())))),

			"<td>%s</td>\n",
			text,

			"</tr>\n");

	}

	String eventText (
			EventRec event) {

		EventTypeRec eventType =
			event.getEventType ();

		String text =
			Html.encode (
				eventType.getDescription ());

		for (
			EventLinkRec evLink
				: event.getEventLinks ()
		) {

			if (evLink.getTypeId () == -1) {

				// integer

				text =
					text.replaceAll (
						"%" + evLink.getIndex (),
						Html.encode (evLink.getRefId ().toString ()));

			} else if (evLink.getTypeId () == -2) {

				// boolean

				text =
					text.replaceAll (
						"%" + evLink.getIndex (),
						evLink.getRefId () != 0 ? "yes" : "no");

			} else {

				// locate referenced object

				Record<?> object =
					objectManager.findObject (
						new GlobalId (
							evLink.getTypeId (),
							evLink.getRefId ()));

				// escape replacement text, what a mess ;-)

				String replacement =
					objectToHtml (object)
						.replaceAll ("\\\\", "\\\\\\\\")
						.replaceAll ("\\$", "\\\\\\$");

				// perform replacement

				text =
					text.replaceAll (
						"%" + evLink.getIndex (),
						replacement);

			}

		}

		return text;

	}

	String objectToHtml (
			Object object) {

		if (object instanceof Integer) {

			return Html.encode (
				object.toString ());

		}

		if (object instanceof TextRec) {

			return Html.encode (
				stringFormat (
					"\"%s\"",
					((TextRec) object).getText ()));

		}

		if (object instanceof MediaRec) {

			MediaRec media =
				(MediaRec) object;

			return stringFormat (
				"<a ",
				"href=\"%h\"",
				objectManager.localLink (
					media),
				">%s</a>",
				mediaConsoleLogic.mediaThumb32 (
					media));

		}

		if (object instanceof Record) {

			Record<?> dataObject =
				(Record<?>) object;

			return objectManager.htmlForObject (
				dataObject,
				null,
				false);

		}

		if (object == null)
			return "NULL";

		throw new IllegalArgumentException ();

	}

}
