package shn.product.console;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.listItemAtIndex;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.forms.core.ConsoleFormBuilder;
import wbs.console.forms.object.ObjectFieldsProvider;
import wbs.console.forms.object.ObjectFormFieldSpec;
import wbs.console.forms.types.ConsoleFormFieldSpec;
import wbs.console.forms.types.FieldsProvider.FormFieldSetPair;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import shn.product.model.ShnProductRec;
import shn.product.model.ShnProductVariantRec;
import shn.product.model.ShnProductVariantValueRec;

@Accessors (fluent = true)
@PrototypeComponent ("shnProductVariantObjectFieldsProvider")
public
class ShnProductVariantObjectFieldsProvider
	implements ObjectFieldsProvider <ShnProductVariantRec, ShnProductRec> {

	// singleton dependencies

	@SingletonDependency
	ConsoleFormBuilder consoleFormBuilder;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ShnProductVariantConsoleHelper productVariantHelper;

	// details

	@Override
	public
	Class <ShnProductVariantRec> containerClass () {
		return ShnProductVariantRec.class;
	}

	@Override
	public
	Class <ShnProductRec> parentClass () {
		return ShnProductRec.class;
	}

	// public implementation

	@Override
	public
	FormFieldSetPair <ShnProductVariantRec> getListFields (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductVariantRec object) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	FormFieldSetPair <ShnProductVariantRec> getCreateFields (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductRec parent) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	FormFieldSetPair <ShnProductVariantRec> getSummaryFields (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductVariantRec productVariant) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getSummaryFields");

		) {

			List <ConsoleFormFieldSpec> columnFields =
				new ArrayList<> ();

			List <ConsoleFormFieldSpec> rowFields =
				new ArrayList<> ();

			// add variant fields

			addVariantFields (
				transaction,
				columnFields,
				productVariant,
				collectionSize (
					productVariant.getVariantValues ()));

			return new FormFieldSetPair <ShnProductVariantRec> ()

				.columnFields (
					consoleFormBuilder.buildFormFieldSet (
						transaction,
						productVariantHelper,
						"settings",
						columnFields))

				.rowFields (
					consoleFormBuilder.buildFormFieldSet (
						transaction,
						productVariantHelper,
						"settings",
						rowFields))

			;

		}

	}

	@Override
	public
	FormFieldSetPair <ShnProductVariantRec> getSettingsFields (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductVariantRec productVariant) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getSettingsFields");

		) {

			List <ConsoleFormFieldSpec> columnFields =
				new ArrayList<> ();

			List <ConsoleFormFieldSpec> rowFields =
				new ArrayList<> ();

			// add variant fields

			addVariantFields (
				transaction,
				columnFields,
				productVariant,
				10l);

			return new FormFieldSetPair <ShnProductVariantRec> ()

				.columnFields (
					consoleFormBuilder.buildFormFieldSet (
						transaction,
						productVariantHelper,
						"settings",
						columnFields))

				.rowFields (
					consoleFormBuilder.buildFormFieldSet (
						transaction,
						productVariantHelper,
						"settings",
						rowFields))

			;

		}

	}

	// private implementation

	private
	void addVariantFields (
			@NonNull Transaction parentTransaction,
			@NonNull List <ConsoleFormFieldSpec> columnFields,
			@NonNull ShnProductVariantRec productVariant,
			@NonNull Long maxVariants) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"addVariantFields");

		) {

			List <ShnProductVariantValueRec> variantValues =
				new ArrayList<> (
					productVariant.getVariantValues ());

			Collections.sort (
				variantValues,
				Ordering.compound (
					ImmutableList.of (

				Ordering.natural ().onResultOf (
					variantValue ->
						variantValue.getType ().getCode ()),

				Ordering.natural ().onResultOf (
					variantValue ->
						variantValue.getCode ())

			)));

			for (
				long index = 0;
				index < maxVariants;
				index ++
			) {

				Optional <ShnProductVariantValueRec> variantValueOptional =
					listItemAtIndex (
						variantValues,
						index);

				String name;
				String label;

				if (
					optionalIsPresent (
						variantValueOptional)
				) {

					ShnProductVariantValueRec variantValue =
						optionalGetRequired (
							variantValueOptional);

					name =
						stringFormat (
							"variant.%s",
							variantValue.getType ().getCode ());

					label =
						stringFormat (
							"Variant type %s",
							variantValue.getType ().getCode ());

				} else {

					name =
						stringFormat (
							"variant.%s",
							integerToDecimalString (
								index));

					label =
						stringFormat (
							"Variant type %s",
							integerToDecimalString (
								index + 1));

				}

				columnFields.add (
					new ObjectFormFieldSpec ()

					.name (
						name)

					.dynamic (
						true)

					.objectTypeName (
						"shnProductVariantValue")

					.label (
						label)

					.nullable (
						true)

				);

			}

		}

	}

}
