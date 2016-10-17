package org.moomin.statemachine.onoff;

import org.moomin.statemachine.TransitionEffect;

public class TurnOnEffect implements TransitionEffect {

	private Switch offOnSwitch;
	
	public TurnOnEffect(Switch offOnSwitch) {
		this.offOnSwitch = offOnSwitch;
	}

	@Override
	public void execute() {
		offOnSwitch.turnOn();
	}
	
}
