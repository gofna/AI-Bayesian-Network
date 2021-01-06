import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * this class is the main class. process all the Bayesian network data from file and call the match methods
 * @author Gofna Ivry
 *
 */
public class Ex1 {

	static HashMap<String, Node> BN;
	static String[] arrange; //to sort the nodes in order to the nodes from file

	public static void main(String[] args) {
		new Ex1().readFile("input4.txt");
	}

	public void readFile(String path) {
		
		Ex1.BN = new HashMap<String, Node>();
		File text = new File(path);

		try {
			Scanner sc = new Scanner(text);
			insertNodes(sc);
			String result = getResults(sc);
			printInFile(result);
			
			System.out.println("Done, output result file location [" + System.getProperty("user.dir") + "]");
			sc.close();
		} catch (FileNotFoundException e) {
			System.out.println("Unable read '"+ path + "' , please make sure file exists!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Unable write output file!");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Something got wrong");
			e.printStackTrace();
		}
	}

	private void insertNodes(Scanner sc) {
		String data = "";
		while (!data.toLowerCase().contains("variables")) {
			data = sc.nextLine();
		}

		data = data.replaceAll(" ", "").substring(data.indexOf(':') + 1);
		StringTokenizer names = new StringTokenizer(data, ",");
		Ex1.arrange = new String[names.countTokens()];
		int index=0;
		while (names.hasMoreElements()) {
			String n = names.nextToken();
			Ex1.arrange[index] = n;
			Ex1.BN.put(n, new Node(n));
		}

		int i = 0;
		while (!data.toLowerCase().contains("queries")) {
			data = sc.nextLine();
			if (data.contains("Var")){
				Node node = Ex1.BN.get(data.split(" ")[1]);
				node.indexInBN = i;
				
				data = sc.nextLine();
				data = data.replaceAll(" ", "").substring(data.indexOf(':') + 1);
				StringTokenizer valuesTok = new StringTokenizer(data, ",");
				node.addValues(valuesTok);

				data = sc.nextLine();
				data = data.replaceAll(" ", "").substring(data.indexOf(':') + 1);
				node.addParents(data);

				sc.nextLine(); // to skip CPT line;

				while (!data.isEmpty())
				{
					data = sc.nextLine(); // build row by row of CPT data
					node.buildCPT(data);
				}
				i++;
			}
		}
	}

	/**
	 * to read queries and call to the right method
	 * @param sc 
	 * @return the result to print in the output file
	 */
	private String getResults(Scanner sc) {
		String result ="";
		String query;
		while(sc.hasNextLine())
		{
			query = sc.nextLine();

			if (query.contains("1")) {	
				Methods method = new Methods(BN,query);
				result += method.simple() +"\n";
			}
			else if (query.contains("2")) {
				Methods method2 = new Methods(BN,query);
				result += method2.variableElimination() +"\n";
			}
			else if(query.contains("3")) {
				Methods method3 = new Methods(BN,query);
				result += method3.heuristic() +"\n";
			}
		}
		return result;
	}

	private static void printInFile(String result) throws IOException {
		 File output = new File("output.txt");
		 FileWriter write = new FileWriter(output);
		 write.write(result);
		 write.close();
	}

}
