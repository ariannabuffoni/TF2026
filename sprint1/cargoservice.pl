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
dispatch( working_state, working_state(STATE) ).
dispatch( hold_status, hold_status(STATE,HOLD_STATE,MSG) ).
dispatch( outOfService, outOfService(CAUSE) ).
dispatch( containerSensed, containerSensed(VAL) ).
dispatch( led, led(STATE) ).
%====================================================================================
context(ctxcargoservice, "localhost",  "TCP", "8050").
 qactor( ioport, ctxcargoservice, "it.unibo.ioport.Ioport").
 static(ioport).
  qactor( cargoservice, ctxcargoservice, "it.unibo.cargoservice.Cargoservice").
 static(cargoservice).
