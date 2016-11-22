package wbs.platform.user.logic;

import static wbs.utils.etc.Misc.hashSha1Base64;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.platform.scaffold.model.SliceObjectHelper;
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

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	UserObjectHelper userHelper;

	@SingletonDependency
	UserOnlineObjectHelper userOnlineHelper;

	@SingletonDependency
	UserSessionObjectHelper userSessionHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// implementation

	@Override
	public
	void userLogon (
			@NonNull UserRec user,
			@NonNull String sessionId,
			@NonNull Optional <String> userAgent) {

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

		Optional<UserOnlineRec> userOnlineOptional =
			userOnlineHelper.find (
				user.getId ());

		if (
			optionalIsNotPresent (
				userOnlineOptional)
		) {
			return;
		}

		UserOnlineRec userOnline =
			userOnlineOptional.get ();

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

		Optional<UserRec> userOptional =
			userHelper.findByCode (
				GlobalId.root,
				sliceCode,
				username);

		if (
			optionalIsNotPresent (
				userOptional)
		) {
			return null;
		}

		UserRec user =
			userOptional.get ();

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

		return user.getId ();

	}

	boolean checkPassword (
			@NonNull UserRec user,
			@NonNull String password) {

		// bypass for dev auto-login

		if (

			stringEqualSafe (
				password,
				"**********")

			&& wbsConfig.testUsers ().contains (
				joinWithFullStop (
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
			stringNotEqualSafe (
				user.getPassword (),
				hashSha1Base64 (
					password))
		) {

			return false;

		}

		// all correct

		return true;

	}

}
