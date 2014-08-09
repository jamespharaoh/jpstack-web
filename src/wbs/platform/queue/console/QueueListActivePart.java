package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.millisToInstant;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.joda.time.Instant;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.queue.console.QueueSubjectSorter.QueueInfo;
import wbs.platform.queue.model.QueueRec;

@PrototypeComponent ("queueListActivePart")
public
class QueueListActivePart
	extends AbstractPagePart {

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	Provider<QueueSubjectSorter> queueSubjectSorter;

	Instant now;
	List<QueueInfo> queueInfos;

	@Override
	public
	void prepare () {

		now =
			Instant.now ();

		List<QueueInfo> queueInfosTemp =
			queueSubjectSorter.get ()
				.sort ()
				.queues ();

		queueInfos =
			new ArrayList<QueueInfo> ();

		for (QueueInfo queueInfo : queueInfosTemp) {

			if (! objectManager.canView (queueInfo.queue ()))
				continue;

			queueInfos.add (queueInfo);

		}

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Object</th>\n",
			"<th>Queue</th>\n",
			"<th colspan=\"2\">Available</th>\n",
			"<th colspan=\"2\">Claimed</th>\n",
			"<th colspan=\"2\">Preferred</th>\n",
			"<th colspan=\"2\">Waiting</th>\n",
			"<th colspan=\"2\">Total</th>\n",
			"</tr>\n");

		for (QueueInfo queueInfo
				: queueInfos) {

			QueueRec queue =
				queueInfo.queue ();

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				objectManager.objectPath (
					objectManager.getParent (
						queue),
					null,
					false,
					false),

				"<td>%h</td>\n",
				queue.getCode (),

				// available

				"<td>%h</td>\n",
				queueInfo.availableItems (),

				"<td>%h</td>\n",
				queueInfo.availableItems () > 0
					? requestContext.prettyDateDiff (
						millisToInstant (queueInfo.oldestAvailable ()),
						now)
					: "-",

				// claimed

				"<td>%h</td>\n",
				queueInfo.claimedItems (),

				"<td>%h</td>\n",
				queueInfo.claimedItems () > 0
					? requestContext.prettyDateDiff (
						millisToInstant (queueInfo.oldestClaimed ()),
						now)
					: "-",

				// preferred

				"<td>%h</td>\n",
				queueInfo.preferredItems (),

				"<td>%h</td>\n",
				queueInfo.preferredItems () > 0
					? requestContext.prettyDateDiff (
						millisToInstant (queueInfo.oldestPreferred ()),
						now)
					: "-",

				// waiting

				"<td>%h</td>\n",
				queueInfo.waitingItems (),

				"<td>%h</td>\n",
				queueInfo.waitingItems () > 0
					? requestContext.prettyDateDiff (
						millisToInstant (queueInfo.oldestWaiting ()),
						now)
					: "-",

				// total

				"<td>%h</td>\n",
				queueInfo.totalItems (),

				"<td>%h</td>\n",
				queueInfo.totalItems () > 0
					? requestContext.prettyDateDiff (
						millisToInstant (queueInfo.oldest ()),
						now)
					: "-",

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
