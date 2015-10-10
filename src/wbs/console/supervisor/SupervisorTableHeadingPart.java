package wbs.console.supervisor;

import java.text.SimpleDateFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.DateTime;

import wbs.console.part.AbstractPagePart;
import wbs.console.reporting.StatsPeriod;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorTableHeadingPart")
public
class SupervisorTableHeadingPart
	extends AbstractPagePart {

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

		DateTime hourStart =
			statsPeriod
				.startTime ()
				.toDateTime ();

		for (
			int hour = 0;
			hour < statsPeriod.size ();
			hour ++
		) {

			printFormat (
				"<th>%h</th>\n",
				hourFormat.format (hourStart.toDate ()));

			hourStart =
				hourStart.plusHours (1);

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
