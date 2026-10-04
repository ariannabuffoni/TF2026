%====================================================================================
% sprint2_project description   
%====================================================================================
request( loadrequest, loadrequest(IOPORT_STATE) ).
reply( retrylater, retrylater(CAUSE,HOLD_STATE) ).  %%for loadrequest
reply( reject, reject(HOLD_STATE) ).  %%for loadrequest
reply( engaged, engaged(RESERVED_SLOT,HOLD_STATE) ).  %%for loadrequest
dispatch( updateHoldDisplay, updateHoldDisplay(STATE,HOLD_STATE,MSG) ).
dispatch( updateWorkingState, updateWorkingState(STATE) ).
dispatch( button_pushed, button_pushed(X) ).
dispatch( containerSensed, containerSensed(VAL) ).
dispatch( outOfService, outOfService(CAUSE) ).
dispatch( led, led(STATE) ).
request( transportContainer, transportContainer(SLOT,TARGETX,TARGETY) ).
reply( transportDone, transportDone(SLOT) ).  %%for transportContainer
reply( transportFailed, transportFailed(CAUSE) ).  %%for transportContainer
request( moverobot, moverobot(TARGETX,TARGETY,STEPTIME) ).
reply( moverobotdone, moverobotok(ARG) ).  %%for moverobot
reply( moverobotfailed, moverobotfailed(PLANDONE,PLANTODO) ).  %%for moverobot
request( tuneAtHome, tuneAtHome(X) ). %reposition in home X don't care
reply( tuneDone, tuneDone(X) ).  %%for tuneAtHome
dispatch( setrobotstate, setpos(X,Y,D) ). %set robot position to (X,Y) direction D=up|down|left|right
dispatch( setplanbuildelay, value(V) ). %parameter = V >= 0
%====================================================================================
context(ctxcargoservice, "localhost",  "TCP", "8120").
context(ctxrobotsmart, "127.0.0.1",  "TCP", "8020").
 qactor( robotsmart, ctxrobotsmart, "external").
  qactor( cargorobot, ctxcargoservice, "it.unibo.cargorobot.Cargorobot").
 static(cargorobot).
  qactor( cargoservice, ctxcargoservice, "it.unibo.cargoservice.Cargoservice").
 static(cargoservice).
  qactor( ioport, ctxcargoservice, "it.unibo.ioport.Ioport").
 static(ioport).
