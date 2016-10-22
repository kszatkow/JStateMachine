package org.moomin.statemachine.onoff;

import org.moomin.statemachine.State;

public class OffProxyState extends State {

	public OffProxyState(String string) {}

	@Override
	public void onEntryBehaviour() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doActionBehaviour() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onExitBehaviour() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPassThrough() {
		return false;
	}

	@Override
	public boolean isComposite() {
		return false;
	}

}
