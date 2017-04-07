package wbs.platform.user.console;

import com.google.common.base.Optional;

import wbs.framework.logging.TaskLogger;

import wbs.platform.user.model.UserRec;
import wbs.platform.user.model.UserSessionRec;

public
interface UserSessionLogic {

	UserSessionRec userLogon (
			TaskLogger parentTaskLogger,
			UserRec user,
			Optional <String> userAgent,
			Optional <String> consoleDeploymentCode);

	Optional <UserSessionRec> userLogonTry (
			TaskLogger parentTaskLogger,
			String sliceCode,
			String username,
			String password,
			Optional <String> userAgent,
			Optional <String> consoleDeploymentCode);

	void userLogoff (
			TaskLogger parentTaskLogger,
			UserRec user);

	boolean userSessionVerify (
			TaskLogger parentTaskLogger);

}
