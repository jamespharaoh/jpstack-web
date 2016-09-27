package wbs.console.reporting;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.mapEntry;
import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.string.StringUtils.emptyStringIfNull;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.web.UrlParams;
import wbs.utils.string.FormatWriter;
import wbs.utils.string.StringSubstituter;
import wbs.utils.time.TimeFormatter;
import wbs.utils.web.HtmlTableCellWriter;

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
	Map <String, String> targetParams =
		new LinkedHashMap<> ();

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
	void format (
			@NonNull FormatWriter formatWriter,
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

			formatWriter.writeFormat (
				"<td></td>\n");

			return;

		}

		// simple cell if no link

		if (
			isNull (
				targetBase)
		) {

			formatWriter.writeFormat (
				"<td style=\"text-align: right\">%h</td>\n",
				value);

			return;

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

				.map (
					paramEntry ->
						mapEntry (
							paramEntry.getKey (),
							substituter.substitute (
								paramEntry.getValue ())))

				.forEach (
					paramEntry ->
						urlParams.add (
							paramEntry.getKey (),
							paramEntry.getValue ()));

		}

		new HtmlTableCellWriter ()

			.href (
				urlParams.toUrl (
					emptyStringIfNull (
						targetBase)))

			.style (
				"text-align: right")

			.write (
				formatWriter);

		formatWriter.writeFormat (
			"%h</td>\n",
			value);

	}

	@Override
	public
	void formatTotal (
			@NonNull FormatWriter formatWriter,
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

			formatWriter.writeFormat (
				"<td></td>\n");

		} else {

			formatWriter.writeFormat (
				"<td",
				" style=\"text-align: right\"",
				">%h</td>\n",
				value);

		}

	}

}
