package wbs.console.misc;

import java.util.ArrayList;
import java.util.List;

public
class Percentager {

	List<Long> values =
		new ArrayList<Long> ();

	long total = 0l;

	public
	void add (
			long value) {

		values.add (
			value);

		total +=
			value;

	}

	public
	List<Long> work () {

		List<Long> ret =
			new ArrayList<Long> ();

		long pcLeft = 100;
		long totalLeft = total;

		for (
			Long value
				: values
		) {

			long pc =
				totalLeft > 0
					? value * pcLeft / totalLeft
					: 0;

			ret.add (
				pc);

			pcLeft -= pc;

			totalLeft -= value;

		}

		assert totalLeft == 0 && pcLeft == 0 : "Percentager goofed";

		return ret;

	}

}
