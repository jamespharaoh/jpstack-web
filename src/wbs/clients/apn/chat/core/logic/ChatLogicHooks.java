package wbs.clients.apn.chat.core.logic;

import java.util.List;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.platform.hooks.logic.HooksProxy;
import wbs.platform.hooks.logic.HooksTarget;

public
interface ChatLogicHooks {

	void chatUserSignupComplete (
			ChatUserRec chatUser);

	void collectChatUserCharges (
			ChatUserRec chatUser,
			List<ChatUserCharge> internal,
			List<ChatUserCharge> external);

	public
	class ChatUserCharge {

		public
		String name;

		public
		int count;

		public
		int charge;

	}

	static
	interface Proxy
		extends
			ChatLogicHooks,
			HooksProxy {

	}

	static
	interface Target
		extends
			ChatLogicHooks,
			HooksTarget {

	}

	static
	class Abstract
		implements Target {

		@Override
		public
		void chatUserSignupComplete (
				ChatUserRec chatUser) {

		}

		@Override
		public
		void collectChatUserCharges (
				ChatUserRec chatUser,
				List<ChatUserCharge> internal,
				List<ChatUserCharge> external) {
		}

	}

}
