package org.moomin.statemachine;

public interface RegionOwner extends StateMachinePart {

	void addRegion(Region region);
	
	void dispatchInternalEvent(Event event);

}
