package org.moomin.statemachine.onoff;

import org.moomin.statemachine.TransitionEffect;

public class TurnOffEffect implements TransitionEffect {

	private Switch offOnSwitch;
	
	public TurnOffEffect(Switch offOnSwitch) {
		this.offOnSwitch = offOnSwitch;
	}

	@Override
	public void execute() {
		offOnSwitch.turnOff();
	}

}
