package wbs.clients.apn.chat.core.logic;

import lombok.experimental.Delegate;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.platform.hooks.logic.HooksProxyImplementation;

@SingletonComponent ("chatLogicHooksImpl")
public
class ChatLogicHooksImplementation
	extends HooksProxyImplementation<
		ChatLogicHooks,
		ChatLogicHooks.Proxy,
		ChatLogicHooks.Target>
	implements ChatLogicHooks.Proxy {

	public
	ChatLogicHooksImplementation () {
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
			(ChatLogicHooks)
			delegate;

	}

}
