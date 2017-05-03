package wbs.apn.chat.user.core.logic;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.logic.NumberLogic;
import wbs.sms.number.core.model.NumberRec;

import wbs.utils.random.RandomLogic;

import wbs.apn.chat.bill.model.ChatUserCreditMode;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.core.logic.ChatNumberReportLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserObjectHelperMethods;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;

public
class ChatUserObjectHelperMethodsImplementation
	implements ChatUserObjectHelperMethods {

	// singleton dependencies

	@WeakSingletonDependency
	ChatNumberReportLogic chatNumberReportLogic;

	@WeakSingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@WeakSingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	NumberLogic numberLogic;

	@SingletonDependency
	RandomLogic randomLogic;

	// implementation

	@Override
	public
	ChatUserRec findOrCreate (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull MessageRec message) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOrCreate");

		) {

			// resolve stuff

			NumberRec number =
				message.getNumber ();

			// check for an existing ChatUser

			Optional <ChatUserRec> existingChatUserOptional =
				chatUserHelper.find (
					transaction,
					chat,
					number);

			if (
				optionalIsNotPresent (
					existingChatUserOptional)
			) {

				return create (
					transaction,
					chat,
					number);

			}

			ChatUserRec existingChatUser =
				optionalGetRequired (
					existingChatUserOptional);

			// check number

			if (

				! chatNumberReportLogic.isNumberReportSuccessful (
					transaction,
					number)

				&& isNull (
					number.getArchiveDate ())

			) {

				transaction.debugFormat (
					"Number archiving %s code %s",
					number.getNumber (),
					existingChatUser.getCode ());

				NumberRec newNumber =
					numberLogic.archiveNumberFromMessage (
						transaction,
						message);

				return create (
					transaction,
					chat,
					newNumber);

			}

			return existingChatUser;

		}

	}

	@Override
	public
	ChatUserRec findOrCreate (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOrCreate");

		) {

			return optionalOrElseRequired (
				chatUserHelper.find (
					transaction,
					chat,
					number),
				() -> create (
					transaction,
					chat,
					number));

		}

	}

	@Override
	public
	ChatUserRec create (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"create");

		) {

			// create him

			ChatUserRec chatUser =
				chatUserHelper.createInstance ()

				.setChat (
					chat)

				.setCode (
					randomLogic.generateNumericNoZero (6))

				.setCreated (
					transaction.now ())

				.setNumber (
					number)

				.setOldNumber (
					number)

				.setType (
					ChatUserType.user)

				.setDeliveryMethod (
					ChatMessageMethod.sms)

				.setGender (
					chat.getGender ())

				.setOrient (
					chat.getOrient ())

				.setCreditMode (
					number.getFree ()
						? ChatUserCreditMode.free
						: ChatUserCreditMode.billedMessages);

			chatUserHelper.insert (
				transaction,
				chatUser);

			return chatUser;

		}

	}

}