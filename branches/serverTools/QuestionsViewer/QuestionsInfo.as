import flash.events.Event;
import flash.events.EventDispatcher;
import flash.net.URLLoader;
import flash.net.URLRequest;

public class QuestionsInfo extends EventDispatcher {

   private static const QUESTIONS:String = "questions.xml";
   private var questionsArray:Array;
   
   private function onXMLLoaded(event:Event):void {
      questionsArray = [];
	  var loader:URLLoader = event.target as URLLoader;
	  var xml:XML = new XML(loader.data);
	  var questions:XMLList = xml.child("question");
	  var questionsNumb:int = questions.length();
	  var question:XML;
	  
	  for(var i:int = 0; i < questionsNum; i++)
	  {
		question = questions[i] as XML;
		questionsArray.push( new Question(question.child("name").toString(),
										  question.child("langue").toString()));		
	  }
	  dispatchEvent(new Event(Event.COMPLETE));
	  
	  
   }
   
   public function loadQuestionsInfo():void {
	  
	  var qLoader:URLLoader = new URLLoader();
	  loader.addEventListener(Event.COMPLETE, onXMLLoaded);
	  loader.load(new URLRequest(QUESTIONS));
   }
	
	
}