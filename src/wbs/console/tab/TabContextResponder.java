package wbs.console.tab;

import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

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
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.responder.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("tabContextResponder")
@DataClass ("tab-context-responder")
public
class TabContextResponder
	implements
		Provider <Responder>,
		Responder {

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
	Provider <TabbedResponder> tabbedPageProvider;

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
			String pagePartName) {

		PagePartFactory factory =
			parentTransaction -> {

			try (

				NestedTransaction transaction =
					parentTransaction.nestTransaction (
						logContext,
						"buildPagePart");

			) {

				Object bean =
					componentManager.getComponentRequired (
						transaction,
						pagePartName,
						Object.class);

				if (bean instanceof PagePart) {
					return (PagePart) bean;
				}

				if (bean instanceof Provider) {

					Provider<?> provider =
						(Provider<?>) bean;

					return (PagePart)
						provider.get ();

				}

				throw new ClassCastException (
					stringFormat (
						"Cannot cast %s to PagePart or Provider<PagePart>",
						bean.getClass ().getName ()));

			}

		};

		return pagePartFactory (
			factory);

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

			Responder responder =
				makeResponder (
					taskLogger);

			responder.execute (
				taskLogger);

		}

	}

	@Override
	public
	Responder get () {

		return this;

	}

	// private implementation

	private
	Responder makeResponder (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnlyWithoutParameters (
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

			return tabbedPageProvider.get ()

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
