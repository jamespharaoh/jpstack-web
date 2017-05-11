package wbs.platform.text.console;

import static wbs.utils.etc.TypeUtils.classEqualSafe;

import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.console.forms.core.FormFieldBuilderContext;
import wbs.console.forms.types.FormFieldPluginProvider;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;

import wbs.platform.text.model.TextRec;

@SingletonComponent ("textFormFieldPluginProvider")
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class TextFormFieldPluginProvider
	implements FormFieldPluginProvider {

	// prototype dependencies

	@PrototypeDependency
	Provider <TextFormFieldNativeMapping>
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

			classEqualSafe (
				genericClass,
				String.class)

			&& classEqualSafe (
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
