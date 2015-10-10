package wbs.console.misc;

import java.util.ArrayList;
import java.util.List;

public
class Percentager {

	List<Integer> values =
		new ArrayList<Integer> ();

	int total = 0;

	public
	void add (
			int value) {

		values.add (
			value);

		total +=
			value;

	}

	public
	List<Integer> work () {

		List<Integer> ret =
			new ArrayList<Integer> ();

		int pcLeft = 100, totalLeft = total;

		for (Integer value : values) {

			int pc = totalLeft > 0
				? value * pcLeft / totalLeft
				: 0;

			ret.add (pc);

			pcLeft -= pc;
			totalLeft -= value;

		}

		assert totalLeft == 0 && pcLeft == 0 : "Percentager goofed";

		return ret;

	}

}
