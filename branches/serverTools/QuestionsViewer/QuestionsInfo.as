package{
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.net.URLLoader;
import flash.net.URLRequest;

public class QuestionsInfo extends EventDispatcher {
	

   private static const QUESTIONS:String = "questions.xml";
   private var questionsArray:Array;
   
   //public function QuestionsInfo(){};
   
   private function onXMLLoaded(event:Event):void {
      questionsArray = [];
	  var loader:URLLoader = event.target as URLLoader;
	  var xml:XML = new XML(loader.data);
	  var questions:XMLList = xml.child("question");
	  var questionsNumb:uint = questions.length();
	  var question:XML;
	  
	  for(var i:uint = 0; i < questionsNumb; i++)
	  {
		question = questions[i] as XML;
		questionsArray.push( new Question(question.child("name").toString(),
										  question.child("langue").toString()));		
	  }
	  dispatchEvent(new Event(Event.COMPLETE));
	  
	  
   }
   
   public function loadQuestionsInfo():void {
	  
	  var qLoader:URLLoader = new URLLoader();
	  qLoader.addEventListener(Event.COMPLETE, onXMLLoaded);
	  qLoader.load(new URLRequest(QUESTIONS));
   }
   
   public function getNames():Array {
      var qArray:Array = [];
	  var questionsNumb:uint = questionsArray.length;
	  var question:Question;
	  for(var i:uint = 0; i < questionsNumb; i++)
	  {
		  question = questionsArray[i] as Question;
		  var link:String = question.getQuestionName() + ".swf";
		  qArray.push({label:link, data:link});
		  //trace(link);
	  }
	  return qArray;
   }
	
}
}