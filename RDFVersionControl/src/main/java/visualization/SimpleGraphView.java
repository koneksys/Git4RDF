/*
 * SimpleGraphView.java
 *
 * Created on March 8, 2007, 7:49 PM; Updated May 29, 2007
 *
 * Copyright March 8, 2007 Grotto Networking
 */

package visualization;

import mainPackage.*;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JApplet;

import org.apache.jena.ext.com.google.common.base.Objects;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.algorithms.shortestpath.MinimumSpanningForest2;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

import java.awt.Dimension;

/**
 *
 * @author Dr. Greg M. Bernstein
 */
public class SimpleGraphView extends JApplet {
	private Graph<String, GraphCreator.Edge> graph;
	
	private Dimension preferredSize = new Dimension(700,740);

	private static final long serialVersionUID = -2023368689258876709L;    

	private AbstractLayout	<String, GraphCreator.Edge>	layout0;
	private AbstractLayout	<String, GraphCreator.Edge> 	layout1;
	private TreeLayout		<String, GraphCreator.Edge> 	layout2;
	private CircleLayout	<String, GraphCreator.Edge> 	layout3;
	private KKLayout		<String, GraphCreator.Edge> 	layout4;
	private FRLayout		<String, GraphCreator.Edge> 	layout5;
	private SpringLayout	<String, GraphCreator.Edge>	layout6;
	private ISOMLayout		<String, GraphCreator.Edge> 	layout7;
	private DAGLayout		<String, GraphCreator.Edge> 	layout8;
	private BalloonLayout	<String, GraphCreator.Edge> 	layout9;

	private VisualizationModel<String, GraphCreator.Edge> vm;

	public VisualizationViewer<String, GraphCreator.Edge> vv;

	/** Creates a new instance of SimpleGraphView */
	public SimpleGraphView(Model model,int layout) {
		// create a simple graph for the demo
		graph = new GraphCreator(model).graph;
		setLayout(model,layout);
		renderViewer();
		setUserInputHandling();
	}

	/** Creates a new instance of SimpleGraphView */
	public SimpleGraphView(Model model,int layout,Dimension size) {
		// create a simple graph for the demo
		graph = new GraphCreator(model).graph;
		preferredSize = size;
		setLayout(model,layout);
		renderViewer();
		setUserInputHandling();
	}

	private void setPositionsForSubordinateVertices(Map<String,Point2D> map, String currentCommit){

		//get position of current commit
		Point2D currentCommitPos = map.get(currentCommit);

		double xCommit = currentCommitPos.getX();//120
		double xModel = xCommit - 130;
		double xDelta;
		double xScript;
		double xStatement;
		double xBaseline = xModel;
		double xDefault;
		double yDelta;
		double yStatement;
		double yBaseline = 50;

		String deltaVertex = null;

		Collection<String> currentCommitPredecessors = graph.getPredecessors(currentCommit);
		Collection<String> currentCommitSuccessors = graph.getSuccessors(currentCommit);
		Collection<String> deltaAdjacentResources = new ArrayList<String>();
		Collection<String> statementAdjacentResources = new ArrayList<String>();
		List<String> nextCommits = new ArrayList<String>();

		//set position of appended commit information vertex
		for(String vertex : currentCommitSuccessors){
			if(vertex.contains("AuthorID: ")){
				Point2D position = new Point2D.Double(xCommit-vertex.length()*6-20,currentCommitPos.getY());
				map.put(vertex, position);
			}
		}

		//set position of appended file vertex
		for(String vertex : currentCommitSuccessors){
			if((vertex.contains("File"))&&(vertex.contains("Model"))){
				Point2D position = new Point2D.Double(xModel,currentCommitPos.getY()+50);
				map.put(vertex, position);
				yBaseline = 100;
			}
		}

		currentCommitSuccessors = graph.getSuccessors(currentCommit);
		//set position of appended baseline vertex
		for(String vertex : currentCommitSuccessors){
			if(vertex.contains("/baseline/")){
				yBaseline = yBaseline + currentCommitPos.getY();
				Point2D position = new Point2D.Double(xBaseline,yBaseline);
				map.put(vertex, position);
			}
		}

		//get preceding commit vertices
		for(String vertex : currentCommitPredecessors){
			if(vertex.contains("/commit/")){
				nextCommits.add(vertex);
			}
		}

		if(nextCommits.size()>0){
			for(String nextCommit : nextCommits){
				Point2D nextCommitPos = map.get(nextCommit);
				//get common delta resource of current and next commit
				Collection<String> nextCommitSuccessors = graph.getSuccessors(nextCommit);
				for(String search1 : currentCommitSuccessors){
					if(search1.contains("/delta/")){
						for(String search2 : nextCommitSuccessors){
							if(search2.contains("/delta/")){
								if(Objects.equal(search1,search2)){
									deltaVertex=search1;
									break;
								}
							}
						}
					}
				}
				if(deltaVertex!=null){
					if(nextCommits.size()==2){
						if(nextCommitPos.getX()<currentCommitPos.getX()){
							xDelta = (currentCommitPos.getX()+nextCommitPos.getX())/2 - 200;
							xScript = xDelta - 220;
							xStatement = xScript;
							xDefault = xStatement - 350;
						}else{
							xDelta = (currentCommitPos.getX()+nextCommitPos.getX())/2 + 150;
							xScript = xDelta + 100;
							xStatement = xScript;
							xDefault = xStatement + 200;
						}
					}else{
						xDelta = (currentCommitPos.getX()+nextCommitPos.getX())/2 + 100;
						xScript = xDelta + 120;
						xStatement = xScript;
						xDefault = xStatement + 250;
					}
					yDelta = currentCommitPos.getY() + 70;
					yStatement = yDelta + 50;

					Point2D position = new Point2D.Double(xDelta,yDelta);
					map.put(deltaVertex, position);

					//get adjacent vertices of delta vertex and position them
					deltaAdjacentResources = graph.getSuccessors(deltaVertex);
					for(String vertex : deltaAdjacentResources){
						if((vertex.contains("File"))&&(vertex.contains("Script"))){
							position = new Point2D.Double(xScript,yDelta - 50);
							map.put(vertex, position);
						}else if((vertex.contains("/statement/"))){
							double yResource = -70;
							position = new Point2D.Double(xStatement,yStatement);
							map.put(vertex, position);
							statementAdjacentResources = graph.getSuccessors(vertex);
							for(String subVertex : statementAdjacentResources){
								position = new Point2D.Double(xDefault,yStatement+yResource);
								map.put(subVertex, position);
								yResource = yResource + 50;
							}
							yStatement = yStatement + yResource;
						}
					}
				}
			}
		}
	}

	private Statement reverseStatement(Statement stmt){
		Model tempModel = ModelFactory.createDefaultModel();
		Statement newStmt = null;
		if(stmt.getObject() instanceof Resource){
			newStmt = tempModel.createStatement(stmt.getObject().asResource(), stmt.getPredicate(), stmt.getSubject());
		}
		return newStmt;
	}

	private Layout<String,GraphCreator.Edge> setPositionsForCommits(Model model){
		Model tempModel = ModelFactory.createDefaultModel();
		StmtIterator iter = model.listStatements();
		while(iter.hasNext()){
			Statement nextStmt = iter.nextStatement();
			if((nextStmt.getSubject().toString().contains("/commit"))&&(nextStmt.getObject().toString().contains("/commit"))){
				nextStmt = reverseStatement(nextStmt);
				tempModel.add(nextStmt);
			}
		}
		Graph<String, GraphCreator.Edge> graph2 = new GraphCreator(tempModel).graph;
		MinimumSpanningForest2<String, GraphCreator.Edge> prim = new MinimumSpanningForest2<String, GraphCreator.Edge>(graph2,new DelegateForest<String, GraphCreator.Edge>(), DelegateTree.<String, GraphCreator.Edge>getFactory(),Functions.<Double>constant(1.0));
		//DelegateForest<String, GraphCreator.Edge> prim = new DelegateForest<String, GraphCreator.Edge>();
		Forest<String, GraphCreator.Edge> tree = prim.getForest();
		Layout<String,GraphCreator.Edge> commitLayout = new TreeLayout<String, GraphCreator.Edge>(tree,300,150);
		commitLayout.setLocation("/commit/1", new Point2D.Double(150,100));
		return commitLayout;
	}

	private Function<String,Point2D> defineVertexLocations(Model model){

		Collection<String> commitVertices = new ArrayList<String>();
		Collection<String> collectionOfCommitVertices = new ArrayList<String>();
		Map<String,Point2D> map = new HashMap<String,Point2D>();
		int numberOfCommits = 0;

		for(String vertex : graph.getVertices()){
			if(vertex.contains("/commit")){
				numberOfCommits++;
			}
		}

		//set commit vertex positions in a tree layout
		Layout<String,GraphCreator.Edge> commitLayout = setPositionsForCommits(model);
		commitVertices = commitLayout.getGraph().getVertices();
		if(commitVertices.size()!=numberOfCommits){//initial dataset, no commits made yet
			for(String vertex : graph.getVertices()){
				if(vertex.contains("/commit")){
					map.put(vertex, new Point2D.Double(100,100));
					collectionOfCommitVertices.add(vertex);
				}
			}
		}
		collectionOfCommitVertices.addAll(commitVertices);
		//adapt commit vertex positions
		for(String vertex : commitVertices){
			map.put(vertex, commitLayout.apply(vertex));
		}

		//set subordinate vertices for every commit
		for(String vertex : collectionOfCommitVertices){
			setPositionsForSubordinateVertices(map, vertex);
		}

		Function<String,Point2D> vertexLocations = Functions.forMap(map);
		return vertexLocations;
	}

	private void setLayout(Model model, int layout){
		switch (layout){
		case 0:
			Function<String,Point2D> vertexLocations = defineVertexLocations(model);
			layout0 = new StaticLayout<String, GraphCreator.Edge>(graph,vertexLocations,preferredSize);
			vm = new DefaultVisualizationModel<String, GraphCreator.Edge>(layout0,preferredSize);
			vv =  new VisualizationViewer<String, GraphCreator.Edge>(vm,preferredSize);
			break;
		case 1:
			layout1 = new StaticLayout<String, GraphCreator.Edge>(graph,preferredSize);
			vm = new DefaultVisualizationModel<String, GraphCreator.Edge>(layout1,preferredSize);
			vv =  new VisualizationViewer<String, GraphCreator.Edge>(vm,preferredSize);
			break;
		case 2:
			MinimumSpanningForest2<String, GraphCreator.Edge> prim = new MinimumSpanningForest2<String, GraphCreator.Edge>(graph,new DelegateForest<String, GraphCreator.Edge>(), DelegateTree.<String, GraphCreator.Edge>getFactory(),Functions.<Double>constant(1.0));
			Forest<String, GraphCreator.Edge> tree = prim.getForest();
			layout2 = new TreeLayout<String, GraphCreator.Edge>(tree,100);
			vm = new DefaultVisualizationModel<String, GraphCreator.Edge>(layout2,preferredSize);
			vv =  new VisualizationViewer<String, GraphCreator.Edge>(vm,preferredSize);
			break;
		case 3:
			layout3 = new CircleLayout<String, GraphCreator.Edge>(graph);
			vm = new DefaultVisualizationModel<String, GraphCreator.Edge>(layout3,preferredSize);
			vv =  new VisualizationViewer<String, GraphCreator.Edge>(vm,preferredSize);
			break;
		case 4:
			layout4 = new KKLayout<String, GraphCreator.Edge>(graph);
			vm = new DefaultVisualizationModel<String, GraphCreator.Edge>(layout4,preferredSize);
			vv =  new VisualizationViewer<String, GraphCreator.Edge>(vm,preferredSize);
			break;
		case 5:
			layout5 = new FRLayout<String, GraphCreator.Edge>(graph);
			vm = new DefaultVisualizationModel<String, GraphCreator.Edge>(layout5,preferredSize);
			vv =  new VisualizationViewer<String, GraphCreator.Edge>(vm,preferredSize);
			break;
		case 6:
			layout6 = new SpringLayout<String, GraphCreator.Edge>(graph);
			vm = new DefaultVisualizationModel<String, GraphCreator.Edge>(layout6,preferredSize);
			vv =  new VisualizationViewer<String, GraphCreator.Edge>(vm,preferredSize);
			break;
		case 7:
			layout7 = new ISOMLayout<String, GraphCreator.Edge>(graph);
			vm = new DefaultVisualizationModel<String, GraphCreator.Edge>(layout7,preferredSize);
			vv =  new VisualizationViewer<String, GraphCreator.Edge>(vm,preferredSize);
			break;
		case 8:
			layout8 = new DAGLayout<String, GraphCreator.Edge>(graph);
			vm = new DefaultVisualizationModel<String, GraphCreator.Edge>(layout8,preferredSize);
			vv =  new VisualizationViewer<String, GraphCreator.Edge>(vm,preferredSize);
			break;
		case 9:
			prim = new MinimumSpanningForest2<String, GraphCreator.Edge>(graph,new DelegateForest<String, GraphCreator.Edge>(), DelegateTree.<String, GraphCreator.Edge>getFactory(),Functions.<Double>constant(1.0));
			tree = prim.getForest();
			layout9 = new BalloonLayout<String, GraphCreator.Edge>(tree);
			vm = new DefaultVisualizationModel<String, GraphCreator.Edge>(layout9,preferredSize);
			vv =  new VisualizationViewer<String, GraphCreator.Edge>(vm,preferredSize);
			break;
		}
	}

	private void renderViewer(){
		// Transformer maps the vertex number to a vertex property
		Function<String,Paint> vertexColor = new Function<String,Paint>() {
			public Paint apply(String i) {
				Paint color = null;
				if(i.contains("/delta/")){
					color = Color.blue;
				}else if(i.contains("/commit/")){
					color = Color.cyan;
				}else if((i.contains("File"))&&(i.contains("Script"))){
					color = Color.red;
				}else if((i.contains("File"))&&(i.contains("Model"))){
					color = Color.magenta;
				}else if((i.contains("/statement/"))){
					color = Color.orange;
				}else if((i.contains("/baseline/"))){
					color = Color.yellow;
				}else if((i.contains("AuthorID: "))){
					color = Color.gray;
				}else{
					color = Color.green;
				}
				return color;
			}
		};

		Function<String,Shape> vertexSize = new Function<String,Shape>() {
			public Shape apply(String i){
				int length = i.length();
				Rectangle rectangle = new Rectangle(-15, -15, length*6, 30);
				return rectangle;
			}
		};

		vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
		vv.setForeground(Color.black);
		vv.getRenderContext().setVertexShapeTransformer(vertexSize);
		vv.getRenderContext().setVertexFillPaintTransformer(vertexColor);
		vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(graph));
	}

	private void setUserInputHandling(){
		// The following code adds capability for mouse picking of vertices/edges. Vertices can even be moved!
		final DefaultModalGraphMouse<String,Number> graphMouse = new DefaultModalGraphMouse<String,Number>();
		vv.setGraphMouse(graphMouse);
		final ScalingControl scaler = new CrossoverScalingControl();
		//vv.scaleToLayout(scaler);
		scaler.scale(vv, 1 / 1.1f, vv.getCenter());
	}

}
