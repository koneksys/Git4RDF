package mainPackage;

import java.util.List;
import org.apache.commons.io.FileUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.util.Objects;

public class VersionModel {
	private Model model = ModelFactory.createDefaultModel();
	private List<String> commitNumbers = new ArrayList<String>();
	private List<String> displayedModelFileNames = new ArrayList<String>();
	private String currentCommit;
	private File file = null;

	//Constructor*******************************************

	public VersionModel(String dir){
		file = new File(dir + File.separator + "versionModel");
		currentCommit = "0";
		commitNumbers.add(currentCommit);
	}

	//Methods*******************************************

	//checks if a model file is attached to the commit with the input hash
	public String checkModelAttachment(String commitHash){
		String fileName = null;
		StmtIterator iter = this.model.listStatements();
		while(iter.hasNext()){
			Statement nextStmt = iter.nextStatement();
			if(		(nextStmt.getSubject().toString().contains("/commit/" + commitHash)) &&
					(nextStmt.getPredicate().toString().contains("Model"))
					){
				fileName=nextStmt.getObject().toString();
				break;
			}
		}
		return fileName;
	}

	//checks if a delta file is attached to the commit with the input hash
	private String checkDeltaAttachment(String commitHash1, String commitHash2){
		boolean  matchFound = false;
		String fileName = null;
		String deltaResource = null;
		String commitStr1 = "commit/" + commitHash1;
		String commitStr2 = "commit/" + commitHash2;
		List<String> objectList = new ArrayList<String>();
		StmtIterator iter = this.model.listStatements();
		while(iter.hasNext()){
			Statement nextStmt = iter.nextStatement();
			if(		(	(nextStmt.getSubject().toString().contains(commitStr1)) ||
					(nextStmt.getSubject().toString().contains(commitStr2)) ) &&
					(nextStmt.getPredicate().toString().contains("Delta"))
					){
				objectList.add(nextStmt.getObject().toString());
			}
		}
		//if two objectItems are equal, the object is the mutual delta resource, that the two commit resources are linked to
		for(int counter1=0;counter1<(objectList.size()-1);counter1++){
			for(int counter2=(counter1+1);counter2<objectList.size();counter2++){
				if((Objects.equals(objectList.get(counter1), objectList.get(counter2)))){
					deltaResource=objectList.get(counter1);
					matchFound=true;
					break;
				}
			}
			if(matchFound) break;
		}
		iter = this.model.listStatements();
		while(iter.hasNext()){
			Statement nextStmt = iter.nextStatement();
			if(		(nextStmt.getSubject().toString().contains(deltaResource)) &&
					(nextStmt.getPredicate().toString().contains("Script"))
					){
				fileName = nextStmt.getObject().toString();
			}
		}
		return fileName;
	}

	//executes all sparql files to the desired commit to create the desired displayed model
	private Model executeSparqlFiles(String firstCommit, String desiredCommit){
		Model displayedModel = ModelFactory.createDefaultModel();
		String fileName = checkModelAttachment(firstCommit);
		if(fileName!=null){
			displayedModel.read(this.file.getParentFile().getAbsolutePath() + File.separator + fileName,Settings.FILEFORMAT);
			String commitSearch = firstCommit;
			String lastCommit = null;
			fileName = null;
			while(!(Objects.equals(commitSearch, desiredCommit))){
				StmtIterator stmtIter = this.model.listStatements();
				while(stmtIter.hasNext()){
					Statement nextStmt = stmtIter.nextStatement();
					if(		(nextStmt.getSubject().toString().contains(commitSearch)) &&
							(nextStmt.getPredicate().toString().contains("backwardStream"))
							){
						lastCommit = commitSearch;
						commitSearch = nextStmt.getObject().toString().substring(26);
						fileName = checkDeltaAttachment(lastCommit,commitSearch);
						File currentFile = new File(this.file.getParentFile().getAbsolutePath() + File.separator + fileName);
						try {
							if(currentFile.exists()){
								String sparqlString = FileUtils.readFileToString(currentFile);
								SparqlUtils.executeSPARQLUpdateQuery(sparqlString, displayedModel);
							}else{
								System.out.println("Sparql file could not be executed properly.");
							}
						} catch (IOException e) {
							//
							e.printStackTrace();
						}
						break;
					}
				}
			}
		}else{
			displayedModel = null;
		}
		return displayedModel;
	}

	//adds additional metadata triples to the model
	private void addModificationDescriptions2Model(CommitModificationMemory cmm, String deltaResourceURI, String lastCommit, String currentCommit){

		int tripleCounter = 0;
		int deleteTripleCounter = 0;
		int insertTripleCounter = 0;
		int modificationTripleCounter = 0;
		List<String[]> deleteTriples = cmm.getDeleteTriples();
		List<String[]> insertTriples = cmm.getInsertTriples();
		List<String[]> modificationTriples = cmm.getModifiedTriples();

		String[] statementResourceStr = new String[(deleteTriples.size()+insertTriples.size()+modificationTriples.size())];

		while(tripleCounter<(deleteTriples.size()+insertTriples.size()+modificationTriples.size())){
			//create statement resource with modification information and append to delta resource
			if(statementResourceStr.length==1){
				statementResourceStr[tripleCounter] = "http://example.org/statement/" + lastCommit + "-" + currentCommit;	
			}else{
				statementResourceStr[tripleCounter] = "http://example.org/statement/" + lastCommit + "-" + currentCommit + "-" + tripleCounter;
			}
			//Deletion
			if(deleteTripleCounter<(deleteTriples.size())){
				//insert delta description triple
				String[] deltaInformation = {"<"+deltaResourceURI+"> ", "<"+VCOnt.Deletion.toString()+"> ", "<"+statementResourceStr[tripleCounter]+"> ."};
				SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(deltaInformation), this.model);
				//link statement resource to statement parts (subject, predicate, object)
				String[] statementSubjectStr = {"<"+statementResourceStr[tripleCounter]+"> ","<"+VCOnt.Subject.toString()+"> ",deleteTriples.get(deleteTripleCounter)[0]+" ."};
				SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(statementSubjectStr), this.model);
				String[] statementPredicateStr = {"<"+statementResourceStr[tripleCounter]+"> ","<"+VCOnt.Predicate.toString()+"> ",deleteTriples.get(deleteTripleCounter)[1]+" ."};
				SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(statementPredicateStr), this.model);
				String[] statementObjectStr = {"<"+statementResourceStr[tripleCounter]+"> ","<"+VCOnt.Object.toString()+"> ",deleteTriples.get(deleteTripleCounter)[2]+" ."};
				SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(statementObjectStr), this.model);
				tripleCounter++;
				deleteTripleCounter++;
			}else if(insertTripleCounter<(insertTriples.size())){//Insertion
				//insert delta description triple
				String[] deltaInformation = {"<"+deltaResourceURI+"> ", "<"+VCOnt.Insertion.toString()+"> ", "<"+statementResourceStr[tripleCounter]+"> ."};
				SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(deltaInformation), this.model);
				//link statement resource to statement parts (subject, predicate, object)
				String[] statementSubjectStr = {"<"+statementResourceStr[tripleCounter]+"> ","<"+VCOnt.Subject.toString()+"> ",insertTriples.get(insertTripleCounter)[0]+" ."};
				SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(statementSubjectStr), this.model);
				String[] statementPredicateStr = {"<"+statementResourceStr[tripleCounter]+"> ","<"+VCOnt.Predicate.toString()+"> ",insertTriples.get(insertTripleCounter)[1]+" ."};
				SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(statementPredicateStr), this.model);
				String[] statementObjectStr = {"<"+statementResourceStr[tripleCounter]+"> ","<"+VCOnt.Object.toString()+"> ",insertTriples.get(insertTripleCounter)[2]+" ."};
				SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(statementObjectStr), this.model);
				tripleCounter++;
				insertTripleCounter++;
			}else if(modificationTripleCounter<(modificationTriples.size())){//Modification
				//insert delta description triple
				String[] deltaInformation1 = {"<"+deltaResourceURI+"> ","<"+VCOnt.Modification.toString()+"> ","<"+statementResourceStr[tripleCounter]+"-D> ."};
				String[] deltaInformation2 = {"<"+deltaResourceURI+"> ","<"+VCOnt.Modification.toString()+"> ","<"+statementResourceStr[tripleCounter]+"-I> ."};
				SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(deltaInformation1), this.model);
				SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(deltaInformation2), this.model);
				//link statement resource to statement parts (subject, predicate, object)
				String[] statement1SubjectStr = {"<"+statementResourceStr[tripleCounter]+"-D> ","<"+VCOnt.Subject.toString()+"> ",modificationTriples.get(modificationTripleCounter)[0]+" ."};
				SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(statement1SubjectStr), this.model);
				String[] statement1PredicateStr = {"<"+statementResourceStr[tripleCounter]+"-D> ","<"+VCOnt.Predicate.toString()+"> ",modificationTriples.get(modificationTripleCounter)[1]+" ."};
				SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(statement1PredicateStr), this.model);
				String[] statement1ObjectStr = {"<"+statementResourceStr[tripleCounter]+"-D> ","<"+VCOnt.Object.toString()+"> ",modificationTriples.get(modificationTripleCounter)[2]+" ."};
				SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(statement1ObjectStr), this.model);
				modificationTripleCounter++;
				String[] statement2SubjectStr = {"<"+statementResourceStr[tripleCounter]+"-I> ","<"+VCOnt.Subject.toString()+"> ",modificationTriples.get(modificationTripleCounter)[0]+" ."};
				SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(statement2SubjectStr), this.model);
				String[] statement2PredicateStr = {"<"+statementResourceStr[tripleCounter]+"-I> ","<"+VCOnt.Predicate.toString()+"> ",modificationTriples.get(modificationTripleCounter)[1]+" ."};
				SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(statement2PredicateStr), this.model);
				String[] statement2ObjectStr = {"<"+statementResourceStr[tripleCounter]+"-I> ","<"+VCOnt.Object.toString()+"> ",modificationTriples.get(modificationTripleCounter)[2]+" ."};
				SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(statement2ObjectStr), this.model);
				tripleCounter++;
				modificationTripleCounter++;
			}else
				//Finish loop
				tripleCounter++;
		}
	}

	/**
	 * attaches baseline tag to certain commit
	 * @param commitHash
	 */
	public void createBaselineTag(String commitHash){
		String currentCommitURI = "http://example.org/commit/" + commitHash;
		String baselineString = "http://example.org/baseline/" + commitHash;
		String[] inputString = {"<"+currentCommitURI+"> ","<"+VCOnt.Baseline.toString()+"> ","\""+baselineString+"\"."};
		SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(inputString), this.model);
		//write to version file
		SparqlUtils.model2File(this.model, file.getAbsolutePath());
	}

	/**
	 * creates new model file attached to the last commit of the respective branch
	 * @param model
	 */
	public void addBranch(String commitHash){
		if(this.commitNumbers.contains(commitHash)){
			String fileName = checkModelAttachment(commitHash);
			if(Objects.equals(fileName,null)){
				DisplayedModel newModel = new DisplayedModel(getApplicationModel(commitHash),this.file.getParentFile().getAbsolutePath(),this.displayedModelFileNames);
				fileName = newModel.getFileName();
				this.displayedModelFileNames.add(newModel.getModelIdentifier());
				this.currentCommit = commitHash;
				String commitResourceURI = "http://example.org/commit/" + currentCommit;
				String[] commitTriple = {"<" + commitResourceURI + ">","<" + VCOnt.Model.toString() + ">","\"" + fileName + "\""};
				SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(commitTriple), this.model);
				//write to version file
				SparqlUtils.model2File(getVersionModel(), file.getAbsolutePath());
			}else{
				this.currentCommit = commitHash;
			}
		}else{
			System.out.println("Commit number does not exist. Hash could not be executed");
		}
	}

	/**
	 * updates version model with new commit information
	 * @param displayedModel
	 * @param sparqlScript
	 * @param lastCommit
	 */
	public void addCommit(SparqlScript sparqlScript){

		//get file name of model file
		String modelFileName = checkModelAttachment(this.currentCommit);

		if(!(Objects.equals(modelFileName, null))){

			//*************version model***************

			//creation of hashed commit number; add to number list;
			String lastCommit = this.currentCommit;
			this.currentCommit = SparqlUtils.createHash(this.commitNumbers);
			this.commitNumbers.add(this.currentCommit);
			//create commitResource and append to previous commit
			String currentCommitURI = "http://example.org/commit/" + this.currentCommit;
			String lastCommitURI = "http://example.org/commit/" + lastCommit;	
			//link last commit and new commit
			String[] commitString = {"<"+currentCommitURI+"> ","<"+VCOnt.backwardStream.toString()+"> ","<"+lastCommitURI+">."};
			SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(commitString), this.model);

			//create deltaResource and append to commit resources
			String deltaResourceURI = "http://example.org/delta/" + lastCommit + "-" + this.currentCommit;
			String[] deltaTriple1 = {"<"+lastCommitURI+"> ","<"+VCOnt.Delta.toString()+"> ","<"+deltaResourceURI+">"};
			String[] deltaTriple2 = {"<"+currentCommitURI+"> ","<"+VCOnt.Delta.toString()+"> ","<"+deltaResourceURI+">"};

			SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(deltaTriple1), this.model);
			SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(deltaTriple2), this.model);

			//create commit information and append to new commit resource
			String[] commitInformation = {"<"+currentCommitURI+"> ","<"+VCOnt.authorID.toString()+"> ","\"AuthorID: " + Settings.authorID + "\""};
			SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(commitInformation), this.model);

			//create new filename
			String sparqlFileName = "File-pathToJavaProject+Script-" + lastCommit + "-" + this.currentCommit;

			//create delta triple with text file name as literal 
			String[] scriptTriple = {"<"+deltaResourceURI+"> ","<"+VCOnt.Script.toString()+"> ","\""+sparqlFileName+"\" ."};
			//execute sparql script to insert delta triple
			SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(scriptTriple), this.model);

			//create new text file
			//convert filename into directory path
			sparqlFileName = this.file.getParentFile().getAbsolutePath() + File.separator + sparqlFileName;
			//write sparql-script to text file	
			Writer writer = null;
			try {
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sparqlFileName), "utf-8"));
				writer.write(sparqlScript.getReverseSparqlString());
			} catch (IOException ex) {
				System.out.println("Write action could not be executed properly.");
			} finally {
				try {
					writer.close();
				}
				catch (Exception ex) {
					System.out.println("Writer could not be closed properly.");
				}
			}

			//analyse commit modifications and append to delta resource
			CommitModificationMemory cmm = new SparqlScriptAnalyser(sparqlScript).getCommitModificationMemory();
			addModificationDescriptions2Model(cmm,deltaResourceURI,lastCommit,this.currentCommit);

			//delete previous links to model file name
			StmtIterator iter = this.model.listStatements();
			while(iter.hasNext()){
				Statement nextStmt = iter.nextStatement();
				if(nextStmt.getObject().toString().contains(modelFileName)){
					this.model.remove(nextStmt);
					break;
				}
			}

			//link model file name to current commit resource
			//create delta triple with text file name as literal 
			String[] modelTriple = {"<"+currentCommitURI+"> ","<"+VCOnt.Model.toString()+"> ","\""+modelFileName+"\" ."};
			//execute sparql script to insert delta triple
			SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(modelTriple), this.model);
			//write to version file
			SparqlUtils.model2File(getVersionModel(), file.getAbsolutePath());

			//*************displayed model***************

			//update displayed model by executing the sparql script on  the model to get the latest model version
			Model displayedModel = getApplicationModel(lastCommit);
			SparqlUtils.executeSPARQLUpdateQuery(sparqlScript.getSparqlString(), displayedModel);
			SparqlUtils.model2File(displayedModel, this.file.getParentFile().getAbsolutePath() + File.separator + modelFileName);
		}
	}

	/**
	 * updates version model with new merge commit information
	 * @param displayedModel
	 * @param sparqlScript
	 * @param lastCommit
	 */
	public void addMergeCommit(String mergeCommit1, String mergeCommit2){

		//*************displayed model***************

		//get models
		Model model1 = getApplicationModel(mergeCommit1);
		Model model2 = getApplicationModel(mergeCommit2);
		Model differenceModel = ModelFactory.createDefaultModel();
		//create difference model as AND-set of both models
		differenceModel.add(model1.listStatements());
		StmtIterator iter = model2.listStatements();
		while(iter.hasNext()){
			Statement nextStmt = iter.nextStatement();
			if(!(differenceModel.contains(nextStmt))){
				differenceModel.add(nextStmt);
			}
		}

		//update displayed model by executing the sparql script on  the model to get the latest model version
		DisplayedModel displayedModel = new DisplayedModel(differenceModel,this.file.getParentFile().getAbsolutePath(),this.displayedModelFileNames);
		String modelFileName = displayedModel.getFileName();
		this.displayedModelFileNames.add(displayedModel.getModelIdentifier());

		//*************version model***************

		this.currentCommit = SparqlUtils.createHash(this.commitNumbers);
		this.commitNumbers.add(this.currentCommit);
		//create commitResource and append to previous commit
		String currentCommitURI = "http://example.org/commit/" + this.currentCommit;
		String mergeCommit1URI = "http://example.org/commit/" + mergeCommit1;
		String mergeCommit2URI = "http://example.org/commit/" + mergeCommit2;
		//link last commit and new commit
		String[] commitString1 = {"<"+currentCommitURI+"> ","<"+VCOnt.backwardStream.toString()+"> ","<"+mergeCommit1URI+">."};
		SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(commitString1), this.model);
		String[] commitString2 = {"<"+currentCommitURI+"> ","<"+VCOnt.backwardStream.toString()+"> ","<"+mergeCommit2URI+">."};
		SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(commitString2), this.model);

		//link model file name to current commit resource
		//create delta triple with text file name as literal 
		String[] modelTriple = {"<"+currentCommitURI+"> ","<"+VCOnt.Model.toString()+"> ","\""+modelFileName+"\" ."};
		//execute sparql script to insert delta triple
		SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(modelTriple), this.model);
		//write to version file
		SparqlUtils.model2File(getVersionModel(), file.getAbsolutePath());
	}

	/**
	 * returns version model
	 * @return
	 */
	public Model getVersionModel(){
		return this.model;
	}

	/**
	 * returns displayed model of the desired commit by executing sparql files
	 */
	public Model getApplicationModel(String commitHash){
		Model displayedModel = ModelFactory.createDefaultModel();
		if(this.commitNumbers.contains(commitHash)){
			String fileName = checkModelAttachment(commitHash);
			if(fileName!=null){
				displayedModel.read(this.file.getParentFile().getAbsolutePath() + File.separator + fileName,Settings.FILEFORMAT);
			}else{//delta files must be executed
				//search graph for a displayed model file
				String firstCommit = getLatestCommit(commitHash);
				displayedModel = executeSparqlFiles(firstCommit,commitHash);
			}
		}else{
			displayedModel = null;
		}
		return displayedModel;
	}

	/**
	 * returns current commit hash
	 */
	public String getCurrentCommit(){
		return this.currentCommit;
	}

	/**
	 * sets current commit hash
	 */
	public void setCurrentCommit(String commitNumber){
		if(commitNumber.length()==4){
			this.currentCommit = commitNumber;
		}
	}

	/**
	 * searches for the last commit of a branch. This commit should have an attached model file
	 * @param commitHash
	 * @return
	 */
	public String getLatestCommit(String commitHash){
		boolean foundStatement = true;
		String commitSearch = commitHash;
		while(foundStatement){
			foundStatement=false;
			StmtIterator stmtIter = this.model.listStatements();
			while(stmtIter.hasNext()){
				Statement nextStmt = stmtIter.nextStatement();
				if(		nextStmt.getPredicate().toString().contains("backwardStream") &&
						nextStmt.getObject().toString().contains(commitSearch)){
					commitSearch = nextStmt.getSubject().toString().substring(26);
					foundStatement=true;
					break;
				}
			}
		}
		return commitSearch;
	}

	/**
	 * returns branch root
	 */
	public String getFirstCommit(String currentCommitHash){
		String firstCommitHash = new String();
		StmtIterator iter = this.model.listStatements();
		while(iter.hasNext()){
			Statement nextStmt = iter.nextStatement();
			if(		(nextStmt.getSubject().toString().contains(currentCommitHash)) &&
					(nextStmt.getPredicate().toString().contains("backwardStream"))
					){
				firstCommitHash = nextStmt.getObject().toString().substring(26);
				break;
			}
		}
		return firstCommitHash;
	}

	/**
	 * reset version model
	 * @param inputModel
	 */
	public void setVersionModel(Model inputModel){
		this.model = inputModel;
	}

	/**
	 * returns list of all commit hashes
	 */
	public List<String> getCommitNumbers(){
		return this.commitNumbers;
	}

	/**
	 * returns directory path of version model file
	 * @return
	 */
	public String getFilename(){
		return this.file.getAbsolutePath();
	}

	/**
	 * returns repository type of version model file
	 * @return
	 */
	public String getRepo(){
		String name = new String();
		String dirName = this.file.getParentFile().getAbsolutePath();
		if(dirName.endsWith("/")){
			dirName.substring(0, dirName.lastIndexOf("/"));
		}
		name = dirName.substring(dirName.lastIndexOf("/"));
		if(name.startsWith("/")){
			name = name.substring(1);
		}
		return name;
	}

	/**
	 * update model with file information
	 * @param file
	 */
	public boolean loadModel(File file){
		boolean modelLoaded = false;
		try {
			Model fileModel = ModelFactory.createDefaultModel().read(file.getAbsolutePath(),Settings.FILEFORMAT);
			this.model.removeAll();
			this.model.add(fileModel.listStatements());
			modelLoaded = true;
		} catch (Exception e) {
			System.out.println("Can not read file");
		}
		if(modelLoaded){
			StmtIterator iter = this.model.listStatements();
			this.commitNumbers.removeAll(this.commitNumbers);
			while(iter.hasNext()){
				Statement nextStmt = iter.nextStatement();
				String subject = nextStmt.getSubject().toString();
				if(subject.contains("commit/")){
					String commit = subject.substring(subject.indexOf("commit/")+"commit/".length());
					if(!(this.commitNumbers.contains(commit))){
						this.commitNumbers.add(commit);
					}
				}
			}
			iter = this.model.listStatements();
			this.displayedModelFileNames.removeAll(this.displayedModelFileNames);
			while(iter.hasNext()){
				Statement nextStmt = iter.nextStatement();
				String object = nextStmt.getObject().toString();
				if(object.contains("File-displayedModel")){
					String modelIdentifier = object.substring(object.indexOf("File-displayedModel")+"File-displayedModel".length());
					if(!(this.displayedModelFileNames.contains(modelIdentifier))){
						this.displayedModelFileNames.add(modelIdentifier);
					}
				}
			}
			this.currentCommit = getLatestCommit("0");
			this.file = file;
			return true;
		}else{
			return false;
		}
	}

	/**
	 * initializes dataset with input model
	 * @param inputModel
	 */
	public void initApplicationModel(Model inputModel){
		DisplayedModel displayedModel = new DisplayedModel(inputModel,this.file.getParentFile().getAbsolutePath() + "/",this.displayedModelFileNames);
		String fileName = displayedModel.getFileName();
		this.displayedModelFileNames.add(displayedModel.getModelIdentifier());
		String commitResourceURI = "http://example.org/commit/" + this.currentCommit;
		String[] commitTriple = {"<" + commitResourceURI + ">","<" + VCOnt.Model.toString() + ">","\"" + fileName + "\""};
		SparqlUtils.executeSPARQLUpdateQuery(SparqlUtils.insertDelta(commitTriple), this.model);
		//write to version file
		SparqlUtils.model2File(this.model, this.file.getAbsolutePath());
	}
}
