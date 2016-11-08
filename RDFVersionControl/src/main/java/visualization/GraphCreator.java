package visualization;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;

public class GraphCreator {

	public class Edge
	{
		private final String name;

		public Edge(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	public Graph<String, Edge> graph;

	public GraphCreator(Model model){
		this.graph = new DirectedSparseMultigraph<String, Edge>();
		if(model.size()<100){
			StmtIterator iter = model.listStatements();
			while(iter.hasNext()){
				Statement nextStmt = iter.nextStatement();
				this.graph.addEdge(new Edge(nextStmt.getPredicate().toString()),nextStmt.getSubject().toString(),nextStmt.getObject().toString());
			}
		}else{
			this.graph.addVertex("The graph is too extensive to be displayed");
		}
	}
}
