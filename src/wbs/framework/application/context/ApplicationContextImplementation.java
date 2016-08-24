package wbs.framework.application.context;

import static wbs.framework.utils.etc.CollectionUtils.iterableCount;
import static wbs.framework.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.framework.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.NumberUtils.equalToZero;
import static wbs.framework.utils.etc.NumberUtils.moreThanOne;
import static wbs.framework.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.framework.utils.etc.OptionalUtils.presentInstances;
import static wbs.framework.utils.etc.StringUtils.joinWithCommaAndSpace;
import static wbs.framework.utils.etc.StringUtils.joinWithSeparator;
import static wbs.framework.utils.etc.StringUtils.nullIfEmptyString;
import static wbs.framework.utils.etc.StringUtils.stringEqual;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringNotEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringNotInSafe;
import static wbs.framework.utils.etc.TypeUtils.classNotInSafe;
import static wbs.framework.utils.etc.TypeUtils.isNotSubclassOf;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import wbs.framework.activitymanager.ActiveTask;
import wbs.framework.activitymanager.ActivityManager;
import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.application.annotations.UninitializedDependency;
import wbs.framework.application.context.EasyReadWriteLock.HeldLock;
import wbs.framework.application.context.InjectedProperty.CollectionType;
import wbs.framework.application.xml.ComponentPropertyValueSpec;
import wbs.framework.application.xml.ComponentsComponentSpec;
import wbs.framework.application.xml.ComponentsPropertiesPropertySpec;
import wbs.framework.application.xml.ComponentsReferencePropertySpec;
import wbs.framework.application.xml.ComponentsSpec;
import wbs.framework.application.xml.ComponentsValuePropertySpec;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.data.tools.DataToXml;
import wbs.framework.utils.etc.BeanLogic;

/**
 * My not-quite-drop-in replacement for spring's ApplicationContext. This
 * provides all of the features we need, misses out what we don't need, and
 * gives much more helpful error messages.
 *
 * It also is much more deterministic, which means it can work out dependencies
 * at runtime, which is extremely helpful. It will also dump out its bean
 * definitions in XML format to help diagnostics.
 *
 * TODO output ordering, indexes in xml
 * TODO split this into registry and runtime
 * TODO handle qualifiers better
 * TODO handle request scope more elegantly, other scopes
 * TODO cache fields, methods, etc for speed, runtime code gen
 */
@Accessors (fluent = true)
@Log4j
public
class ApplicationContextImplementation 
	implements ApplicationContext {

	@Getter @Setter
	String outputPath;

	@Getter @Setter
	ActivityManager activityManager;

	List <ComponentDefinition> componentDefinitions =
		new ArrayList <> ();

	Map <String, ComponentDefinition> componentDefinitionsByName =
		new HashMap <> ();

	Map <Class <?>, Map <String, ComponentDefinition>>
	componentDefinitionsByClass =
		new HashMap <> ();

	Map <Class <?>, Map <String, ComponentDefinition>>
	singletonComponentDefinitionsByClass =
		new HashMap <> ();

	Map <Class <?>, Map <String,ComponentDefinition>>
	prototypeComponentDefinitionsByClass =
		new HashMap<> ();

	Map <Annotation, List <ComponentDefinition>>
	componentDefinitionsByQualifier =
		new HashMap<> ();

	Map <Annotation, List <ComponentDefinition>>
	singletonComponentDefinitionsByQualifier =
		new HashMap<> ();

	Map <Annotation, List <ComponentDefinition>>
	prototypeComponentDefinitionsByQualifier =
		new HashMap <Annotation, List <ComponentDefinition>> ();

	Map <String, Object> singletonComponents =
		new HashMap <> ();

	Set <String> singletonComponentsInCreation =
		new LinkedHashSet <> ();

	Set <String> singletonComponentsFailed =
		new HashSet <> ();

	Map <Object, ComponentMetaData> componentMetaDatas =
		new MapMaker ()
			.weakKeys ()
			.makeMap ();

	// TODO not pretty
	@Getter
	List <String> requestComponentNames =
		new ArrayList<> ();

	EasyReadWriteLock lock =
		EasyReadWriteLock.instantiate ();

	public
	Optional <ComponentDefinition> getComponentDefinition (
			@NonNull String componentName) {

		return optionalFromNullable (
			componentDefinitionsByName.get (
				componentName));

	}

	@Override
	public <ComponentType>
	Optional <ComponentType> getComponent (
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass) {

		@Cleanup
		HeldLock heldLock =
			lock.read ();

		ComponentDefinition componentDefinition =
			componentDefinitionsByName.get (
				componentName);

		if (
			isNotNull (
				componentDefinition)
		) {

			return Optional.of (
				componentClass.cast (
					getComponent (
						componentDefinition,
						true)));

		} else {

			return Optional.absent ();

		}

	}

	@Override
	public <ComponentType>
	ComponentType getComponentRequired (
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass) {

		@Cleanup
		HeldLock heldLock =
			lock.read ();

		ComponentDefinition componentDefinition =
			componentDefinitionsByName.get (
				componentName);

		if (componentDefinition == null) {

			throw new NoSuchComponentException (
				stringFormat (
					"Component definition with name %s does not exist",
					componentName));

		}

		return componentClass.cast (
			getComponent (
				componentDefinition,
				true));

	}

	@Override
	public <ComponentType>
	ComponentType getComponentOrElse (
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass,
			@NonNull Supplier <ComponentType> orElse) {

		@Cleanup
		HeldLock heldLock =
			lock.read ();

		ComponentDefinition componentDefinition =
			componentDefinitionsByName.get (
				componentName);

		if (
			isNull (
				componentDefinition)
		) {
			return orElse.get ();
		}

		return componentClass.cast (
			getComponent (
				componentDefinition,
				true));

	}

	@Override
	public <ComponentType>
	Provider <ComponentType> getComponentProviderRequired (
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		ComponentDefinition componentDefinition =
			componentDefinitionsByName.get (
				componentName);

		if (componentDefinition == null) {

			throw new NoSuchComponentException (
				stringFormat (
					"Component definition with name %s does not exist",
					componentName));

		}

		if (
			isNotSubclassOf (
				componentClass,
				componentDefinition.componentClass ())
		) {

			throw new NoSuchComponentException (
				stringFormat (
					"Component definition with name %s is of type %s instead of %s",
					componentName,
					componentDefinition.componentClass ().getName (),
					componentClass.getName ()));

		}

		@SuppressWarnings ("unchecked")
		Provider <ComponentType> componentProvider =
			(Provider <ComponentType>)
			getComponentProvider (
				componentDefinition);

		return componentProvider;

	}

	public
	Map <String, Object> getAllSingletonComponents () {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		return componentDefinitions.stream ()

			.filter (
				componentDefinition ->
					stringEqual (
						componentDefinition.scope (),
						"singleton"))

			.collect (
				Collectors.toMap (
					ComponentDefinition::name,
					componentDefinition ->
						getComponent (
						componentDefinition,
						true)));

	}

	public
	List <ComponentDefinition> getComponentDefinitionsWithAnnotation (
			@NonNull Class <? extends Annotation> annotationClass) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		ImmutableList.Builder <ComponentDefinition>
			componentDefinitionsWithAnnotationBuilder =
				ImmutableList.builder ();

		for (
			ComponentDefinition componentDefinition
				: componentDefinitions
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

	private
	Object getComponent (
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Boolean initialize) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		if (
			stringEqual (
				componentDefinition.scope (),
				"prototype")
		) {

			return instantiateComponent (
				componentDefinition,
				initialize);

		} else if (
			stringEqual (
				componentDefinition.scope (),
				"singleton")
		) {

			if (! initialize) {
				throw new IllegalArgumentException ();
			}

			Object component =
				singletonComponents.get (
					componentDefinition.name ());

			if (component != null)
				return component;

			if (
				singletonComponentsInCreation.contains (
					componentDefinition.name ())
			) {

				throw new RuntimeException (
					stringFormat (
						"Singleton component %s already in creation (%s)",
						componentDefinition.name (),
						joinWithCommaAndSpace (
							singletonComponentsInCreation)));

			}

			if (
				singletonComponentsFailed.contains (
					componentDefinition.name ())
			) {

				throw new RuntimeException (
					stringFormat (
						"Singleton component %s already failed",
						componentDefinition.name ()));

			}

			singletonComponentsInCreation.add (
				componentDefinition.name ());

			try {

				component =
					instantiateComponent (
						componentDefinition,
						true);

				singletonComponents.put (
					componentDefinition.name (),
					component);

			} finally {

				singletonComponentsInCreation.remove (
					componentDefinition.name ());

				if (component == null)
					singletonComponentsFailed.add (
						componentDefinition.name ());

			}

			return component;

		} else {

			throw new RuntimeException (
				stringFormat (
					"Unrecognised scope %s for component %s",
					componentDefinition.scope (),
					componentDefinition.name ()));

		}

	}

	@SneakyThrows (Exception.class)
	public
	Object instantiateComponent (
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Boolean initialize) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		@Cleanup
		ActiveTask activeTask =
			activityManager.start (
				"application-context",
				stringFormat (
					"instantiateComponent (%s)",
					componentDefinition.name ()),
				this);

		log.debug (
			stringFormat (
				"Instantiating %s (%s)",
				componentDefinition.name (),
				componentDefinition.scope ()));

		// instantiate

		Class <?> instantiateClass =
			ifNull (
				componentDefinition.factoryClass (),
				componentDefinition.componentClass ());

		Object protoComponent =
			instantiateClass.newInstance ();

		// set properties

		setComponentValueProperties (
			componentDefinition,
			protoComponent);

		setComponentReferenceProperties (
			componentDefinition,
			protoComponent);

		setComponentInjectedProperties (
			componentDefinition,
			protoComponent);

		// call factory

		Object component;
		ComponentMetaData componentMetaData;

		if (
			isNotNull (
				componentDefinition.factoryClass ())
		) {

			ComponentFactory componentFactory =
				(ComponentFactory)
				protoComponent;

			component =
				componentFactory.makeComponent ();

			if (
				isNull (
					component)
			) {

				throw new RuntimeException (
					stringFormat (
						"Factory component returned null for %s",
						componentDefinition.name ()));

			}

			componentMetaData =
				findOrCreateMetaDataForComponent (
					componentDefinition,
					component);

		} else {

			component =
				protoComponent;

			componentMetaData =
				findOrCreateMetaDataForComponent (
					componentDefinition,
					component);

		}

		// initialize

		if (

			initialize

			&& enumEqualSafe (
				componentMetaData.state,
				ComponentState.uninitialized)

		) {

			initializeComponent (
				componentDefinition,
				component,
				componentMetaData);

		}

		// and finish

		log.debug (
			stringFormat (
				"Component %s instantiated successfully",
				componentDefinition.name ()));

		return component;

	}

	@SneakyThrows (Exception.class)
	private
	void initializeComponent (
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Object component,
			@NonNull ComponentMetaData componentMetaData) {

		synchronized (componentMetaData) {

			if (
				enumNotEqualSafe (
					componentMetaData.state,
					ComponentState.uninitialized)
			) {

				throw new IllegalStateException (
					stringFormat (
						"Tried to initialize component %s ",
						componentMetaData.definition.name (),
						"in %s state",
						componentMetaData.state.name ()));

			}
		
			try {

				// run post construct
		
				for (
					Method method
						: component.getClass ().getMethods ()
				) {
	
					PostConstruct postConstructAnnotation =
						method.getAnnotation (
							PostConstruct.class);
	
					if (postConstructAnnotation == null)
						continue;
	
					log.debug (
						stringFormat (
							"Running post construct method %s.%s",
							componentDefinition.name (),
							method.getName ()));
	
					method.invoke (
						component);
	
				}

				componentMetaData.state =
					ComponentState.active;

			} finally {

				if (
					enumNotEqualSafe (
						componentMetaData.state,
						ComponentState.active)
				) {

					componentMetaData.state =
						ComponentState.error;

				}

			}

		}

	}

	private
	ComponentMetaData findOrCreateMetaDataForComponent (
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Object component) {

		// create component info

		return componentMetaDatas.computeIfAbsent (
			component,
			_component -> {

			ComponentMetaData newComponentMetaData =
				new ComponentMetaData ();

			newComponentMetaData.definition =
				componentDefinition;

			newComponentMetaData.state =
				componentDefinition.owned ()
					? ComponentState.uninitialized
					: ComponentState.unmanaged;

			return newComponentMetaData;

		});

	}

	private
	void setComponentValueProperties (
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Object component) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		for (
			Map.Entry <String,Object> valuePropertyEntry
				: componentDefinition.valueProperties ().entrySet ()
		) {

			log.debug (
				stringFormat (
					"Setting value property %s.%s",
					componentDefinition.name (),
					valuePropertyEntry.getKey ()));

			BeanLogic.set (
				component,
				valuePropertyEntry.getKey (),
				valuePropertyEntry.getValue ());

		}

	}

	private
	void setComponentReferenceProperties (
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Object component) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		for (
			Map.Entry <String,String> entry
				: componentDefinition.referenceProperties ().entrySet ()
		) {

			log.debug (
				stringFormat (
					"Setting reference property %s.%s",
					componentDefinition.name (),
					entry.getKey ()));

			Object target =
				getComponentRequired (
					entry.getValue (),
					Object.class);

			BeanLogic.set (
				component,
				entry.getKey (),
				target);

		}

	}

	private
	void setComponentInjectedProperties (
			ComponentDefinition componentDefinition,
			Object component)
		throws Exception {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		for (
			InjectedProperty injectedProperty
				: componentDefinition.injectedProperties ()
		) {

			log.debug (
				stringFormat (
					"Setting injected property %s.%s",
					componentDefinition.name (),
					injectedProperty.fieldName ()));

			// lookup target components

			List <Pair <ComponentDefinition, Object>> targetComponents =
				new ArrayList<> ();

			for (
				String targetComponentDefinitionName
					: injectedProperty.targetComponentNames ()
			) {

				ComponentDefinition targetComponentDefinition =
					componentDefinitionsByName.get (
						targetComponentDefinitionName);

				Object injectValue;

				if (injectedProperty.provider ()) {

					injectValue =
						getComponentProvider (
							targetComponentDefinition,
							injectedProperty.initialized ());

				} else {

					injectValue =
						getComponent (
							targetComponentDefinition,
							injectedProperty.initialized ());

				}

				targetComponents.add (
					Pair.of (
						targetComponentDefinition,
						injectValue));

			}

			// package appropriately

			Object value;

			switch (injectedProperty.collectionType ()) {

			case componentClassMap:

				Map <Class <?>, Object> componentClassMap =
					new LinkedHashMap<> ();

				for (
					Pair <ComponentDefinition,Object> pair
						: targetComponents
				) {

					componentClassMap.put (
						pair.getLeft ().componentClass (),
						pair.getRight ());

				}

				value = componentClassMap;

				break;

			case componentNameMap:

				Map <String, Object> componentNameMap =
					new LinkedHashMap<> ();

				for (
					Pair <ComponentDefinition, Object> pair
						: targetComponents
				) {

					componentNameMap.put (
						pair.getLeft ().name (),
						pair.getRight ());

				}

				value = componentNameMap;

				break;

			case list:

				List<Object> componentsList =
					new ArrayList <> ();

				for (
					Pair <ComponentDefinition, Object> pair
						: targetComponents
				) {

					componentsList.add (
						pair.getRight ());

				}

				value = componentsList;

				break;

			case single:

				if (targetComponents.size () != 1) {

					throw new RuntimeException (
						stringFormat (
							"Trying to inject %s components into a single field %s.%s",
							targetComponents.size (),
							componentDefinition.name (),
							injectedProperty.fieldName ()));

				}

				value =
					targetComponents.get (0).getRight ();

				break;

			default:

				throw new RuntimeException ();

			}

			// and inject it

			Field field =
				injectedProperty
					.fieldDeclaringClass ()
					.getDeclaredField (
						injectedProperty.fieldName ());

			field.setAccessible (
				true);

			field.set (
				component,
				value);

		}

	}

	public
	ApplicationContext registerComponentDefinition (
			@NonNull ComponentDefinition componentDefinition) {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		// sanity check

		if (componentDefinition.name () == null) {

			throw new RuntimeException (
				stringFormat (
					"Component definition has no name"));

		}

		if (componentDefinition.componentClass () == null) {

			throw new RuntimeException (
				stringFormat (
					"Component definition %s has no component class",
					componentDefinition.componentClass ()));

		}

		if (componentDefinition.scope () == null) {

			throw new RuntimeException (
				stringFormat (
					"Copmonent definition %s has no scope",
					componentDefinition.name ()));

		}

		if (componentDefinitionsByName.containsKey (
				componentDefinition.name ())) {

			throw new RuntimeException (
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

			throw new RuntimeException (
				stringFormat (
					"Component definition %s has invalid scope %s",
					componentDefinition.name (),
					componentDefinition.scope ()));

		}

		Class<?> instantiationClass =
			ifNull (
				componentDefinition.factoryClass (),
				componentDefinition.componentClass ());

		// check the class looks ok

		if (! Modifier.isPublic (
				instantiationClass.getModifiers ())) {

			throw new RuntimeException (
				stringFormat (
					"Component definition %s refers to non-public class %s",
					componentDefinition.name (),
					instantiationClass.getName ()));

		}

		if (Modifier.isAbstract (
				instantiationClass.getModifiers ())) {

			throw new RuntimeException (
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

			throw new RuntimeException (
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

			throw new RuntimeException (
				stringFormat (
					"Component definition %s refers to class %s with non-public ",
					componentDefinition.name (),
					instantiationClass.getName (),
					"default constructor"));

		}

		// store component definition

		componentDefinitions.add (
			componentDefinition);

		componentDefinitionsByName.put (
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
				componentDefinitionsByClass,
				componentClasses,
				componentDefinition);

			if (
				stringEqual (
					componentDefinition.scope (),
					"singleton")
			) {

				updateIndexByClass (
					singletonComponentDefinitionsByClass,
					componentClasses,
					componentDefinition);

			}

			if (
				stringEqual (
					componentDefinition.scope (),
					"prototype")
			) {

				updateIndexByClass (
					prototypeComponentDefinitionsByClass,
					componentClasses,
					componentDefinition);

			}

		}

		// index by qualifiers

		if (! componentDefinition.hide ()) {

			for (
				Annotation annotation
					: componentDefinition.componentClass ().getDeclaredAnnotations ()
			) {

				Qualifier qualifierAnnotation =
					annotation
						.annotationType ()
						.getAnnotation (Qualifier.class);

				if (qualifierAnnotation == null)
					continue;

				updateIndexByQualifier (
					componentDefinitionsByQualifier,
					annotation,
					componentDefinition);

				if (
					stringEqual (
						componentDefinition.scope (),
						"singleton")
				) {

					updateIndexByQualifier (
						singletonComponentDefinitionsByQualifier,
						annotation,
						componentDefinition);

				}

				if (
					stringEqual (
						componentDefinition.scope (),
						"prototype")
				) {

					updateIndexByQualifier (
						prototypeComponentDefinitionsByQualifier,
						annotation,
						componentDefinition);

				}

			}

		}

		return this;

	}

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

	public
	ApplicationContext init () {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		int errors = 0;

		// automatic components

		registerUnmanagedSingleton (
			"applicationContext",
			this);

		registerUnmanagedSingleton (
			"activityManager",
			activityManager);

		// work out dependencies

		for (
			ComponentDefinition componentDefinition
				: componentDefinitions
		) {

			errors +=
				initComponentDefinition (
					componentDefinition);

		}

		// check dependencies exist

		for (
			ComponentDefinition componentDefinition
				: componentDefinitions
		) {

			for (
				String dependency
					: componentDefinition.orderedDependencies ()
			) {

				if (
					! componentDefinitionsByName.containsKey (
						dependency)
				) {

					log.error (
						stringFormat (
							"Can't provide dependency %s for %s",
							dependency,
							componentDefinition.name ()));

					errors ++;

				}

			}

		}

		// order component definitions

		List <ComponentDefinition> unorderedComponentDefinitions =
			new ArrayList<> (
				componentDefinitions);

		Map <String, ComponentDefinition> orderedComponentDefinitions =
			new LinkedHashMap <> ();

		while (! unorderedComponentDefinitions.isEmpty ()) {

			boolean madeProgress = false;

			ListIterator <ComponentDefinition> unorderedComponentDefinitionIterator =
				unorderedComponentDefinitions.listIterator ();

			OUTER: while (
				unorderedComponentDefinitionIterator.hasNext ()
			) {

				ComponentDefinition componentDefinition =
					unorderedComponentDefinitionIterator.next ();

				for (
					String targetComponentDefinitionName
						: componentDefinition.orderedDependencies ()
				) {

					if (! componentDefinitionsByName.containsKey (
							targetComponentDefinitionName))
						continue OUTER;

				}

				log.debug (
					stringFormat (
						"Ordered component definition %s",
						componentDefinition.name ()));

				orderedComponentDefinitions.put (
					componentDefinition.name (),
					componentDefinition);

				unorderedComponentDefinitionIterator.remove ();

				madeProgress = true;

			}

			if (! madeProgress) {

				for (
					ComponentDefinition componentDefinition
						: unorderedComponentDefinitions
				) {

					List<String> unresolvedDependencyNames =
						new ArrayList<String> (
							Sets.difference (
								componentDefinition.orderedDependencies (),
								orderedComponentDefinitions.keySet ()));

					Collections.sort (
						unresolvedDependencyNames);

					log.error (
						stringFormat (
							"Unable to resolve dependencies for %s (%s)",
							componentDefinition.name (),
							joinWithSeparator (
								", ",
								unresolvedDependencyNames)));

					errors ++;

				}

				break;

			}

		}

		// output component definitions

		if (outputPath != null) {

			outputComponentDefinitions (
				outputPath);

		}

		// check for errors

		if (errors > 0) {

			throw new RuntimeException (
				stringFormat (
					"Aborting due to %s errors",
					errors));

		}

		// instantiate singletons

		for (
			ComponentDefinition componentDefinition
				: componentDefinitions
		) {

			if (
				stringNotEqualSafe (
					componentDefinition.scope (),
					"singleton")
			) {
				continue;
			}

			getComponentRequired (
				componentDefinition.name (),
				Object.class);

		}

		return this;

	}

	public
	int initComponentDefinition (
			@NonNull ComponentDefinition componentDefinition) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		int errors = 0;

		Class<?> instantiateClass =
			ifNull (
				componentDefinition.factoryClass (),
				componentDefinition.componentClass ());

		componentDefinition.orderedDependencies ().addAll (
			componentDefinition.referenceProperties ().values ());

		for (
			Field field
				: FieldUtils.getAllFields (
					instantiateClass)
		) {

			Inject injectAnnotation =
				field.getAnnotation (
					Inject.class);

			PrototypeDependency prototypeDependencyAnnotation =
				field.getAnnotation (
					PrototypeDependency.class);

			SingletonDependency singletonDependencyAnnotation =
				field.getAnnotation (
					SingletonDependency.class);

			UninitializedDependency uninitializedDependencyAnnotation =
				field.getAnnotation (
					UninitializedDependency.class);

			long numAnnotations =
				iterableCount (
					presentInstances (
						optionalFromNullable (
							injectAnnotation),
						optionalFromNullable (
							prototypeDependencyAnnotation),
						optionalFromNullable (
							singletonDependencyAnnotation),
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
				throw new RuntimeException ();
			}

			Named namedAnnotation =
				field.getAnnotation (
					Named.class);

			if (
				isNotNull (
					namedAnnotation)
			) {

				errors +=
					initInjectedFieldByName (
						componentDefinition,
						namedAnnotation,
						field,
						isNull (
							uninitializedDependencyAnnotation));

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

				if (qualifierAnnotations.size () > 1)
					throw new RuntimeException ();

				InjectedProperty injectedProperty =
					new InjectedProperty ()

					.componentDefinition (
						componentDefinition)

					.fieldDeclaringClass (
						field.getDeclaringClass ())

					.fieldName (
						field.getName ())

					.initialized (
						isNull (
							uninitializedDependencyAnnotation));

				errors +=
					initInjectedPropertyField (
						componentDefinition,
						field,
						injectedProperty);

				if (qualifierAnnotations.size () == 1) {

					errors +=
						initInjectedPropertyTargetByQualifier (
							componentDefinition,
							qualifierAnnotations.get (0),
							injectedProperty);

				} else {

					errors +=
						initInjectedPropertyTargetByClass (
							componentDefinition,
							field,
							injectedProperty);

				}

				componentDefinition.injectedProperties ().add (
					injectedProperty);

			}

		}

		return errors;

	}

	private
	int initInjectedFieldByName (
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Named namedAnnotation,
			@NonNull Field field,
			@NonNull Boolean initialized) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		// TODO merge this

		String targetComponentDefinitionName =
			ifNull (
				nullIfEmptyString (
					namedAnnotation.value ()),
				field.getName ());

		ComponentDefinition targetComponentDefinition =
			componentDefinitionsByName.get (
				targetComponentDefinitionName);

		if (targetComponentDefinition == null) {

			log.error (
				stringFormat (
					"Named component %s does not exist for %s.%s",
					targetComponentDefinitionName,
					componentDefinition.name (),
					field.getName ()));

			return 1;

		}

		componentDefinition.orderedDependencies.add (
			targetComponentDefinition.name ());

		componentDefinition.injectedProperties ().add (
			new InjectedProperty ()

			.componentDefinition (
				componentDefinition)

			.fieldDeclaringClass (
				field.getDeclaringClass ())

			.fieldName (
				field.getName ())

			.provider (
				field.getType () == Provider.class)

			.initialized (
				initialized)

			.targetComponentNames (
				Collections.singletonList (
					targetComponentDefinitionName)));

		return 0;

	}

	private
	int initInjectedPropertyField (
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Field field,
			@NonNull InjectedProperty injectedProperty) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		Type fieldType =
			field.getGenericType ();

		ParameterizedType parameterizedFieldType =
			field.getGenericType () instanceof ParameterizedType
				? (ParameterizedType) field.getGenericType ()
				: null;

		Class<?> fieldClass =
			parameterizedFieldType != null
				? (Class<?>) parameterizedFieldType.getRawType ()
				: (Class<?>) fieldType;

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

				log.error (
					stringFormat (
						"Don't know how to inject map with key type %s for %s.%s",
						keyType.toString (),
						componentDefinition.name (),
						field.getName ()));

				return 1;

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

				log.error (
					stringFormat (
						"No type information for provider %s at %s.%s",
						injectType,
						componentDefinition.name (),
						field.getName ()));

				return 1;

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
			.provider (isProvider)
			.finalType (fieldType)
			.injectType (injectType)
			.targetType (valueType);

		return 0;

	}

	private
	int initInjectedPropertyTargetByClass (
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Field field,
			@NonNull InjectedProperty injectedProperty) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		// lookup target components

		ParameterizedType parameterizedTargetType =
			injectedProperty.targetType () instanceof ParameterizedType
				? (ParameterizedType) injectedProperty.targetType ()
				: null;

		Class<?> targetClass =
			parameterizedTargetType != null
				? (Class<?>) parameterizedTargetType.getRawType ()
				: (Class<?>) injectedProperty.targetType ();

		Map <String, ComponentDefinition> targetComponentDefinitions =
			ifNull (
				injectedProperty.provider ()
					? ifNull (
						prototypeComponentDefinitionsByClass.get (
							targetClass),
						singletonComponentDefinitionsByClass.get (
							targetClass))
					: singletonComponentDefinitionsByClass.get (
						targetClass),
				Collections.emptyMap ());

		if (injectedProperty.collectionType () == CollectionType.single) {

			if (targetComponentDefinitions.isEmpty ()) {

				log.error (
					stringFormat (
						"Unable to find component of type %s for %s.%s",
						injectedProperty.targetType (),
						componentDefinition.name (),
						field.getName ()));

				return 1;

			}

			if (targetComponentDefinitions.size () > 1) {

				log.error (
					stringFormat (
						"Found %s ",
						targetComponentDefinitions.size (),
						"candidate components of type %s ",
						injectedProperty.targetType (),
						"for %s.%s: ",
						componentDefinition.name (),
						field.getName (),
						"%s",
						joinWithSeparator (
							", ",
							targetComponentDefinitions.keySet ())));

				return 1;

			}

		}

		// register dependencies

		if (! injectedProperty.provider ()) {

			for (
				ComponentDefinition targetComponentDefinition
					: targetComponentDefinitions.values ()
			) {

				componentDefinition.orderedDependencies ().add (
					targetComponentDefinition.name ());

			}

		}

		// store injected target components

		injectedProperty.targetComponentNames (
			ImmutableList.copyOf (
				targetComponentDefinitions.keySet ()));

		return 0;

	}

	private
	int initInjectedPropertyTargetByQualifier (
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Annotation qualifier,
			@NonNull InjectedProperty injectedProperty) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		// lookup target components

		List <ComponentDefinition> targetComponentDefinitions =
			ifNull (
				injectedProperty.provider ()
					? ifNull (
						prototypeComponentDefinitionsByQualifier.get (
							qualifier),
						singletonComponentDefinitionsByQualifier.get (
							qualifier))
					: singletonComponentDefinitionsByQualifier.get (
						qualifier),
				Collections.emptyList ());

		if (injectedProperty.collectionType () == CollectionType.single) {

			if (targetComponentDefinitions.isEmpty ()) {

				log.error (
					stringFormat (
						"Unable to find component of type %s for %s.%s",
						injectedProperty.targetType (),
						componentDefinition.name (),
						injectedProperty.fieldName ()));

				return 1;

			}

			if (targetComponentDefinitions.size () > 1) {

				log.error (
					stringFormat (
						"Found %s candidate components of type %s for %s.%s",
						targetComponentDefinitions.size (),
						injectedProperty.targetType (),
						componentDefinition.name (),
						injectedProperty.fieldName ()));

				return 1;

			}

		}

		// register dependencies

		if (! injectedProperty.provider ()) {

			for (
				ComponentDefinition targetComponentDefinition
					: targetComponentDefinitions
			) {

				componentDefinition.orderedDependencies ().add (
					targetComponentDefinition.name ());

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

		return 0;

	}

	public
	void outputComponentDefinitions (
			String outputPath) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		try {

			FileUtils.deleteDirectory (
				new File (outputPath));

			FileUtils.forceMkdir (
				new File (outputPath));

		} catch (IOException exception) {

			log.warn (
				stringFormat (
					"Error deleting contents of %s",
					outputPath),
				exception);

		}

		for (
			ComponentDefinition componentDefinition
				: componentDefinitions
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

			} catch (IOException exception) {

				log.warn (
					stringFormat (
						"Error writing %s",
						outputFile),
					exception);

			}

		}

	}

	public
	ApplicationContext registerUnmanagedSingleton (
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

		registerComponentDefinition (
			componentDefinition);

		if (
			componentMetaDatas.containsKey (
				object)
		) {
			throw new IllegalStateException ();
		}

		ComponentMetaData componentMetaData =
			new ComponentMetaData ();

		componentMetaData.definition =
			componentDefinition;

		componentMetaData.state =
			ComponentState.unmanaged;		

		componentMetaDatas.put (
			object,
			componentMetaData);

		return this;

	}

	public
	ApplicationContext registerXmlClasspath (
			String classpath) {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

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

		ComponentsSpec components =
			(ComponentsSpec)
			dataFromXml.readClasspath (
				classpath);

		components.register (
			this);

		return this;

	}

	public
	ApplicationContext registerXmlFilename (
			String filename) {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

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

		ComponentsSpec componentsSpec =
			(ComponentsSpec)
			dataFromXml.readFilename (
				filename);

		componentsSpec.register (
			this);

		return this;

	}

	@Override
	public
	void close () {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		// TODO

	}

	public
	Provider<?> getComponentProvider (
			@NonNull ComponentDefinition componentDefinition) {

		return getComponentProvider (
			componentDefinition,
			true);

	}

	public
	Provider<?> getComponentProvider (
			final ComponentDefinition componentDefinition,
			final Boolean initialized) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		return new Provider <Object> () {

			@Override
			public
			Object get () {

				return getComponent (
					componentDefinition,
					initialized);

			}

		};

	}

	@SneakyThrows (Exception.class)
	public <ComponentType>
	ComponentType injectDependencies (
			@NonNull ComponentType component) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		ComponentDefinition componentDefinition =
			new ComponentDefinition ()

			.name (
				component.getClass ().getSimpleName ())

			.componentClass (
				component.getClass ());

		int errors = 0;

		errors =
			initComponentDefinition (
				componentDefinition);

		if (errors > 0)
			throw new RuntimeException ();

		setComponentValueProperties (
			componentDefinition,
			component);

		setComponentReferenceProperties (
			componentDefinition,
			component);

		setComponentInjectedProperties (
			componentDefinition,
			component);

		return component;

	}

	public static
	class ComponentMetaData {
		ComponentDefinition definition;
		ComponentState state;
	}

	public static
	enum ComponentState {
		uninitialized,
		active,
		tornDown,
		error,
		unmanaged;
	}

}
