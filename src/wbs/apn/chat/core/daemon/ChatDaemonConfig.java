package wbs.apn.chat.core.daemon;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.platform.misc.SymbolicLock;

@SingletonComponent ("chatDaemonConfig")
public
class ChatDaemonConfig {

	@SingletonComponent ("chatUserDeliveryLocks")
	public
	SymbolicLock<Integer> chatUserDeliveryLocks () {
		return new SymbolicLock<Integer> ();
	}

}
