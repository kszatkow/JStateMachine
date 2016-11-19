package org.moomin.statemachine;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
   StateMachineTest.class,
   SimpleStateCompletionTransitionTest.class,
   TransitionsStateMachineTest.class,
   JunctionStateStateMachineTest.class,
   ActivatableStateMachineTest.class,
   StateMachineCreationTest.class,
   EventHandlingStateMachineTest.class,
   StateBehaviourStateMachineTest.class
})
public class StateMachineTestSuite {
	// empty on purpose
}
