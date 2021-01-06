import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class represent a three different way to make inferences from Bayesian network
 * @author Gofna Ivry
 *
 */
public class Methods {

	HashMap<String, Node> BN;
	String[] arrange; //to sort the nodes in order to the nodes from file
	String nodeQuery[];
	ArrayList<String> hidden;
	HashMap<String, String> evidences;
	int muls, plus;

	/**
	 * Constructor
	 * @param BN the Bayesian network from file
	 * @param query the query to process from file
	 */
	public Methods(HashMap<String, Node> BN, String query) { 
		this.BN = BN;
		this.arrange = new String[this.BN.size()];
		this.nodeQuery = query.substring(query.indexOf('(') + 1, query.indexOf('|')).split("=");
		this.hidden = new ArrayList<String>();
		this.evidences = new HashMap<String, String>();
		for (String tempEvidence : query.substring(query.indexOf('|') + 1, query.indexOf(')')).split(",")){
			if (!tempEvidence.isEmpty()){
				evidences.put(tempEvidence.substring(0, tempEvidence.indexOf('=')), tempEvidence.substring(tempEvidence.indexOf('=') + 1));
			}
		}

		for (HashMap.Entry<String, Node> n : BN.entrySet()) {
			this.arrange[this.BN.get(n.getKey()).indexInBN] = n.getKey();
		}

		for (String s : this.arrange) {
			if(this.evidences.get(s) == null && !s.equals(this.nodeQuery[0])) {
				this.hidden.add(s);
			}
		}
	}

	/**
	 * to make simple inference
	 * @return the result of the conclusion
	 */
	public String simple() {
		double result = 0;
		boolean instant = checkIfInstant();
		if(instant == false) {
			this.evidences.put(nodeQuery[0], nodeQuery[1]);
			double numerator = allOptions(this.hidden, this.evidences);
			this.evidences.remove(nodeQuery[0]);
			this.hidden.add(nodeQuery[0]);
			double denominator = normalize(this.hidden, this.evidences);
			result = numerator/denominator;
		}
		else {
			result = Helper.getFromCPT(this.BN.get(this.nodeQuery[0]).CPT , this.BN.get(nodeQuery[0]), this.nodeQuery[1], this.evidences);
		}
		System.out.println(String.format("%.5f",result)+","+this.plus+","+this.muls);
		return (String.format("%.5f",result)+","+this.plus+","+this.muls);
	}

	/**
	 * calculate the numerator by the Baye's theorem
	 * @param hidden
	 * @param evidences
	 * @return the value of numerator by the Baye's theorem
	 */
	private double allOptions(ArrayList<String> hidden, HashMap<String, String> evidences) {
		String[][] hiddenOptions = Helper.cartesianProduct(this.BN, hidden, evidences);
		if(hiddenOptions != null) {
			this.muls = hiddenOptions.length * (this.BN.size()-1)*2;
			this.plus = (hiddenOptions.length-1) * 2 + 1;
		}

		String[][] allOptions = new String[hiddenOptions.length][this.BN.size()];  

		for (int i = 0 ; i < allOptions.length ; i++) {
			for(int h = 0 ; h<hiddenOptions[0].length ; h++) {
				allOptions[i][BN.get(hidden.get(h)).indexInBN] = hiddenOptions[i][h];
			}
			for (HashMap.Entry<String, String> e : evidences.entrySet()) {

				allOptions[i][BN.get(e.getKey()).indexInBN] = evidences.get(e.getKey());
			}
		}

		double res = Helper.calculate(BN, arrange, allOptions, hidden, evidences);
		return res ;
	}

	/**
	 * calculate the denominator by the Baye's theorem
	 * @param hidden
	 * @param evidences
	 * @return the value of denominator by the Baye's theorem
	 */
	private double normalize(ArrayList<String> hidden, 	HashMap<String, String> evidences) {
		String[][] hiddenOptions = Helper.cartesianProduct(this.BN, hidden, evidences);

		String[][] allOptions = new String[hiddenOptions.length][this.BN.size()];  

		for (int i = 0 ; i < allOptions.length ; i++) {
			for(int h = 0 ; h<hiddenOptions[0].length ; h++) {
				allOptions[i][BN.get(hidden.get(h)).indexInBN] = hiddenOptions[i][h];
			}
			for (HashMap.Entry<String, String> e : evidences.entrySet()) {

				allOptions[i][BN.get(e.getKey()).indexInBN] = evidences.get(e.getKey());
			}
		}

		double res = Helper.calculate(BN, arrange, allOptions, hidden, evidences);
		return res ;
	}

	/**
	 * to use Variable Elimination algorithm
	 * @return the result of the calculate
	 */
	public String variableElimination() {
		String result = "";
		boolean instant = checkIfInstant();
		if(instant  == false) {
			Collections.sort(this.hidden);
			VariableElimination VE = new VariableElimination(this.BN, this.arrange, this.hidden, this.nodeQuery, this.evidences);
			result  = VE.make();
		}
		else {
			double p = Helper.getFromCPT(this.BN.get(this.nodeQuery[0]).CPT , this.BN.get(nodeQuery[0]), this.nodeQuery[1], this.evidences);
			result = String.format("%.5f", p) + ",0,0";
		}
		System.out.println(result);
		return result;	
	}

	/**
	 * use Variable Elimination algorithm but different order of the hidden variables
	 * @return the result of the calculate
	 */
	public String heuristic() {
		String result = "";
		boolean instant  = checkIfInstant();
		if(instant  == false) {
			ArrayList<Node> hiddenNodes = new ArrayList<Node>();
			for (String h : this.hidden) {
				hiddenNodes.add(this.BN.get(h));
			}
			Collections.sort(hiddenNodes); //sort by number of neighbors
			this.hidden.clear();
			for (Node n : hiddenNodes) {
				this.hidden.add(n.name);
			}
			VariableElimination VE = new VariableElimination(this.BN, this.arrange, this.hidden, this.nodeQuery, this.evidences);
			result  = VE.make();
		}
		else {
			double p = Helper.getFromCPT(this.BN.get(this.nodeQuery[0]).CPT , this.BN.get(nodeQuery[0]), this.nodeQuery[1], this.evidences);
			result = String.format("%.5f", p) + ",0,0";
		}
		System.out.println(result);
		return result;
	}

	/**
	 * check if all the evidences is ancestor and probability can be pulled out directly from CPT
	 * @return true if we need to calculate, otherwise return false
	 */
	private boolean checkIfInstant() {
		boolean allParetnIsE = true;
		boolean allIsAncestor = true;
		if(this.BN.get(nodeQuery[0]).parents.size() == 0) return false;
		for(String parent : this.BN.get(nodeQuery[0]).parents) {
			if(this.evidences.get(parent) == null) {
				allParetnIsE = false;
			}
		}
		for (String evidence : evidences.keySet()) {
			if(!Helper.isAncestor(this.BN, evidence, this.nodeQuery[0], new HashSet<String>(Arrays.asList(evidence)))) {
				allIsAncestor =  false;
			}
		}
		if(allIsAncestor && allParetnIsE) return true;
		else return false;
	}
	
}

