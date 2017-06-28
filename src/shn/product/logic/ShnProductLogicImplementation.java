package shn.product.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.logging.LogContext;

import shn.product.model.ShnProductVariantValueRec;

@SingletonComponent ("shnProductLogic")
public
class ShnProductLogicImplementation
	implements ShnProductLogic {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	List <ShnProductVariantValueRec> sortVariantValues (
			@NonNull Collection <ShnProductVariantValueRec>
				variantValuesUnsorted) {

		List <ShnProductVariantValueRec> variantValuesSorted =
			new ArrayList<> (
				variantValuesUnsorted);

		Collections.sort (
			variantValuesSorted,
			Ordering.compound (
				ImmutableList.of (

			Ordering.natural ().onResultOf (
				variantValue ->
					variantValue.getType ().getCode ()),

			Ordering.natural ().onResultOf (
				variantValue ->
					variantValue.getCode ())

		)));

		return variantValuesSorted;

	}

}
