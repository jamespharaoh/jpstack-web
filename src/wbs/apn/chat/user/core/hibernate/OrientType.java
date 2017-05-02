package wbs.apn.chat.user.core.hibernate;

import java.sql.Types;

import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

import wbs.framework.hibernate.EnumUserType;

import wbs.apn.chat.user.core.model.Orient;

public
class OrientType
	extends EnumUserType<String,Orient> {

	{

		sqlType (Types.VARCHAR);
		enumClass (Orient.class);

		add ("g", Orient.gay);
		add ("s", Orient.straight);
		add ("b", Orient.bi);

	}

	public final static Type INSTANCE =
		new CustomType (
			new OrientType ());

}
