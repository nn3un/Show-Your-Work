
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;


public class scanner {

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		Scanner scanner = new Scanner(new File("C:/Users/nnuzaba47/eclipse-workspace/Practice.csv"));
		scanner.useDelimiter(",");
		String content = " ";
        
		while(scanner.hasNext()){
			String check = scanner.next();
			if(check.equals("add")){
				int number = Integer.parseInt(scanner.next());
				String toAdd = scanner.next();
				toAdd = toAdd.replace('`', ',');
				content = content.substring(0, number)+toAdd+content.substring(number);
			}
			else if (check.equals("sub")) {
				int number = Integer.parseInt(scanner.next());
				String toDelete = scanner.next();
				content = content.substring(0,number)+content.substring(number+toDelete.length());
			}
			if (scanner.hasNext()){
				scanner.nextLine();
			}
		}
		System.out.println(content);
		scanner.close();

		PrintWriter writer = new PrintWriter("the-file-name.txt", "UTF-8");
		writer.println(content);

		writer.close();
	}

}
