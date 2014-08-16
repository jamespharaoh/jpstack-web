package wbs.platform.core.console;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.ABSwap;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.HtmlResponder;
import wbs.platform.menu.console.MenuGroupConsoleHelper;
import wbs.platform.menu.model.MenuGroupRec;
import wbs.platform.menu.model.MenuRec;

@PrototypeComponent ("coreSidebarMenuResponder")
public
class CoreSidebarMenuResponder
	extends HtmlResponder {

	// dependencies

	@Inject
	MenuGroupConsoleHelper menuGroupHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	List<MenuGroupRec> menuGroups;

	// implementation

	@Override
	protected
	void prepare () {

		menuGroups =
			menuGroupHelper.findAll ();

		Collections.sort (
			menuGroups);

	}

	@Override
	protected
	void goBodyStuff () {

		printFormat (
			"<table class=\"menu\" width=\"100%%\">\n");

		ABSwap abSwap =
			new ABSwap ();

		for (MenuGroupRec menuGroup
				: menuGroups) {

			boolean doneGroup = false;

			for (MenuRec menu
					: menuGroup.getMenus ()) {

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
					"%s\n",
					Html.magicTr (
						requestContext.resolveApplicationUrl (
							menu.getPath ()),
						false,
						menu.getTarget (),
						null,
						null,
						abSwap),

					"<td>%h</td> ",
						menu.getLabel (),

					"</tr>\n");

			}

		}

		printFormat (
			"</table>\n");

	}

}
