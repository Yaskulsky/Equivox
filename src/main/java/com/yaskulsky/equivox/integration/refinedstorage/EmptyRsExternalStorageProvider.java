package com.yaskulsky.equivox.integration.refinedstorage;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.external.ExternalStorageProvider;
import java.util.Collections;
import java.util.Iterator;

final class EmptyRsExternalStorageProvider implements ExternalStorageProvider {

	static final EmptyRsExternalStorageProvider INSTANCE = new EmptyRsExternalStorageProvider();

	private EmptyRsExternalStorageProvider() {
	}

	@Override
	public Iterator<ResourceAmount> iterator() {
		return Collections.emptyIterator();
	}

	@Override
	public long extract(ResourceKey resource, long amount, Action action, Actor actor) {
		return 0;
	}

	@Override
	public long insert(ResourceKey resource, long amount, Action action, Actor actor) {
		return 0;
	}
}
