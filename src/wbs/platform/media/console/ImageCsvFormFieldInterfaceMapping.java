package wbs.platform.media.console;

import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.ResultUtils.successResult;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.FormFieldInterfaceMapping;

import wbs.framework.component.annotations.PrototypeComponent;

import wbs.platform.media.model.MediaRec;

import fj.data.Either;

@PrototypeComponent ("imageCsvFormFieldInterfaceMapping")
public
class ImageCsvFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,MediaRec,String> {

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <MediaRec> genericValue) {

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
