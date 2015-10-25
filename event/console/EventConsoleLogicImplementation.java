package wbs.platform.event.console;

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
import wbs.platform.event.model.EventLinkRec;
import wbs.platform.event.model.EventRec;
import wbs.platform.event.model.EventTypeRec;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextRec;

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
			objectManager.getGlobalId (object));

		for (Record<?> child : children)
			objectGlobalIds.add (
				objectManager.getGlobalId (child));

		return objectEventsPart.get ()
			.dataObjectIds (objectGlobalIds);

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
