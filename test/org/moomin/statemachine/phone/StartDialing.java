package org.moomin.statemachine.phone;

import org.moomin.statemachine.SimpleState;

public class StartDialing extends SimpleState {

	private ToneDialer toneDialer = new ToneDialer();
	
	public StartDialing(String string) {}

	@Override
	public void onEntryBehaviour() {
		toneDialer.startDialTone();
	}

	@Override
	public void doActionBehaviour() {
		// empty on purpose
	}

	@Override
	public void onExitBehaviour() {
		toneDialer.stopDialTone();
	}

	@Override
	public boolean isPassThrough() {
		return false;
	}

	@Override
	public boolean isComposite() {
		return false;
	}

	public boolean isDialToneOn() {
		return toneDialer.isDialToneOn();
	}
}
