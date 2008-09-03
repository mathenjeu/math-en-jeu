

/******************************************************************************
Classe pour le jeu Lingo (version1)

09-07-2008 Jean-Michel Ruel - Version initiale
******************************************************************************/
class Lingo
{

	private var noMot:Number;				//le numéro du mot en cours dans le tableau mots
	private var pointage:Number;			//le pointage total du joueur
	private var mots:Array;					//tableau des mots
	private var motsSaisis:Array;			//tableau des mots saisis par le joueur lors de la recherche d'un mot
	private var tabChiffres:Array;			//tableau de chiffres indiquant la comparaison de chacun des mots saisis au mot recherché
	private var motDejaTrouve:Array;		//tableau des mots déjà trouvé
	private var nbMotTrouve:Number;			//le nombre de mots déjà trouvé
	private var tempsTimer:Number;			//le temps qu'il reste pour trouver le mot
	

	/*****************************************************************************
	Fonction : lingo
	Description : constructeur de la classe lingo
	******************************************************************************/
	public function Lingo()
	{
		this.pointage=0;
		this.nbMotTrouve=0;
		this.motsSaisis = new Array();
		this.tabChiffres = new Array();
		this.motDejaTrouve = new Array();
	}
	
	/*****************************************************************************
	Fonction : chargerMots
	Paramètre :
		- myXML : un objet XML déjà chargé avec les valeurs
	Description : on parcours l'objet XML et on ajoute les mots
		au tableau de mot.
	******************************************************************************/
	public function chargerMots(myXML:XML)
	{
		var root = myXML.firstChild;
        var nodes = root.childNodes;
		this.mots = new Array(nodes.length);
        for(var i=0; i<nodes.length; i++)
		{  
			var subnodes = nodes[i].childNodes;
			this.mots[i] = new Array(4);
			this.mots[i][0] = subnodes[0].firstChild.nodeValue.toString();  //mot
			this.mots[i][1] = this.calculerValeurMot(this.mots[i][0]);  	//valeur
			this.mots[i][2] = subnodes[1].firstChild.nodeValue.toString();  //description
			this.mots[i][3] = subnodes[2].firstChild.nodeValue.toString();  //hint
        }
	}

	
	/*****************************************************************************
	Fonction : calculerValeurMot
	Paramètre :
		- mot : le mot pour lequel il faut calculer la valeur
	Description : on calcule la valeur du mot selon sa longueur
	******************************************************************************/
	private function calculerValeurMot(mot:String)
	{
		var valeur:Number = mot.length;
		
		valeur = int(valeur/2+0.5);
		
		return valeur;
	}
	
	
	/*****************************************************************************
	Fonction : calculerMauvaiseReponse
	Description : On met le pointage à jour après un mauvaise réponse
	******************************************************************************/
	public function calculerMauvaiseReponse()
	{
		this.pointage = int((this.pointage/2)+0.5);
	}
	
	
	/*****************************************************************************
	Fonction : estDejaTrouve
	Paramètre :
		- no : le numéro du mot 
	Description : on vérifie si le no du mot a déjà été trouvé
		par le joueur. Retourne vrai si c'est la cas, faux sinon
	******************************************************************************/
	private function estDejaTrouve(no:Number)
	{
		var i:Number;
		for(i=0;i<this.motDejaTrouve.length;i++)
		{
			if(this.motDejaTrouve[i]==no)
			{
				return true;
			}
		}
		return false;
	}
	
	/*****************************************************************************
	Fonction : nouveauMot
	Paramètre :
	Description : on trouve un nouveau mot pour le joueur et
		on met à jour les informations.
		S'il n'en reste pas on retourne faux.
	******************************************************************************/
	public function nouveauMot()
	{
		//on vérifier s'il reste des mots à trouver
		if(this.motDejaTrouve.length == this.mots.length)
		{
			trace('Aucun mot restant');
			return false;
		}
		
		//on trouve un mot qui n'a pas encore été trouvé
		do
		{
			this.noMot = Math.floor(Math.random()*this.mots.length);
		}while(estDejaTrouve(this.noMot))

		//on vide la liste des mots saisis si nécessaire
		while (this.motsSaisis.length > 0)
		{
			this.motsSaisis.pop();
		}

		while (this.tabChiffres.length > 0)
		{
			this.tabChiffres.pop();
		}
		
		trace('Le mot à trouver est : ' + this.mots[this.noMot][0]);
		
		return true;
	}
	
	/*****************************************************************************
	Fonction : ajouterMotSaisi
	Paramètre :
	    - leMotSaisi
	Description : on ajoute aux tableaux motsSaisis et tabChiffres le mot passé
	    en paramètre et le résultat de comparaison avec le mot recherché
		respectivement
	******************************************************************************/
	public function ajouterMotSaisi(leMotSaisi:String)
	{
		if (motsSaisis.length == 5)
		{
			motsSaisis.shift();
			tabChiffres.shift();
		}
		
		motsSaisis.push(leMotSaisi);		
		tabChiffres.push(verifierMotSaisi(leMotSaisi, this.mots[this.noMot][0]));
	}
	
	/*****************************************************************************
	Fonction : verifierMotSaisi
	Paramètre :
	    - s1: le mot saisi 
		- s2: le mot recherché
	Description : on compare les deux mots, on vérifie s'il y a des lettres en
		commun et si elles sont bien placés, et on retourne une chaîne numérique
		qui est le résultat de la comparaison
	******************************************************************************/
	private function verifierMotSaisi(s1:String, s2:String)
	{
		var codeChiffre:String = new String("");
		
		//Premiere etape: identifier les bonnes lettres à la bonne place
		for (var i:Number = 0; i < s1.length; i++)
		{
			if (s1.charAt(i) == s2.charAt(i))
			{
				codeChiffre += "3";
				s1 = s1.substring(0, i) + "-" + s1.substring(i+1, s1.length);
				s2 = s2.substring(0, i) + "-" + s2.substring(i+1, s2.length);
			}
			else
			{
				codeChiffre += "1";
			}
		}
	
		//Deuxieme etape: identifier les bonnes lettres à la mauvaise place
		for (i = 0; i < s1.length; i++)
		{
			if (s1.charAt(i) != '-')
			{
				var j:Number = 0;
				var lettreTrouvee:Boolean = false;
				while(j < s2.length && !lettreTrouvee)
				{
					if (s1.charAt(i) == s2.charAt(j))
					{
						lettreTrouvee = true;
						codeChiffre = codeChiffre.substring(0, i) 
							+ "2" + codeChiffre.substring(i+1, s1.length);
						s1 = s1.substring(0, i) + "-" + s1.substring(i+1, s1.length);
						s2 = s2.substring(0, j) + "-" + s2.substring(j+1, s2.length);
					}
					j++;
				}
			}
		}
		trace(codeChiffre);
		
		return codeChiffre;
	}
	
	/*****************************************************************************
	Fonction : motTrouve
	Paramêtre :
	Description : on vérifie si le mot a été trouvé en entier, si
		oui on ajoute les points, ajoute le mot aux mots déjà trouvés 
		et retourne vrai, sinon on retourne faux
	******************************************************************************/
	public function motTrouve(nbEssais:Number)
	{
		this.pointage += this.retValeur();
		this.pointage += Math.max(0, 4 - nbEssais);
		
		trace('Le pointage : ' + this.pointage);
		this.motDejaTrouve.push(this.noMot);
		this.nbMotTrouve++;
		return true;
	}

	
	/*****************************************************************************
	* FONCTIONS DE RETOUR
	******************************************************************************/

	public function retPointage()
	{
		return this.pointage;
	}
	public function retMot()
	{
		return this.mots[this.noMot][0];
	}
	public function retValeur()
	{
		return this.mots[this.noMot][1];
	}
	public function retDescription()
	{
		return this.mots[this.noMot][2];
	}
	public function retIndice()
	{
		return this.mots[this.noMot][3];
	}
	
	public function obtenirMots()
	{
		return this.mots;
	}
	
	public function retChaineMotsSaisis()
	{
		var str = new String("");
		
		for (var i=0; i<motsSaisis.length; i++)
		{
			if (i != 0) {str += "\n";}
			str += motsSaisis[i];
		}

		return str;
	}
	
	public function retChaineTabChiffres()
	{
		var str = new String("");
		
		for (var i=0; i<tabChiffres.length; i++)
		{
			if (i != 0) {str += "\n";}
			str += tabChiffres[i];
		}

		return str;
	}
	
	public function retNbMotTrouve()
	{
		return this.nbMotTrouve;
	}
}