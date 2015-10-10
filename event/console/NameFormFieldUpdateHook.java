package wbs.platform.event.console;

import static wbs.framework.utils.etc.Misc.codify;
import static wbs.framework.utils.etc.Misc.equal;

import javax.inject.Inject;

import wbs.console.forms.FormFieldUpdateHook;
import wbs.console.forms.FormField.UpdateResult;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("createNameEventFormFieldUpdateHook")
public
class NameFormFieldUpdateHook
	implements FormFieldUpdateHook<Record<?>,String,String> {

	// dependencies

	@Inject
	EventLogic eventLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserObjectHelper userHelper;

	// implementation

	@Override
	public
	void onUpdate (
			UpdateResult<String,String> updateResult,
			Record<?> container,
			Record<?> linkObject,
			Object objectRef,
			String objectType) {

		UserRec user =
			userHelper.find (
				requestContext.userId ());

		// don't create event on initial creation

		if (updateResult.oldNativeValue () == null)
			return;

		// derive codes

		String oldCode =
			codify (updateResult.oldNativeValue ());

		String newCode =
			codify (updateResult.newNativeValue ());

		boolean codeChanged =
			! equal (
				oldCode,
				newCode);

		// create an event

		if (objectRef != null) {

			eventLogic.createEvent (
				"object_name_changed_in",
				user,
				objectRef,
				objectType,
				linkObject,
				updateResult.oldNativeValue (),
				updateResult.newNativeValue ());

			if (codeChanged) {

				eventLogic.createEvent (
					"object_code_changed_in",
					user,
					objectRef,
					objectType,
					linkObject,
					oldCode,
					newCode);

			}

		} else {

			eventLogic.createEvent (
				"object_name_changed",
				user,
				linkObject,
				updateResult.oldNativeValue (),
				updateResult.newNativeValue ());

			if (codeChanged) {

				eventLogic.createEvent (
					"object_code_changed",
					user,
					linkObject,
					oldCode,
					newCode);

			}

		}

	}

}
