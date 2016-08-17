package wbs.clients.apn.chat.user.core.logic;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;

public
class ChatUserLogicTest
	extends TestCase {

	// dependencies

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserLogicImplementation chatUserLogic;

	// data

	List<Orient> compatibleOrients =
		ImmutableList.<Orient>of (
			Orient.gay,
			Orient.gay,
			Orient.bi,
			Orient.bi,
			Orient.straight,
			Orient.straight);

	List<Gender> compatibleGenders =
		ImmutableList.<Gender>of (
			Gender.male,
			Gender.female,
			Gender.male,
			Gender.female,
			Gender.male,
			Gender.female);

	List<List<Boolean>> compatibleResults =
		ImmutableList.<List<Boolean>>of (

			ImmutableList.<Boolean>of (
				true, false, true, false, false, false),

			ImmutableList.<Boolean>of (
				false, true, false, true, false, false),

			ImmutableList.<Boolean>of (
				true, false, true, true, false, true),

			ImmutableList.<Boolean>of (
				false, true, true, true, true, false),

			ImmutableList.<Boolean>of (
				false, false, false, true, false, true),

			ImmutableList.<Boolean>of (
				false, false, true, false, true, false));

	public
	void testChatUsersCompatibleGenderOrientGenderOrient () {

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
						indexLeft,
						indexRight),

					(boolean)
					compatibleResults
						.get (indexLeft)
						.get (indexRight),

					(boolean)
					chatUserLogic.compatible (
						compatibleGenders.get (
							indexLeft),
						compatibleOrients.get (
							indexLeft),
						Optional.absent (),
						compatibleGenders.get (
							indexRight),
						compatibleOrients.get (
							indexRight),
						Optional.absent ()));

			}

		}

	}

	public
	void testChatUsersCompatibleChatUserChatUser () {

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
						chatUser1,
						chatUser2);

				assertEquals (
					stringFormat (
						"users %s and %s",
						index1,
						index2),
					expectedResult,
					actualResult);

			}

		}

	}

}
