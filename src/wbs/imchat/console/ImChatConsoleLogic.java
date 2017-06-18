package wbs.imchat.console;

import java.util.Map;
import java.util.Set;

import wbs.framework.database.Transaction;

public
interface ImChatConsoleLogic {

	Set <Long> getSupervisorSearchIds (
			Transaction parentTransaction,
			Map <String, Set <String>> conditions);

	Set <Long> getSupervisorFilterIds (
			Transaction parentTransaction);

}
