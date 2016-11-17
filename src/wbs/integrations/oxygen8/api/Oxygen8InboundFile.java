package wbs.integrations.oxygen8.api;

import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalValueEqualSafe;

import java.io.IOException;

import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.file.AbstractWebFile;
import wbs.web.responder.Responder;

@SingletonComponent ("oxygen8InboundFile")
public
class Oxygen8InboundFile
	extends AbstractWebFile {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <Oxygen8InboundMmsAction> oxygen8InboundMmsActionPrototype;

	@PrototypeDependency
	Provider <Oxygen8InboundSmsAction> oxygen8InboundSmsActionPrototype;

	// implementation

	@Override
	public
	void doPost (
			@NonNull TaskLogger parentTaskLogger)
		throws
			IOException,
			ServletException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doPost");

		// detect request type

		Action action;

		if (
			optionalValueEqualSafe (
				optionalFromNullable (
					requestContext.header (
						"X-Mms-Message-Type")),
				"MO_MMS")
		) {

			action =
				oxygen8InboundMmsActionPrototype.get ();

		} else {

			action =
				oxygen8InboundSmsActionPrototype.get ();

		}

		Responder responder =
			action.handle (
				taskLogger);

		responder.execute (
			taskLogger);

	}

}
