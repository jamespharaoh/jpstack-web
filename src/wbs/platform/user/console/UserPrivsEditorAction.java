package wbs.platform.user.console;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.priv.console.PrivConsoleHelper;
import wbs.platform.priv.model.PrivRec;
import wbs.platform.updatelog.logic.UpdateManager;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserPrivObjectHelper;
import wbs.platform.user.model.UserPrivRec;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("userPrivsEditorAction")
public
class UserPrivsEditorAction
	extends ConsoleAction {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	PrivChecker privChecker;

	@Inject
	PrivConsoleHelper privHelper;

	@Inject
	UpdateManager updateManager;

	@Inject
	UserObjectHelper userHelper;

	@Inject
	UserPrivObjectHelper userPrivHelper;

	static
	Pattern privDataPattern =
		Pattern.compile ("(\\d+)-(can|cangrant)=(0|1)");

	@Override
	public
	Responder backupResponder () {
		return responder ("userPrivsEditorResponder");
	}

	@Override
	public
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		UserRec user =
			userHelper.find (
				requestContext.stuffInt ("userId"));

		Matcher matcher =
			privDataPattern.matcher (
				requestContext.parameter ("privdata"));

		while (matcher.find ()) {

			int privId =
				Integer.parseInt (matcher.group (1));

			boolean grant =
				matcher.group (2).equals ("cangrant");

			boolean can =
				matcher.group (3).equals ("1");

			PrivRec priv =
				privHelper.find (privId);

			UserPrivRec userPriv =
				userPrivHelper.find (
					user,
					priv);

			// check we have permission to update this priv

			if (! privChecker.canGrant (priv.getId ()))
				continue;

			boolean changed = false;

			if (userPriv == null) {

				// create new UserPriv

				if (can) {

					userPriv =
						new UserPrivRec ()
							.setPriv (priv)
							.setUser (user);

					if (grant) {

						userPriv
							.setCan (false)
							.setCanGrant (true);

					} else {

						userPriv
							.setCan (true)
							.setCanGrant (false);

					}

					userPrivHelper.insert (
						userPriv);

					changed = true;

				}

			} else {

				// update userpriv

				if (grant) {

					if (userPriv.getCanGrant () != can)
						changed = true;

					userPriv.setCanGrant (can);

				} else {

					if (userPriv.getCan () != can)
						changed = true;

					userPriv.setCan (can);

				}

				// remove if necessary

				if (! userPriv.getCan ()
						&& ! userPriv.getCanGrant ()) {

					userPrivHelper.remove (
						userPriv);

				}

			}

			// create event

			if (changed) {

				eventLogic.createEvent (
					can
						? (grant
							? "user_grant_grant"
							: "user_grant")
						: (grant
							? "user_revoke_grant"
							: "user_revoke"),
					myUser,
					priv,
					user);

			}

		}

		// signal the privs have been updated

		updateManager.signalUpdate (
			"user_privs",
			user.getId ());

		updateManager.signalUpdate (
			"privs",
			0);

		transaction.commit ();

		requestContext.addNotice ("Updated user privileges");

		return null;

	}

}
