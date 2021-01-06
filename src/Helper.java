import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * this class represent few methods to help calculate complex probabilities
 * @author Gofna Ivry
 *
 */
public class Helper {

	public Helper() {}

	static String[][] cartesianProduct(HashMap<String, Node> BN, ArrayList<String> hidden, HashMap<String, String> evidences) {
		int solutions = 1;
		for(int i = 0; i < hidden.size(); solutions *= BN.get(hidden.get(i)).possibleVals.size(), i++);
		String[][] options = new String[solutions][hidden.size()]; // to save all the options of the hidden nodes
		ArrayList<ArrayList<String>> sets = new ArrayList<>(); // to save all the array list of variables of the hidden nodes

		for (int h = 0 ; h < hidden.size() ; h++) {
			sets.add(BN.get(hidden.get(h)).possibleVals);
		}
		int col = 0;
		for(int i = 0; i < solutions; i++) {
			int j = 1;
			col = 0;
			for(ArrayList<String> vals : sets) {
				options[i][col++] = vals.get((i/j)%vals.size());
				j *= vals.size();
			}
		}

		return options;
	}

	static double calculate(HashMap<String, Node> BN, String[] arrang, String[][] allOptions, ArrayList<String> hidden, HashMap<String, String> evidences) {
		double resOption = 1;
		double numerator = 0.0;
		boolean rightP = true;
		for(int i = 0 ; i < allOptions.length ; i++) {
			for(int j = 0 ; j < allOptions[0].length ; j++) {
				Node n = BN.get(arrang[j]);
				if(n.parents.size() > 0 && n.parents != null) {
					for(int row = 0 ; row < n.CPT.size() ; row++) {
						if(n.CPT.get(row)[n.parents.size()].equals(allOptions[i][j])) {
							for (int p = 0 ; p < n.parents.size() ; p++ ) {
								String parent = BN.get(n.parents.get(p)).getName();
								if(!n.CPT.get(row)[p].equals(allOptions[i][BN.get(parent).indexInBN])) {
									rightP = false;
									break;
								}
							}
							if(rightP == true) { 
								resOption *= Double.valueOf(n.CPT.get(row)[n.CPT.get(row).length-1]);
							}
							rightP = true;
						}
					}
				}else{
					for(int row = 0 ; row < n.CPT.size() ; row++) {
						if(n.CPT.get(row)[0].equals(allOptions[i][j])) {
							resOption *= Double.valueOf(n.CPT.get(row)[1]);
						}
					}
				}
			}
			numerator += resOption;
			resOption = 1;
		}
		return numerator;
	}

	/**
	 * to make direct retrieval from CPT if the probabilities derived directly from the query
	 * @param cpt the CPT to going through 
	 * @param nQuery
	 * @param nQueryVar
	 * @param evidences
	 * @return
	 */
	public static double getFromCPT(ArrayList<String[]> cpt, Node nQuery, String nQueryVar , HashMap<String, String> evidences ) {
		boolean rightP = true;
		for(int row =0 ; row < cpt.size() ; row++) {
			if(cpt.get(row)[nQuery.parents.size()].equals(nQueryVar)) {
				for (int p = 0 ; p < nQuery.parents.size() ; p ++) {
					if(!cpt.get(row)[p].equals(evidences.get(nQuery.parents.get(p)))){
						rightP = false;
					}
				}
				if(rightP == true) {
					return Double.valueOf(cpt.get(row)[cpt.get(row).length-1]);
				}
				rightP = true;
			}
		}
		return -1;
	}

	/**
	 * 
	 * @param hiddenNode
	 * @param factors
	 * @return the nodes that contains Hidden nodes
	 */
	public static ArrayList<Integer> containsHidden(String hiddenNode, ArrayList<Node> factors) {
		ArrayList<Integer> result = new ArrayList<Integer>();

		for (int i = 0; i < factors.size(); i++){
			if (factors.get(i).getName().equals(hiddenNode)){
				result.add(i);
			}
			else {
				for (String parent : factors.get(i).getParents()){
					if (parent.equals(hiddenNode)){
						result.add(i);
						break;
					}
				}
			}
		}
		return result;
	}
	
	public static void fillRow(Node n, int indexRowVar, Node result){
		for (int k = 0; k < n.getParents().size(); k++){
			String currentName = n.getParents().get(k);
			int IndexOfCurrent = result.getPos(currentName);
			if (IndexOfCurrent != -1){
				String value = n.getCPT().get(indexRowVar)[k];
				result.fillRow(value, IndexOfCurrent);
			}
		}
		int indexOfCurrent = result.getPos(n.getName());
		if (indexOfCurrent != -1){
			String value = n.getCPT().get(indexRowVar)[n.getParents().size()];
			result.fillRow(value, indexOfCurrent);
		}
	}
	
	public static void multiplyCol(String[] n1, String[] n2, Node result) 
	{
		double newValue = Double.parseDouble(n1[n1.length - 1]) * Double.parseDouble(n2[n2.length - 1]);
		result.fillRow("" + newValue, result.getParents().size() + 1);
	}
	

	/**
	 * Helper method to add value to HashMap with initialize Integer to 1
	 * @param name
	 * @param sharedNodes
	 */
	public static void addValue(String name, HashMap<String, Integer> sharedNodes) {
		if (sharedNodes.containsKey(name))
			sharedNodes.put(name, sharedNodes.get(name) + 1);
		else
			sharedNodes.put(name, 1);
	}
	
	public static void normalize(Node n) {
		double sumRows = 0;
		for (String row[] : n.getCPT())
			sumRows += Double.parseDouble(row[row.length - 1]);

		for (String row[] : n.getCPT())
			row[row.length - 1] = "" + (Double.parseDouble(row[row.length - 1]) / sumRows);
	}
	
	/**
	 * @param currentNode
	 * @param nodeQuery
	 * @param visited
	 * @return true if node is Ancestor of evidences or node-query, otherwise return false
	 */
	public static boolean isAncestor( HashMap<String, Node> BN, String currentNode, String nodeQuery, HashSet<String> visited) {
		if (currentNode.equals(nodeQuery)) {
			return true;
		}
		boolean result = false;
		for (String nodeChild : BN.get(currentNode).getChilds()){
			if (!visited.contains(nodeChild)){
				visited.add(nodeChild);
				result = result || isAncestor(BN, nodeChild, nodeQuery, visited);
				if (result) return true;
			}
		}
		return result;
	}



}
