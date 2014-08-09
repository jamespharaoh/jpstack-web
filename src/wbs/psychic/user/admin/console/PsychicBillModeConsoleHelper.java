package wbs.psychic.user.admin.console;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.helper.EnumConsoleHelper;
import wbs.psychic.bill.model.PsychicBillMode;

@SingletonComponent ("psychicBillModelConsoleHelper")
public
class PsychicBillModeConsoleHelper
	extends EnumConsoleHelper<PsychicBillMode> {

	{

		enumClass (PsychicBillMode.class);

		add (PsychicBillMode.normal, "normal");
		add (PsychicBillMode.prepay, "prepay");
		add (PsychicBillMode.free, "free");
		add (PsychicBillMode.barred, "barred");

	}

}
