package wbs.console.tab;

import static wbs.utils.etc.NullUtils.isNull;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.part.PagePart;
import wbs.console.part.PagePartFactory;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.responder.WebResponder;

@Accessors (fluent = true)
@PrototypeComponent ("tabContextResponder")
@DataClass ("tab-context-responder")
public
class TabContextResponder
	implements WebResponder {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <TabbedResponder> tabbedPageProvider;

	// properties

	@DataAttribute
	@Getter @Setter
	Object tab;

	@DataAttribute
	@Getter @Setter
	String title;

	@DataAttribute
	@Getter @Setter
	PagePartFactory pagePartFactory;

	// utility methods

	public
	TabContextResponder pagePartName (
			@NonNull String pagePartName) {

		return pagePartFactory (
			parentTransaction -> {

			try (

				NestedTransaction transaction =
					parentTransaction.nestTransaction (
						logContext,
						"buildPagePart");

			) {

				return componentManager.getComponentRequired (
					transaction,
					pagePartName,
					PagePart.class);

			}

		});

	}

	public
	void setPagePart (
			PagePart pagePart) {

		pagePartFactory =
			nextedTaskLogger ->
				pagePart;

	}

	@Override
	public
	void execute (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"execute");

		) {

			WebResponder responder =
				makeResponder (
					taskLogger);

			responder.execute (
				taskLogger);

		}

	}

	// private implementation

	private
	WebResponder makeResponder (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"execute");

		) {

			if (
				isNull (
					pagePartFactory)
			) {

				throw new NullPointerException (
					"pagePartFactory");

			}

			return tabbedPageProvider.provide (
				transaction)

				.tab (
					requestContext.consoleContextStuffRequired ().getTab (
						requestContext.consoleContextRequired (),
						tab))

				.title (
					title)

				.pagePart (
					pagePartFactory.buildPagePart (
						transaction))

			;

		}

	}

}
