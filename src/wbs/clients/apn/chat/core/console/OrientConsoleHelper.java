package wbs.clients.apn.chat.core.console;

import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.console.helper.EnumConsoleHelper;
import wbs.framework.application.annotations.SingletonComponent;

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
