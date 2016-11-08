package mainPackage;

import visualization.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import javax.swing.JFrame;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.*;

public class KVC extends Object {
	// some definitions
	public static JFrame frame = null;
	public static boolean initWorkspace = false;
	public static boolean initLocalRepo = false;
	public static VersionModel workspaceModel = null;
	public static VersionModel localRepModel = null;
	public static Map<String,String> workspaceNameMap = new HashMap<String,String>();
	public static Map<String,String> workspaceTypeMap = new HashMap<String,String>();
	public static Scanner scanner = new Scanner(System.in);
	public static String dirWorkspace = new String();
	public static String dirLocalRep = new String();
	public static SparqlScript indexScript = new SparqlScript();
	
	/**
	0: User defined layout
	1: StaticLayout<String, String>(graph,preferredSize);
	2: TreeLayout
	3: CircleLayout
	4: KKLayout
	5: FRLayout
	6: SpringLayout
	7: ISOMLayout
	8: DAGLayout
	9: BalloonLayout
	 */

	//checks all triples of a triple iterator list, if one contains a resource with a certain name
	private static boolean compareTripleNames(StmtIterator iter, String resourceStr){
		String[] tripleString = new String[3];
		boolean bool = false;
		while(iter.hasNext()){
			Statement nextStmt = iter.nextStatement();
			tripleString[0] = nextStmt.getSubject().toString();
			tripleString[1] = nextStmt.getPredicate().toString();
			tripleString[2] = nextStmt.getObject().toString();
			if(Objects.equals(tripleString[0],resourceStr)){
				bool = true;
			}else if(Objects.equals(tripleString[1],resourceStr)){
				bool = true;
			}else if(Objects.equals(tripleString[2],resourceStr)){
				bool = true;
			}
		}
		return bool;
	}

	//empty local and remote repositories
	private static void emptyRepositories(){
		System.out.println("Do you want to delete this repository?");
		System.out.println(dirLocalRep);
		System.out.println("(y)\n(n)");
		String decider = scanner.nextLine();
		if(Objects.equals(decider, "y")){
			try {
				FileUtils.deleteDirectory(new File(dirLocalRep));
				//				FileUtils.deleteDirectory(new File(dirRemoteRep));
				//				new File(dirRemoteRep).mkdirs();
				System.out.println("Repositories emptied.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//deletes all created files
	private static void workspaceDeletion(){
		System.out.println("Do you want to delete this repository?");
		System.out.println(dirWorkspace);
		System.out.println("(y)\n(n)");
		String decider = scanner.nextLine();
		if(Objects.equals(decider, "y")){
			try {
				FileUtils.deleteDirectory(new File(dirWorkspace));
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Workspace deleted.");
		}
	}

	//terminates the program
	public static void exitFunction(){
		//delete all sparql-Script files
		//deleteSparqlFiles();
		workspaceDeletion();
		emptyRepositories();
		//close scanner
		scanner.close();
		//exit program
		System.out.println("Program terminated");
		System.exit(0);
	}

	public static void createBranch(VersionModel versionModel){
		System.out.println(versionModel.getCommitNumbers() + ", c: " + versionModel.getCurrentCommit());
		System.out.println("Back (b)");
		String commitNumber = scanner.nextLine();
		if(Objects.equals(commitNumber, "b")){
			return;
		}else if(Objects.equals(commitNumber, "c")){
			commitNumber = versionModel.getCurrentCommit();
		}
		if(versionModel.getCommitNumbers().contains(commitNumber)){
			versionModel.addBranch(commitNumber);
			versionModel.setCurrentCommit(commitNumber);
			if(Settings.QVF){
				Visualizer.displayModels(workspaceModel,localRepModel);
			}else{
				Visualizer.displayModels(workspaceModel);
			}
		}else{
			System.out.println("This is not a valid number, please insert a different one.");
		}
	}

	//scans two displayed models for their differences//merge algorithm idea 1
	public static void mergeModels(VersionModel versionModel){
		boolean found = false;
		boolean sameBranch = false;
		boolean backwardFound = true;

		String firstCommitNumber = new String();
		String secondCommitNumber = new String();

		//get first version
		while(!found){
			System.out.println(versionModel.getCommitNumbers() + ", c: " + versionModel.getCurrentCommit());
			System.out.println("First model: ");
			System.out.println("Back (b)");
			firstCommitNumber = scanner.nextLine();
			if(Objects.equals(firstCommitNumber, "c")){
				firstCommitNumber = versionModel.getCurrentCommit();
				found=true;
			}else if(Objects.equals(firstCommitNumber, "b")){
				return;
			}else if(!(versionModel.getCommitNumbers().contains(firstCommitNumber))){
				System.out.println("This is not a valid number, please insert a different one.");
			}else{
				found=true;
			}
		}

		found = false;
		//get second version
		while(!found){
			System.out.println(versionModel.getCommitNumbers() + ", c: " + versionModel.getCurrentCommit());
			System.out.println("Second model: ");
			System.out.println("Back (b)");
			secondCommitNumber = scanner.nextLine();
			if(Objects.equals(secondCommitNumber, "c")){
				secondCommitNumber = versionModel.getCurrentCommit();
				found = true;
			}else if(Objects.equals(secondCommitNumber, "b")){
				return;
			}else if(!(versionModel.getCommitNumbers().contains(secondCommitNumber))){
				System.out.println("This is not a valid number, please insert a different one.");
				found = false;
			}else{
				found = true;
			}
			if(Objects.equals(firstCommitNumber, secondCommitNumber)){
				System.out.println("The two versions intended to be merged are equal");
				found = false;
			}
		}

		Model model = versionModel.getVersionModel();
		String commitString = "http://example.org/commit/" + firstCommitNumber;
		//search branch of first version backward to find if it contains second version
		while(backwardFound){
			backwardFound=false;
			StmtIterator stmtIter = model.listStatements();
			while(stmtIter.hasNext()){
				Statement nextStmt = stmtIter.nextStatement();
				if(		(nextStmt.getSubject().toString().equals(commitString)) &&
						(nextStmt.getPredicate().toString().contains("backwardStream"))){
					commitString = nextStmt.getObject().toString();
					if(Objects.equals(commitString, ("http://example.org/commit/" + secondCommitNumber))) sameBranch=true;
					backwardFound=true;
					break;
				}
			}
		}

		backwardFound = true;
		commitString = "http://example.org/commit/" + secondCommitNumber;
		//search branch of second version backward to find if it contains first version
		if(!sameBranch){
			while(backwardFound){
				backwardFound=false;
				StmtIterator stmtIter = model.listStatements();
				while(stmtIter.hasNext()){
					Statement nextStmt = stmtIter.nextStatement();
					if(		(nextStmt.getSubject().toString().equals(commitString)) &&
							(nextStmt.getPredicate().toString().contains("backwardStream"))){
						commitString = nextStmt.getObject().toString();
						if(Objects.equals(commitString, ("http://example.org/commit/" + firstCommitNumber))) sameBranch=true;
						backwardFound=true;
						break;
					}
				}
			}
		}
		if(sameBranch){
			System.out.println("The two versions can not be merged, because they do not reside in different branches.");
		}else{
			versionModel.addMergeCommit(firstCommitNumber, secondCommitNumber);
		}
		if(Settings.QVF){
			Visualizer.displayModels(workspaceModel,localRepModel);
		}else{
			Visualizer.displayModels(workspaceModel);
		}
	}

	//scans two displayed models for their differences//merge algorithm idea 1
	public static void mergeModels(VersionModel versionModel, String commit1, String commit2){

		boolean sameBranch = false;
		boolean backwardFound = true;

		//check first version
		if(Objects.equals(commit1, versionModel.getLatestCommit(commit1))){
			System.out.println("Commit does not indicate the end of a branch");
			return;
		}

		if(Objects.equals(commit2, versionModel.getLatestCommit(commit2))){
			System.out.println("Commit does not indicate the end of a branch");
			return;
		}

		Model model = versionModel.getVersionModel();
		String commitString = "http://example.org/commit/" + commit1;
		//search branch of first version backward to find if it contains second version
		while(backwardFound){
			backwardFound=false;
			StmtIterator stmtIter = model.listStatements();
			while(stmtIter.hasNext()){
				Statement nextStmt = stmtIter.nextStatement();
				if(		(nextStmt.getSubject().toString().equals(commitString)) &&
						(nextStmt.getPredicate().toString().contains("backwardStream"))){
					commitString = nextStmt.getObject().toString();
					if(Objects.equals(commitString, ("http://example.org/commit/" + commit2))) sameBranch=true;
					backwardFound=true;
					break;
				}
			}
		}

		backwardFound = true;
		commitString = "http://example.org/commit/" + commit2;
		//search branch of second version backward to find if it contains first version
		if(!sameBranch){
			while(backwardFound){
				backwardFound=false;
				StmtIterator stmtIter = model.listStatements();
				while(stmtIter.hasNext()){
					Statement nextStmt = stmtIter.nextStatement();
					if(		(nextStmt.getSubject().toString().equals(commitString)) &&
							(nextStmt.getPredicate().toString().contains("backwardStream"))){
						commitString = nextStmt.getObject().toString();
						if(Objects.equals(commitString, ("http://example.org/commit/" + commit1))) sameBranch=true;
						backwardFound=true;
						break;
					}
				}
			}
		}

		if(sameBranch){
			System.out.println("The two versions can not be merged, because they do not reside in different branches.");
		}else{
			versionModel.addMergeCommit(commit1, commit2);
		}
		if(Settings.QVF){
			Visualizer.displayModels(workspaceModel,localRepModel);
		}else{
			Visualizer.displayModels(workspaceModel);
		}
	}

	//highlight commit with additional literal
	public static void tagCommit(VersionModel model){
		System.out.println(model.getCommitNumbers());
		String commitNumber = scanner.nextLine();
		if(model.getCommitNumbers().contains(commitNumber)){
			model.createBaselineTag(commitNumber);
			if(Settings.QVF){
				if(Objects.equals(model, workspaceModel)){
					Visualizer.displayModels(workspaceModel,commitNumber,localRepModel,localRepModel.getCurrentCommit());
				}else if(Objects.equals(model, localRepModel)){
					Visualizer.displayModels(workspaceModel,workspaceModel.getCurrentCommit(),localRepModel,commitNumber);
				}
			}else{
				Visualizer.displayModels(model,commitNumber);
			}
		}else{
			System.out.println("This is not a valid number, please insert a different one.");
		}
	}

	/*
	private static void initializeRemoteRepository(){
		String inputString;
		boolean found = false;

		System.out.println("Insert directory of remote repository:");
		//dirRemoteRep = "/Users/Andreas/Documents/EigeneDokumente/Uni/Workspace/Git-Projekte/rdf-version-management-demo/RDFVersionControl/RemoteRepository";
		//System.out.println("**********************************************\n" + dirRemoteRep + "\n**********************************************");
		do{
			inputString = scanner.nextLine();
			if(new File(inputString).isDirectory()){
				dirRemoteRep=inputString;
				found = true;
			}else{
				System.out.println("The directory \"" + inputString + "\" could not be found.");
				System.out.println("Please enter new directory path");
			}
		}while(!found);
		if(!(dirRemoteRep.endsWith("/"))) dirRemoteRep = dirRemoteRep + "/";

		System.out.println("Remote repository set up.");
	}
	 */

	//sets up local repository in input directory
	public static void initializeLocalRepository(String inputString){
		//dirLocalRep = "/Users/Andreas/Documents/EigeneDokumente/Uni/Workspace/Git-Projekte/rdf-version-management-demo/RDFVersionControl/LocalRepository";
		//System.out.println("**********************************************\n" + dirLocalRep + "\n**********************************************");
		if(!(new File(inputString).isDirectory())){
			new File(inputString).mkdirs();
			/*try {
				File file = new File(inputString);
				file.createNewFile();
				file.mkdirs();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
		}
		dirLocalRep=inputString;
		if(!(dirLocalRep.endsWith("/"))) dirLocalRep = dirLocalRep + "/";
		initLocalRepo = true;
		//create versionModel to track progress
		try {
			File file = new File(dirLocalRep + "versionModel");
			file.getParentFile().mkdirs();
			file.createNewFile();
			localRepModel = new VersionModel(dirLocalRep);
			localRepModel.initApplicationModel(ModelFactory.createDefaultModel());
			SparqlUtils.model2File(localRepModel.getVersionModel(), file.getAbsolutePath());
			System.out.println("Local repository set up:  " + dirLocalRep);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//sets up workingspace in input directory
	public static void initializeWorkspace(String inputString){
		if(!(new File(inputString).isDirectory())){
			new File(inputString).mkdirs();
		}
		dirWorkspace = inputString + File.separator;
		initWorkspace = true;

		try {
			File file = new File(dirWorkspace + "versionModel");
			file.getParentFile().mkdirs();
			file.createNewFile();
			//create versionModel to track progress
			workspaceModel = new VersionModel(dirWorkspace);
			workspaceModel.initApplicationModel(ModelFactory.createDefaultModel());
			SparqlUtils.model2File(workspaceModel.getVersionModel(), file.getAbsolutePath());
			System.out.println("New workspace directory set up: " + dirWorkspace);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Prompts insertion triple
	private static String[] createInsertionTriple(Model model){
		String continueString = "n";
		int objectDecider = 0;
		boolean bool = false;
		String subjectStr = null;
		String predicateStr = null;
		String objectStr = null;
		StmtIterator tripleIter = null;

		//Prompt subject name
		do{
			System.out.println("Please enter the triple's subject URI");
			subjectStr = scanner.nextLine();
			//Compare subject name to existing subjects
			tripleIter = model.listStatements();
			bool = compareTripleNames(tripleIter,subjectStr);
			//Different continutation depending on resource name existence
			if (bool){//existing resource
				System.out.println("A resource with this name already exists. Do you want to add a property to an existing resource?\n(y)\n(n)");
			}else{//new resource
				System.out.println("This is a new resource name. Do you want to create a new resource?\n(y)\n(n)");
			}
			System.out.println("Exit the program with (q)");
			continueString = scanner.nextLine();
			//Program exit with q
			if(continueString == "q"){
				exitFunction();
			}
		}while(Objects.equals(continueString, "n"));
		subjectStr = "<" + subjectStr + ">";

		//Prompt predicate name
		System.out.println("Please enter a corresponding predicate");
		predicateStr = "<" + scanner.nextLine() + ">";

		//Prompt object type
		do{System.out.println("Is the object\n(1) A resource\n(2) A literal");
		try {
			objectDecider = Integer.parseInt(scanner.nextLine());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		if(objectDecider != 1 && objectDecider != 2){
			System.out.println("The input is invalid");
		}
		}while(objectDecider != 1 && objectDecider != 2);

		//Prompt object name
		do{
			if(objectDecider == 1){
				System.out.println("Please enter the triple's object URI");
			}else if(objectDecider == 2){
				System.out.println("Please enter the literal");
			}
			objectStr = scanner.nextLine();
			//Compare object name to existing triples
			tripleIter = model.listStatements();
			bool = compareTripleNames(tripleIter,objectStr);
			//Different continuation depending on resource name existence
			if(objectDecider==1){//resource
				if (bool){//existing resource
					System.out.println("The object name corresponds to an existing resource in the dataset. Do you want to use an existing resource as an object?\n(y)\n(n)");
					continueString = "y";
				}else{
					System.out.println("The object name does not exist already. Do you want to create a new resource?\n(y)\n(n)");
				}
				continueString = scanner.nextLine();
			}
			if(Objects.equals(continueString, "q")){
				exitFunction();
			}
		}while(Objects.equals(continueString, "n"));

		if(objectDecider == 1){
			objectStr = "<" + objectStr + ">";
		}else if(objectDecider == 2){
			objectStr = "\"" + objectStr + "\"";
		}

		//store all parts of the triple in a string array
		String[] statementString = {subjectStr,predicateStr,objectStr};
		return statementString;
	}

	//searches the dataset for the desired triple by requesting user inputs
	private static Statement getTriple(Model model){
		if(model.isEmpty()){
			System.out.println("There are no triples in the model");
			return null;
		}
		//Selects the correct triple by requesting it from the user
		Model lastModel = model;
		Model currentModel = ModelFactory.createDefaultModel();
		StmtIterator iter = lastModel.listStatements();
		Statement currentStmt = null;
		Statement desiredStmt = null;
		String wantedTriple;
		int hitCounter = 0;
		//run through model and select triples that match the search criteria
		do{
			wantedTriple = scanner.nextLine();
			if(Objects.equals(wantedTriple, "b")){
				Visualizer.printModel(model);
				System.out.println("\nWhich triple do you look for?");
				lastModel = model;
			}else if(Objects.equals(wantedTriple, "q")){
				exitFunction();
			}else{
				iter = lastModel.listStatements();
				System.out.println("\nThe corresponding statements are:");
				while (iter.hasNext()) {
					currentStmt = iter.nextStatement();
					if(currentStmt.toString().contains(wantedTriple)){
						hitCounter++;
						Visualizer.printStatement(currentStmt);
						//save last matched triple (when query is correct, it is the only one, otherwise query is repeated)
						desiredStmt = currentStmt;
						currentModel.add(currentStmt);
					}
				}
				//if there was no hit, ask for another search criteria
				if(hitCounter<1){
					System.out.println("\nUnfortunately no triple in the dataset matches the search criterion");
					System.out.println("Please define a different search criterion or get back to the whole dataset with (b)");
					System.out.println("Exit the program with (q)");
				}
				//if there is more than one hit, ask to state the selection more precisely
				if(hitCounter>1){
					System.out.println("\nPlease specify further, which one of the matched triples you looked for:");
					lastModel = currentModel;
					currentModel = ModelFactory.createDefaultModel();
					hitCounter = 0;
				}
			}		
		}while (hitCounter==0);
		return desiredStmt;
	}

	//promts user input concerning triple selections
	private static String[][] promptTriple(Model model, int modificationDecider){
		Statement stmt = null;
		String subjectStr = null;
		String predicateStr = null;
		String objectStr = null;
		String newObjectStr = null;
		String deciderStr = null;

		switch(modificationDecider){
		case 1://delete triple
			do{
				System.out.println("\nWhich triple do you want to delete?");
				stmt = getTriple(model);
				if(!(Objects.equals(stmt, null))){
					System.out.println("\nIs this the triple you want to delete?\n(y)\n(n)");
					deciderStr = scanner.nextLine();
				}
			}while(Objects.equals(deciderStr,"n"));
			break;
		case 3://modify triple
			do{
				System.out.println("\nWhich triple do you want to modify?");
				stmt = getTriple(model);
				if(!(Objects.equals(stmt, null))){
					System.out.println("\nIs this the triple you want to modify?\n(y)\n(n)");
					deciderStr = scanner.nextLine();
				}
			}while(Objects.equals(deciderStr,"n"));
			if(!(Objects.equals(stmt, null))){
				//Prompt new object 
				if(stmt.getObject() instanceof Resource){
					System.out.println("The triple's object is a resource. Please enter the new resource name");
					newObjectStr = "<" + scanner.nextLine() + ">";
				}else{
					System.out.println("The triple's object is a literal. Please enter the new literal string");
					newObjectStr = "\"" + scanner.nextLine() + "\"";
				}
			}
			break;
		}
		if(!(Objects.equals(stmt, null))){
			subjectStr = "<" + stmt.getSubject().toString() + ">";
			predicateStr = "<" + stmt.getPredicate().toString() + ">";
			if(stmt.getObject() instanceof Resource){
				objectStr = "<" + stmt.getObject().toString() + ">";
			}else{
				objectStr = "\"" + stmt.getObject().toString() + "\"";
			}
			String[][] tripleString = {{subjectStr,predicateStr,objectStr},{subjectStr,predicateStr,newObjectStr}};
			return tripleString;
		}else{
			return null;
		}
	}

	//prompts intended modification
	private static int modificationOption(){
		int decider = 0;
		//Prompt the desired modification
		do{
			System.out.println("\nDo you want to\n(1) Delete a triple\n(2) Insert a new triple\n(3) Modify the object of a triple");
			System.out.println("Back (b)");
			System.out.println("Quit (q)");
			String deciderStr = scanner.nextLine();
			//Termination option
			if(Objects.equals(deciderStr, "q")){
				exitFunction();
			}else if(Objects.equals(deciderStr, "b")){
				return 0;
			}
			//Scan option number
			try {
				decider = Integer.parseInt(deciderStr);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			//Failure robustness
			if(!(decider==1 || decider==2 ||decider==3)){
				System.out.println("This option is not available. Please try again or exit with \"q\".");
			}
		}while(!(decider==1 || decider==2 || decider==3));
		return decider;
	}

	//updates on initial RDF model by sparql update file
	private static String updateDatasetByFile(){
		Model newModel = ModelFactory.createDefaultModel();
		String sparqlString = new String();
		String deciderStr = "n";
		do{
			do{
				System.out.println("Please insert a sparql script and finish it with \"END\"");
				scanner.useDelimiter("END");
				sparqlString = scanner.next();
				scanner.reset();//reset scanner
				scanner.nextLine();
				System.out.print(sparqlString);
				System.out.println("\nIs this the SPARQL update script you want to execute?\n(y)\n(n)");
				System.out.println("Back (b)");
				System.out.println("Exit (q)");
				deciderStr = scanner.nextLine();
				if(Objects.equals(deciderStr, "q")){
					exitFunction();
				}else if(Objects.equals(deciderStr, "b")){
					return null;
				}
			}while(Objects.equals(deciderStr,"n"));
			try {
				SparqlUtils.executeSPARQLUpdateQuery(sparqlString, newModel);//try to execute the script
				deciderStr = "n";
			} catch (Exception e) {
				System.out.println("The inserted sparql script is invalid. Please try again");
				System.out.println("Please keep in mind that \" < \",\" > \" and \" \" \" have to be appended at the appropriate positions.");
				deciderStr = "y";
			}
		}while(Objects.equals(deciderStr, "y"));
		return sparqlString;
	}

	//updates on initial RDF model by menu inputs
	private static SparqlScript updateDatasetByMenu(Model model){
		SparqlScript sparqlScript = new SparqlScript();
		List<String[]> tripleStr = new ArrayList<String[]>();
		//Prompt the type of intended modification
		int decider = modificationOption();
		//Prompt the particular triple intended to be modified
		switch (decider){
		case 1://delete triple
			String[][] triple = promptTriple(model,decider);
			if(!(Objects.equals(triple, null))){
				//Prompt wanted triple and create string of its parts
				tripleStr.add(triple[0]);
				sparqlScript.addDeleteTriples(tripleStr);
			}else{
				sparqlScript = null;
			}
			break;
		case 2://insert triple
			tripleStr.add(createInsertionTriple(model));
			sparqlScript.addInsertTriples(tripleStr);
			break;
		case 3://modify triple
			//Prompt wanted triple and create string of its parts
			String[][] tempTriple =promptTriple(model,decider);
			if(!(Objects.equals(tempTriple, null))){
				tripleStr.removeAll(tripleStr);
				tripleStr.add(tempTriple[0]);
				sparqlScript.addDeleteTriples(tripleStr);
				//add modified version of triple as insert triple
				tripleStr.removeAll(tripleStr);
				tripleStr.add(tempTriple[1]);
				sparqlScript.addInsertTriples(tripleStr);
			}else{
				sparqlScript = null;
			}
			break;
		default:
			sparqlScript = null;
			break;
		}
		return sparqlScript;
	}

	//graph analysis menu
	public static void overlookProject(String requestStr){
		boolean found = false;
		String commitNumber = null;
		String currentCommit = workspaceModel.getCurrentCommit();

		switch(requestStr){
		case "version":
			do{
				System.out.println(workspaceModel.getCommitNumbers());
				commitNumber = scanner.nextLine();
				if(workspaceModel.getCommitNumbers().contains(commitNumber)){
					currentCommit = commitNumber;
					Visualizer.printModel(workspaceModel.getApplicationModel(currentCommit));
					if(Settings.QVF){
						Visualizer.visualizationTool(workspaceModel,currentCommit,localRepModel,localRepModel.getCurrentCommit(),true);
					}else{
						Visualizer.visualizationTool(workspaceModel,currentCommit,true);
					}
					found=true;
				}else{
					System.out.println("This is not a valid number, please insert a different one.");
				}
			}while(!found);
			break;
		case "branch":
			createBranch(workspaceModel);
			break;
		case "merge":
			mergeModels(workspaceModel);
			if(Settings.QVF){
				Visualizer.displayModels(workspaceModel,localRepModel);
			}else{
				Visualizer.displayModels(workspaceModel);
			}
			break;
		case "tag":
			tagCommit(workspaceModel);
			break;
		case "exit":
			exitFunction();
			break;
		}

	}

	//Decides, if the update is executed via file or via menu control	
	public static void updateAction(String requestStr){
		SparqlScript sparqlScript = null;
		String sparqlString = null;

		switch(requestStr){
		case "help":
			System.out.print(Scripts.change);
			break;
		case "exit"://Termination option
			exitFunction();
			break;
		case "script":
			sparqlString = updateDatasetByFile();
			if(!(Objects.equals(sparqlString, null))){
				sparqlScript = new SparqlScriptAnalyser(workspaceModel.getApplicationModel(workspaceModel.getCurrentCommit()),sparqlString).getSparqlScript();
				workspaceModel.addCommit(sparqlScript);
				if(Settings.QVF){
					Visualizer.displayModels(workspaceModel,localRepModel);
				}else{
					Visualizer.displayModels(workspaceModel);
				}
			}
			break;
		case "select":
			sparqlScript = updateDatasetByMenu(workspaceModel.getApplicationModel(workspaceModel.getCurrentCommit()));
			if(!(Objects.equals(sparqlScript, null))){
				workspaceModel.addCommit(sparqlScript);
				if(Settings.QVF){
					Visualizer.displayModels(workspaceModel,localRepModel);
				}else{
					Visualizer.displayModels(workspaceModel);
				}
			}
			break;
		case "tag":
			tagCommit(workspaceModel);
			break;
		}
	}

	public static void main(String[] args) {
		//create triplestore dataset
		//Dataset dataset = TDBFactory.createDataset("triplestore");

		do{

			System.out.println("\nMenu:");
			if(initWorkspace&&(workspaceModel.getCurrentCommit()!="0")) System.out.println("(Version: " + workspaceModel.getCurrentCommit() + ")");
			System.out.println("Whenever you need help with the program: \"help\"");

			CommandLineHandler.executeCommand(scanner.nextLine());

		}while(true);
	}
}