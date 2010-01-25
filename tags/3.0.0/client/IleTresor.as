/*******************************************************************
Math en jeu
Copyright (C) 2007 Projet SMAC

Ce programme est un logiciel libre ; vous pouvez le
redistribuer et/ou le modifier au titre des clauses de la
Licence Publique Générale Affero (AGPL), telle que publiée par
Affero Inc. ; soit la version 1 de la Licence, ou (à
votre discrétion) une version ultérieure quelconque.

Ce programme est distribué dans l'espoir qu'il sera utile,
mais SANS AUCUNE GARANTIE ; sans même une garantie implicite de
COMMERCIABILITE ou DE CONFORMITE A UNE UTILISATION
PARTICULIERE. Voir la Licence Publique
Générale Affero pour plus de détails.

Vous devriez avoir reçu un exemplaire de la Licence Publique
Générale Affero avec ce programme; si ce n'est pas le cas,
écrivez à Affero Inc., 510 Third Street - Suite 225,
San Francisco, CA 94107, USA.
*********************************************************************/


/******************************************************************************
Classe pour le jeu ileTresor (version1)

05-07-2007 Alexandre Couët - Version initiale
******************************************************************************/

class IleTresor
{
	private var noQuestion:Number;				//le numéro du mot en cours dans le tableau Question
	private var nbQuestionRepondue:Number;		//le nombre de questions déjà trouvées
	private var reponseEnCours:Boolean;			//la réponse de la question en cours
	private var questionEnCours:Boolean;		//la question en cours
	private var questionRepondue:Array;			//tableau des questions déja trouvées
	private var tempsTimer:Number;				//le temps qu'il reste pour répondre
	private var tabQuestion:Array;				//tableau des questions
	private var tabReponse:Array;				//tableau des réponses
	
	
	
	/*****************************************************************************
	Fonction : anagram
	Description : constructeur de la classe anagram
	******************************************************************************/
	public function IleTresor()
	{
		this.tempsTimer = 45;
		this.nbQuestionRepondue = 0;
		this.questionRepondue = new Array();
	}
	
	
	/*****************************************************************************
	Fonction : chargerQuestionReponse
	Paramêtre :
		- myXML : un objet XML déjà chargé avec les valeurs
	Description : on parcours l'objet XML et on ajoute les questions et
		les réponses aux tableaux respectifs.
	******************************************************************************/
	public function chargerQuestionReponse(myXML:XML)
	{
		var root = myXML.firstChild;
        var nodes = root.childNodes;
		
		this.tabQuestion = new Array();
		this.tabReponse = new Array();
		
        for(var i=0; i<nodes.length; i++)
		{  
			var subnodes = nodes[i].childNodes;
			
			this.tabQuestion.push(subnodes[0].firstChild.nodeValue.toString());
			this.tabReponse.push(subnodes[1].firstChild.nodeValue.toString());

			//au choix : ajouter un tableau pour les niveau de difficulté
			//selon ce modèle et celui dans anagram.as		
        }
	}
		

	
	/*****************************************************************************
	Fonction : estDejaTrouve
	Paramêtre :
		- no : le numéro du mot 
	Description : on vérifie si le no de la question a déjà été trouvé
		par le joueur. Retourne vrai si c'est la cas, faux sinon
	******************************************************************************/
	private function estDejaTrouve(no:Number)
	{
		var i:Number;
		for(i=0;i<this.questionRepondue.length;i++)
		{
			if(this.questionRepondue[i]==no)
			{
				return true;
			}
		}
		return false;
	}
	
	
	/*****************************************************************************
	Fonction : nouvelleQuestion
	Paramêtre :
	Description : on trouve une nouvelle question pour le joueur et
		on met à jour les informations.
		S'il n'en reste pas on retourne faux.
	******************************************************************************/
	public function nouvelleQuestion()
	{
		//on vérifier s'il reste des mots à trouver
		if(this.questionRepondue.length == this.tabQuestion.length)
		{
			trace('Aucun mot restant');
			return false;
		}

		do
		{
			this.noQuestion = Math.floor(Math.random()*this.tabQuestion.length);
		}while(estDejaTrouve(this.noQuestion))

		this.reponseEnCours = this.tabReponse[this.noQuestion];
		this.questionEnCours = this.tabQuestion[this.noQuestion];

		trace('La réponse est : ' + this.tabReponse[this.noQuestion]);
		
		return true;	
	}

	
	/*****************************************************************************
	Fonction : bonneReponse
	Paramêtre :
	Description : en cas de bonne réponse, on garde en mémoire le numéro de la
	question et on ajoute au nombre de bonnes réponses.
	******************************************************************************/
	public function bonneReponse()
	{
		this.questionRepondue.push(this.noQuestion);
		this.nbQuestionRepondue++;
	}

	
	/*****************************************************************************
	* FONCTIONS DE RETOUR
	******************************************************************************/

	public function getNoQuestion()
	{
		return this.noQuestion;
	}
	
	public function getNbQuestionRepondue()
	{
		return this.nbQuestionRepondue;
	}
	
	public function getReponseEnCours()
	{
			return this.reponseEnCours;
	}
	
	public function getQuestionEnCours()
	{
			return this.questionEnCours;
	}
	
	public function getTabQuestion()
	{
		return this.tabQuestion;
	}
	
	public function getTabReponse()
	{
		return this.tabReponse;
	}
	
	public function getTempsTimer()
	{
		return this.tempsTimer;
	}
}