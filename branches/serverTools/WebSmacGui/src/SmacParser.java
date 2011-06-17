//Author:    David Breton, dbreton@cs.sfu.ca
//
//Copyright: You can do whatever you want with this application.
//           The author is not liable for any loss caused by the
//           use of this application.
//
//Date:      June 2011
//
//Version:   0.18 * Web version  
//           0.17 * French pedantic parsing
//                  Errors are thrown when tags of translated questions conflict
//                  Errors are thrown for questions with identical identifiers
//           0.16 * Each question must now have a unique identifier which is either
//                  (Source,Title,Question,Language) or (Creator,Question,Language)
//                  depending on whether the %Source tag is defined.
//           0.15 * Added tag %Keywords, eliminated %Subject and %Category.
//                * Added -legacy option to parse file containing %Subject and %Category
//                * The %Source tag implies %Title
//                * The %Question and %Creator tags are now mandatory and are used as a
//                  unique id WHEN there are no %Source.  When a %Source is present
//                  %Question is still just the label of the question in the %Source
//           0.14 * Fix a bug. %Answer was not being validated properly for Type=TF.
//           0.13 * Added a -pedantic flag that searches %[FQ]text for inconsistencies.
//           0.12 * Feedback is written on an outputstream instead of System.out.
//                * Enhanced tag extraction feature.
//                * All 'whitespace' is trimmed at the beginning and end of
//                  %Qtext, %Ftext and all the %ChoiceX.
//           0.11 * MC questions with 5 answers now have Tag.Type=MC5 instead of MC.
//           0.10 * There is a command line option to find .tex files recursively.
//           0.9  * There is a command line option to choose the input charset.
//                * The \pagebreak at the start of %Ftext and %Qtext are optional
//                  and are stripped if present.
//                * The \\ at the end of each %ChoiceX are stripped.
//           0.8  * The %ChoiceX tag format must start with '(X) '.
//                * The \\ at the end of the last %ChoiceX are stripped.
//                * The %Ftext and %Qtext tags must start with a \pagebreak.
//           0.7  * Added qdiff.length to config file.
//                * Force input file to be read in UTF-8 charset.
//           0.6  * Changed config file format and added config to jar file.
//           0.5  * Added config file and translated tags.
//           0.4  * Fix parser bug when %Type=MC but %Answer > %Choices.
//           0.3  * Fix parser bug when comments appear after the last %End tag.
//           0.2  * Fix parser bug that did not require last %End tag.
//                * Made a lookup table of allowable subjects->categories.
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class SmacParser implements SmacUI
{
  public static final String VERSION = "0.18";
  private static int QDIFF_LENGTH = 18;
  private static final String allPossibleMultipleChoiceAnswers = "ABCDEFGH";
  private SmacUI ui = null;
  public enum Tag { NONE, COMMENT,
      Begin, End,
      Language, Source, Title, Question, Subject, Category, Keywords, Type,
      Choices, Answer, Creator, Translator,
      Rdifficulty, Qdifficulty,
      Qtext, ChoiceA, ChoiceB, ChoiceC, ChoiceD, ChoiceE, ChoiceF, ChoiceG, ChoiceH, Ftext};
  //Specify which header tags are mandatory.  This is a default list that can be overwritten by the value
  //of the tag.mandatory property of the config file.
  private static Tag[] MandatoryHeaderTags = new Tag[]{Tag.Language, Tag.Title, Tag.Question, Tag.Keywords, Tag.Type,
                                                       Tag.Answer, Tag.Creator};

  //Matches strings starting with a latex 'length' specifier, e.g.: [3.14 cm] or [40px] or [-1.5 em] or ...
  private static final Pattern LATEX_LENGTH = Pattern.compile("^\\[-?[0-9.]+\\s*[a-zA-Z][a-zA-Z]\\](.*)$", Pattern.DOTALL);
  private static final Pattern LATEX_FRACTION = Pattern.compile("^\\\\frac\\{([^}]+)\\}\\{([^}]+)\\}.*", Pattern.DOTALL);
  //The stringToTagMap maps all translations for the name of a tag to the given tag.  Tags are
  //keywords recognized by the parser, they start with the '%' sign in the .tex file.  The list
  //of recgonized tags is listed in the Tag enum above.  For example both the strings "Begin"
  //and "Début" map to the tag Tag.Begin so in the .tex file both %Begin and %Début can be use
  //to signify Tag.Begin
  private final Map<String, Tag> stringToTagMap = new TreeMap<String, Tag>();
  //The allowableValuesMap maps 'keys' to a list of allowable values.  The keys are found in the config file
  //under tag.values.XXX.  If YYY is a translation for XXX, it is wrong to use both tag.values.XXX and tag.values.YYY
  //in the config file.  Proper practice is to use tag.values.XXX and translation.XXX=[YYY,ZZZ,etc...]
  private final Map<String, ArrayList<String>> allowableValuesMap = new TreeMap<String, ArrayList<String>>();
  //The translationMap maps keywords to a list of equivalent strings.  When a keyword is expected it can be replaced
  //by any of the translated strings equivalent to it.  It is not necessary to add the keyword itself to the list of
  //translations.  To add an entry to this map, simply add a translation.XXX=[YYY,ZZZ,etc...] line to the config file.
  private final Map<String, ArrayList<String>> translationsMap = new TreeMap<String, ArrayList<String>>();
  //Once extracted, the questions end up in this set.  It's a set so every question can only appear once.
  private final TreeSet<Map<Tag,String>> allParsedQuestions = new TreeSet<Map<Tag,String>>(new QuestionComparator());
  //The tags to extract are the ones specified on the command line.
  private final Map<Tag, Collection<String>> allExtractedTagArguments = new TreeMap<Tag, Collection<String>>();
  //When false the extracted tags with the same values are grouped together and only the group size is displayed
  //When true all tags are listed
  private boolean listAllExtractedTagArguments = false;
    
  private final Map<Integer,String> rdiffToQdiffMap = new TreeMap<Integer,String>();

  //The output stream used to display feedback.
  //private OutputStream messageStream;

  //Populates the stringToTagMap, allowableValuesMap and translationsMap from
  //the data contained in the specified config file.
  private void populateTagHandlingMaps(Properties config) throws Exception
  {
    StringTokenizer st;
    String delimiters = " \t\n\r\f[],";
    allowableValuesMap.clear();
    translationsMap.clear();
    stringToTagMap.clear();
    rdiffToQdiffMap.clear();
    
    for (String name : config.stringPropertyNames())
    {
      //Fill in the allowableValuesMap
      if (name.startsWith("tag.values."))
      {
        String key = name.substring("tag.values.".length());
        st = new StringTokenizer(config.getProperty(name), delimiters);
        ArrayList<String> values = new ArrayList<String>();
        while (st.hasMoreTokens())
          values.add(st.nextToken().trim());
        allowableValuesMap.put(key, values);
      }
      //Fill in the translationsMap
      else if (name.startsWith("translation."))
      {
        String key = name.substring("translation.".length());
        st = new StringTokenizer(config.getProperty(name), delimiters);
        ArrayList<String> values = new ArrayList<String>();
        while (st.hasMoreTokens())
          values.add(st.nextToken().trim());
        translationsMap.put(key, values);
      }
      else if (name.startsWith("mapping.rdiff."))
      {
        String rdiffId = name.substring("mapping.rdiff.".length());
        st = new StringTokenizer(config.getProperty(name), delimiters);
        String qdiff = "";
        while (st.hasMoreTokens())
        {
          qdiff += st.nextToken().trim();
          if (st.hasMoreTokens())
            qdiff += ",";
        }
        rdiffToQdiffMap.put(Integer.parseInt(rdiffId),qdiff);
      }
      
    }

    //Make sure all required value lists are present.
    if (allowableValuesMap.get("Language") == null) throw new Exception("Language tag requires a value list.  That list is missing from the configuration file.");
    if (allowableValuesMap.get("Type") == null) throw new Exception("Type tag requires a value list.  That list is missing from the configuration file.");
    if (allowableValuesMap.get("Type.TF") == null) throw new Exception("Type.TF requires a value list.  That list is missing from the configuration file.");
    
    else if (allowableValuesMap.get("Keywords") == null)
      throw new Exception("Keywords tag requires a value list.  That list is missing from the configuration file.");

    //Fill in the stringToTagMap: All translations of a tag name get map to the Tag itself.
    for (Tag tag : Tag.values())
    {
      stringToTagMap.put(tag.toString(), tag);
      if (translationsMap.get(tag.toString()) != null)
        for (String translatedTagString : translationsMap.get(tag.toString()))
          stringToTagMap.put(translatedTagString, tag);
    }

    String mandatoryTags = config.getProperty("tag.mandatory");
    if (mandatoryTags != null) //if this is null a default list of mandatory tags is is used (see MandatoryHeaderTags declaration at the top of the file)
    {
      st = new StringTokenizer(mandatoryTags, delimiters);
      MandatoryHeaderTags = new Tag[st.countTokens()];
      while (st.hasMoreTokens())
        MandatoryHeaderTags[st.countTokens()-1] = stringToTagMap.get(st.nextToken().trim());
    }

    String qdiffLength = config.getProperty("qdiff.length");
    if (qdiffLength == null) throw new Exception("qdiff.length is missing from configuration file.");
    QDIFF_LENGTH = Integer.parseInt(qdiffLength);
  }

  //Parse the given tag and throw and exception if its value is
  //not found in the allowableValues array, if this array is null
  //all values are allowables.  We give the user a bit of leeway
  //by ignoring the case when doing the comparison.
  //NOTE:  if a value must be taken from a set of allowable values,
  //       this method will update the tagline argument with the
  //       English translation of the value.
  private void validateTagLine(TagLine t, ArrayList<String> allowableValues) throws Exception
  {
    String arg = t.argument.trim();
    if (arg.length() == 0)
      throw new Exception("Tag %" + t.tagString + " requires an argument but none was specified.");

    if (allowableValues == null)
      return;

    //If the argument is a (comma separated) list every item in the list
    //is validated.
    StringTokenizer argList = new StringTokenizer(t.argument, ",");
    if (argList.countTokens() > 1 && t.tag != Tag.Keywords)
      throw new Exception("Only the %Keywords tag supports an argument list.");

    TreeSet<String> orderedArguments = new TreeSet<String>();
    while (argList.hasMoreTokens())
    {
      String currentArg = argList.nextToken();
      String found = null;
      for (String value : allowableValues)
      {
        if (currentArg.equalsIgnoreCase(value))
          found = value;
        else if (translationsMap.get(value) != null)
          for (String translatedValue : translationsMap.get(value))
            if (currentArg.equalsIgnoreCase(translatedValue))
            {
              found = value;
              break;
            }
        if (found != null) break;
      }
      if (found != null)
      {
        orderedArguments.add(found);
        continue;
      }
      String errorMessage = "Invalid argument for " + t.tagString + " tag.  Found: '" + currentArg + "' but must be one of: ";
      for (String value : allowableValues)
      {
        errorMessage += value + " ";
        if (translationsMap.get(value) != null)
          for (String translatedValue : translationsMap.get(value))
            errorMessage += translatedValue + " ";
      }
      throw new Exception(errorMessage.trim());
    }
    t.argument = "";
    for (String args : orderedArguments)
      t.argument += args + ",";
    t.argument = t.argument.substring(0,t.argument.length()-1);
  }

  //The %Creator or %Translator tags take a person's name as argument.  Your options for
  //the name format are:
  //1) username
  //2) Firstname Lastname
  //3) Lastname, Firstname
  //If either the firstname or lastname contains a space option 3) MUST be used
  //If a person does not have a firstname option 3) MUST be use and the firstname must be left blank
  //If a person does not have a lastname option 3) MUST be use and the lastname must be left blank.
  //Examples:
  //   dbreton        --> a username
  //   David Breton   --> a regular name
  //   Breton, David  --> a regular name
  //   Breton,        --> a person without a first name
  //   , David        --> a person without a last name
  private void validateName(TagLine t) throws Exception
  {
    StringTokenizer st;
    //boolean usingComma = false;
    if (t.argument.indexOf(",") >= 0)
    {
      st = new StringTokenizer(" " + t.argument + " ",",");
     // usingComma = true;
    }
    else
      st = new StringTokenizer(t.argument);

    if (st.countTokens() == 0)
      throw new Exception("You must specify a name when using the %Creator or %Translator tags");
    else if (st.countTokens() > 2)
      throw new Exception("The name you supplied: '"+t.argument.trim()+"' has more than 2 parts.  If your name contains a space you must use the format: Last name, First name");
  }

  //Parse the %Question tag.  This is a tag that is used to store the question number
  //as it appears in the original source where the problem was taken.  When there are
  //no source, this tag together together with the the %Creator tag must form a unique
  //id for the question.
  private void validateQuestion(TagLine t) throws Exception
  {
    String arg = t.argument.trim();
    if (arg.length() == 0)
      throw new Exception("Tag %" + t.tagString + " requires an argument but none was specified.");

    if (arg.length() > 16)
      throw new Exception("%Question tag is limited to 16 characters");
  }

  //Parse the %ChoiceA, ..., %ChoiceH tags and make sure that they start with the
  //string: '(A) '  That's an upper case label between two parenthesis followed by
  //a space.  Then return the %ChoiceX without that header.
  private String validateChoiceX(Tag t, String arg) throws Exception
  {
    String label = t.toString().substring(t.toString().length()-1);
    Pattern p = Pattern.compile("^\\("+label+"\\) \\s*(.*)", Pattern.DOTALL);
    Matcher m = p.matcher(arg);
    if (m.matches())
    {
      return m.group(1).trim();
    }
    throw new Exception("Latex for tag %"+t + " must start with: '("+ label +") '");
  }

  //Parse the %Rdifficulty tag and throw and exception if its value is
  //not an integer in {1, 2, ..., 35}.
  //The actual value is not important and is ignored.
  private void validateRdifficulty(TagLine t) throws Exception
  {
    try
    {
      int d = Integer.parseInt(t.argument.trim());
      if (1 <= d && d <= 35)
        return;
    }
    catch (Exception e) {}
    throw new Exception("Invalid argument for %Rdifficulty tag.  Found: '" + t.argument.trim() + "' but must be an integer between 1 and 35 (both inclusive).");
  }

  //Parse the %Qdifficulty tag and throw and exception if its value is
  //not an array of QDIFF_LENGTH integers each in {0, 1, 2, 3, 4, 5, 6}.
  //The actual value is not important and is ignored.
  private void validateQdifficulty(TagLine t) throws Exception
  {
    String errorMessage = "Invalid argument for %Qdifficulty tag.  Found: '" + t.argument.trim() + "' but must be a comma separated list of exactly " + QDIFF_LENGTH + " integers each between 0 and 6 (both inclusive).";
    StringTokenizer st = new StringTokenizer(t.argument," ,");
    if (st.countTokens() != QDIFF_LENGTH)
      throw new Exception(errorMessage + " You specified " + st.countTokens() + " values");
    try
    {
      while (st.hasMoreTokens())
      {
        int d = Integer.parseInt(st.nextToken());
        if (d < 0 || d > 6)
          throw new Exception(errorMessage);
      }
    }
    catch (Exception e)
    {
      throw new Exception(errorMessage);
    }
  }


  //Parse the %Choices tag and throw and exception if its value is
  //not an integer in {0, 3, 4, 5, 6, 7, 8}
  private void validateChoices(TagLine t) throws Exception
  {
    StringTokenizer st = new StringTokenizer(t.argument);
    if (st.countTokens() != 1)
      throw new Exception("%Choices tag takes exactly one argument but '" + t.argument.trim() + "' was specified");
    try
    {
      int n = Integer.parseInt(t.argument.trim());
      if (n != 0 && (n < 3 || n > 8))
        throw new Exception("%Choices tag must be an integer between 3 and 8 (both inclusive).  If you must you " +
                            "can also enter 0 but only if %Type is SA or TF (you should ommit the %Choices tag altogether for %Type SA and TF");
    }
    catch(Exception e)
    {
      throw new Exception("Unparsable integer for %Choices tag: " + t.argument.trim());
    }
  }
  
  //Returns a canonical true/false representation of the specified string or null
  //if this cannot be evaluated.
  private String determineTFValue(String s)
  {
    for (String value : allowableValuesMap.get("Type.TF"))
    {
      if (s.equalsIgnoreCase(value))
        return value;
      ArrayList<String> translations = translationsMap.get(value);
      if (translations == null) continue;
      for (String translatedValue : translations)
        if (s.equalsIgnoreCase(translatedValue))
          return value;
    }
    return null;
  }

  //We trim 'whitespace' latex formatting from the given argument.
  //The list of recognized keywords is:
  //   o) \\         : latex syntax for newline
  //   o) \\*        : latex syntax for newline, forbids pagebreak
  //   o) \pagebreak : latex syntax for page break
  //   o) \newpage   : latex syntax for new page
  //   o) \newline   : latex syntax for newline
  //Additionally every keyword can be followed by a length argument in the form [x unit]
  //where x is a real number and unit is a latex unit symbol
  private String trimWhitespace(String arg)
  {
    String regex = "\\\\\\\\\\*?|\\\\pagebreak\\b|\\\\newpage\\b|\\\\newline\\b";
    String[] allparts = arg.split(regex, -1);
    int first = 0, last = 0;
    //Find the first part that doesn't contain a keyword
    for (int i=0; i<allparts.length; i++)
    {
      String part = allparts[i].trim();
      if (part.length() == 0) continue;
      Matcher m = LATEX_LENGTH.matcher(part);
      if (m.matches() && m.group(1).trim().length() == 0) continue;
      if (m.matches()) allparts[i] = m.group(1).trim();
      first = i;
      break;
    }
    //Find the last part that contains a keyword
    for (int i=allparts.length-1; i>=first; i--)
    {
      String part = allparts[i].trim();
      if (part.length() == 0) continue;
      Matcher m = LATEX_LENGTH.matcher(part);
      if (m.matches() && m.group(1).trim().length() == 0) continue;
      last = i;
      break;
    }
    //Trim the leading and trailing whitespaces
    String retString = allparts[first].trim();
    for (int i=first+1; i<=last; i++)
    {
      String part = allparts[i].trim();
      if (part.length() == 0) continue;
      Matcher m = LATEX_LENGTH.matcher(part);
      if (m.matches())
        retString += "\\\\" + part + "\n";
      else
        retString += "\\\\\n" + part + (part.length()>0?" ":"");
    }
    return retString;
  }

 
  //Construct a TagLine object from a String.
  //  If the string does not start with (at least) one '%' the
  //    TagLine 'tag' component will have the value Tag.NONE.
  //  Otherwise we check if the string starts with %word where
  //    word is one of the reserved keyword (see Tag enum at
  //    top of this file and the translationsMap)
  //  Finally if the string starts with a '%' but does not match
  //    any of the keywords, the special tag Tag.COMMENT is
  //    used for the 'tag' component of the returned TagLine.
  public TagLine getTagLine(String line)
  {
    if (!line.startsWith("%"))
      return new TagLine(Tag.NONE,"",line);

    for (String name : stringToTagMap.keySet())
    {
      Pattern p = Pattern.compile("^%+"+name+"\\b(.*)");
      Matcher m = p.matcher(line);
      if (m.matches()) return new TagLine(stringToTagMap.get(name), name, m.group(1).trim());
    }
    return new TagLine(Tag.COMMENT,"",line);
  }

  //Return the next expected tag (ommitting the header tags, which appear in
  //any order between %Begin and %Qtext) given the current tag and the number
  //of choices expected to be present.
  //The Tag order is:
  //   Begin->...->Qtext->ChoiceA->ChoiceB->ChoiceC->ChoiceD->ChoiceE->ChoiceF->ChoiceG->ChoiceH->Ftext->End
  private Tag getNextRegion(Tag currentRegion, int numchoices) throws Exception
  {
    switch (currentRegion)
    {
      case Begin: return Tag.Qtext;
      case Qtext: return numchoices > 0 ? Tag.ChoiceA : Tag.Ftext;
      case ChoiceA: return numchoices > 1 ? Tag.ChoiceB : Tag.Ftext;
      case ChoiceB: return numchoices > 2 ? Tag.ChoiceC : Tag.Ftext;
      case ChoiceC: return numchoices > 3 ? Tag.ChoiceD : Tag.Ftext;
      case ChoiceD: return numchoices > 4 ? Tag.ChoiceE : Tag.Ftext;
      case ChoiceE: return numchoices > 5 ? Tag.ChoiceF : Tag.Ftext;
      case ChoiceF: return numchoices > 6 ? Tag.ChoiceG : Tag.Ftext;
      case ChoiceG: return numchoices > 7 ? Tag.ChoiceH : Tag.Ftext;
      case ChoiceH: return Tag.Ftext;
      case Ftext: return Tag.End;
      case End: return Tag.Begin;
    }
    throw new Exception("Invalid current region: " + currentRegion);
  }

  //Returns the 'region' for a given tag.
  //Every tag is its own region except that all the 'header' tags
  //belong to the 'Begin' region.
  private Tag getRegion(Tag tag)
  {
    switch (tag)
    {
      case Qtext:
      case ChoiceA:
      case ChoiceB:
      case ChoiceC:
      case ChoiceD:
      case ChoiceE:
      case ChoiceF:
      case ChoiceG:
      case ChoiceH:
      case Ftext:
      case End: return tag;
      default: return Tag.Begin;
    }
  }

  //This is the main method, it parses the given file for questions.
  //If the format is invalid an error is thrown together with a message
  //explaining the reason for the error.  This method does not insert the
  //questions into the database, it merely checks whether the format is
  //valid.
  public void parseFile(String filename, String charset) throws Exception
  {
    //int numQuestionParsed = 0;
    Tag currentRegion = Tag.End;
    Tag nextRegion = Tag.Begin;
    int linenumber = 0;
    BufferedReader file = null;
    TagLine tagLine = null;
    TagLine categoryTagLine = null;
    TagLine answerTagLine = null;
    Map<Tag, String> questionInfo = new TreeMap<Tag, String>();
    Set<Tag> unextractedTags = null;
    try
    {
      file = new BufferedReader(new InputStreamReader(new FileInputStream(filename), charset));
      String line = file.readLine();
      linenumber = 1;
      while (line != null)
      {
        //We can't trim the line with line=line.trim() yet because I don't
        //know how much formatting is required by latex.  Does an empty line
        //of latex code start a new paragraph???
        tagLine = getTagLine(line.trim());
        //outputMessage("TagLine: " + tagLine + "\n");
        if (tagLine.tag == Tag.Begin)
          questionInfo.put(Tag.COMMENT, "Question in file '" + filename + "' starting on line " + linenumber);
        
        if (tagLine.tag == Tag.COMMENT || tagLine.tag == Tag.NONE)
        {
          switch (currentRegion)
          {
            case Begin:
            case End: break;
            default:
              String value = questionInfo.remove(currentRegion);
              if (value.length() == 0)
                questionInfo.put(currentRegion, line);
              else
                questionInfo.put(currentRegion, value+"\n"+line);
          }
          line = file.readLine();
          linenumber++;
          continue;
        }

        if (currentRegion != Tag.Begin && tagLine.tag != nextRegion)
          throw new Exception("Expecting tag %" + nextRegion + ", but found %" + tagLine.tag);

        switch (currentRegion)
        {
          case ChoiceA:
          case ChoiceB:
          case ChoiceC:
          case ChoiceD:
          case ChoiceE:
          case ChoiceF://Here we make sure that the value for tag %ChoiceX starts with '(X) '
          case ChoiceH://We then strip that '(X) ' and all leading and trailing 'whitespace'
            String formattedChoiceX = validateChoiceX(currentRegion, questionInfo.get(currentRegion));
            formattedChoiceX = trimWhitespace(formattedChoiceX);
            questionInfo.put(currentRegion, formattedChoiceX);
            break;
          case Qtext:
          case Ftext:
            String formattedText = trimWhitespace(questionInfo.get(currentRegion));
            questionInfo.put(currentRegion, formattedText);
            break;
          default: break;
        }
        String currentRegionValue = questionInfo.get(currentRegion);
        if (currentRegionValue != null && (currentRegionValue.indexOf("\\pagebreak") >= 0 || currentRegionValue.indexOf("\\newpage") >= 0))
          throw new Exception("Parsing error for tag %"+ currentRegion + ": \\pagebreak and \\newpage are disallowed.  If you want to insert a page break, do so between the %End tag of a question and the %Begin tag of the next one.");

        //now we can trim the line, none of the remaining tags contain latex info.
        line = line.trim();
        currentRegion = getRegion(tagLine.tag);
        //outputMessage("CurrentRegion: " + currentRegion + ", NextRegion: " + nextRegion + ", Tag: " + tagLine + "\n");


        //Only the 'header' tags belong to the Tag.Begin region.
        if (currentRegion == Tag.Begin)
        {
          if (questionInfo.keySet().contains(tagLine.tag))
            throw new Exception("Tag %" + tagLine.tag + " already used for this question.");

          switch(tagLine.tag)
          {
            case Begin:
              unextractedTags = new TreeSet<Tag>();
              for (Tag t : allExtractedTagArguments.keySet())
                unextractedTags.add(t);
              break;
            case Keywords:
              validateTagLine(tagLine, allowableValuesMap.get(Tag.Keywords.toString()));
              break;
            case Subject:
              validateTagLine(tagLine, allowableValuesMap.get(tagLine.tag.toString()));
              if (categoryTagLine != null)
                validateTagLine(categoryTagLine, allowableValuesMap.get("Category."+tagLine.argument));
              break;
            case Category:
              if (questionInfo.get(Tag.Subject) != null)
                validateTagLine(tagLine, allowableValuesMap.get("Category."+questionInfo.get(Tag.Subject)));
              else
                categoryTagLine = tagLine;
              break;
            case Rdifficulty: validateRdifficulty(tagLine); break;
            case Qdifficulty: validateQdifficulty(tagLine); break;
            case Choices: validateChoices(tagLine); break;
            case Creator:
            case Translator: validateName(tagLine); break;
            case Question: validateQuestion(tagLine); break;
            case Answer:
              if (questionInfo.get(Tag.Type) != null)
                validateTagLine(tagLine, allowableValuesMap.get("Type." + questionInfo.get(Tag.Type)));
              else
                answerTagLine = tagLine;
              break;
            case Type:
              validateTagLine(tagLine, allowableValuesMap.get(tagLine.tag.toString()));
              if (answerTagLine != null)
                validateTagLine(answerTagLine, allowableValuesMap.get("Type." + tagLine.argument));
            case Language:
            case Source:
            case Title: validateTagLine(tagLine, allowableValuesMap.get(tagLine.tag.toString())); break;
          }
          //Save the tag argument if the user wants to extract all values for the current tag.
          Collection<String> args = allExtractedTagArguments.get(tagLine.tag);
          if (args != null) //null means we don't want to extract this tag, non-null means we do.
          {
            args.add(tagLine.argument);
            unextractedTags.remove(tagLine.tag);
          }
          if (tagLine.tag != Tag.Begin)
            questionInfo.put(tagLine.tag, tagLine.argument.trim());
          nextRegion = getNextRegion(currentRegion, -1);
        }
        else //The current tag does not belong to the header, which means that the entire header has been parsed.
        {
          if (tagLine.tag != nextRegion)
            throw new Exception("Expecting tag %" + nextRegion + ", but found %" + tagLine.tag);

          int numchoices = questionInfo.get(Tag.Choices) == null ? 0 : Integer.parseInt(questionInfo.get(Tag.Choices));
          if (tagLine.tag == Tag.Qtext)
          {
            for (Tag t : MandatoryHeaderTags)
              if (!questionInfo.keySet().contains(t))
                throw new Exception("No %" + t + " tag found in question header");

            int diff = 0;
            if (questionInfo.keySet().contains(Tag.Rdifficulty)) diff++;
            if (questionInfo.keySet().contains(Tag.Qdifficulty)) diff++;
            if (diff != 1)
              throw new Exception("Exactly one of the %Rdifficulty and %Qdifficulty tags must be present for each question.");

            String answerType = questionInfo.get(Tag.Type);
            String answer = questionInfo.get(Tag.Answer);
            if (!answerType.startsWith("MC") && numchoices != 0)
              throw new Exception("%Choices tag can only be non-zero when %Type is MC or MC5");
            if (answerType.equals("MC5") && numchoices != 5)
              throw new Exception("%Choices tag must be 5 when %Type is MC5");

            int answerIndex = allPossibleMultipleChoiceAnswers.indexOf(answer);
            if (answerType.startsWith("MC") && (answer.length() != 1 || answerIndex < 0 || answerIndex > numchoices-1))
            {
              String answerString = "";
              for (int i=0; i<numchoices; i++)
              {
                answerString += allPossibleMultipleChoiceAnswers.charAt(i);
                if (i+1 < numchoices)
                  answerString += ", ";
              }
              throw new Exception("%Answer must be one of " + answerString + " when %Type is MC and %Choices is " + numchoices + " but the value: '" + answer + "' was specified.");
            }
            //////////////////////////
            //WARNING          WARNING
            //THIS IS A HORRIBLE HACK
            if (answerType.equals("MC") && numchoices == 5)
              questionInfo.put(Tag.Type, "MC5");
            //////////////////////////
          }
          if (tagLine.tag != Tag.End)
            questionInfo.put(tagLine.tag, tagLine.argument.trim());

          nextRegion = getNextRegion(currentRegion, numchoices);
        }

        if (nextRegion == Tag.Begin)
        {          
          boolean inserted = allParsedQuestions.add(questionInfo);
          if (!inserted)
          {
            Map<Tag,String> conflictingQuestion = allParsedQuestions.ceiling(questionInfo);
            throw new Exception("A question with this identifier: \n" +
                                questionIdentifier(questionInfo) + "\n" +
                                "already exists.  The conflict is with:\n" +
                                "  " + conflictingQuestion.get(Tag.COMMENT) + "\n" +
                                "If this is a modification of an original question\n" +
                                "the %Question tag must also be modified, perhaps by\n" +
                                "appending vN at the end of the tag to signify version#N\n");
          }
          
          questionInfo = new HashMap<Tag, String>();
          categoryTagLine = null;
          answerTagLine = null;
          for (Tag t : unextractedTags)
            allExtractedTagArguments.get(t).add(null);
        }
        line = file.readLine();
        linenumber++;
      }
      if (nextRegion != Tag.Begin)
        throw new Exception("The last question in the file did not end with the %End tag");
      file.close();
    }
    catch (Exception e)
    {
      try { file.close(); } catch (Exception ioe) {}
      if (linenumber > 0)
        throw new Exception("-------\nParse error in " + filename + " near line " + linenumber + ".\n-------\n" + e.getMessage());
    }
  }

  //Given a valid question stored in a Map of Tag-->Value, this method returns
  //the 'identifier' for the question.  The identifier is
  //(Creator,Question,Language)       if %Source is undefined
  //(Source,Title,Question,Language)  if %Source is defined
  private static String questionIdentifier(Map<Tag, String> qInfo)
  {
    //String identifier;
    if (qInfo.get(Tag.Source) == null)
      return "  Creator: " + qInfo.get(Tag.Creator) + "\n" +
        "  Question:" + qInfo.get(Tag.Question) + "\n"+
        "  Language: " + qInfo.get(Tag.Language);
    return "  Source: " + qInfo.get(Tag.Source) + "\n" +
      "  Title: " + qInfo.get(Tag.Title) + "\n"+
      "  Question: " + qInfo.get(Tag.Question) + "\n"+
      "  Language: " + qInfo.get(Tag.Language);
    
  }

  //Validates all translations.  This method iterates through the _ordered set_ of all parsed
  //questions.  In that ordered set every translations T1(Q),T2(Q),...,Tk(Q) of a question Q
  //immediately follow Q.  Thus the set looks like:  Q1,T1(Q1),...,Tk(Q1),Q2,T1(Q2),...,etc
  //Because we currently only have French and English there is at most one translation so the
  //set looks like this: Q1,T(Q1),Q2,Q3,Q4,T(Q4), ...
  //
  //If T is a translation, %Translator must be defined otherwise an exception is thrown.
  //An exception is thrown if any of the following is true
  //    o) Q.Creator != T.Creator
  //    o) Q.Type != T.Type
  //    o) Q.Choices != T.Choices
  //    o) Q.Keywords != T.Keywords
  //    o) Q.Qdifficulty != T.Qdifficulty ***
  //    o) Q.Answer != T.Answer ###
  //*** When Q or T uses %Rdifficulty, it is converted to %Qdifficulty using the mapping in parser.ini
  //### For SA question, we only issue a warning instead of throwing an error (because there is no way
  //    of making sure the values are equivalent.  For example the question 'Spell 10' would have %Answer
  //    'dix' in French and 'ten' in English.)
  public void validateTranslations() throws Exception
  {
    Map<Tag,String> original = null;
    for (Map<Tag,String> translation : allParsedQuestions)
    {
      if (original == null || QuestionComparator.compareIdentifiers(original, translation) != 0)
      {
        original = translation;
        continue;
      }

      String errorMessage = null;
      String warningMessage = null;
      if (translation.get(Tag.Translator) == null)
        errorMessage = "Unable to determine which question is the original and which one is the translation because the %Translator tag is missing.";
      if (!original.get(Tag.Creator).equals(translation.get(Tag.Creator)))
        errorMessage = "%Creator mismatch";
      else if (!original.get(Tag.Type).equals(translation.get(Tag.Type)))
        errorMessage = "%Type mismatch";
      else if (original.get(Tag.Type).startsWith("MC") && !original.get(Tag.Choices).equals(translation.get(Tag.Choices)))
        errorMessage = "%Choices mismatch";
      else if (!original.get(Tag.Keywords).equals(translation.get(Tag.Keywords))) //the keywords are already sorted and converted to English
        errorMessage = "%Keywords mismatch";
      else if (!getQdiff(original).equals(getQdiff(translation)))
        errorMessage = "Difficulty level mismatch (when used, %Rdifficulty is first converted to %Qdifficulty using the mapping in smacparser.ini)";
      else if (!original.get(Tag.Answer).equalsIgnoreCase(translation.get(Tag.Answer)))
      {
        if (!original.get(Tag.Type).equals("SA"))
          errorMessage = "%Answer mismatch";
        else
          warningMessage = "WARNING: Suspicious %Answer value.  original %Answer: '" + original.get(Tag.Answer) + "', translated %Answer: '" + translation.get(Tag.Answer) + "'";
      }
      if (errorMessage != null)
        throw new Exception(errorMessage +"\nOriginal: " + original.get(Tag.COMMENT) + "\nTranslation: " + translation.get(Tag.COMMENT));
      if (warningMessage != null)
        ui.outputMessage(warningMessage +"\nOriginal: " + original.get(Tag.COMMENT) + "\nTranslation: " + translation.get(Tag.COMMENT) + "\n");
    }
  }

  //Return the %Qdifficulty of a question.  If %Rdifficulty is used instead, we convert it using the mapping in parser.ini
  private String getQdiff(Map<SmacParser.Tag,String> q1)
  {
    String diff = q1.get(SmacParser.Tag.Qdifficulty);
    if (diff != null) return diff;
    diff = q1.get(SmacParser.Tag.Rdifficulty);
    return rdiffToQdiffMap.get(Integer.parseInt(diff));
  }
  public int numParsed()
  {
    return allParsedQuestions.size();
  }
  public TreeSet<Map<Tag,String>> allParsedQuestions()
  {
    return allParsedQuestions;
  }

  
  //Initialize the SmacParser.
  // o) Read in the config file
  // o) Populate the various maps
  // o) Set the messageStream.  All feedback is written on this outputstream.
  public SmacParser(boolean tagListing) throws Exception
  {
    this.ui = this;
    listAllExtractedTagArguments = tagListing;
    String configFilename = "smacparser.ini";
    Properties config = new Properties();
    InputStream conf = SmacParser.class.getResourceAsStream(configFilename);
    config.load(new InputStreamReader(new BufferedInputStream(conf), "UTF-8"));
    //messageStream = s;
    populateTagHandlingMaps(config);

  }
  
  
  public SmacParser(boolean tagListing, SmacUI smacui) throws Exception
  {
    this(tagListing);
    this.ui = smacui;
  }

  //Set which Tag are to be extracted during parsing.
  public void setTagsToExtract(Collection<String> tags)
  {
    allExtractedTagArguments.clear();
    for (String tag : tags)
    {
      Tag t = getTagLine(tag).tag;
      if (t == Tag.COMMENT)
      {
        outputMessage("Unrecognized tag: " + tag + "\n");
        continue;
      }
      boolean extractable = (getRegion(t) == Tag.Begin && t != Tag.Begin && t != Tag.COMMENT);
      
      if (!extractable)
        outputMessage("Tag " + tag + " is not extractable\n");
      else
        allExtractedTagArguments.put(t, new ArrayList<String>());
    }
  }

  //Fills the specified collection with the names of all files ending in .tex in the root directory and all its subdirectories (at any depth)
  public void getFileRecursively(Collection<String> files, File root) throws Exception
  {
    if (!root.isDirectory()) return;
    if (!root.canRead()) { outputMessage("Could not read directory " + root + ".  Permission denied\n"); return; }
    if (!root.canExecute()) { outputMessage("Could not list content of directory " + root + ".  Permission denied\n"); return; }
    try
    {
      for (File f : root.listFiles())
      {
        if (f.isDirectory())
          getFileRecursively(files, f);
        else if (f.getName().endsWith(".tex"))
          files.add(f.getPath());
      }
    }
    catch (Exception e)
    {
      throw new Exception("Error reading directory tree: " + e.getMessage());
    }
  }

  public void outputMessage(String message)
  {
    if (ui == null) return;
    try
    {
    	ui.outputMessage(message);
      //messageStream.write(message.getBytes());
      //messageStream.flush();
    }
    catch (Exception e)
    {
      System.err.println(e.getMessage());
    }
  }

  static class TagLine
  {
    Tag tag;
    String tagString;
    String argument;

    TagLine(Tag t, String str, String arg)
    {
      tag = t;
      tagString = str;
      argument = arg;
    }

    public String toString()
    {
      String tagStr = tag.toString().equals(tagString) ? tag.toString() : tag + "("+tagString+")";
      return tagStr + "-->" + argument;
    }
  }
}
