package wbs.clients.apn.chat.user.core.console;

import java.util.Comparator;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;

public
class ChatUserOnlineComparator
	implements Comparator<ChatUserRec> {

	private
	long order (
			ChatUserRec chatUser) {

		if (chatUser.getLastAction () != null)
			return chatUser.getLastAction ().getTime ();

		if (chatUser.getType () == ChatUserType.user)
			return -1L;

		if (chatUser.getType () == ChatUserType.monitor)
			return -2L;

		throw new RuntimeException ();

	}

	@Override
	public
	int compare (
			ChatUserRec chatUser1,
			ChatUserRec chatUser2) {

		long order1 =
			order (chatUser1);

		long order2 =
			order (chatUser2);

		if (order2 < order1)
			return -1;

		if (order1 < order2)
			return +1;

		return chatUser1.getCode ().compareTo (
			chatUser2.getCode ());

	}

	public final static
	ChatUserOnlineComparator INSTANCE =
		new ChatUserOnlineComparator ();

}
