package wbs.platform.user.logic;

import static wbs.framework.utils.etc.Misc.hashSha1;

import java.util.Date;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.GlobalId;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserOnlineObjectHelper;
import wbs.platform.user.model.UserOnlineRec;
import wbs.platform.user.model.UserRec;
import wbs.platform.user.model.UserSessionObjectHelper;
import wbs.platform.user.model.UserSessionRec;

@SingletonComponent ("userLogic")
public
class UserLogicImpl
	implements UserLogic {

	// dependencies

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	UserObjectHelper userHelper;

	@Inject
	UserOnlineObjectHelper userOnlineHelper;

	@Inject
	UserSessionObjectHelper userSessionHelper;

	// implementation

	@Override
	public
	void userLogon (
			UserRec user,
			String sessionId) {

		Date now =
			new Date ();

		// end any existing session

		userLogoff (user);

		// start the session log

		UserSessionRec session =
			userSessionHelper.insert (
				new UserSessionRec ()
					.setUser (user)
					.setStartTime (now));

		// go online

		userOnlineHelper.insert (
			new UserOnlineRec ()
				.setUser (user)
				.setSessionId (sessionId)
				.setTimestamp (now)
				.setUserSession (session));

	}

	@Override
	public
	void userLogoff (
			UserRec user) {

		UserOnlineRec userOnline =
			userOnlineHelper.find (
				user.getId ());

		if (userOnline == null)
			return;

		// end the session log

		UserSessionRec userSession =
			userOnline.getUserSession ();

		if (userSession != null) {

			userSession.setEndTime (
				userOnline.getTimestamp ());

		}

		// go offline

		userOnlineHelper.remove (
			userOnline);

	}

	@Override
	public
	Integer userLogonTry (
			String sliceCode,
			String username,
			String password,
			String sessionId) {

		// lookup the user

		SliceRec slice =
			sliceHelper.findByCode (
				GlobalId.root,
				sliceCode);

		if (sliceCode == null)
			return null;

		UserRec user =
			userHelper.findByCode (
				slice,
				username);

		if (user == null)
			return null;

		// check password

		if (user.getPassword () == null
				|| ! user.getPassword ().equals (hashSha1 (password)))

			return null;

		// update user (bring online)

		userLogon (
			user,
			sessionId);

		// and return

		return user.getId ();

	}

}
