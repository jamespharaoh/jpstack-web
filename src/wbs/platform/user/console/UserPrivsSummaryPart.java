package wbs.platform.user.console;

import static wbs.framework.utils.etc.StringUtils.joinWithCommaAndSpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectManager;
import wbs.platform.group.model.GroupRec;
import wbs.platform.priv.model.PrivRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserPrivRec;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("userPrivsSummaryPart")
public
class UserPrivsSummaryPart
	extends AbstractPagePart {

	@Inject
	ObjectManager objectManager;

	@Inject
	UserPrivChecker privChecker;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserObjectHelper userHelper;

	Set<PrivStuff> privStuffs;

	@Override
	public
	void prepare () {

		Map<Long,PrivStuff> privStuffsByPrivId =
			new HashMap<> ();

		UserRec user =
			userHelper.findRequired (
				requestContext.stuffInteger (
					"userId"));

		// load up some info about the acting user

		for (
			UserPrivRec userPriv
				: user.getUserPrivs ()
		) {

			// check we can see this priv

			if (
				! privChecker.canGrant (
					userPriv.getPriv ().getId ())
			) {
				continue;
			}

			PrivRec priv =
				userPriv.getPriv ();

			// create PrivStuff

			PrivStuff privStuff =
				new PrivStuff ();

			Record<?> parent =
				objectManager.getParent (
					priv);

			privStuff.path =
				objectManager.objectPath (
					parent,
					userConsoleLogic.sliceRequired ());

			privStuff.privCode =
				priv.getCode ();

			privStuff.userPriv =
				userPriv;

			privStuffsByPrivId.put (
				priv.getId (),
				privStuff);

		}

		for (GroupRec group
				: user.getGroups ()) {

			for (PrivRec priv
					: group.getPrivs ()) {

				// check we can see this priv

				if (! privChecker.canGrant (
						priv.getId ()))
					continue;

				// find or create the priv stuff

				PrivStuff privStuff =
					privStuffsByPrivId.get (
						priv.getId ());

				if (privStuff == null) {

					privStuff =
						new PrivStuff ();

					Record<?> parent =
						objectManager.getParent (
							priv);

					privStuff.path =
						objectManager.objectPath (
							parent,
							userConsoleLogic.sliceRequired ());

					privStuff.privCode =
						priv.getCode ();

					privStuffsByPrivId.put (
						priv.getId (),
						privStuff);

				}

				// and add this group to it

				privStuff.groups.add (
					group.getCode ());

			}

		}

		privStuffs =
			new TreeSet<PrivStuff> (
				privStuffsByPrivId.values ());

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"list\">");

		printFormat (
			"<tr>\n",
			"<th>Object</th>\n",
			"<th>Priv</th>\n",
			"<th>Can</th>\n",
			"<th>Grant</th>\n",
			"<th>Groups</th>\n",
			"</tr>\n");

		for (PrivStuff privStuff
				: privStuffs) {

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				privStuff.path,

				"<td>%h</td>\n",
				privStuff.privCode,

				"<td>%h</td>\n",
				privStuff.userPriv != null
						&& privStuff.userPriv.getCan ()
					? "yes"
					: "no",

				"<td>%h</td>\n",
				privStuff.userPriv != null
						&& privStuff.userPriv.getCanGrant ()
					? "yes"
					: "no",

				"<td>%h</td>\n",
				joinWithCommaAndSpace (
					privStuff.groups),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

	class PrivStuff
		implements Comparable<PrivStuff> {

		String path;
		String privCode;
		UserPrivRec userPriv;

		List<String> groups =
			new ArrayList<String> ();

		@Override
		public
		int compareTo (
				PrivStuff other) {

			return new CompareToBuilder ()
				.append (path, other.path)
				.append (privCode, other.privCode)
				.toComparison ();

		}

	}

}
