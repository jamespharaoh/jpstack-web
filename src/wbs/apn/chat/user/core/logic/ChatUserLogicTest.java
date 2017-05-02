package wbs.apn.chat.user.core.logic;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import junit.framework.TestCase;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;

public
class ChatUserLogicTest
	extends TestCase {

	// singleton dependencies

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogicImplementation chatUserLogic;

	@ClassSingletonDependency
	LogContext logContext;

	// data

	List <Orient> compatibleOrients =
		ImmutableList.of (
			Orient.gay,
			Orient.gay,
			Orient.bi,
			Orient.bi,
			Orient.straight,
			Orient.straight);

	List <Gender> compatibleGenders =
		ImmutableList.of (
			Gender.male,
			Gender.female,
			Gender.male,
			Gender.female,
			Gender.male,
			Gender.female);

	List <List <Boolean>> compatibleResults =
		ImmutableList.of (

			ImmutableList.of (
				true, false, true, false, false, false),

			ImmutableList.of (
				false, true, false, true, false, false),

			ImmutableList.of (
				true, false, true, true, false, true),

			ImmutableList.of (
				false, true, true, true, true, false),

			ImmutableList.of (
				false, false, false, true, false, true),

			ImmutableList.of (
				false, false, true, false, true, false));

	public
	void testChatUsersCompatibleGenderOrientGenderOrient (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"testChatUsersCompatibleGenderOrientGenderOrient");

		) {

			for (
				int indexLeft = 0;
				indexLeft < 6;
				indexLeft ++
			) {

				for (
					int indexRight = 0;
					indexRight < 6;
					indexRight ++
				) {

					assertEquals (

						stringFormat (
							"users %s and %s",
							integerToDecimalString (
								indexLeft),
							integerToDecimalString (
								indexRight)),

						(boolean)
						compatibleResults
							.get (indexLeft)
							.get (indexRight),

						(boolean)
						chatUserLogic.compatible (
							transaction,
							compatibleGenders.get (
								indexLeft),
							compatibleOrients.get (
								indexLeft),
							optionalAbsent (),
							compatibleGenders.get (
								indexRight),
							compatibleOrients.get (
								indexRight),
							optionalAbsent ()));

				}

			}

		}

	}

	public
	void testChatUsersCompatibleChatUserChatUser (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"testChatUsersCompatibleChatUserChatUser");

		) {

			ChatUserRec chatUser1 =
				chatUserHelper.createInstance ();

			ChatUserRec chatUser2 =
				chatUserHelper.createInstance ();

			for (
				int index1 = 0;
				index1 < 6;
				index1 ++
			) {

				for (
					int index2 = 0;
					index2 < 6;
					index2 ++
				) {

					chatUser1

						.setGender (
							compatibleGenders.get (
								index1))

						.setOrient (
							compatibleOrients.get (
								index1));

					chatUser2

						.setGender (
							compatibleGenders.get (
								index2))

						.setOrient (
							compatibleOrients.get (
								index2));

					boolean expectedResult =
						compatibleResults
							.get (index1)
							.get (index2);

					boolean actualResult =
						chatUserLogic.compatible (
							transaction,
							chatUser1,
							chatUser2);

					assertEquals (
						stringFormat (
							"users %s and %s",
							integerToDecimalString (
								index1),
							integerToDecimalString (
								index2)),
						expectedResult,
						actualResult);

				}

			}

		}

	}

}
