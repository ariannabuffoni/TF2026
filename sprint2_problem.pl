%====================================================================================
% sprint2_problem description   
%====================================================================================
request( loadrequest, loadrequest(IOPORT_STATE) ).
reply( retrylater, retrylater(CAUSE,HOLD_STATE) ).  %%for loadrequest
reply( reject, reject(HOLD_STATE) ).  %%for loadrequest
reply( engaged, engaged(RESERVED_SLOT,HOLD_STATE) ).  %%for loadrequest
dispatch( button_pushed, button_pushed(X) ).
dispatch( containerSensed, containerSensed(X) ).
dispatch( updateHoldDisplay, updateHoldDisplay(STATE,HOLD_STATE,MSG) ).
request( transportContainer, transportContainer(SLOT,TARGETX,TARGETY) ).
reply( transportDone, transportDone(SLOT) ).  %%for transportContainer
reply( transportFailed, transportFailed(CAUSE) ).  %%for transportContainer
request( moverobot, moverobot(TARGETX,TARGETY,STEPTIME) ).
reply( moverobotdone, moverobotok(ARG) ).  %%for moverobot
reply( moverobotfailed, moverobotfailed(PLANDONE,PLANTODO) ).  %%for moverobot
%====================================================================================
context(ctxcargoservice, "localhost",  "TCP", "8120").
 qactor( robotsmart_mock, ctxcargoservice, "it.unibo.robotsmart_mock.Robotsmart_mock").
 static(robotsmart_mock).
  qactor( cargorobot, ctxcargoservice, "it.unibo.cargorobot.Cargorobot").
 static(cargorobot).
  qactor( cargoservice_ridotto, ctxcargoservice, "it.unibo.cargoservice_ridotto.Cargoservice_ridotto").
 static(cargoservice_ridotto).
