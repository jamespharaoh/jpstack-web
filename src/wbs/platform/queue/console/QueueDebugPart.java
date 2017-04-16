package wbs.platform.queue.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.LogicUtils.booleanToString;
import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.integerNotEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlRowSpanAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlStyleAttribute;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlUtils.htmlLinkWrite;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Provider;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import org.joda.time.Duration;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormType;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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

	// singleton dependencies

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ObjectTypeConsoleHelper objectTypeHelper;

	@SingletonDependency
	@Named
	ConsoleModule queueConsoleModule;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserConsoleHelper userHelper;

	@SingletonDependency
	UserPrivChecker userPrivChecker;

	// prototype dependencies

	@PrototypeDependency
	Provider <MasterQueueCache> masterQueueCacheProvider;

	@PrototypeDependency
	Provider <QueueSubjectSorter> queueSubjectSorterProvider;

	// state

	FormFieldSet <QueueDebugForm> formFields;

	QueueDebugForm form;

	List <QueueInfo> queueInfos;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"prepare");

		formFields =
			queueConsoleModule.formFieldSet (
				"queue-debug-form",
				QueueDebugForm.class);

		form =
			new QueueDebugForm ()

			.userId (
				userConsoleLogic.userIdRequired ());

		formFieldLogic.update (
			taskLogger,
			requestContext,
			formFields,
			form,
			emptyMap (),
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

			.sort (
				taskLogger);

		queueInfos =
			sortedQueueSubjects.allQueues ().stream ()

			.filter (queueInfo ->
				userPrivChecker.canRecursive (
					taskLogger,
					queueInfo.queue (),
					"supervisor"))

			.collect (
				Collectors.toList ());

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

		formFieldLogic.outputFormTable (
			taskLogger,
			requestContext,
			formatWriter,
			formFields,
			optionalAbsent (),
			form,
			emptyMap (),
			"get",
			requestContext.resolveLocalUrl (
				"/queue.debug"),
			"update",
			FormType.search,
			"search");

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Queue",
			"Own operator activity",
			"Preferred user",
			"Overflow",
			"Conclusion");

		for (
			QueueInfo queueInfo
				: queueInfos
		) {

			if (

				isNotNull (
					form.sliceId ())

				&& integerNotEqualSafe (
					form.sliceId (),
					queueInfo.slice.getId ())

			) {
				continue;
			}

			Record<?> queueParent =
				objectManager.getParentRequired (
					queueInfo.queue ());

			ObjectTypeRec queueParentType =
				objectTypeHelper.findRequired (
					objectManager.getObjectTypeId (
						queueParent));

			htmlTableRowOpen (
				htmlColumnSpanAttribute (
					form.allItems ()
						? 2l * queueInfo.subjectInfos.size ()
						: 2l));

			List <SubjectInfo> subjectInfos =
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

					htmlTableCellOpen (
						htmlRowSpanAttribute (
							2l * subjectInfos.size ()),
						htmlStyleAttribute (
							htmlStyleRuleEntry (
								"vertical-align",
								"top")));

					formatWriter.writeLineFormat (
						"Slice: <a href=\"%h\">%h</a><br>",
						objectManager.localLink (
							taskLogger,
							queueInfo.slice ()),
						queueInfo.slice ().getCode ());

					formatWriter.writeLineFormat (
						"Type: <a href=\"%h\">%h</a><br>",
						objectManager.localLink (
							taskLogger,
							queueParentType),
						queueParentType.getCode ());

					formatWriter.writeLineFormat (
						"Service: <a href=\"%h\">%h</a><br>",
						objectManager.localLink (
							taskLogger,
							queueParent),
						objectManager.objectPathMini (
							queueParent,
							queueInfo.slice ()));

					formatWriter.writeLineFormat (
						"Queue: <a href=\"%h\">%h</a><br>",
						objectManager.localLink (
							taskLogger,
							queueInfo.queue ()),
						queueInfo.queue ().getCode ());

					formatWriter.writeLineFormat (
						"Reply priv: %h<br>",
						queueInfo.canReplyExplicit ()
							? "explicit"
							: queueInfo.canReplyImplicit ()
								? "implicit"
								: "no");

					formatWriter.writeLineFormat (
						"Reply overflow priv: %h<br>",
						queueInfo.canReplyOverflowExplicit ()
							? "explicit"
							: queueInfo.canReplyOverflowImplicit ()
								? "implicit"
								: "no");

					formatWriter.writeLineFormat (
						"Is overflow user: %h<br>",
						queueInfo.isOverflowUser ()
							? "yes"
							: "no");

					htmlTableCellClose ();

					// operator activity

					htmlTableCellOpen (
						htmlRowSpanAttribute (
							2l * subjectInfos.size ()),
						htmlStyleAttribute (
							htmlStyleRuleEntry (
								"vertical-align",
								"top")));

					formatWriter.writeLineFormat (
						"Last update: %h<br>",
						userConsoleLogic.timestampWithoutTimezoneString (
							queueInfo.slice ()
								.getCurrentQueueInactivityUpdateTime ()));

					formatWriter.writeLineFormat (
						"Inactive since: %h<br>",
						ifNotNullThenElse (
							queueInfo.slice ().getCurrentQueueInactivityTime (),
							() -> userConsoleLogic
								.timestampWithoutTimezoneString (
									queueInfo.slice ()
										.getCurrentQueueInactivityTime ()),
							() -> "none"));

					formatWriter.writeLineFormat (
						"Configured inactivity time: %h<br>",
						ifNotNullThenElse (
							queueInfo.slice ()
								.getQueueOverflowInactivityTime (),
							() -> userConsoleLogic.prettyDuration (
								Duration.standardSeconds (
									queueInfo.slice ()
										.getQueueOverflowInactivityTime ())),
							() -> "disabled"));

					formatWriter.writeLineFormat (
						"Actual inactivity time: %h<br>",
						ifNotNullThenElse (
							queueInfo.slice ()
								.getCurrentQueueInactivityTime (),
							() -> userConsoleLogic.prettyDuration (
								queueInfo.slice ()
									.getCurrentQueueInactivityTime (),
								transaction.now ()),
							() -> "none"));

					formatWriter.writeLineFormat (
						"Conclusion: %h<br>",
						queueInfo.slice ().getCurrentQueueInactivityTime () != null
							? queueInfo.ownOperatorsActive ()
								? "own operators active"
								: "overflow active"
							: "no data");

					htmlTableCellClose ();

				}

				// subject

				htmlTableCellOpen (
					htmlColumnSpanAttribute (3l),
					htmlStyleAttribute (
						htmlStyleRuleEntry (
							"text-align",
							"center"),
						htmlStyleRuleEntry (
							"background",
							"#dddddd")));

				formatWriter.writeLineFormat (
					"Subject:");

				htmlLinkWrite (
					objectManager.localLink (
						taskLogger,
						subjectInfo.subject ()),
					objectManager.objectPathMini (
						subjectInfo.subject (),
						queueInfo.queue ()));

				htmlTableCellClose ();

				htmlTableRowClose ();

				// preferred user

				htmlTableRowOpen ();

				htmlTableCellOpen (
					htmlStyleAttribute (
						htmlStyleRuleEntry (
							"vertical-align",
							"top")));

				formatWriter.writeLineFormat (
					"Preferred by: %s",
					subjectInfo.preferred ()
						? subjectInfo.preferredByUs ()
							? "this user"
							: stringFormat (
								"<a href=\"%h\">%h</a>",
								objectManager.localLink (
									taskLogger,
									subjectInfo.preferredUser ()),
								objectManager.objectPathMini (
									subjectInfo.preferredUser ()))
						: "nobody");

				formatWriter.writeLineFormat (
					"Preferred by overflow user: %h<br>",
					booleanToYesNo (
						subjectInfo.preferredByOverflowOperator ()));

				formatWriter.writeLineFormat (
					"Configured delay: %h<br>",
					userConsoleLogic.prettyDuration (
						queueInfo.configuredPreferredUserDelay ()));

				formatWriter.writeLineFormat (
					"Actual delay: %h<br>",
					subjectInfo.actualPreferredUserDelay () != null
						? userConsoleLogic.prettyDuration (
							subjectInfo.actualPreferredUserDelay ())
						: "none");

				htmlTableCellClose ();

				// overflow

				htmlTableCellOpen (
					htmlStyleAttribute (
						htmlStyleRuleEntry (
							"vertical-align",
							"top")));

				formatWriter.writeLineFormat (
					"Configured grace time: %h<br>",
					ifNotNullThenElse (
						queueInfo.slice ().getQueueOverflowGraceTime (),
						() -> userConsoleLogic.prettyDuration (
							Duration.standardSeconds (
								queueInfo.slice ()
									.getQueueOverflowGraceTime ())),
						() -> "none"));

				formatWriter.writeLineFormat (
					"Configured overload time: %h<br>",
					ifNotNullThenElse (
						queueInfo.slice ().getQueueOverflowOverloadTime (),
						() -> userConsoleLogic.prettyDuration (
							Duration.standardSeconds (
								queueInfo.slice ()
									.getQueueOverflowOverloadTime ())),
						() -> "none"));

				formatWriter.writeLineFormat (
					"Is overflow user: %h<br>",
					booleanToYesNo (
						queueInfo.isOverflowUser ()));

				formatWriter.writeLineFormat (
					"Own operators active: %h<br>",
					booleanToYesNo (
						queueInfo.ownOperatorsActive ()));

				formatWriter.writeLineFormat (
					"Actual overflow delay: %h<br>",
					ifNotNullThenElse (
						subjectInfo.overflowDelay (),
						() -> userConsoleLogic.prettyDuration (
							subjectInfo.overflowDelay ()),
						() -> "none"));

				htmlTableCellClose ();

				// conclusion

				htmlTableCellOpen (
					htmlStyleAttribute (
						htmlStyleRuleEntry (
							"vertical-align",
							"top")));

				formatWriter.writeLineFormat (
					"Priority: %h<br>",
					integerToDecimalString (
						subjectInfo.priority ()));

				formatWriter.writeLineFormat (
					"Created time: %h<br>",
					userConsoleLogic.timestampWithoutTimezoneString (
						subjectInfo.createdTime ()));

				formatWriter.writeLineFormat (
					"Preferred user delay: %h<br>",
					ifNotNullThenElse (
						subjectInfo.actualPreferredUserDelay,
						() -> userConsoleLogic.prettyDuration (
							subjectInfo.actualPreferredUserDelay ()),
						() -> "none"));

				if (
					isNotNull (
						subjectInfo.overflowDelay)
				) {

					formatWriter.writeLineFormat (
						"Overflow delay: %h (%h)<br>",
						ifNotNullThenElse (
							subjectInfo.overflowDelay,
							() -> userConsoleLogic.prettyDuration (
								subjectInfo.overflowDelay ()),
							() -> "none"),
						booleanToString (
							queueInfo.ownOperatorsActive (),
							"overload",
							"grace"));

				} else {

					formatWriter.writeLineFormat (
						"Overflow delay: none<br>");

				}

				formatWriter.writeLineFormat (
					"Effective time: %h<br>",
					userConsoleLogic.timestampWithoutTimezoneString (
						subjectInfo.effectiveTime ()));

				if (subjectInfo.claimed ()) {

					formatWriter.writeLineFormat (
						"Claimed: yes, by <a href=\"%h\">%h</a><br>",
						objectManager.localLink (
							taskLogger,
							subjectInfo.claimedByUser ()),
						objectManager.objectPathMini (
							subjectInfo.claimedByUser ()));

				} else {

					formatWriter.writeLineFormat (
						"Claimed: no<br>");

				}

				if (subjectInfo.available ()) {

					formatWriter.writeLineFormat (
						"Available: yes, for %h<br>",
						userConsoleLogic.prettyDuration (
							subjectInfo.effectiveTime (),
							transaction.now ()));

				} else if (subjectInfo.claimed ()) {

					formatWriter.writeLineFormat (
						"Available: no, already claimed<br>");

				} else {

					formatWriter.writeLineFormat (
						"Available: no, for %h<br>",
						userConsoleLogic.prettyDuration (
							transaction.now (),
							subjectInfo.effectiveTime ()));

				}

				htmlTableCellClose ();

				htmlTableRowClose ();

				row ++;

			}

		}

		htmlTableClose ();

	}

}
