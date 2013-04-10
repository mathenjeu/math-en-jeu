//Author:    David Breton, dbreton@cs.sfu.ca
//
//Copyright: You can do whatever you want with this application.
//           The author is not liable for any loss caused by the
//           use of this application.
//
//Date:      January 2010
//
//Version    0.4  * Replaced Parse/Export Buttons with Parse/Parse+SWF/Parse+SWF+DB Radio Button
//                * Can now generate SWF without having to export to DB
//           0.3  * Added a checkbox for the legacy parsing option (to parse files
//                  using the old (Subject,Category) instead of the new Keywords)
//           0.2  * The various input parameters can now be set through the
//                  smacgui.ini file
//           0.1  * The application can parse .tex files and export them
//                  to the December 2009 DB version of MeJ
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.io.*;

public class SmacGUI extends JFrame implements Runnable, WindowListener, ActionListener, SmacUI
{
	private static final long serialVersionUID = 1L;

	//This list of allowable charset is displayed in the charsetComboBox
	private static final String[] charsets = new String[] {"UTF-8", "ISO-8859-1"};

	//Global variables used to control thread states
	private Thread myThread;             //The thread used to perform the computationally expensive parsing/exporting operations.
	private boolean generateSWF;         //Whether to generate SWF
	private boolean exportToDB;          //Means the application is parsing but won't export to DB
	private boolean createZip;           //Do or not the zip file with questions swf's

	//Global variables used to store GUI state at begining of computation
	private String charset;              //charset used to encode input .tex files
	private String rootdir;              //The directory in which to start looking for files to parse
	private int recursionDepth;          //# of levels below the root dir to look for files to parse (0 or Integer.MAX_VALUE)
	private boolean pedanticParsing;     //Whether to parse %Ftext and %Qtext for suspicious content
	//private boolean legacyParsing;       //When true expect (Subject,Category), when false expect Keywords
	//private boolean validateLinkage;     // update or not the links between questions in tex files and questions in DB
	private boolean texOnly;             //Whether to look only for files ending in .tex when searching for files to parse	
	private String goodPatternString;    //pattern to look for when searching for files to parse
	private String badPatternString;     //pattern to avoid when searching for files to parse (supercedes good patterns)
	private String[] DB_INFO;            //Stores [host,db,user,password] to connect to MySQL database
	private boolean logMessages;         //Whether to log error messages.
	private String logfile;              //The name of the log file.
	private String flashFolderName;      //The name of the folder in which to store the .swf files
	private String tex2swfFolderName;    //The name of the folder in which to find the 'tex2swf.sh' script
	private String tex2swfTmpFolderName; //The name of the temporary folder required by the 'tex2swf.sh' script
	private String zipFilename;          //The name of the file zip to create by flash folder with questions swf's   
	private boolean alwaysUseFirstUser;  //When exporting questions to DB, always use the first match when the DB looks for creators.
	private boolean overwriteFlashFiles; //When true flash files in flashFolderName are overwritten if they already exits, when false the Flash movie creation is skipped for the offending file.
	private boolean overwriteDBEntries;  //When true export to DB overwrites questions already in DB, when false question already in DB are skipped.
	private String[] TEX2SWF_INFO;       //Stores [tex2swfFolderName, tex2swfTmpFolderName, flashFolderName, graphics root directory] to pass to LatexToMeJ
	private static final String questionsXMLFileName = "questions.xml";

	//Global variables used for I/O by the GUI.
	private OutputStream outputStream;     //The output stream to 'logfile' used when logMessages is true
	private String terminateLine = "";     //This variable is used so that message starting with '\r' can overwrite the last line in the text area
	private SmacOptionDialog smacOptionDialog; //A dialog that handles all interaction with the user when exporting files to the DB.

	private final XMLWriter questionsWriter;
	private Properties configSWF; 
	private LatexToMeJ ltmej;		

	//Main Tab
	private JRadioButton parseRadioButton;
	private JRadioButton generateRadioButton;
	private JRadioButton exportRadioButton;
	private JButton startButton;
	private JButton quitButton;
	private JCheckBox logCheckBox;
	private JCheckBox autoScrollCheckBox;
	private JTextArea messageArea;

	//Parser Tab
	private JCheckBox recursiveSearchCheckBox;
	private JCheckBox pedanticCheckBox;
	// private JCheckBox legacyCheckBox;
	private JCheckBox texOnlyCheckBox;
	private JTextField inputFilesFolderTextField;
	private JButton inputFilesFolderBrowseButton;
	private JTextField goodPatternTextField;
	private JTextField badPatternTextField;
	private JComboBox charsetComboBox;

	//Export Tab
	private JTextField flashFolderTextField;
	private JTextField tex2swfFolderTextField;
	private JTextField tex2swfTmpTextField;
	private JTextField zipFilenameTextField;
	private JButton flashFolderBrowseButton;
	private JButton tex2swfFolderBrowseButton;
	private JButton tex2swfTmpBrowseButton;
	private JCheckBox firstUserCheckBox;
	private JCheckBox overwriteFlashFilesCheckBox;
	private JCheckBox overwriteDBEntriesCheckBox;
	private JCheckBox createZipCheckBox;

	//DB Tab
	private JPanel dbParams;
	private JCheckBox useConfigFileCheckBox;
	private JTextField mysqlServerTextField;
	private JTextField mysqlDBTextField;
	private JTextField mysqlUserTextField;
	private JPasswordField mysqlPasswordField;


	public SmacGUI()
	{
		super("SMAC utilities");
		try 
		{
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {}

		this.init(); //reads in the smacgui.ini file
		this.addTabs();

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(Math.min(800,dim.width),Math.min(600,dim.height));
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //The close operation is handled by the quit() method below

		this.addWindowListener(this);
		smacOptionDialog = new SmacOptionDialog(); //used for interaction with the user when exporting
		smacOptionDialog.setSize(new Dimension(800,600));
		this.setVisible(true);

		this.questionsWriter = new XMLWriter(this);
	}

	private void init()
	{
		Properties config = new Properties();
		try
		{
			InputStream conf = SmacGUI.class.getResourceAsStream("smacgui.ini");
			config.load(new InputStreamReader(new BufferedInputStream(conf), "UTF-8"));
		} catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		charset = config.getProperty("charset").trim();
		rootdir = config.getProperty("tex_rootdir").trim();
		goodPatternString = config.getProperty("goodPatternString").trim();
		badPatternString = config.getProperty("badPatternString").trim();
		flashFolderName = config.getProperty("flash_export_dir").trim();
		zipFilename = config.getProperty("zip_export_name").trim();
		tex2swfFolderName = config.getProperty("tex2swf_script_dir").trim();
		tex2swfTmpFolderName = config.getProperty("tex2swf_tmp_dir").trim();
		recursionDepth = config.getProperty("recursive_search").trim().toUpperCase().equals("Y")?Integer.MAX_VALUE:1;
		pedanticParsing = config.getProperty("pedantic_parsing").trim().toUpperCase().equals("Y")?true:false;
		//legacyParsing = config.getProperty("legacy_parsing").trim().toUpperCase().equals("Y")?true:false;
		//validateLinkage = config.getProperty("validate_linkage").trim().toUpperCase().equals("Y")?true:false;
		exportToDB = config.getProperty("default_action").trim().toUpperCase().equals("EXPORT")?true:false;
		generateSWF = exportToDB || !config.getProperty("default_action").trim().toUpperCase().equals("PARSE")?true:false;
		logfile = config.getProperty("logfile").trim();
		if (logfile == null) logfile="smac.log";
	}

	//////////////////////////////////////////////
	//SmacGUI Building methods
	private void addTabs()
	{
		JTabbedPane tp = new JTabbedPane();
		tp.addTab("Main", null, createMainPanel(), "Control center");
		tp.addTab("Parser", null, createParserPanel(), "Set paramaters used by the SMAC parser");
		tp.addTab("Export to DB", null, createExportPanel(), "Set parameters used to export .tex files to the SMAC DB");
		tp.addTab("DB setup", null, createDBPanel(), "Set parameters used to initialize the SMAC database");
		getContentPane().add(tp);
	}
	//It is amazingly difficult to nicely align components with swing.
	//This method takes a matrix of components and arrange them in a
	//way that is nicer-than-average-but-still-not-perfect.
	private JPanel createLignedUpPanel(Component[][] components)
	{
		JPanel p = new JPanel();
		GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
		for (int j=0; j<components[0].length; j++)
		{
			GroupLayout.ParallelGroup pg = layout.createParallelGroup();
			for (int i=0; i<components.length; i++)
				pg.addComponent(components[i][j]);
			hGroup.addGroup(pg);
		}
		layout.setHorizontalGroup(hGroup);
		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		for (int i=0; i<components.length; i++)
		{
			GroupLayout.ParallelGroup pg = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
			for (int j=0; j<components[i].length; j++)
				pg.addComponent(components[i][j]);
			vGroup.addGroup(pg);
		}
		layout.setVerticalGroup(vGroup);
		return p;
	}
	//The main panel consists of
	//   o) A text area to display messages
	//   o) A check box to indicate whether the messages should be copied to file
	//   o) A Parse button to start parsing
	//   o) A Export button to parse AND export to DB
	//   o) A Quit button to close the application
	private JPanel createMainPanel()
	{
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel np = new JPanel();
		np.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		np.setLayout(new BoxLayout(np, BoxLayout.X_AXIS));
		autoScrollCheckBox = new JCheckBox("Auto scroll", true);
		autoScrollCheckBox.setToolTipText("Select this option if you want the text area to always show its last line");
		np.add(autoScrollCheckBox);
		logCheckBox = new JCheckBox("Log messages", false);
		logCheckBox.setToolTipText("Messages are logged in the file " + logfile);
		np.add(Box.createHorizontalGlue());
		np.add(logCheckBox);
		mainPanel.add(np, BorderLayout.NORTH);

		messageArea = new JTextArea(15, 40);
		messageArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(messageArea);
		mainPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel bp = new JPanel();
		ButtonGroup bg = new ButtonGroup();
		parseRadioButton = new JRadioButton("Parse", !generateSWF);
		generateRadioButton = new JRadioButton("Parse+SWF", generateSWF && !exportToDB);
		exportRadioButton = new JRadioButton("Parse+SWF+DB", exportToDB);
		exportRadioButton.setEnabled(true);
		startButton = new JButton("Start");
		startButton.addActionListener(this);
		quitButton = new JButton("Quit");
		quitButton.addActionListener(this);
		bg.add(parseRadioButton);
		bg.add(generateRadioButton);
		bg.add(exportRadioButton);
		bp.add(parseRadioButton);
		bp.add(generateRadioButton);
		bp.add(exportRadioButton);
		bp.add(startButton);
		bp.add(quitButton);
		mainPanel.add(bp, BorderLayout.SOUTH);
		return mainPanel;
	}
	//The parser panel consists of
	//  o) Two text fields to specify patterns when search for files to parse and a check box
	//     to indicate whether only .tex files should be considered for parsing.
	//     One text field specifies patterns that must be present, the other patterns to avoid.
	//     The 'avoidance' patterns have priority on the 'good' patterns.  In addition when the
	//     .tex only check box is on, files that do not end in .tex are automatically ignored.
	//  o) A text field to specify the directory in which to start looking for files to parse,
	//     together with a Browse button to easily browse the file system for this directory.
	//  o) A combo box to select the encoding of the input .tex files.
	//  o) A check box to indicate whether to recursively look down the root directory for
	//     additional files to parse
	private JPanel createParserPanel()
	{
		goodPatternTextField = new JTextField(goodPatternString);
		badPatternTextField = new JTextField(badPatternString);
		inputFilesFolderTextField = new JTextField(rootdir);
		charsetComboBox = new JComboBox(charsets);
		charsetComboBox.setSelectedItem(charset);

		texOnlyCheckBox = new JCheckBox(".tex only", true);
		inputFilesFolderBrowseButton = new JButton("Browse...");
		inputFilesFolderBrowseButton.addActionListener(this);
		recursiveSearchCheckBox = new JCheckBox("Recursive Search", recursionDepth==Integer.MAX_VALUE);
		recursiveSearchCheckBox.setToolTipText("Recursively search the input file folders for more files to parse");
		pedanticCheckBox = new JCheckBox("Pedantic parsing", pedanticParsing==true);
		pedanticCheckBox.setToolTipText("Parse %Ftext and %Qtext for suspicious content");
		//legacyCheckBox = new JCheckBox("Legacy parsing (export to DB won't work)", legacyParsing==true);
		//legacyCheckBox.setToolTipText("Parse files using old (Subject,Category) instead of Keywords");
		//legacyCheckBox.addActionListener(this);

		Component c[][] = new Component[4][3];
		c[0][0] = new JLabel("Input files pattern"); c[0][1] = goodPatternTextField;       c[0][2] = texOnlyCheckBox;
		c[1][0] = new JLabel("Patterns to avoid");   c[1][1] = badPatternTextField;        c[1][2] = new JLabel();
		c[2][0] = new JLabel("Input files folder");  c[2][1] = inputFilesFolderTextField;  c[2][2] = inputFilesFolderBrowseButton;
		c[3][0] = new JLabel("Input files charset"); c[3][1] = charsetComboBox;            c[3][2] = new JLabel();    
		JPanel p = createLignedUpPanel(c);

		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
		checkBoxPanel.add(pedanticCheckBox);
		//checkBoxPanel.add(legacyCheckBox);
		checkBoxPanel.add(recursiveSearchCheckBox);
		JPanel q = new JPanel(new FlowLayout(FlowLayout.LEFT));
		q.add(checkBoxPanel);

		JPanel parserPanel = new JPanel();
		parserPanel.setLayout(new BoxLayout(parserPanel, BoxLayout.Y_AXIS));
		parserPanel.add(p);
		parserPanel.add(q);
		return parserPanel;
	}
	//The export panel consists of
	//   o) Three text fields and two radio buttons that sets the parameters for the 'tex2swf.sh' script.
	//      The parameters that need to be set are: the folder in which to store the .swf files, the folder
	//      in which to find the 'tex2swf.sh' script and a tmp folder for the script
	//   o) A set of check box that indicate the default behavior when input is required from the user.
	private JPanel createExportPanel()
	{
		flashFolderTextField = new JTextField(flashFolderName, 40);
		flashFolderTextField.setMinimumSize(flashFolderTextField.getPreferredSize());
		tex2swfFolderTextField = new JTextField(tex2swfFolderName, 40);
		tex2swfTmpTextField = new JTextField(tex2swfTmpFolderName, 40);
		zipFilenameTextField = new JTextField(zipFilename, 40);
		flashFolderBrowseButton = new JButton("Browse...");
		flashFolderBrowseButton.addActionListener(this);
		tex2swfFolderBrowseButton = new JButton("Browse...");
		tex2swfFolderBrowseButton.addActionListener(this);
		tex2swfTmpBrowseButton = new JButton("Browse...");
		tex2swfTmpBrowseButton.addActionListener(this);

		firstUserCheckBox = new JCheckBox("Always select first user when a match exist", false);
		firstUserCheckBox.setToolTipText("<html>When uncheck a pop up will appear asking for a selection when the creator of a .tex file matches several DB users<br>" +
		"Otherwise the first DB match will be used each time</html>");
		overwriteFlashFilesCheckBox = new JCheckBox("Overwrite .swf", true);
		overwriteFlashFilesCheckBox.setToolTipText("<html>When checked, created .swf overwrite previously created .swf with the same name.<br>" +
		"When unchecked, .swf creation is skipped for already existing files</html>");
		overwriteDBEntriesCheckBox = new JCheckBox("Overwrite DB questions", true);
		overwriteDBEntriesCheckBox.setToolTipText("<html>When checked, exporting to DB overwrites questions already present.<br>" +
		"When unchecked, questions already in the DB are updatted.</html>");

		createZipCheckBox = new JCheckBox("Create Questions Zip", true);
		createZipCheckBox.setToolTipText("<html>When checked, create zip file with directory of questions swf's.</html>");

		JLabel flashFolderLabel = new JLabel("Flash folder");
		flashFolderLabel.setToolTipText("Where to store the .swf file produced by tex2swf. Empty means {current directory}");
		JLabel tex2swfFolderLabel = new JLabel("tex2swf folder");
		tex2swfFolderLabel.setToolTipText("The location of the tex2swf script. Empty means {current directory}");
		JLabel tex2swfTmpLabel = new JLabel("tex2swf tmp folder");
		tex2swfTmpLabel.setToolTipText("The folder where tex2swf creates AND deletes all intermediate files. Empty means {current directory}");
		JLabel zipFilenameLabel = new JLabel("zip file name");
		zipFilenameLabel.setToolTipText("The name of zip file created swf2zip.sh and that contain all questions swf files and questions.xml file from flash directory.");
		JLabel nullLabel = new JLabel("");

		Component c[][] = new Component[4][3];
		c[0][0] = flashFolderLabel;   c[0][1] = flashFolderTextField;    c[0][2] = flashFolderBrowseButton;
		c[1][0] = tex2swfFolderLabel; c[1][1] = tex2swfFolderTextField;  c[1][2] = tex2swfFolderBrowseButton;
		c[2][0] = tex2swfTmpLabel;    c[2][1] = tex2swfTmpTextField;     c[2][2] = tex2swfTmpBrowseButton;
		c[3][0] = zipFilenameLabel;   c[3][1] = zipFilenameTextField;    c[3][2] = nullLabel;

		JPanel p = createLignedUpPanel(c);
		JPanel flashPanel = new JPanel();
		flashPanel.setLayout(new BoxLayout(flashPanel, BoxLayout.Y_AXIS));
		flashPanel.setBorder(BorderFactory.createTitledBorder("tex2swf and swf2zip parameters"));
		flashPanel.add(p);
		flashPanel.setMaximumSize(flashPanel.getMinimumSize());
		flashPanel.setAlignmentX(LEFT_ALIGNMENT);

		JPanel exportPanel = new JPanel();
		exportPanel.setLayout(new BoxLayout(exportPanel, BoxLayout.Y_AXIS));
		exportPanel.add(flashPanel);
		exportPanel.add(firstUserCheckBox);
		exportPanel.add(overwriteFlashFilesCheckBox);
		exportPanel.add(overwriteDBEntriesCheckBox);
		exportPanel.add(createZipCheckBox);
		return exportPanel;
	}
	//The DB panel consists of
	//  o) Four text field to specify the url of the MySQL server, the database name
	//     the mysql user and password to use for the connection.
	//  o) A check box that indicates whether all this information should be taken
	//     from the config file (latextomej.ini) located inside the .jar archive.
	private JPanel createDBPanel()
	{
		useConfigFileCheckBox = new JCheckBox("Use config file", true);
		useConfigFileCheckBox.addActionListener(this);

		mysqlServerTextField = new JTextField(32);
		mysqlDBTextField = new JTextField(32);
		mysqlUserTextField = new JTextField(32);
		mysqlPasswordField = new JPasswordField(32);

		Component c[][] = new Component[4][2];
		c[0][0] = new JLabel("MySQL server"); c[0][1] = mysqlServerTextField;
		c[1][0] = new JLabel("DB name");      c[1][1] = mysqlDBTextField;
		c[2][0] = new JLabel("DB user");      c[2][1] = mysqlUserTextField;
		c[3][0] = new JLabel("DB password");  c[3][1] = mysqlPasswordField;

		dbParams = createLignedUpPanel(c);
		dbParams.setBorder(BorderFactory.createTitledBorder("Database setup"));
		dbParams.setVisible(!useConfigFileCheckBox.isSelected());
		dbParams.setAlignmentX(LEFT_ALIGNMENT);

		JPanel dbPanel = new JPanel();
		dbPanel.setLayout(new BoxLayout(dbPanel, BoxLayout.Y_AXIS));
		dbPanel.add(useConfigFileCheckBox);
		dbPanel.add(dbParams);
		return dbPanel;
	}
	/////////////////////////////////////////



	//Pops up a file selection window to choose a folder on the file system
	//and stores the absolute path name in the specified text field.
	private void selectFolder(JTextField tf)
	{
		JFileChooser chooser = new JFileChooser(".");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int ret = chooser.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION)
			tf.setText(chooser.getSelectedFile().getAbsolutePath());
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

		if (generateSWF)
		{
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
				file = new File(tex2swfFolderName+File.separator+"tex2swf.sh");
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
		}

		if (msg == null)
			return true;

		JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
		return false;
	}


	//Attempts to write the specified text to the log file.
	private void log(String text)
	{
		if (outputStream == null) return;
		if (text.length() == 0) return;
		try
		{
			outputStream.write(text.getBytes());
			outputStream.flush();
		}
		catch (Exception e) {}
	}


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
	private void getFileRecursively(Collection<String> files, File root, ArrayList<String> goodPatterns, ArrayList<String> badPatterns, int depth)
	{
		if (depth == 0) return;
		if (!root.isDirectory()) return;
		if (!root.canRead()) { outputMessage("Could not read directory " + root + ".  Permission denied, directory skipped.\n"); return; } 
		if (!root.canExecute()) { outputMessage("Could not list content of directory " + root + ".  Permission denied, directory skipped.\n"); return; }

		for (File f : root.listFiles())
		{
			if (f.isDirectory() && f.canRead() && f.canExecute())
				getFileRecursively(files, f, goodPatterns, badPatterns, depth-1);
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
	//  3) Add a File.separator at the end of the name if not alreday there.
	private String getFolderName(String s)
	{
		if (s.trim().length() == 0) return "./";
		if (s.trim().endsWith(File.separator)) return s.trim();
		return s.trim()+File.separator;
	}

	//This method starts the parsing/exporting thread.
	//It first stores the value of all the parameters defined by the SmacGUI in
	//order to have reliable copies for use by that thread.
	//There is one exception:
	//   The autoScrollCheckBox is not 'cached' that way
	//   because it can be turned on and off after the parsing/exporting thread
	//   was launched.
	private synchronized void start(boolean generate, boolean export)
	{
		if (myThread != null && myThread.isAlive())
			return;
		generateSWF = generate;
		exportToDB = export;

		//I/O related variables
		logMessages = logCheckBox.isSelected();
		//parsing related variables
		rootdir = getFolderName(inputFilesFolderTextField.getText());
		recursionDepth = recursiveSearchCheckBox.isSelected()?Integer.MAX_VALUE:1;
		pedanticParsing = pedanticCheckBox.isSelected();
		//legacyParsing = legacyCheckBox.isSelected();
		charset = charsets[charsetComboBox.getSelectedIndex()];
		goodPatternString = goodPatternTextField.getText();
		badPatternString = badPatternTextField.getText();
		texOnly = texOnlyCheckBox.isSelected();
		//export related variables (needed by LatexToMeJ)
		flashFolderName = getFolderName(flashFolderTextField.getText());
		tex2swfFolderName = getFolderName(tex2swfFolderTextField.getText());
		tex2swfTmpFolderName = getFolderName(tex2swfTmpTextField.getText());
		zipFilename = zipFilenameTextField.getText();
		alwaysUseFirstUser = firstUserCheckBox.isSelected();
		overwriteFlashFiles = overwriteFlashFilesCheckBox.isSelected();
		overwriteDBEntries = overwriteDBEntriesCheckBox.isSelected();
		createZip = createZipCheckBox.isSelected();

		TEX2SWF_INFO = new String[]{tex2swfFolderName, tex2swfTmpFolderName, flashFolderName, rootdir, zipFilename};
		configSWF = new Properties();

		//DB setup variables
		DB_INFO = useConfigFileCheckBox.isSelected()? null : new String[]{mysqlServerTextField.getText().trim(), 
			mysqlDBTextField.getText().trim(), mysqlUserTextField.getText().trim(), mysqlPasswordField.getPassword().toString().trim()};			

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
		int n = JOptionPane.showConfirmDialog(this, "The application is still busy parsing and/or exporting, are you sure you want to quit.", "Really?", JOptionPane.YES_NO_OPTION);
		switch (n)
		{
		case JOptionPane.YES_OPTION:
			System.exit(0);
		default:
			break;
		}
	}


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Implementation of Runnable Interface
	//
	//Starts a child thread that parses and export the input file(s).
	//Using a separate thread for the heavy crunching makes the gui responsive while
	//parsing/exporting.  This is necessary, for instance, to allow displaying
	//messages in the text area while parsing/exporting.
	public void run()
	{
		outputStream = null;
		if (logMessages)
		{
			try
			{
				//Overwrite the logfile
				outputStream = new BufferedOutputStream(new FileOutputStream(logfile));
			}
			catch(Exception e)
			{
				outputStream = null;
				System.out.println(" output stream not created! ");
			}
		}

		// get configuration file for swf creation
		try
		{
			InputStream conf = SmacGUI.class.getResourceAsStream("latextomej.ini");
			configSWF.load(new InputStreamReader(new BufferedInputStream(conf), "UTF-8"));
		}
		catch(Exception e)
		{
			outputMessage(e.getMessage() + " ...exception to read latextomej.ini \n");
		}

		try 
		{
			ltmej = new LatexToMeJ(DB_INFO, TEX2SWF_INFO, this, alwaysUseFirstUser, overwriteFlashFiles, createZip);
		} catch (Exception e1) {
			outputMessage(e1.getMessage() + " ...exception to create ltmej instance... \n");
		}	

		ArrayList<String> goodPatternRegex = extractRegex(goodPatternString);
		ArrayList<String> badPatternRegex = extractRegex(badPatternString);

		File root = rootdir.length() == 0 ? new File(".") : new File(rootdir);
		Collection<String> filesToParse = new ArrayList<String>();

		outputMessage("Looking for " + goodPatternRegex + " -- ");
		//Get the files to be parsed/exported.  When recursion is not required the recursionDepth is set to 0
		//meaning only the file in the specified directory will be tested against the desired patterns.
		getFileRecursively(filesToParse, root, goodPatternRegex, badPatternRegex, recursionDepth);
		outputMessage("Found " + filesToParse.size() + " file(s) to parse\n");
		SmacParser parser = null;
		try
		{
			//SmacParser parameters are (in order):
			//    o) Where to print the error messages
			//    o) Whether to list all extracted %Tag (false produces a summary, true lists *everything*)
			//    o) Whether to use pedantic parsing
			//    o) Who implements the SmacUI methods
			parser = new SmacParser(outputStream, false, pedanticParsing, this);
		}
		catch (Exception e)
		{
			outputMessage(e.getMessage() + "\n");
		}

		//The allQuestions ArrayList will eventually contains all questions that were parsed.
		int lastSize=0;
		for (String filename : filesToParse)
		{
			outputMessage("Parsing file: " + filename + " \n");
			try
			{
				//The actual parsing is done in this line, a call to the SmacParser class
				parser.parseFile(filename, charset); 
				outputMessage((parser.numParsed() - lastSize) + " questions parsed.\n");
				lastSize = parser.numParsed();
				//Technically sleeping should not be necessary but it appears to help
				//the redrawing triggered by the scroll pane for the messageArea.
				if (autoScrollCheckBox.isSelected())
					Thread.sleep(10); //the parameter is a number of milliseconds
			}
			catch (Exception e)
			{
				outputMessage(e.getMessage() + " error in parsing question. Skipping this file... " + filename + "\n");
				//return;
			}
		}

		int numb = parser == null  ? 0 : parser.numParsed();
		outputMessage("Done.  Parsed a total of " + numb + " questions.\nValidating translations\n\n");
		try
		{
			parser.validateTranslations();
		}
		catch (Exception e)
		{
			outputMessage(e.getMessage() +"\n");
			return;
		}
		outputMessage("All translations are valid, any output lines starting with 'WARNING' are for your benefit only.\n");


		if (generateSWF)
			createFlashFiles(parser);

	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @param parser
	 */
	private void createFlashFiles(SmacParser parser) 
	{		
		outputMessage("Generating SWF for all questions\n");

		File file = new File(flashFolderName + SmacGUI.questionsXMLFileName);
		this.outputMessage(flashFolderName);  
		if (!file.exists())
		{    this.outputMessage("get OldXMl");  	 
			// init the xml doc for the questions list to create
			this.questionsWriter.initXMLDoc();
		}else{
			
			this.questionsWriter.getOldXml(flashFolderName + SmacGUI.questionsXMLFileName); 
		}

		int qid = 1;
		Collection<Map<Tag, String>> allQuestions = parser.allParsedQuestions();
		int numQuestions = allQuestions.size();

		if(exportToDB)
		{
			outputMessage("\rCreate with adding questions to the DB.\n");
			for (Map<Tag, String> q : allQuestions)
			{
				qid = findMatchAndExportInDB(q);
				if(qid > 0){
					outputMessage("\rProcessing question " + qid + "\n");
					createFlash(qid, q);
				}else{
					outputMessage("\rBad id : " + qid + " skipping question flash files\n" );
				}
			}
		}else{
			outputMessage("\rCreate without adding questions to the DB.\n");
			for (Map<Tag, String> q : allQuestions)
			{
				outputMessage("\rProcessing question " + qid + " of " + numQuestions);
				createFlash(qid, q);
				qid++;
			}
		}


		this.questionsWriter.writeXmlFile(flashFolderName + SmacGUI.questionsXMLFileName);


		// create the zip of flash files using the swf2zip.sh script
		if(createZip){
			String commandToExecute = tex2swfFolderName+"swf2zip.sh " +                 //script name
			zipFilename+".zip " +                                                       //zip file name
			tex2swfTmpFolderName + " " +                                                //tmp folder
			flashFolderName;                                                            //flash folder 
			try {
				Runtime.getRuntime().exec(commandToExecute).waitFor();
				outputMessage("\rDone " + zipFilename + " \n");

			} catch (InterruptedException e) {
				outputMessage("\r" + e.getMessage() + " ...error in creating zip file! \n");
				e.printStackTrace();
			} catch (IOException e) {
				outputMessage("\r" + e.getMessage() + " ...error in creating zip file! \n");
				e.printStackTrace();
			}
		}

		outputMessage("\rDone\n");
		return;
	}

	/**
	 * @param qid
	 * @param numQuestions
	 * @param q
	 * @return
	 */
	private void createFlash(int qid, Map<Tag, String> q) 
	{	
		int language_id = Integer.parseInt(configSWF.getProperty("language_id." + q.get(Tag.Language)));
		outputMessage("\r question id : " + qid + " and language_id : " + language_id);

		String question_flash_file_prefix = "Q-" + qid + "-" + LatexToMeJ.shortLanguage(language_id);
		String feedback_flash_file_prefix = "Q-" + qid + "-F-" + LatexToMeJ.shortLanguage(language_id);

		//Check if a flash movie with the same name already exists.
		File flashFile = new File(flashFolderName + question_flash_file_prefix + ".swf");
		boolean writeXml = true;

		// if movie exist check if the question is in list
		if(flashFile.exists())
			// tempoararily desactivate option
			writeXml = this.questionsWriter.verifyQuestion(qid); 

		try
		{
			
			ltmej.createFlashFiles(qid, language_id, question_flash_file_prefix, feedback_flash_file_prefix, q);  ///???
			
		}
		catch (Exception e)
		{
			outputMessage("\r" + e.getMessage() + " ...error in proccesing question!");
		}
		
		// Process the xml
		if(writeXml)
			this.questionsWriter.addQuestions(question_flash_file_prefix, qid, LatexToMeJ.shortLanguage(language_id));
	}

	/**
	 * export the questions in DB
	 * @param parser
	 */
	private int findMatchAndExportInDB(Map<Tag, String> q) {
		//This is where the exporting is done, a call to the LatexToMeJ class.
		int qid = -1;
		try
		{
			qid = ltmej.findMatchAndExportInDB(q, overwriteDBEntries);
			outputMessage("\rSmacGUI - Done exporting file to DB.\n");
		}		
		catch (SQLException ex)
		{
			outputMessage("\n");
			outputMessage("\rError in sql while exporting");
			outputMessage("\r" + ex.getMessage());

		}catch (Exception e)
		{
			outputMessage("\n");
			outputMessage("\rError while exporting");
			outputMessage("\r" + e.getMessage());
		}

		return qid;
	}


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Implementation of WindowListener Interface
	//
	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) { quit(); } //triggered when the dispose() method is used
	public void windowClosing(WindowEvent e) { quit(); } //triggered when the app is closed through application menu
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Implementation of ActionListener Interface
	//
	public void actionPerformed(ActionEvent ae)
	{
		Object ob = ae.getSource();
		if (ob == useConfigFileCheckBox)
			dbParams.setVisible(!useConfigFileCheckBox.isSelected());

		else if (ob == inputFilesFolderBrowseButton)
			selectFolder(inputFilesFolderTextField);
		else if (ob == flashFolderBrowseButton)
			selectFolder(flashFolderTextField);
		else if (ob == tex2swfFolderBrowseButton)
			selectFolder(tex2swfFolderTextField);
		else if (ob == tex2swfTmpBrowseButton)
			selectFolder(tex2swfTmpTextField);
		else if (ob == startButton)
			start(generateRadioButton.isSelected()||exportRadioButton.isSelected(),exportRadioButton.isSelected());
		else if (ob == quitButton)
			quit();
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Implementation of SmacUI interface
	//
	//Handles the interaction with the user during the export phase.
	//When a creator (or source) isn't found in the DB, a list of possible
	//match are presented and the user is asked to either select one or create
	//a new entry in the DB.  This is most useful when a source name is 
	//spelled differently in a .tex file then it is in the DB.
	public int selectOption(String msg, ArrayList<SmacOptionData> options)
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setLayout(new GridLayout(0,1));
		int size = options.size();
		JRadioButton[] buttons = new JRadioButton[size];
		ButtonGroup bg = new ButtonGroup();
		msg = msg.replaceAll("\n", "<br/>");
		panel.add(new JLabel("<html><b>" + msg + "</b><br><br></html>"), BorderLayout.NORTH);

		JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));

		for (int i=0; i<size; i++)
		{
			buttons[i] = new JRadioButton(options.get(i).optionString);
			buttons[i].addActionListener(smacOptionDialog);
			buttons[i].setActionCommand(""+i);
			bg.add(buttons[i]);
			optionPanel.add(buttons[i]);
			if (i == 0) buttons[i].setSelected(true);
		}
		panel.add(optionPanel, BorderLayout.CENTER);
		smacOptionDialog.setMessage(panel);
		smacOptionDialog.setSelection(0);
		smacOptionDialog.createDialog(this,"").setVisible(true);
		smacOptionDialog.setSize(new Dimension(800,600));

		Object value = smacOptionDialog.getValue();
		if (value == null || value == JOptionPane.UNINITIALIZED_VALUE) 
			return Integer.MIN_VALUE; //The dialog was closed by some other mean then pressing the 'ok' button

		return options.get(smacOptionDialog.getSelection()).optionId;
	}

	//Outputs the specified message in the text area and, if logging is
	//turned on, in the log file.
	public void outputMessage(String msg)
	{
		if (msg.startsWith("\r"))
		{
			int linenumber = messageArea.getLineCount()-1;
			try { 
				messageArea.replaceRange(msg.substring(1), messageArea.getLineStartOffset(linenumber),  messageArea.getLineEndOffset(linenumber)); }
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
			log(msg.substring(1));
			terminateLine = "\n";
		}
		else
		{
			messageArea.append(terminateLine + msg);
			log(msg);
			terminateLine = "";
		}
		if (autoScrollCheckBox.isSelected())
			messageArea.setCaretPosition(messageArea.getDocument().getLength());
	}


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
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		SmacGUI gui = new SmacGUI();
	}
}