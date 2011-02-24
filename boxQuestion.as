//Avant tout, mettre tous les boutons invisibles
//_level0.loader.contentHolder.box_question.swapDepths(_level0.loader.contentHolder.menuPointages);

bugText = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.buttonBug;
bugTextRoll = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.buttonBug;

if(_level0.loader.contentHolder.langue == "en")
{
	cadreLoading.texte = "Loading ...";	
}

// to stop drag the game table
Mouse.removeListener(_level0.loader.contentHolder.mouseListener);

_root.prob_txt._visible = false;
btn_ok_erreur._visible = false;



btn_a._visible = false;
btn_b._visible = false;
btn_c._visible = false;
btn_d._visible = false;
btn_e._visible = false;

btn_vrai._visible = false;
btn_faux._visible = false;
//_root.vrai_txt._visible = false;
//_root.faux_txt._visible = false;

rep._visible = false;
btn_ok._visible = false;



// pour savoir si la question a ete loadee
var loaded:Boolean = false;

// pour garder l'indice du livre en cas d'utilisation
var livreIndice:Number;

// pour garder l'indice de la boule en cas d'utilisation
var bouleIndice:Number;

/*
if(_level0.loader.contentHolder.objGestionnaireEvenements.typeDeJeu == "MathEnJeu"){ 
   this.btnSendErreur._visible = false;
} else if(_level0.loader.contentHolder.objGestionnaireEvenements.typeDeJeu == "Tournament"){
   this.btnSendErreur._visible = true;
   this.question_txt._visible = false;
   this.textBoxNumQues._visible = false;
}
*/

this.btnSendError._visible = true;
this.question_txt._visible = false;
this.textBoxNumQues_txt._visible = false;


// Decompose l'url pour afficher le num de la question
var parties_url:Array = _level0.loader.contentHolder.url_question.split("/");
var parties_nom:Array = parties_url[parties_url.length-1].split("-");
textBoxNumQues_txt = parties_nom[1];


monScroll.setStyle("borderStyle", "none");
monScroll.setStyle("scrollTrackColor", "0x333333");
monScroll.setStyle("theme", "simple");

var oUserKey:Object = new Object();

oUserKey.onKeyDown = function():Void {
      if( Key.getCode() == 65 && (_level0.loader.contentHolder.type_question == "MULTIPLE_CHOICE"
							  || _level0.loader.contentHolder.type_question == "MULTIPLE_CHOICE_5"
							  || _level0.loader.contentHolder.type_question == "MULTIPLE_CHOICE_3")){
          _level0.loader.contentHolder.objGestionnaireEvenements.repondreQuestion("1");
	      _level0.loader.contentHolder.box_question.enleverBoutons();
       
      } else if( Key.getCode() == 66 && (_level0.loader.contentHolder.type_question == "MULTIPLE_CHOICE"
							  || _level0.loader.contentHolder.type_question == "MULTIPLE_CHOICE_5"
							  || _level0.loader.contentHolder.type_question == "MULTIPLE_CHOICE_3")) {
          _level0.loader.contentHolder.objGestionnaireEvenements.repondreQuestion("2");
	      _level0.loader.contentHolder.box_question.enleverBoutons();
       
      } else if( Key.getCode() == 67 && (_level0.loader.contentHolder.type_question == "MULTIPLE_CHOICE"
							  || _level0.loader.contentHolder.type_question == "MULTIPLE_CHOICE_5"
							  || _level0.loader.contentHolder.type_question == "MULTIPLE_CHOICE_3") ) {
          _level0.loader.contentHolder.objGestionnaireEvenements.repondreQuestion("3");
	      _level0.loader.contentHolder.box_question.enleverBoutons();
       
      } else if( Key.getCode() == 68 && (_level0.loader.contentHolder.type_question == "MULTIPLE_CHOICE"
							  || _level0.loader.contentHolder.type_question == "MULTIPLE_CHOICE_5"
							  || _level0.loader.contentHolder.type_question == "MULTIPLE_CHOICE_3")) {
          _level0.loader.contentHolder.objGestionnaireEvenements.repondreQuestion("4");
	      _level0.loader.contentHolder.box_question.enleverBoutons();
       
      } else if( Key.getCode() == 69 && (_level0.loader.contentHolder.type_question == "MULTIPLE_CHOICE"
							  || _level0.loader.contentHolder.type_question == "MULTIPLE_CHOICE_5"
							  || _level0.loader.contentHolder.type_question == "MULTIPLE_CHOICE_3")) {
          _level0.loader.contentHolder.objGestionnaireEvenements.repondreQuestion("5");
	      _level0.loader.contentHolder.box_question.enleverBoutons();
       
      } else {
           
      }
   };
   Key.addListener(oUserKey);


loadListener = new Object();
loadListener.complete = function(eventObject)
{
	
	// on vérifie si on a cahrgé quelque quelque chose en regardant la taille chargée
	if(monScroll.getBytesLoaded()!=0)
	{
		loaded = true;
		

		cadreLoading._visible = false;
		
		
		//Quand les questions seront bien formatees, enlever ce qu'il y a entre les
		//pointillés et mettre les 2 lignes suivantes à la place
		//FAIRE LA MEME CHOSE DANS GUI_RETRO
		
		monScroll.content._width = 444;
		monScroll.content._yscale = monScroll.content._xscale;
		Selection.setFocus(monScroll);
	    
		this.onEnterFrame = null;
	  
	  var count:Number = _level0.loader.contentHolder.planche.obtenirPerso().obtenirNombreObjet();
	
	switch (_level0.loader.contentHolder.type_question)
	{
		
		case "MULTIPLE_CHOICE" ://"ChoixReponse" :
			btn_a._visible = true;
			btn_b._visible = true;
			btn_c._visible = true;
			btn_d._visible = true;
			//btn_e._visible = true;
			
			for(var i:Number = 0; i < count; i++)
			{
				if( _level0.loader.contentHolder.planche.obtenirPerso().obtenirRangObjet(i) == "Livre")
				{
					livreIndice =  _level0.loader.contentHolder.planche.obtenirPerso().obtenirRangIDObjet(i);
				}
				else if( _level0.loader.contentHolder.planche.obtenirPerso().obtenirRangObjet(i) == "Boule")
				{
					bouleIndice =  _level0.loader.contentHolder.planche.obtenirPerso().obtenirRangIDObjet(i);
				}
			}
						
			break;
			
		case "MULTIPLE_CHOICE_3" ://"ChoixReponse" :
			btn_a._visible = true;
			btn_b._visible = true;
			btn_c._visible = true;
			
			for(var i:Number = 0; i < count; i++)
			{
				if( _level0.loader.contentHolder.planche.obtenirPerso().obtenirRangObjet(i) == "Livre")
				{
					livreIndice =  _level0.loader.contentHolder.planche.obtenirPerso().obtenirRangIDObjet(i);
				}
				else if( _level0.loader.contentHolder.planche.obtenirPerso().obtenirRangObjet(i) == "Boule")
				{
					bouleIndice =  _level0.loader.contentHolder.planche.obtenirPerso().obtenirRangIDObjet(i);
				}
			}
			
			
			break;
			
		case "MULTIPLE_CHOICE_5" ://"ChoixReponse" :
			btn_a._visible = true;
			btn_b._visible = true;
			btn_c._visible = true;
			btn_d._visible = true;
			btn_e._visible = true;
			
			for(var i:Number = 0; i < count; i++)
			{
				if( _level0.loader.contentHolder.planche.obtenirPerso().obtenirRangObjet(i) == "Livre")
				{
					livreIndice =  _level0.loader.contentHolder.planche.obtenirPerso().obtenirRangIDObjet(i);
				}
				else if( _level0.loader.contentHolder.planche.obtenirPerso().obtenirRangObjet(i) == "Boule")
				{
					bouleIndice =  _level0.loader.contentHolder.planche.obtenirPerso().obtenirRangIDObjet(i);
				}
			}
						
			break;
			
		case "TRUE_OR_FALSE" :
			
			btn_vrai._visible = true;
			btn_faux._visible = true;
			Key.removeListener(oUserKey);
			//_root.vrai_txt._visible = true;
			//_root.faux_txt._visible = true;
			
			for(var i:Number = 0; i < count; i++)
			{
				if( _level0.loader.contentHolder.planche.obtenirPerso().obtenirRangObjet(i) == "Boule")
				{
					bouleIndice =  _level0.loader.contentHolder.planche.obtenirPerso().obtenirRangIDObjet(i);
				}
			}
			break;
			
		case "SHORT_ANSWER" :
			rep._visible = true;
			Selection.setFocus(rep);
			
			btn_ok._visible = true;
			Key.removeListener(oUserKey);
			
			for(var i:Number = 0; i < count; i++)
			{
				if( _level0.loader.contentHolder.planche.obtenirPerso().obtenirRangObjet(i) == "Boule")
				{
					bouleIndice =  _level0.loader.contentHolder.planche.obtenirPerso().obtenirRangIDObjet(i);
				}
			}
			
			
			btn_ok.onRelease = function()
			{
				trace("btnOK tappé");
				traiterReponseCourte();
			}
			
			onKeyDown = function()
			{
				if(Key.getCode() == Key.ENTER)
				{
					trace("enter tappé");
					traiterReponseCourte();
				}
				else
				{
					trace("pas ENTER tappé");
				}
				
			}
			
		break;
			
		default :
			trace("Type de question inconnu");
		break;
		}
	
	}
	else
	{
		//trace("Chargement d'une question : 0 byte chargé => erreur");
		var horsService:MovieClip;
				
				cadreLoading._visible = false;
		
				horsService = _level0.loader.contentHolder.attachMovie("GUI_erreur", "HorsService", 9999);
				
				horsService.textGUI_erreur.text = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.GUIerreurQuestion;
				horsService.linkGUI_erreur._visible = true;
				horsService.linkGUI_erreur.text = _level0.loader.contentHolder.url_question;//_root.texteSource_xml.firstChild.attributes.GUIhorsService2;
				horsService.linkGUI_erreur.html = true;
	}
	
};

var sendReport:MovieClip;

this.btnSendError.onPress = function(){
	
	sendReport = _level0.loader.contentHolder.attachMovie("GUI_bugReport", "bugReport", 9999);
	Key.removeListener(oUserKey);
}

/*
livre.onPress = function()
{
	trace("utilisation du livre (ds mathemaquoi.GUI_question)\n");
	trace("id: " + livreIndice);
	//_level0.loader.contentHolder.planche.obtenirPerso().afficherObjets();
 	_level0.loader.contentHolder.objGestionnaireEvenements.utiliserObjet(livreIndice);
	_level0.loader.contentHolder.planche.obtenirPerso().enleverObjet("Livre");
		
	livre._visible = false;
}


boule.onPress = function()
{
	trace("utilisation de la boule (ds mathemaquoi.GUI_question)\n");
	trace("id: " + bouleIndice);
	
 	_level0.loader.contentHolder.objGestionnaireEvenements.utiliserObjet(bouleIndice);
	_level0.loader.contentHolder.planche.obtenirPerso().enleverObjet("Boule");
	
	boule._visible = false;
}
*/
monScroll.addEventListener("complete", loadListener);

monScroll.contentPath = _level0.loader.contentHolder.url_question;

var timerInit = getTimer();
var nbEssais = 1;

this.onEnterFrame = this.checkLoadProgress;


function checkLoadProgress()
{
	if(loaded == false) // car "this.onEnterFrame = null;" dans le "complete" ne semble pas marcher...???
	{
		var tempsPasse = (getTimer() -  timerInit)/1000;
	
		if(tempsPasse > 5)	// ca fait plus de 5 secondes qu'on essaye de loader la question
		{
			trace("Plus de 5 sec...");
			if(nbEssais < 5)
			{
				//renvoyer une requête
				monScroll.contentPath = _level0.loader.contentHolder.url_question;
				//monScroll.contentPath = "http://mathenjeu.mat.ulaval.ca/~mathenjeu/questions/MP-7701212-Q.swf";
				//reset le timer
				timerInit = getTimer();
				nbEssais++;
			}
			else
			{
				//_root.prob_txt._visible = true;
				//btn_ok_erreur._visible = true;

				var horsService:MovieClip;
				
				cadreLoading._visible = false;
		
				horsService = _level0.loader.contentHolder.attachMovie("GUI_erreur", "HorsService", 9999);
				
				horsService.textGUI_erreur.text = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.GUIerreurQuestion;
				horsService.linkGUI_erreur._visible = false;
				horsService.linkGUI_erreur.text = _level0.loader.contentHolder.url_question;//_root.texteSource_xml.firstChild.attributes.GUIhorsService2;
				horsService.linkGUI_erreur.html = true;
				
				horsService.btn_ok.onRelease = function()
				{						
					_level0.loader.contentHolder.erreurConnexion = true;
					// envoyer une réponse assurement mauvaise
					_level0.loader.contentHolder.objGestionnaireEvenements.repondreQuestion("-e+b*x");
					_level0.loader.contentHolder.box_question.gotoAndPlay(9);
					horsService.removeMovieClip();
				}
				/*
				var formatLink = new TextFormat();
				formatLink.url = _root.texteSource_xml.firstChild.attributes.GUIhorsServiceURL;
				formatLink.target = "_blank";
				formatLink.font = "Arial";
				formatLink.size = 12;
				formatLink.color = 0xFFFFFF;
				formatLink.bold = true;
				formatLink.underline = true;
				formatLink.align = "Center";
				
				horsService.linkGUI_erreur.setTextFormat(formatLink);
				*/
				_level0.loader.contentHolder.type_question = "erreur connexion";
				
				
				
				monScroll.contentPath = null;
				loaded = true;
			}
		}
	}
}



function traiterReponseCourte()
{
	rep.text._visible = false;
	if(rep.text != "")
	{
		// faire valider la réponse=rep.text
		 _level0.loader.contentHolder.objGestionnaireEvenements.repondreQuestion(rep.text);
		
		//_level0.loader.contentHolder.fenetreAffichee = false;
			//_level0.loader.contentHolder.box_question.gotoAndPlay(9);
	}	
	Key.removeListener(oUserKey);
}


function enleverBoutons()
{
	btn_a._visible = false;
	btn_b._visible = false;
	btn_c._visible = false;
	btn_d._visible = false;
	btn_e._visible = false;
	btn_ok._visible = false;
	rep._visible = false;
	btn_vrai._visible = false;
	btn_faux._visible = false;
	Key.removeListener(oUserKey);
	Mouse.addListener(_level0.loader.contentHolder.mouseListener);
	monScroll.contentPath = null;
	_level0.loader.contentHolder.planche.obtenirPerso().setUsedBook(false);

	//btnSendError._visible = false;
}