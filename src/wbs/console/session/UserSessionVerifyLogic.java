package wbs.console.session;

import wbs.framework.logging.TaskLogger;

public
interface UserSessionVerifyLogic {

	boolean userSessionVerify (
			TaskLogger parentTaskLogger,
			String sessionId,
			Long userId,
			Boolean forceReload);

}
