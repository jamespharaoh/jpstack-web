package wbs.console.supervisor;

import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;

import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.part.AbstractPagePart;
import wbs.console.part.PagePart;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorTablePart")
public
class SupervisorTablePart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	SupervisorTablePartBuilder supervisorTablePartBuilder;

	List <PagePart> pageParts =
		Collections.emptyList ();

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"prepare");

		// prepare page parts

		ImmutableList.Builder <PagePart> pagePartsBuilder =
			ImmutableList.<PagePart> builder ();

		for (
			Provider <PagePart> pagePartFactory
				: supervisorTablePartBuilder.pagePartFactories ()
		) {

			PagePart pagePart =
				pagePartFactory.get ();

			pagePart.setup (
				parameters);

			pagePart.prepare (
				taskLogger);

			pagePartsBuilder.add (
				pagePart);

		}

		pageParts =
			pagePartsBuilder.build ();

	}

	@Override
	public
	void renderHtmlHeadContent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlHeadContent");

		for (
			PagePart pagePart
				: pageParts
		) {

			pagePart.renderHtmlHeadContent (
				taskLogger);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

		htmlTableOpenList ();

		for (
			PagePart pagePart
				: pageParts
		) {

			pagePart.renderHtmlBodyContent (
				taskLogger);

		}

		htmlTableClose ();

	}

}
