package wbs.console.reporting;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.LocalDate;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.framework.web.UrlParams;

@Accessors (fluent = true)
@PrototypeComponent ("intStatsFormatter")
public
class IntegerStatsFormatter
	implements StatsFormatter {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	String targetBase;

	@Getter @Setter
	String targetGroupParamName;

	@Getter @Setter
	String targetStepParamName;

	@Getter @Setter
	Map<String,String> targetParams =
		new LinkedHashMap<String,String> ();

	// implementation

	public
	IntegerStatsFormatter addTargetParam (
			String name,
			String value) {

		targetParams.put (
			name,
			value);

		return this;

	}

	@Override
	public
	String format (
			Object group,
			String step,
			Object value) {

		Integer intValue =
			(Integer) value;

		if (intValue == null || intValue == 0)
			return "<td></td>";

		if (targetBase == null) {

			return stringFormat (
				"<td style=\"text-align: right\">%h</td>\n",
				intValue);

		}

		UrlParams urlParams =
			new UrlParams ();

		if (targetParams != null) {

			for (Map.Entry<String,String> entry
					: targetParams.entrySet ()) {

				// TODO this is so wrong

				urlParams.set (
					entry.getKey (),
					entry.getValue ().replace (
						"{dateYmd}",
						requestContext.parameter (
							"date",
							LocalDate.now ().toString ())));

			}

		}

		if (targetGroupParamName != null)
			urlParams.set (
				targetGroupParamName,
				group.toString ());

		if (targetStepParamName != null)
			urlParams.set (
				targetStepParamName,
				step);

		return stringFormat (
			"%s%h</td>\n",

			Html.magicTd (
				urlParams.toUrl (targetBase),
				null,
				1,
				" text-align: right;",
				""),

			intValue);

	}

	@Override
	public
	String formatTotal (
			Object group,
			Object value) {

		Integer intValue =
			(Integer) value;

		if (intValue == null || intValue == 0)
			return "<td></td>";

		return stringFormat (
			"<td style=\"text-align: right\">%h</td>\n",
			intValue);

	}

}
