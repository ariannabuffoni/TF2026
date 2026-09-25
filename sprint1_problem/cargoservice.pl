%====================================================================================
% cargoservice description   
%====================================================================================
request( loadrequest, loadrequest(IOPORT_STATE) ).
reply( retrylater, retrylater(CAUSE,HOLD_STATE) ).  %%for loadrequest
reply( reject, reject(HOLD_STATE) ).  %%for loadrequest
reply( engaged, engaged(RESERVED_SLOT,HOLD_STATE) ).  %%for loadrequest
dispatch( updateHoldDisplay, updateHoldDisplay(STATE,HOLD_STATE,MSG) ).
dispatch( updateWorkingState, updateWorkingState(STATE) ).
dispatch( button_pushed, button_pushed(X) ).
dispatch( hold_status, hold_status(SLOT_STATES) ).
dispatch( working_state, working_state(STATE) ).
%====================================================================================
context(ctxcargoservice, "localhost",  "TCP", "8120").
 qactor( cargoservice, ctxcargoservice, "it.unibo.cargoservice.Cargoservice").
 static(cargoservice).
  qactor( ioport, ctxcargoservice, "it.unibo.ioport.Ioport").
 static(ioport).
