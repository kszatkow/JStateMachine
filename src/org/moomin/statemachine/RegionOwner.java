package org.moomin.statemachine;

public interface RegionOwner extends StateMachinePart {

	void dispatchInternalEvent(Event event);

}
