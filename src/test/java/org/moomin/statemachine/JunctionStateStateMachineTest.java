package org.moomin.statemachine;

import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.moomin.statemachine.oddeven.CheckParityJunctionState;
import org.moomin.statemachine.oddeven.EvenNumberCompeltionGuard;
import org.moomin.statemachine.oddeven.EvenNumberGuard;
import org.moomin.statemachine.oddeven.EvenState;
import org.moomin.statemachine.oddeven.FeedNumberEvent;
import org.moomin.statemachine.oddeven.OddNumberCompletionGuard;
import org.moomin.statemachine.oddeven.OddNumberGuard;
import org.moomin.statemachine.oddeven.OddState;
import org.moomin.statemachine.oddeven.ZeroNumberCompletionGuard;
import org.moomin.statemachine.oddeven.ZeroNumberGuard;
import org.moomin.statemachine.oddeven.ZeroState;

public class JunctionStateStateMachineTest extends StateMachineTestBase {

	private static interface EventDispatcher {
		public void dispatchAndAssertCurrentState(Event event, State expectedActiveState);
	}
	
	private class SingleEventDispatcher implements EventDispatcher {
		@Override
		public void dispatchAndAssertCurrentState(Event event, State expectedActiveState) {
			dispatchThenProcessEventAndCheckActiveState(event, expectedActiveState);
		}
	}
	
	private class DoubleEventDispatcher implements EventDispatcher {
		@Override
		public void dispatchAndAssertCurrentState(Event event, State expectedActiveState) {
			dispatchTwiceThenProcessAndCheckActiveState(event, expectedActiveState);
		}
	}
	
	
	private State zeroState;
	private State oddState;
	private State evenState;
	
	private JunctionState checkParityJunctionState;
	
	@Override
	@Before
	public void setUp() {
		super.setUp();
		
		addNonJunctionStates();
	}
	
	private void addNonJunctionStates() {
		zeroState = addState(new ZeroState("Zero"));
		oddState = addState(new OddState("Odd"));
		evenState = addState(new EvenState("Even"));
	}
	
	@Override
	@After
	public void tearDown() {
		super.setUp();
		checkParityJunctionState = null;
	}
	
	
	@Test
	public void junctionStateWithoutElseTransitionTest() {
		checkParityJunctionState = (JunctionState) addState(new NoBehaviourJunctionState("CheckParity"));
		
		addJunctionIncomingTransitions();
		addTransition(checkParityJunctionState, evenState, FeedNumberEvent.class, new EvenNumberGuard());
		addTransition(checkParityJunctionState, oddState, FeedNumberEvent.class, new OddNumberGuard());
		addTransition(checkParityJunctionState, zeroState, FeedNumberEvent.class, new ZeroNumberGuard());
		
		setInitialTransitionAndActivate();
		
		assertMachineWorking(new DoubleEventDispatcher());
	}
	

	@Test
	public void junctionStateWithElseTransitionTest() {
		checkParityJunctionState = (JunctionState) addState(new NoBehaviourJunctionState("CheckParity"));
		
		addJunctionIncomingTransitions();
		addTransition(checkParityJunctionState, evenState, FeedNumberEvent.class, new EvenNumberGuard());
		addTransition(checkParityJunctionState, oddState, FeedNumberEvent.class, new OddNumberGuard());
		TransitionEffect elseTransitionEffectMock = mock(TransitionEffect.class);
		checkParityJunctionState.setElseTrasition(new NoGuardTransition(checkParityJunctionState, zeroState, 
				FeedNumberEvent.class, elseTransitionEffectMock));
		
		setInitialTransitionAndActivate();
		
		verify(elseTransitionEffectMock, never()).execute();
		assertMachineWorking(new DoubleEventDispatcher());
		verify(elseTransitionEffectMock, times(3)).execute();
	}
	
	@Test
	public void junctionStateOutgoingCompletionTransitionsWithoutElseTransitionTest() {
		checkParityJunctionState = (JunctionState) addState(new CheckParityJunctionState("CheckParity"));

		addJunctionIncomingTransitions();
		addTransition(checkParityJunctionState, evenState, CompletionEvent.class, 
						new EvenNumberCompeltionGuard());
		addTransition(checkParityJunctionState, oddState, CompletionEvent.class, 
						new OddNumberCompletionGuard());
		addTransition(checkParityJunctionState, zeroState, CompletionEvent.class, 
						new ZeroNumberCompletionGuard());
		
		setInitialTransitionAndActivate();
		
		assertMachineWorking(new SingleEventDispatcher());
	}
	
	@Test
	public void junctionStateOutgoingCompletionTransitionsWithElseTransitionTest() {
		checkParityJunctionState = (JunctionState) addState(new CheckParityJunctionState("CheckParity"));

		addJunctionIncomingTransitions();			
		addTransition(checkParityJunctionState, evenState, CompletionEvent.class, 
						new EvenNumberCompeltionGuard());
		addTransition(checkParityJunctionState, oddState, CompletionEvent.class, 
						new OddNumberCompletionGuard());
		checkParityJunctionState.setElseTrasition(new NoGuardTransition(checkParityJunctionState, zeroState, CompletionEvent.class));
	
		setInitialTransitionAndActivate();
		
		assertMachineWorking(new SingleEventDispatcher());
	}

	private void addJunctionIncomingTransitions() {
		// various NoGuardTransition constructors used to improve code coverage
		addTransition(new NoGuardTransition(zeroState, checkParityJunctionState, 
				Collections.singletonList(FeedNumberEvent.class)));
		addTransition(new NoGuardTransition(oddState, checkParityJunctionState, 
				Collections.singletonList(FeedNumberEvent.class), 
				mock(TransitionEffect.class)));
		addTransition(new NoGuardTransition(evenState, checkParityJunctionState, FeedNumberEvent.class));
	}
	
	private void setInitialTransitionAndActivate() {
		setInitialTransitionAndActivate(zeroState);
	}

	private void assertMachineWorking(EventDispatcher eventDispatcher) {
		// zero -> zero
		eventDispatcher.dispatchAndAssertCurrentState(new FeedNumberEvent(0), zeroState);
		// zero -> odd
		eventDispatcher.dispatchAndAssertCurrentState(new FeedNumberEvent(3) , oddState);
		// odd -> odd
		eventDispatcher.dispatchAndAssertCurrentState(new FeedNumberEvent(5) , oddState);
		// odd -> zero
		eventDispatcher.dispatchAndAssertCurrentState(new FeedNumberEvent(0) , zeroState);
		// zero -> even
		eventDispatcher.dispatchAndAssertCurrentState(new FeedNumberEvent(2) , evenState);
		// even -> even
		eventDispatcher.dispatchAndAssertCurrentState(new FeedNumberEvent(12) , evenState);
		// even -> zero
		eventDispatcher.dispatchAndAssertCurrentState(new FeedNumberEvent(0) , zeroState);
		// zero -> odd
		eventDispatcher.dispatchAndAssertCurrentState(new FeedNumberEvent(13) , oddState);
		// odd -> even
		eventDispatcher.dispatchAndAssertCurrentState(new FeedNumberEvent(18) , evenState);
		// even -> odd
		eventDispatcher.dispatchAndAssertCurrentState(new FeedNumberEvent(15) , oddState);
	}
}
