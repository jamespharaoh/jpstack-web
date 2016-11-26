package wbs.framework.component.registry;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.collection.IterableUtils.iterableCount;
import static wbs.utils.collection.MapUtils.mapContainsKey;
import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanOne;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.etc.TypeUtils.classNotEqual;
import static wbs.utils.etc.TypeUtils.classNotInSafe;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.nullIfEmptyString;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotInSafe;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import wbs.framework.activitymanager.ActiveTask;
import wbs.framework.activitymanager.ActivityManager;
import wbs.framework.activitymanager.RuntimeExceptionWithTask;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.component.manager.ComponentManagerImplementation;
import wbs.framework.component.registry.InjectedProperty.CollectionType;
import wbs.framework.component.tools.EasyReadWriteLock;
import wbs.framework.component.tools.EasyReadWriteLock.HeldLock;
import wbs.framework.component.tools.SingletonComponentFactory;
import wbs.framework.component.xml.ComponentPropertyValueSpec;
import wbs.framework.component.xml.ComponentsComponentSpec;
import wbs.framework.component.xml.ComponentsPropertiesPropertySpec;
import wbs.framework.component.xml.ComponentsReferencePropertySpec;
import wbs.framework.component.xml.ComponentsSpec;
import wbs.framework.component.xml.ComponentsValuePropertySpec;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.data.tools.DataToXml;
import wbs.framework.logging.DefaultLogContext;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.LogContextComponentFactory;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class ComponentRegistryImplementation
	implements ComponentRegistry {

	private final static
	LogContext logContext =
		DefaultLogContext.forClass (
			ComponentRegistryImplementation.class);

	// properties

	@Getter @Setter
	ActivityManager activityManager;

	@Getter @Setter
	String outputPath;

	// state

	EasyReadWriteLock lock =
		EasyReadWriteLock.instantiate ();

	List <ComponentDefinition> definitions =
		new ArrayList<> ();

	List <ComponentDefinition> singletons =
		new ArrayList<> ();

	List <ComponentDefinition> prototypes =
		new ArrayList<> ();

	Map <String, ComponentDefinition> byName =
		new HashMap<> ();

	Map <Class <?>, Map <String, ComponentDefinition>> byClass =
		new HashMap<> ();

	Map <Class <?>, Map <String, ComponentDefinition>> singletonsByClass =
		new HashMap<> ();

	Map <Class <?>, Map <String, ComponentDefinition>> prototypesByClass =
		new HashMap<> ();

	Map <Annotation, List <ComponentDefinition>> byQualifier =
		new HashMap<> ();

	Map <Annotation, List <ComponentDefinition>> singletonsByQualifier =
		new HashMap<> ();

	Map <Annotation, List <ComponentDefinition>> prototypesByQualifier =
		new HashMap<> ();

	List <String> requestComponentNames =
		new ArrayList<> ();

	// accessors

	@Override
	public
	List <ComponentDefinition> all () {
		return definitions;
	}

	@Override
	public
	Optional <ComponentDefinition> byName (
			@NonNull String componentName) {

		return optionalFromNullable (
			byName.get (
				componentName));

	}

	@Override
	public
	ComponentDefinition byNameRequired (
			@NonNull String componentName) {

		return requiredValue (
			byName.get (
				componentName));

	}

	@Override
	public
	boolean hasName (
			@NonNull String componentName) {

		return isNotNull (
			byName.get (
				componentName));

	}

	@Override
	public
	Map <String, ComponentDefinition> singletonsByClass (
			@NonNull Class <?> targetClass) {

		return singletonsByClass.get (
			targetClass);

	}

	@Override
	public
	Map <String, ComponentDefinition> prototypesByClass (
			@NonNull Class <?> targetClass) {

		return prototypesByClass.get (
			targetClass);

	}

	@Override
	public
	List <ComponentDefinition> withAnnotation (
			@NonNull Class <? extends Annotation> annotationClass) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		ImmutableList.Builder <ComponentDefinition>
			componentDefinitionsWithAnnotationBuilder =
				ImmutableList.builder ();

		for (
			ComponentDefinition componentDefinition
				: definitions
		) {

			Annotation annotation =
				componentDefinition.componentClass ().getAnnotation (
					annotationClass);

			if (annotation == null)
				continue;

			componentDefinitionsWithAnnotationBuilder.add (
				componentDefinition);

		}

		return componentDefinitionsWithAnnotationBuilder.build ();

	}

	// public implementation

	@Override
	public
	ComponentManager build () {

		TaskLogger taskLogger =
			logContext.createTaskLogger (
				"build");

		try (

			ActiveTask activeTask =
				activityManager.start (
					"component-registry",
					"build ()",
					this);

		) {

			// create component manager

			@SuppressWarnings ("resource")
			ComponentManagerImplementation componentManager =
				new ComponentManagerImplementation ()

				.registry (
					this)

				.activityManager (
					activityManager);

			// automatic components

			registerUnmanagedSingleton (
				"componentManager",
				componentManager);

			registerUnmanagedSingleton (
				"activityManager",
				activityManager);

			// register scoped singletons

			for (
				ComponentDefinition componentDefinition
					: ImmutableList.copyOf (
						definitions)
			) {

				registerScopedSingletons (
					taskLogger,
					componentDefinition);

			}

			// work out dependencies

			for (
				ComponentDefinition componentDefinition
					: definitions
			) {

				initComponentDefinition (
					taskLogger,
					componentDefinition);

			}

			// check dependencies exist

			for (
				ComponentDefinition componentDefinition
					: definitions
			) {

				for (
					String dependency
						: componentDefinition.strongDependencies ()
				) {

					if (
						! byName.containsKey (
							dependency)
					) {

						taskLogger.errorFormat (
							"Can't provide dependency %s for %s",
							dependency,
							componentDefinition.name ());

					}

				}

			}

			// order component definitions

			singletons =
				ImmutableList.copyOf (
					orderByStrongDepedendencies (
						taskLogger,
						singletons));

			// output component definitions

			if (outputPath != null) {

				outputComponentDefinitions (
					taskLogger,
					outputPath);

			}

			taskLogger.makeException ();

			// initialise

			componentManager.init (
				taskLogger);

			taskLogger.makeException ();

			// and return

			return componentManager;

		}

	}

	@Override
	public
	ComponentRegistryImplementation registerDefinition (
			@NonNull ComponentDefinition componentDefinition) {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		// sanity check

		if (componentDefinition.name () == null) {

			throw new RuntimeExceptionWithTask (
				activityManager.currentTask (),
				stringFormat (
					"Component definition has no name"));

		}

		if (componentDefinition.componentClass () == null) {

			throw new RuntimeExceptionWithTask (
				activityManager.currentTask (),
				stringFormat (
					"Component definition %s has no component class",
					componentDefinition.name ()));

		}

		if (componentDefinition.scope () == null) {

			throw new RuntimeExceptionWithTask (
				activityManager.currentTask (),
				stringFormat (
					"Copmonent definition %s has no scope",
					componentDefinition.name ()));

		}

		if (
			byName.containsKey (
				componentDefinition.name ())
		) {

			throw new RuntimeExceptionWithTask (
				activityManager.currentTask (),
				stringFormat (
					"Duplicated component definition name %s",
					componentDefinition.name ()));

		}

		if (
			stringNotInSafe (
				componentDefinition.scope (),
				"singleton",
				"prototype")
		) {

			throw new RuntimeExceptionWithTask (
				activityManager.currentTask (),
				stringFormat (
					"Component definition %s has invalid scope %s",
					componentDefinition.name (),
					componentDefinition.scope ()));

		}

		Class <?> instantiationClass =
			ifNull (
				componentDefinition.factoryClass (),
				componentDefinition.componentClass ());

		// check the class looks ok

		if (! Modifier.isPublic (
				instantiationClass.getModifiers ())) {

			throw new RuntimeExceptionWithTask (
				activityManager.currentTask (),
				stringFormat (
					"Component definition %s refers to non-public class %s",
					componentDefinition.name (),
					instantiationClass.getName ()));

		}

		if (Modifier.isAbstract (
				instantiationClass.getModifiers ())) {

			throw new RuntimeExceptionWithTask (
				activityManager.currentTask (),
				stringFormat (
					"Component definition %s refers to abstract class %s",
					componentDefinition.name (),
					instantiationClass.getName ()));

		}

		Constructor<?> constructor;

		try {

			constructor =
				instantiationClass.getDeclaredConstructor ();

		} catch (NoSuchMethodException exception) {

			throw new RuntimeExceptionWithTask (
				activityManager.currentTask (),
				stringFormat (
					"Component definition %s refers class %s with no default ",
					componentDefinition.name (),
					instantiationClass.getName (),
					"constructor"));

		}

		if (
			! Modifier.isPublic (
				constructor.getModifiers ())
		) {

			throw new RuntimeExceptionWithTask (
				activityManager.currentTask (),
				stringFormat (
					"Component definition %s ",
					componentDefinition.name (),
					"refers to class %s ",
					instantiationClass.getName (),
					"with non-public default constructor"));

		}

		// store component definition

		definitions.add (
			componentDefinition);

		if (
			stringEqualSafe (
				componentDefinition.scope (),
				"singleton")
		) {

			singletons.add (
				componentDefinition);

		}

		if (
			stringEqualSafe (
				componentDefinition.scope (),
				"prototype")
		) {

			prototypes.add (
				componentDefinition);

		}

		// index by name

		byName.put (
			componentDefinition.name (),
			componentDefinition);

		// index by class

		if (! componentDefinition.hide ()) {

			Set <Class <?>> componentClasses =
				new HashSet<> ();

			componentClasses.add (
				componentDefinition.componentClass ());

			componentClasses.addAll (
				ClassUtils.getAllSuperclasses (
					componentDefinition.componentClass ()));

			componentClasses.addAll (
				ClassUtils.getAllInterfaces (
					componentDefinition.componentClass ()));

			updateIndexByClass (
				byClass,
				componentClasses,
				componentDefinition);

			if (
				stringEqualSafe (
					componentDefinition.scope (),
					"singleton")
			) {

				updateIndexByClass (
					singletonsByClass,
					componentClasses,
					componentDefinition);

			}

			if (
				stringEqualSafe (
					componentDefinition.scope (),
					"prototype")
			) {

				updateIndexByClass (
					prototypesByClass,
					componentClasses,
					componentDefinition);

			}

		}

		// index by qualifiers

		if (! componentDefinition.hide ()) {

			for (
				Annotation annotation
					: componentDefinition.componentClass ()
						.getDeclaredAnnotations ()
			) {

				Qualifier qualifierAnnotation =
					annotation
						.annotationType ()
						.getAnnotation (Qualifier.class);

				if (qualifierAnnotation == null)
					continue;

				updateIndexByQualifier (
					byQualifier,
					annotation,
					componentDefinition);

				if (
					stringEqualSafe (
						componentDefinition.scope (),
						"singleton")
				) {

					updateIndexByQualifier (
						singletonsByQualifier,
						annotation,
						componentDefinition);

				}

				if (
					stringEqualSafe (
						componentDefinition.scope (),
						"prototype")
				) {

					updateIndexByQualifier (
						prototypesByQualifier,
						annotation,
						componentDefinition);

				}

			}

		}

		return this;

	}

	@Override
	public
	ComponentRegistryImplementation registerUnmanagedSingleton (
			@NonNull String componentName,
			@NonNull Object object) {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		ComponentDefinition componentDefinition =
			new ComponentDefinition ()

			.name (
				componentName)

			.componentClass (
				object.getClass ())

			.scope (
				"singleton")

			.factoryClass (
				SingletonComponentFactory.class)

			.addValueProperty (
				"object",
				object)

			.owned (
				false);

		registerDefinition (
			componentDefinition);

		return this;

	}

	public
	ComponentRegistryImplementation registerXmlClasspath (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String classpath) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"registerXmlClasspath");

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		ComponentsSpec components =
			(ComponentsSpec)
			dataFromXml.readClasspath (
				taskLogger,
				classpath);

		components.register (
			this);

		return this;

	}

	@Override
	public
	ComponentRegistryImplementation registerXmlFilename (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String filename) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"registerXmlClasspath");
		@Cleanup
		HeldLock heldlock =
			lock.write ();

		ComponentsSpec componentsSpec =
			(ComponentsSpec)
			dataFromXml.readFilename (
				taskLogger,
				filename);

		componentsSpec.register (
			this);

		return this;

	}

	@Override
	public
	ComponentRegistry addRequestComponentName (
			@NonNull String name) {

		requestComponentNames.add (
			name);

		return this;

	}

	@Override
	public
	List <String> requestComponentNames () {

		return requestComponentNames;

	}

	public
	void outputComponentDefinitions (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String outputPath) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"outputComponentDefinitions");

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		taskLogger.noticeFormat (
			"Writing component definitions to %s",
			outputPath);

		try {

			FileUtils.deleteDirectory (
				new File (outputPath));

			FileUtils.forceMkdir (
				new File (outputPath));

		} catch (IOException exception) {

			taskLogger.warningFormatException (
				exception,
				"Error deleting contents of %s",
				outputPath);

		}

		for (
			ComponentDefinition componentDefinition
				: definitions
		) {

			String outputFile =
				stringFormat (
					"%s/%s.xml",
					outputPath,
					componentDefinition.name ());

			try {

				new DataToXml ().writeToFile (
					outputFile,
					componentDefinition);

			} catch (Exception exception) {

				taskLogger.warningFormatException (
					exception,
					"Error writing %s",
					outputFile);

			}

		}

		String orderedSingletonsFile =
			stringFormat (
				"%s/singleton-component-list.xml",
				outputPath);

		try {

			new DataToXml ().writeToFile (
				orderedSingletonsFile,
				singletons);

		} catch (Exception exception) {

			taskLogger.warningFormatException (
				exception,
				"Error writing %s",
				orderedSingletonsFile);

		}

	}

	// private implementation

	private
	void updateIndexByClass (
			@NonNull Map <Class <?>, Map <String, ComponentDefinition>> index,
			@NonNull Set <Class <?>> componentClasses,
			@NonNull ComponentDefinition componentDefinition) {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		for (
			Class<?> componentClass
				: componentClasses
		) {

			Map <String, ComponentDefinition> componentDefinitionsForClass =
				index.get (
					componentClass);

			if (componentDefinitionsForClass == null) {

				index.put (
					componentClass,
					componentDefinitionsForClass =
						new HashMap <> ());

			}

			componentDefinitionsForClass.put (
				componentDefinition.name (),
				componentDefinition);

		}

	}

	private
	void updateIndexByQualifier (
			@NonNull Map <Annotation, List <ComponentDefinition>> index,
			@NonNull Annotation annotation,
			@NonNull ComponentDefinition componentDefinition) {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		List <ComponentDefinition> componentDefinitionsForQualifier =
			index.get (annotation);

		if (componentDefinitionsForQualifier == null) {

			index.put (
				annotation,
				componentDefinitionsForQualifier =
					new ArrayList <> ());

		}

		componentDefinitionsForQualifier.add (
			componentDefinition);

	}

	private
	void initInjectedPropertyTargetByQualifier (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Annotation qualifier,
			@NonNull InjectedProperty injectedProperty,
			@NonNull Boolean weak) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"initInjectedPropertyTargetByQualifier");

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		// lookup target components

		List <ComponentDefinition> targetComponentDefinitions =
			ifNull (
				injectedProperty.prototype ()
					? prototypesByQualifier.get (
						qualifier)
					: singletonsByQualifier.get (
						qualifier),
				Collections.emptyList ());

		if (injectedProperty.collectionType () == CollectionType.single) {

			if (targetComponentDefinitions.isEmpty ()) {

				taskLogger.errorFormat (
					"Unable to find %s component of type %s for %s.%s",
					injectedProperty.prototype ()
						? "prototype"
						: "singleton",
					injectedProperty.targetType ().toString (),
					classNameSimple (
						injectedProperty.field ().getDeclaringClass ()),
					injectedProperty.field ().getName ());

				return;

			}

			if (targetComponentDefinitions.size () > 1) {

				taskLogger.errorFormat (
					"Found %s candidate components of type %s for %s.%s",
					integerToDecimalString (
						targetComponentDefinitions.size ()),
					injectedProperty.targetType ().toString (),
					classNameSimple (
						injectedProperty.field ().getClass ()),
					injectedProperty.field ().getName ());

				return;

			}

		}

		// register dependencies

		if (! injectedProperty.prototype ()) {

			for (
				ComponentDefinition targetComponentDefinition
					: targetComponentDefinitions
			) {

				if (weak) {

					componentDefinition.weakDependencies ().add (
						targetComponentDefinition.name ());

				} else {

					componentDefinition.strongDependencies ().add (
						targetComponentDefinition.name ());

				}

			}

		}

		// store injected target components

		List <String> targetComponentDefinitionNames =
			new ArrayList <> ();

		for (
			ComponentDefinition targetComponentDefinition
				: targetComponentDefinitions
		) {

			targetComponentDefinitionNames.add (
				targetComponentDefinition.name ());

		}

		injectedProperty.targetComponentNames (
			ImmutableList.copyOf (
				targetComponentDefinitionNames));

		return;

	}

	private
	void registerScopedSingletons (
			@NonNull TaskLogger taskLogger,
			@NonNull ComponentDefinition componentDefinition) {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		Class <?> instantiateClass =
			ifNull (
				componentDefinition.factoryClass (),
				componentDefinition.componentClass ());

		for (
			Field field
				: FieldUtils.getAllFields (
					instantiateClass)
		) {

			ClassSingletonDependency classSingletonDependencyAnnotation =
				field.getAnnotation (
					ClassSingletonDependency.class);

			if (
				isNull (
					classSingletonDependencyAnnotation)
			) {
				continue;
			}

			if (
				classNotEqual (
					field.getType (),
					LogContext.class)
			) {
				throw new RuntimeException ();
			}

			String scopedComponentName =
				stringFormat (
					"%s:%s",
					classNameFull (
						field.getDeclaringClass ()),
					classNameFull (
						LogContext.class));

			if (
				mapContainsKey (
					byName,
					scopedComponentName)
			) {
				continue;
			}

			registerDefinition (
				new ComponentDefinition ()

				.name (
					scopedComponentName)

				.scope (
					"singleton")

				.componentClass (
					LogContext.class)

				.factoryClass (
					LogContextComponentFactory.class)

				.hide (
					true)

				.addValueProperty (
					"componentClass",
					field.getDeclaringClass ())

			);

		}

	}

	private
	void initComponentDefinition (
			@NonNull TaskLogger taskLogger,
			@NonNull ComponentDefinition componentDefinition) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		Class <?> instantiateClass =
			ifNull (
				componentDefinition.factoryClass (),
				componentDefinition.componentClass ());

		componentDefinition.strongDependencies ().addAll (
			componentDefinition.referenceProperties ().values ());

		for (
			Field field
				: FieldUtils.getAllFields (
					instantiateClass)
		) {

			PrototypeDependency prototypeDependencyAnnotation =
				field.getAnnotation (
					PrototypeDependency.class);

			SingletonDependency singletonDependencyAnnotation =
				field.getAnnotation (
					SingletonDependency.class);

			ClassSingletonDependency classSingletonDependencyAnnotation =
				field.getAnnotation (
					ClassSingletonDependency.class);

			WeakSingletonDependency weakSingletonDependencyAnnotation =
				field.getAnnotation (
					WeakSingletonDependency.class);

			UninitializedDependency uninitializedDependencyAnnotation =
				field.getAnnotation (
					UninitializedDependency.class);

			long numAnnotations =
				iterableCount (
					presentInstances (
						optionalFromNullable (
							prototypeDependencyAnnotation),
						optionalFromNullable (
							singletonDependencyAnnotation),
						optionalFromNullable (
							classSingletonDependencyAnnotation),
						optionalFromNullable (
							weakSingletonDependencyAnnotation),
						optionalFromNullable (
							uninitializedDependencyAnnotation)));

			if (
				equalToZero (
					numAnnotations)
			) {
				continue;
			}

			if (
				moreThanOne (
					numAnnotations)
			) {

				throw new RuntimeExceptionWithTask (
					activityManager.currentTask ());

			}

			Boolean prototype =
				isNotNull (
					prototypeDependencyAnnotation)
				|| isNotNull (
					uninitializedDependencyAnnotation);

			Boolean scoped =
				isNotNull (
					classSingletonDependencyAnnotation);

			Boolean initialized =
				isNull (
					uninitializedDependencyAnnotation);

			Boolean weak =
				isNotNull (
					weakSingletonDependencyAnnotation);

			Named namedAnnotation =
				field.getAnnotation (
					Named.class);

			if (
				isNotNull (
					namedAnnotation)
			) {

				if (scoped) {
					throw new RuntimeException ();
				}

				String targetComponentName =
					ifNull (
						nullIfEmptyString (
							namedAnnotation.value ()),
						field.getName ());

				initInjectedFieldByName (
					taskLogger,
					componentDefinition,
					targetComponentName,
					field,
					prototype,
					initialized,
					weak);

			} else if (scoped) {

				String targetComponentName =
					stringFormat (
						"%s:%s",
						classNameFull (
							field.getDeclaringClass ()),
						classNameFull (
							field.getType ()));

				initInjectedFieldByName (
					taskLogger,
					componentDefinition,
					targetComponentName,
					field,
					prototype,
					initialized,
					weak);

			} else {

				List <Annotation> qualifierAnnotations =
					new ArrayList <Annotation> ();

				for (
					Annotation annotation
						: field.getDeclaredAnnotations ()
				) {

					if (annotation instanceof Named)
						continue;

					Qualifier metaAnnotation =
						annotation.annotationType ().getAnnotation (
							Qualifier.class);

					if (metaAnnotation == null)
						continue;

					qualifierAnnotations.add (
						annotation);

				}

				if (qualifierAnnotations.size () > 1) {

					throw new RuntimeExceptionWithTask (
						activityManager.currentTask ());

				}

				InjectedProperty injectedProperty =
					new InjectedProperty ()

					.componentDefinition (
						componentDefinition)

					.field (
						field)

					.initialized (
						isNull (
							uninitializedDependencyAnnotation))

					.weak (
						weak);

				initInjectedPropertyField (
					taskLogger,
					componentDefinition,
					field,
					injectedProperty);

				if (qualifierAnnotations.size () == 1) {

					initInjectedPropertyTargetByQualifier (
						taskLogger,
						componentDefinition,
						qualifierAnnotations.get (0),
						injectedProperty,
						weak);

				} else {

					initInjectedPropertyTargetByClass (
						taskLogger,
						componentDefinition,
						field,
						injectedProperty,
						weak);

				}

				componentDefinition.injectedProperties ().add (
					injectedProperty);

			}

		}

		return;

	}

	private
	void initInjectedFieldByName (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition,
			@NonNull String targetComponentName,
			@NonNull Field field,
			@NonNull Boolean prototype,
			@NonNull Boolean initialized,
			@NonNull Boolean weak) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"initInjectedFieldByName");

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		// TODO merge this

		Optional <ComponentDefinition> targetComponentDefinitionOptional =
			optionalFromNullable (
				byName.get (
					targetComponentName));

		if (
			optionalIsNotPresent (
				targetComponentDefinitionOptional)
		) {

			taskLogger.errorFormat (
				"Named component %s does not exist for %s.%s",
				targetComponentName,
				componentDefinition.name (),
				field.getName ());

			return;

		}

		ComponentDefinition targetComponentDefinition =
			optionalGetRequired (
				targetComponentDefinitionOptional);

		if (weak) {

			componentDefinition.weakDependencies ().add (
				targetComponentDefinition.name ());

		} else {

			componentDefinition.strongDependencies ().add (
				targetComponentDefinition.name ());

		}

		componentDefinition.injectedProperties ().add (
			new InjectedProperty ()

			.componentDefinition (
				componentDefinition)

			.field (
				field)

			.prototype (
				prototype)

			.initialized (
				initialized)

			.targetComponentNames (
				Collections.singletonList (
					targetComponentName))

			.weak (
				weak)

		);

		return;

	}

	private
	void initInjectedPropertyField (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Field field,
			@NonNull InjectedProperty injectedProperty) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"initInjectedPropertyField");

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		Type fieldType =
			field.getGenericType ();

		ParameterizedType parameterizedFieldType =
			field.getGenericType () instanceof ParameterizedType
				? (ParameterizedType) field.getGenericType ()
				: null;

		Class <?> fieldClass =
			parameterizedFieldType != null
				? (Class <?>) parameterizedFieldType.getRawType ()
				: (Class <?>) fieldType;

		// handle collections

		Type injectType;

		InjectedProperty.CollectionType collectionType;

		if (fieldClass == List.class
				&& parameterizedFieldType != null) {

			injectType =
				parameterizedFieldType
					.getActualTypeArguments () [0];

			collectionType = CollectionType.list;

		} else if (fieldClass == Map.class
				&& parameterizedFieldType != null) {

			Type keyType =
				parameterizedFieldType
					.getActualTypeArguments () [0];

			ParameterizedType keyParameterizedType =
				keyType instanceof ParameterizedType
					? (ParameterizedType) keyType
					: null;

			Class<?> keyClass =
				keyParameterizedType != null
					? (Class<?>) keyParameterizedType.getRawType ()
					: (Class<?>) keyType;

			if (
				classNotInSafe (
					keyClass,
					String.class,
					Class.class)
			) {

				taskLogger.errorFormat (
					"Don't know how to inject map with key type %s for %s.%s",
					keyType.toString (),
					componentDefinition.name (),
					field.getName ());

				return;

			}

			injectType =
				parameterizedFieldType
					.getActualTypeArguments () [1];

			collectionType =
				keyClass == String.class
					? CollectionType.componentNameMap
					: CollectionType.componentClassMap;

		} else {

			injectType =
				fieldType;

			collectionType =
				CollectionType.single;

		}

		ParameterizedType parameterizedInjectType =
			injectType instanceof ParameterizedType
				? (ParameterizedType) injectType
				: null;

		Class<?> injectClass =
			parameterizedInjectType != null
				? (Class<?>) parameterizedInjectType.getRawType ()
				: (Class<?>) injectType;

		// handle providers

		Type valueType;

		boolean isProvider;

		if (injectClass == Provider.class) {

			if (parameterizedInjectType == null) {

				taskLogger.errorFormat (
					"No type information for provider %s at %s.%s",
					injectType.toString (),
					componentDefinition.name (),
					field.getName ());

				return;

			}

			valueType =
				parameterizedInjectType
					.getActualTypeArguments () [0];

			isProvider = true;

		} else {

			valueType =
				injectType;

			isProvider = false;

		}

		// return

		injectedProperty
			.collectionType (collectionType)
			.prototype (isProvider)
			.finalType (fieldType)
			.injectType (injectType)
			.targetType (valueType);

		return;

	}

	private
	void initInjectedPropertyTargetByClass (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Field field,
			@NonNull InjectedProperty injectedProperty,
			@NonNull Boolean weak) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"initInjectedPropertyTargetByClass");

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		// lookup target components

		ParameterizedType parameterizedTargetType =
			injectedProperty.targetType () instanceof ParameterizedType
				? (ParameterizedType) injectedProperty.targetType ()
				: null;

		Class <?> targetClass =
			parameterizedTargetType != null
				? (Class <?>) parameterizedTargetType.getRawType ()
				: (Class <?>) injectedProperty.targetType ();

		Map <String, ComponentDefinition> targetComponentDefinitions =
			ifNull (
				injectedProperty.prototype ()
					? prototypesByClass.get (
						targetClass)
					: singletonsByClass.get (
						targetClass),
				Collections.emptyMap ());

		if (injectedProperty.collectionType () == CollectionType.single) {

			if (targetComponentDefinitions.isEmpty ()) {

				taskLogger.errorFormat (
					"Unable to find %s component of type %s for %s.%s",
					injectedProperty.prototype ()
						? "prototype"
						: "singleton",
					injectedProperty.targetType ().toString (),
					componentDefinition.name (),
					field.getName ());

				return;

			}

			if (targetComponentDefinitions.size () > 1) {

				taskLogger.errorFormat (
					"Found %s ",
					integerToDecimalString (
						targetComponentDefinitions.size ()),
					"candidate components of type %s ",
					injectedProperty.targetType ().toString (),
					"for %s.%s: ",
					componentDefinition.name (),
					field.getName (),
					"%s",
					joinWithCommaAndSpace (
						targetComponentDefinitions.keySet ()));

				return;

			}

		}

		// register dependencies

		if (! injectedProperty.prototype ()) {

			for (
				ComponentDefinition targetComponentDefinition
					: targetComponentDefinitions.values ()
			) {

				if (weak) {

					componentDefinition.weakDependencies ().add (
						targetComponentDefinition.name ());

				} else {

					componentDefinition.strongDependencies ().add (
						targetComponentDefinition.name ());

				}

			}

		}

		// store injected target components

		injectedProperty.targetComponentNames (
			ImmutableList.copyOf (
				targetComponentDefinitions.keySet ()));

		return;

	}

	private
	List <ComponentDefinition> orderByStrongDepedendencies (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <ComponentDefinition> definitions) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"orderByStrongDependencies");

		List <ComponentDefinition> unorderedDefinitions =
			new ArrayList<> (
				definitions);

		List <ComponentDefinition> orderedDefinitions =
			new ArrayList<> ();

		Set <String> orderedDefinitionNames =
			new HashSet<> ();

		while (
			collectionIsNotEmpty (
				unorderedDefinitions)
		) {

			boolean madeProgress = false;

			ListIterator <ComponentDefinition>
				unorderedDefinitionIterator =
					unorderedDefinitions.listIterator ();

			OUTER: while (
				unorderedDefinitionIterator.hasNext ()
			) {

				ComponentDefinition definition =
					unorderedDefinitionIterator.next ();

				for (
					String targetDefinitionName
						: definition.strongDependencies ()
				) {

					if (
						doesNotContain (
							orderedDefinitionNames,
							targetDefinitionName)
					) {
						continue OUTER;
					}

				}

				taskLogger.debugFormat (
					"Ordered component definition %s",
					definition.name ());

				orderedDefinitions.add (
					definition);

				orderedDefinitionNames.add (
					definition.name ());

				unorderedDefinitionIterator.remove ();

				madeProgress = true;

			}

			if (! madeProgress) {

				for (
					ComponentDefinition definition
						: unorderedDefinitions
				) {

					List <String> unresolvedDependencyNames =
						new ArrayList<> (
							Sets.difference (
								definition.strongDependencies (),
								orderedDefinitionNames));

					Collections.sort (
						unresolvedDependencyNames);

					taskLogger.errorFormat (
						"Unable to resolve dependencies for %s (%s)",
						definition.name (),
						joinWithCommaAndSpace (
							unresolvedDependencyNames));

				}

				break;

			}

		}

		return orderedDefinitions;

	}

	// data

	private static final
	DataFromXml dataFromXml =
		new DataFromXmlBuilder ()

		.registerBuilderClasses (
			ComponentsSpec.class,
			ComponentsComponentSpec.class,
			ComponentsValuePropertySpec.class,
			ComponentsReferencePropertySpec.class,
			ComponentsPropertiesPropertySpec.class,
			ComponentPropertyValueSpec.class)

		.build ();

}
