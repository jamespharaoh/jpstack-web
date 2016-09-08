package wbs.platform.core.console;

import static wbs.framework.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.framework.utils.etc.StringUtils.joinWithSpace;
import static wbs.framework.utils.etc.StringUtils.stringNotEqualSafe;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.utils.etc.ABSwap;
import wbs.platform.menu.console.MenuGroupConsoleHelper;
import wbs.platform.menu.model.MenuGroupRec;
import wbs.platform.menu.model.MenuItemRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("coreSidebarMenuResponder")
public
class CoreSidebarMenuResponder
	extends HtmlResponder {

	// singleton dependencies

	@SingletonDependency
	MenuGroupConsoleHelper menuGroupHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// state

	List <MenuGroupRec> menuGroups;

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				MagicTableScriptRef.instance)

			.build ();

	}

	// implementation

	@Override
	protected
	void prepare () {

		UserRec currentUser =
			userConsoleLogic.userRequired ();

		SliceRec apnSlice =
			optionalOrNull (
				sliceHelper.findByCode (
					GlobalId.root,
					"apn"));

		if (

			apnSlice != null

			&& stringNotEqualSafe (
				currentUser.getUsername (),
				"stuart_test")

		) {

			menuGroups =
				menuGroupHelper.findByParent (
					sliceHelper.findByCodeRequired (
						GlobalId.root,
						"apn"));

		} else {

			menuGroups =
				menuGroupHelper.findByParent (
					currentUser.getSlice ());

		}

		Collections.sort (
			menuGroups);

	}

	@Override
	protected
	void renderHtmlBodyContents () {

		printFormat (
			"<table",
			" class=\"menu\"",
			" width=\"100%%\"",
			">\n");

		ABSwap abSwap =
			new ABSwap ();

		for (
			MenuGroupRec menuGroup
				: menuGroups
		) {

			boolean doneGroup = false;

			for (
				MenuItemRec menu
					: menuGroup.getMenus ()
			) {

				if (menu.getDeleted ())
					continue;

				if (! objectManager.canView (menu))
					continue;

				if (! doneGroup) {

					printFormat (
						"<tr>\n",
						"<th>%h</th>\n",
						menuGroup.getLabel (),

						"</tr>\n");

					doneGroup = true;

				}

				printFormat (
					"<tr",
					" class=\"%h\"",
					joinWithSpace (
						"magic-table-row",
						abSwap.swap ()),
					" data-target-href=\"%h\"",
					requestContext.resolveApplicationUrl (
						menu.getTargetPath ()),
					" data-target-frame=\"%h\"",
					menu.getTargetFrame (),
					">\n");

				printFormat (
					"<td>%h</td>\n",
						menu.getLabel ());

				printFormat (
					"</tr>\n");

			}

		}

		printFormat (
			"</table>\n");

	}

}
