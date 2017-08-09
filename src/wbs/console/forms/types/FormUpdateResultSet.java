package wbs.console.forms.types;

import static wbs.utils.etc.NumberUtils.moreThanZero;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

@Accessors (fluent = true)
@Data
public
class FormUpdateResultSet {

	Map <Pair <String, String>, FormUpdateResult <?, ?>> updateResults =
		new LinkedHashMap<> ();

	long errorCount;
	long updateCount;

	public
	boolean errors () {

		return moreThanZero (
			errorCount);

	}

	public
	boolean updates () {

		return moreThanZero (
			updateCount);

	}

}
