package wbs.smsapps.autoresponder.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.simplify;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenLayout;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.NonNull;

import wbs.console.forms.core.ConsoleForm;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.misc.ConsoleUserHelper;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageSearch;

import wbs.smsapps.autoresponder.model.AutoResponderRec;

import wbs.utils.time.DurationFormatter;

@PrototypeComponent ("autoResponderVotesPart")
public
class AutoResponderVotesPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@NamedDependency ("autoResponderVotesSearchFormType")
	ConsoleFormType <AutoResponderVotesForm> formType;

	@SingletonDependency
	AutoResponderConsoleHelper autoResponderHelper;

	@SingletonDependency
	ConsoleUserHelper consoleUserHelper;

	@SingletonDependency
	DurationFormatter intervalFormatter;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserPrivChecker privChecker;

	// state

	ConsoleForm <AutoResponderVotesForm> form;

	Map <String, Long> votes;

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			// setup form context

			form =
				formType.buildResponse (
					transaction,
					emptyMap ());

			// process form

			form.update (
				transaction);

			if (form.errors ()) {
				return;
			}

			// lookup objects

			AutoResponderRec autoResponder =
				autoResponderHelper.findFromContextRequired (
					transaction);

			ServiceRec autoResponderService =
				serviceHelper.findByCodeRequired (
					transaction,
					autoResponder,
					"default");

			// retrieve messages

			MessageSearch messageSearch =
				new MessageSearch ()

				.serviceId (
					autoResponderService.getId ())

				.createdTime (
					form.value ().timePeriod ())

				.direction (
					MessageDirection.in);

			List <MessageRec> messages =
				messageHelper.search (
					transaction,
					messageSearch);

			// now aggregate them

			votes =
				new TreeMap <String, Long> ();

			for (
				MessageRec message
					: messages
			) {

				String body =
					simplify (
						message.getText ().getText ());

				Long oldVal =
					ifNull (
						votes.get (
							body),
						0l);

				votes.put (
					body,
					oldVal + 1);

			}

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			// form

			form.outputFormTable (
				transaction,
				"get",
				requestContext.resolveLocalUrl (
					"/autoResponder.votes"),
				"search");

			// votes

			if (
				isNull (
					votes)
			) {
				return;
			}

			htmlHeadingTwoWrite (
				"Vote summary");

			htmlTableOpenLayout ();

			htmlTableHeaderRowWrite (
				"Content",
				"Votes");

			for (
				Map.Entry <String, Long> entry
					: votes.entrySet ()
			) {

				htmlTableRowOpen ();

				htmlTableCellWrite (
					entry.getKey ());

				htmlTableCellWrite (
					integerToDecimalString (
						entry.getValue ()));

			}

			htmlTableClose ();

		}

	}

}
