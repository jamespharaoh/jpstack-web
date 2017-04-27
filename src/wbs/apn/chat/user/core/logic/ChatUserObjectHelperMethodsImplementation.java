package wbs.apn.chat.user.core.logic;

import static wbs.utils.etc.Misc.isNull;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.BorrowedTransaction;
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatRec chat,
			@NonNull MessageRec message) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"findOrCreate");

		) {

			// resolve stuff

			NumberRec number =
				message.getNumber ();

			// check for an existing ChatUser

			ChatUserRec chatUser =
				chatUserHelper.find (
					chat,
					number);

			if (chatUser != null) {

				// check number

				if (

					! chatNumberReportLogic.isNumberReportSuccessful (
						taskLogger,
						number)

					&& isNull (
						number.getArchiveDate ())

				) {

					taskLogger.debugFormat (
						"Number archiving %s code %s",
						number.getNumber (),
						chatUser.getCode ());

					NumberRec newNumber =
						numberLogic.archiveNumberFromMessage (
							taskLogger,
							message);

					return create (
						taskLogger,
						chat,
						newNumber);

				}

				return chatUser;

			}

			return create (
				taskLogger,
				chat,
				number);

		}

	}

	@Override
	public
	ChatUserRec findOrCreate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatRec chat,
			@NonNull NumberRec number) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"findOrCreate");

		) {

			// check for an existing ChatUser

			ChatUserRec chatUser =
				chatUserHelper.find (
					chat,
					number);

			if (chatUser != null)
				return chatUser;

			return create (
				taskLogger,
				chat,
				number);

		}

	}

	@Override
	public
	ChatUserRec create (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatRec chat,
			@NonNull NumberRec number) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"create");

		) {

			BorrowedTransaction transaction =
				database.currentTransaction ();

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
				taskLogger,
				chatUser);

			return chatUser;

		}

	}

}