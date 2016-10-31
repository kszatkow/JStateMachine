package org.moomin.statemachine;

public interface Region extends StateMachinePart, StateOwner, 
	TransitionOwner, Activatable, EventConsumer {

}