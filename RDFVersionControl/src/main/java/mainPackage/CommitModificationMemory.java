package mainPackage;

import java.util.ArrayList;
import java.util.List;

public class CommitModificationMemory {
	private List<String[]> deleteTriples = new ArrayList<String[]>();
	private List<String[]> insertTriples = new ArrayList<String[]>();
	private List<String[]> modifiedTriples = new ArrayList<String[]>();

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
	 * adds modified triples to list
	 * @param modificationInput
	 */
	public void addModifiedTriples(List<String[]> modificationInput){
		modifiedTriples.addAll(modificationInput);
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
	 * return modified triples
	 * @return
	 */
	public List<String[]> getModifiedTriples(){
		return modifiedTriples;
	}

}
