package wbs.platform.core.console;

import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.web.utils.HtmlAttributeUtils.htmlAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.MagicTableScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.TaskLogger;
import wbs.platform.menu.console.MenuGroupConsoleHelper;
import wbs.platform.menu.model.MenuGroupRec;
import wbs.platform.menu.model.MenuItemRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.web.utils.ABSwap;

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
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

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
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

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
	void renderHtmlBodyContents (
			@NonNull TaskLogger parentTaskLogger) {

		htmlTableOpen (
			htmlClassAttribute (
				"menu"),
			htmlAttribute (
				"width",
				"100%"));

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

					htmlTableRowOpen ();

					htmlTableHeaderCellWrite (
						menuGroup.getLabel ());

					htmlTableRowClose ();

					doneGroup = true;

				}

				htmlTableRowOpen (

					htmlClassAttribute (
						"magic-table-row",
						abSwap.swap ()),

					htmlDataAttribute (
						"target-href",
						requestContext.resolveApplicationUrl (
							menu.getTargetPath ())),

					htmlDataAttribute (
						"target-frame",
						menu.getTargetFrame ())

				);

				htmlTableCellWrite (
					menu.getLabel ());

				htmlTableRowClose ();

			}

		}

		htmlTableClose ();

	}

}
