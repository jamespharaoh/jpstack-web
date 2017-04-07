package wbs.platform.user.console;

import com.google.common.base.Optional;

import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.model.UserRec;

import wbs.utils.time.TimezoneTimeFormatter;

public
interface UserConsoleLogic
	extends TimezoneTimeFormatter {

	Optional <UserRec> user ();
	UserRec userRequired ();

	Optional <SliceRec> slice ();
	SliceRec sliceRequired ();

	Optional <Long> userId ();
	Long userIdRequired ();

	Optional <Long> sliceId ();
	Long sliceIdRequired ();

	boolean loggedIn ();
	boolean notLoggedIn ();

}
