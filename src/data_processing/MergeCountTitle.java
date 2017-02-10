package data_processing;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MergeCountTitle {

	public static void main(String[] args) {
		Path countFilePath = Paths.get("/scratch/cluster-share/ghadakcv/"
				+ args[0]);
		Path pathToTitleFile = Paths.get("/scratch/cluster-share/ghadakcv/"
				+ args[1]);
		// Path countFilePath = Paths.get("count.txt");
		// Path pathToTitleFile = Paths.get("grep.txt");
		List<String> pathTitles;
		try {
			pathTitles = Files.readAllLines(pathToTitleFile,
					Charset.forName("UTF-8"));
			Map<String, Integer> pathCountMap = new HashMap<String, Integer>();
			Map<String, String> titlePathMap = new HashMap<String, String>();
			for (String pathTitle : pathTitles) {
				Pattern pat = Pattern.compile("(inex_13/[0-9/]+.xml):(.*)");
				Matcher mat = pat.matcher(pathTitle);
				if (mat.find()) {
					try {
						String path = mat.group(1);
						String title = mat.group(2);
						titlePathMap.put(title.trim(), path);
						pathCountMap.put(path, 0);
					} catch (IllegalStateException e1) {
						e1.printStackTrace();
					} catch (IndexOutOfBoundsException e2) {
						e2.printStackTrace();
					}
				}
			}
			try (BufferedReader br = Files.newBufferedReader(countFilePath,
					Charset.forName("ISO-8859-1"))) {
				String line = br.readLine();
				do {
					Pattern pat = Pattern.compile("en (.+) (\\d+) \\d+");
					Matcher mat = pat.matcher(line);
					if (mat.find()) {
						try {
							String title = mat.group(1).replace("_",  " ");
							String count = mat.group(2);
							if (titlePathMap.containsKey(title)) {
								String path = titlePathMap.get(title);
								Integer oldFreq = pathCountMap.get(path);
								Integer newFreq = Integer.parseInt(count);
								pathCountMap.put(path, oldFreq + newFreq);
							} else {
								// System.err.println("missing key: " + title);
							}
						} catch (IllegalStateException e1) {
							e1.printStackTrace();
						} catch (IndexOutOfBoundsException e2) {
							e2.printStackTrace();
						}
					}
					line = br.readLine();
				} while (line != null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (Map.Entry<String, Integer> entry : pathCountMap.entrySet()) {
				System.out.println(entry.getKey() + ", " + entry.getValue());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
