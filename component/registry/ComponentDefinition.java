package wbs.framework.component.registry;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataName;

import wbs.utils.data.Pair;

@Accessors (fluent = true)
@DataClass
public
class ComponentDefinition {

	@DataName
	@Getter @Setter
	String name;

	@DataAttribute
	@Getter @Setter
	String scope;

	@DataAttribute
	@Getter @Setter
	Class <?> componentClass;

	@DataAttribute
	@Getter @Setter
	Class <? extends ComponentFactory <?>> factoryClass;

	@DataAttribute
	@Getter @Setter
	Class <?> interfaceClass;

	@DataAttribute
	@Getter @Setter
	Boolean hide = false;

	@DataAttribute
	@Getter @Setter
	Boolean owned = true;

	@DataAttribute
	@Getter @Setter
	Boolean fromAnnotatedClass = false;

	@DataChildren
	@Getter @Setter
	Map <String, Object> valueProperties =
		new LinkedHashMap<> ();

	@DataChildren
	@Getter @Setter
	Map <String, Pair <String, String>> referenceProperties =
		new LinkedHashMap<> ();

	@DataChildren
	@Getter @Setter
	Map <String, Pair <String, List <String>>> referenceListProperties =
		new LinkedHashMap<> ();

	@DataChildren
	@Getter @Setter
	Map <String, Pair <String, Map <Object, String>>> referenceMapProperties =
		new LinkedHashMap<> ();

	@DataChildren
	@Getter @Setter
	List <InjectedProperty> injectedProperties =
		new ArrayList<> ();

	@DataChildren
	@Getter @Setter
	List <Method> normalSetupMethods =
		new ArrayList<> ();

	@DataChildren
	@Getter @Setter
	List <Method> lateSetupMethods =
		new ArrayList<> ();

	@DataChildren
	@Getter @Setter
	List <Method> normalTeardownMethods =
		new ArrayList<> ();

	@DataChildren
	@Getter @Setter
	Set <String> strongDependencies =
		new HashSet<> ();

	@DataChildren
	@Getter @Setter
	Set <String> weakDependencies =
		new HashSet<> ();

	// property setters

	public
	ComponentDefinition nameFormat (
			@NonNull CharSequence ... arguments) {

		return name (
			stringFormat (
				arguments));

	}

	public
	ComponentDefinition addValueProperty (
			@NonNull String name,
			@NonNull Optional <?> value) {

		if (
			optionalIsPresent (
				value)
		) {

			valueProperties.put (
				name,
				optionalGetRequired (
					value));

		}

		return this;

	}

	public
	ComponentDefinition addValuePropertyFormat (
			@NonNull String name,
			@NonNull String ... valueArguments) {

		valueProperties.put (
			name,
			stringFormatArray (
				valueArguments));

		return this;

	}

	public
	ComponentDefinition addReferenceProperty (
			@NonNull String name,
			@NonNull String targetScope,
			@NonNull String targetName) {

		referenceProperties.put (
			name,
			Pair.of (
				targetScope,
				targetName));

		return this;

	}

	public
	ComponentDefinition addReferenceProperty (
			@NonNull String name,
			@NonNull String targetScope,
			@NonNull Optional <String> targetName) {

		if (
			optionalIsPresent (
				targetName)
		) {

			referenceProperties.put (
				name,
				Pair.of (
					targetScope,
					optionalGetRequired (
						targetName)));

		}

		return this;

	}

	public
	ComponentDefinition addReferencePropertyFormat (
			@NonNull String name,
			@NonNull String targetScope,
			@NonNull String ... targetNameArguments) {

		referenceProperties.put (
			name,
			Pair.of (
				targetScope,
				stringFormatArray (
					targetNameArguments)));

		return this;

	}

	public
	ComponentDefinition addReferenceListProperty (
			@NonNull String name,
			@NonNull String targetScope,
			@NonNull List <String> targetNames) {

		referenceListProperties.put (
			name,
			Pair.of (
				targetScope,
				targetNames));

		return this;

	}

	public
	ComponentDefinition addReferenceMapProperty (
			@NonNull String name,
			@NonNull String targetScope,
			@NonNull Map <?, String> targetNames) {

		referenceMapProperties.put (
			name,
			Pair.of (
				targetScope,
				genericCastUnchecked (
					targetNames)));

		return this;

	}

}
