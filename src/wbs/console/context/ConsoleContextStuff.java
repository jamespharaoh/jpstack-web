package wbs.console.context;

import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleManager;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.Tab;
import wbs.console.tab.TabList;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.AbstractStringSubstituter;

/**
 * Holds local variables and path info during a request.
 */
@Accessors (fluent = true)
@PrototypeComponent ("consoleContextStuff")
public
class ConsoleContextStuff {

	// singleton dependencies

	@SingletonDependency
	ConsoleManager consoleManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <TabList> tabListProvider;

	// properties

	@Getter @Setter
	String foreignPath;

	@Getter @Setter
	ConsoleContext consoleContext;

	@Getter @Setter
	ConsoleContextStuff parentContextStuff;

	@Getter @Setter
	ConsoleContextTab embeddedParentContextTab;

	// state

	Map <ConsoleContext, Map <String, Tab>> tabsByNameByContext =
		new HashMap <> ();

	Map <String, Object> attributes =
		new HashMap<>();

	Set <String> privs =
		new HashSet<> ();

	// property accessors

	public
	Map <String, Object> attributes () {

		return ImmutableMap.copyOf (
			attributes);

	}

	public
	Set <String> privs () {

		return ImmutableSet.copyOf (
			privs);

	}

	// implementation

	public
	TabList makeContextTabs (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleContext consoleContext) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeContextTabs");

		) {

			Map <String, Tab> tabsByName =
				new HashMap<> ();

			TabList tabList =
				tabListProvider.provide (
					taskLogger);

			tabsByNameByContext.put (
				consoleContext,
				tabsByName);

			for (ConsoleContextTab contextTab
					: consoleContext.contextTabs ().values ()) {

				Tab realTab =
					contextTab.realTab (
						this,
						consoleContext);

				tabsByName.put (
					contextTab.name (),
					realTab);

				tabList.add (realTab);

			}

			return tabList;

		}

	}

	public
	Tab getTab (
			@NonNull ConsoleContext consoleContext,
			@NonNull Object object) {

		if (object instanceof Tab)
			return (Tab) object;

		if (object instanceof String) {

			String tabName =
				(String) object;

			Map<String,Tab> tabsByName =
				tabsByNameByContext.get (
					consoleContext);

			if (tabsByName == null) {

				tabsByName =
					new HashMap<String,Tab> ();

				tabsByNameByContext.put (
					consoleContext,
					tabsByName);

			}

			Tab tab =
				tabsByName.get (tabName);

			if (tab == null) {

				ConsoleContextTab contextTab =
					consoleManager.tab (
						tabName,
						true);

				if (contextTab == null) {

					throw new RuntimeException (
						stringFormat (
							"Context tab %s not found for context %s",
							tabName,
							consoleContext.name ()));

				}

				tab =
					contextTab.realTab (
						this,
						consoleContext);

				tabsByNameByContext
					.get (consoleContext)
					.put (tabName, tab);

			}

			return tab;

		}

		throw new RuntimeException (
			stringFormat (
				"Not a tab: %s",
				classNameSimple (
					object.getClass ())));

	}

	public
	void reset () {

		tabsByNameByContext =
			new HashMap <> ();

	}

	public
	void set (
			String key,
			Object value) {

		attributes.put (
			key,
			value);

	}

	public
	Object get (
			String key) {

		return attributes.get (
			key);

	}

	public
	void grant (
			String... keys) {

		for (String key : keys)
			privs.add (key);

	}

	public
	boolean can (
			String... keys) {

		for (String key : keys) {

			if (privs.contains (key))
				return true;

		}

		return false;

	}

	public
	String substitutePlaceholders (
			String url) {

		AbstractStringSubstituter substituter =
			new AbstractStringSubstituter () {

			@Override
			public
			String getSubstitute (
					String name) {

				Object object = get (name);

				if (object == null) {

					throw new RuntimeException (
						stringFormat (
							"Requested param not found in context stuff: %s",
							name));

				}

				return object.toString ();

			}

		};

		return substituter.substitute (url);

	}

}
