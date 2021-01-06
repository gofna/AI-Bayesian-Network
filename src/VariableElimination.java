import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * this class represent the Variable Elimination algorithm to probabilistic inference
 * @author Gofna Ivry
 *
 */
public class VariableElimination {

	private HashMap<String, Node> BN;
	String[] arrang;
	String nodeQuery[];
	ArrayList<String> hidden;
	HashMap<String, String> evidences;
	private int muls, plus;;

	/**
	 * Constructor
	 * @param BN the Bayesian Network from file
	 * @param arrange the arrange of the nodes in order to the file
	 * @param hidden the hidden nodes
	 * @param nodeQuery the node-query with it's variable
	 * @param evidences the evidences nodes
	 */
	public VariableElimination(HashMap<String, Node> BN, String[] arrange, ArrayList<String> hidden,String nodeQuery[],HashMap<String, String> evidences) {
		this.BN = BN;
		this.arrang = arrange;
		this.hidden = hidden;
		this.evidences = evidences;
		this.nodeQuery = nodeQuery;
		plus = muls = 0;
	}

	public String make() {
		ArrayList<Node> factors = makeFactors(nodeQuery[0]);

		for (String hiddenNode : hidden){
			while (true){
				int indexMinimalFactors[] = minimalFactors(hiddenNode, factors);
				if (indexMinimalFactors == null){
					break;
				}
				else if (indexMinimalFactors.length == 1){
					Node newNode = sumFactors(hiddenNode, factors.get(indexMinimalFactors[0]));
					factors.remove(indexMinimalFactors[0]);
					factors.add(newNode);
				}
				else{
					joinFactors(hiddenNode, indexMinimalFactors, factors);
				}
			}
		}
		while (factors.size() > 1){
			int indexs[] = {0, 1};
			joinFactors(nodeQuery[0], indexs, factors);
		}
		Helper.normalize(factors.get(0));

		return finalResult(factors.get(0), this.nodeQuery);
	}

	/**
	 * make factors and insert them to ArrayList
	 * @param nodeQuery
	 * @return arrayList of the factors
	 */
	private ArrayList<Node> makeFactors(String nodeQuery) {
		ArrayList<Node> factors = new ArrayList<Node>();
		for (String hiddenNode : hidden){
			if (isAncestor(hiddenNode, nodeQuery, new HashSet<String>(Arrays.asList(hiddenNode)))) {
				factors.add(new Node(this.BN.get(hiddenNode)));
			}
		}
		factors.add(new Node(this.BN.get(nodeQuery)));
		for (String evidence : evidences.keySet()) {
			if(this.BN.get(evidence) != null) { //TO CHECK!!!!!!!!!!!!!!!!!!!!!!
				factors.add(new Node(this.BN.get(evidence)));
			}
		}
		removeInstants(factors);
		return factors;
	}

	/**
	 * Remove from factors list the nodes that has instant valued
	 * @param factors the factors without the nodes to remove
	 */
	private void removeInstants(ArrayList<Node> factors) {
		for (int i = 0; i < factors.size(); i++){
			boolean instant = true;
			ArrayList<String> parents = factors.get(i).getParents();
			if (!this.evidences.containsKey(factors.get(i).getName())){
				instant = false;
			}
			for (int j = 0; j < parents.size() && instant; j++) { // if the node contain all evidences we should remove it
				if (!this.evidences.containsKey(parents.get(j)))
					instant = false;
			}
			if (instant) {
				factors.remove(i--);
			}
		}
	}

	/**
	 * find the two minimal factors with minimum ASCI sum
	 * @param hiddenNode
	 * @param factors
	 * @return
	 */
	private int[] minimalFactors(String hiddenNode, ArrayList<Node> factors) {
		int minRows[] = {Integer.MAX_VALUE, Integer.MAX_VALUE};
		ArrayList<Integer> containsHidden = Helper.containsHidden(hiddenNode, factors);

		if (containsHidden.size() == 0) {
			return null;
		}
		if (containsHidden.size() == 1){
			int result[] = {containsHidden.get(0)};
			return result;
		}
		int result[] = {containsHidden.get(0), containsHidden.get(1)};
		for (int i = 0; i < containsHidden.size() - 1 ; i++){
			for (int j = i + 1; j < containsHidden.size(); j++){
				HashMap<String, Integer> sharedVars = makeSharedVariables(factors.get(containsHidden.get(i)), factors.get(containsHidden.get(j)));
				int rows = 1;
				int asciSum = 0;
				for (String n : sharedVars.keySet()){
					rows *= this.BN.get(n).getNumberOfValues();
					for (int k = 0; k < n.length(); k++) {
						asciSum += n.charAt(k);
					}
				}
				if (rows < minRows[0] || (rows == minRows[0] && asciSum < minRows[1])){
					minRows = new int[2];
					minRows[0] = rows;
					minRows[1] = asciSum;
					result[0] = containsHidden.get(i);
					result[1] = containsHidden.get(j);
				}
			}
		}
		return result;
	}

	/**
	 * Find match rows according to the join rules
	 * @param n1
	 * @param n2
	 * @param result
	 */
	private void join(Node n1, Node n2, Node result) {
		HashMap<String, Integer> sharedVariable = makeSharedVariables(n1, n2);

		for (int i = 0; i < n1.getCPT().size(); i++){
			for (int j = 0; j < n2.getCPT().size(); j++){
				boolean isMatchRow = true;
				for (Map.Entry<String, Integer> set : sharedVariable.entrySet()){
					if (set.getValue() == 2 && !n1.getCPT().get(i)[n1.getPos(set.getKey())].
							equals(n2.getCPT().get(j)[n2.getPos(set.getKey())])){
						isMatchRow = false;
						break;
					}
				}
				if (isMatchRow){
					result.addEmptyRow();
					Helper.fillRow(n1,i,result);
					Helper.fillRow(n2,j,result);
					Helper.multiplyCol(n1.getCPT().get(i), n2.getCPT().get(j), result);
					this.muls++;
				}
			}
		}
	}

	/**
	 * Initialize parent of the new result by join and remove evidences from the CPT
	 * @param newNode the result node of the join
	 * @param n
	 * @param hiddenNode
	 */
	private void initAndRemoveEvidences(Node newNode, Node n, String hiddenNode){
		for (String parent : n.getParents()){
			if (!this.evidences.containsKey(parent) && !parent.equals(hiddenNode) && !newNode.getParents().contains(parent)) {
				newNode.addParents(parent);
			}
		}
		if (!this.evidences.containsKey(n.getName()) && !n.getName().equals(hiddenNode) && !n.getParents().contains(n.getName())) {
			newNode.addParents(n.getName());
		}
		for (int i = 0; i < n.getCPT().size(); i++){
			String currentRow[] = n.getCPT().get(i);
			if (Double.parseDouble(currentRow[currentRow.length - 1]) == 0){
				n.getCPT().remove(i--);
			}
			else {
				for (Map.Entry<String, String> evidence : evidences.entrySet()){
					int CPTindex = n.getPos(evidence.getKey());
					if (CPTindex != -1 && !currentRow[CPTindex].equals(evidence.getValue())) {
						n.getCPT().remove(i--);
					}
					if(i<0)break; //TO CHECK !!!!!!!!!!!!!!!!!!
				}
			}
		}
	}

	private Node sumFactors(String hiddenNode, Node n) {
		int indexOfHidden = n.getPos(hiddenNode);
		boolean isRowCalculated[] = new boolean[n.getCPT().size()];
		ArrayList<String[]> nodeCPT = n.getCPT();
		Node newNode = initNewNodeSum(n, hiddenNode);

		for (int i = 0; i < nodeCPT.size(); i++){
			if (!isRowCalculated[i]){
				isRowCalculated[i] = true;
				String currentRow[] = nodeCPT.get(i);
				double sumRows = Double.parseDouble(currentRow[currentRow.length - 1]);
				for (int j = i + 1; j < nodeCPT.size(); j++){
					if (!isRowCalculated[j]){
						String otherRow[] = nodeCPT.get(j);
						boolean isMatch = true;
						for (int k = 0; k < currentRow.length - 1 && isMatch; k++){
							if (k != indexOfHidden){
								if (!currentRow[k].equals(otherRow[k]))
									isMatch = false;
							}
						}

						if (isMatch){
							isRowCalculated[j] = true;
							sumRows += Double.parseDouble(otherRow[otherRow.length - 1]);
							this.plus++;
						}
					}
				}
				newNode.addEmptyRow();
				int col = 0;
				for (int l = 0; l < currentRow.length - 1; l++){
					if (l != indexOfHidden)
						newNode.fillRow(currentRow[l], col++);
				}
				newNode.fillRow("" + sumRows, col);
			}
		}
		return newNode;
	}
	
	/**
	 * @param currentNode
	 * @param nodeQuery
	 * @param visited
	 * @return true if node is Ancestor of evidences or node-query, otherwise return false
	 */
	private boolean isAncestor( String currentNode, String nodeQuery, HashSet<String> visited) {
		if (currentNode.equals(nodeQuery) || evidences.get(currentNode) != null) {
			return true;
		}
		boolean result = false;
		for (String nodeChild : BN.get(currentNode).getChilds()){
			if (!visited.contains(nodeChild)){
				visited.add(nodeChild);
				result = result || isAncestor(nodeChild, nodeQuery, visited);
				if (result) return true;
			}
		}
		return result;
	}


	/**
	 * Prepare node result of the Eliminate action
	 * @param n
	 * @param hiddenNode
	 * @return
	 */
	private Node initNewNodeSum(Node n, String hiddenNode) {
		ArrayList<String> tempVars = new ArrayList<String>();
		for (String parent : n.getParents()){
			if (!parent.equals(hiddenNode))
				tempVars.add(parent);
		}
		if (!n.getName().equals(hiddenNode)) {
			tempVars.add(n.getName());
		}
		Node newNode = new Node(tempVars.get(tempVars.size() - 1));
		for (int i = 0; i < tempVars.size() - 1; i++) {
			newNode.addParents(tempVars.get(i));
		}
		return newNode;
	}


	private void joinFactors(String hiddenNode, int[] indexMinimalFactors, ArrayList<Node> factors){
		Node combined = new Node(hiddenNode);
		Node n1 = factors.get(indexMinimalFactors[0]);
		initAndRemoveEvidences(combined, n1, hiddenNode);
		Node n2 = factors.get(indexMinimalFactors[1]);
		initAndRemoveEvidences(combined, n2, hiddenNode);
		join(n1, n2, combined);
		factors.remove(n1);
		factors.remove(n2);
		factors.add(combined);
	}

	/**
	 * Calculate all the shared variables between two nodes according parents
	 * @param n1
	 * @param n2
	 * @param evidences
	 * @return
	 */
	HashMap<String, Integer> makeSharedVariables(Node n1, Node n2){
		HashMap<String, Integer> sharedNodes = new HashMap<String, Integer>();
		if (!evidences.containsKey(n1.getName())) {
			Helper.addValue(n1.getName(), sharedNodes);
		}
		if (!evidences.containsKey(n2.getName())) {
			Helper.addValue(n2.getName(), sharedNodes);
		}
		for (String parent : n1.getParents()){
			if (!evidences.containsKey(parent))
				Helper.addValue(parent, sharedNodes);
		}
		for (String parent : n2.getParents()){
			if (!evidences.containsKey(parent))
				Helper.addValue(parent, sharedNodes);
		}
		return sharedNodes;
	}

	private String finalResult(Node n, String[] nodeQuery){
		this.plus += n.getCPT().size() - 1;
		int indexOfResult = n.getPos(nodeQuery[0]);

		String result = "," + this.plus + "," + this.muls;
		for (String s[] : n.getCPT()){
			if (s[indexOfResult].equals(nodeQuery[1]))
				return result = String.format("%.5f", Double.parseDouble(s[s.length - 1])) + result;

		}
		return null;
	}
}
