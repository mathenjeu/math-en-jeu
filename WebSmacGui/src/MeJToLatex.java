//Author:    David Breton, dbreton@cs.sfu.ca
//
//Copyright: You can do whatever you want with this application.
//           The author is not liable for any loss caused by the
//           use of this application.
//
//Date:      March 2009
//
//Version:   0.5  * Added database interaction and config file
//           0.4  * Fix parser bug when %Type=MC but %Answer > %Choices
//           0.3  * Fix parser bug when comments appear after the last %End tag.
//           0.2  * Fix parser bug that did not require last %End tag.
//                * Made a lookup table of allowable subjects->categories
import java.io.*;
import java.sql.*;
import java.util.*;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class MeJToLatex
{
  private static final int SERVER_NAME=0;
  private static final int DB_NAME=1;
  private static final int USER=2;
  private static final int PASSWORD=3;
  private static String[] MEJ_DB = null;

  private static MysqlDataSource createDataSource(String[] data)
  {
    MysqlDataSource ds = new MysqlDataSource();
    ds.setServerName(data[SERVER_NAME]);
    ds.setDatabaseName(data[DB_NAME]);
    ds.setUser(data[USER]);
    ds.setPassword(data[PASSWORD]);
    ds.setEncoding("UTF-8");
    return ds;
  }


  private static Map<Integer, String> getUserMap()
  {
    Map<Integer, String> userInfoMap = new TreeMap<Integer, String>();
    Connection conn = null;
    try
    {
      conn = createDataSource(MEJ_DB).getConnection();
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT id, username FROM jos_users ORDER BY id");
      while (rs.next())
        userInfoMap.put(rs.getInt("id"), rs.getString("username"));
      conn.close();
    } catch (SQLException ex)
    {
      try {conn.close();}catch(Exception e){};
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
    }
    return userInfoMap;
  }
  
  private static Map<Integer, String> getSourceMap()
  {
    Map<Integer, String> sourceMap = new TreeMap<Integer, String>();
    Connection conn = null;
    try
    {
      conn = createDataSource(MEJ_DB).getConnection();
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT source_id, name FROM source ORDER BY source_id");
      while (rs.next())
        sourceMap.put(rs.getInt("source_id"),rs.getString("name"));
      conn.close();
    } catch (SQLException ex)
    {
      try {conn.close();}catch(Exception e){};
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
    }
    return sourceMap;
  }

  private static Map<Integer, String> getTitleMap()
  {
    Map<Integer, String> titleMap = new TreeMap<Integer, String>();
    Connection conn = null;
    try
    {
      conn = createDataSource(MEJ_DB).getConnection();
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT title_id, name FROM title ORDER BY title_id");
      while (rs.next())
        titleMap.put(rs.getInt("title_id"),rs.getString("name"));
      conn.close();
    } catch (SQLException ex)
    {
      try {conn.close();}catch(Exception e){};
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
    }
    return titleMap;
  }
  
  /*
  private static Map<Integer, String> getCategoryMap(Properties config)
  {
    Map<Integer, String> categoryTexName = new TreeMap<Integer, String>();
    for (String name : config.stringPropertyNames())
    {
      if (name.startsWith("category_id."))
      {
        int category_id = Integer.parseInt(config.getProperty(name));
        StringTokenizer st = new StringTokenizer(name, ".");
        st.nextToken();
        st.nextToken();
        String category=st.nextToken();
        categoryTexName.put(category_id, category);
      }
    }
    Map<Integer, String> categoryMap = new TreeMap<Integer, String>();
    Connection conn = null;
    try
    {
      conn = createDataSource(MEJ_DB).getConnection();
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT category_id,name FROM category_info WHERE language_id=2 ORDER BY category_id");
      while (rs.next())
      {
        int catid = rs.getInt("category_id");
        String cat = categoryTexName.get(catid);
        if (cat == null)
        {
          cat = categoryTexName.get(catid+1);
          if (cat == null)
          {
            cat = rs.getString("name");
            System.out.println("Unknown category: " + catid);
          }
        }
        categoryMap.put(catid, cat);
      }
      conn.close();
    } catch (SQLException ex)
    {
      try {conn.close();}catch(Exception e){};
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
    }
    return categoryMap;
  }

  private static Map<Integer, String> getSubjectMap(Properties config)
  {
    Map<Integer, String> subjectTexName = new TreeMap<Integer, String>();
    for (String name : config.stringPropertyNames())
    {
      if (name.startsWith("category_id."))
      {
        int category_id = Integer.parseInt(config.getProperty(name));
        StringTokenizer st = new StringTokenizer(name, ".");
        st.nextToken();
        subjectTexName.put(category_id, st.nextToken());
      }
    }

    Map<Integer, String> subjectMap = new TreeMap<Integer, String>();
    Connection conn = null;
    try
    {
      conn = createDataSource(MEJ_DB).getConnection();
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT c.category_id, si.name FROM category c, subject_info si WHERE c.subject_id=si.subject_id");
      while (rs.next())
      {
        int catid = rs.getInt("category_id");
        String sub = subjectTexName.get(catid);
        if (sub == null)
        {
          sub = subjectTexName.get(catid+1);
          if (sub == null)
          {
            sub = rs.getString("name");
            System.out.println("Unknown subject: " + catid);
          }
        }
        subjectMap.put(catid,sub);
      }
      conn.close();
    } catch (SQLException ex)
    {
      try {conn.close();}catch(Exception e){};
      // handle any errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
    }
    return subjectMap;
  }
  */


  public static void main(String[] args)
  {
    Properties config = new Properties();
    try
    {
      //InputStream conf = SmacParser.class.getResourceAsStream("latextomej.ini");
      //config.load(new InputStreamReader(new BufferedInputStream(conf), "UTF-8"));
      config.load(new InputStreamReader(new BufferedInputStream(new FileInputStream("latextomej.ini")), "UTF-8"));
    } catch(Exception e)
    {
      e.printStackTrace();
      System.out.println(e.getMessage());
    }

    ////Parsing is done, now add the questions to DB
    MEJ_DB = new String[]{config.getProperty("db.mej.server"),
                          config.getProperty("db.mej.name"),
                          config.getProperty("db.mej.user"),
                          config.getProperty("db.mej.password")};

    
    
    
    Map<Integer, String> allCreators = getUserMap();
    Map<Integer, String> allSources = getSourceMap();
    Map<Integer, String> allTitles = getTitleMap();
   // Map<Integer, String> allCategories = getCategoryMap(config);
   // Map<Integer, String> allSubjects = getSubjectMap(config);

    
    Connection mejConnection = null;
    
    try
    {
      mejConnection = createDataSource(MEJ_DB).getConnection();
      Statement stmt = mejConnection.createStatement();
      Statement stmt2 = mejConnection.createStatement();
      Statement stmt3 = mejConnection.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT * FROM question q, question_info qi WHERE q.question_id=qi.question_id ORDER BY q.question_id,qi.language_id");
      while (rs.next())
      {
        int question_id = rs.getInt("question_id");
        if (question_id >= 6090)
          break;
        //int category_id = rs.getInt("category_id");
        int answer_type_id = rs.getInt("answer_type_id");
        int source_id = rs.getInt("source_id");
        int title_id = rs.getInt("title_id");
        String qlabel = rs.getString("label");

        int language_id = rs.getInt("language_id");
        String qlatex = rs.getString("question_latex");
        String flatex = rs.getString("feedback_latex");
        int user_id = rs.getInt("user_id");

        ////////
        String language = language_id==1?"French":"English";
        String source = source_id==0?"":allSources.get(source_id);
        String title = allTitles.get(title_id);
        //String category = allCategories.get(category_id);
        //String subject = allSubjects.get(category_id);
        String type = answer_type_id==1?"MC":answer_type_id==2?"TF":"SA";
        String creator = allCreators.get(user_id);

        ResultSet answers_rs = null;
        Map<Integer, String[]> answers = new TreeMap<Integer, String[]>();
        String right_answer = "";
        int choices = 0;
        if (type.equals("MC"))
        {
          answers_rs = stmt2.executeQuery("SELECT * FROM answer a, answer_info ai WHERE a.question_id=" + question_id + " && a.answer_id=ai.answer_id && ai.language_id=" + language_id);
          while (answers_rs.next())
          {
            choices++;
            answers.put(answers_rs.getInt("answer_id"), new String[]{answers_rs.getString("label"), answers_rs.getString("answer_latex")});
            if (answers_rs.getInt("is_right") == 1)
              right_answer = answers_rs.getString("label").toUpperCase();
          }
        }
        else if (type.equals("SA"))
        {
          answers_rs = stmt2.executeQuery("SELECT * FROM answer a, answer_info ai WHERE a.question_id=" + question_id + " && a.answer_id=ai.answer_id && ai.language_id=" + language_id);
          answers_rs.next();
          right_answer = answers_rs.getString("answer_latex");
        }
        else if (type.equals("TF"))
        {
          answers_rs = stmt2.executeQuery("SELECT * FROM answer a WHERE a.question_id=" + question_id);
          answers_rs.next();
          right_answer = answers_rs.getString("is_right").equals("0")?"F":"T";
        }
        answers_rs.close();

        ResultSet level_rs = stmt3.executeQuery("SELECT * FROM question_level WHERE question_id = " + question_id + " ORDER BY level_id");
        int l=1;
        String vector = "";
        while (level_rs.next())
        {
          int level=level_rs.getInt("level_id");
          int value=level_rs.getInt("value");
          while (l < level)
          {
            vector += "0 ";
            l++;
          }
          vector += value + " ";
          l=level+1;
        }
        while (l++<=18)
          vector += "0 ";
        level_rs.close();
        
        System.out.println("%=====================");
        System.out.println("%Begin");
        System.out.println("%Language " + language);
        if (!source.trim().equals(""))
          System.out.println("%Source " + source);
        System.out.println("%Title " + title);
        if (!qlabel.trim().equals(""))
          System.out.println("%Question " + qlabel);
        ///System.out.println("%Subject " + subject);
        ///System.out.println("%Category " + category);
        System.out.println("%Type " + type);
        if (choices != 0)
          System.out.println("%Choices " + choices);
        System.out.println("%Answer " + right_answer);
        System.out.println("%Creator " + creator);        
        System.out.println("%Qdifficulty " + vector);
        System.out.println("%Qtext\n " + qlatex);
        if (type.equals("MC"))
        {
          for (Integer aid : answers.keySet())
          {
            
            System.out.println("%Choice"+answers.get(aid)[0].toUpperCase());
            System.out.println(answers.get(aid)[1]);
          }
        }
        System.out.println("%Ftext\n " + flatex);
        System.out.println("%End");
      }
      rs.close();
     }
    catch (Exception e)
    {
      e.printStackTrace();
      System.exit(1);
    }
  }
    
}
