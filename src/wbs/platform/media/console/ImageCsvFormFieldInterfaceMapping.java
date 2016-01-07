package wbs.platform.media.console;

import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.successResult;
import lombok.NonNull;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.forms.FormFieldInterfaceMapping;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.media.model.MediaRec;

@PrototypeComponent ("imageCsvFormFieldInterfaceMapping")
public
class ImageCsvFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,MediaRec,String> {

	@Override
	public
	Either<Optional<MediaRec>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<MediaRec> genericValue) {

		if (
			isNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.of (
					""));

		} else {

			return successResult (
				Optional.of (
					genericValue.get ().getFilename ()));

		}

	}

}
