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
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.event.model.EventLinkObjectHelper;
import wbs.platform.event.model.EventLinkRec;
import wbs.platform.event.model.EventRec;
import wbs.platform.text.model.TextRec;

@Accessors (fluent = true)
@PrototypeComponent ("objectEventsPart")
public
class ObjectEventsPart
	extends AbstractPagePart {

	// dependencies

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

	Set<EventRec> events;

	@Override
	public
	void prepare () {

		events =
			new TreeSet<EventRec> ();

		for (GlobalId dataObjectId
				: dataObjectIds) {

			Collection<EventLinkRec> eventLinks =
				eventLinkHelper.findByTypeAndRef (
					dataObjectId.typeId (),
					dataObjectId.objectId ());

			for (EventLinkRec eventLink
					: eventLinks) {

				events.add (
					eventLink.getEvent ());

			}

		}

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"list\">");

		printFormat (
			"<tr>\n",
			"<th>Time</th>\n",
			"<th>Details</th>\n",
			"</tr>");

		int dayNumber = 0;

		Calendar cal =
			Calendar.getInstance ();

		for (EventRec event
				: events) {

			cal.setTime (
				event.getTimestamp ());

			int newDayNumber =
				(cal.get (Calendar.YEAR) << 9)
					+ cal.get (Calendar.DAY_OF_YEAR);

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

			String text =
				Html.encode (event.getEventType ().getDescription ());

			for (EventLinkRec evLink
					: event.getEventLinks ().values ()) {

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

			printFormat (
				"<tr>\n",

				"<td>%s</td>\n",
				Html.nbsp (Html.encode (
					timeFormatter.instantToTimeString (
						timeFormatter.defaultTimezone (),
						dateToInstant (
							event.getTimestamp ())))),

				"<td>%s</td>\n",
				text,

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

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
