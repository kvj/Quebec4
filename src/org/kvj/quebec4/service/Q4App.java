package org.kvj.quebec4.service;

import org.kvj.bravo7.ApplicationContext;

public class Q4App extends ApplicationContext {

	@Override
	protected void init() {
		publishBean(new Q4Controller());
	}

}
