public class Question {

   private var qName:String;
   private var qLangue:String;
   
   public function Question(name:String, langue:String)
   {
	   qName = name;
	   qLangue = langue;
   }
	
   public function getQuestionName():String {
	   return qName;   
   }
   
   public function getQuestionLangue():String {
	   return qLangue;   
   }
	
}