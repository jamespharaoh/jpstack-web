package wbs.platform.supervisor;

import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.console.part.PagePart;

import com.google.common.collect.ImmutableList;

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
	void goHeadStuff () {

		for (PagePart pagePart
				: pageParts) {

			pagePart.goHeadStuff ();

		}

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"list\">\n");

		for (PagePart pagePart
				: pageParts) {

			pagePart.goBodyStuff ();

		}

		printFormat (
			"</table>\n");

	}

}
