%====================================================================================
% sprint0 description   
%====================================================================================
event( sonar, distance(D) ).
dispatch( storeContainer, storeContainer(C) ).
dispatch( markingDone, markingDone(C) ).
request( loadrequest, loadrequest(C) ).
reply( retrylater, retrylater(CAUSE) ).  %%for loadrequest
reply( reject, reject(R) ).  %%for loadrequest
reply( engaged, engaged(RESERVED_SLOT) ).  %%for loadrequest
%====================================================================================
context(ctxcargoservice, "localhost",  "TCP", "8120").
 qactor( sonar, ctxcargoservice, "it.unibo.sonar.Sonar").
 static(sonar).
  qactor( marker, ctxcargoservice, "it.unibo.marker.Marker").
 static(marker).
  qactor( cargorobot, ctxcargoservice, "it.unibo.cargorobot.Cargorobot").
 static(cargorobot).
  qactor( cargoservice, ctxcargoservice, "it.unibo.cargoservice.Cargoservice").
 static(cargoservice).
