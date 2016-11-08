package mainPackage;

import visualization.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.google.common.base.Objects;

public class CommandLineHandler {

	public static void executeCommand(String commandStr){

		String[] command = {null, null, null, null, null};

		//Make sure that command array has at least length 5
		String[] commandInput = parseCommand(commandStr);
		int number = commandInput.length;
		if(number<5){
			for(int i=0;i<number;i++){
				command[i]=commandInput[i];
			}
		}else{
			command = commandInput;
		}

		if(Objects.equal(command[0],null)){
			System.out.println("This is not a valid command. Please insert a different one.");
		}else{
			switch(command[0]){
			case "help":
				System.out.print(Scripts.help);
				break;
			case "config":
				if((number<2)||(Objects.equal(command[1], null))){
					System.out.println("The command elements are not sufficient for a config operation");
				}else{
					String inputString = new String();
					switch(command[1]){
					case "user.name":
						try {
							inputString = command[2].substring(command[2].indexOf("\"")+1, command[2].lastIndexOf("\""));
							Settings.authorID = inputString;
							System.out.println("Author ID: " + Settings.authorID);
						} catch (Exception e) {
							System.out.println("Please insert the argument within \"quotation marks\".");
						}
						break;
					case "rdfFormat":
						//TODO robust error 
						try {
							inputString = command[2].substring(command[2].indexOf("\"")+1, command[2].lastIndexOf("\""));
							if(Settings.formats.contains(inputString)){
								Settings.FILEFORMAT = inputString;
								System.out.println("File format: " + Settings.FILEFORMAT);
							}else{
								System.out.println("This format is not supported.");
							}
						} catch (Exception e) {
							System.out.println("Please insert the argument within \"quotation marks\".");
						}
						break;
					case "QVF":
						try {
							inputString = command[2].substring(command[2].indexOf("\"")+1, command[2].lastIndexOf("\""));
							if((Objects.equal(inputString, "true"))||(Objects.equal(inputString, "false"))){
								Settings.QVF = Boolean.parseBoolean(inputString);
								System.out.println("QVF: " + Settings.QVF);
							}else{
								System.out.println("This is not a valid assignment for the configuration of quarter visualization");
							}
						} catch (Exception e) {
							System.out.println("Please insert the argument within \"quotation marks\".");
						}
						break;
					default:
						System.out.println("The config operation could not be executed, please insert different arguments");
						break;
					}
				}
				break;
			case "C":
				String temp = new String();
				if(Objects.equal(command[1], null)){
					temp = new File("").getAbsolutePath() + File.separator + "Workspace";
				}else{
					try {
						temp = command[1].substring(command[1].indexOf("<")+1, command[1].lastIndexOf(">"));
					} catch (Exception e) {
						System.out.println("Please enter file name in <angle brackets>");
						return;
					}
				}

				File file = new File(temp + File.separator + "versionModel");
				if(file.exists()){
					try {
						Model tempModel = ModelFactory.createDefaultModel().read(file.getAbsolutePath(),Settings.FILEFORMAT);
						KVC.dirWorkspace = temp;
						if(Objects.equal(KVC.workspaceModel, null)){
							KVC.workspaceModel = new VersionModel(KVC.dirWorkspace);
						}
						KVC.workspaceModel.getVersionModel().removeAll();
						KVC.workspaceModel.getVersionModel().add(tempModel.listStatements());
						System.out.println("Workspace adopted: " + KVC.dirWorkspace);
						KVC.initWorkspace = true;
					} catch (Exception e) {
						System.out.println("Can not read file");
					}
				}else{
					if(temp.endsWith(File.separator)){
						temp = temp + "Workspace";
					}else{
						temp = temp + File.separator + "Workspace";
					}
					KVC.initializeWorkspace(temp);
				}
				if(!(KVC.dirLocalRep.endsWith(File.separator))){
					KVC.dirLocalRep = KVC.dirLocalRep + File.separator;
				}
				if(!(KVC.dirWorkspace.endsWith(File.separator))){
					KVC.dirWorkspace = KVC.dirWorkspace + File.separator;
				}
				break;
			case "init":
				temp = new String();
				if(Objects.equal(command[1], null)){
					temp = new File("").getAbsolutePath() + File.separator + "Workspace";
				}else{
					try {
						temp = command[1].substring(command[1].indexOf("<")+1, command[1].lastIndexOf(">"));
					} catch (Exception e) {
						System.out.println("Please enter file name in <angle brackets>.");
						return;
					}
				}

				if(new File(temp).isDirectory()){
					if(temp.endsWith(File.separator)){
						temp = temp + "LocalRepository";
					}else{
						temp = temp + File.separator + "LocalRepository";
					}
					file = new File(temp);
					if(file.exists()){
						System.out.println("A Local Repository folder already exists in this directory.");
						System.out.println("Do you want to overwrite the folder?\n(y)\n(n)");
						String decider = KVC.scanner.nextLine();
						if(Objects.equal(decider, "y")){
							try {
								FileUtils.deleteDirectory(file);
								file = new File(temp);
								KVC.initializeLocalRepository(temp);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}else{
							System.out.println("Init command could not be executed.");
						}
					}else{
						KVC.initializeLocalRepository(temp);
					}
				}else{
					System.out.println("The directory path does not exist.");
				}
				if(!(KVC.dirLocalRep.endsWith(File.separator))){
					KVC.dirLocalRep = KVC.dirLocalRep + File.separator;
				}
				if(!(KVC.dirWorkspace.endsWith(File.separator))){
					KVC.dirWorkspace = KVC.dirWorkspace + File.separator;
				}
				break;
			case "load":
				if(!KVC.initWorkspace){
					System.out.println("Workspace is not set up properly");
				}else{
					if(Objects.equal(command[1], null)){
						System.out.println("This is not a valid command. Please insert a different one.");
					}else if(Objects.equal(command[1], "local")){
						TransferFunctions.loadLocalRepoData();
					}else{
						String inputString = new String();
						try {
							if(Objects.equal(command[1], "example")){
								inputString = System.getProperty("user.dir") + File.separator + "src" + File.separator + "additional" + File.separator + "RDFdocuments" + File.separator + "ExampleDataset.txt";
							}else{
								inputString = command[1].substring(command[1].indexOf("<")+1, command[1].lastIndexOf(">"));
							}
							file = new File(inputString);
							if(file.exists()){
								boolean success = false;
								Model addedModel = ModelFactory.createDefaultModel();
								if(file.getAbsolutePath().endsWith(".txt")){
									try {
										addedModel.read(file.getAbsolutePath(),Settings.FILEFORMAT);
										success = true;
									} catch (Exception e) {
										System.out.println("Could not import file content into RDF dataset.");
									}
								}else if(file.getAbsolutePath().endsWith(".rdf")){
									try {
										addedModel.read(file.getAbsolutePath());
										success = true;
									} catch (Exception e) {
										System.out.println("Could not import file content into RDF dataset.");
									}
								}
								if(success){
									Model currentApplicationModel = KVC.workspaceModel.getApplicationModel(KVC.workspaceModel.getCurrentCommit());
									currentApplicationModel.add(addedModel.listStatements());
									SparqlUtils.model2File(currentApplicationModel, KVC.dirWorkspace + KVC.workspaceModel.checkModelAttachment(KVC.workspaceModel.getCurrentCommit()));
									SparqlUtils.parseModel(currentApplicationModel);
									System.out.println("Model loaded into workspace:");
									Visualizer.printModel(currentApplicationModel);
								}
							}else{
								System.out.println("This file does not exist.");
							}
						} catch (Exception e) {
							System.out.println("Please enter file name in <angle brackets>");
						}
					}
				}
				break;
			case "clone":
				System.out.println("Not implemented yet.");
				//				TransferFunctions.cloneDirectories(KVC.dirLocalRep, KVC.dirWorkspace);
				break;
			case "dir":
				if(Objects.equal(command[1], null)){
					System.out.println("This is not a valid command. Please insert a different one.");
				}else{
					try {
						String inputString = command[1].substring(command[1].indexOf("<")+1, command[1].lastIndexOf(">"));
						file = new File(inputString + File.separator + "versionModel");
						if(file.exists()){
							if(Objects.equal(KVC.localRepModel, null)){
								KVC.localRepModel = new VersionModel(inputString);
							}
							if(KVC.localRepModel.loadModel(file)){
								String repoDir = KVC.localRepModel.getFilename();
								if(repoDir.endsWith(File.separator)){
									KVC.dirLocalRep = repoDir.substring(0,repoDir.lastIndexOf(File.separator));
									KVC.dirLocalRep = repoDir.substring(0,repoDir.lastIndexOf(File.separator));
								}else{
									KVC.dirLocalRep = repoDir.substring(0,repoDir.lastIndexOf(File.separator));
								}
								KVC.initLocalRepo = true;
								System.out.println("Changed repository directory: " + KVC.dirLocalRep);
							}
						}else{
							System.out.println("The inserted file name does not point to a KLD repository");
						}
					} catch (Exception e) {
						System.out.println("Please enter file name in <angle brackets>");
					}
				}
				if(!(KVC.dirLocalRep.endsWith(File.separator))){
					KVC.dirLocalRep = KVC.dirLocalRep + File.separator;
				}
				if(!(KVC.dirWorkspace.endsWith(File.separator))){
					KVC.dirWorkspace = KVC.dirWorkspace + File.separator;
				}
				break;
			case "pwd":
				String currentDirectory = new File("").getAbsolutePath();
				System.out.println(currentDirectory);
				break;
			case "gui":
				if((!KVC.initWorkspace)||(!KVC.initLocalRepo)){
					System.out.println("Working environment is not set up properly");
				}else{
					if(number<2){
						if(Settings.QVF){
							Visualizer.visualizationTool(KVC.workspaceModel, KVC.workspaceModel.getCurrentCommit(),KVC.localRepModel, KVC.localRepModel.getCurrentCommit());
						}else{
							System.out.println("Please select, if you want to see the \"workspace\" or the \"localrepo\".");
						}
					}else if(number<3){
						if(Objects.equal(command[1], null)){
							System.out.println("Please select, if you want to see the \"workspace\" or the \"localrepo\".");
						}else{
							if(Objects.equal(command[1], "workspace")){
								Settings.QVF = false;
								Visualizer.visualizationTool(KVC.workspaceModel, KVC.workspaceModel.getCurrentCommit());
							}else if(Objects.equal(command[1], "localrepo")){
								Settings.QVF = false;
								Visualizer.visualizationTool(KVC.localRepModel, KVC.localRepModel.getCurrentCommit());
							}else{
								System.out.println("Please select, if you want to see the \"workspace\" or the \"localrepo\".");
							}
						}
					}else{
						System.out.println("Incorrect command!");
					}
				}
				break;
			case "index":
				Visualizer.displayIndexScript();
				break;
			case "add":
				if((!KVC.initWorkspace)||(!KVC.initLocalRepo)){
					System.out.println("The local repository has not been initialized yet.");
				}else{
					if((number<2)||(Objects.equal(command[1], null))){
						System.out.println("This is not a valid command. Please insert a different one.");
					}else if(Objects.equal(command[1],"a")){
						File file1 = new File(KVC.dirLocalRep + KVC.localRepModel.checkModelAttachment(KVC.localRepModel.getCurrentCommit()));
						//specified version of the dataset in the workspace
						File file2 = new File(KVC.dirWorkspace + KVC.workspaceModel.checkModelAttachment(KVC.workspaceModel.getCurrentCommit()));
						if(file1.exists() && file2.exists()){
							KVC.indexScript = SparqlUtils.diff(file1, file2);
						}
					}else{
						try {
							int tripleNumber = Integer.parseInt(command[1]);
							TransferFunctions.addData(tripleNumber);
						} catch (NumberFormatException e) {
							System.out.println("This is not a valid command. Please insert a different one.");
						}
					}
				}
				break;
			case "rm":
				if((!KVC.initWorkspace)||(!KVC.initLocalRepo)){
					System.out.println("The local repository has not been initialized yet.");
				}else{
					if((KVC.indexScript.getDeleteTriples().size()+KVC.indexScript.getInsertTriples().size())<0){
						System.out.println("Index is empty");
					}else{
						if((number<2)||(Objects.equal(command[1], null))){
							System.out.println("This is not a valid command. Please insert a different one.");
						}else if(Objects.equal(command[1],"a")){
							KVC.indexScript = new SparqlScript();
							System.out.println("Index repository is emptied.");
						}else{
							try {
								int tripleNumber = Integer.parseInt(command[1]);
								TransferFunctions.removeData(tripleNumber);
							} catch (NumberFormatException e) {
								System.out.println("This is not a valid command. Please insert a different one.");
							}
						}
					}
				}
				break;
			case "reset":
				System.out.println("Not implemented yet.");
				break;
			case "tag":
				if((!KVC.initWorkspace)||(!KVC.initLocalRepo)){
					System.out.println("The dataset has not been initialized yet.");
				}else{
					KVC.tagCommit(KVC.localRepModel);
				}
				break;
			case "branch":
				if((!KVC.initWorkspace)||(!KVC.initLocalRepo)){
					System.out.println("The dataset has not been initialized yet.");
				}else{
					Visualizer.listBranches();
				}
				break;
			case "merge":
				if((!KVC.initWorkspace)||(!KVC.initLocalRepo)){
					System.out.println("The dataset has not been initialized yet.");
				}else if((number<2)||(Objects.equal(command[1], null))){
					KVC.mergeModels(KVC.localRepModel);
				}else{
					String mergeCommit = command[1].substring(command[1].indexOf("[")+1,command[1].lastIndexOf("]"));
					if(KVC.localRepModel.getCommitNumbers().contains(mergeCommit)){
						KVC.mergeModels(KVC.localRepModel,KVC.localRepModel.getCurrentCommit(),mergeCommit);
					}else{
						System.out.println("Commit is not included in repository model");
					}
				}
				break;
			case "push":
				System.out.println("Not implemented yet.");
				//				if((!KVC.initWorkspace)||(!KVC.initLocalRepo)){
				//					System.out.println("The dataset has not been initialized yet.");
				//				}else{
				//					TransferFunctions.pushData();
				//				}
				break;
			case "pull":
				System.out.println("Not implemented yet.");
				//				if((!KVC.initWorkspace)||(!KVC.initLocalRepo)){
				//					System.out.println("The dataset has not been initialized yet.");
				//				}else{
				//					TransferFunctions.pullData();
				//				}
				break;
			case "commit":
				if((!KVC.initWorkspace)||(!KVC.initLocalRepo)){
					System.out.println("Working environment is not set up properly.");
				}else{
					if((number<2)||(Objects.equal(command[1], null))){
						TransferFunctions.commitData();
					}else if(Objects.equal(command[1],"a")){
						TransferFunctions.commitAllData();
					}else{
						System.out.println("This is not a valid command. Please insert a different one.");
					}
				}
				break;
			case "checkout":
				if((!KVC.initWorkspace)||(!KVC.initLocalRepo)){
					System.out.println("Working environment is not set up properly.");
				}else{
					if((number<2)||(Objects.equal(command[1], null))){
						System.out.println("Not implemented yet.");
					}else if(Objects.equal(command[1], "b")){
						KVC.createBranch(KVC.localRepModel);
					}else if((command[1].contains("["))&&(command[1].contains("]"))){
						String commitNumber = command[1].substring(command[1].indexOf("[")+1,command[1].lastIndexOf("]"));
						if(KVC.localRepModel.getCommitNumbers().contains(commitNumber)){
							if(Objects.equal(commitNumber,KVC.localRepModel.getLatestCommit(commitNumber))){
								KVC.localRepModel.setCurrentCommit(commitNumber);
							}else{
								System.out.println("This commit does not conform to the end of a branch");
								System.out.println("Show all branch ends with: \"branch\"");
							}
						}else{
							System.out.println("This commit number is not included in the model");
						}
					}else{
						System.out.println("Please enter branch end commit in [square brackets]");
					}
				}


				break;
			case "fetch":
				System.out.println("Not implemented yet.");
				//				if((!KVC.initWorkspace)||(!KVC.initLocalRepo)){
				//					System.out.println("The dataset has not been initialized yet.");
				//				}else{
				//					TransferFunctions.fetchData();
				//				}
				break;
			case "change":

				if((number<2)||(Objects.equal(command[1], null))){
					System.out.println(Scripts.change);
				}else{
					if(!KVC.initWorkspace){
						System.out.println("The workspace has not been set up properly.");
					}else{
						KVC.updateAction(command[1]);
					}
				}
				break;
			case "show":
				if((number<2)||(Objects.equal(command[1], null))){
					System.out.println(Scripts.show);
				}else{
					if(!KVC.initWorkspace){
						System.out.println("The workspace has not been set up properly.");
					}else{
						KVC.overlookProject(command[1]);
					}
				}
				break;
			case "log":
				Settings.printSettings();
				System.out.println("Workspace directory:\t\t" + KVC.dirWorkspace);
				System.out.println("Local repository directory:\t" + KVC.dirLocalRep);
				System.out.println("Workspace initialized:\t\t" + KVC.initWorkspace);
				if(KVC.initWorkspace){
					System.out.println("Current branch workspace:\t" + KVC.workspaceModel.getLatestCommit(KVC.workspaceModel.getCurrentCommit()));
				}
				System.out.println("Local repository directory:\t" + KVC.initLocalRepo);
				if(KVC.initLocalRepo){
					System.out.println("Current branch local repo:\t" + KVC.localRepModel.getLatestCommit(KVC.localRepModel.getCurrentCommit()));
				}
				break;
			case "status":
				if((!KVC.initWorkspace)||(!KVC.initLocalRepo)){
					System.out.println("The dataset has not been initialized yet.");
				}else{
					TransferFunctions.statusOutput();
				}
				break;
			case "exit":
				KVC.exitFunction();
				break;
			default:
				System.out.println("This is not a valid command. Please insert a different one.");
				break;
			}
		}
	}

	private static String[] parseCommand(String command){
		String[] elements = command.split(" |-");
		String[] elements2 = new String[elements.length];
		int j=0;
		for(int i=0;i<elements.length;i++){
			if(!(Objects.equal(elements[i], ""))){
				elements2[j]=elements[i];
				j++;
			}
		}
		return elements2;
	}


}
