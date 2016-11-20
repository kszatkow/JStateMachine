package org.moomin.statemachine;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	StateMachineCreationTest.class,
	SimpleStateCompletionTransitionTest.class,
	TransitionsStateMachineTest.class,
	EventHandlingStateMachineTest.class,
	StateBehaviourStateMachineTest.class,
	ActivatableStateMachineTest.class,
	JunctionStateStateMachineTest.class,
	ChoiceStateStateMachineTest.class,
	SimpleCompositeStateStateMachineTest.class,
	ContainingStateMachineTest.class
})
public class StateMachineTestSuite {
	// empty on purpose
}
