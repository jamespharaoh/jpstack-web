package wbs.clients.apn.chat.core.console;

import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.helper.EnumConsoleHelper;

@SingletonComponent ("genderConsoleHelper")
public
class GenderConsoleHelper
	extends EnumConsoleHelper<Gender> {

	{

		enumClass (Gender.class);

		add (Gender.male, "male");
		add (Gender.female, "female");

	}

}
