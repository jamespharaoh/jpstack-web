package wbs.apn.chat.core.logic;

import static wbs.utils.string.StringUtils.stringEqualSafe;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.object.ObjectManager;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.lookup.logic.NumberLookupHelper;
import wbs.sms.number.lookup.model.NumberLookupRec;

@SingletonComponent ("chatNumberLookupHelper")
public
class ChatNumberLookupHelper
	implements NumberLookupHelper {

	// singleton dependencies

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ObjectManager objectManager;

	// details

	@Override
	public
	String parentObjectTypeCode () {
		return "chat";
	}

	// implementation

	@Override
	public
	boolean lookupNumber (
			NumberLookupRec numberLookup,
			NumberRec number) {

		ChatRec chat =
			(ChatRec)
			objectManager.getParentOrNull (
				numberLookup);

		if (
			stringEqualSafe (
				numberLookup.getCode (),
				"block_all")
		) {

			ChatUserRec chatUser =
				chatUserHelper.find (
					chat,
					number);

			if (chatUser == null)
				return false;

			return chatUser.getBlockAll ();

		} else {

			throw new RuntimeException ();

		}

	}

}
