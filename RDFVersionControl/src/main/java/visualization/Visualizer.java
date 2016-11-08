package visualization;

import mainPackage.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;

public class Visualizer {

	private static String instructions =
			"<html>"+
					"<h3>All Modes:</h3>"+
					"<ul>"+
					"<li>Right-click an empty area for <b>Create Vertex</b> popup"+
					"<li>Right-click on a Vertex for <b>Delete Vertex</b> popup"+
					"<li>Right-click on a Vertex for <b>Add Edge</b> menus <br>(if there are selected Vertices)"+
					"<li>Right-click on an Edge for <b>Delete Edge</b> popup"+
					"<li>Mousewheel scales with a crossover value of 1.0.<p>"+
					"     - scales the graph layout when the combined scale is greater than 1<p>"+
					"     - scales the graph view when the combined scale is less than 1"+

	        "</ul>"+
	        "<h3>Editing Mode:</h3>"+
	        "<ul>"+
	        "<li>Left-click an empty area to create a new Vertex"+
	        "<li>Left-click on a Vertex and drag to another Vertex to create an Undirected Edge"+
	        "<li>Shift+Left-click on a Vertex and drag to another Vertex to create a Directed Edge"+
	        "</ul>"+
	        "<h3>Picking Mode:</h3>"+
	        "<ul>"+
	        "<li>Mouse1 on a Vertex selects the vertex"+
	        "<li>Mouse1 elsewhere unselects all Vertices"+
	        "<li>Mouse1+Shift on a Vertex adds/removes Vertex selection"+
	        "<li>Mouse1+drag on a Vertex moves all selected Vertices"+
	        "<li>Mouse1+drag elsewhere selects Vertices in a region"+
	        "<li>Mouse1+Shift+drag adds selection of Vertices in a new region"+
	        "<li>Mouse1+CTRL on a Vertex selects the vertex and centers the display on it"+
	        "<li>Mouse1 double-click on a vertex or edge allows you to edit the label"+
	        "</ul>"+
	        "<h3>Transforming Mode:</h3>"+
	        "<ul>"+
	        "<li>Mouse1+drag pans the graph"+
	        "<li>Mouse1+Shift+drag rotates the graph"+
	        "<li>Mouse1+CTRL(or Command)+drag shears the graph"+
	        "<li>Mouse1 double-click on a vertex or edge allows you to edit the label"+
	        "</ul>"+
	        "<h3>Annotation Mode:</h3>"+
	        "<ul>"+
	        "<li>Mouse1 begins drawing of a Rectangle"+
	        "<li>Mouse1+drag defines the Rectangle shape"+
	        "<li>Mouse1 release adds the Rectangle as an annotation"+
	        "<li>Mouse1+Shift begins drawing of an Ellipse"+
	        "<li>Mouse1+Shift+drag defines the Ellipse shape"+
	        "<li>Mouse1+Shift release adds the Ellipse as an annotation"+
	        "<li>Mouse3 shows a popup to input text, which will become"+
	        "<li>a text annotation on the graph at the mouse location"+
	        "</ul>"+
	        "</html>";

	//visualizes the graph
	public static void visualizationTool(VersionModel versionModel, String selectedCommit, boolean decider){
		if((!KVC.initWorkspace)||(!KVC.initLocalRepo)){
			System.out.println("Workspace is not set up yet");
		}else{
			Dimension frameSize = new Dimension(700,740);
			if(Objects.equals(KVC.frame, null)){
				KVC.frame = new JFrame();
				KVC.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
			Container content = KVC.frame.getContentPane();
			Container versionPanel = new JPanel(new BorderLayout());	
			Container applicationPanel = new JPanel(new BorderLayout());
			content.removeAll();

			Model model1 = versionModel.getVersionModel();
			Model model2 = versionModel.getApplicationModel(selectedCommit);

			String versionName = "Version model: " + versionModel.getRepo();
			String applicationName = "Application model: Commit " + selectedCommit;

			SimpleGraphView sgv1 = new SimpleGraphView(model1,0,frameSize);//versionGraph
			SimpleGraphView sgv2 = new SimpleGraphView(model2,Settings.GRAPHLAYOUT,frameSize);//displayedGraph

			if(decider){
				String commitString = "http://example.org/commit/" + selectedCommit;
				sgv1.vv.getPickedVertexState().pick(commitString, true);
				sgv1.vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<String>(sgv1.vv.getPickedVertexState(), Color.getColor(commitString), Color.yellow));
			}

			/*GraphZoomScrollPane gzsp = new GraphZoomScrollPane(sgv.vv);
	        panel.add(gzsp);*/
			// create a GraphMouse for the main view
			GraphZoomScrollPane gzsp1 = new GraphZoomScrollPane(sgv1.vv);
			GraphZoomScrollPane gzsp2 = new GraphZoomScrollPane(sgv2.vv);
			versionPanel.add(gzsp1);
			applicationPanel.add(gzsp2);

			JComboBox<?> modeBox = ((DefaultModalGraphMouse<?, ?>) sgv1.vv.getGraphMouse()).getModeComboBox();
			modeBox.addItemListener(((DefaultModalGraphMouse<?, ?>) sgv1.vv.getGraphMouse()).getModeListener());
			modeBox.addItemListener(((DefaultModalGraphMouse<?, ?>) sgv2.vv.getGraphMouse()).getModeListener());

			JButton help = new JButton("Help");
			help.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(sgv1.vv, instructions);
				}
			});

			JPanel control = new JPanel();
			control.add(modeBox);
			control.add(help);

			//label the graphs
			versionPanel.add(new JLabel(versionName), BorderLayout.NORTH);
			applicationPanel.add(new JLabel(applicationName), BorderLayout.NORTH);

			content.add(versionPanel);
			content.add(applicationPanel, BorderLayout.EAST);
			content.add(control,BorderLayout.SOUTH);

			content.setBackground(java.awt.Color.white);
			KVC.frame.pack();
			KVC.frame.setVisible(true);
		}
	}

	//visualizes the graph
	public static void visualizationTool(VersionModel versionModel1, String selectedCommit1, VersionModel versionModel2, String selectedCommit2, boolean decider){
		if((!KVC.initWorkspace)||(!KVC.initLocalRepo)){
			System.out.println("Workspace is not set up yet");
		}else{
			Dimension frameSize = new Dimension(700,340);
			if(Objects.equals(KVC.frame, null)){
				KVC.frame = new JFrame();
				KVC.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
			Container content = KVC.frame.getContentPane();
			Container versionPanel1 = new JPanel(new BorderLayout());	
			Container applicationPanel1 = new JPanel(new BorderLayout());
			Container versionPanel2 = new JPanel(new BorderLayout());	
			Container applicationPanel2 = new JPanel(new BorderLayout());
			content.removeAll();

			Model model1 = versionModel1.getVersionModel();
			Model model2 = versionModel1.getApplicationModel(selectedCommit1);
			Model model3 = versionModel2.getVersionModel();
			Model model4 = versionModel2.getApplicationModel(selectedCommit2);

			String versionName1 = "Version model: " + versionModel1.getRepo();
			String applicationName1 = "Application model: Commit " + selectedCommit1;
			String versionName2 = "Version model: " + versionModel2.getRepo();
			String applicationName2 = "Application model: Commit " + selectedCommit2;

			SimpleGraphView sgv1 = new SimpleGraphView(model1,0,frameSize);//versionGraph, working directory
			SimpleGraphView sgv2 = new SimpleGraphView(model2,Settings.GRAPHLAYOUT,frameSize);//displayedGraph, working directory
			SimpleGraphView sgv3 = new SimpleGraphView(model3,0,frameSize);//versionGraph, local repository
			SimpleGraphView sgv4 = new SimpleGraphView(model4,Settings.GRAPHLAYOUT,frameSize);//displayedGraph, local repository

			if(decider){
				String commitString1 = "http://example.org/commit/" + selectedCommit1;
				sgv1.vv.getPickedVertexState().pick(commitString1, true);
				sgv1.vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<String>(sgv1.vv.getPickedVertexState(), Color.getColor(commitString1), Color.yellow));
				String commitString2 = "http://example.org/commit/" + selectedCommit2;
				sgv3.vv.getPickedVertexState().pick(commitString2, true);
				sgv3.vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<String>(sgv3.vv.getPickedVertexState(), Color.getColor(commitString2), Color.yellow));
			}

			/*GraphZoomScrollPane gzsp = new GraphZoomScrollPane(sgv.vv);
		        panel.add(gzsp);*/
			// create a GraphMouse for the main view
			GraphZoomScrollPane gzsp1 = new GraphZoomScrollPane(sgv1.vv);
			GraphZoomScrollPane gzsp2 = new GraphZoomScrollPane(sgv2.vv);
			GraphZoomScrollPane gzsp3 = new GraphZoomScrollPane(sgv3.vv);
			GraphZoomScrollPane gzsp4 = new GraphZoomScrollPane(sgv4.vv);
			versionPanel1.add(gzsp1);
			applicationPanel1.add(gzsp2);
			versionPanel2.add(gzsp3);
			applicationPanel2.add(gzsp4);

			JComboBox<?> modeBox = ((DefaultModalGraphMouse<?, ?>) sgv1.vv.getGraphMouse()).getModeComboBox();
			modeBox.addItemListener(((DefaultModalGraphMouse<?, ?>) sgv1.vv.getGraphMouse()).getModeListener());
			modeBox.addItemListener(((DefaultModalGraphMouse<?, ?>) sgv2.vv.getGraphMouse()).getModeListener());
			modeBox.addItemListener(((DefaultModalGraphMouse<?, ?>) sgv3.vv.getGraphMouse()).getModeListener());
			modeBox.addItemListener(((DefaultModalGraphMouse<?, ?>) sgv4.vv.getGraphMouse()).getModeListener());

			JButton help = new JButton("Help");
			help.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(sgv1.vv, instructions);
				}
			});

			JPanel control = new JPanel();
			control.add(modeBox);
			control.add(help);

			//label the graphs
			versionPanel1.add(new JLabel(versionName1), BorderLayout.NORTH);
			applicationPanel1.add(new JLabel(applicationName1), BorderLayout.NORTH);
			versionPanel2.add(new JLabel(versionName2), BorderLayout.NORTH);
			applicationPanel2.add(new JLabel(applicationName2), BorderLayout.NORTH);

			versionPanel1.add(applicationPanel1,BorderLayout.EAST);
			versionPanel2.add(applicationPanel2,BorderLayout.EAST);
			content.add(versionPanel1,BorderLayout.NORTH);
			content.add(versionPanel2);
			content.add(control,BorderLayout.SOUTH);

			content.setBackground(java.awt.Color.white);
			KVC.frame.pack();
			KVC.frame.setVisible(true);
		}
	}

	//visualizes the graph
	public static void visualizationTool(VersionModel versionModel, String selectedCommit){
		visualizationTool(versionModel,selectedCommit,false);
	}

	//visualizes the graph
	public static void visualizationTool(VersionModel versionModel1, String selectedCommit1, VersionModel versionModel2, String selectedCommit2){
		visualizationTool(versionModel1,selectedCommit1,versionModel2,selectedCommit2,false);
	}

	//display both version and displayed model
	public static void displayModels(VersionModel versionModel1, String currentCommit1,VersionModel versionModel2, String currentCommit2){
		if((versionModel1.getCommitNumbers().contains(currentCommit1))&&(versionModel2.getCommitNumbers().contains(currentCommit2))){
			//print modified RDF dataset
			/*
			System.out.println("This is the version model \"" + versionModel1.getRepo() + "\":");
			printModel(versionModel1.getVersionModel());
			System.out.println("This is the dataset after commit " + currentCommit1 + ":");
			printModel(versionModel1.getApplicationModel(currentCommit1));
			System.out.println("This is the version model \"" + versionModel2.getRepo() + "\":");
			printModel(versionModel2.getVersionModel());
			System.out.println("This is the dataset after commit " + currentCommit2 + ":");
			printModel(versionModel2.getApplicationModel(currentCommit2));
			*/
			visualizationTool(versionModel1,currentCommit1,versionModel2,currentCommit2);
		}else{
			System.out.println("At least one of the commit numbers is invalid.");
		}
	}

	//display both version and displayed model
	public static void displayModels(VersionModel versionModel1, VersionModel versionModel2){
		String currentCommit1 = versionModel1.getCurrentCommit();
		String currentCommit2 = versionModel2.getCurrentCommit();

		displayModels(versionModel1,currentCommit1,versionModel2,currentCommit2);
	}

	//display both version and displayed model
	public static void displayModels(VersionModel versionModel, String currentCommit){
		if(versionModel.getCommitNumbers().contains(currentCommit)){
			//print modified RDF dataset
			/*
			System.out.println("This is the version model:");
			printModel(versionModel.getVersionModel());
			System.out.println("This is the dataset after commit " + currentCommit + ":");
			printModel(versionModel.getApplicationModel(currentCommit));
			*/
			visualizationTool(versionModel,currentCommit);
		}else{
			System.out.println("The commit number is invalid.");
		}
	}

	//display both version and displayed model of current commit
	public static void displayModels(VersionModel versionModel){
		displayModels(versionModel,versionModel.getCurrentCommit());
	}

	//prints out the triples of the input model, counts displayed model triples and additional triples and prints the numbers
	public static void printModel(Model model) {
		// list the statements in the Model
		StmtIterator iter = model.listStatements();

		// print out the predicate, subject and object of each statement
		int dataTriples = 0;
		int commitTriples = 0;
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement(); // get next statement
			Resource subject = stmt.getSubject(); // get the subject
			Property predicate = stmt.getPredicate(); // get the predicate
			RDFNode object = stmt.getObject(); // get the object

			String subjectFormat = "%1$-80s";
			String predicateFormat = "%1$-80s";
			String objectFormat = "%1$-80s";

			//System.out.print(counter + ". "); //numbers triples
			System.out.printf(subjectFormat, "<" + subject.toString() + ">");
			System.out.printf(predicateFormat, "<" + predicate.toString() + ">");
			if (object instanceof Resource) {
				System.out.printf(objectFormat, "<" + object.toString() + ">");
			} else {
				// object is a literal
				System.out.printf(objectFormat, "\"" + object.toString() + "\"");
			}

			System.out.println(".");
			if(subject.toString().contains("memoriesOfSF/")){
				dataTriples++; //counts the number of data triples
			}else{
				commitTriples++; //counts the number of hidden triples in displayed model
			}
		}
		System.out.println("\nNumber of data triples in the dataset: " + dataTriples);
		System.out.println("Number of commit triples in the dataset: " + commitTriples);
		System.out.println();
		/*
		 * // now write the model in XML form to a file System.out.println(
		 * "\n---------XML---------\n "); model.write(System.out,
		 * "RDF/XML-ABBREV");
		 * 
		 * // now write the model in N-TRIPLES form to a file
		 * model.write(System.out, "N-TRIPLES");
		 */
	}

	//prints an individual statement
	public static void printStatement (Statement stmt) {

		Resource  subject   = stmt.getSubject();     // get the subject
		Property  predicate = stmt.getPredicate();   // get the predicate
		RDFNode   object    = stmt.getObject();      // get the object
		
		if(subject.toString().contains("<")&&subject.toString().contains(">")){
			System.out.printf(Settings.printFormat,subject.toString());
		}else{
			System.out.printf(Settings.printFormat,"<" + subject.toString() + ">");
		}
		if(predicate.toString().contains("<")&&predicate.toString().contains(">")){
			System.out.printf(Settings.printFormat,predicate.toString());
		}else{
			System.out.printf(Settings.printFormat,"<" + predicate.toString() + ">");
		}
		if (object instanceof Resource) {
			if(object.toString().contains("<")&&object.toString().contains(">")){
				System.out.printf(Settings.printFormat,object.toString());
			}else{
				System.out.printf(Settings.printFormat,"<" + object.toString() + ">");
			}
		} else {
			// object is a literal
			if(object.toString().contains("\"")){
				System.out.printf(Settings.printFormat,object.toString());
			}else{
				System.out.printf(Settings.printFormat,"\"" + object.toString() + "\"");
			}
		}

		System.out.println(".");
	}

	//prints an individual statement
	public static void printTriple (String[] triple) {

		System.out.printf(Settings.printFormat,triple[0] + ": ");
		
		System.out.printf(Settings.printFormat,triple[1]);
		
		System.out.printf("%1$-80s",triple[2]);
		
		for(int i=3; i<triple.length;i++){
			System.out.printf(Settings.printFormat,triple[i]);
		}	
		System.out.println(".");
	}

	//prints an individual statement
	public static void displayIndexScript(){
		int counter = 1;
		List<String[]> triples = new ArrayList<String[]>();
		triples.addAll(KVC.indexScript.getDeleteTriples());
		Iterator<String[]> iter = triples.iterator();
		while(iter.hasNext()){
			System.out.print("Deleted triple: " + counter + " ");
			printStatement(SparqlUtils.convertTriple2Statement(iter.next()));
		}

		triples.removeAll(triples);
		triples.addAll(KVC.indexScript.getInsertTriples());
		iter = triples.iterator();
		while(iter.hasNext()){
			System.out.print("Inserted triple: " + counter + " ");
			printStatement(SparqlUtils.convertTriple2Statement(iter.next()));
			counter++;
		}
	}

	//lists last commits of all branches
	public static void listBranches(){
		List<String> branches = new ArrayList<String>();
		StmtIterator iter = KVC.localRepModel.getVersionModel().listStatements();
		while(iter.hasNext()){
			Statement nextStmt = iter.nextStatement();
			if(nextStmt.getObject().toString().contains("File-displayedModel")){
				String commitNumber = nextStmt.getSubject().toString().substring(nextStmt.getSubject().toString().indexOf("commit/")+"commit/".length());
				if(KVC.localRepModel.getCommitNumbers().contains(commitNumber)){
					branches.add(commitNumber);
				}
			}
		}
		int counter = 0;
		for(String currentBranch : branches){
			counter++;
			System.out.println("Branch " + counter + ": " + currentBranch);
		}
		System.out.println("Current branch " + KVC.localRepModel.getLatestCommit(KVC.localRepModel.getCurrentCommit()));
	}

}
