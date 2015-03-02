package wbs.clients.apn.chat.user.core.hibernate;

import java.sql.Types;

import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.framework.hibernate.EnumUserType;

public
class GenderType
	extends EnumUserType<String,Gender> {

	{

		sqlType (Types.VARCHAR);
		enumClass (Gender.class);

		add ("m", Gender.male);
		add ("f", Gender.female);

	}

	public final static
	Type INSTANCE =
		new CustomType (
			new GenderType ());

}
