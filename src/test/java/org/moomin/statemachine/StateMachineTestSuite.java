package org.moomin.statemachine;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
   StateMachineTest.class,
   SimpleStateCompletionTransitionTest.class,
   TransitionEffectStateMachineTest.class,
   JunctionStateStateMachineTest.class,
   ActivateDeactivateStateMachineTest.class
})
public class StateMachineTestSuite {
	// empty on purpose
}
