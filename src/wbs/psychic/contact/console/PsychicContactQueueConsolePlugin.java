package wbs.psychic.contact.console;

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
import wbs.psychic.affiliategroup.model.PsychicAffiliateGroupRec;
import wbs.psychic.core.model.PsychicRec;
import wbs.psychic.core.model.PsychicSettingsRec;

@PrototypeComponent ("psychicContactQueueConsolePlugin")
public
class PsychicContactQueueConsolePlugin
	extends AbstractQueueConsolePlugin {

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleManager consoleManager;

	{

		queueTypeCode (
			"psychic_affiliate_group",
			"request");

	}

	@Override
	public
	Responder makeResponder (
			QueueItemRec queueItem) {

		QueueSubjectRec queueSubject =
			queueItem.getQueueSubject ();

		ConsoleContext targetContext =
			consoleManager.context (
				"psychicContact",
				true);

		consoleManager.changeContext (
			targetContext,
			"/" + queueSubject.getObjectId ());

		return responder ("psychicContactFormResponder")
			.get ();

	}

	@Override
	public
	long preferredUserDelay (
			QueueRec queue) {

		PsychicAffiliateGroupRec psychicAffiliateGroup =
			(PsychicAffiliateGroupRec) (Object)
			objectManager.getParent (
				queue);

		PsychicRec psychic =
			psychicAffiliateGroup.getPsychic ();

		PsychicSettingsRec settings =
			psychic.getSettings ();

		return settings.getRequestPreferredQueueTime () * 1000L;

	}

}
