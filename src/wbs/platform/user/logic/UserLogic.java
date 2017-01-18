package wbs.platform.user.logic;

import com.google.common.base.Optional;

import wbs.platform.user.model.UserRec;
import wbs.platform.user.model.UserSessionRec;

public
interface UserLogic {

	UserSessionRec userLogon (
			UserRec user,
			String sessionId,
			Optional <String> userAgent,
			Optional <String> consoleDeployment);

	void userLogoff (
			UserRec user);

	Optional <UserSessionRec> userLogonTry (
			String sliceCode,
			String username,
			String password,
			String sessionId,
			Optional <String> userAgent,
			Optional <String> consoleDeployment);

}
