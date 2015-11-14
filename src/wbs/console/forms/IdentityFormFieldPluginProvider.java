package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.equal;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("identityFormFieldPluginProvider")
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class IdentityFormFieldPluginProvider
	extends AbstractFormFieldPluginProvider {

	// prototype dependencies

	@Inject
	Provider<IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

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
				nativeClass)
		) {

			return Optional.of (
				(FormFieldNativeMapping)
				identityFormFieldNativeMappingProvider.get ());

		} else {

			return Optional.absent ();

		}

	}

}
