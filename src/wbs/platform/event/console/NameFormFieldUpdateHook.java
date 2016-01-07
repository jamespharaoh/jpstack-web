package wbs.platform.event.console;

import static wbs.framework.utils.etc.CodeUtils.simplifyToCodeRequired;
import static wbs.framework.utils.etc.Misc.notEqual;

import javax.inject.Inject;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.console.forms.FormField.UpdateResult;
import wbs.console.forms.FormFieldUpdateHook;
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
			@NonNull UpdateResult<String,String> updateResult,
			@NonNull Record<?> container,
			@NonNull Record<?> linkObject,
			@NonNull Optional<Object> objectRef,
			@NonNull Optional<String> objectType) {

		UserRec user =
			userHelper.find (
				requestContext.userId ());

		// don't create event on initial creation

		if (! updateResult.oldNativeValue ().isPresent ()) {
			return;
		}

		// derive codes

		String oldCode =
			simplifyToCodeRequired (
				updateResult.oldNativeValue ().get ());

		String newCode =
			simplifyToCodeRequired (
				updateResult.newNativeValue ().get ());

		boolean codeChanged =
			notEqual (
				oldCode,
				newCode);

		// create an event

		if (objectRef.isPresent ()) {

			eventLogic.createEvent (
				"object_name_changed_in",
				user,
				objectRef.get (),
				objectType.get (),
				linkObject,
				updateResult.oldNativeValue ().get (),
				updateResult.newNativeValue ().get ());

			if (codeChanged) {

				eventLogic.createEvent (
					"object_code_changed_in",
					user,
					objectRef.get (),
					objectType.get (),
					linkObject,
					oldCode,
					newCode);

			}

		} else {

			eventLogic.createEvent (
				"object_name_changed",
				user,
				linkObject,
				updateResult.oldNativeValue ().get (),
				updateResult.newNativeValue ().get ());

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
