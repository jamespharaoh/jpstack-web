package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.part.PagePart;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.status.console.StatusLine;
import wbs.platform.user.console.UserConsoleLogic;

@SingletonComponent ("queueItemStatusLine")
public
class QueueItemStatusLine
	implements StatusLine {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@Inject
	Provider<QueueItemsStatusLinePart> queueItemsStatusLinePart;

	@Inject
	Provider<QueueSubjectSorter> queueSubjectSorter;

	// details

	@Override
	public
	String getName () {
		return "queueItems";
	}

	@Override
	public
	PagePart get () {
		return queueItemsStatusLinePart.get ();
	}

	// implementation

	@Override
	public
	String getUpdateScript () {

		SortedQueueSubjects sortedSubjects =
			queueSubjectSorter.get ()

			.user (
				userConsoleLogic.userRequired ())

			.sort ();

		// return

		return stringFormat (
			"updateQueueItems (%s, %s);\n",
			sortedSubjects.totalAvailableItems (),
			sortedSubjects.userClaimedItems ());

	}

}
