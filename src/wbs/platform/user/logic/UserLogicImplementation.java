package wbs.platform.user.logic;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.hashSha1;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserOnlineObjectHelper;
import wbs.platform.user.model.UserOnlineRec;
import wbs.platform.user.model.UserRec;
import wbs.platform.user.model.UserSessionObjectHelper;
import wbs.platform.user.model.UserSessionRec;

@SingletonComponent ("userLogic")
public
class UserLogicImplementation
	implements UserLogic {

	// dependencies

	@Inject
	Database database;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	UserObjectHelper userHelper;

	@Inject
	UserOnlineObjectHelper userOnlineHelper;

	@Inject
	UserSessionObjectHelper userSessionHelper;

	@Inject
	WbsConfig wbsConfig;

	// implementation

	@Override
	public
	void userLogon (
			@NonNull UserRec user,
			@NonNull String sessionId,
			@NonNull Optional<String> userAgent) {

		Transaction transaction =
			database.currentTransaction ();

		// end any existing session

		userLogoff (
			user);

		// start the session log

		UserSessionRec session =
			userSessionHelper.insert (
				userSessionHelper.createInstance ()

			.setUser (
				user)

			.setStartTime (
				transaction.now ())

			.setUserAgent (
				textHelper.findOrCreate (
					userAgent.orNull ()))

		);

		// go online

		userOnlineHelper.insert (
			userOnlineHelper.createInstance ()

			.setUser (
				user)

			.setSessionId (
				sessionId)

			.setTimestamp (
				transaction.now ())

			.setUserSession (
				session)

		);

	}

	@Override
	public
	void userLogoff (
			@NonNull UserRec user) {

		UserOnlineRec userOnline =
			userOnlineHelper.findOrNull (
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
	Long userLogonTry (
			@NonNull String sliceCode,
			@NonNull String username,
			@NonNull String password,
			@NonNull String sessionId,
			@NonNull Optional<String> userAgent) {

		// lookup the user

		SliceRec slice =
			sliceHelper.findByCodeOrNull (
				GlobalId.root,
				sliceCode);

		if (slice == null)
			return null;

		UserRec user =
			userHelper.findByCodeOrNull (
				slice,
				username);

		if (user == null)
			return null;

		// check password

		if (
			! checkPassword (
				user,
				password)
		) {

			return null;

		}

		// update user (bring online)

		userLogon (
			user,
			sessionId,
			userAgent);

		// and return

		return (long) (int)
			user.getId ();

	}

	boolean checkPassword (
			@NonNull UserRec user,
			@NonNull String password) {

		// bypass for dev auto-login

		if (

			equal (
				password,
				"**********")

			&& wbsConfig.testUsers ().contains (
				stringFormat (
					"%s.%s",
					user.getSlice ().getCode (),
					user.getUsername ()))

		) {

			return true;

		}

		// always fail if user has no password

		if (user.getPassword () == null) {

			return false;

		}

		// fail if password hash doesn't match

		if (
			notEqual (
				user.getPassword (),
				hashSha1 (password))
		) {

			return false;

		}

		// all correct

		return true;

	}

}
