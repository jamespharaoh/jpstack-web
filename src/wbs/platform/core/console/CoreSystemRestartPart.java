package wbs.platform.core.console;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.IterableUtils.iterableFilterToList;
import static wbs.utils.etc.EnumUtils.enumName;
import static wbs.utils.etc.EnumUtils.enumNameHyphens;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.earlierThan;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPost;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowSeparatorWrite;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.console.html.HtmlLink;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.deployment.console.ApiDeploymentConsoleHelper;
import wbs.platform.deployment.console.ConsoleDeploymentConsoleHelper;
import wbs.platform.deployment.console.DaemonDeploymentConsoleHelper;
import wbs.platform.deployment.model.ApiDeploymentRec;
import wbs.platform.deployment.model.ConsoleDeploymentRec;
import wbs.platform.deployment.model.DaemonDeploymentRec;
import wbs.platform.deployment.model.DeploymentState;

@PrototypeComponent ("coreSystemRestartPart")
public
class CoreSystemRestartPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ApiDeploymentConsoleHelper apiDeploymentHelper;

	@SingletonDependency
	ConsoleDeploymentConsoleHelper consoleDeploymentHelper;

	@SingletonDependency
	DaemonDeploymentConsoleHelper daemonDeploymentHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker userPrivChecker;

	// state

	List <ApiDeploymentRec> apiDeployments;
	List <ConsoleDeploymentRec> consoleDeployments;
	List <DaemonDeploymentRec> daemonDeployments;

	// details

	@Override
	public
	Set <HtmlLink> links () {

		return ImmutableSet.<HtmlLink> of (

			HtmlLink.applicationCssStyle (
				"/style/core-system-restart.css")

		);

	}

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"prepare");

		) {

			apiDeployments =
				iterableFilterToList (
					apiDeployment ->
						userPrivChecker.canRecursive (
							taskLogger,
							apiDeployment,
							"restart"),
					apiDeploymentHelper.findAllNotDeletedEntities ());

			Collections.sort (
				apiDeployments);

			consoleDeployments =
				iterableFilterToList (
					consoleDeployment ->
						userPrivChecker.canRecursive (
							taskLogger,
							consoleDeployment,
							"restart"),
					consoleDeploymentHelper.findAllNotDeletedEntities ());

			Collections.sort (
				consoleDeployments);

			daemonDeployments =
				iterableFilterToList (
					daemonDeployment ->
						userPrivChecker.canRecursive (
							taskLogger,
							daemonDeployment,
							"restart"),
					daemonDeploymentHelper.findAllNotDeletedEntities ());

			Collections.sort (
				daemonDeployments);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"renderHtmlBodyContent");

		) {

			htmlHeadingTwoWrite (
				"Restart server components");

			htmlParagraphWriteFormat (
				"Use these controls to restart server compoments. This may be ",
				"necessary from time to time if they encounter certain types of ",
				"problem.");

			htmlFormOpenPost ();

			htmlTableOpenDetails ();

			htmlTableHeaderRowWrite (
				"Name",
				"Description",
				"Status",
				"Restart");

			htmlTableRowSeparatorWrite ();

			renderApiDeployments (
				taskLogger);

			htmlTableRowSeparatorWrite ();

			renderDaemonDeployments (
				taskLogger);

			htmlTableRowSeparatorWrite ();

			renderConsoleDeployments (
				taskLogger);

			htmlTableClose ();

			htmlFormClose ();

		}

	}

	private
	void renderApiDeployments (
			@NonNull TaskLogger parentTaskLogger) {

		if (
			collectionIsEmpty (
				apiDeployments)
		) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				"(you do not have permission to restart any API deployments)",
				htmlColumnSpanAttribute (
					4l));

			htmlTableRowClose ();

		}

		for (
			ApiDeploymentRec apiDeployment
				: apiDeployments
		) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				apiDeployment.getName ());

			htmlTableCellWrite (
				apiDeployment.getDescription ());

			writeDeploymentState (
				optionalFromNullable (
					apiDeployment.getState ()),
				optionalFromNullable (
					apiDeployment.getStateTimestamp ()));

			htmlTableCellWriteHtml (
				() -> formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"api/%h\"",
					apiDeployment.getCode (),
					" value=\"restart\"",
					">"));

			htmlTableRowClose ();

		}

	}

	private
	void renderConsoleDeployments (
			@NonNull TaskLogger parentTaskLogger) {

		if (
			collectionIsEmpty (
				consoleDeployments)
		) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				stringFormat (
					"(you do not have permission to restart any console ",
					"deployments)"),
				htmlColumnSpanAttribute (
					4l));

			htmlTableRowClose ();

		}

		for (
			ConsoleDeploymentRec consoleDeployment
				: consoleDeployments
		) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				consoleDeployment.getName ());

			htmlTableCellWrite (
				consoleDeployment.getDescription ());

			writeDeploymentState (
				optionalFromNullable (
					consoleDeployment.getState ()),
				optionalFromNullable (
					consoleDeployment.getStateTimestamp ()));

			htmlTableCellWriteHtml (
				() -> formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"console/%h\"",
					consoleDeployment.getCode (),
					" value=\"restart\"",
					">"));

			htmlTableRowClose ();

		}

	}

	private
	void renderDaemonDeployments (
			@NonNull TaskLogger parentTaskLogger) {

		if (
			collectionIsEmpty (
				daemonDeployments)
		) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				stringFormat (
					"(you do not have permission to restart any daemon ",
					"deployments)"),
				htmlColumnSpanAttribute (
					4l));

			htmlTableRowClose ();

		}

		for (
			DaemonDeploymentRec daemonDeployment
				: daemonDeployments
		) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				daemonDeployment.getName ());

			htmlTableCellWrite (
				daemonDeployment.getDescription ());

			writeDeploymentState (
				optionalFromNullable (
					daemonDeployment.getState ()),
				optionalFromNullable (
					daemonDeployment.getStateTimestamp ()));

			htmlTableCellWriteHtml (
				() -> formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"daemon/%h\"",
					daemonDeployment.getCode (),
					" value=\"restart\"",
					">"));

			htmlTableRowClose ();

		}

	}

	private
	void writeDeploymentState (
			@NonNull Optional <DeploymentState> deploymentState,
			@NonNull Optional <Instant> deploymentStateTimestamp) {

		Pair <String, String> deploymentStatePair =
			deploymentStateClassAndLabel (
				deploymentState,
				deploymentStateTimestamp);

		htmlTableCellWrite (
			deploymentStatePair.getLeft (),
			htmlClassAttribute (
				deploymentStatePair.getRight ()));

	}

	private
	Pair <String, String> deploymentStateClassAndLabel (
			@NonNull Optional <DeploymentState> deploymentState,
			@NonNull Optional <Instant> deploymentStateTimestamp) {

		if (

			optionalIsNotPresent (
				deploymentState)

			|| optionalIsNotPresent (
				deploymentStateTimestamp)

			|| earlierThan (
				optionalGetRequired (
					deploymentStateTimestamp),
				Instant.now ().minus (
					Duration.standardSeconds (
						5l)))

		) {

			return Pair.of (
				"—",
				"core-system-restart-state-unknown");

		}

		return Pair.of (
			enumName (
				optionalGetRequired (
					deploymentState)),
			stringFormat (
				"core-system-restart-state-%s",
				enumNameHyphens (
					optionalGetRequired (
						deploymentState))));


	}

}
