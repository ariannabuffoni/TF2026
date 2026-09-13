%====================================================================================
% sprint1 description   
%====================================================================================
request( loadrequest, loadrequest(IOPORT_STATE) ).
reply( retrylater, retrylater(CAUSE,HOLD_STATE) ).  %%for loadrequest
reply( reject, reject(HOLD_STATE) ).  %%for loadrequest
reply( engaged, engaged(RESERVED_SLOT,HIOLD_STATE) ).  %%for loadrequest
dispatch( pushButton, pushButton(none) ).
dispatch( setOccupied, setOccupied(FLAG) ).
dispatch( updateDisplay, updateDisplay(STATE,HOLD,MSG) ).
%====================================================================================
context(ctxcargoservice, "localhost",  "TCP", "8050").
 qactor( cargoservice, ctxcargoservice, "it.unibo.cargoservice.Cargoservice").
 static(cargoservice).
  qactor( ioport, ctxcargoservice, "it.unibo.ioport.Ioport").
 static(ioport).
