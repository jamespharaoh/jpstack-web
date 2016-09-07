package wbs.framework.component.manager;

import static wbs.framework.utils.etc.CollectionUtils.collectionIsEmpty;
import static wbs.framework.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.framework.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.framework.utils.etc.IterableUtils.iterableMapToList;
import static wbs.framework.utils.etc.MapUtils.mapIsNotEmpty;
import static wbs.framework.utils.etc.MapUtils.mapItemForKey;
import static wbs.framework.utils.etc.Misc.doesNotContain;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.framework.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalOf;
import static wbs.framework.utils.etc.StringUtils.joinWithCommaAndSpace;
import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringNotEqualSafe;
import static wbs.framework.utils.etc.TypeUtils.classInstantiate;
import static wbs.framework.utils.etc.TypeUtils.isNotSubclassOf;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.activitymanager.ActiveTask;
import wbs.framework.activitymanager.ActivityManager;
import wbs.framework.activitymanager.RuntimeExceptionWithTask;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.component.registry.ComponentRegistry;
import wbs.framework.component.registry.InjectedProperty;
import wbs.framework.component.registry.InjectedProperty.CollectionType;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.component.tools.EasyReadWriteLock;
import wbs.framework.component.tools.EasyReadWriteLock.HeldLock;
import wbs.framework.component.tools.NoSuchComponentException;
import wbs.framework.utils.etc.BeanLogic;

/**
 * My not-quite-drop-in replacement for spring's ApplicationContext. This
 * provides all of the feat and
 * gives much more helpful error messages.
 *
 * It also in work out dependencies
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
@SingletonComponent ("applicationContext")
public
class ComponentManagerImplementation
	implements ComponentManager {

	// properties

	@Getter @Setter
	ComponentRegistry registry;

	@Getter @Setter
	ActivityManager activityManager;

	// state

	EasyReadWriteLock lock =
		EasyReadWriteLock.instantiate ();

	State state =
		State.creation;

	Map <String, Object> singletonComponents =
		new HashMap<> ();

	Set <String> singletonComponentsInCreation =
		new LinkedHashSet<> ();

	Set <String> singletonComponentsFailed =
		new HashSet<> ();

	Map <Object, ComponentMetaData> componentMetaDatas =
		new MapMaker ()
			.weakKeys ()
			.makeMap ();

	Map <String, List <Injection>> pendingInjectionsByDependencyName =
		new HashMap<> ();

	// public implementation

	@Override
	public
	List <String> requestComponentNames () {

		return registry.requestComponentNames ();

	}

	@Override
	public <ComponentType>
	Optional <ComponentType> getComponent (
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass) {

		@Cleanup
		HeldLock heldLock =
			lock.read ();

		Optional <ComponentDefinition> componentDefinitionOptional =
			registry.byName (
				componentName);

		if (
			optionalIsNotPresent (
				componentDefinitionOptional)
		) {

			return optionalAbsent ();

		}

		ComponentDefinition componentDefinition =
			optionalGetRequired (
				componentDefinitionOptional);

		return optionalOf (
			componentClass.cast (
				getComponent (
					componentDefinition,
					true)));

	}

	@Override
	public <ComponentType>
	ComponentType getComponentRequired (
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass) {

		@Cleanup
		HeldLock heldLock =
			lock.read ();

		Optional <ComponentDefinition> componentDefinitionOptional =
			registry.byName (
				componentName);

		if (
			optionalIsNotPresent (
				componentDefinitionOptional)
		) {

			throw new NoSuchComponentException (
				stringFormat (
					"Component definition with name %s does not exist",
					componentName));

		}

		ComponentDefinition componentDefinition =
			optionalGetRequired (
				componentDefinitionOptional);

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

		Optional <ComponentDefinition> componentDefinitionOptional =
			registry.byName (
				componentName);

		if (
			optionalIsNotPresent (
				componentDefinitionOptional)
		) {
			return orElse.get ();
		}

		ComponentDefinition componentDefinition =
			optionalGetRequired (
				componentDefinitionOptional);

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

		Optional <ComponentDefinition> componentDefinitionOptional =
			registry.byName (
				componentName);

		if (
			optionalIsNotPresent (
				componentDefinitionOptional)
		) {

			throw new NoSuchComponentException (
				stringFormat (
					"Component definition with name %s does not exist",
					componentName));

		}

		ComponentDefinition componentDefinition =
			optionalGetRequired (
				componentDefinitionOptional);

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

		return registry.all ().stream ()

			.filter (
				componentDefinition ->
					stringEqualSafe (
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

	private
	Object getComponent (
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Boolean initialize) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		if (
			stringEqualSafe (
				componentDefinition.scope (),
				"prototype")
		) {

			return instantiateComponent (
				componentDefinition,
				initialize);

		} else if (
			stringEqualSafe (
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

				throw new RuntimeExceptionWithTask (
					activityManager.currentTask (),
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

				throw new RuntimeExceptionWithTask (
					activityManager.currentTask (),
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

				if (component == null) {

					singletonComponentsFailed.add (
						componentDefinition.name ());

				}

			}

			return component;

		} else {

			throw new RuntimeExceptionWithTask (
				activityManager.currentTask (),
				stringFormat (
					"Unrecognised scope %s for component %s",
					componentDefinition.scope (),
					componentDefinition.name ()));

		}

	}

	private
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

		Object protoComponent =
			classInstantiate (
				ifNull (
					componentDefinition.factoryClass (),
					componentDefinition.componentClass ()));

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

				throw new RuntimeExceptionWithTask (
					activityManager.currentTask (),
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

			newComponentMetaData.component =
				new WeakReference <Object> (
					component);

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
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Object component) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		for (
			InjectedProperty injectedProperty
				: componentDefinition.injectedProperties ()
		) {

			injectProperty (
				componentDefinition,
				component,
				injectedProperty);

		}

	}

	private static
	class Injection {

		Object component;

		InjectedProperty injectedProperty;

		List <ComponentDefinition> targetComponents;

		Function <Provider <?>, Object> transformer;

		Function <List <Pair <ComponentDefinition, Object>>, Object> aggregator;

		Set <String> missingComponents;

	}

	private
	void injectProperty (
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Object component,
			@NonNull InjectedProperty injectedProperty) {

		log.debug (
			stringFormat (
				"Setting injected property %s.%s",
				componentDefinition.name (),
				injectedProperty.fieldName ()));

		Injection injection =
				new Injection ();

		injection.component =
			component;

		injection.injectedProperty =
			injectedProperty;

		injection.targetComponents =
			iterableMapToList (
				registry::byNameRequired,
				injectedProperty.targetComponentNames ());

		// define transformer

		if (injectedProperty.provider ()) {

			injection.transformer =
				provider -> provider;

		} else {

			injection.transformer =
				provider -> provider.get ();

		}

		// define aggregator

		if (
			enumEqualSafe (
				injectedProperty.collectionType (),
				CollectionType.componentClassMap)
		) {

			injection.aggregator =
				targetComponents -> {

				Map <Class <?>, Object> componentClassMap =
					new LinkedHashMap<> ();

				for (
					Pair <ComponentDefinition, Object> pair
						: targetComponents
				) {

					componentClassMap.put (
						pair.getLeft ().componentClass (),
						pair.getRight ());

				}

				return componentClassMap;

			};

		} else if (
			enumEqualSafe (
				injectedProperty.collectionType (),
				CollectionType.componentNameMap)
		) {

			injection.aggregator =
				targetComponents -> {

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

				return componentNameMap;

			};

		} else if (
			enumEqualSafe (
				injectedProperty.collectionType (),
				CollectionType.list)
		) {

			injection.aggregator =
				targetComponents -> {

				List <Object> componentsList =
					new ArrayList <> ();

				for (
					Pair <ComponentDefinition, Object> pair
						: targetComponents
				) {

					componentsList.add (
						pair.getRight ());

				}

				return componentsList;

			};

		} else if (
			enumEqualSafe (
				injectedProperty.collectionType (),
				CollectionType.single)
		) {

			injection.aggregator =
				targetComponents -> {

				if (targetComponents.size () != 1) {

					throw new RuntimeExceptionWithTask (
						activityManager.currentTask (),
						stringFormat (
							"Trying to inject %s components into a single field %s.%s",
							targetComponents.size (),
							componentDefinition.name (),
							injectedProperty.fieldName ()));

				}

				return targetComponents.get (0).getRight ();

			};

		} else {

			throw new RuntimeExceptionWithTask (
				activityManager.currentTask ());

		}

		if (injectedProperty.weak ()) {

			injection.missingComponents =
				injection.targetComponents.stream ()

				.filter (
					definition ->
						doesNotContain (
							singletonComponents.keySet (),
							definition.name ()))

				.map (
					definition ->
						definition.name ())

				.collect (
					Collectors.toSet ());

		} else {

			injection.missingComponents =
				ImmutableSet.of ();

		}

		if (
			collectionIsEmpty (
				injection.missingComponents)
		) {

			performInjection (
				injection);

		} else {

			for (
				String missingComponentName
					: injection.missingComponents
			) {

				List <Injection> injectionsByDependency =
					pendingInjectionsByDependencyName.computeIfAbsent (
						missingComponentName,
						name -> new ArrayList<> ());

				injectionsByDependency.add (
					injection);

			}

		}

	}

	private
	void performInjection (
			@NonNull Injection injection) {

		List <Pair <ComponentDefinition, Object>> unaggregatedValues =
			iterableMapToList (
				targetComponentDefinition ->
					Pair.of (
						targetComponentDefinition,
						injection.transformer.apply (
							getComponentProvider (
								targetComponentDefinition,
								injection.injectedProperty.initialized ()))),
				injection.targetComponents);

		Object aggregatedValue =
			injection.aggregator.apply (
				unaggregatedValues);

		try {

			Field field =
				injection.injectedProperty
					.fieldDeclaringClass ()
					.getDeclaredField (
						injection.injectedProperty.fieldName ());

			field.setAccessible (
				true);

			field.set (
				injection.component,
				aggregatedValue);

		} catch (NoSuchFieldException noSuchFieldException) {

			throw new RuntimeException (
				noSuchFieldException);

		} catch (IllegalAccessException illegalAccessException) {

			throw new RuntimeException (
				illegalAccessException);

		}

	}

	public
	ComponentManager init () {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		if (
			enumNotEqualSafe (
				state,
				State.creation)
		) {
			throw new IllegalStateException ();
		}

		state =
			State.initialization;

		// instantiate singletons

		for (
			ComponentDefinition componentDefinition
				: registry.all ()
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

			// fill in weak links as we go

			Optional <List <Injection>> pendingInjectionsOptional =
				mapItemForKey (
					pendingInjectionsByDependencyName,
					componentDefinition.name ());

			if (
				optionalIsPresent (
					pendingInjectionsOptional)
			) {

				ListIterator <Injection> pendingInjectionIterator =
					pendingInjectionsOptional.get ().listIterator ();

				while (pendingInjectionIterator.hasNext ()) {

					Injection pendingInjection =
						pendingInjectionIterator.next ();

					pendingInjection.missingComponents.remove (
						componentDefinition.name ());

					if (
						collectionIsEmpty (
							pendingInjection.missingComponents)
					) {

						performInjection (
							pendingInjection);

						pendingInjectionIterator.remove ();

					}

				}

			}

		}

		// check we filled all weak dependencies

		if (
			mapIsNotEmpty (
				pendingInjectionsByDependencyName)
		) {
			throw new RuntimeException ();
		}

		// return

		state =
			State.running;

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

	private static
	enum State {
		creation,
		initialization,
		running,
		closing,
		closed;
	}

	public static
	class ComponentMetaData {
		ComponentDefinition definition;
		WeakReference <Object> component;
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
