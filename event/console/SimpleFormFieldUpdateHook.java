package wbs.platform.event.console;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.FormField.UpdateResult;
import wbs.console.forms.FormFieldUpdateHook;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.EventRecord;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.framework.record.RootRecord;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@Accessors (fluent = true)
@PrototypeComponent ("createUpdateEventFormFieldUpdateHook")
public
class SimpleFormFieldUpdateHook<Container extends Record<?>,Generic,Native>
	implements FormFieldUpdateHook<Container,Generic,Native> {

	// dependencies

	@Inject
	EventLogic eventLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserObjectHelper userHelper;

	// properties

	@Getter @Setter
	String name;

	// implementation

	@Override
	public
	void onUpdate (
			UpdateResult<Generic,Native> updateResult,
			Container container,
			Record<?> linkObject,
			Object objectRef,
			String objectType) {

		// lookup user

		UserRec user =
			userHelper.find (
				requestContext.userId ());

		// determine if it's an admin event

		// TODO this needs to be much better

		String adminPrefix;

		if (
			container instanceof RootRecord
			|| container instanceof MajorRecord
			|| container instanceof MinorRecord
			|| container instanceof EphemeralRecord
		) {

			adminPrefix = "admin_";

		} else if (
			container instanceof CommonRecord
			|| container instanceof EventRecord
		) {

			adminPrefix = "";

		} else {

			throw new RuntimeException ();

		}

		// create an event

		if (objectRef != null) {

			if (updateResult.newNativeValue () != null) {

				eventLogic.createEvent (
					adminPrefix + "object_field_updated_in",
					user,
					name (),
					objectRef,
					objectType,
					linkObject,
					updateResult.newNativeValue ());

			} else {

				eventLogic.createEvent (
					adminPrefix + "object_field_nulled_in",
					user,
					name (),
					objectRef,
					objectType,
					linkObject);

			}

		} else {

			if (updateResult.newNativeValue () != null) {

				eventLogic.createEvent (
					adminPrefix + "object_field_updated",
					user,
					name (),
					linkObject,
					updateResult.newNativeValue ());

			} else {

				eventLogic.createEvent (
					adminPrefix + "object_field_nulled",
					user,
					name (),
					linkObject);

			}

		}

	}

}
