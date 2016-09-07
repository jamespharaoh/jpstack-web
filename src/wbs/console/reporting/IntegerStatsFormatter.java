package wbs.console.reporting;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.mapEntry;
import static wbs.framework.utils.etc.NumberUtils.equalToZero;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.utils.StringSubstituter;
import wbs.framework.utils.TimeFormatter;
import wbs.framework.utils.etc.Html;
import wbs.framework.web.UrlParams;

@Accessors (fluent = true)
@PrototypeComponent ("intStatsFormatter")
public
class IntegerStatsFormatter
	implements StatsFormatter {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
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
			@NonNull Optional <Object> objectValueOptional) {

		Instant instant =
			period.step (
				step);

		Long value =
			(Long)
			objectValueOptional.or (
				0l);

		// empty cell for missing or zero value

		if (
			equalToZero (
				value)
		) {

			return "<td></td>\n";

		}

		// simple cell if no link

		if (
			isNull (
				targetBase)
		) {

			return stringFormat (
				"<td style=\"text-align: right\">%h</td>\n",
				value);

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
				urlParams.toUrl (
					targetBase),
				null,
				1,
				" text-align: right;",
				""),

			value);

	}

	@Override
	public
	String formatTotal (
			@NonNull Object group,
			@NonNull Optional <Object> objectValueOptional) {

		Long value =
			(Long)
			objectValueOptional.or (
				0l);

		if (
			equalToZero (
				value)
		) {
			return "<td></td>";
		}

		return stringFormat (
			"<td",
			" style=\"text-align: right\"",
			">%h</td>\n",
			value);

	}

}
