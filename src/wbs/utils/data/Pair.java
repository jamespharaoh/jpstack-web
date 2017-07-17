package wbs.utils.data;

import java.util.Map;

import lombok.NonNull;

public final
class Pair <Left, Right>
	implements Map.Entry <Left, Right> {

	// state

	private final
	Left left;

	private final
	Right right;

	// constructors;

	public
	Pair (
			@NonNull Left left,
			@NonNull Right right) {

		this.left =
			left;

		this.right =
			right;

	}

	public static <Left, Right>
	Pair <Left, Right> of (
			@NonNull Left left,
			@NonNull Right right) {

		return new Pair <Left, Right> (
			left,
			right);

	}

	// accessors

	public
	Left left () {
		return left;
	}

	public
	Right right () {
		return right;
	}

	// map entry implementations

	@Override
	public
	Left getKey () {
		return left;
	}

	@Override
	public
	Right getValue () {
		return right;
	}

	@Override
	public
	Right setValue (
			@NonNull Right value) {

		throw new UnsupportedOperationException (
			"Pair.setValue (...)");

	}

}
