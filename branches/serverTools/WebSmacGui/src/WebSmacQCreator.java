import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;


/**
 * @author Android
 *
 */
public class WebSmacQCreator implements SmacUI, Runnable  {

	//This list of allowable charset is displayed in the charsetComboBox
	private static final String[] charsets = new String[] {"UTF-8", "ISO-8859-1"};

	//Global variables used to control thread states
	private Thread myThread;             //The thread used to perform the computationally expensive parsing/exporting operations.

	//Global variables used to store state at begining of computation
	private String charset;              //charset used to encode input .tex files
	private String rootdir;              //The directory in which to start looking for files to parse
	private int recursionDepth;          //# of levels below the root dir to look for files to parse (0 or Integer.MAX_VALUE)
	private boolean texOnly;             //Whether to look only for files ending in .tex when searching for files to parse
	private String goodPatternString;    //pattern to look for when searching for files to parse
	private String badPatternString;     //pattern to avoid when searching for files to parse (supercedes good patterns)
	private String logfile;              //The name of the log file.
	private String flashFolderName;      //The name of the folder in which to store the .swf files
	private String tex2swfFolderName;    //The name of the folder in which to find the 'tex2swf.sh' script
	private String tex2swfTmpFolderName; //The name of the tempory folder required by the 'tex2swf.sh' script
	private String zipFilename;          //The name of the file zip to create by flash folder with questions swf's  
	private String userFolder;
	private boolean overwriteFlashFiles; //When true flash files in flashFolderName are overwritten if they already exits, when false the Flash movie creation is skipped for the offending file.

	//Global variables used for I/O by the GUI.
	private OutputStream outputStream;     //The output stream to 'logfile' used when logMessages is true
	//private String terminateLine = "";     //This variable is used so that message starting with '\r' can overwrite the last line in the text area
	//private SmacCommunicator communicator; //A object that handles all interaction with the user when exporting files to the DB.

	private final XMLWriter questionsWriter;

	private static final String LATEX_HEADER=
		"\\documentclass{article}\n" +
		"\\usepackage{amsfonts,amsmath,amssymb,amsthm,cancel,color,enumerate,fixltx2e,graphicx,hyperref,multirow,pstricks,pstricks-add,pst-math,pst-xkey,textcomp,wasysym,wrapfig}\n" +
		"\\usepackage[T1]{fontenc}\n" +
		"\\usepackage[french]{babel}\n" +
		"\\usepackage{ae,aecompl}\n" +
		"\\setlength{\\topmargin}{-2cm}\n" +
		"\\setlength{\\oddsidemargin}{-1cm}\n" +
		"\\setlength{\\parindent}{0pt}\n" +
		"\\pagestyle{empty}\n" +
		"\n" +
		"\\newenvironment{mej-enumerate}{\n" +
		"  \\begin{enumerate}[(A)]\n"+
		"    \\setlength{\\itemsep}{5pt}\n" +
		"    \\setlength{\\parskip}{0pt}\n" +
		"    \\setlength{\\parsep}{0pt}\n" +
		"}{\\end{enumerate}}\n" +
		"\\usepackage[utf8]{inputenc}\n" +
		"\\begin{document}\n" +
		"\\shorthandoff{:}\n";

	private static final String LATEX_FOOTER = "\\begin{flushright}\\textcolor{white}{.}\\end{flushright}\\end{document}";
	private static final SmacParser.Tag[] CHOICES = new SmacParser.Tag[]{
		SmacParser.Tag.ChoiceA,SmacParser.Tag.ChoiceB,SmacParser.Tag.ChoiceC,SmacParser.Tag.ChoiceD,
		SmacParser.Tag.ChoiceE,SmacParser.Tag.ChoiceF,SmacParser.Tag.ChoiceG,SmacParser.Tag.ChoiceH
	};

	public WebSmacQCreator()
	{
		this.init();             //reads in the smacgui.ini file
		this.questionsWriter = new XMLWriter(this);
	}

	public void init()
	{
		Properties config = new Properties();
		try
		{
			InputStream conf = WebSmacQCreator.class.getResourceAsStream("smacgui.ini");
			config.load(new InputStreamReader(new BufferedInputStream(conf), "UTF-8"));
		} catch(Exception e)
		{
			outputMessage(e.getMessage());
		}
		charset = config.getProperty("charset").trim();
		rootdir = getFolderName(config.getProperty("tex_rootdir").trim());
		goodPatternString = config.getProperty("goodPatternString").trim();
		badPatternString = config.getProperty("badPatternString").trim();
		flashFolderName = getFolderName(config.getProperty("flash_export_dir")).trim();
		zipFilename = config.getProperty("zip_export_name").trim();
		tex2swfFolderName = getFolderName(config.getProperty("tex2swf_script_dir")).trim();
		tex2swfTmpFolderName = getFolderName(config.getProperty("tex2swf_tmp_dir").trim());
		recursionDepth = config.getProperty("recursive_search").trim().toUpperCase().equals("Y")?Integer.MAX_VALUE:1;
		logfile = config.getProperty("logfile").trim();
		if (logfile == null) logfile="smac.log";
		texOnly = true;
	}

	//Make sure all the folder specified by the user are valid.
	private boolean validateFoldersSelection()
	{
		File file;
		String msg = null;

		//validate input files folder.  It must exist and be a directory in which the user has read permission.
		file = new File(rootdir);
		if (!file.exists()) msg = "The input files folder you selected does not exists";
		else if (!file.isDirectory()) msg = "The input files folder you selected is not a directory";
		else if (!file.canRead() || !file.canExecute()) msg = "You do not have the required permission to read the input files folder you selected";


		//validate flash folder.  It must exist and be a directory in which the user has write permission.
		//Additionally, it cannot be the same as the 'tex2swf.sh' tmp folder.
		if (msg == null)
		{
			file = new File(flashFolderName);
			if (!file.exists()) msg = "The flash folder you selected does not exists";
			else if (!file.isDirectory()) msg = "The flash folder you selected is not a directory";
			else if (!file.canExecute() || !file.canWrite()) msg = "You do not have the required permission to write in the flash folder you selected";
			else
			{
				File file2 = new File(tex2swfTmpFolderName);
				try
				{
					if (file.getCanonicalPath().equals(file2.getCanonicalPath())) msg = "The flash folder and the 'tex2swf.sh' tmp folder cannot be the same folder";
				}
				catch (Exception e)
				{
					msg = e.getMessage();
				}
			}
		}

		//validate 'tex2swf.sh' file.  It must exists and be executable by the user.
		if (msg == null)
		{
			file = new File(tex2swfFolderName + File.separator + "tex2swf.sh");
			if (!file.exists()) msg = "The 'tex2swf.sh' script cannot be found in the folder you specified";
			else if (!file.canExecute()) msg = "You do not have the required permission to execute the 'tex2swf.sh' script";
		}

		//validate 'tex2swf.sh' tmp folder.  It must exits and be a directory in which the user has write permission.
		if (msg == null)
		{
			file = new File(tex2swfTmpFolderName);
			if (!file.exists()) msg = "The 'tex2swf.sh' tmp folder you selected does not exists";
			else if (!file.isDirectory()) msg = "The 'tex2swf.sh' tmp folder you selected is not a directory";
			else if (!file.canExecute() || !file.canWrite()) msg = "You do not have the required permission to write in the 'tex2swf.sh' tmp folder you selected";
		}
		
		if (msg == null)
			return true;

		outputMessage(msg);//communicator.sendMessage(msg, "Error");
		return false;
	}

	/*
	//Attempts to write the specified text to the log file.
	private void log(String text)
	{
		if (text.length() == 0) return;
		if (outputStream == null) 
			
			try
		{
			//Overwrite the logfile
			outputStream = new BufferedOutputStream(new FileOutputStream(logfile));
		}
		catch(Exception e)
		{
			outputMessage("WARNING.  Could not open " + logfile + " for writing, nothing will be logged.\n");
			outputStream = null;
		}
		
		try
		{
			outputStream.write(text.getBytes());
			outputStream.flush();
		}
		catch (Exception e) {}
	}*/

	//Given a string consisting of a space separated list of patterns
	//this method construct an array of regular expression, one regex
	//for each pattern.
	//The patterns in the input are space separated but an individual
	//pattern can contain a space if it is escaped between double
	//quotation marks (" ").  All quotation mark are stripped when
	//producing the regular expressions.
	private static ArrayList<String> extractRegex(String patterns)
	{
		StringTokenizer st = new StringTokenizer(patterns, " ");
		ArrayList<String> regexPatterns = new ArrayList<String>();
		while (st.hasMoreTokens())
		{
			String tok = st.nextToken();
			if (tok.startsWith("\""))
			{
				String arg="";
				while (st.hasMoreTokens() && !tok.endsWith("\""))
				{
					arg += tok + " ";
					tok = st.nextToken();
				}
				arg += tok;
				regexPatterns.add(toJavaRegex(arg.replaceAll("\"","")));
			}
			else
				regexPatterns.add(toJavaRegex(tok));
		}
		return regexPatterns;
	}

	//The format for the patterns we use is slightly different then the one used by Java.
	//The two differences are that
	//   1) . means . as opposed to 'any character' which is used by Java
	//   2) * means any substring (like .* in Java)
	//This allows us to use 'common' patterns like *.tex to denote all
	//files ending in .tex, in java this would be .*\.tex, a syntax that is less
	//intuitive for the unfamiliar user.
	private static String toJavaRegex(String regex)
	{
		String res = ".*(";
		for (int i=0; i<regex.length(); i++)
			if (regex.charAt(i) == '.')
				res += "\\.";
			else if (regex.charAt(i) == '*')
				res += ".*";
			else
				res += regex.charAt(i);
		res += ").*";
		return res;
	}

	//Search the root dir for files whose absolute path match at least one of the regular expression in 'good patterns'
	//but none of the ones in 'bad patterns'.  Additionally files that do not end in .tex are ignored if the texOnly flag
	//is true.  The depth variable is used to indicate how many levels below the 'root' dir to explore.
	//Bad batterns have priority on good patterns meaning that if a file matches both a good and a bad pattern, it will NOT be reported.
	private void getFileRecursively(Collection<String> files, File root, ArrayList<String> goodPatterns, ArrayList<String> badPatterns, int depth, boolean texOnly)
	{
		if (depth == 0) return;
		if (!root.isDirectory()) return;
		if (!root.canRead()) { outputMessage("Could not read directory " + root + ".  Permission denied, directory skipped.\n"); return; } 
		if (!root.canExecute()) { outputMessage("Could not list content of directory " + root + ".  Permission denied, directory skipped.\n"); return; }

		for (File f : root.listFiles())
		{
			if (f.isDirectory() && f.canRead() && f.canExecute())
				getFileRecursively(files, f, goodPatterns, badPatterns, depth-1, texOnly);
			else if (texOnly && !f.getName().endsWith(".tex"))
				continue;
			else
			{
				boolean avoid = false;
				for (String pattern : badPatterns)
				{
					String path = f.getAbsolutePath();
					if (path.matches(pattern))
					{
						avoid = true;
						break;
					}
				}
				if (!avoid)
				{
					for (String pattern : goodPatterns)
					{
						String path = f.getAbsolutePath();
						if (path.matches(pattern))
						{
							files.add(path);
							break;
						}
					}
				}
			}
		}
	}

	//Clean up the name of a folder.
	//  1) Remove leading/trailing white space
	//  2) return 'current dir' when name is empty.
	//  3) Add a File.separator at the end of the name if not already there.
	private String getFolderName(String s)
	{
		if (s.trim().length() == 0) return "./";
		if (s.trim().endsWith(File.separator)) return s.trim();
		return s.trim()+File.separator;
	}

	//This method starts the parsing/exporting thread.
	//It first stores the value of all the parameters defined by the SmacGUI in
	//order to have reliable copies for use by that thread.
	
	public synchronized void start(String rootDir)
	{
		if (myThread != null && myThread.isAlive())
			return;
				
		//parsing related variables
		rootdir = getFolderName(rootDir);
		//recursionDepth = recursiveCheck?Integer.MAX_VALUE:1;
		charset = charsets[0];
		//goodPatternString = goodPattern;
		//badPatternString = badPattern;
		//texOnly = texonly;
		//export related variables (needed by LatexToMeJ)
		//flashFolderName = getFolderName(flashFolder);
		//tex2swfFolderName = getFolderName(tex2swfFolder);
		//tex2swfTmpFolderName = getFolderName(tex2swfTmp);
		//zipFilename = zipQFilename;
		//overwriteFlashFiles = overwriteFlash;
		
		//make sure the user has made a 'reasonable' selection for the various folders
		if (validateFoldersSelection())
		{
			myThread = new Thread(this);
			myThread.start();
		}
	}
	
	//This method starts the parsing/exporting thread.
	//It first stores the value of all the parameters defined by the SmacGUI in
	//order to have reliable copies for use by that thread.
	
	public synchronized void start(String rootDir, String userFolder)
	{
		if (myThread != null && myThread.isAlive())
			return;
				
		//parsing related variables
		rootdir = getFolderName(rootDir);
		//recursionDepth = recursiveCheck?Integer.MAX_VALUE:1;
		charset = charsets[0];
		//goodPatternString = goodPattern;
		//badPatternString = badPattern;
		//texOnly = texonly;
		//export related variables (needed by LatexToMeJ)
		flashFolderName = getFolderName(userFolder + "/" + flashFolderName);
		//tex2swfFolderName = getFolderName(tex2swfFolder);
		tex2swfTmpFolderName = getFolderName(userFolder + "/" +  tex2swfTmpFolderName);
		//zipFilename = zipQFilename;
		//overwriteFlashFiles = overwriteFlash;
		logfile = userFolder + "/" + logfile;
		this.userFolder = userFolder;
		
		//make sure the user has made a 'reasonable' selection for the various folders
		if (validateFoldersSelection())
		{
			myThread = new Thread(this);
			myThread.start();
		}
	}

	//Quit terminates the application, however if the parsing/exporting thread is still
	//running, a pop up appears to confirm the action.
	//The only way to avoid this pop up is to kill the application by some external
	//mean (using 'top', typing Ctrl-c in the terminal used to start the app, etc)
	public void quit()
	{
		if (myThread == null || !myThread.isAlive())
			System.exit(0);

	}

	//Outputs the specified message in the text area and, if logging is
	//turned on, in the log file.
	public void outputMessage(String text)
	{   
		//log(msg);
		//communicator.sendMessage(msg, type);
		
		if (text.length() == 0) return;
		if (outputStream == null){ 
			try
			{
				//Overwrite the logfile
				outputStream = new BufferedOutputStream(new FileOutputStream(logfile));
			}
			catch(Exception e)
			{
				outputStream = null;
			}
		}
		
		try
		{
			outputStream.write(text.getBytes());
			outputStream.flush();
		}
		catch (Exception e) {}
		
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	//Implementation of Runnable Interface
	//
	//Starts a child thread that parses and export the input file(s).
	//Using a separate thread for the heavy crunching makes the gui responsive while
	//parsing/exporting.  This is necessary, for instance, to allow displaying
	//messages in the text area while parsing/exporting.
	public void run()
	{
		outputStream = null;

		try
		{
			//Overwrite the logfile
			outputStream = new BufferedOutputStream(new FileOutputStream(logfile));
		}
		catch(Exception e)
		{
			System.out.println("WARNING.  Could not open " + logfile + " for writing, nothing will be logged.\n");
			outputStream = null;
		}


		ArrayList<String> goodPatternRegex = extractRegex(goodPatternString);
		ArrayList<String> badPatternRegex = extractRegex(badPatternString);

		File root = rootdir.length() == 0 ? new File(".") : new File(rootdir);
		Collection<String> filesToParse = new ArrayList<String>();

		outputMessage("Looking for " + goodPatternRegex + " -- ");
		//Get the files to be parsed/exported.  When recursion is not required the recursionDepth is set to 0
		//meaning only the file in the specified directory will be tested against the desired patterns.
		getFileRecursively(filesToParse, root, goodPatternRegex, badPatternRegex, recursionDepth, texOnly);
		outputMessage("Found " + filesToParse.size() + " file(s) to parse\n");
		SmacParser parser = null;
		try
		{
			//SmacParser parameters are (in order):
			//    o) Where to print the error messages
			//    o) Whether to list all extracted %Tag (false produces a summary, true lists *everything*)
			//    o) Who implements the SmacUI methods
			parser = new SmacParser(false, this);
		}
		catch (Exception e)
		{
			outputMessage(e.getMessage() + "\n");
		}

		//The allQuestions ArrayList will eventually contains all questions that were parsed.
		int lastSize=0;
		for (String filename : filesToParse)
		{
			outputMessage("Parsing file: " + filename + " ");
			try
			{
				//The actual parsing is done in this line, a call to the SmacParser class
				parser.parseFile(filename, charset); 
				outputMessage((parser.numParsed()-lastSize) + " questions parsed.\n");
				lastSize = parser.numParsed();
				
			}
			catch (Exception e)
			{
				outputMessage(e.getMessage() +"\n");
				//return;
			}
		}
		outputMessage("Done.  Parsed a total of " + parser.numParsed() + " questions.\n");
		outputMessage("Validating translations\n");
		try
		{
			parser.validateTranslations();
		}
		catch (Exception e)
		{
			outputMessage(e.getMessage() +"\n");
			//return;
		}
		outputMessage("All translations are valid, any output lines starting with 'WARNING' are for your benefit only.\n");

		outputMessage("Generating SWF for all questions\n");

		File file = new File(getFolderName(flashFolderName) + "questions.xml");
		if (!file.exists())
		{      	 
			// init the xml doc for the questions list to create
			this.questionsWriter.initXMLDoc();
		}else{
			this.questionsWriter.getOldXml(flashFolderName); 
		}

		try
		{
			//LatexToMeJ ltmej = new LatexToMeJ(DB_INFO, TEX2SWF_INFO, this, alwaysUseFirstUser, overwriteFlashFiles, createZip);
			Properties config = new Properties();
			try
			{
				InputStream conf = WebSmacQCreator.class.getResourceAsStream("latextomej.ini");
				config.load(new InputStreamReader(new BufferedInputStream(conf), "UTF-8"));
			}
			catch(Exception e)
			{
				outputMessage(e.getMessage()+" ...exception to read latextomej.ini \n");
			}
			int qid = 1;
			Collection<Map<SmacParser.Tag,String>> allQuestions = parser.allParsedQuestions();
			int numQuestions = allQuestions.size();        

			for (Map<SmacParser.Tag,String> q : allQuestions)
			{
				outputMessage("\rProcessing question " + qid + " of " + numQuestions);

				String qidHash = "";
				try
				{
					int language_id = Integer.parseInt(config.getProperty("language_id."+q.get(SmacParser.Tag.Language)));

					qidHash = encodeQuestionNumber(qid);
					outputMessage("\rquestion code " + qidHash);

					String question_flash_file_prefix = "Q-" + qidHash + "-" + shortLanguage(language_id);
					String feedback_flash_file_prefix = "Q-" + qidHash + "-F-"+shortLanguage(language_id);

					//Check if a flash movie with the same name already exists.
					File flashFile = new File(getFolderName(flashFolderName) + question_flash_file_prefix + ".swf");
					boolean writeXml = true;

					// if movie existe check if the question is in list
					if(flashFile.exists())
						writeXml = this.questionsWriter.verifyQuestion(qid); 

					createFlashFiles(qid++, language_id, question_flash_file_prefix, feedback_flash_file_prefix, q);

					// Process the xml
					if(writeXml)
						this.questionsWriter.addQuestions(question_flash_file_prefix, qid, shortLanguage(language_id));
				}
				catch (Exception e)
				{
					outputMessage(e.getMessage() + " ...error in proccesing question! \n");
				}
			}
		}
		catch (Exception e)
		{
			outputMessage("\n");
			outputMessage("---------------\n");
			outputMessage("Error while generating SWF\n");
			outputMessage("---------------\n");
			outputMessage(e.getMessage() + "\n");
		}

		this.questionsWriter.writeXmlFile(flashFolderName);


		// create the zip of flash files using the swf2zip.sh script

		String commandToExecute = getFolderName(tex2swfFolderName)+"swf2zip.sh " +                    //script name
		zipFilename+".zip " +                                //zip file name
		getFolderName(tex2swfTmpFolderName) + " " +                         //tmp folder
		getFolderName(flashFolderName) + " " + 
		userFolder;                                     //flash folder 
		try {
			Runtime.getRuntime().exec(commandToExecute).waitFor();
			outputMessage("\nDone " + zipFilename + " \n");

		} catch (InterruptedException e) {
			outputMessage(e.getMessage() + " ...error in creating zip file! \n");
			e.printStackTrace();
		} catch (IOException e) {
			outputMessage(e.getMessage() + " ...error in creating zip file! \n");
			e.printStackTrace();
		}


		outputMessage("\nDone\n");
		return;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////
	//This method creates the flash files for the specified question.
	//The parameters are the question_id and language_id together with the question itself.
	//Two files will be created
	//   1) Q-qid-lang.swf        where qid=question_id, lang=2-letter abbreviation for the language
	//   2) Q-qid-F-lang.swf      where qid=question_id, lang=2-letter abbreviation for the language
	//The method just calls the 'tex2swf' script like this:
	//   {script_dir}/tex2swf.sh {tmp_dir}/$1 {flash_dir} {base_dir}     where $1 the file name shown in 1) and 2)
	//This means that
	//    o) the script must be in {script_dir}                             (see variable tex2swfFolderName)
	//    o) the flash files will be copied to {flash_dir}                  (see variable flashFolderName)
	//    o) all intermediate files will be written to {tmp_dir}            (see variable tex2swfTmpFolderName)
	//    o) all graphic files must be in {base_dir}                        (see variable tex2swfBaseDir)
	public void createFlashFiles(int question_id, int language_id, String questionShortFilename, String feedbackShortFilename, Map<SmacParser.Tag, String> question) throws FlashException, Exception
	{
		//////////////////////////////////////////////////////////
		/// Generate the feedback swf
		//Check if a flash movie with the same name already exists.
		File flashFile = new File(getFolderName(flashFolderName)+feedbackShortFilename+".swf");
		if (flashFile.exists() && !overwriteFlashFiles)
			throw new FlashException("File " + flashFolderName+feedbackShortFilename+".swf" + " exists and overwrite is set to false, the Flash movie will not be created.");
		//Build the .tex file from the %Ftext tag
		PrintWriter fout = new PrintWriter(new BufferedWriter(new FileWriter(getFolderName(tex2swfTmpFolderName)+feedbackShortFilename+".tex")));
		fout.println(LATEX_HEADER);
		fout.println(question.get(SmacParser.Tag.Ftext));
		fout.println(LATEX_FOOTER);
		fout.close();

		//Convert .tex to .swf using the tex2swf.sh script
		String commandToExecute = getFolderName(tex2swfFolderName) + "tex2swf.sh " +                    //script name
		tex2swfTmpFolderName + feedbackShortFilename + ".tex " + //file to convert
		tex2swfTmpFolderName + " " +                         //tmp dir
		flashFolderName+ " " +                               //flash dir
		rootdir;                                      //root of grahics dir subtree
		Runtime.getRuntime().exec(commandToExecute).waitFor();

		//Check if the script was sucessful.
		flashFile = new File(getFolderName(flashFolderName) + feedbackShortFilename + ".swf");
		if (!flashFile.exists())
			throw new FlashException("File " + flashFolderName+feedbackShortFilename+".swf could not be created automatically.\n" +
					"See " + tex2swfTmpFolderName+"mej_convert.log for more details.\n" +
					"The failing command was: '" +commandToExecute + "'");

		//////////////////////////////////////////////////////////
		/// Generate the question+answer swf
		// questionShortFilename = "Q-"+question_id+"-"+shortLanguage(language_id);

		//Check if a flash movie with the same name already exists.
		flashFile = new File(flashFolderName+questionShortFilename+".swf");
		if (flashFile.exists() && !overwriteFlashFiles)
			throw new FlashException("File " + flashFolderName+questionShortFilename+".swf" + " exists and overwrite is set to false, the Flash movie will not be created.");

		//Build the .tex file from the %Qtext (and if appropriate %ChoiceX) tags
		fout = new PrintWriter(new BufferedWriter(new FileWriter(tex2swfTmpFolderName+questionShortFilename+".tex")));
		fout.println(LATEX_HEADER);
		fout.println(question.get(SmacParser.Tag.Qtext));
		fout.println();//insert a blank line between the question and answers
		String answerType = question.get(SmacParser.Tag.Type);
		if (answerType.startsWith("MC"))
		{
			fout.println("\\begin{mej-enumerate}");
			for (SmacParser.Tag t : CHOICES)
				if (question.get(t) != null)
					fout.println("\\item " + question.get(t));
			fout.println("\\end{mej-enumerate}");
		}
		else if (answerType.equals("TF"))
		{
			fout.println("\\begin{mej-enumerate}");
			fout.println("\\item " + trueString(language_id));
			fout.println("\\item " + falseString(language_id));
			fout.println("\\end{mej-enumerate}");
		}
		fout.println();
		fout.println(LATEX_FOOTER);
		fout.close();

		//Convert .tex to .swf using the tex2swf.sh script
		commandToExecute = getFolderName(tex2swfFolderName) + "tex2swf.sh " +                    //script name
		tex2swfTmpFolderName+questionShortFilename+".tex " + //file to convert
		tex2swfTmpFolderName + " " +                         //tmp dir
		flashFolderName+ " " +                               //flash dir
		rootdir;                                      //root of grahics dir subtree
		Runtime.getRuntime().exec(commandToExecute).waitFor();

		//Check if the script was sucessful.
		flashFile = new File(flashFolderName + questionShortFilename + ".swf");
		if (!flashFile.exists())
			throw new FlashException("File " + flashFolderName+questionShortFilename+".swf could not be created automatically.\n" +
					"See " + tex2swfTmpFolderName+"mej_convert.log for more details.\n" +
					"The failing command was: '" +commandToExecute + "'");
		outputMessage("INFO: Finishing to add new flash question...\n");

	}
	//////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Used to create a type of hash to encode questions name
	 * @param qid
	 * @return
	 */
	public  String encodeQuestionNumber(int qid)
	{
		StringBuffer keyBuffer = new StringBuffer("");
		Random rand = new Random();
		keyBuffer.setLength(0);
		for(int x = 0; x < 2; x++)
		{
			keyBuffer.append(Integer.toHexString(rand.nextInt(126) + 48));
			keyBuffer.append(Integer.toHexString(rand.nextInt(333) + 65));
			keyBuffer.append(Integer.toHexString(rand.nextInt(777) + 97));
		}

		keyBuffer.append(Integer.toHexString(qid));
		keyBuffer.append(Integer.toHexString(rand.nextInt(126) + 65));
		keyBuffer.append(Integer.toHexString(rand.nextInt(126) + 97));

		return keyBuffer.toString();

	}

	private static String shortLanguage(int language_id)
	{
		switch (language_id)
		{
		case 1: return "fr";
		case 2: return "en";
		default: return ""+language_id;
		}
	}

	private static String trueString(int language_id)
	{
		switch (language_id)
		{
		case 1: return "Vrai";
		case 2: return "True";
		default: return "True";
		}
	}
	private static String falseString(int language_id)
	{
		switch (language_id)
		{
		case 1: return "Faux";
		case 2: return "False";
		default: return "False";
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String root;
		String folder;
		
		WebSmacQCreator creator = new WebSmacQCreator();
		if(args.length == 1)
		{
		   root = args[0];
		   creator.start(root);
		}
		else if(args.length == 2 )
		{
			root = args[0];
			folder = args[1];
			creator.start(root, folder);
		}
		else root = "";		
		
	}

}
