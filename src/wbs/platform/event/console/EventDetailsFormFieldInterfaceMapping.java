package wbs.platform.event.console;

import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ResultUtils.successResult;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.FormFieldInterfaceMapping;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.model.EventRec;

import wbs.utils.string.StringFormatWriter;

import fj.data.Either;

@PrototypeComponent ("eventDetailsFormFieldInterfaceMapping")
public
class EventDetailsFormFieldInterfaceMapping
	implements FormFieldInterfaceMapping <EventRec, EventRec, String> {

	// dependencies

	@SingletonDependency
	EventConsoleLogic eventConsoleLogic;

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull EventRec container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <EventRec> genericValue) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"genericToInterface");

		StringFormatWriter formatWriter =
			new StringFormatWriter ();

		eventConsoleLogic.writeEventHtml (
			taskLogger,
			formatWriter,
			genericValue.get ());

		return successResult (
			optionalOf (
				formatWriter.toString ()));

	}

}
