package wbs.psychic.keyword.hibernate;

import java.sql.Types;

import wbs.framework.hibernate.EnumUserType;
import wbs.psychic.keyword.model.PsychicKeywordType;

public
class PsychicKeywordTypeType
	extends EnumUserType<String,PsychicKeywordType> {

	{

		sqlType (Types.VARCHAR);
		enumClass (PsychicKeywordType.class);

		add ("command", PsychicKeywordType.command);
		add ("join", PsychicKeywordType.join);

	}

}
