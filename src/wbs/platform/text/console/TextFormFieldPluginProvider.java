package wbs.platform.text.console;

import static wbs.framework.utils.etc.Misc.equal;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.console.forms.AbstractFormFieldPluginProvider;
import wbs.console.forms.FormFieldBuilderContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.text.model.TextRec;

@SingletonComponent ("textFormFieldPluginProvider")
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class TextFormFieldPluginProvider
	extends AbstractFormFieldPluginProvider {

	// prototype dependencies

	@Inject
	Provider<TextFormFieldNativeMapping>
	textFormFieldNativeMappingProvider;

	// implementation

	@Override
	public
	Optional getNativeMapping (
			FormFieldBuilderContext context,
			Class containerClass,
			String fieldName,
			Class genericClass,
			Class nativeClass) {

		if (

			equal (
				genericClass,
				String.class)

			&& equal (
				nativeClass,
				TextRec.class)

		) {

			return Optional.of (
				textFormFieldNativeMappingProvider.get ());

		} else {

			return Optional.absent ();

		}

	}

}
