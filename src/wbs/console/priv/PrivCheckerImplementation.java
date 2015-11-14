package wbs.console.priv;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.experimental.Accessors;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.ProxiedRequestComponent;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;

@Accessors (fluent = true)
@ProxiedRequestComponent (
	value = "privChecker",
	proxyInterface = PrivChecker.class)
public
class PrivCheckerImplementation
	implements PrivChecker {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	PrivDataLoader privDataLoader;

	// state

	UserPrivData userPrivData;

	// lifecycle

	@PostConstruct
	public
	void init () {

		Integer userId =
			(Integer)
			requestContext.session ("myUserId");

		if (userId == null) {

			userPrivData = null;

			return;

		}

		userPrivData =
			privDataLoader.getUserPrivData (
				userId);

	}

	// implementation

	@Override
	public
	boolean can (
			int privId) {

		return userPrivData.canNormal (
			privId);

	}

	@Override
	public
	boolean can (
			GlobalId parentGlobalId,
			String... privCodes) {

		return userPrivData.canList (
			parentGlobalId,
			Arrays.asList (privCodes));

	}

	@Override
	public
	boolean can (
			Class<? extends Record<?>> parentClass,
			int parentId,
			String... privCodes) {

		int parentTypeId =
			userPrivData.coreGetObjectTypeId (
				parentClass);

		return userPrivData.canList (
			new GlobalId (
				parentTypeId,
				parentId),
			Arrays.asList (privCodes));

	}

	@Override
	public
	boolean can (
			Record<?> parentObject,
			String... privCodes) {

		int parentObjectTypeId =
			userPrivData.coreGetObjectTypeId (
				parentObject.getClass ());

		return userPrivData.canList (
			new GlobalId (
				parentObjectTypeId,
				parentObject.getId ()),
			Arrays.asList (privCodes));

	}

	@Override
	public
	boolean can (
			Map<Object,Collection<String>> map) {

		for (Map.Entry<Object,Collection<String>> ent
				: map.entrySet ()) {

			Object key =
				ent.getKey ();

			Collection<String> privCodes =
				ent.getValue ();

			GlobalId parentObjectId;

			if (key instanceof GlobalId) {

				parentObjectId =
					(GlobalId) key;

			} else if (key instanceof Record) {

				Record<?> parentObject =
					(Record<?>) key;

				int parentObjectTypeId =
					userPrivData.coreGetObjectTypeId (
						parentObject.getClass ());

				parentObjectId =
					new GlobalId (
						parentObjectTypeId,
						parentObject.getId ());

			} else {

				throw new IllegalArgumentException ();

			}

			if (privCodes != null
					&& userPrivData.canList (
						parentObjectId,
						privCodes))
				return true;

			if (privCodes == null
					&& userPrivData.canList (
						parentObjectId,
						Collections.<String>emptyList ()))
				return true;

		}

		return false;

	}

	@Override
	public
	boolean canGrant (
			int privId) {

		Integer managePrivId =
			userPrivData.sharedData.managePrivIds.get (privId);

		return managePrivId != null
			&& userPrivData.canChain (managePrivId);

	}

	@Override
	public
	void refresh () {

		privDataLoader.refresh ();

		init ();

	}

}
