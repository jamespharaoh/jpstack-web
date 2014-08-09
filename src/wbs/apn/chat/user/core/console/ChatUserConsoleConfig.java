package wbs.apn.chat.user.core.console;

import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("chatUserConfigConfig")
public
class ChatUserConsoleConfig {

	@SingletonComponent ("chatUserSearchItemsPerSubPage")
	public
	Integer chatUserSearchItemsPerSubPage () {

		return 30;

	}

	@SingletonComponent ("chatUserSearchSubPagesPerPage")
	public
	Integer chatUserSearchSubPagesPerPage () {

		return 20;

	}

}
