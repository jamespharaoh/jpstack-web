package wbs.console.tab;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextStuff;
import wbs.console.module.ConsoleManager;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@DataClass ("context-tab")
@PrototypeComponent ("consoleContextTab")
public
class ConsoleContextTab {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@WeakSingletonDependency
	ConsoleManager consoleManager;

	// attributes

	@DataAttribute
	@Getter @Setter
	String name;

	@DataAttribute
	@Getter @Setter
	String defaultLabel;

	@DataAttribute
	@Getter @Setter
	String localFile;

	@DataChildren
	List <String> privKeys =
		new ArrayList<> ();

	// property helpers

	public
	List <String> privKeys () {

		return privKeys;

	}

	public
	ConsoleContextTab privKeys (
			@NonNull String ... privKeys) {

		for (
			String privKey
				: privKeys
		) {

			if (privKey == null)
				continue;

			this.privKeys.add (
				privKey);

		}

		return this;

	}

	public
	ConsoleContextTab privKeys (
			List <String> privKeys) {

		this.privKeys =
			privKeys;

		return this;

	}

	// implementation

	public
	Tab realTab (
			ConsoleContextStuff contextStuff,
			ConsoleContext consoleContext) {

		return new RealTab (
			contextStuff,
			consoleContext);

	}

	private
	class RealTab
		extends Tab {

		final
		ConsoleContextStuff contextStuff;

		final
		ConsoleContext consoleContext;

		private
		RealTab (
				ConsoleContextStuff newContextStuff,
				ConsoleContext newContext) {

			super (defaultLabel);

			contextStuff = newContextStuff;
			consoleContext = newContext;

		}

		@Override
		public
		String getUrl (
				@NonNull TaskLogger parentTaskLogger) {

			try (

				TaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"RealTab.getUrl");

			) {

				return consoleManager.resolveLocalFile (
					taskLogger,
					contextStuff,
					consoleContext,
					localFile);

			}

		}

		@Override
		public
		boolean isAvailable () {

			if (privKeys.isEmpty ())
				return true;

			for (String privKey
					: privKeys) {

				if (contextStuff.can (privKey))
					return true;

			}

			return false;

		}

	}

}
