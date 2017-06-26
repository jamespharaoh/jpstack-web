package wbs.apn.chat.broadcast.console;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import wbs.console.helper.core.ConsoleHooks;

import wbs.framework.component.annotations.SingletonComponent;

import wbs.apn.chat.broadcast.model.ChatBroadcastRec;

@SingletonComponent ("chatBroadcastConsoleHooks")
public
class ChatBroadcastConsoleHooks
	implements ConsoleHooks <ChatBroadcastRec> {

	// public implementation

	@Override
	public
	Ordering <ChatBroadcastRec> defaultOrdering () {

		return Ordering.compound (
			ImmutableList.of (
				Ordering.natural ().reverse ().onResultOf (
					ChatBroadcastRec::getCreatedTime),
				Ordering.natural ().reverse ().onResultOf (
					ChatBroadcastRec::getId)));

	}

}
