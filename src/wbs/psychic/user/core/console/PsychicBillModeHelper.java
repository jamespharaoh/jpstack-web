package wbs.psychic.user.core.console;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.helper.EnumConsoleHelper;
import wbs.psychic.bill.model.PsychicBillMode;

@SingletonComponent ("psychicBillModelHelper")
public
class PsychicBillModeHelper
	extends EnumConsoleHelper<PsychicBillMode> {

	{

		enumClass (PsychicBillMode.class);

		add (PsychicBillMode.normal, "normal");
		add (PsychicBillMode.prepay, "pre-pay");
		add (PsychicBillMode.free, "free");
		add (PsychicBillMode.barred, "barred");

	}

}
