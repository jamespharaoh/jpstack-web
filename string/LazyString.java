package wbs.utils.string;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

public
class LazyString
	implements CharSequence {

	// state

	private final
	Provider <String> provider;

	private
	Optional <String> value =
		optionalAbsent ();

	// constructors

	public
	LazyString (
			@NonNull Provider <String> provider) {

		this.provider =
			provider;

	}

	// implementation

	@Override
	public
	int length () {

		return toString ().length ();

	}

	@Override
	public
	char charAt (
			int index) {

		return toString ().charAt (
			index);


	}

	@Override
	public
	CharSequence subSequence (
			int start,
			int end) {

		return toString ().subSequence (
			start,
			end);

	}

	@Override
	public
	String toString () {

		if (
			optionalIsNotPresent (
				value)
		) {

			value =
				optionalOf (
					provider.get ());

		}

		return optionalGetRequired (
			value);

	}

}
