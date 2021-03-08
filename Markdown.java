package markdown;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class Markdown {


	private static final String QUOTE = "<SPAN CLASS=\"quote\" " //
			+ "STYLE=\"font-size:150px; line-height:0.6em; opacity:0.2;\">" //
			+ "«</SPAN><P CLASS=\"quote\">";
	private final String filename;
	private final PrintStream out;

	public Markdown(String filename) {
		this.filename = filename;
		out = System.out;
	}

	public Markdown(String filename, String outputFilename) {
		this.filename = filename;
		try {
			this.out = new PrintStream(new File(outputFilename), "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	public void start() {
		run();
	}

	public void run() {
		appendHeader();
		boolean firstInstanceOfDIV = true;
		boolean firstInstanceOfLI = true;
		boolean firstInstanceOfQuote = true;
		boolean tagPOpened = false;
		String line;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"))) {
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("# ")) {
					if (firstInstanceOfDIV) {
						line = line.replaceAll("# ", "<DIV><H1>");
						firstInstanceOfDIV = false;
					} else {
						line = line.replaceAll("# ", "</DIV>\n\n<DIV><H1>");
						firstInstanceOfLI = true;
					}
					line = line + "</H1>";
				} else if (line.equals("___")) {
					line = "<HR>";
				} else if (line.startsWith("- ")) {
					if (firstInstanceOfLI) {
						line = line.replaceAll("- ", "<UL>\n<LI>");
						firstInstanceOfLI = false;
					} else {
						line = line.replaceAll("- ", "<LI>");
					}
				} else if (line.startsWith(">> ")) {
					line = line.replaceAll(">> ", QUOTE) + "<BR>";
					tagPOpened = true;
					firstInstanceOfQuote = false;
				} else if (!line.equals("") && !firstInstanceOfQuote) {
					line = "- " + line + "<BR>";
				} else if (line.equals("") && !firstInstanceOfQuote) {
					if (tagPOpened) {
						line = "</P>";
						tagPOpened = false;
						firstInstanceOfQuote = true;
					}
				}
				line = transformLink(line);
				line = transformB(line);
				line = transformI(line);
				out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		appendFooter();
	}

	private void appendFooter() {
		appendFile("footer.html");
	}

	private void appendHeader() {
		appendFile("header.html");
	}

	private void appendFile(String fileName) {
		String line;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"))) {
			while ((line=reader.readLine())!=null) {
				out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private String transformB(String line) {
		int indexOfStar = 0;
		while ((indexOfStar = line.indexOf("**", indexOfStar))>0) {
			if (line.charAt(indexOfStar + 2) != ' ') {
				line = line.substring(0, indexOfStar) + "<B>" + line.substring(indexOfStar + 2);
			} else if (line.charAt(indexOfStar - 1) != ' ') {
				line = line.substring(0, indexOfStar) + "</B>" + line.substring(indexOfStar + 2);
			}
			indexOfStar++;
		}
		return line;
	}

	private String transformI(String line) {
		int indexOfStar = 0;
		while ((indexOfStar = line.indexOf("*", indexOfStar))>0) {
			if (line.charAt(indexOfStar + 1) != ' ') {
				line = line.substring(0, indexOfStar) + "<I>" + line.substring(indexOfStar + 1);
			} else if (line.charAt(indexOfStar - 1) != ' ') {
				line = line.substring(0, indexOfStar) + "</I>" + line.substring(indexOfStar + 1);
			}
		}
		return line;
	}

	private String transformLink(String line) {
		int firstIndex = 0;
		int indexOf = line.indexOf("](");
		if (indexOf != -1) {
			int startOfTag = line.indexOf("[", firstIndex);
			int endOfUrl = line.indexOf(")", indexOf);
			line = line.substring(0, startOfTag) //
					+ "<A HREF=\"" + line.substring(indexOf + 2, endOfUrl) + "\">" //
					+ line.substring(startOfTag + 1, indexOf) //
					+ "</A>" + line.substring(endOfUrl + 1);
		}
		return line;
	}

	public static void main(String[] args) {
		Markdown md;
		if (args.length==0) {
			md = new Markdown("index.md");
		} else {
			String fileName = args[0];
			int indexOf = fileName.indexOf(".md");
			String radical = fileName.substring(0, indexOf);
			String outputFilename = radical + ".html";
			md = new Markdown(args[0], outputFilename);
			System.out.println(outputFilename);
		}
		md.start();
	}
}
