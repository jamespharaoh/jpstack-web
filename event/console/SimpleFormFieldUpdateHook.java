package wbs.platform.event.console;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.forms.FormFieldUpdateHook;
import wbs.console.forms.FormField.UpdateResult;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
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

		// create an event

		if (objectRef != null) {

			if (updateResult.newNativeValue () != null) {

				eventLogic.createEvent (
					"object_field_updated_in",
					user,
					name (),
					objectRef,
					objectType,
					linkObject,
					updateResult.newNativeValue ());

			} else {

				eventLogic.createEvent (
					"object_field_nulled_in",
					user,
					name (),
					objectRef,
					objectType,
					linkObject);

			}

		} else {

			if (updateResult.newNativeValue () != null) {

				eventLogic.createEvent (
					"object_field_updated",
					user,
					name (),
					linkObject,
					updateResult.newNativeValue ());

			} else {

				eventLogic.createEvent (
					"object_field_nulled",
					user,
					name (),
					linkObject);

			}

		}

	}

}
