package wbs.console.misc;

import com.google.common.base.Optional;

import wbs.utils.time.TimezoneTimeFormatter;

public
interface ConsoleUserHelper
	extends TimezoneTimeFormatter {

	Optional <Long> loggedInUserId ();
	Long loggedInUserIdRequired ();

	void login (
			Long userId);

	void logout ();

	boolean loggedIn ();
	boolean notLoggedIn ();

	Long hourOffset ();

}
