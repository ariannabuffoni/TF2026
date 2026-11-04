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
with Diagram('sprint3_problemArch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
### see https://renenyffenegger.ch/notes/tools/Graphviz/attributes/label/HTML-like/index
     with Cluster('ctxcargoservice', graph_attr=nodeattr):
          sonar_analyst=Custom('sonar_analyst','./qakicons/symActorWithobjSmall.png')
          cargoservice_analyst=Custom('cargoservice_analyst','./qakicons/symActorWithobjSmall.png')
          cargorobot_analyst=Custom('cargorobot_analyst','./qakicons/symActorWithobjSmall.png')
     sys >> Edge( label='sonarreading', **evattr, decorate='true', fontcolor='darkgreen') >> sonar_analyst
     cargorobot_analyst >> Edge( label='alarm', **eventedgeattr, decorate='true', fontcolor='red') >> sys
diag
