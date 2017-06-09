package wbs.integrations.dialogue.api;

import static wbs.utils.string.StringUtils.joinWithNewline;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.text.web.TextResponder;

import wbs.web.responder.Responder;

@PrototypeComponent ("dialogueResponder")
public
class DialogueResponderFactory
	implements ComponentFactory <Responder> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// uninitialized dependencies

	@UninitializedDependency
	Provider <TextResponder> textResponderProvider;

	// implementation

	@Override
	public
	Responder makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return textResponderProvider.get ()

				.text (
					joinWithNewline (
						"<HTML>",
						"<!-- X-E3-Submission-Report: \"00\" -->",
						"</HTML>",
						""))

			;

		}

	}

}
