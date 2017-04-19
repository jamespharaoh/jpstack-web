package wbs.smsapps.autoresponder.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalOf;
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

import javax.inject.Named;

import lombok.NonNull;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormType;
import wbs.console.misc.ConsoleUserHelper;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageSearch;

import wbs.smsapps.autoresponder.model.AutoResponderRec;

import wbs.utils.time.DurationFormatter;
import wbs.utils.time.TextualInterval;

@PrototypeComponent ("autoResponderVotesPart")
public
class AutoResponderVotesPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@Named
	ConsoleModule autoResponderVotesConsoleModule;

	@SingletonDependency
	AutoResponderConsoleHelper autoResponderHelper;

	@SingletonDependency
	ConsoleUserHelper consoleUserHelper;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

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

	// state

	FormFieldSet <AutoResponderVotesForm> formFields;
	AutoResponderVotesForm formValue;
	UpdateResultSet formUpdate;

	Map <String, Long> votes;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"prepare");

		// get fields

		formFields =
			autoResponderVotesConsoleModule.formFieldSetRequired (
				"auto-responder-votes",
				AutoResponderVotesForm.class);

		// process form

		formValue =
			new AutoResponderVotesForm ()

			.timePeriod (
				TextualInterval.parseRequired (
					consoleUserHelper.timezone (),
					"last 12 hours",
					consoleUserHelper.hourOffset ()));

		formUpdate =
			formFieldLogic.update (
				taskLogger,
				requestContext,
				formFields,
				formValue,
				emptyMap (),
				"votes");

		if (formUpdate.errors ()) {
			return;
		}

		// lookup objects

		AutoResponderRec autoResponder =
			autoResponderHelper.findFromContextRequired ();

		ServiceRec autoResponderService =
			serviceHelper.findByCodeRequired (
				autoResponder,
				"default");

		// retrieve messages

		MessageSearch messageSearch =
			new MessageSearch ()

			.serviceId (
				autoResponderService.getId ())

			.createdTime (
				formValue.timePeriod ())

			.direction (
				MessageDirection.in);

		List <MessageRec> messages =
			messageHelper.search (
				taskLogger,
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
					votes.get (body),
					0l);

			votes.put (
				body,
				oldVal + 1);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

		// form

		formFieldLogic.outputFormTable (
			taskLogger,
			requestContext,
			formatWriter,
			formFields,
			optionalOf (
				formUpdate),
			formValue,
			emptyMap (),
			"get",
			requestContext.resolveLocalUrl (
				"/autoResponder.votes"),
			"search",
			FormType.search,
			"votes");

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
