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
var version:String = "";
var nCase:Number = 4; // 

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
	version = dokuxml.firstChild.attributes.caseVersion;
	
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
				  group.addCase(caseStr, this[caseStr].NB);				  
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
    // mathdoku version
	if(version != undefined)
       test_mc.NX.text = " MathDoku " + version + " x " + version;	
	else
	   test_mc.NX.text = " MathDoku ";	
	   
	// trace(unseenLines.length);
	 for(var i in unseenLines)
	    this[unseenLines[i]]._visible = false;
		
	 for(var i in groupLaw)
	 {
		 this[groupLaw[i].lawCase].text = groupLaw[i].law;
	 }
	 	
}// end InitGroups()

// to restrict only 1-4 numbers in input
for(var i = 1; i <= 4; i++)
{
	for(var j = 1; j <= 4; j++)
	{
	  this["N" + i + j].NB.restrict = "1-4";
	  //this["G" + i + j].background = true;
	  this["N" + i + j].NB.onChanged = function(numberField:TextField){
		  dokuGame.setCaseValue(numberField._name, numberField.text);//getGroup(numberField._name); //setCaseValue(numberField._name, numberField.text);	     
	  }
	  
	  this["N" + i + j].RC._alpha = 3;
	  
	  this["N" + i + j].onRollOver = function(){
		  //var names:String = "F" + this._name.substr(1);
		  //_root[this._name].RC._alpha = 60;
		  this.RC._alpha = 60;	
		  //_root[this._name].NB.text = "4";
		  //_root.attachMovie("numInsert", "nums", _root.getNextHighestDepth(),{_x:_root[this._name]._x + 25,_y:_root[this._name]._y + 15});
		  //trace(names );
	  }
	  
	  this["N" + i + j].onRollOut = function(){
		  
		  //var names:String = "F" + this._name.substr(1);
		  this.RC._alpha = 3;
		  //_root.nums.removeMovieClip();
		  //trace(this._name + this);
	  }
	  
	  this["N" + i + j].onPress = function(){
		  var names:String = this._name;
		  this.RC._alpha = 20;		  
		  this._parent.nums.removeMovieClip();
		  this._parent.attachMovie("numInsert", "nums", this._parent.getNextHighestDepth(),{_x:this._parent[this._name]._x + 25,_y:this._parent[this._name]._y + 15});
		 
		  this._parent.nums.bt1_bt.onPress = function(){ this._parent._parent[names].NB.text = "1";}
		  this._parent.nums.bt2_bt.onPress = function(){ this._parent._parent[names].NB.text = "2";}
		  this._parent.nums.bt3_bt.onPress = function(){ this._parent._parent[names].NB.text = "3";}
		  this._parent.nums.bt4_bt.onPress = function(){ this._parent._parent[names].NB.text = "4";}
		  this._parent.nums.btx_bt.onPress = function(){ this._parent._parent[names].NB.text = "";}
	  }
	  
	  
	}
}// end for's


function verifyIfDid():Boolean
{
	return dokuGame.verifyIfDid();
    //test_mc.NX.text = "";	
}
