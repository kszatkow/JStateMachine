package org.moomin.statemachine;

public interface RegionOwner extends StateMachinePart {

	// TODO - how about add region for consistency?
	
	void dispatchInternalEvent(Event event);

}
