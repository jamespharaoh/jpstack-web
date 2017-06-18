package wbs.smsapps.manualresponder.console;

import java.util.Map;
import java.util.Set;

import wbs.framework.database.Transaction;

public
interface ManualResponderConsoleLogic {

	Set <Long> getSupervisorSearchIds (
			Transaction parentTransaction,
			Map <String, Set <String>> conditions);

	Set <Long> getSupervisorFilterIds (
			Transaction parentTransaction);

}
