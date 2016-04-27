package wbs.platform.user.logic;

import com.google.common.base.Optional;

import wbs.platform.user.model.UserRec;

public
interface UserLogic {

	/**
	 * Logs the specified user onto the system. This will first call userLogoff
	 * to end any existing session. The user record is then updated and a new
	 * UserSession created.
	 */
	void userLogon (
			UserRec user,
			String sessionId,
			Optional<String> userAgent);

	/**
	 * Logs the specified user off the system. This includes updating all
	 * relevant fields and updating the UserSession. This does nothing if the
	 * user is not currently online.
	 */
	void userLogoff (
			UserRec user);

	Long userLogonTry (
			String sliceCode,
			String username,
			String password,
			String sessionId,
			Optional<String> userAgent);

}
