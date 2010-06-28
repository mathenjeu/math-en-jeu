/* 
  Code for the actions:Frame1 MovieClip BananaToss Mathenjeu
 */
 
 
bt_annulerText = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.boutonRetour;
bt_annulerTextRoll = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.boutonRetour;


var bananaPlayers:Array = new Array();

var count:Number = _level0.loader.contentHolder.objGestionnaireEvenements.listeDesPersonnages.length;
for (var i:Number = 0; i < count; i++) {
					bananaPlayers[i] = new Object();
			        bananaPlayers[i].nom =  _level0.loader.contentHolder.objGestionnaireEvenements.listeDesPersonnages[i].nom;
			        bananaPlayers[i].pointage =  _level0.loader.contentHolder.objGestionnaireEvenements.listeDesPersonnages[i].pointage;
			        bananaPlayers[i].role = _level0.loader.contentHolder.objGestionnaireEvenements.listeDesPersonnages[i].role;
					bananaPlayers[i].idessin = _level0.loader.contentHolder.objGestionnaireEvenements.listeDesPersonnages[i].idessin;
					bananaPlayers[i].clocolor = _level0.loader.contentHolder.objGestionnaireEvenements.listeDesPersonnages[i].clocolor;
					//trace("ICI CLOCOLOR : " + bananaPlayers[i].clocolor + " " + _level0.loader.contentHolder.objGestionnaireEvenements.listeDesPersonnages[i].clocolor);
								
}// end for

//count = bananaPlayers.length;
for (i in bananaPlayers) {
	if((bananaPlayers[i].role == 2 || bananaPlayers[i].role == 3) && _level0.loader.contentHolder.objGestionnaireEvenements.typeDeJeu == "Tournament")
	   bananaPlayers.removeItemAt(i);
}// end for


bananaPlayers.sort(_level0.loader.contentHolder.objGestionnaireEvenements.compareByPointsDescending);
var id:Number;

var ID:Number = this.id;

if(_level0.loader.contentHolder.langue == "en")
   this.attachMovie("lancer_en", "lancer", 121, {_x:100, _y:224} );
else 
   this.attachMovie("lancer_fr", "lancer", 121, {_x:58, _y:229} );

/*
var myLoader1:MovieClipLoader = new MovieClipLoader();
var mclListener1:Object = new Object();

var myLoader2:MovieClipLoader = new MovieClipLoader();
var mclListener2:Object = new Object();

var myLoader3:MovieClipLoader = new MovieClipLoader();
var mclListener3:Object = new Object();

var myLoader4:MovieClipLoader = new MovieClipLoader();
var mclListener4:Object = new Object();
*/


for(var i:Number = 1; i <= count; i++)
{
   id = bananaPlayers[i - 1].idessin;
   
   if(bananaPlayers[i - 1].nom != _level0.loader.contentHolder.objGestionnaireEvenements.nomUtilisateur){
     	    	 
	  // to load the perso .. use ClipLoader to know the moment of complet load
	  // we are in for so load it dynamically
        mclListenerString = "mclListener" + i;
		this["mclListenerString"] = new Object();
		this["mclListenerString"].onLoadComplete = function(target_mc:MovieClip) {
    
	       // attention if use 10 or more players!!! must be changed! 10 == 0
		    var nameX:String = "x" + target_mc;
	        var i:Number = Number(nameX.slice(-1,nameX.length));
			target_mc.clothesCol = _level0.loader.contentHolder.toss.bananaPlayers[i - 1].clocolor;
			//trace("ICI TOSS " + target_mc.clothesCol + " " + nameX.slice(-1,nameX.length) + "  " + nameX);
						
        };
		myLoaderString = "myLoader" + i;
		this["myLoaderString"] = new MovieClipLoader();
		this["myLoaderString"].addListener(this["mclListenerString"]);
		
		this["myLoaderString"].loadClip("persox" + id + ".swf", this["perso" + i].createEmptyMovieClip("persoBanana"  + i, 100 + i)); 
				  
	  //this["perso" + i].createEmptyMovieClip("persoBanana"  + i, 100 + i);
	  //this["perso" + i]["persoBanana"  + i].loadMovie("perso" + id + ".swf");
	  this["perso" + i]["persoBanana" + i]._x = 25;
      this["perso" + i]["persoBanana" + i]._y = 65;
	  this["perso" + i]["persoBanana" + i]._xscale = 55;
      this["perso" + i]["persoBanana" + i]._yscale = 55;
      this["name" + i]["persoName" + i] = bananaPlayers[i - 1].nom;
  }//end if
 
 
 /*
  this["perso" + i]["persoBanana" + i].onRelease = function()
  {
		var namePlayer:String = _level0.loader.contentHolder.toss["name" + i]["persoName" + i];
		useBanana(namePlayer);
  }; 
  this["perso" + i]["persoBanana" + i].onRollOver = function()
  {
		_level0.loader.contentHolder.toss["name" + i]["persoName" + i].attachMovie("butToss", "but" + i, this.getNextHighestDepth(), {_x:5, _y:5});
  };
  this["perso" + i]["persoBanana" + i].onRollOut = function()
  {
		_level0.loader.contentHolder.toss["name" + i]["persoName" + i]["but" + i].removeMovieClip();
  };*/
 
}// end for

var but:MovieClip;

this.perso1.onRollOver = function()
{
	   // var movieY:Number = _level0.loader.contentHolder.toss.perso1._height * 0.9;
	    //Mouse.hide();
		but = _level0.loader.contentHolder.toss.perso1.attachMovie("bananaShell", "but1", this.getNextHighestDepth(), {_x:10, _y:10});
		but._xscale = 100;
		but._yscale = 100;
};

this.perso1.onRollOut = function()
{
		_level0.loader.contentHolder.toss.perso1.but1.removeMovieClip();
		Mouse.show();
};
this.perso1.onRelease = function()
{
		var namePlayer:String = _level0.loader.contentHolder.toss.name1.persoName1;
		useBanana(namePlayer);
}; 

this.perso2.onRollOver = function()
{
	    //var movieY:Number = _level0.loader.contentHolder.toss.perso2._height * 0.9;
    	but = _level0.loader.contentHolder.toss.perso2.attachMovie("bananaShell", "but2", this.getNextHighestDepth(), {_x:10, _y:10});
		but._xscale = 100;
		but._yscale = 100;
};

this.perso2.onRollOut = function()
{
		_level0.loader.contentHolder.toss.perso2.but2.removeMovieClip();
};

this.perso2.onRelease = function()
{
		var namePlayer:String = _level0.loader.contentHolder.toss.name2.persoName2;
		useBanana(namePlayer);
};  

this.perso3.onRollOver = function()
{
	    // var movieY:Number = _level0.loader.contentHolder.toss.perso3.persoBanana3._height * 0.9;
		but = _level0.loader.contentHolder.toss.perso3.attachMovie("bananaShell", "but3", this.getNextHighestDepth(), {_x:10, _y:10});
		but._xscale = 100;
		but._yscale = 100;;
};

this.perso3.onRollOut = function()
{
		_level0.loader.contentHolder.toss.perso3.but3.removeMovieClip();
};
this.perso3.onRelease = function()
{
		var namePlayer:String = _level0.loader.contentHolder.toss.name3.persoName3;
		useBanana(namePlayer);
}; 

this.perso4.onRollOver = function()
{
	    //var movieY:Number = _level0.loader.contentHolder.toss.perso4.persoBanana4._height * 0.9;
		but = _level0.loader.contentHolder.toss.perso4.attachMovie("bananaShell", "but4", this.getNextHighestDepth(), {_x:10, _y:10});
		but._xscale = 100;
		but._yscale = 100;
};

this.perso4.onRollOut = function()
{
		_level0.loader.contentHolder.toss.perso4.but4.removeMovieClip();
};

this.perso4.onRelease = function()
{
		var namePlayer:String = _level0.loader.contentHolder.toss.name4.persoName4;
		useBanana(namePlayer);
}; 

this.perso5.onRollOver = function()
{
	    //var movieY:Number = _level0.loader.contentHolder.toss.perso5.persoBanana5._height * 0.9;
		but = _level0.loader.contentHolder.toss.perso5.attachMovie("bananaShell", "but5", this.getNextHighestDepth(), {_x:10, _y:10});
		but._xscale = 100;
		but._yscale = 100;
};

this.perso5.onRollOut = function()
{
		_level0.loader.contentHolder.toss.perso5.but5.removeMovieClip();
};

this.perso5.onRelease = function()
{
		var namePlayer:String = _level0.loader.contentHolder.toss.name5.persoName5;
		useBanana(namePlayer);
};

this.perso6.onRelease = function()
{
		var namePlayer:String = _level0.loader.contentHolder.toss.name6.persoName6;
		useBanana(namePlayer);
};

this.perso6.onRollOver = function()
{
	    ///var movieY:Number = _level0.loader.contentHolder.toss.perso6.persoBanana6._height * 0.9;
		but = _level0.loader.contentHolder.toss.perso6.persoBanana6.attachMovie("bananaShell", "but6", this.getNextHighestDepth(), {_x:10, _y:10});
		but._xscale = 100;
		but._yscale = 100;
};

this.perso6.onRollOut = function()
{
		_level0.loader.contentHolder.toss.perso6.but6.removeMovieClip();
};

this.perso7.onRelease = function()
{
		var namePlayer:String = _level0.loader.contentHolder.toss.name7.persoName7;
		useBanana(namePlayer);
}; 
this.perso7.onRollOver = function()
{
	    //var movieY:Number = _level0.loader.contentHolder.toss.perso7.persoBanana7._height * 0.9;
		but = _level0.loader.contentHolder.toss.perso7.persoBanana7.attachMovie("bananaShell", "but7", this.getNextHighestDepth(), {_x:10, _y:10});
		but._xscale = 100;
		but._yscale = 100;
};

this.perso7.onRollOut = function()
{
		_level0.loader.contentHolder.toss.perso7.but7.removeMovieClip();
};

this.perso8.onRelease = function()
{
		var namePlayer:String = _level0.loader.contentHolder.toss.name8.persoName8;
		useBanana(namePlayer);
};

this.perso8.onRollOver = function()
{
	    //var movieY:Number = _level0.loader.contentHolder.toss.perso7.persoBanana7._height * 0.9;
		but = _level0.loader.contentHolder.toss.perso8.persoBanana8.attachMovie("bananaShell", "but8", this.getNextHighestDepth(), {_x:10, _y:10});
		but._xscale = 100;
		but._yscale = 100;
};

this.perso8.onRollOut = function()
{
		_level0.loader.contentHolder.toss.perso8.but8.removeMovieClip();
};

this.perso9.onRelease = function()
{
		var namePlayer:String = _level0.loader.contentHolder.toss.name9.persoName9;
		useBanana(namePlayer);
}; 

this.perso9.onRollOver = function()
{
	    //var movieY:Number = _level0.loader.contentHolder.toss.perso7.persoBanana7._height * 0.9;
		but = _level0.loader.contentHolder.toss.perso9.persoBanana9.attachMovie("bananaShell", "but9", this.getNextHighestDepth(), {_x:10, _y:10});
		but._xscale = 100;
		but._yscale = 100;
};

this.perso9.onRollOut = function()
{
		_level0.loader.contentHolder.toss.perso9.but9.removeMovieClip();
};

this.perso10.onRelease = function()
{
		var namePlayer:String = _level0.loader.contentHolder.toss.name10.persoName10;
		useBanana(namePlayer);
};  

this.perso10.onRollOver = function()
{
	    //var movieY:Number = _level0.loader.contentHolder.toss.perso7.persoBanana7._height * 0.9;
		but = _level0.loader.contentHolder.toss.perso10.persoBanana10.attachMovie("bananaShell", "but10", this.getNextHighestDepth(), {_x:10, _y:10});
		but._xscale = 100;
		but._yscale = 100;
};

this.perso10.onRollOut = function()
{
		_level0.loader.contentHolder.toss.perso10.but10.removeMovieClip();
};

this.perso11.onRelease = function()
{
		var namePlayer:String = _level0.loader.contentHolder.toss.name11.persoName11;
		useBanana(namePlayer);
};  

this.perso11.onRollOver = function()
{
	    //var movieY:Number = _level0.loader.contentHolder.toss.perso7.persoBanana7._height * 0.9;
		but = _level0.loader.contentHolder.toss.perso11.persoBanana11.attachMovie("bananaShell", "but11", this.getNextHighestDepth(), {_x:10, _y:10});
		but._xscale = 100;
		but._yscale = 100;
};

this.perso11.onRollOut = function()
{
		_level0.loader.contentHolder.toss.perso11.but11.removeMovieClip();
};

this.perso12.onRelease = function()
{
		var namePlayer:String = _level0.loader.contentHolder.toss.name12.persoName12;
		useBanana(namePlayer);
};  

this.perso12.onRollOver = function()
{
	    //var movieY:Number = _level0.loader.contentHolder.toss.perso7.persoBanana7._height * 0.9;
		but = _level0.loader.contentHolder.toss.perso12.persoBanana12.attachMovie("bananaShell", "but12", this.getNextHighestDepth(), {_x:10, _y:10});
		but._xscale = 100;
		but._yscale = 100;
};

this.perso12.onRollOut = function()
{
		_level0.loader.contentHolder.toss.perso12.but12.removeMovieClip();
};

bt_annulerBanane.onRelease = function()
{
	_level0.loader.contentHolder.toss.removeMovieClip();
};

function useBanana(namePlayer:String):Void
{
  _level0.loader.contentHolder.planche.obtenirPerso().enleverObjet("Banane");	
  _level0.loader.contentHolder.objGestionnaireEvenements.utiliserObjet(ID, namePlayer);
  //_level0.loader.contentHolder.planche.obtenirPerso().tossBanana();
  _level0.loader.contentHolder.planche.tossBananaShell(_level0.loader.contentHolder.objGestionnaireEvenements.nomUtilisateur, namePlayer);
  _level0.loader.contentHolder.toss.removeMovieClip();
}

