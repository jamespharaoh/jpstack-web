package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.module.ConsoleManager;
import wbs.framework.web.Responder;

@Accessors (fluent = true)
public abstract
class AbstractQueueConsolePlugin
	implements QueueConsolePlugin {

	@Inject
	ConsoleManager consoleManager;

	@Getter @Setter
	List<String> queueTypeCodes =
		new ArrayList<String> ();

	protected
	void queueTypeCode (
			String parentType,
			String queueType) {

		queueTypeCodes.add (
			stringFormat (
				"%s.%s",
				parentType,
				queueType));

	}

	protected
	Provider<Responder> responder (
			String responderName) {

		return consoleManager.responder (
			responderName,
			true);

	}

}
