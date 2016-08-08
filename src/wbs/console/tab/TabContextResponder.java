package wbs.console.tab;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.part.PagePart;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.web.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("tabContextResponder")
@DataClass ("tab-context-responder")
public
class TabContextResponder
	implements
		Provider<Responder>,
		Responder {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	ApplicationContext applicationContext;

	@Inject
	Provider<TabbedResponder> tabbedPage;

	// properties

	@DataAttribute
	@Getter @Setter
	Object tab;

	@DataAttribute
	@Getter @Setter
	String title;

	@DataAttribute
	@Getter @Setter
	Provider<PagePart> pagePartFactory;

	// utility methods

	public
	TabContextResponder pagePartName (
			final String pagePartName) {

		Provider<PagePart> factory =
			new Provider<PagePart> () {

			@Override
			public
			PagePart get () {

				Object bean =
					applicationContext.getBean (
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

		return pagePartFactory (factory);

	}

	public
	void setPagePart (
			final PagePart pagePart) {

		pagePartFactory =
			new Provider<PagePart> () {

			@Override
			public
			PagePart get () {
				return pagePart;
			}

		};

	}

	@Override
	public
	void execute ()
		throws IOException {

		if (pagePartFactory == null) {

			throw new NullPointerException (
				"pagePartFactory");

		}

		tabbedPage.get ()

			.tab (
				requestContext.contextStuff ().getTab (
					requestContext.consoleContext (),
					tab))

			.title (
				title)

			.pagePart (
				pagePartFactory.get ())

			.execute ();

	}

	@Override
	public
	Responder get () {

		return this;

	}

}
