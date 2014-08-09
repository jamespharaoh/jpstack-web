package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.part.PagePart;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.status.console.StatusLine;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@SingletonComponent ("queueItemStatusLine")
public
class QueueItemStatusLine
	implements StatusLine {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserObjectHelper userHelper;

	@Inject
	Provider<QueueItemsStatusLinePart> queueItemsStatusLinePart;

	@Inject
	Provider<QueueSubjectSorter> queueSubjectSorter;

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

	@Override
	public
	String getUpdateScript () {

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		QueueSubjectSorter sortedSubjects =
			queueSubjectSorter.get ()
				.user (myUser)
				.sort ();

		// return

		return stringFormat (
			"updateQueueItems (%s);\n",
			sortedSubjects.availableItems ());

	}

}
