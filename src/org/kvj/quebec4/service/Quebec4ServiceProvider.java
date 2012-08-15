package org.kvj.quebec4.service;

import org.kvj.bravo7.ipc.RemotelyBindableService;

import android.os.Binder;

public class Quebec4ServiceProvider extends
		RemotelyBindableService<Q4Controller, Q4App> {

	public Quebec4ServiceProvider() {
		super(Q4Controller.class);
	}

	@Override
	public Binder getStub() {
		return controller.getService();
	}

}
