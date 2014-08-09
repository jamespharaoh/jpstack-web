package wbs.platform.console.tab;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.web.Responder;
import wbs.platform.console.part.PagePart;
import wbs.platform.console.request.ConsoleRequestContext;

@Accessors (fluent = true)
@PrototypeComponent ("tabContextResponder")
public
class TabContextResponder
	implements
		Provider<Responder>,
		Responder {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	ApplicationContext applicationContext;

	@Inject
	Provider<TabbedResponder> tabbedPage;

	@Getter @Setter
	Object tab;

	@Getter @Setter
	String title;

	@Getter @Setter
	Provider<PagePart> pagePartFactory;

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
				public PagePart get () {
					return pagePart;
				}
			};

	}

	@Override
	public
	void execute ()
		throws IOException {

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
