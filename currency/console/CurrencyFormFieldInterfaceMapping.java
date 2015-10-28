package wbs.platform.currency.console;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.console.forms.FormFieldInterfaceMapping;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.currency.model.CurrencyRec;

@Accessors (fluent = true)
@PrototypeComponent ("currencyFormFieldInterfaceMapping")
public
class CurrencyFormFieldInterfaceMapping<Container extends Record<?>>
	implements FormFieldInterfaceMapping<Container,Long,String> {

	// dependencies

	@Inject
	CurrencyLogic currencyLogic;

	@Inject
	ObjectManager objectManager;

	// properties

	@Getter @Setter
	String currencyPath;

	@Getter @Setter
	Boolean blankIfZero = false;

	// implementation

	@Override
	public
	Optional<Long> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			@NonNull List<String> errors) {

		if (! interfaceValue.isPresent ()) {
			return Optional.<Long>absent ();
		}

		if (interfaceValue.get ().isEmpty ()) {
			return Optional.<Long>absent ();
		}

		CurrencyRec currency =
			(CurrencyRec)
			(Object)
			objectManager.dereference (
				container,
				currencyPath);

		if (currency != null) {

			return Optional.of (
				currencyLogic.parseText (
					currency,
					interfaceValue.get ()));

		} else {

			return Optional.of (
				Long.parseLong (
					interfaceValue.get ()));

		}

	}

	@Override
	public
	Optional<String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<Long> genericValue) {

		if (! genericValue.isPresent ()) {
			return Optional.<String>absent ();
		}

		CurrencyRec currency =
			(CurrencyRec)
			(Object)
			objectManager.dereference (
				container,
				currencyPath);

		if (genericValue.get () == 0 && blankIfZero) {
			return Optional.of ("");
		}

		if (currency != null) {

			return Optional.of (
				currencyLogic.formatText (
					currency,
					genericValue.get ()));

		} else {

			return Optional.of (
				Long.toString (
					genericValue.get ()));

		}


	}

}
