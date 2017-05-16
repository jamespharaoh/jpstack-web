package wbs.console.forms.basic;

import static wbs.utils.etc.LogicUtils.equalSafe;

import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.console.forms.core.ConsoleFormBuilderContext;
import wbs.console.forms.types.ConsoleFormNativeMapping;
import wbs.console.forms.types.FormFieldPluginProvider;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;

@SingletonComponent ("identityFormFieldPluginProvider")
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class IdentityFormFieldPluginProvider
	implements FormFieldPluginProvider {

	// prototype dependencies

	@PrototypeDependency
	Provider <IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	// implementation

	@Override
	public
	Optional getNativeMapping (
			ConsoleFormBuilderContext context,
			Class containerClass,
			String fieldName,
			Class genericClass,
			Class nativeClass) {

		if (
			equalSafe (
				genericClass,
				nativeClass)
		) {

			return Optional.of (
				(ConsoleFormNativeMapping)
				identityFormFieldNativeMappingProvider.get ());

		} else {

			return Optional.absent ();

		}

	}

}
