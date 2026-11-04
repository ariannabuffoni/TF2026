%====================================================================================
% sprint3_problem description   
%====================================================================================
mqttBroker("192.168.1.8", "1883", "mqttpicoTF26").
event( sonarreading, sonarreading(D) ).
dispatch( sonarcontainer, sonarcontainer(DISTANCE) ).
dispatch( sonarfault, sonarfault(CAUSE) ).
dispatch( sonarrestored, sonarrestored(NONE) ).
dispatch( blinkLed, blinkLed(FLAG) ).
event( alarm, alarm(X) ).
request( loadrequest, loadrequest(IOPORT_STATE) ).
reply( engaged, engaged(RESERVED_SLOT,HOLD_STATE) ).  %%for loadrequest
request( transportContainer, transportContainer(SLOT,TARGETX,TARGETY) ).
reply( transportDone, transportDone(SLOT) ).  %%for transportContainer
request( moverobot, moverobot(TARGETX,TARGETY,STEPTIME) ).
reply( moverobotdone, moverobotok(ARG) ).  %%for moverobot
reply( moverobotfailed, moverobotfailed(PLANDONE,PLANTODO) ).  %%for moverobot
%====================================================================================
context(ctxcargoservice, "localhost",  "TCP", "8120").
 qactor( sonar_analyst, ctxcargoservice, "it.unibo.sonar_analyst.Sonar_analyst").
 static(sonar_analyst).
  qactor( cargoservice_analyst, ctxcargoservice, "it.unibo.cargoservice_analyst.Cargoservice_analyst").
 static(cargoservice_analyst).
  qactor( cargorobot_analyst, ctxcargoservice, "it.unibo.cargorobot_analyst.Cargorobot_analyst").
 static(cargorobot_analyst).
