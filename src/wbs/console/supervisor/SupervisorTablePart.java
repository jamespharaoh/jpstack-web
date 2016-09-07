package wbs.console.supervisor;

import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.collect.ImmutableList;

import wbs.console.part.AbstractPagePart;
import wbs.console.part.PagePart;
import wbs.framework.component.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorTablePart")
public
class SupervisorTablePart
	extends AbstractPagePart {

	@Getter @Setter
	SupervisorTablePartBuilder supervisorTablePartBuilder;

	List<PagePart> pageParts =
		Collections.emptyList ();

	@Override
	public
	void prepare () {

		// prepare page parts

		ImmutableList.Builder<PagePart> pagePartsBuilder =
			ImmutableList.<PagePart>builder ();

		for (Provider<PagePart> pagePartFactory
				: supervisorTablePartBuilder.pagePartFactories ()) {

			PagePart pagePart =
				pagePartFactory.get ();

			pagePart.setup (
				parameters);

			pagePart.prepare ();

			pagePartsBuilder.add (
				pagePart);

		}

		pageParts =
			pagePartsBuilder.build ();

	}

	@Override
	public
	void renderHtmlHeadContent () {

		for (PagePart pagePart
				: pageParts) {

			pagePart.renderHtmlHeadContent ();

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"list\">\n");

		for (PagePart pagePart
				: pageParts) {

			pagePart.renderHtmlBodyContent ();

		}

		printFormat (
			"</table>\n");

	}

}
