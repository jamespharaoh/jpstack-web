package wbs.platform.event.console;

import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.FormField.UpdateResult;
import wbs.console.forms.FormFieldUpdateHook;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;

@PrototypeComponent ("createNameEventFormFieldUpdateHook")
public
class NameFormFieldUpdateHook
	implements FormFieldUpdateHook <Record <?>, String, String> {

	// singeton dependencies

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// implementation

	@Override
	public
	void onUpdate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull UpdateResult <String, String> updateResult,
			@NonNull Record <?> container,
			@NonNull Record <?> linkObject,
			@NonNull Optional <Object> objectRef,
			@NonNull Optional <String> objectType) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"onUpdate");

		) {

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
					taskLogger,
					"object_name_changed_in",
					userConsoleLogic.userRequired (),
					objectRef.get (),
					objectType.get (),
					linkObject,
					updateResult.oldNativeValue ().get (),
					updateResult.newNativeValue ().get ());

				if (codeChanged) {

					eventLogic.createEvent (
						taskLogger,
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
					taskLogger,
					"object_name_changed",
					userConsoleLogic.userRequired (),
					linkObject,
					updateResult.oldNativeValue ().get (),
					updateResult.newNativeValue ().get ());

				if (codeChanged) {

					eventLogic.createEvent (
						taskLogger,
						"object_code_changed",
						userConsoleLogic.userRequired (),
						linkObject,
						oldCode,
						newCode);

				}

			}

		}

	}

}
