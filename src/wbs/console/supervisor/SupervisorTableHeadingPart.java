package wbs.console.supervisor;

import java.text.SimpleDateFormat;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.console.misc.TimeFormatter;
import wbs.console.part.AbstractPagePart;
import wbs.console.reporting.StatsPeriod;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorTableHeadingPart")
public
class SupervisorTableHeadingPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	TimeFormatter timeFormatter;

	// properties

	@Getter @Setter
	SupervisorTableHeadingSpec supervisorTableHeadingSpec;

	// state

	StatsPeriod statsPeriod;

	// implementation

	@Override
	public
	void prepare () {

		statsPeriod =
			(StatsPeriod)
			parameters.get ("statsPeriod");

	}

	@Override
	public
	void renderHtmlBodyContent () {

		// main heading

		if (supervisorTableHeadingSpec.label () != null) {

			printFormat (
				"<tr>\n",

				"<th colspan=\"%h\">%h</th>\n",
				statsPeriod.size () + 2,
				supervisorTableHeadingSpec.label (),

				"</tr>\n");

		}

		// hours

		printFormat (
			"<tr>\n",
			"<th>%h</th>\n",
			supervisorTableHeadingSpec.groupLabel ());

		for (
			Instant step
				: statsPeriod.steps ()
		) {

			printFormat (
				"<th>%h</th>\n",
				String.format (
					"%02d",
					step.toDateTime (
							timeFormatter.defaultTimezone ())
						.getHourOfDay ()));

		}

		printFormat (
			"<th>Total</th>\n");

		printFormat (
			"</tr>\n");

	}

	static
	SimpleDateFormat hourFormat =
		new SimpleDateFormat ("HH");

}
