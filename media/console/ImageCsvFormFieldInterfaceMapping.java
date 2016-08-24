package wbs.platform.media.console;

import static wbs.framework.utils.etc.Misc.successResult;

import java.util.Map;

import lombok.NonNull;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.forms.FormFieldInterfaceMapping;
import wbs.framework.application.annotations.PrototypeComponent;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;
import wbs.platform.media.model.MediaRec;

@PrototypeComponent ("imageCsvFormFieldInterfaceMapping")
public
class ImageCsvFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,MediaRec,String> {

	@Override
	public
	Either<Optional<MediaRec>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<MediaRec> genericValue) {

		if (
			optionalIsNotPresent (
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
