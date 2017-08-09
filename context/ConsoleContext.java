package wbs.console.context;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleManager;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.Tab;
import wbs.console.tab.TabList;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.file.WebFile;

@Accessors (fluent = true)
public abstract
class ConsoleContext
	implements Comparable <ConsoleContext> {

	// singleton dependencies

	@WeakSingletonDependency
	ConsoleManager consoleManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// properties

	public abstract
	String name ();

	public abstract
	String typeName ();

	public abstract
	String pathPrefix ();

	public abstract
	Boolean global ();

	public abstract
	String parentContextName ();

	public abstract
	String parentContextTabName ();

	public abstract
	Map<String,ConsoleContextTab> contextTabs ();

	public abstract
	Map <String, WebFile> files ();

	public abstract
	ConsoleContext files (
		Map <String, WebFile> files);

	public abstract
	ConsoleContext contextTabs (
		Map <String, ConsoleContextTab> tabs);

	@Getter @Setter
	ConsoleContextType contextType;

	// implementation

	public
	abstract String titleForStuff (
			ConsoleContextStuff stuff);

	public abstract
	void initContext (
			Transaction parentTransaction,
			UserPrivChecker privChecker,
			PathSupply pathParts,
			ConsoleContextStuff contextStuff);

	public
	String localPathForStuff (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleContextStuff contextStuff) {

		return "";

	}

	public
	void initTabContext (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleContextStuff contextStuff) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initTabContext");

		) {

			TabList tabList =
				contextStuff.makeContextTabs (
					taskLogger,
					this);

			ConsoleContext embeddedParentContext =
				contextStuff.parentContextStuff () != null
					? contextStuff.parentContextStuff ().consoleContext ()
					: null;

			ConsoleContextTab embeddedParentContextTab =
				contextStuff.embeddedParentContextTab ();

			ConsoleContext parentContext =
				parentContext ();

			if (parentContext != null) {

				parentContext.initTabContext (
					taskLogger,
					contextStuff);

				requestContext.addTabContext (
					contextStuff.getTab (
						parentContext,
						parentContextTab (
							contextStuff
						).name ()),
						titleForStuff (contextStuff),
						tabList);

			} else {

				if (embeddedParentContext != null) {

					Tab tab =
						contextStuff.parentContextStuff ().getTab (
							embeddedParentContext,
							embeddedParentContextTab.name ());

					if (tab == null) {

						tab =
							embeddedParentContextTab.realTab (
								contextStuff,
								embeddedParentContext);

					}

					requestContext.addTabContext (
						tab,
						titleForStuff (contextStuff),
						tabList);

				} else {

					requestContext.tabContext (
						titleForStuff (contextStuff),
						tabList);

				}

			}

		}

	}

	public
	ConsoleContext parentContext () {

		if (parentContextName () == null)
			return null;

		ConsoleContext parentContext =
			consoleManager.context (
				parentContextName (),
				true);

		return parentContext;

	}

	public
	ConsoleContextTab parentContextTab (
			ConsoleContextStuff stuff) {

		return consoleManager.tab (
			parentContextTabName (),
			true);

	}

	@Override
	public
	int hashCode () {

		return name ()
			.hashCode ();

	}

	public static
	class PathSupply {

		List<String> source;

		int used = 0;

		public
		PathSupply (
				List<String> newSource) {
			source = newSource;
		}

		public
		int size () {
			return source.size () - used;
		}

		public
		String next () {

			if (used == source.size ())
				throw new NoSuchElementException ();

			return source.get (
				used ++);

		}

		public
		String used () {

			StringBuilder stringBuilder =
				new StringBuilder ();

			for (
				int index = 0;
				index < used;
				index ++
			) {

				stringBuilder.append (
					'/');

				stringBuilder.append (
					source.get (index));

			}

			return stringBuilder.toString ();

		}

	}

	// compare to

	@Override
	public
	int compareTo (
			ConsoleContext other) {

		return name ().compareTo (
			other.name ());

	}

}
