package wbs.platform.priv.console;

import static wbs.framework.utils.etc.StringUtils.joinWithCommaAndSpace;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.inject.Inject;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.lookup.ObjectLookup;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.Html;
import wbs.platform.group.console.GroupConsoleHelper;
import wbs.platform.group.model.GroupRec;
import wbs.platform.priv.model.PrivRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserPrivRec;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("objectSummaryPrivPart")
public
class ObjectSummaryPrivPart
	extends AbstractPagePart {

	@Inject
	GroupConsoleHelper groupHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	UserPrivChecker privChecker;

	@Inject
	UserObjectHelper userHelper;;

	Record<?> object;

	Map<String,UserPrivSets> userPrivs =
		new TreeMap<String,UserPrivSets> ();

	Map<String,Set<String>> groupPrivs =
		new TreeMap<String,Set<String>> ();

	Map<String,UserRec> users =
		new TreeMap<String,UserRec> ();

	Map<String,GroupRec> groups =
		new TreeMap<String,GroupRec> ();

	@Override
	public
	void prepare () {

		@SuppressWarnings ("unchecked")
		ObjectLookup<? extends Record<?>> objectLookup =
			(ObjectLookup<? extends Record<?>>)
				requestContext.stuff ("dataObjectLookup");

		object =
			objectLookup.lookupObject (
				requestContext.contextStuff ());

		List<PrivRec> privs =
			objectManager.getChildren (
				object,
				PrivRec.class);

		// for each priv

		for (
			PrivRec priv
				: privs
		) {

			// build user priv list

			for (
				UserPrivRec userPriv
					: priv.getUserPrivs ()
			) {

				String userPath =
					objectManager.objectPathMini (
						userPriv.getUser ());

				UserPrivSets userPrivSets =
					userPrivs.get (userPath);

				if (userPrivSets == null) {

					userPrivs.put (
						userPath,
						userPrivSets = new UserPrivSets ());

				}

				if (userPriv.getCan ()) {

					userPrivSets.canPrivCodes.add (
						priv.getCode ());

				}

				if (userPriv.getCanGrant ()) {

					userPrivSets.canGrantPrivCodes.add (
						priv.getCode ());

				}

			}

			// build group priv list

			for (
				GroupRec group
					: priv.getGroups ()
			) {

				String groupPath =
					objectManager.objectPathMini (
						group);

				Set<String> privCodes =
					groupPrivs.get (groupPath);

				if (privCodes == null) {

					privCodes =
						new TreeSet<String>();

					groupPrivs.put (
						groupPath,
						privCodes);

				}

				privCodes.add (
					priv.getCode ());

			}

		}

		for (UserRec user
				: userHelper.findAll ()) {

			if (! objectManager.canView (user))
				continue;

			users.put (
				objectManager.objectPathMini (
					user),
				user);

		}

		for (
			GroupRec group
				: groupHelper.findAll ()
		) {

			if (! objectManager.canView (group))
				continue;

			groups.put (
				objectManager.objectPathMini (
					group),
				group);

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<h2>Users</h2>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>User</th>\n",
			"<th>Privs</th>\n",
			"<th>Grant privs</th>\n",
			"</tr>\n");

		if (userPrivs.size () == 0) {

			printFormat (
				"<td colspan=\"3\">No user privs to show</td>\n");

		}

		for (Map.Entry<String,UserPrivSets> entry
				: userPrivs.entrySet ()) {

			String userPath =
				entry.getKey ();

			UserPrivSets userPrivSets =
				entry.getValue ();

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				userPath,

				"<td>%h</td>\n",
				joinWithCommaAndSpace (
					userPrivSets.canPrivCodes),

				"<td>%h</td>\n",
				joinWithCommaAndSpace (
					userPrivSets.canGrantPrivCodes),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

		printFormat (
			"<form",
			" method=\"get\"",
			" action=\"\"",
			">\n");

		printFormat (
			"<p>Edit privs for user to user<br>\n",

			"<select name=\"userId\">\n");

		for (Map.Entry<String, UserRec> entry :
				users.entrySet ()) {

			printFormat (
				"%s\n",
				Html.option (
					entry.getValue ().getId ().toString (),
					entry.getKey (),
					null));

		}

		printFormat (
			"</select>\n",

			"<input",
			" type=\"submit\"",
			" value=\"go\"",
			"></p>\n");

		printFormat (
			"</form>\n");

		printFormat (
			"<h2>Groups</h2>\n");

		printFormat (
			"<table class=\"list\">");

		printFormat (
			"<tr>\n",
			"<th>Group</th>\n",
			"<th>Privs</th>\n",
			"</tr>");

		if (groupPrivs.size () == 0) {

			printFormat (
				"<td colspan=\"3\">No user privs to show</td>\n");

		}

		for (Map.Entry<String, Set<String>> entry
				: groupPrivs.entrySet ()) {

			String groupPath =
				entry.getKey ();

			Set<String> privCodes =
				entry.getValue ();

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				groupPath,

				"<td>%h</td>\n",
				joinWithCommaAndSpace (
					privCodes),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

	@Accessors (fluent = true)
	@Data
	public static
	class UserPrivSets {

		Set<String> canPrivCodes =
			new TreeSet<String> ();

		Set<String> canGrantPrivCodes =
			new TreeSet<String> ();

	}

}
