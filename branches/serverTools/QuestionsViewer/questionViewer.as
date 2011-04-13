import flash.display.*;
import flash.net.URLRequest;
import fl.controls.Button;
import fl.containers.ScrollPane;
import fl.controls.List;
import fl.data.DataProvider;

var langue:String;
var questionsInfo:QuestionsInfo;
var quLocation:String;

loadQuestion_btn.enabled = false;
questionsList.addEventListener(Event.CHANGE, onQuestionSelected);
function loadQuestionsInfo():void {
	questionsInfo = new QuestionsInfo();
	questionsInfo.addEventListener(Event.COMPLETE, onQuestionsLoaded);
	questionsInfo.loadQuestionsInfo();
}

function onQuestionsLoaded(event:Event):void {
  questionsInfo.removeEventListener(Event.COMPLETE, onQuestionsLoaded);
  //var qd:Array = 
  questionsList.dataProvider = new DataProvider(questionsInfo.getNames());
}

function onQuestionSelected(event:Event):void {
   	loadQuestion_btn.enabled = true;
	quLocation = event.target.selectedItem.data;
}


// load questions from xml
loadQuestionsInfo();

 

//questionBox.source = "http://dev2.mathamaze.ca/questions5/Q-2071-en.swf";
//questionBox.source = quLocation;

questionBox.addEventListener(Event.COMPLETE,loadListener);

function loadListener(event:Event):void {
    // on vérifie si on a cahrgé quelque quelque chose en regardant la taille chargée
	//if(questionBox.bytesLoaded != 0)
	//{
		//loaded = true;
		questionBox.content.width = 375;
		questionBox.content.scaleY = questionBox.content.scaleX;
	//}
}

//retroBox.source = "http://dev2.mathamaze.ca/questions5/Q-2071-F-en.swf";
// treat the name of question to obtein the 'retro' name
function getRetro(questionName:String):String {

   // Decompose the url to obtein the num of the question
   var parties_url:Array = questionName.split("/");
   var parties_nom:Array = parties_url[parties_url.length-1].split("-");
   //parties_nom[1] += "-F";
   var question:String = questionName.replace(parties_nom[1], parties_nom[1] + "-F");
   //var partie:String = "";
   //for each (partie in parties_nom)
      //question = question + partie + "-";
   questionNumber_txt.text = parties_nom[1];
   return question;
}

retroBox.addEventListener(Event.COMPLETE,loadRetroListener);

function loadRetroListener(event:Event):void {
    // on vérifie si on a cahrgé quelque quelque chose en regardant la taille chargée
	//if(questionBox.bytesLoaded != 0)
	//{
		//loaded = true;
		retroBox.content.width = 375;
		retroBox.content.scaleY = questionBox.content.scaleX;
	//}
}

loadQuestion_btn.label = "Load question";
loadQuestion_btn.addEventListener(MouseEvent.CLICK, clickHandler);

function clickHandler(event:MouseEvent):void {
    questionBox.load(new URLRequest(quLocation));
	retroBox.load(new URLRequest(getRetro(quLocation)));
}

previousBtn.label = "Previous";
previousBtn.addEventListener(MouseEvent.CLICK, previousHandler);

// used to obtein the next or previous name of question
function getNext(questionName:String, forward:Boolean, retro:Boolean):String {

   // Decompose the url to obtein the num of the question
   var parties_url:Array = questionName.split("/");
   var parties_nom:Array = parties_url[parties_url.length-1].split("-");
   var parties_nombre:int = int(parties_nom[1]);
   if(forward)
      parties_nombre++;
   else
      parties_nombre--;
   questionNumber_txt.text = String(parties_nombre);
   var question:String = "";
   if(retro)
      question = questionName.replace(parties_nom[1], parties_nombre + "-F");
   else
      question = questionName.replace(parties_nom[1], parties_nombre + "");   
   return question;
}

function previousHandler(event:MouseEvent):void {
	questionBox.load(new URLRequest(getNext(quLocation, false, false)));
	retroBox.load(new URLRequest(getNext(quLocation, false, true)));
    quLocation = getNext(quLocation, false, false);

}

nextBtn.label = "Next";
nextBtn.addEventListener(MouseEvent.CLICK, nextHandler);

function nextHandler(event:MouseEvent):void {
	questionBox.load(new URLRequest(getNext(quLocation, true, false)));
	retroBox.load(new URLRequest(getNext(quLocation, true, true)));
    quLocation = getNext(quLocation, true, false);

}