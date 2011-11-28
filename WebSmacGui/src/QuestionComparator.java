import java.util.*;

public class QuestionComparator implements Comparator<Map<Tag,String>>
{

  public static int compareIdentifiers(Map<Tag,String> q1, Map<Tag,String> q2)
  {
    String v1 = q1.get(Tag.Source);
    if (v1 != null) //q1 has a Source
    {
      //Compare Tag.Source (this tag is optional)
      String v2 = q2.get(Tag.Source);
      if (v2 == null) return -1;  //q1 has a Source, q2 does not so q1<q2
      int cmp = v1.compareTo(v2); //alphabetical comparision
      if (cmp != 0) return cmp;
      //Tie breaker 1: Compare Tag.Title (this tag is mandatory)
      v1 = q1.get(Tag.Title);
      v2 = q2.get(Tag.Title);
      cmp = v1.compareTo(v2);    //alphabetical comparison
      if (cmp !=0) return cmp;
    }
    else //q1 does not have a Source
    {
      //q2 has a Source so q1>q2
      if (q2.get(Tag.Source) != null) return 1;
      //Compare Tag.Creator (this tag is mandatory)
      v1 = q1.get(Tag.Creator);
      String v2 = q2.get(Tag.Creator);
      int cmp = v1.compareTo(v2);
      if (cmp != 0) return cmp;
    }

    //Next tie breaker: Compare Tag.Question (this tag is mandatory)
    v1 = q1.get(Tag.Question);
    String v2 = q2.get(Tag.Question);
    int cmp = v1.compareTo(v2);
    return cmp; 

  }

  public int compare(Map<Tag,String> q1, Map<Tag,String> q2)
  {
    int cmp = QuestionComparator.compareIdentifiers(q1, q2);
    if (cmp != 0) return cmp;
    
    //At this point we know either q1==q2 or they are translations of the same question
    String v1 = q1.get(Tag.Language);
    String v2 = q2.get(Tag.Language);
    cmp = v1.compareTo(v2);
    if (cmp == 0) return 0; //q1 == q2

    //At this point we know q1 and q2 are translations of the same question, we make sure
    //that the original question (the one without a Translator) appears first in the ordering.
    String t1 = q1.get(Tag.Translator);
    String t2 = q2.get(Tag.Translator);
    if (t1 == null && t2 != null) return -1; //q1 is original, q2 is a translation
    if (t1 != null && t2 == null) return 1;  //q1 is a translation, q2 is original
    if (t1 != null && t2 != null) return cmp; //q1 and q2 are both translations

    //Something went wrong we have two identical questions in different language
    //and neither has a Translator.  Most likely the Translator tag was omitted,
    //this is an error and will be caught when we verify translation status.
    return cmp; 
  }

}