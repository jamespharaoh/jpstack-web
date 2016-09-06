package wbs.framework.utils.etc;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

public
class IterableUtils {

	public static
	long iterableCount (
			@NonNull Iterable <?> iterable) {

		long size = 0;

		for (
			@SuppressWarnings ("unused")
			Object _item
				: iterable
		) {
			size ++;
		}

		return size;

	}

	public static <InputType, OutputType>
	Iterable <OutputType> iterableMap (
			@NonNull Function <
				? super InputType,
				? extends OutputType
			> mapFunction,
			@NonNull Iterable <InputType> input) {

		List <OutputType> output =
			new ArrayList<> ();

		for (
			InputType inputItem
				: input
		) {

			output.add (
				mapFunction.apply (
					inputItem));

		}

		return output;

	}

	public static <InputType, OutputType>
	List <OutputType> iterableMapToList (
			@NonNull Function <
				? super InputType,
				? extends OutputType
			> mapFunction,
			@NonNull Iterable <InputType> input) {

		return ImmutableList.copyOf (
			iterableMap (
				mapFunction,
				input));

	}

}
