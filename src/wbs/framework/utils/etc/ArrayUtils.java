package wbs.framework.utils.etc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import lombok.NonNull;

public
class ArrayUtils {

	public static
	boolean arrayIsEmpty (
			@NonNull Object[] array) {

		return array.length == 0;

	}

	public static
	boolean arrayIsNotEmpty (
			@NonNull Object[] array) {

		return array.length > 0;

	}

	@SuppressWarnings ("unchecked")
	public static <InputType, OutputType>
	List <OutputType> arrayMap (
			@NonNull Function <? super InputType, ? extends OutputType> mapFunction,
			@NonNull InputType ... input) {

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

}
