package wbs.console.helper;

import static wbs.framework.utils.etc.Misc.camelToSpaces;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.framework.utils.etc.Html;

@Accessors (fluent = true)
public
class EnumConsoleHelper<E extends Enum<E>> {

	Map<E,String> map =
		new HashMap<E,String> ();

	Map<String,String> optionsMap =
		new LinkedHashMap<String,String> ();

	Map<String,String> nullOptionsMap =
		new LinkedHashMap<String,String> ();

	@Getter @Setter
	Class<E> enumClass;

	{

		optionsMap.put ("", "");

		nullOptionsMap.put ("", "");
		nullOptionsMap.put ("null", "none");

	}

	public
	void add (
			E value,
			String label) {

		map.put (value, label);
		optionsMap.put (value.name (), label);
		nullOptionsMap.put (value.name (), label);

	}

	public
	String toText (
			E value) {

		if (value == null)
			return "-";

		String ret =
			map.get (value);

		if (ret == null)
			throw new IllegalArgumentException ();

		return ret;

	}

	public
	String toTdReal (
			E value) {

		return "<td>" + toText (value) + "</td>";

	}

	@SuppressWarnings ("unchecked")
	public
	String toTd (
			Enum<?> value) {

		return toTdReal ((E) value);

	}

	public
	String select (
			String name,
			String value) {

		return Html.select (
			name,
			optionsMap,
			value);

	}

	public
	String selectNull (
			String name,
			String value,
			String nullName) {

		return Html.select (
			name,
			nullOptionsMap,
			value);

	}

	protected
	EnumConsoleHelper<E> auto () {

		for (
			E value
				: enumClass.getEnumConstants ()
		) {

			add (
				value,
				camelToSpaces (
					value.name ()));

		}

		return this;

	}

	public
	Optional<String> htmlClass (
			@NonNull E value) {

		return Optional.<String>absent ();

	}

}
