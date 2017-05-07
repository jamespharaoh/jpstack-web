package wbs.apn.chat.bill.console;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.console.action.ConsoleAction;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleModule;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleLogic;

import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.bill.logic.ChatCreditLogic.BillCheckOptions;
import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.Responder;

@PrototypeComponent ("chatRebillSendAction")
public
class ChatRebillSendAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	@Named
	ConsoleModule chatRebillConsoleModule;

	@SingletonDependency
	ChatRebillLogConsoleHelper chatRebillLogHelper;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	FormFieldSet <?> formFields;
	ChatRebillSearch formValues;
	Map <String, Object> formHints;
	UpdateResultSet formUpdates;

	// details

	@Override
	protected
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"chatRebillSendResponder");

	}

	// impementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			ChatRec chat =
				chatHelper.findFromContextRequired (
					transaction);

			// process form submission

			formFields =
				chatRebillConsoleModule.formFieldSets ().get (
					"search");

			formValues =
				new ChatRebillSearch ();

			formHints =
				ImmutableMap.<String, Object> builder ()

				.put (
					"chat",
					chat)

				.build ();

			formUpdates =
				formFieldLogic.update (
					transaction,
					requestContext,
					formFields,
					formValues,
					formHints,
					"rebill");

			requestContext.request (
				"formValues",
				formValues);

			requestContext.request (
				"formUpdates",
				formUpdates);

			if (formUpdates.errors ()) {
				return null;
			}

			// search for users

			BillCheckOptions billCheckOptions =
				new BillCheckOptions ()

				.retry (
					true)

				.includeBlocked (
					formValues.includeBlocked ())

				.includeFailed (
					formValues.includeFailed ());

			List <Pair <ChatUserRec, Optional <String>>> allChatUsers =
				chatUserHelper.findWantingBill (
					transaction,
					chat,
					formValues.lastAction (),
					ifNull (
						- formValues.minimumCreditOwed () + 1,
						0l))

				.stream ()

				.sorted (
					(left, right) ->
						left.getLastAction ().compareTo (
							right.getLastAction ()))

				.map (
					chatUser ->
						Pair.of (
							chatUser,
							chatCreditLogic.userBillCheck (
								transaction,
								chatUser,
								billCheckOptions)))

				.collect (
					Collectors.toList ());

			List <ChatUserRec> billChatUsers =
				allChatUsers.stream ()

				.filter (
					chatUserWithReason ->
						optionalIsNotPresent (
							chatUserWithReason.getRight ()))

				.map (
					Pair::getLeft)

				.collect (
					Collectors.toList ());

			List <Long> billChatUserIds =
				iterableMapToList (
					billChatUsers,
					ChatUserRec::getId);

			List <Pair <Long, String>> nonBillChatUsers =
				allChatUsers.stream ()

				.filter (
					chatUserWithReason ->
						optionalIsPresent (
							chatUserWithReason.getRight ()))

				.map (
					chatUserWithReason ->
						Pair.of (
							chatUserWithReason.getLeft ().getId (),
							chatUserWithReason.getRight ().get ()))

				.collect (
					Collectors.toList ());

			requestContext.request (
				"billSearchResults",
				billChatUserIds);

			requestContext.request (
				"nonBillSearchResults",
				nonBillChatUsers);

			// bill users

			if (
				optionalIsPresent (
					requestContext.parameter (
						"rebill"))
			) {

				chatRebillLogHelper.insert (
					transaction,
					chatRebillLogHelper.createInstance ()

					.setChat (
						chat)

					.setTimestamp (
						transaction.now ())

					.setUser (
						userConsoleLogic.userRequired (
							transaction))

					.setLastAction (
						formValues.lastAction ())

					.setMinimumCreditOwed (
						formValues.minimumCreditOwed ())

					.setIncludeBlocked (
						formValues.includeBlocked ())

					.setIncludeFailed (
						formValues.includeFailed ())

					.setNumChatUsers (
						collectionSize (
							billChatUsers))

					.setChatUsers (
						ImmutableSet.copyOf (
							billChatUsers))

				);

				billChatUsers.forEach (
					chatUser ->
						chatCreditLogic.userBillReal (
							transaction,
							chatUser,
							true));

				transaction.commit ();

				requestContext.addNoticeFormat (
					"Rebilled %s users",
					integerToDecimalString (
						collectionSize (
							billChatUsers)));

			}

			return null;

		}

	}

}
