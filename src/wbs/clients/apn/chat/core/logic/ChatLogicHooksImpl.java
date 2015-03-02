package wbs.clients.apn.chat.core.logic;

import lombok.Delegate;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.hooks.logic.HooksProxyImpl;

@SingletonComponent ("chatLogicHooksImpl")
public
class ChatLogicHooksImpl
	extends HooksProxyImpl<
		ChatLogicHooks,
		ChatLogicHooks.Proxy,
		ChatLogicHooks.Target>
	implements ChatLogicHooks.Proxy {

	public
	ChatLogicHooksImpl () {
		super (
			ChatLogicHooks.class,
			ChatLogicHooks.Proxy.class,
			ChatLogicHooks.Target.class);
	}

	@Delegate
	ChatLogicHooks delegate;

	@Override
	public
	void setDelegate (
			Object delegate) {

		this.delegate =
			(ChatLogicHooks) delegate;

	}

}
