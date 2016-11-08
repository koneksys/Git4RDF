package mainPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.update.UpdateAction;

import com.google.common.base.Objects;

public class SparqlUtils {

	//creates and writes text to file
	public static void writeStringToFile(String dir, String content) {
		try {
			File file = new File(dir);

			// if file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//creates and writes text to file
	public static String readStringFromFile(String dir) {
		String content = new String();
		try {
			File file = new File(dir);
			// if file doesn't exists, then create it
			if (file.exists()) {
				FileReader fr = new FileReader(file.getAbsoluteFile());
				BufferedReader br = new BufferedReader(fr);
				String line = new String();
				while((line = br.readLine())!=null){
					content = content + line + "\n";
				}
				br.close();
			}else{
				System.out.println("File not found");
				content = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	//compares triples in all three elements
	public static boolean compareTriples(String[] triple1, String[] triple2){
		boolean match = false;
		if(Objects.equal(triple1[0], triple2[0])){
			if(Objects.equal(triple1[1], triple2[1])){
				if(Objects.equal(triple1[2], triple2[2])){
					match = true;
				}
			}
		}
		return match;
	}

	//converts triple array into statement
	public static Statement convertTriple2Statement(String[] triple){
		Statement stmt = null;
		if(triple.length==3){
			Model model = ModelFactory.createDefaultModel();
			Resource subject = model.createResource(triple[0]);
			Property predicate = model.createProperty(triple[1]);
			if(triple[2].contains("<")&&triple[2].contains(">")){
				RDFNode object = model.createResource(triple[2]).asResource();
				stmt = model.createStatement(subject,predicate,object);
			}else{
				String object = triple[2].substring(triple[2].indexOf("\"")+1, triple[2].lastIndexOf("\""));
				stmt = model.createStatement(subject,predicate,object);
			}
		}else{
			System.out.println("Could not convert String-array to Statement, because it does not contain 3 elements");
		}
		return stmt;
	}

	//adds name of resource to triple, if existent
	public static String[] modifyTriple(String[] triple){
		boolean found = false;
		boolean replaced = false;
		String[] sixtuple = new String[6];
		String[] workTriple = new String[3];
		if((triple[0].contains("<"))&&(triple[0].contains(">"))){
			workTriple[0] = triple[0].substring(triple[0].indexOf("<")+1, triple[0].lastIndexOf(">"));
		}else{
			workTriple[0] = triple[0];
		}
		if((triple[1].contains("<"))&&(triple[1].contains(">"))){
			workTriple[1] = triple[1].substring(triple[1].indexOf("<")+1, triple[1].lastIndexOf(">"));
		}else{
			workTriple[1] = triple[1];
		}
		if((triple[2].contains("<"))&&(triple[2].contains(">"))){
			workTriple[2] = triple[2].substring(triple[2].indexOf("<")+1, triple[2].lastIndexOf(">"));
		}else{
			workTriple[2] = triple[2];
		}
		//check if resource has name
		//looks for replacement possibility for subject
		if(KVC.workspaceNameMap.containsKey(workTriple[0])){
			sixtuple[0] = KVC.workspaceNameMap.get(workTriple[0]);
			found = true;
			replaced = true;
		}
		if(KVC.workspaceTypeMap.containsKey(workTriple[0])){
			sixtuple[0] = KVC.workspaceTypeMap.get(workTriple[0]) +"::" + sixtuple[0];
			found = true;
			replaced = true;
		}
		if(!found){
			sixtuple[0] = triple[0];
		}
		//looks for replacement possibility for predicate
		if(KVC.workspaceNameMap.containsKey(workTriple[1])){
			sixtuple[1] = KVC.workspaceNameMap.get(workTriple[1]);
			replaced = true;
		}else{
			sixtuple[1] = triple[1];
		}
		//looks for replacement possibility for object
		found=false;
		if(KVC.workspaceNameMap.containsKey(workTriple[2])){
			sixtuple[2] = KVC.workspaceNameMap.get(workTriple[2]);
			found = true;
			replaced = true;
		}
		if(KVC.workspaceTypeMap.containsKey(workTriple[2])){
			sixtuple[2] = KVC.workspaceTypeMap.get(workTriple[2]) +"::" + sixtuple[2];
			found = true;
			replaced = true;
		}
		if(!found){
			sixtuple[2] = triple[2];
		}

		if(replaced){
			for(int i=0;i<3;i++){
				sixtuple[i+3] = triple[i];
			}
			return sixtuple;
		}else{//name description does not exist
			return triple;
		}
	}


	public static void parseModel(Model model){
		StmtIterator stmtIter = model.listStatements();
		while(stmtIter.hasNext()){
			Statement nextStmt = stmtIter.nextStatement();
			if(Objects.equal(nextStmt.getPredicate().toString(),"http://localhost:8181/oslc4jsimulink/services/rdfvocabulary#Block_name")){
				KVC.workspaceNameMap.put(nextStmt.getSubject().toString(), nextStmt.getObject().toString().substring(0, nextStmt.getObject().toString().indexOf("^")));
			}else if(Objects.equal(nextStmt.getPredicate().toString(),"http://localhost:8181/oslc4jsimulink/services/rdfvocabulary#Block_type")){
				KVC.workspaceTypeMap.put(nextStmt.getSubject().toString(), nextStmt.getObject().toString().substring(0, nextStmt.getObject().toString().indexOf("^")));
			}
		}
	}

	//computes difference between two datasets 
	public static SparqlScript diff(File file1, File file2){
		SparqlScript script = new SparqlScript();
		List<String[]> delStmts = new ArrayList<String[]>();
		List<String[]> insStmts = new ArrayList<String[]>();
		boolean found=false;
		Model model1 = ModelFactory.createDefaultModel().read(file1.getAbsolutePath(),Settings.FILEFORMAT);
		Model model2 = ModelFactory.createDefaultModel().read(file2.getAbsolutePath(),Settings.FILEFORMAT);
		StmtIterator iter1 = model1.listStatements();
		StmtIterator iter2 = model2.listStatements();
		//search for triples in first model that are absent in the second -> deletion
		while(iter1.hasNext()){
			Statement stmt1 = iter1.nextStatement();
			iter2 = model2.listStatements();
			while(iter2.hasNext()){
				Statement stmt2 = iter2.nextStatement();
				if(Objects.equal(stmt1, stmt2)){
					found=true;
					break;
				}
			}
			if(!found){
				if(stmt1.getObject() instanceof Resource){
					String[] string = {"<" + stmt1.getSubject().toString() + ">","<" + stmt1.getPredicate().toString() + ">","<" + stmt1.getObject().toString() + ">"};
					delStmts.add(string);
				}else{
					String[] string = {"<" + stmt1.getSubject().toString() + ">","<" + stmt1.getPredicate().toString() + ">","\"" + stmt1.getObject().toString() + "\""};
					delStmts.add(string);	
				}
			}
			found=false;
		}
		//search for triples in second model that are absent in the first -> insertion
		iter2 = model2.listStatements();
		while(iter2.hasNext()){
			Statement stmt2 = iter2.nextStatement();
			iter1 = model1.listStatements();
			while(iter1.hasNext()){
				Statement stmt1 = iter1.nextStatement();
				if(Objects.equal(stmt1, stmt2)){
					found=true;
					break;
				}
			}
			if(!found){
				if(stmt2.getObject() instanceof Resource){
					String[] string = {"<" + stmt2.getSubject().toString() + ">","<" + stmt2.getPredicate().toString() + ">","<" + stmt2.getObject().toString() + ">"};
					insStmts.add(string);
				}else{
					String[] string = {"<" + stmt2.getSubject().toString() + ">","<" + stmt2.getPredicate().toString() + ">","\"" + stmt2.getObject().toString() + "\""};
					insStmts.add(string);	
				}
			}
			found=false;
		}

		script.addDeleteTriples(delStmts);
		script.addInsertTriples(insStmts);

		return script;
	}

	//combines several sparqlscripts to one
	public static SparqlScript combineSparqlScripts(SparqlScript[] scripts){
		SparqlScript newScript = new SparqlScript();
		for(SparqlScript script : scripts){
			newScript.addDeleteTriples(script.getDeleteTriples());
			newScript.addInsertTriples(script.getInsertTriples());
		}
		return newScript;
	}

	//creates file and writes model to it
	public static void model2File(Model model,String fileDir){
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), "utf-8"));
			model.write(writer,Settings.FILEFORMAT);
		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			}
			catch (Exception ex) {
				/*ignore*/
			}
		}
	}

	//generates a sparql insert script for the insertion of a certain triple
	public static String insertDelta(String[] statementAsStr){
		String insertStr ="prefix VCOnt: <http://example.com/VCOnt-directory/>\ninsert {"
				+ statementAsStr[0] + " "
				+ statementAsStr[1] + " "
				+ statementAsStr[2]
						+ "}\n"
						+ "where {}";
		return insertStr;
	}

	//generates a sparql delete script for the deletion of a certain triple
	public static String deleteDelta(String[] statementAsStr){
		String deleteStr = "prefix VCOnt: <http://example.com/VCOnt-directory/>\ndelete {"
				+ statementAsStr[0] + " "
				+ statementAsStr[1] + " "
				+ statementAsStr[2]
						+ "}\n"
						+ "where {}";
		return deleteStr;
	}

	//runs a sparql update script over the input model
	public static void executeSPARQLUpdateQuery(String sparqlUpdate, Model model){
		try {
			UpdateAction.parseExecute(sparqlUpdate,model);
		} catch (Exception e) {
			System.out.println("The update action for statement\n\" " + sparqlUpdate +" \"\ncould not be executed properly.");
		}
	}

	//runs a sparql select script over the input model
	public static void executeSPARQLSelectQuery(String sparqlQuery, Model model) {
		Query query = QueryFactory.create(sparqlQuery);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);

		ResultSet results = qe.execSelect();

		// Output query results
		ResultSetFormatter.out(System.out, results, query);

		// Important - free up resources used running the query
		qe.close();

	}

	//create random 4-digit number
	public static String createHash(List<String> numbers){
		Random randomGenerator = new Random();
		String randomNum = "" + randomGenerator.nextInt(10);
		do{
			for(int x=0;x<3;x++){
				randomNum = randomNum + "" + randomGenerator.nextInt(10);
			}
		}while(numbers.contains(randomNum));
		return randomNum;
	}

	public static List<Resource> getPredecessorResources(Model model, String resourceName){
		List<Resource> outputList = new ArrayList<Resource>();
		StmtIterator iter = model.listStatements();
		while(iter.hasNext()){
			Statement nextStmt = iter.nextStatement();
			if(nextStmt.getObject().toString().contains(resourceName)){
				outputList.add(nextStmt.getSubject());
			}
		}
		return outputList;
	}

	public static List<Object> getSuccessorResources(Model model, String resourceName){
		List<Object> outputList = new ArrayList<Object>();
		StmtIterator iter = model.listStatements();
		while(iter.hasNext()){
			Statement nextStmt = iter.nextStatement();
			if(nextStmt.getSubject().toString().contains(resourceName)){
				outputList.add(nextStmt.getObject());
			}
		}
		return outputList;
	}

}
