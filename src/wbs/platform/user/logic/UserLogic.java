package wbs.platform.user.logic;

import com.google.common.base.Optional;

import wbs.framework.logging.TaskLogger;

import wbs.platform.user.model.UserRec;
import wbs.platform.user.model.UserSessionRec;

public
interface UserLogic {

	UserSessionRec userLogon (
			TaskLogger parentTaskLogger,
			UserRec user,
			String sessionId,
			Optional <String> userAgent,
			Optional <String> consoleDeployment);

	void userLogoff (
			TaskLogger parentTaskLogger,
			UserRec user);

	Optional <UserSessionRec> userLogonTry (
			TaskLogger parentTaskLogger,
			String sliceCode,
			String username,
			String password,
			String sessionId,
			Optional <String> userAgent,
			Optional <String> consoleDeployment);

}
