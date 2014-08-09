package wbs.psychic.help.console;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.Responder;
import wbs.platform.console.context.ConsoleContext;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.queue.console.AbstractQueueConsolePlugin;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.psychic.core.model.PsychicRec;
import wbs.psychic.core.model.PsychicSettingsRec;

@PrototypeComponent ("psychicHelpQueueConsolePlugin")
public
class PsychicHelpQueueConsolePlugin
	extends AbstractQueueConsolePlugin {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleManager consoleManager;

	// details

	{

		queueTypeCode (
			"psychic",
			"help");

	}

	// implementation

	@Override
	public
	Responder makeResponder (
			QueueItemRec queueItem) {

		QueueSubjectRec queueSubject =
			queueItem.getQueueSubject ();

		ConsoleContext targetContext =
			consoleManager.context (
				"psychicUser",
				true);

		consoleManager.changeContext (
			targetContext,
			"/" + queueSubject.getObjectId ());

		return responder ("psychicHelpFormResponder")
			.get ();

	}

	@Override
	public
	long preferredUserDelay (
			QueueRec queue) {

		PsychicRec psychic =
			(PsychicRec) (Object)
			objectManager.getParent (
				queue);

		PsychicSettingsRec settings =
			psychic.getSettings ();

		return settings.getHelpPreferredQueueTime () * 1000L;

	}

}
