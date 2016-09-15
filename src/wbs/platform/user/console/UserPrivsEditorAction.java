package wbs.platform.user.console;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Cleanup;

import wbs.console.action.ConsoleAction;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
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

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	PrivConsoleHelper privHelper;

	@SingletonDependency
	UpdateManager updateManager;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	@SingletonDependency
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
				"UserPrivsEditorAction.goReal ()",
				this);

		UserRec user =
			userHelper.findRequired (
				requestContext.stuffInteger (
					"userId"));

		Matcher matcher =
			privDataPattern.matcher (
				requestContext.parameterRequired (
					"privdata"));

		while (matcher.find ()) {

			Long privId =
				Long.parseLong (
					matcher.group (
						1));

			boolean grant =
				matcher.group (2).equals ("cangrant");

			boolean can =
				matcher.group (3).equals ("1");

			PrivRec priv =
				privHelper.findRequired (
					privId);

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
						userPrivHelper.createInstance ()

						.setPriv (
							priv)

						.setUser (
							user);

					if (grant) {

						userPriv

							.setCan (
								false)

							.setCanGrant (
								true);

					} else {

						userPriv

							.setCan (
								true)

							.setCanGrant (
								false);

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
					userConsoleLogic.userRequired (),
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
			0l);

		transaction.commit ();

		requestContext.addNotice ("Updated user privileges");

		return null;

	}

}
