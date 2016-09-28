package wbs.platform.user.console;

import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
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

	// singleton dependencies

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// state

	Set <PrivStuff> privStuffs;

	// implementation

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

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Object",
			"Priv",
			"Can",
			"Grant",
			"Groups");

		for (
			PrivStuff privStuff
				: privStuffs
		) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				privStuff.path);

			htmlTableCellWrite (
				privStuff.privCode);

			htmlTableCellWrite (
				booleanToYesNo (
					privStuff.userPriv != null
					&& privStuff.userPriv.getCan ()));

			htmlTableCellWrite (
				booleanToYesNo (
					privStuff.userPriv != null
					&& privStuff.userPriv.getCanGrant ()));

			htmlTableCellWrite (
				joinWithCommaAndSpace (
					privStuff.groups));

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

	class PrivStuff
		implements Comparable <PrivStuff> {

		String path;
		String privCode;
		UserPrivRec userPriv;

		List <String> groups =
			new ArrayList<> ();

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
