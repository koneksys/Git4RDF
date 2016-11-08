package mainPackage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SparqlScript extends Object{
	private String prefixString = new String();
	private List<String[]> deleteTriples = new ArrayList<String[]>();
	private List<String[]> insertTriples = new ArrayList<String[]>();

	/**
	 * adds prefix string
	 * @param prefixInput
	 */
	public void addPrefixStrings(String prefixInput){
		prefixString = prefixInput;
	}

	/**
	 * adds delete triples to list
	 * @param deleteInput
	 */
	public void addDeleteTriples(List<String[]> deleteInput){
		deleteTriples.addAll(deleteInput);
	}

	/**
	 * adds insert triples to list
	 * @param insertInput
	 */
	public void addInsertTriples(List<String[]> insertInput){
		insertTriples.addAll(insertInput);
	}

	/**
	 * adds delete triples to list after emptying it
	 * @param deleteInput
	 */
	public void updateDeleteTriples(List<String[]> deleteInput){
		deleteTriples.removeAll(deleteTriples);
		deleteTriples.addAll(deleteInput);
	}

	/**
	 * adds insert triples to list after emptying it
	 * @param insertInput
	 */
	public void updateInsertTriples(List<String[]> insertInput){
		insertTriples.removeAll(insertTriples);
		insertTriples.addAll(insertInput);
	}

	/**
	 * return prefix string
	 * @return
	 */
	public String getPrefixStrings(){
		return prefixString;
	}

	/**
	 * return delete triples
	 * @return
	 */
	public List<String[]> getDeleteTriples(){
		return deleteTriples;
	}

	/**
	 * return insert triples
	 * @return
	 */
	public List<String[]> getInsertTriples(){
		return insertTriples;
	}

	/**
	 * remove one of the delete triples in the script
	 */
	public void removeDeleteTriple(String[] triple){
		if(deleteTriples.contains(triple)){
			List<String[]> tempTriples = new ArrayList<String[]>();
			Iterator<String[]> iter = deleteTriples.listIterator();
			while(iter.hasNext()){
				String[] nextTriple = iter.next();
				if(!(SparqlUtils.compareTriples(nextTriple,triple))){
					tempTriples.add(nextTriple);
				}
			}
			deleteTriples.removeAll(deleteTriples);
			deleteTriples.addAll(tempTriples);
		}else{
			System.out.println("Triple could not be deleted.");
		}
	}

	/**
	 * remove one of the insert triples in the script
	 */
	public void removeInsertTriple(String[] triple){
		if(insertTriples.contains(triple)){
			List<String[]> tempTriples = new ArrayList<String[]>();
			Iterator<String[]> iter = insertTriples.listIterator();
			while(iter.hasNext()){
				String[] nextTriple = iter.next();
				if(!(SparqlUtils.compareTriples(nextTriple,triple))){
					tempTriples.add(nextTriple);
				}
			}
			insertTriples.removeAll(insertTriples);
			insertTriples.addAll(tempTriples);
		}else{
			System.out.println("Triple could not be deleted.");
		}
	}

	/**
	 * return sparql script as string
	 * @return
	 */
	public String getSparqlString(){
		String sparqlString = new String();
		//prefix strings
		if(!(prefixString.isEmpty())){
			sparqlString = sparqlString + "prefix" + prefixString.toString();
		}
		//delete triples
		if(!(deleteTriples.isEmpty())){
			sparqlString = sparqlString + "delete {\n";
			for(String[] deleteTriple : deleteTriples){
				sparqlString = sparqlString + deleteTriple[0] + " " + deleteTriple[1] + " " + deleteTriple[2] + " .\n";
			}
			sparqlString = sparqlString + "}\n";
		}
		//insert triples
		if(!(insertTriples.isEmpty())){
			sparqlString = sparqlString + "insert {\n";
			for(String[] insertTriple : insertTriples){
				sparqlString = sparqlString + insertTriple[0] + " " + insertTriple[1] + " " + insertTriple[2] + " .\n";
			}
			sparqlString = sparqlString + "}\n";
		}
		if((!(deleteTriples.isEmpty()))||(!(insertTriples.isEmpty()))){
			sparqlString = sparqlString + "where {}";
		}
		return sparqlString;
	}

	/**
	 * return reverse sparql script as string
	 * @return
	 */
	public String getReverseSparqlString(){
		String sparqlString = new String();
		//prefix strings
		if(!(prefixString.isEmpty())){
			sparqlString = sparqlString + "prefix" + prefixString.toString();
		}
		//insert triples convert to delete triples
		if(!(insertTriples.isEmpty())){
			sparqlString = sparqlString + "delete {\n";
			for(String[] insertTriple : insertTriples){
				sparqlString = sparqlString + insertTriple[0] + " " + insertTriple[1] + " " + insertTriple[2] + " .\n";
			}
			sparqlString = sparqlString + "}\n";
		}
		//delete triples convert to insert triples
		if(!(deleteTriples.isEmpty())){
			sparqlString = sparqlString + "insert {\n";
			for(String[] deleteTriple : deleteTriples){
				sparqlString = sparqlString + deleteTriple[0] + " " + deleteTriple[1] + " " + deleteTriple[2] + " .\n";
			}
			sparqlString = sparqlString + "}\n";
		}
		if((!(deleteTriples.isEmpty()))||(!(insertTriples.isEmpty()))){
			sparqlString = sparqlString + "where {}";
		}
		return sparqlString;
	}

}
