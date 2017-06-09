package wbs.utils.string;

import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.etc.NullUtils.isNull;

import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

public
class LazyStringSingleton
	implements LazyString {

	// state

	private
	Provider <String> provider;

	private
	String value;

	// constructor

	public
	LazyStringSingleton (
			@NonNull Provider <String> provider) {

		this.provider =
			provider;

	}

	// lazy string implementation

	@Override
	public
	List <String> toParts () {

		return singletonList (
			toString ());

	}

	// object implementation

	@Override
	public
	String toString () {

		if (
			isNull (
				value)
		) {

			value =
				provider.get ();

			provider =
				null;

		}

		return value;

	}

}
