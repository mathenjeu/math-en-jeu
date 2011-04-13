package{
public class Question {

   private var qName:String;
   private var qLangue:String;
   
   public function Question(nameS:String, langue:String)
   {
	   qName = nameS;
	   qLangue = langue;
   }
	
   public function getQuestionName():String {
	   return qName;   
   }
   
   public function getQuestionLangue():String {
	   return qLangue;   
   }
	
}
}