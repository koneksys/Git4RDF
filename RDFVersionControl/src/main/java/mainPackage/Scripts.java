package mainPackage;

public class Scripts {

	public static String help =
			"OPTIONS\n"
					+ "	-help:\n"
					+ "	Print the help menu with all commands\n"
					+ "\n"
					+ "	-C [<path>]\n"
					+ "	Set the directory of the workspace and initialize it. The current working directory is set as the default path, if no <path> is specified.\n"
					+ "	If the directory already runs as a KLD workspace, the current state will be adopted.\n"
					+ "\n"
					+ "	-config [user.name „authorname“] [rdfFormat „format“] [QVF „boolean“]:\n"
					+ "	Set configurations for the program\n"
					+ "	user.name: Set author ID for commit information\n"
					+ "	rdfFormat: Set format for RDF data input and output\n"
					+ "	QVF: quarter visualization format: Determine, wether both version model and application model should be shown\n"
					+ "		concurrently or only one at a time\n"
					+ "\n"
					+ "	-init [<path>]:\n"
					+ "	Set the path for the local repository and initialize it. If no path argument is added, the current working directory\n"
					+ "	will be set as the parent directory for the local repository.\n"
					+ "\n"
					+ "	-load [<path>] [-local]:\n"
					+ "		path: Load RDF dataset stored in the appropriate form (see config and log) in a text file into the working directory\n"
					+ "		-local: Update your workspace with the current version of the local repository\n"
					+ "\n"
					+ "	-clone:\n"
					+ "\n"
					+ "	-dir <path>:\n"
					+ "	Set the directory of the local repository. The given directory indicated by <path> has to be a previously initialized repository\n"
					+ "\n"
					+ "	-pwd:\n"
					+ "	Request current working directory\n"
					+ "\n"
					+ "	-gui [-workspace] [-localrepo]:\n"
					+ "	Display graphs in graphical visualization\n"
					+ "	If argument workspace is appended and QVF = false, workspace models will be displayed\n"
					+ "	If argument localrepo is appended and QVF = false, local repository models will be displayed\n"
					+ "\n"
					+ "	-index:\n"
					+ "	Display content of index layer\n"
					+ "\n"
					+ "	-add [-a] [-#]:\n"
					+ "	Add unstaged changes to index layer\n"
					+ "		-a adds all unstaged changes\n"
					+ "		-# adds the corresponding change identified by the given number\n"
					+ "\n"
					+ "	-rm:\n"
					+ "	Remove content from index layer\n"
					+ "		-a removes everything\n"
					+ "		-# removes the corresponding change identified by the given number\n"
					+ "\n"
					+ "	-reset:\n"
					+ "	\n"
					+ "	-tag:\n"
					+ "	Highlight particular commit with additional literal\n"
					+ "\n"
					+ "	-branch:\n"
					+ "	Print out the identifier of the last commit of every branch and identify the current branch\n"
					+ "\n"
					+ "	-merge [-[#]]:\n"
					+ "	Merge two commits from different branches in the local repository together by creating a union of the two RDF dataset\n"
					+ "		-[#]: Merge branch indicated by ‚#‘ to current branch\n"
					+ "\n"
					+ "	-push:\n"
					+ "\n"
					+ "	-pull:\n"
					+ "\n"
					+ "	-commit [-a]:\n"
					+ "	Add all changes from the index layer to the local repository\n"
					+ "		-a: Add all unstaged changes to the local repository irrespective of the state of the index layer\n"
					+ "	 \n"
					+ "	-checkout [-b] [-[#]]:\n"
					+ "		-b: Create new branch and switch to it\n"
					+ "		-[#]: Change current branch to branch indicated by number\n"
					+ "\n"
					+ "	-fetch:\n"
					+ "\n"
					+ "	-change [-script] [-select] [-tag]:\n"
					+ "	Add changes to the dataset in the workspace\n"
					+ "		-script: Enables modification via sparql scripts\n"
					+ "		-select: Enables modification by selecting the type of modification and the respective triple\n"
					+ "		-tag: Highlights a specific commit in the workspace history\n"
					+ "\n"
					+ "	-show [-version] [-branch] [-merge] [-tag]:\n"
					+ "	Enables overview of the workspace history\n"
					+ "		-version: displays the state of the dataset at a certain point in time both via the GUI and via the console\n"
					+ "		-branch: generates a new branch of commits in the workspace\n"
					+ "		-merge: merges two commits by generating the union of the two datasets\n"
					+ "		-tag: Highlights a specific commit in the workspace history\n"
					+ "\n"
					+ "	-log:\n"
					+ "	Prints out the configurations of the program:\n"
					+ "	File format: format for dataset storage and retrieval,\n"
					+ "	Author ID: name of the author\n"
					+ "	Quarter Visualization: GUI output format (if true: four windows, if false, two windows)\n"
					+ "	\n"
					+ "	-status:\n"
					+ "	List all unstaged changes (= differences between dataset in workspace and dataset in local repository)\n"
					+ "\n"
					+ "	-exit:\n"
					+ "	Exit the program and delete the workspace environment\n";



	public static String change =
			"\"-script\":\tInsert executable sparqlScript\n" +
					"\"-select\":\tSelect triple to delete, insert or modify\n" +
					"\"-tag\":\t\tTag this version as a baseline\n";
	
	public static String show =
			"\"-version\":\tLook at a particular version of the dataset\n"+
					"\"-branch\":\tWork further on the dataset by editing the chosen version\n" +
					"\"-merge\":\tMerge version with a different version and combine the branches\n" +
					"\"-tag\":\t\tTag this version as a baseline\n";

	public static String log = "\"help\":\t\tPrint help menu\n" +
			"\"init\":\t\tInitialize dataset\n";

	public static String status = "\"help\":\t\tPrint help menu\n" +
			"\"init\":\t\tInitialize dataset\n";

}