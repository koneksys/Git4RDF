package mainPackage;

import visualization.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class TransferFunctions {

	//add data from workspace to index repository
	public static void addData(int number){
		boolean tempFile = false;
		File file1 = new File(KVC.dirLocalRep + KVC.localRepModel.checkModelAttachment(KVC.localRepModel.getCurrentCommit()));
		//specified version of the dataset in the workspace
		File file2 = new File(KVC.dirWorkspace + KVC.workspaceModel.checkModelAttachment(KVC.workspaceModel.getCurrentCommit()));
		if(!(file1.exists())){
			try {
				file1 = new File(KVC.dirLocalRep + File.separator + "File");
				file1.getParentFile().mkdirs();
				file1.createNewFile();
				tempFile = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(file1.exists() && file2.exists()){
			List<String[]> triples = new ArrayList<String[]>();
			List<String[]> tempList = new ArrayList<String[]>();
			SparqlScript script = SparqlUtils.diff(file1, file2);
			if(tempFile) file1.delete();
			if((number<1)||(number>(script.getDeleteTriples().size()+script.getInsertTriples().size()))){
				System.out.println("This triple is not in the index.");
			}else{
				number--;
				if(number<script.getDeleteTriples().size()){
					triples = script.getDeleteTriples();
					tempList.add(triples.get(number));
					KVC.indexScript.addDeleteTriples(tempList);
					tempList.removeAll(tempList);
				}else{
					number = number - script.getDeleteTriples().size();	
					triples = script.getInsertTriples();
					tempList.add(triples.get(number));
					KVC.indexScript.addInsertTriples(tempList);
					tempList.removeAll(tempList);
				}
				System.out.println("Triple added to index:");
				Visualizer.printStatement(SparqlUtils.convertTriple2Statement(triples.get(number)));
			}
		}else{
			System.out.println("Error: Files not found!");
		}
	}

	//remove data from index repository
	public static void removeData(int number){
		if(number>(KVC.indexScript.getDeleteTriples().size()+KVC.indexScript.getInsertTriples().size())){
			System.out.println("This triple is not in the index.");
		}else{
			number--;
			if(number<KVC.indexScript.getDeleteTriples().size()){
				String[] triple = KVC.indexScript.getDeleteTriples().get(number);
				KVC.indexScript.removeDeleteTriple(triple);
				System.out.println("Triple removed from index:");
				Visualizer.printStatement(SparqlUtils.convertTriple2Statement(triple));
			}else{
				number = number - KVC.indexScript.getDeleteTriples().size();
				String[] triple = KVC.indexScript.getInsertTriples().get(number);
				KVC.indexScript.removeInsertTriple(triple);
				System.out.println("Triple removed from index:");
				Visualizer.printStatement(SparqlUtils.convertTriple2Statement(triple));
			}
			System.out.println("Index:");
			Visualizer.displayIndexScript();
		}
	}

	//commits dataset from index repository to local repository
	public static void commitData(){
		if(new File(KVC.dirLocalRep).isDirectory()){
			if((KVC.indexScript.getDeleteTriples().size() + KVC.indexScript.getInsertTriples().size())<1){
				System.out.println("Index is empty, please add changes");
			}else{
				KVC.localRepModel.addCommit(KVC.indexScript);
				KVC.indexScript = new SparqlScript();
				if(Settings.QVF){
					Visualizer.displayModels(KVC.workspaceModel,KVC.localRepModel);
				}else{
					Visualizer.displayModels(KVC.localRepModel);
				}
			}
		}else{
			System.out.println("Local repository is not set up yet");
		}
	}

	//commits dataset from workspace to local repository
	public static void commitAllData(){
		if(new File(KVC.dirLocalRep).isDirectory()){
			boolean tempFile = false;
			//last version of the dataset in the local repository
			File file1 = new File(KVC.dirLocalRep + KVC.localRepModel.checkModelAttachment(KVC.localRepModel.getCurrentCommit()));
			//specified version of the dataset in the workspace
			File file2 = new File(KVC.dirWorkspace + KVC.workspaceModel.checkModelAttachment(KVC.workspaceModel.getCurrentCommit()));
			if(!(file1.exists())){
				try {
					file1 = new File(KVC.dirLocalRep + File.separator + "File");
					file1.getParentFile().mkdirs();
					file1.createNewFile();
					tempFile = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(file1.exists() && file2.exists()){
				KVC.indexScript = SparqlUtils.diff(file1, file2);
				if(tempFile) file1.delete();
				commitData();
			}else{
				System.out.println("Error: Files are empty!");
			}
		}else{
			System.out.println("Local repository is not set up yet");
		}
	}

	//pulls dataset from remote repository to workspace
	//REQUIRES MERGE!!!!!!!!!!!!!!!!!
	/*
	public static void pullData(){
		try {
			File sourceDir = new File(KVC.dirRemoteRep);
			if(sourceDir.isDirectory()){
				File destDir = new File(KVC.dirWorkspace);
				if(destDir.isDirectory()){
					String versionModelFileName = new String();
					//empty directory
					FileUtils.deleteDirectory(destDir);
					destDir = new File(KVC.dirWorkspace);
					destDir.mkdirs();
					//copy directory content
					FileUtils.copyDirectory(sourceDir, destDir);
					//load version model
					KVC.workspaceModel.getVersionModel().removeAll();
					String[] fileNames = destDir.list();
					for(String string : fileNames){
						if(string.contains("versionModel")){
							versionModelFileName = destDir.getAbsolutePath() + File.separator + string;
							break;
						}
					}
					Model model = ModelFactory.createDefaultModel().read(versionModelFileName);
					KVC.workspaceModel.setVersionModel(model);
					if(Settings.QVF){
						Visualizer.displayModels(KVC.workspaceModel,KVC.localRepModel);
					}else{
						Visualizer.displayModels(KVC.workspaceModel);
					}
				}else{
					System.out.println("Dataset could not be transferred");
				}
			}else{
				System.out.println("Dataset could not be loaded");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Dataset could not be pulled from remote repository properly.");
		}
	}
	 */
	/*
	//pushes dataset from local repository to remote repository
	public static void pushData(){
		try {
			File sourceDir = new File(KVC.dirLocalRep);
			if(sourceDir.isDirectory()){
				File destDir = new File(KVC.dirRemoteRep);
				if(destDir.isDirectory()){
					//empty directory
					FileUtils.deleteDirectory(destDir);
					destDir = new File(KVC.dirRemoteRep);
					destDir.mkdirs();
					//copy directory content
					FileUtils.copyDirectory(sourceDir, destDir);
				}else{
					System.out.println("Dataset could not be transferred");
				}
			}else{
				System.out.println("Dataset could not be loaded");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Dataset could not be pushed to remote repository properly.");
		}
	}
	 */
	/*
	//fetches dataset from remote repository to local repository
	public static void fetchData(){
		try {
			File sourceDir = new File(KVC.dirRemoteRep);
			if(sourceDir.isDirectory()){
				File destDir = new File(KVC.dirLocalRep);
				if(destDir.isDirectory()){
					//empty directory
					FileUtils.deleteDirectory(destDir);
					destDir = new File(KVC.dirLocalRep);
					destDir.mkdirs();
					//copy directory content
					FileUtils.copyDirectory(sourceDir, destDir);
				}else{
					System.out.println("Dataset could not be transferred");
				}
			}else{
				System.out.println("Dataset could not be loaded");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Dataset could not be fetched from remote repository properly.");
		}
	}
	 */

	//checks out dataset from index repository to workspace
	public static void checkoutXData(){
		try {
			File sourceDir = new File(KVC.dirLocalRep);
			if(sourceDir.isDirectory()){
				File destDir = new File(KVC.dirWorkspace);
				if(destDir.isDirectory()){
					String versionModelFileName = new String();
					//empty directory
					FileUtils.deleteDirectory(destDir);
					destDir = new File(KVC.dirWorkspace);
					destDir.mkdirs();
					//copy directory content
					FileUtils.copyDirectory(sourceDir, destDir);
					//load version model
					KVC.workspaceModel.getVersionModel().removeAll();
					String[] fileNames = destDir.list();
					for(String string : fileNames){
						if(string.contains("versionModel")){
							versionModelFileName = destDir.getAbsolutePath() + File.separator + string;
							break;
						}
					}
					Model model = ModelFactory.createDefaultModel().read(versionModelFileName);
					KVC.workspaceModel.setVersionModel(model);
					if(Settings.QVF){
						Visualizer.displayModels(KVC.workspaceModel,KVC.localRepModel);
					}else{
						Visualizer.displayModels(KVC.workspaceModel);
					}
				}else{
					System.out.println("Dataset could not be transferred");
				}
			}else{
				System.out.println("Dataset could not be loaded");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Dataset could not be checked out from local repository properly.");
		}
	}

	//checks out dataset from local repository to workspace
	public static void loadLocalRepoData(){
		if((!KVC.initWorkspace)||(!KVC.initLocalRepo)){
			System.out.println("The dataset has not been initialized yet.");
		}else{
			//TODO delete previous script and model files in dirWorkspace
			KVC.workspaceModel = new VersionModel(KVC.dirWorkspace);
			KVC.workspaceModel.initApplicationModel(KVC.localRepModel.getApplicationModel(KVC.localRepModel.getCurrentCommit()));
			if(Settings.QVF){
				Visualizer.displayModels(KVC.workspaceModel,KVC.localRepModel);
			}else{
				Visualizer.displayModels(KVC.workspaceModel);
			}
		}
	}

	//commits dataset from workspace to index repository
	public static void copyDirectories(String sourceDir,String destDir){
		try {
			File sourceFile = new File(sourceDir);
			if(sourceFile.isDirectory()){
				File destFile = new File(destDir);
				if(destFile.isDirectory()){
					//empty directory
					FileUtils.deleteDirectory(destFile);
					destFile = new File(destDir);
					destFile.mkdirs();
					//copy directory content
					FileUtils.copyDirectory(sourceFile, destFile);
					System.out.println("Files were copied succesfully");
				}else{
					System.out.println("Files could not be transferred.");
				}
			}else{
				System.out.println("Files could not be loaded.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Files could not be copied.");
		}
	}

	//commits dataset from workspace to index repository
	public static void cloneDirectories(String sourceDir,String destDir){
		try {
			File sourceFile = new File(sourceDir);
			if(sourceFile.isDirectory()){
				File destFile = new File(destDir);
				if(destFile.isDirectory()){
					KVC.workspaceModel = new VersionModel(KVC.dirWorkspace);
					Model applicationModel = ModelFactory.createDefaultModel().read(KVC.dirLocalRep + KVC.localRepModel.checkModelAttachment(KVC.localRepModel.getCurrentCommit()),Settings.FILEFORMAT);
					KVC.workspaceModel.initApplicationModel(applicationModel);
					System.out.println("Files were copied succesfully");
				}else{
					System.out.println("Files could not be transferred.");
				}
			}else{
				System.out.println("Files could not be loaded.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Files could not be copied.");
		}
	}

	//displays unstaged changes
	public static void statusOutput(){
		//last version of the dataset in the local repository
		File file1 = new File(KVC.dirLocalRep + KVC.localRepModel.checkModelAttachment(KVC.localRepModel.getCurrentCommit()));
		//specified version of the dataset in the workspace
		File file2 = new File(KVC.dirWorkspace + KVC.workspaceModel.checkModelAttachment(KVC.workspaceModel.getCurrentCommit()));
		if(!(file1.exists())){
			try {
				file1 = new File(KVC.dirLocalRep + "File");
				file1.getParentFile().mkdirs();
				file1.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(file1.exists() && file2.exists()){
			int counter = 1;
			SparqlScript sparqlScript = SparqlUtils.diff(file1, file2);
			if((sparqlScript.getDeleteTriples().size()+sparqlScript.getInsertTriples().size())<1){
				System.out.println("All files are up to date.");
				System.out.println("No differences between dataset in working directory and dataset in local repository.");
			}else{
				List<String[]> triples = sparqlScript.getDeleteTriples();
				Iterator<String[]> iter = triples.listIterator();
				while(iter.hasNext()){
					String[] nextTriple = iter.next();
					System.out.print("Deleted triple: " + counter + " ");
					nextTriple = SparqlUtils.modifyTriple(nextTriple);
					if(nextTriple.length==3){
						Visualizer.printStatement(SparqlUtils.convertTriple2Statement(nextTriple));
					}else{
						Visualizer.printTriple(nextTriple);
					}
					counter ++;
				}

				triples = sparqlScript.getInsertTriples();
				iter = triples.listIterator();
				while(iter.hasNext()){
					String[] nextTriple = iter.next();
					System.out.print("Inserted triple: " + counter + " ");
					nextTriple = SparqlUtils.modifyTriple(nextTriple);
					if(nextTriple.length==3){
						Visualizer.printStatement(SparqlUtils.convertTriple2Statement(nextTriple));
					}else{
						Visualizer.printTriple(nextTriple);
					}
					counter++;
				}
			}
		}else{
			System.out.println("Error: Files are empty!");
		}
	}

}
