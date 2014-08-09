package wbs.psychic.keyword.console;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.helper.EnumConsoleHelper;
import wbs.psychic.keyword.model.PsychicKeywordType;

@SingletonComponent ("psychicKeywordTypeEnumHelper")
public
class PsychicKeywordTypeEnumHelper
	extends EnumConsoleHelper<PsychicKeywordType> {

	{

		enumClass (PsychicKeywordType.class);

		add (PsychicKeywordType.command, "command");
		add (PsychicKeywordType.join, "join");

	}

}
