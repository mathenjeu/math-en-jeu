// import the Delegate class
import mx.utils.Delegate;
import mx.controls.TextInput;

var dokuGame:MathDoku = new MathDoku();

// declare a new XML instance
var dokuxml:XML = new XML();

// ignore tabs, returns, and other whitespace between nodes
dokuxml.ignoreWhite = true;

// set the scope of the onLoad function to the MathDoku timeline, not configxml
dokuxml.onLoad = Delegate.create(this, onXmlLoaded);

//create arrays needed for groups and laws
var unseenLines:Array = new Array();
var groupLaw:Array = new Array();
var path:String = _level0.loader.contentHolder.url_question;

this.onEnterFrame = loadXML;

function loadXML()
{
	if(path == undefined)
       dokuxml.load("mathdoku.xml");
	else
	   dokuxml.load(path);
   delete this.onEnterFrame;	
}

function onXmlLoaded(boole:Boolean)
{	
  if(boole)
  {	
	trace('reading mathdoku XML');
	treatXML();
  }
  else
  {
	 // if an XML read error occurred
     trace('error reading XML de config');
  }
}




function treatXML()
{
	var groupsChildNodes:Array = dokuxml.firstChild.childNodes;
	for(var i in groupsChildNodes)
	{
		// create new group
		var group:DokuGroup = new DokuGroup(dokuGame);
		
	   //trace(groupsChildNodes[i].nodeName);
	   var groupChildNodes:Array = groupsChildNodes[i].childNodes;
	   for(var j in groupChildNodes)
	   {
	      //trace(groupChildNodes[j].nodeName);
		  if(groupChildNodes[j].nodeName == "unseen")
		  {
			  var unseenChildNodes:Array = groupChildNodes[j].childNodes;
			  for(var s in unseenChildNodes)
			  {
				  unseenLines.push(unseenChildNodes[s].firstChild.nodeValue);				  
			  }   
		  }else if(groupChildNodes[j].nodeName == "law")
		  {
			  groupLaw.push(new Object());
			  groupLaw[groupLaw.length - 1].law = groupChildNodes[j].firstChild.nodeValue;
			  groupLaw[groupLaw.length - 1].lawCase = groupChildNodes[j].attributes.id;
			  group.setGroupLaw(groupChildNodes[j].firstChild.nodeValue, groupChildNodes[j].attributes.id, this[groupChildNodes[j].attributes.id]);
			  //trace(groupLaw[groupLaw.length - 1].law + " " + groupLaw[groupLaw.length - 1].lawCase)
		  }else if(groupChildNodes[j].nodeName == "cases")
		  {
			  var casesChildNodes:Array = groupChildNodes[j].childNodes;
			  for(var s in casesChildNodes)
			  {
				  var caseStr:String = casesChildNodes[s].attributes.id;
				  group.addCase(caseStr, this[caseStr]);				  
			  }   
		  }		  
	   }
	   // add created group to game proccesor
	   dokuGame.addGroup(group);
	}
	
	initGroups();
}

//initGroups();

function initGroups()
{	
	// trace(unseenLines.length);
	 for(var i in unseenLines)
	    this[unseenLines[i]]._visible = false;
		
	 for(var i in groupLaw)
	 {
		 this[groupLaw[i].lawCase].text = groupLaw[i].law;
	 }
	 	
}

/*
var nListener:Object = new Object();
nListener.change = function(evt_obj:Object){
	changeNumber();
};
N14.addEventListener("change", nListener);*/
/*
N14.onChanged = function(){

	dokuGame.getGroup("N14").verifyGroup();	
}


function changeNumber()
{
  //trace(" on change");
   dokuGame.getGroup("N14").verifyGroup();	
} */

// to restrict only 1-4 numbers in input
for(var i = 1; i <= 4; i++)
{
   for(var j = 1; j <= 4; j++)
   {
      this["N" + i + j].restrict = "1-4";
	  //this["G" + i + j].background = true;
	  this["N" + i + j].onChanged = function(numberField:TextField){
		  dokuGame.setCaseValue(numberField._name, numberField.text);//getGroup(numberField._name); //setCaseValue(numberField._name, numberField.text);	     
      }
   }
}

function verifyIfDid():Boolean
{
	return dokuGame.verifyIfDid();
    test_mc.NX.text = "";	
}
