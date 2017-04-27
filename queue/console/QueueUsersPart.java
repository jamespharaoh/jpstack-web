package wbs.platform.queue.console;

import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlAttributeUtils.htmlAttribute;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import org.joda.time.Instant;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.queue.model.QueueItemClaimObjectHelper;
import wbs.platform.queue.model.QueueItemClaimRec;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserRec;

import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("queueUsersPart")
public
class QueueUsersPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	QueueItemClaimObjectHelper queueItemClaimHelper;

	@SingletonDependency
	TimeFormatter timeFormatter;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	List <UserData> userDatas;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"prepare");

		) {

			Map <Long, UserData> temp =
				new HashMap<> ();

			List <QueueItemClaimRec> queueItemClaims =
				queueItemClaimHelper.findClaimed ();

			for (
				QueueItemClaimRec queueItemClaim
					: queueItemClaims
			) {

				QueueItemRec queueItem =
					queueItemClaim.getQueueItem ();

				QueueRec queue =
					queueItem.getQueue ();

				Record <?> parent =
					objectManager.getParentRequired (
						queue);

				if (
					! privChecker.canRecursive (
						taskLogger,
						parent,
						"manage")
				) {
					continue;
				}

				Instant createdTime =
					queueItem.getCreatedTime ();

				UserData line =
					temp.get (queueItemClaim.getUser ().getId ());

				if (line == null) {

					temp.put (
						queueItemClaim.getUser ().getId (),
						line = new UserData ()
							.user (queueItemClaim.getUser ())
							.oldest (createdTime)
							.count (0));

				}

				if (createdTime.isBefore (line.oldest))
					line.oldest = createdTime;

				line.count ++;

			}

			userDatas =
				new ArrayList<> (
					temp.values ());

			Collections.sort (
				userDatas);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"renderHtmlBodyContent");

		) {

			htmlTableOpenList ();

			htmlTableHeaderRowWrite (
				"User",
				"Items",
				"Oldest",
				"Reclaim",
				"Unclaim");

			for (
				UserData userData
					: userDatas
			) {

				htmlTableRowOpen ();

				objectManager.writeTdForObjectMiniLink (
					taskLogger,
					userData.user);

				htmlTableCellWrite (
					integerToDecimalString (
						userData.count),
					htmlAttribute (
						"align",
						"right"));

				htmlTableCellWrite (
					timeFormatter.prettyDuration (
						userData.oldest,
						transaction.now ()));

				if (
					referenceEqualWithClass (
						UserRec.class,
						userData.user,
						userConsoleLogic.userRequired ())
				) {

					htmlTableCellWrite (
						"");

				} else {

					htmlTableCellOpen ();

					htmlFormOpenPostAction (
						 requestContext.resolveLocalUrl (
						 	"/queue.users"));

					formatWriter.writeLineFormat (
						"<input",
						" type=\"hidden\"",
						" name=\"userId\"",
						" value=\"%h\"",
						integerToDecimalString (
							userData.user.getId ()),
						">");

					formatWriter.writeLineFormat (
						"<input",
						" type=\"submit\"",
						" name=\"reclaim\"",
						" value=\"reclaim\"",
						">");

					htmlFormClose ();

					htmlTableCellClose ();

				}

				htmlTableCellOpen ();

				htmlFormOpenPostAction (
					requestContext.resolveLocalUrl (
						"/queue.users"));

				formatWriter.writeLineFormat (
					"<input",
					" type=\"hidden\"",
					" name=\"userId\"",
					" value=\"%h\"",
					integerToDecimalString (
						userData.user.getId ()),
					">");

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"unclaim\"",
					" value=\"unclaim\"",
					">");


				htmlFormClose ();

				htmlTableCellClose ();

				htmlTableRowClose ();

			}

			htmlTableClose ();

		}

	}

	@Accessors (fluent = true)
	@Data
	static
	class UserData
		implements Comparable<UserData> {

		UserRec user;
		Instant oldest;
		long count;

		@Override
		public
		int compareTo (
				UserData other) {

			return new CompareToBuilder ()

				.append (
					oldest,
					other.oldest)

				.append (
					user,
					other.user)

				.toComparison ();

		}

	}

}
