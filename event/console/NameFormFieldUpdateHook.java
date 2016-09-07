package wbs.platform.event.console;

import static wbs.framework.utils.etc.CodeUtils.simplifyToCodeRequired;
import static wbs.framework.utils.etc.StringUtils.stringNotEqualSafe;

import javax.inject.Inject;

import com.google.common.base.Optional;

import lombok.NonNull;
import wbs.console.forms.FormField.UpdateResult;
import wbs.console.forms.FormFieldUpdateHook;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.record.Record;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;

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
	UserConsoleLogic userConsoleLogic;

	// implementation

	@Override
	public
	void onUpdate (
			@NonNull UpdateResult <String, String> updateResult,
			@NonNull Record <?> container,
			@NonNull Record <?> linkObject,
			@NonNull Optional <Object> objectRef,
			@NonNull Optional <String> objectType) {

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
			stringNotEqualSafe (
				oldCode,
				newCode);

		// create an event

		if (objectRef.isPresent ()) {

			eventLogic.createEvent (
				"object_name_changed_in",
				userConsoleLogic.userRequired (),
				objectRef.get (),
				objectType.get (),
				linkObject,
				updateResult.oldNativeValue ().get (),
				updateResult.newNativeValue ().get ());

			if (codeChanged) {

				eventLogic.createEvent (
					"object_code_changed_in",
					userConsoleLogic.userRequired (),
					objectRef.get (),
					objectType.get (),
					linkObject,
					oldCode,
					newCode);

			}

		} else {

			eventLogic.createEvent (
				"object_name_changed",
				userConsoleLogic.userRequired (),
				linkObject,
				updateResult.oldNativeValue ().get (),
				updateResult.newNativeValue ().get ());

			if (codeChanged) {

				eventLogic.createEvent (
					"object_code_changed",
					userConsoleLogic.userRequired (),
					linkObject,
					oldCode,
					newCode);

			}

		}

	}

}
