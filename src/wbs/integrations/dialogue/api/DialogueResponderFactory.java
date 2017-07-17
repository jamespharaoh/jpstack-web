package wbs.integrations.dialogue.api;

import static wbs.utils.string.StringUtils.joinWithNewline;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.responder.TextResponder;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("dialogueResponder")
public
class DialogueResponderFactory
	implements ComponentFactory <WebResponder> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <TextResponder> textResponderProvider;

	// implementation

	@Override
	public
	WebResponder makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return textResponderProvider.provide (
				taskLogger,
				textResponder ->
					textResponder

				.text (
					joinWithNewline (
						"<HTML>",
						"<!-- X-E3-Submission-Report: \"00\" -->",
						"</HTML>",
						""))

			);

		}

	}

}
