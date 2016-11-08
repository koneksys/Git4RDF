#1. What is a Version Control System?
A version control system allows you to track the history of data. It manages changes to a dataset and supports creating different versions of it.

https://en.wikipedia.org/wiki/Version_control
#2. Introduction to Koneksys Version Control
Koneksys Version Control (KVC) is a version control concept specifically designed for the management of RDF datasets. It tracks the progress of an RDF project and stores previous versions of the data.
## KVC repositories ##
Koneksys Version Control contains multiple repositories.
The *workspace repository* is a directory on the machine, where changes on the dataset are performed.

The *index layer* is an intermediate level, where changes that are intended to be committed can be selected and stored.

The staged changes can be committed to the *local repository*.

![Repositories.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/4268399974-Repositories.png)
#3. Details of a commit action
A new commit means a user-induced update of the version model of the respective repository with a new version of the dataset.
## Version Model ##
The version model is an RDF dataset in every repository of Koneksys Version Control. It tracks the changes between two consecutive dataset versions and ensures retrievability of previous versions. Its functionality will be displayed with the following example dataset.

![Dataset1.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/4068699272-Dataset1.png)

This image depicts an RDF dataset illustrated as a graph. As a user wants to modify the underlying data, the graph will be altered with deleted and added triples:

![Dataset2.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/21033112-Dataset2.png)

Once this modified dataset gets committed to the local repository, all selected changes will be stored in the version model of the local repository and the updated version of the dataset will be saved. The appearance of a version model between two commits is shown below:

![VersionModel.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/1191799286-VersionModel.png)
#4. KVC tooling
Important commands to control the program will be presented in the following. They are summarized in the following table and will be explained in detail afterwards.

Command       | Action
------------- | -------------
`help`        | Open a menu that lists all possible commands
`config`      | Customize the preferences of your application
`log`         | Display the preferences of your application
`C`           | Set the directory, where the workspace environment should be set up
`init`        | Set the directory, where the local repository should be set up
`dir`         | If a local repository already exists, set the directory of the local repository there
`load`        | Load an RDF dataset into your workspace to work on it
`status`      | Display the difference between the workspace and the local repository
`add`         | Add all changes that you intend to commit to your local repository to the index layer
`rm`          | Remove particular changes from the index layer
`index`       | Display the content of the index layer
`commit`      | Commit the changes of the index layer to the local repository
`branch`      | List all existing branches by the version identifier of their last commit
`merge`       | Merge two versions of different branches together
`checkout`    | Create a new branch or switch to an existing one
`exit`        | Exit the program



    help
Use the command "help" to open a menu that lists all possible commands.
****
    config
Set configurations for the program.

    config [user.name] "authorID"
Set author name to authorID for commit information

    config [rdfFormat] "format"
Set format for RDF data input and output to format

    config [QVF] "boolean"
Switch Quarter Visualization Format on or off: Determine, wether both version model and application model should be shown concurrently or only one at a time
****
    log
Display the preferences of your application
****
    C
Set up the workspace environment in the current working directory.

    C <path>
Set up the workspace in the specified directory.

Setting up the workspace environment means creating a directory folder, where the RDF datasets of the application will be stored locally.
****
    init
Set up the local repository in the current working directory.

    init <path>
Set up the local repository in the specified directory.

Setting up the local repository means creating a directory folder, where the RDF datasets of the repository will be stored.
****
    dir <path>
If a local repository already exists, set the directory of the local repository on this directory. The program will check, if the specified directory is a repository indeed.

Both workspace and local repository **must** be set up, before one can start working with the program.
****
    load <path>
Load an RDF dataset stored in a file at path into your workspace and work on it.

    load local
Load the most recent dataset from the local repository.
****
    status:
Display all changes between the workspace and the local repository.
****
    add [change identifier]
After modifications on the workspace dataset, add all changes that you intend to commit to your repository to the index layer. Select change to be added to index layer by its identifier shown with the status command.

    add -a
After modifications on the workspace dataset, add all changes that you intend to commit to your repository to the index layer. Add all differences between workspace and local repository that are shown in status to the index layer.
****
    rm [change identifier]
Remove particular change from the index layer by specifying its identifier shown with the index command.

    rm -a
Remove all content from the index layer.
****
    index
Show all the changes that are added to the index layer to be committed as a new version to the local repository.
****
    commit
Commit the selected changes of the index layer to the local repository.

    commit -a
Commit all changes shown in status to the local repository
****
    branch
List all existing branches and identify the current branch. Every branch is specified by the version identifier of their last commit.
****
    merge
Merge two versions of different branches together.

    merge [branch identifier]
Merge branch specified by branch identifier to current branch.
****
    checkout -b
Create new branch and switch to it

    checkout [branch identifier]
Change current branch to branch indicated by branch identifier
****
    gui
Display the datasets in a graph visualization

    gui [-workspace]
Display only the workspace dataset in a graph visualization

    gui [-localrepo]
Display only the local repository dataset in a graph visualization
****
    exit
Exit the application
#5. Installation of the KVC command line application
This following sections will give you a short overview of the first steps that are necessary to get your application up and running.
## Download program files
Please download the jar file "git4RDF.jar" and the text file "ExampleDataset.txt" and store both on your machine.

![DownloadDropboxJar.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-07%20um%2016.31.54.png)

## Open program in commandline
type "java -jar <path-to-file>.jar"

![Bildschirmfoto 2016-11-04 um 15.58.43.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-04%20um%2015.58.43.png)
#6. KVC configuration
## config command:
user.name: Important to specify the user's ID, when working collaboratively on the program

rdfFormat: Specify the storage format of RDF files to be loaded

QVF: Only important for the graphical visualization of a dataset. Determines, wether both version model and application model are shown concurrently or only one at a time
#7. Example: Exercising a version-controlled modification on an imaginary RDF dataset
In the following, the KVC application will be set up and applied to an example RDF dataset to explain the basic functionalities. The initial dataset consists of only 10 triples and looks like that:

![Bildschirmfoto 2016-08-23 um 15.36.13.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/1007350453-Bildschirmfoto%202016-08-23%20um%2015.36.13.png)

Before starting to work with the application, it is expedient to survey the preferences by the "log" command:

![Bildschirmfoto 2016-11-04 um 15.58.54.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-04%20um%2015.58.54.png)

To change the author ID, the "config" command is used:

![Bildschirmfoto 2016-11-04 um 16.04.44.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-04%20um%2016.04.44.png)

This ID will characterize all subsequent commits from this application account, unless its changed with config again.

It should be ensured, that the triple format of files to be loaded into the workspace correspond to the appointed format in the preferences. Otherwise problems could occur while loading the data.

##Initialize repositories
Before the features of KVC can be deployed, both workspace directory and local repository directory have to be specified first.
To determine the directory of the workspace, the command "C" is used:

![Bildschirmfoto 2016-11-04 um 16.05.57.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-04%20um%2016.05.57.png)

If no <path> had been denoted, the workspace would have been set up in the current working directory.

If a local repository already exists, the command "dir" will be used to determine its directory:

![Bildschirmfoto 2016-11-04 um 16.12.06.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-04%20um%2016.12.06.png)

Else, the user should use the command "init" to create a new local repository:

![Bildschirmfoto 2016-11-04 um 16.10.13.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-04%20um%2016.10.13.png)

Similar to the "C" command, if no <path> is denoted with the init command, the local repository will be set up in the current working directory.
##Start working with the program
Load a dataset into the workspace to work on it with the command "load".
You can download the demonstration dataset that is used here at: 

http://www.koneksys.com/idontknowyet

Specify the file path of the RDF file and make sure that the read format in config is equivalent to the file storage format:

![Bildschirmfoto 2016-11-04 um 16.23.25.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-04%20um%2016.23.25.png)![Bildschirmfoto 2016-11-04 um 16.23.30.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-04%20um%2016.23.30.png)

If the local repository was already set up and the user wants to check out the last version of it and copy it to the workspace, the load command should be used with the argument "-local".

![Bildschirmfoto 2016-11-04 um 17.10.26.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-04%20um%2017.10.26.png)![Bildschirmfoto 2016-11-04 um 17.10.58.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-04%20um%2017.10.58.png)

The loaded dataset will be displayed in a graphical representation:

![Bildschirmfoto 2016-11-04 um 17.11.02.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-04%20um%2017.11.02.png)

As in this particular case the local repository was only set up recently and is still empty, the first step should be a commit of the initial dataset, that was loaded into the workspace from an external file, to the local repository. For this purpose, "commit -a" is used:

![Bildschirmfoto 2016-11-04 um 17.15.00.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-04%20um%2017.15.00.png)![Bildschirmfoto 2016-11-04 um 17.22.20.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-04%20um%2017.22.20.png)

Now the version-tracked work on the dataset can start. To modify the dataset, the application-embedded control tools will be used with the command "change -script". With this command, a SPARQL script can be inserted to directly execute update operations on the data.

![Bildschirmfoto 2016-11-07 um 11.30.52.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-07%20um%2011.30.52.png)
![Bildschirmfoto 2016-11-07 um 11.38.32.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-07%20um%2011.38.32.png)
![Bildschirmfoto 2016-11-07 um 11.38.37.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-07%20um%2011.38.37.png)

The executed modification changes contain two additional triples and one deleted one connected to resource 3, as well as both one inserted and one deleted triple linked to resource 7.

The dataset can also be modified using other tools that are connected with OSLC adaptors or manually by editing the XML or JSON storage files.

After the modification, the difference between the current state in the workspace and in the local repository can be shown using "status":

![Bildschirmfoto 2016-11-07 um 11.40.18.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-07%20um%2011.40.18.png)

This command lists all triples, that are affected by a change or represent a difference between the two models. It also assigns every change with a number, that has to be used to identify the change with "add", "commit" or "rm" commands.

As all the recent changes to the last version of the local repository are clear and identified now, the repository can be updated with a new version. A user might only want to commit certain changes at once for reasons of clarity and confirmability.

In this particular case, all changes regarding resource 3 might be intended to be committed together, and all changes concerning resource 7 in a different commit afterwards.

To achieve that, the "add" command will be used repeatedly, identifying all changes linked to resource 3:

![Bildschirmfoto 2016-11-07 um 11.42.39.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-07%20um%2011.42.39.png)

After that, the content of the index layer could be reviewed with the command "index":

![Bildschirmfoto 2016-11-07 um 11.42.54.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-07%20um%2011.42.54.png)

To commit all the changes of the index layer to the local repository model, the "commit" command is used again.

As one can see in the graphical illustration, both the version model and the application model of the local repository have been updated now:

![Bildschirmfoto 2016-11-07 um 11.43.22.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-07%20um%2011.43.22.png)

Using the "status" command again, the differences between the current datasets in workspace and local repository have been reduced to the ones related to resource 7:

![Bildschirmfoto 2016-11-07 um 11.59.20.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-07%20um%2011.59.20.png)

To commit those changes to the local repository as well, the previous steps will be repeated to create yet another commit:

![Bildschirmfoto 2016-11-07 um 12.35.37.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-07%20um%2012.35.37.png)

With this command you get a view of the local repository only:

![Bildschirmfoto 2016-11-07 um 12.38.15.png](https://github.com/koneksys/Git4RDF/blob/master/Graphics/Bildschirmfoto%202016-11-07%20um%2012.38.15.png)

The RDF datasets and scripts that were used in the demonstration of a KVC set up are included in the application download and can be reenacted to become familiar with the program.

The used commands can be copied and pasted in the following order:

The directory where the source code of the program and hence the resources for the example
set up are stored, varies on your personal settings. Therefore, it can not be XX in general.

The directory of the application files will therefore be substituted with <file-path>.
Whenever <file-path> occurs in the following commands, please replace it with the directory,
that you stored the application files of KVC in.

1. Initialize workspace:

     Windows:	`C <C:\windows\desktop>`

     Mac OS:	`C </Users/StandardUser/Desktop>`

     Linux:	`C </usr/bin>`

2. Initialize or determine local repository:

    Windows:	`init <C:\windows\desktop>`

	or `dir <C:\windows\local-repo-dir>`

    Mac OS:	`init </Users/StandardUser/Desktop>`

	or `dir </Users/StandardUser/local-repo-dir>`

    Linux:	`init </usr/bin>`

	or `dir </usr/bin/local-repo-dir>`
		
3. Load dataset into workspace:

    Windows:	`load <filepath\ExampleDataset.txt>`

    Mac OS:	`load <filepath/ExampleDataset.txt>`

    Linux:	`load <filepath/ExampleDataset.txt>`

4. Commit initial dataset to local repository

    `commit -a`

5. Change dataset with a SPARQL script:

	1. `change -script`
	
	2.

            prefix VCOnt: <http://example.com/VCOnt-directory/>

		    delete

		    {<http://example/resource3> <VCOnt:connectedTo> <http://example/resource2> .

		    <http://example/resource7> <VCOnt:describedBy> "Literal 2" . }

		    insert

		    {<http://example/resource3> <VCOnt:connectedTo> <http://example/resource8> .

		    <http://example/resource3> <VCOnt:describedBy> "Literal 3" .

		    <http://example/resource7> <VCOnt:connectedTo> <http://example/resource1> . }

		    where {}

		    END
		
	3. `y`
	
6. Add resource 3 related changes to index:

	1. `add 2`

	2. `add 4`

	3. `add 5`
	
7. Review index layer content:

    `index`

8. Commit index layer content to local repository

    `commit`

9. Show graphical depiction of workspace and local repository:

    `gui`

10. List unstaged differences between workspace and local repositories:

    `status`

11. Add remaining changes (all related to resource 7) to index layer:

    `add -a`

12. Commit index layer content to local repository:

    `commit`

13. Check graphical illustration again for accordance of workspace model and local repository model

    `gui localrepo`
