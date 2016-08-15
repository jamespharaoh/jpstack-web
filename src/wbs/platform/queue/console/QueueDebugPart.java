package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.framework.utils.etc.StringUtils.joinWithSemicolonAndSpace;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.joda.time.Duration;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import wbs.console.forms.FormField.FormType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.platform.object.core.console.ObjectTypeConsoleHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.queue.console.QueueSubjectSorter.QueueInfo;
import wbs.platform.queue.console.QueueSubjectSorter.SubjectInfo;
import wbs.platform.queue.logic.MasterQueueCache;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

@PrototypeComponent ("queueDebugPart")
public
class QueueDebugPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ObjectTypeConsoleHelper objectTypeHelper;

	@Inject @Named
	ConsoleModule queueConsoleModule;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserConsoleHelper userHelper;

	@Inject
	UserPrivChecker userPrivChecker;

	// prototype dependencies

	@Inject
	Provider<MasterQueueCache> masterQueueCacheProvider;

	@Inject
	Provider<QueueSubjectSorter> queueSubjectSorterProvider;

	// state

	FormFieldSet formFields;

	QueueDebugForm form;

	List<QueueInfo> queueInfos;

	// implementation

	@Override
	public
	void prepare () {

		formFields =
			queueConsoleModule.formFieldSets ().get (
				"queue-debug-form");

		form =
			new QueueDebugForm ()

			.userId (
				userConsoleLogic.userIdRequired ());

		formFieldLogic.update (
			requestContext,
			formFields,
			form,
			ImmutableMap.<String,Object>of (),
			"search");

		SortedQueueSubjects sortedQueueSubjects =
			queueSubjectSorterProvider.get ()

			.queueCache (
				masterQueueCacheProvider.get ())

			.loggedInUser (
				userConsoleLogic.userRequired ())

			.effectiveUser (
				optionalOrNull (
					userHelper.find (
						form.userId ())))

			.sort ();

		queueInfos =
			sortedQueueSubjects.allQueues ().stream ()

			.filter (queueInfo ->
				userPrivChecker.canRecursive (
					queueInfo.queue (),
					"supervisor"))

			.collect (
				Collectors.toList ());

	}

	@Override
	public
	void renderHtmlBodyContent () {

		formFieldLogic.outputFormTable (
			requestContext,
			formatWriter,
			formFields,
			Optional.absent (),
			form,
			ImmutableMap.of (),
			"get",
			requestContext.resolveLocalUrl (
				"/queue.debug"),
			"update",
			FormType.search,
			"search");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Queue</th>",
			"<th>Own operator activity</th>\n",
			"<th>Preferred user</th>\n",
			"<th>Overflow</th>\n",
			"<th>Conclusion</th>\n",
			"</tr>\n");

		for (
			QueueInfo queueInfo
				: queueInfos
		) {

			if (

				isNotNull (
					form.sliceId ())

				&& notEqual (
					form.sliceId (),
					(long) (int)
					queueInfo.slice.getId ())

			) {
				continue;
			}

			Record<?> queueParent =
				objectManager.getParent (
					queueInfo.queue ());

			ObjectTypeRec queueParentType =
				objectTypeHelper.findRequired (
					objectManager.getObjectTypeId (
						queueParent));

			printFormat (
				"<tr colspan=\"%h\">\n",
				form.allItems ()
					? 2 * queueInfo.subjectInfos.size ()
					: 2);

			List<SubjectInfo> subjectInfos =
				form.allItems ()
					? queueInfo.subjectInfos
					: ImmutableList.of (
						queueInfo.subjectInfos.get (0));

			int row = 0;

			for (
				SubjectInfo subjectInfo
					: subjectInfos
			) {

				if (row == 0) {

					// queue

					printFormat (
						"<td",
						" rowspan=\"%h\"",
						subjectInfos.size () * 2,
						" style=\"vertical-align: top\"",
						">Slice: <a href=\"%h\">%h</a><br>\n",
						objectManager.localLink (
							queueInfo.slice ()),
						queueInfo.slice ().getCode ());

					printFormat (
						"Type: <a href=\"%h\">%h</a><br>\n",
						objectManager.localLink (
							queueParentType),
						queueParentType.getCode ());

					printFormat (
						"Service: <a href=\"%h\">%h</a><br>\n",
						objectManager.localLink (
							queueParent),
						objectManager.objectPathMini (
							queueParent,
							queueInfo.slice ()));

					printFormat (
						"Queue: <a href=\"%h\">%h</a><br>\n",
						objectManager.localLink (
							queueInfo.queue ()),
						queueInfo.queue ().getCode ());

					printFormat (
						"Reply priv: %h<br>\n",
						queueInfo.canReplyExplicit ()
							? "explicit"
							: queueInfo.canReplyImplicit ()
								? "implicit"
								: "no");

					printFormat (
						"Reply overflow priv: %h<br>\n",
						queueInfo.canReplyOverflowExplicit ()
							? "explicit"
							: queueInfo.canReplyOverflowImplicit ()
								? "implicit"
								: "no");

					printFormat (
						"Is overflow user: %h</td>\n",
						queueInfo.isOverflowUser ()
							? "yes"
							: "no");

					// operator activity

					printFormat (
						"<td",
						" rowspan=\"%h\"",
						subjectInfos.size () * 2,
						" style=\"vertical-align: top\"",
						">Last update: %h<br>\n",
						userConsoleLogic.timestampWithoutTimezoneString (
							queueInfo.slice ().getCurrentQueueInactivityUpdateTime ()));

					printFormat (
						"Inactive since: %h<br>\n",
						queueInfo.slice ().getCurrentQueueInactivityTime () != null
							? userConsoleLogic.timestampWithoutTimezoneString (
								queueInfo.slice ().getCurrentQueueInactivityTime ())
							: "none");

					printFormat (
						"Configured inactivity time: %h<br>\n",
						queueInfo.slice ().getQueueOverflowInactivityTime () != null
							? userConsoleLogic.prettyDuration (
								Duration.standardSeconds (
									queueInfo.slice ().getQueueOverflowInactivityTime ()))
							: "disabled");

					printFormat (
						"Actual inactivity time: %h<br>\n",
						queueInfo.slice ().getCurrentQueueInactivityTime () != null
							? userConsoleLogic.prettyDuration (
								queueInfo.slice ().getCurrentQueueInactivityTime (),
								transaction.now ())
							: "none");

					printFormat (
						"Conclusion: %h</td>",
						queueInfo.slice ().getCurrentQueueInactivityTime () != null
							? queueInfo.ownOperatorsActive ()
								? "own operators active"
								: "overflow active"
							: "no data");

				}

				// subject

				printFormat (
					"<td",
					" colspan=\"3\"",
					" style=\"%h\"",
					joinWithSemicolonAndSpace (
						"text-align: center",
						"background: #dddddd"),
					">Subject: <a",
					" href=\"%h\"",
					objectManager.localLink (
						subjectInfo.subject ()),
					">%h</a></td>\n",
					objectManager.objectPathMini (
						subjectInfo.subject (),
						queueInfo.queue ()));

				printFormat (
					"</tr>\n",
					"<tr>\n");

				// preferred user

				printFormat (
					"<td",
					" style=\"vertical-align: top\"",
					">Preferred by: %s<br>",
					subjectInfo.preferred ()
						? subjectInfo.preferredByUs ()
							? "this user"
							: stringFormat (
								"<a href=\"%h\">%h</a>",
								objectManager.localLink (
									subjectInfo.preferredUser ()),
								objectManager.objectPathMini (
									subjectInfo.preferredUser ()))
						: "nobody");

				printFormat (
					"Preferred by overflow user: %h<br>\n",
					subjectInfo.preferredByOverflowOperator ()
						? "yes"
						: "no");

				printFormat (
					"Configured delay: %h<br>\n",
					userConsoleLogic.prettyDuration (
						queueInfo.configuredPreferredUserDelay ()));

				printFormat (
					"Actual delay: %h</td>\n",
					subjectInfo.actualPreferredUserDelay () != null
						? userConsoleLogic.prettyDuration (
							subjectInfo.actualPreferredUserDelay ())
						: "none");

				// overflow

				printFormat (
					"<td",
					" style=\"vertical-align: top\"",
					">Configured grace time: %h<br>\n",
					queueInfo.slice ().getQueueOverflowGraceTime () != null
						? userConsoleLogic.prettyDuration (
							Duration.standardSeconds (
								queueInfo.slice ().getQueueOverflowGraceTime ()))
						: "none");

				printFormat (
					"Configured overload time: %h<br>\n",
					queueInfo.slice ().getQueueOverflowOverloadTime () != null
						? userConsoleLogic.prettyDuration (
							Duration.standardSeconds (
								queueInfo.slice ().getQueueOverflowOverloadTime ()))
						: "none");

				printFormat (
					"Is overflow user: %h<br>\n",
					queueInfo.isOverflowUser ()
						? "yes"
						: "no");

				printFormat (
					"Own operators active: %h<br>",
					queueInfo.ownOperatorsActive ()
						? "yes"
						: "no");

				printFormat (
					"Actual overflow delay: %h</td>\n",
					subjectInfo.overflowDelay () != null
						? userConsoleLogic.prettyDuration (
							subjectInfo.overflowDelay ())
						: "none");

				// conclusion

				printFormat (
					"<td",
					" style=\"vertical-align: top\"",
					">Priority: %h<br>\n",
					subjectInfo.priority ());

				printFormat (
					"Created time: %h<br>\n",
					userConsoleLogic.timestampWithoutTimezoneString (
						subjectInfo.createdTime ()));

				printFormat (
					"Preferred user delay: %h<br>\n",
					subjectInfo.actualPreferredUserDelay != null
						? userConsoleLogic.prettyDuration (
							subjectInfo.actualPreferredUserDelay ())
						: "none");

				if (subjectInfo.overflowDelay != null) {

					printFormat (
						"Overflow delay: %h (%h)<br>\n",
						subjectInfo.overflowDelay != null
							? userConsoleLogic.prettyDuration (
								subjectInfo.overflowDelay ())
							: "none",
						queueInfo.ownOperatorsActive ()
							? "overload"
							: "grace");

				} else {

					printFormat (
						"Overflow delay: none<br>\n");

				}

				printFormat (
					"Effective time: %h<br>\n",
					userConsoleLogic.timestampWithoutTimezoneString (
						subjectInfo.effectiveTime ()));

				if (subjectInfo.claimed ()) {

					printFormat (
						"Claimed: yes, by <a href=\"%h\">%h</a><br>\n",
						objectManager.localLink (
							subjectInfo.claimedByUser ()),
						objectManager.objectPathMini (
							subjectInfo.claimedByUser ()));

				} else {

					printFormat (
						"Claimed: no");

				}

				if (subjectInfo.available ()) {

					printFormat (
						"Available: yes, for %h</td>\n",
						userConsoleLogic.prettyDuration (
							subjectInfo.effectiveTime (),
							transaction.now ()));

				} else if (subjectInfo.claimed ()) {

					printFormat (
						"Available: no, already claimed</td>\n");

				} else {

					printFormat (
						"Available: no, for %h</td>\n",
						userConsoleLogic.prettyDuration (
							transaction.now (),
							subjectInfo.effectiveTime ()));

				}

				printFormat (
					"</tr>\n");

				row ++;

			}

		}

		printFormat (
			"</table>\n");

	}

}
