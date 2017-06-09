package wbs.utils.string;

import static wbs.utils.collection.CollectionUtils.singletonList;

import java.util.List;

import lombok.NonNull;

public
class LazyStringSimple
	implements LazyString {

	// state

	private final
	String value;

	// constructors

	public
	LazyStringSimple (
			@NonNull String value) {

		this.value =
			value;

	}

	// lazy string implementation

	@Override
	public
	List <String> toParts () {

		return singletonList (
			value);

	}

	// char sequence implementation

	@Override
	public
	int length () {

		return value.length ();

	}

	@Override
	public
	char charAt (
			int index) {

		return value.charAt (
			index);

	}

	@Override
	public
	CharSequence subSequence (
			int start,
			int end) {

		return value.subSequence (
			start,
			end);

	}

	// object implementation

	@Override
	public
	String toString () {

		return value.toString ();

	}

}
