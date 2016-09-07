package wbs.platform.event.console;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.record.GlobalId;
import wbs.platform.event.model.EventLinkObjectHelper;
import wbs.platform.event.model.EventLinkRec;
import wbs.platform.event.model.EventRec;
import wbs.platform.user.console.UserConsoleLogic;

@Accessors (fluent = true)
@PrototypeComponent ("objectEventsPart")
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
	UserConsoleLogic userConsoleLogic;

	// properties

	@Getter @Setter
	Collection<GlobalId> dataObjectIds;

	// state

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

		eventConsoleLogic.renderEventsTable (
			formatWriter,
			events);

	}

}
