package wbs.platform.queue.console;

import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;

import lombok.NonNull;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.platform.object.core.console.ObjectTypeConsoleHelper;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("queueItemActionsPart")
public
class QueueItemActionsPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ObjectTypeConsoleHelper objectTypeHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	QueueConsoleLogic queueConsoleLogic;

	@SingletonDependency
	QueueItemConsoleHelper queueItemHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	QueueItemRec queueItem;

	boolean canSupervise;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		queueItem =
			queueItemHelper.findFromContextRequired ();

		canSupervise =
			queueConsoleLogic.canSupervise (
				queueItem.getQueue ());

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		if (! canSupervise) {

			htmlParagraphWriteFormat (
				"You do not have permission to perform actions on this queue ",
				"item.");

			return;

		}

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/queueItem.actions"));

		if (
			isNotNull (
				queueItem.getQueueItemClaim ())
		) {

			if (
				referenceEqualWithClass (
					UserRec.class,
					queueItem.getQueueItemClaim ().getUser (),
					userConsoleLogic.userRequired ())
			) {

				htmlParagraphWriteFormat (
					"This queue item is claimed by you. You may return it to ",
					"the queue.");

				htmlParagraphOpen ();

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"unclaim\"",
					" value=\"unclaim\"",
					">");

				htmlParagraphClose ();

			} else {

				htmlParagraphWriteFormat (
					"This queue item is claimed by \"%h\". ",
					objectManager.objectPathMini (
						queueItem.getQueueItemClaim ().getUser ()),
					"You may return it to the queue or reclaim it yourself.");

				htmlParagraphOpen ();

				formatWriter.writeFormat (
					"<input",
					" type=\"submit\"",
					" name=\"unclaim\"",
					" value=\"unclaim\"",
					">");

				formatWriter.writeFormat (
					"<input",
					" type=\"submit\"",
					" name=\"reclaim\"",
					" value=\"reclaim\"",
					">");

				htmlParagraphClose ();

			}

		} else {

			htmlParagraphWriteFormat (
				"There are no actions that you can perform on this queue item ",
				"at this time.");

		}

		htmlFormClose ();

	}

}
