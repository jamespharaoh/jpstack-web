package wbs.console.reporting;

import com.google.common.base.Optional;

import wbs.utils.string.FormatWriter;

public
interface StatsFormatter {

	void format (
			FormatWriter formatWriter,
			Object group,
			StatsPeriod period,
			Integer step,
			Optional <Object> value);

	void formatTotal (
			FormatWriter formatWriter,
			Object group,
			Optional <Object> value);

}
