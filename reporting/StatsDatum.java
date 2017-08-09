package wbs.console.reporting;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

@Accessors (fluent = true)
@Data
public
class StatsDatum {

	Instant startTime;

	Map<String,Object> indexes =
		new LinkedHashMap<String,Object> ();

	Map<String,Object> values =
		new HashMap<String,Object> ();

	public
	StatsDatum addIndex (
			String key,
			Object value) {

		indexes.put (
			key,
			value);

		return this;

	}

	public
	StatsDatum addValue (
			String key,
			Object value) {

		values.put (
			key,
			value);

		return this;

	}

	public final static
	Object UNARY =
		new Object () {

		@Override
		public
		String toString () {

			return "StatsDatum.UNARY";

		}

	};

}
