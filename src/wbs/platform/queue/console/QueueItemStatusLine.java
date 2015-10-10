package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.part.PagePart;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.status.console.StatusLine;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@SingletonComponent ("queueItemStatusLine")
public
class QueueItemStatusLine
	implements StatusLine {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserObjectHelper userHelper;

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

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		QueueSubjectSorter sortedSubjects =
			queueSubjectSorter.get ()
				.user (myUser)
				.sort ();

		// return

		return stringFormat (
			"updateQueueItems (%s, %s);\n",
			sortedSubjects.availableItems (),
			sortedSubjects.userClaimedItems ());

	}

}
