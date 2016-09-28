package wbs.platform.event.console;

import static wbs.utils.etc.NumberUtils.integerEqualSafe;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.instantToDateNullSafe;
import static wbs.utils.time.TimeUtils.millisToInstant;
import static wbs.utils.web.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowSeparatorWrite;
import static wbs.utils.web.HtmlUtils.htmlLinkWriteHtml;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.lookup.ObjectLookup;
import wbs.console.part.PagePart;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.PermanentRecord;
import wbs.framework.entity.record.Record;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.event.model.EventLinkRec;
import wbs.platform.event.model.EventRec;
import wbs.platform.event.model.EventTypeRec;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.utils.string.FormatWriter;
import wbs.utils.string.StringFormatWriter;
import wbs.utils.web.HtmlUtils;

@Log4j
@SingletonComponent ("eventConsoleLogic")
public
class EventConsoleLogicImplementation
	implements EventConsoleLogic {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@PrototypeDependency
	Provider <ObjectEventsPart> objectEventsPart;

	// implementation

	@Override
	public
	PagePart makeEventsPart (
			PermanentRecord<?> object) {

		List<Record<?>> children =
			objectManager.getMinorChildren (
				object);

		List<GlobalId> objectGlobalIds =
			new ArrayList<GlobalId> ();

		objectGlobalIds.add (
			objectManager.getGlobalId (
				object));

		for (
			Record<?> child
				: children
		) {

			objectGlobalIds.add (
				objectManager.getGlobalId (
					child));

		}

		return objectEventsPart.get ()

			.dataObjectIds (
				objectGlobalIds);

	}

	@Override
	public
	Provider<PagePart> makeEventsPartFactory (
			final ObjectLookup<?> objectLookup) {

		return new Provider<PagePart> () {

			@Override
			public
			PagePart get () {

				@Cleanup
				Transaction transaction =
					database.beginReadOnly (
						"ObjectEventsPartFactory.get ()",
						this);

				PermanentRecord<?> object =
					(PermanentRecord<?>)
					objectLookup.lookupObject (
						requestContext.contextStuff ());

				return makeEventsPart (
					object);

			}

		};

	}

	@Override
	public
	void writeEventHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull EventRec event) {

		EventTypeRec eventType =
			event.getEventType ();

		String text =
			HtmlUtils.htmlEncode (
				eventType.getDescription ());

		// TODO this is not correct and will not handle text matching "%#" in
		// the previous replacements. it should be replaced with a single pass.

		for (
			EventLinkRec eventLink
				: event.getEventLinks ()
		) {

			if (
				integerEqualSafe (
					eventLink.getTypeId (),
					EventLogic.integerEventLinkType)
			) {

				// integer

				text =
					text.replaceAll (
						"%" + eventLink.getIndex (),
						HtmlUtils.htmlEncode (
							eventLink.getRefId ().toString ()));

			} else if (
				integerEqualSafe (
					eventLink.getTypeId (),
					EventLogic.booleanEventLinkType)
			) {

				// boolean

				text =
					text.replaceAll (
						"%" + eventLink.getIndex (),
						eventLink.getRefId () != 0 ? "yes" : "no");

			} else if (
				integerEqualSafe (
					eventLink.getTypeId (),
					EventLogic.instantEventLinkType)
			) {

				// instant

				text =
					text.replaceAll (
						"%" + eventLink.getIndex (),
						userConsoleLogic.timestampWithTimezoneString (
							millisToInstant (
								eventLink.getRefId ())));

			} else {

				// locate referenced object

				Record<?> object =
					objectManager.findObject (
						new GlobalId (
							eventLink.getTypeId (),
							eventLink.getRefId ()));

				// escape replacement text, what a mess ;-)

				StringFormatWriter objectFormatWriter =
					new StringFormatWriter ();

				writeObjectAsHtml (
					objectFormatWriter,
					object);

				String replacement =
					objectFormatWriter.toString ()

					.replaceAll (
						"\\\\",
						"\\\\\\\\")

					.replaceAll (
						"\\$",
						"\\\\\\$");

				// perform replacement

				text =
					text.replaceAll (
						"%" + eventLink.getIndex (),
						replacement);

			}

		}

		formatWriter.writeString (
			text);

	}

	@Override
	public
	void writeObjectAsHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull Object object) {

		if (object instanceof Integer) {

			formatWriter.writeString (
				object.toString ());

		} else if (object instanceof TextRec) {

			TextRec text =
				(TextRec)
				object;

			formatWriter.writeFormat (
				"\"%s\"",
				text.getText ());

		} else if (object instanceof MediaRec) {

			MediaRec media =
				(MediaRec)
				object;

			htmlLinkWriteHtml (
				objectManager.localLink (
					media),
				() -> mediaConsoleLogic.writeMediaThumb32 (
					formatWriter,
					media));

		} else if (object instanceof Record) {

			Record <?> dataObject =
				(Record <?>)
				object;

			objectManager.writeHtmlForObject (
				formatWriter,
				dataObject,
				optionalAbsent (),
				false);

		} else {

			throw new IllegalArgumentException ();

		}

	}

	@Override
	public
	void writeEventsTable (
			@NonNull FormatWriter htmlWriter,
			@NonNull Iterable <EventRec> events) {

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Time",
			"Details");

		int dayNumber = 0;

		for (
			EventRec event
				: events
		) {

			Calendar calendar =
				Calendar.getInstance ();

			calendar.setTime (
				instantToDateNullSafe (
					event.getTimestamp ()));

			int newDayNumber =
				(calendar.get (Calendar.YEAR) << 9)
				+ calendar.get (Calendar.DAY_OF_YEAR);

			if (newDayNumber != dayNumber) {

				htmlTableRowSeparatorWrite ();

				htmlTableRowOpen (
					htmlStyleRuleEntry (
						"font-weight",
						"bold"));

				htmlTableCellWrite (
					userConsoleLogic.dateStringLong (
						event.getTimestamp ()),
					htmlColumnSpanAttribute (2l));

				htmlTableRowClose ();

				dayNumber =
					newDayNumber;

			}

			writeEventRow (
				htmlWriter,
				event);

		}

		htmlTableClose ();

	}

	@Override
	public
	void writeEventRow (
			@NonNull FormatWriter formatWriter,
			@NonNull EventRec event) {

		htmlTableRowOpen ();

		htmlTableCellWrite (
			userConsoleLogic.timeString (
				event.getTimestamp ()));

		try {

			StringFormatWriter eventFormatWriter =
				new StringFormatWriter ();

			writeEventHtml (
				eventFormatWriter,
				event);

			htmlTableCellWriteHtml (
				eventFormatWriter.toString ());

		} catch (RuntimeException exception) {

			log.error (
				stringFormat (
					"Error displaying event %s",
					event.getId ()),
				exception);

			htmlTableCellWrite (
				"(error displaying this event)");

		}

		htmlTableRowClose ();

	}

}
