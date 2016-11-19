package org.moomin.statemachine;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.moomin.statemachine.onoff.OffEvent;
import org.moomin.statemachine.onoff.OnEvent;

@RunWith(MockitoJUnitRunner.class)
public class SimpleStateCompletionTransitionTest extends StateMachineTestBase {

	private State offState;
	private State onState;
	private State onProxyState;
	private State offProxyState;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		offState = addState(spy(State.class));
		onState = addState(spy(State.class));
		onProxyState = addState(spy(SimpleState.class));
		offProxyState = addState(spy(SimpleState.class));
		
		addTransition(offState, onProxyState, OnEvent.class);
		addTransition(onState, offProxyState, OffEvent.class);
	}
	
	@Test
	public void simpleStateCompletionTransitionWithoutGuardTest() {
		// off to on through proxy state
		addCompletionTransition(onProxyState, onState);
		
		// on to off through proxy state
		TransitionEffect effectMock = mock(TransitionEffect.class);
		addCompletionTransition(offProxyState, offState, effectMock);
				
		setInitialTransitionAndActivate(offState);
	
		// off -> on
		dispatchThenProcessEventAndCheckActiveState(new OnEvent() , onState);
		
		// on -> off
		verify(effectMock, never()).execute();
		dispatchThenProcessEventAndCheckActiveState(new OffEvent() , offState);
		verify(effectMock).execute();
	}
	
	@Test
	public void simpleStateCompletionTransitionWithGuardTest() {
		// off to on through proxy state
		TransitionGuard onStateGuard =  mock(TransitionGuard.class);
		addCompletionTransition(onProxyState, onState, onStateGuard);
		
		// on to off through proxy state
		TransitionEffect effectMock = mock(TransitionEffect.class);
		TransitionGuard offStateGuard =  mock(TransitionGuard.class);
		addCompletionTransition(offProxyState, offState, offStateGuard, effectMock);
				
		setInitialTransitionAndActivate(offState);
	
		// off -> on unsuccessful - guard evaluates to false by default
		dispatchThenProcessEventAndCheckActiveState(new OnEvent() , onProxyState);
		
		// off -> on
		CompletionEvent onProxyCompletionEvent = new CompletionEvent(onProxyState);
		when(onStateGuard.evaluate(onProxyState, onProxyCompletionEvent)).thenReturn(true);
		dispatchThenProcessEventAndCheckActiveState(onProxyCompletionEvent , onState);
				
		// on -> off unsuccessful - guard evaluates to false by default
		dispatchThenProcessEventAndCheckActiveState(new OffEvent() , offProxyState);
		verify(effectMock, never()).execute();
		
		// on -> off
		CompletionEvent offProxyCompletionEvent = new CompletionEvent(offProxyState);
		when(offStateGuard.evaluate(offProxyState, offProxyCompletionEvent)).thenReturn(true);
		dispatchThenProcessEventAndCheckActiveState(offProxyCompletionEvent , offState);
		verify(effectMock).execute();	
	}
	
}
