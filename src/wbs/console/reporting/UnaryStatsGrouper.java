package wbs.console.reporting;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("unaryStatsGrouper")
public
class UnaryStatsGrouper
	implements StatsGrouper {

	@Getter @Setter
	String label;

	@Override
	public
	Set<Object> getGroups (
			StatsDataSet dataSet) {

		return Collections.singleton (
			StatsDatum.UNARY);

	}

	@Override
	public
	String tdForGroup (
			Object group) {

		return stringFormat (
			"<td>%h</td>\n",
			label);

	}

	@Override
	public
	List<Object> sortGroups (
			Set<Object> groups) {

		if (groups.size () != 1)
			throw new IllegalArgumentException ();

		if (! groups.contains (StatsDatum.UNARY))
			throw new IllegalArgumentException ();

		return Collections.singletonList (
			StatsDatum.UNARY);

	}

}
