package wbs.platform.event.console;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

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
	String fieldName;

	// implementation

	@Override
	public
	void onUpdate (
			@NonNull UpdateResult<Generic,Native> updateResult,
			@NonNull Container container,
			@NonNull Record<?> linkObject,
			@NonNull Optional<Object> objectRef,
			@NonNull Optional<String> objectType) {

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

		if (objectRef.isPresent ()) {

			if (updateResult.newNativeValue ().isPresent ()) {

				eventLogic.createEvent (
					adminPrefix + "object_field_updated_in",
					user,
					fieldName,
					objectRef.get (),
					objectType.get (),
					linkObject,
					updateResult.newNativeValue ().get ());

			} else {

				eventLogic.createEvent (
					adminPrefix + "object_field_nulled_in",
					user,
					fieldName,
					objectRef.get (),
					objectType.get (),
					linkObject);

			}

		} else {

			if (updateResult.newNativeValue ().isPresent ()) {

				eventLogic.createEvent (
					adminPrefix + "object_field_updated",
					user,
					fieldName,
					linkObject,
					updateResult.newNativeValue ().get ());

			} else {

				eventLogic.createEvent (
					adminPrefix + "object_field_nulled",
					user,
					fieldName,
					linkObject);

			}

		}

	}

}
