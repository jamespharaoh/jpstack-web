package wbs.psychic.bill.hibernate;

import java.sql.Types;

import wbs.framework.hibernate.EnumUserType;
import wbs.psychic.bill.model.PsychicBillMode;

public
class PsychicBillModeType
	extends EnumUserType<String,PsychicBillMode> {

	{

		sqlType (Types.VARCHAR);
		enumClass (PsychicBillMode.class);

		add ("normal", PsychicBillMode.normal);
		add ("prepay", PsychicBillMode.prepay);
		add ("free", PsychicBillMode.free);
		add ("barred", PsychicBillMode.barred);

	}

}
