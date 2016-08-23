package wbs.platform.queue.console;

import static wbs.framework.utils.etc.LogicUtils.referenceEqualSafe;
import static wbs.framework.utils.etc.Misc.isNotNull;

import javax.inject.Inject;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.platform.object.core.console.ObjectTypeConsoleHelper;
import wbs.platform.queue.metamodel.QueueTypeSpec;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.user.console.UserConsoleLogic;

@PrototypeComponent ("queueItemActionsPart")
public
class QueueItemActionsPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ObjectTypeConsoleHelper objectTypeHelper;

	@Inject
	UserPrivChecker privChecker;

	@Inject
	QueueConsoleLogic queueConsoleLogic;

	@Inject
	QueueItemConsoleHelper queueItemHelper;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// state

	QueueTypeSpec queueTypeSpec;

	QueueItemRec queueItem;

	boolean canSupervise;

	// implementation

	@Override
	public
	void prepare () {

		queueItem =
			queueItemHelper.findRequired (
				requestContext.stuffInteger (
					"queueItemId"));

		queueTypeSpec =
			queueConsoleLogic.queueTypeSpec (
				queueItem.getQueue ().getQueueType ());

		String[] supervisorParts =
			queueTypeSpec.supervisorPriv ().split (":");

		Record<?> supervisorDelegate =
			(Record<?>)
			objectManager.dereference (
				queueItem.getQueue (),
				supervisorParts [0]);

		canSupervise =
			privChecker.canRecursive (
				supervisorDelegate,
				supervisorParts [1]);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		if (! canSupervise) {

			printFormat (
				"<p>You do not have permission to perform actions on this ",
				"queue item.</p>\n");

			return;

		}

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/queueItem.actions"),
			">\n");

		if (
			isNotNull (
				queueItem.getQueueItemClaim ())
		) {

			if (
				referenceEqualSafe (
					queueItem.getQueueItemClaim ().getUser (),
					userConsoleLogic.userRequired ())
			) {

				printFormat (
					"<p>This queue item is claimed by you.</p>\n");

			} else {

				printFormat (
					"<p>This queue item is claimed by \"%h\". You may return ",
					objectManager.objectPathMini (
						queueItem.getQueueItemClaim ().getUser ()),
					"it to the queue or reclaim it yourself.</p>\n");

			}

		} else {

			printFormat (
				"<p>There are no actions that you can perform on this queue ",
				"item at this time.</p>\n");

		}

		printFormat (
			"</form>\n");

	}

}
