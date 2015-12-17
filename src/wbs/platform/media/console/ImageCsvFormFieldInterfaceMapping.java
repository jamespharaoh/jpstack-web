package wbs.platform.media.console;

import static wbs.framework.utils.etc.Misc.isNotPresent;

import java.util.List;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.console.forms.FormFieldInterfaceMapping;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.media.model.MediaRec;

@PrototypeComponent ("imageCsvFormFieldInterfaceMapping")
public
class ImageCsvFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,MediaRec,String> {

	@Override
	public
	Optional<MediaRec> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			@NonNull List<String> errors) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Optional<String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<MediaRec> genericValue) {

		if (
			isNotPresent (
				genericValue)
		) {

			return Optional.of ("");

		} else {

			return Optional.of (
				genericValue.get ().getFilename ());

		}

	}

}
