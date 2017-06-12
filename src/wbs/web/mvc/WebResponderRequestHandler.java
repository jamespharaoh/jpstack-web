package wbs.web.mvc;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.responder.WebResponder;

@Accessors (fluent = true)
@PrototypeComponent ("webResponderRequestHandler")
public
class WebResponderRequestHandler
	implements WebRequestHandler {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	Provider <? extends WebResponder> responderProvider;

	// public implementation

	@Override
	public
	void handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handle");

		) {

			WebResponder responder =
				responderProvider.get ();

			responder.execute (
				taskLogger);

		}

	}

}
