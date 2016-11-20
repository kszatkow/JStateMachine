package org.moomin.statemachine;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.moomin.statemachine.phone.DigitEvent;
import org.moomin.statemachine.phone.FinishDialingEvent;
import org.moomin.statemachine.phone.HangUpEvent;
import org.moomin.statemachine.phone.LiftReceiverEvent;

@RunWith(MockitoJUnitRunner.class)
public class ContainingStateMachineTest extends StateMachineTestBase {
	
	@Test
	public void containingStateMachineForStateMachineWithSimpleCompositeStateTest() {
		State phoneIdleState = addState(mock(State.class));
		SimpleCompositeState dialingState = addSimpleCompositeState(mock(SimpleCompositeState.class));
		State connectingState = addState(mock(State.class));
		
		Transition phoneIdleToDialingTransition = addTransition(phoneIdleState, dialingState, LiftReceiverEvent.class);
		CompletionTransition dialingToConnectionTransition = addCompletionTransition(dialingState, connectingState);
		Transition connectingToPhoneIdleTransition = addTransition(connectingState, phoneIdleState, HangUpEvent.class);
		
		Region dialingStateRegion = new RegionStateMachine(dialingState);
		dialingState.addRegion(dialingStateRegion);
		State startDialingSubstate = addSubstate(dialingStateRegion, mock(State.class));
		State partialDialSubstate = addSubstate(dialingStateRegion, mock(State.class));
		State dialingFinishedSubstate = addSubstate(dialingStateRegion, mock(State.class));
		
		PrimitiveTransition initialDialingTransition = new PrimitiveTransition(startDialingSubstate, mock(TransitionEffect.class));
		CompletionTransition startDialingToPartialDialTransition = new CompletionTransition(startDialingSubstate, partialDialSubstate);
		dialingStateRegion.addTransition(startDialingToPartialDialTransition);
		Transition partialDialInternalTransition = new Transition(partialDialSubstate, partialDialSubstate, DigitEvent.class);
		dialingStateRegion.addTransition(partialDialInternalTransition);
		Transition partialDialToDialingFinishedTransition = new Transition(partialDialSubstate, dialingFinishedSubstate, FinishDialingEvent.class);
		dialingStateRegion.addTransition(partialDialToDialingFinishedTransition);
		
		List<StateMachinePart> stateMachineParts = new ArrayList<>();
		stateMachineParts.add(phoneIdleState);
		stateMachineParts.add(dialingState);
		stateMachineParts.add(connectingState);
		stateMachineParts.add(phoneIdleToDialingTransition);
		stateMachineParts.add(dialingToConnectionTransition);
		stateMachineParts.add(connectingToPhoneIdleTransition);
		stateMachineParts.add(startDialingSubstate);
		stateMachineParts.add(partialDialSubstate);
		stateMachineParts.add(dialingFinishedSubstate);
		stateMachineParts.add(initialDialingTransition);
		stateMachineParts.add(startDialingToPartialDialTransition);
		stateMachineParts.add(partialDialInternalTransition);
		stateMachineParts.add(partialDialToDialingFinishedTransition);
		stateMachineParts.add(stateMachineRegion);
		stateMachineParts.add(dialingStateRegion);
		
		for(StateMachinePart part : stateMachineParts) {
			assertSame(stateMachine, part.containingStateMachine());
		}
	}
	
}
