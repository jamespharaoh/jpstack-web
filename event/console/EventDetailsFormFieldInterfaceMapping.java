package wbs.platform.event.console;

import static wbs.utils.etc.ResultUtils.successResultPresent;
import static wbs.utils.string.FormatWriterUtils.formatWriterConsumerToString;

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

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"genericToInterface");

		) {

			return successResultPresent (
				formatWriterConsumerToString (
					formatWriter ->
						eventConsoleLogic.writeEventHtml (
							taskLogger,
							formatWriter,
							genericValue.get ())));

		}

	}

}
