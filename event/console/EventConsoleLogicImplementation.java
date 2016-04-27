package wbs.platform.event.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.millisToInstant;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.lookup.ObjectLookup;
import wbs.console.part.PagePart;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
import wbs.framework.record.PermanentRecord;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.Html;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.event.model.EventLinkRec;
import wbs.platform.event.model.EventRec;
import wbs.platform.event.model.EventTypeRec;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.console.UserConsoleLogic;

@SingletonComponent ("eventConsoleLogic")
public
class EventConsoleLogicImplementation
	implements EventConsoleLogic {

	// dependencies

	@Inject
	Database database;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@Inject
	Provider<ObjectEventsPart> objectEventsPart;

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
	String eventText (
			EventRec event) {

		EventTypeRec eventType =
			event.getEventType ();

		String text =
			Html.encode (
				eventType.getDescription ());

		// TODO this is not correct and will not handle text matching "%#" in
		// the previous replacements. it should be replaced with a single pass.

		for (
			EventLinkRec eventLink
				: event.getEventLinks ()
		) {

			if (
				equal (
					eventLink.getTypeId (),
					EventLogic.integerEventLinkType)
			) {

				// integer

				text =
					text.replaceAll (
						"%" + eventLink.getIndex (),
						Html.encode (
							eventLink.getRefId ().toString ()));

			} else if (
				equal (
					eventLink.getTypeId (),
					EventLogic.booleanEventLinkType)
			) {

				// boolean

				text =
					text.replaceAll (
						"%" + eventLink.getIndex (),
						eventLink.getRefId () != 0 ? "yes" : "no");

			} else if (
				equal (
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
							(int) (long)
							eventLink.getRefId ()));

				// escape replacement text, what a mess ;-)

				String replacement =
					objectToHtml (
						object)

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

		return text;

	}

	@Override
	public
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
