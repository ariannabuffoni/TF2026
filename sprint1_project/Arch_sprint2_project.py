### conda install diagrams
from diagrams import Cluster, Diagram, Edge
from diagrams.custom import Custom
import os
os.environ['PATH'] += os.pathsep + 'C:/Program Files/Graphviz/bin/'

graphattr = {     #https://www.graphviz.org/doc/info/attrs.html
    'fontsize': '22',
}

nodeattr = {   
    'fontsize': '22',
    'bgcolor': 'lightyellow'
}

eventedgeattr = {
    'color': 'red',
    'style': 'dotted'
}
evattr = {
    'color': 'darkgreen',
    'style': 'dotted'
}
with Diagram('sprint2_projectArch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
### see https://renenyffenegger.ch/notes/tools/Graphviz/attributes/label/HTML-like/index
     with Cluster('ctxcargoservice', graph_attr=nodeattr):
          cargorobot=Custom('cargorobot','./qakicons/symActorWithobjSmall.png')
          cargoservice=Custom('cargoservice','./qakicons/symActorWithobjSmall.png')
          ioport=Custom('ioport','./qakicons/symActorWithobjSmall.png')
     with Cluster('ctxrobotsmart', graph_attr=nodeattr):
          robotsmart=Custom('robotsmart(ext)','./qakicons/externalQActor.png')
     ioport >> Edge(color='magenta', style='solid', decorate='true', label='<loadrequest<font color="darkgreen"> retrylater reject engaged</font> &nbsp; >',  fontcolor='magenta') >> cargoservice
     cargoservice >> Edge(color='magenta', style='solid', decorate='true', label='<transportContainer<font color="darkgreen"> transportDone transportFailed</font> &nbsp; >',  fontcolor='magenta') >> cargorobot
     cargorobot >> Edge(color='magenta', style='solid', decorate='true', label='<moverobot<font color="darkgreen"> moverobotdone moverobotfailed</font> &nbsp; tuneAtHome<font color="darkgreen"> tuneDone</font> &nbsp; >',  fontcolor='magenta') >> robotsmart
     ioport >> Edge(color='blue', style='solid',  decorate='true', label='<containerSensed &nbsp; outOfService &nbsp; >',  fontcolor='blue') >> cargoservice
     cargoservice >> Edge(color='blue', style='solid',  decorate='true', label='<updateWorkingState &nbsp; led &nbsp; updateHoldDisplay &nbsp; >',  fontcolor='blue') >> ioport
     cargorobot >> Edge(color='blue', style='solid',  decorate='true', label='<setrobotstate &nbsp; setplanbuildelay &nbsp; >',  fontcolor='blue') >> robotsmart
diag
