package wbs.clients.apn.chat.core.console;

import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.helper.EnumConsoleHelper;

@SingletonComponent ("orientConsoleHelper")
public
class OrientConsoleHelper
	extends EnumConsoleHelper<Orient> {

	{

		enumClass (Orient.class);

		add (Orient.gay, "gay");
		add (Orient.bi, "bi");
		add (Orient.straight, "straight");

	}

}
