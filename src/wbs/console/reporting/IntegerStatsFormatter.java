package wbs.console.reporting;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.isZero;
import static wbs.framework.utils.etc.Misc.mapEntry;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import com.google.common.base.Optional;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.StringSubstituter;
import wbs.framework.utils.TimeFormatter;
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

	@Inject
	TimeFormatter timeFormatter;

	// properties

	@Getter @Setter
	String targetBase;

	@Getter @Setter
	Map<String,String> targetParams =
		new LinkedHashMap<String,String> ();

	// implementation

	public
	IntegerStatsFormatter addTargetParam (
			@NonNull String name,
			@NonNull String value) {

		targetParams.put (
			name,
			value);

		return this;

	}

	@Override
	public
	String format (
			@NonNull Object group,
			@NonNull StatsPeriod period,
			@NonNull Integer step,
			@NonNull Optional<Object> value) {

		Instant instant =
			period.step (
				step);

		Integer intValue =
			(Integer)
			value.or (0);

		// empty cell for missing or zero value

		if (intValue == 0) {
			return "<td></td>\n";
		}

		// simple cell if no link

		if (
			isNull (
				targetBase)
		) {

			return stringFormat (
				"<td style=\"text-align: right\">%h</td>\n",
				intValue);

		}

		// work out link etc

		UrlParams urlParams =
			new UrlParams ();

		if (
			isNotNull (
				targetParams)
		) {

			StringSubstituter substituter =
				new StringSubstituter ()

				.param (
					"group",
					group.toString ())

				.param (
					"interval",
					timeFormatter.timestampHourStringIso (
						instant));

			targetParams.entrySet ().stream ()

				.map (paramEntry ->
					mapEntry (
						paramEntry.getKey (),
						substituter.substitute (
							paramEntry.getValue ())))

				.forEach (paramEntry ->
					urlParams.add (
						paramEntry.getKey (),
						paramEntry.getValue ()));

		}

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
			@NonNull Object group,
			@NonNull Optional<Object> value) {

		Integer integerValue =
			(Integer)
			value.or (0);

		if (
			isZero (
				integerValue)
		) {
			return "<td></td>";
		}

		return stringFormat (
			"<td",
			" style=\"text-align: right\"",
			">%h</td>\n",
			integerValue);

	}

}
