import flash.display.*;
import flash.net.URLRequest;
import fl.controls.Button;
import fl.containers.ScrollPane;
import fl.controls.ComboBox;
import fl.controls.RadioButton;
import fl.data.DataProvider;
import fl.managers.StyleManager;
import flash.text.TextFormat;
import flash.events.Event;

var interfaceLang:String;
var questionsInfo:QuestionsInfo;
var quLocation:String;



// block used to load var from html page
function loaderComplete(myEvent:Event)
{
  var flashVars = this.loaderInfo.parameters;
  interfaceLang = flashVars.langue;
  //loadInterfaceXML();
  setInterface(interfaceLang);
}

this.loaderInfo.addEventListener(Event.COMPLETE, loaderComplete);

function setInterface(lang:String):void {
	if(lang == "fr")
	{
	   previousBtn.label = "Précédent";
       nextBtn.label = "Suivant";
       interfaceL.text = "Langue de l'interface";
	   previousBtn2.label = "Précédent";
       nextBtn2.label = "Suivant";
       //questionNr.text = "Question Nr";
	}
	else
	{
	   previousBtn.label = "Previous";
       nextBtn.label = "Next";
       interfaceL.text = "Interface Language";
	   previousBtn2.label = "Previous";
       nextBtn2.label = "Next";
       //questionNr.text = "Question Nr";
	}

}
// to change color of RadioButtons label
StyleManager.setComponentStyle(RadioButton, "textFormat", new TextFormat("Arial Black", 12, 0x660000));

interfaceFR.addEventListener(Event.CHANGE, setInterfaceFR);
interfaceEN.addEventListener(Event.CHANGE, setInterfaceEN);

function setInterfaceFR(event:Event):void { setInterface("fr");}
function setInterfaceEN(event:Event):void { setInterface("en");}

questionsList.addEventListener(Event.CHANGE, onQuestionSelected);

function onQuestionSelected(event:Event):void {
	quLocation = event.target.selectedItem.data;
	questionBox.load(new URLRequest(quLocation));
	retroBox.load(new URLRequest(getRetro(quLocation)));
}

function loadQuestionsInfo():void {
	questionsInfo = new QuestionsInfo();
	questionsInfo.addEventListener(Event.COMPLETE, onQuestionsLoaded);
	questionsInfo.loadQuestionsInfo();
}

function onQuestionsLoaded(event:Event):void {
  questionsInfo.removeEventListener(Event.COMPLETE, onQuestionsLoaded);
  questionsList.dataProvider = new DataProvider(questionsInfo.getNames());
}

// load questions from xml
loadQuestionsInfo();

//questionBox.source = "http://dev2.mathamaze.ca/questions5/Q-2071-en.swf";
//questionBox.source = quLocation;

questionBox.addEventListener(Event.COMPLETE, loadListener);

function loadListener(event:Event):void {
    	questionBox.content.width = 375;
		questionBox.content.scaleY = questionBox.content.scaleX;
}

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

retroBox.addEventListener(Event.COMPLETE, loadRetroListener);

function loadRetroListener(event:Event):void {
		retroBox.content.width = 375;
		retroBox.content.scaleY = questionBox.content.scaleX;
}

function clickHandler(event:MouseEvent):void {
    
}


previousBtn.addEventListener(MouseEvent.CLICK, previousHandler);
previousBtn2.addEventListener(MouseEvent.CLICK, previousHandler);


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


nextBtn.addEventListener(MouseEvent.CLICK, nextHandler);
nextBtn2.addEventListener(MouseEvent.CLICK, nextHandler);


function nextHandler(event:MouseEvent):void {
	questionBox.load(new URLRequest(getNext(quLocation, true, false)));
	retroBox.load(new URLRequest(getNext(quLocation, true, true)));
    quLocation = getNext(quLocation, true, false);

}