/*******************************************************************
Math en jeu
Copyright (C) 2007 Projet SMAC

Ce programme est un logiciel libre ; vous pouvez le
redistribuer et/ou le modifier au titre des clauses de la
Licence Publique Generale Affero (AGPL), telle que publiee par
Affero Inc. ; soit la version 1 de la Licence, ou (a
votre discretion) une version ulterieure quelconque.

Ce programme est distribue dans l'espoir qu'il sera utile,
mais SANS AUCUNE GARANTIE ; sans meme une garantie implicite de
COMMERCIABILITE ou DE CONFORMITE A UNE UTILISATION
PARTICULIERE. Voir la Licence Publique
Generale Affero pour plus de details.

Vous devriez avoir recu un exemplaire de la Licence Publique
Generale Affero avec ce programme; si ce n'est pas le cas,
ecrivez a Affero Inc., 510 Third Street - Suite 225,
San Francisco, CA 94107, USA.
*********************************************************************/



import Etat;
import Timer;
import ExtendedArray;
import ExtendedString;
import DispatchingXMLSocket;
import Point;
import mx.transitions.Tween;
import mx.transitions.easing.*;
import mx.events.EventDispatcher;
import mx.utils.Delegate;
// TODO : Un moment donne il va falloir integrer le reply a un evenement
//        comme pour le ping
class GestionnaireCommunication
{
    // Declaration d'une fonction qui va prendre en parametre un ecouteur
    // et qui va le garder en memoire et l'appeler lorsque necessaire
    public var addEventListener:Function;
    // Declaration d'une fonction qui va permettre d'enlever une fonction
    // qui ecoute l'evenement
    public var removeEventListener:Function;
    // Declaration d'une fonction qui va permettre d'envoyer un evenement
    // aux fonctions qui ont ete ajoutees par addEventListener
    private var dispatchEvent:Function;
    // Declaration d'un objet DispatchingXMLSocket qui va servir a communiquer
    // avec le serveur de jeu
    private var objSocketClient:DispatchingXMLSocket;
    // Declaration d'une constante gardant le delai que le timer doit attendre
    // pour determiner si une commande envoyee s'est perdue en chemin
    private var TEMPS_TIMER:Number = 8000;
    // Declaration d'un delegate qui va pointer vers la fonction qui gere
    // l'evenement de temps ecoule
    private var objTimerDelegate:Function;
    // Declaration d'un Timer qui va servir a verifier si une commande
    // a bien ete envoyee au serveur (il faut que le serveur ait renvoye
    // un accuse reception de notre commande)
    private var objTimerEnvoiCommandes:Timer;
    // Declaration d'un delegate qui va pointer vers la fonction qui gere
    // l'evenement de connexion physique
    private var objConnexionPhysiqueDelegate:Function;
    // Declaration d'un delegate qui va pointer vers la fonction qui gere
    // l'evenement de deconnexion physique
    private var objDeconnexionPhysiqueDelegate:Function;
    // Declaration d'une variable qui va permettre de garder l'etat courant du
    // client (les valeurs sont definies dans la classe Etat)
    private var intEtatClient:Number;
    // Declaration d'un tableau ou chaque element est un objet ayant les
    // champs no pour le numero de la commande, nom pour le nom de la commande,
    // objectXML pour garder la commande a envoyer en XML et listeDelegate pour
    // la liste des fonctions a appeler lors de l'arrivee des evenements.
    // listeDelegate est un tableau ExtendedArray et non une liste associative.
    private var lstCommandesAEnvoyer:ExtendedArray;
    // Declaration d'une liste dont les indices sont le nom de la commande ou
    // evenement a appeler et le contenu est un Delegate. Cette liste contient
    // un element special qui est identifie par la chaine "tableau" et qui
    // a comme valeur un ExtendedArray contenant tous les noms cles de cette
    // liste
    private var lstDelegateEvenements:Object;
    // Declaration d'un tableau ou le contenu est un objet XMLNode
    private var lstEvenementsRecus:ExtendedArray;
    // Declaration d'un tableau ou le contenu est un objet XMLNode
    // Cette liste contient les evenements a verifier et a envoyer
    private var lstEvenementsAVerifier:ExtendedArray;
    // Declaration d'une contante gardant le maximum possible pour le
    // compteur de commandes du client
    private var MAX_COMPTEUR:Number = 100;
    // Declaration d'une variable qui va servir de compteur pour envoyer des
    // commandes serveur de jeu (sa valeur maximale est 100, apres 100 on
    // recommence a 0)
    private var intCompteurCommande:Number;
    // Declaration d'une variable qui va contenir le dernier numero envoye
    // par le serveur. Cette variable va servir a traiter les evenements
    // recus dans l'ordre. Ce compteur va egalement jusqu'a 100
    private var intDerniereCommandeServeur:Number;
    // Declaration d'une variable qui va contenir le nombre d'erreurs de
    // suite etant survenues. Apres 3 erreurs, un message est envoye au
    // client
    private var intCompteurErreurs:Number;
    // Declaration d'une reference vers un objet qui garde en memoire le
    // numero de la commande par le champ no, le nom de la commande par
    // le champ nom, l'objet XML de la commande par le champ objectXML
    // et la liste des Delegate pour cette commande. C'est la commande
    // qui est presentement en train de se faire traiter (il ne peut y
    // en avoir qu'une seule). Si cette reference est a null, c'est
    // qu'aucune commande n'est presentement en traitement
    private var objCommandeEnTraitement:Object;
	
	
    /**
     *                  CONSTRUCTEUR
     * 
     de la classe GestionnaireCommunication qui prend le
     * gestionnaire de commandes en parametre.
     */
    function GestionnaireCommunication(connexionPhysiqueDelegate:Function, deconnexionPhysiqueDelegate:Function, url_serveur:String, port:Number)
    {
        // Initialiser le dispatcher d'evenements (ajoute les fonctions
        // addEventListener, removeEventListener et dispatchEvent)
        EventDispatcher.initialize(this);
        // Garder en memoire le delegate de la fonction de connexion physique
        objConnexionPhysiqueDelegate = connexionPhysiqueDelegate;
        // Garder en memoire le delegate de la fonction de deconnexion physique
        objDeconnexionPhysiqueDelegate = deconnexionPhysiqueDelegate;
        // Au debut le joueur n'est pas connecte
        intEtatClient = Etat.NON_CONNECTE.no;
        // Creer un nouveau tableau d'objets
        lstCommandesAEnvoyer = new ExtendedArray();
        // Creer une nouvelle liste d'objets
        lstDelegateEvenements = new Object();
        // Creer l'element tableau dans la liste qui va contenir les noms
        // cles dans un tableau
        lstDelegateEvenements.tableau = new ExtendedArray();
        // Creer un nouveau tableau d'objets
        lstEvenementsRecus = new ExtendedArray();
        // Creer un nouveau tableau d'objets
        lstEvenementsAVerifier = new ExtendedArray();
        // Initialiser le compteur a 0
        intCompteurCommande = 0;
        // Initialiser le compteur a -1
        intDerniereCommandeServeur = -1;
        // Au depart, aucune commande n'est en traitement
        objCommandeEnTraitement = null;
        // Le temps pour recevoir un accuse reception sera de TEMPS_TIMER/1000 secondes
        objTimerEnvoiCommandes = new Timer(TEMPS_TIMER);
        // Garder en memoire le delegate pour l'evenement de temps ecoule
        objTimerDelegate = Delegate.create(this, objTimerEnvoiCommandes_tempsEcoule);
        // Initialiser le compteur d'erreurs a 0
        intCompteurErreurs = 0;
        // Creer un nouveauDispatching XMLSocket qui va permettre de
        // communiquer avec le serveur en java
        objSocketClient = new DispatchingXMLSocket();
        // Ajouter l'ecouteur de l'evenement connect
        objSocketClient.addEventListener("connect", Delegate.create(this, objSocketClient_onConnect));
        // Ajouter l'ecouteur de l'evenement xml
        objSocketClient.addEventListener("xml", Delegate.create(this, objSocketClient_onXML));
        // Ajouter l'ecouteur de l'evenement close
        objSocketClient.addEventListener("close", Delegate.create(this, objSocketClient_onClose));
        // Essayer de se connecter au serveur de jeu
        
		objSocketClient.connect(url_serveur, port);
    }
	
    /**
     * Cet evenement est appele lorsque la connexion a reussi ou echoue.
     *
     * @param Object objetEvenement : L'objet contenant les proprietes de
     *                                l'evenement
     */
    private function objSocketClient_onConnect(objetEvenement:Object)
    {
        // Si la connexion a reussi, alors on change l'etat a DECONNECTE,
        // sinon on le remet a NON_CONNECTE (normalement il devrait deja etre
        // a cette valeur)
		//trace("ds gestCom resultat de la connection :  "+objetEvenement.succes);
        if (objetEvenement.succes==true)
        {
            intEtatClient = Etat.DECONNECTE.no;
            // Ajouter l'ecouteur de l'evenement du timer
            objTimerEnvoiCommandes.addEventListener("timeout", objTimerDelegate);
        }
        else
        {
            intEtatClient = Etat.NON_CONNECTE.no;
        }
        // Ajouter l'ecouteur de l'evenement de connexion physique
        this.addEventListener("ConnexionPhysique", objConnexionPhysiqueDelegate);
        // Lancer l'evenement permettant d'indiquer a Flash si la connexion a
        // reussie ou non
        dispatchEvent({type:"ConnexionPhysique", target:this, resultat:objetEvenement.succes});
        // Enlever l'ecouteur de l'evenement de connexion physique
        this.removeEventListener("ConnexionPhysique", objConnexionPhysiqueDelegate);
    }
	
    /**
     * Cet evenement est appele lorsque la connexion a ete coupee.
     */
    private function objSocketClient_onClose(objetEvenement:Object)
    {
	    
        // Arreter le timer s'il est actif
        if (objTimerEnvoiCommandes.estActif() == true)
        {
            objTimerEnvoiCommandes.arreter();
        }
        // Enlever l'ecouteur pour le timer
        objTimerEnvoiCommandes.removeEventListener("timeout", objTimerDelegate);
        // Le client n'est plus connecte physiquement au serveur de jeu
        intEtatClient = Etat.NON_CONNECTE.no;
        // Reinitialiser le compteur d'erreurs a 0
        intCompteurErreurs = 0;
        // S'il y a une commande en traitement, alors on doit arreter
        // l'ecouteur pour le retour de la commande
        if (objCommandeEnTraitement != null)
        {
            // Enlever l'ecouteur pour le retour de la commande qui est en
            // traitement
            this.removeEventListener(objCommandeEnTraitement.listeDelegate[0].nom, objCommandeEnTraitement.listeDelegate[0].delegate);
            // Dire qu'il n'y a plus de commandes en traitement
            objCommandeEnTraitement = null;
        }
        // Vider le tableau contenant les evenements en attente d'etre envoyes
        lstEvenementsRecus.clear();
        // Vider le tableau contenant les evenements a verifier et a envoyer
        lstEvenementsAVerifier.clear();
        // Vider le tableau des commandes a traiter (on ne peut pas traiter
        // d'autres commandes)
        lstCommandesAEnvoyer.clear();
        // Passer la liste des delegate et enlever tous les handlers
        for (var i:Number = lstDelegateEvenements.tableau.length - 1; i >= 0; i--)
        {
            // Enlever l'ecouteur pour l'evenement courant
            this.removeEventListener(lstDelegateEvenements.tableau[i], lstDelegateEvenements[lstDelegateEvenements.tableau[i]]);
            // Supprimer l'objet delegate courant
            delete lstDelegateEvenements[lstDelegateEvenements.tableau[i]];
            lstDelegateEvenements.tableau.remove(lstDelegateEvenements.tableau[i]);
        }
        // Ajouter l'ecouteur de l'evenement de deconnexion physique
        this.addEventListener("DeconnexionPhysique", objDeconnexionPhysiqueDelegate);
        // Lancer l'evenement permettant d'indiquer a Flash qu'il y a eu une
        // deconnexion physique
        dispatchEvent({type:"DeconnexionPhysique", target:this});
        // Enlever l'ecouteur de l'evenement de deconnexion physique
        this.removeEventListener("DeconnexionPhysique", objDeconnexionPhysiqueDelegate);
    }
	
    /**
     * Cet evenement est appele lorsqu'un message XML est recu par le client.
     * Un document XML est passe en parametres. Si une commande recue n'est
     * pas connue par le client, alors celle-ci est ignoree.
     *
     * @param Object objetEvenement : L'objet contenant les proprietes de
     *                                l'evenement
     */
    private function objSocketClient_onXML(objetEvenement:Object)
    {
        // Declaration d'une reference vers le noeud de commande recu
        var objNoeudCommande:XMLNode = objetEvenement.donnees.firstChild;
		
        // Si le noeud de commande est null alors on ne fait rien
        if (objNoeudCommande != null)
        {
            // Si le nom du noeud de commande est ping, alors il faut renvoyer
            // immediatement le meme objet XML au serveur
            if (objNoeudCommande.nodeName == "ping")
            {
                objSocketClient.send(objetEvenement.donnees);
            }
            // Si le message recu est une commande, alors on va determiner
            // laquelle est-ce et on va appeler la fonction adequate avec les
            // bons parametres
            else if (objNoeudCommande.nodeName == "commande")
            {
                //TODO: A ameliorer
                // Mettre a jour le numero de la derniere commande envoyee par
                // le serveur seulement si ce numero est le suivant qui devrait
                // arriver
                if ((intDerniereCommandeServeur + 1) % MAX_COMPTEUR == objNoeudCommande.attributes.no)
                {
                    intDerniereCommandeServeur = objNoeudCommande.attributes.no;
                }
                // Si la commande recue est un evenement, alors on va verifier
                // si on accepte l'evenement ou non et si on doit le garder en
                // attente ou le traiter immediatement
                if (objNoeudCommande.attributes.type == "Evenement")
                {
					trace("C'est un evenement: " + objNoeudCommande.attributes.nom);
					
                    // Appeler une fonction qui va traiter l'evenement
                    // (l'envoyer tout de suite, l'ajouter dans une liste en
                    // attendant ou l'ignorer)
                    traiterEvenement(objNoeudCommande);
                }
                // Sinon s'il y a une commande en traitement (s'il n'y en n'a
                // pas on fait rien), si la commande recue contient bien un
                // numero de client (notre numero que le serveur doit avoir
                // renvoye) et si ce numero egale le numero de notre commande
                // courante, alors on va savoir que c'est vraiment la reponse
                // a la commande courante qu'on a envoyee au serveur
                else if (objCommandeEnTraitement != null &&
                         objNoeudCommande.attributes.noClient != undefined &&
                         objNoeudCommande.attributes.noClient == objCommandeEnTraitement.no &&
                         objNoeudCommande.attributes.nom != "ParametrePasBon" &&
                         objNoeudCommande.attributes.nom != "CommandeNonReconnue")
                {
                    // Arreter le timer puisque la commande s'est bien rendue
                    // au serveur
                    objTimerEnvoiCommandes.arreter();
                    // Reinitialiser le compteur d'erreurs a 0
                    intCompteurErreurs = 0;
                    // Aiguiller la reponse du serveur vers la bonne fonction
                    switch (objCommandeEnTraitement.nom)
                    {
                        case "Connexion":
                            retourConnexion(objNoeudCommande);
                            break;
                        case "Deconnexion":
                            retourDeconnexion(objNoeudCommande);
                            break;
                        case "ObtenirListeJoueurs":
                            retourObtenirListeJoueurs(objNoeudCommande);
                            break;
                        case "ObtenirListeSalles":
                            retourObtenirListeSalles(objNoeudCommande);
                            break;
						//************************************************
						case "CreateRoom":
                            retourCreateRoom(objNoeudCommande);
                            break;
						case "getReport":
                            retourGetReport(objNoeudCommande);
                            break;
                        case "EntrerSalle":
                            retourEntrerSalle(objNoeudCommande);
                            break;
                        case "QuitterSalle":
                            retourQuitterSalle(objNoeudCommande);
                            break;
                        case "ObtenirListeJoueursSalle":
                            retourObtenirListeJoueursSalle(objNoeudCommande);
                            break;
                        case "ObtenirListeTables":
                            retourObtenirListeTables(objNoeudCommande);
                            break;
                        case "CreerTable":
                            retourCreerTable(objNoeudCommande);
                            break;
                        case "EntrerTable":
                            retourEntrerTable(objNoeudCommande);
                            break;
                        case "QuitterTable":
                            retourQuitterTable(objNoeudCommande);
                            break;
                        case "DemarrerPartie":
                            retourDemarrerPartie(objNoeudCommande);
                            break;
                        case "DeplacerPersonnage":
                            retourDeplacerPersonnage(objNoeudCommande);
                            break;
                        case "RepondreQuestion":
                            retourRepondreQuestion(objNoeudCommande);
                            break;
						case "Pointage":
                            retourDefinirPointageApresMinigame(objNoeudCommande);
                            break;
						case "Argent":
                            retourDefinirArgentApresMinigame(objNoeudCommande);
                            break;
                        case "DemarrerMaintenant":
							retourDemarrerMaintenant(objNoeudCommande);
							break;
						case "AcheterObjet":
							retourAcheterObjet(objNoeudCommande);
							break;
						case "UtiliserObjet":
							retourUtiliserObjet(objNoeudCommande);
							break;
						case "CancelQuestion":
							returnCancelQuestion(objNoeudCommande);
							break;
							
						case "Erreur":
							trace("reponse du serveur: "+objCommandeEnTraitement.nom);
							//retourErreur(objNoeudCommande);
						break;
						default:
							trace("reponse du serveur: "+objCommandeEnTraitement.nom);
						break;
                        // TODO: Ajouter d'autres cas
                    }
                }
				else
					trace("find me in gesComm ! : "+objNoeudCommande.attributes.nom);
            }
        }
    }
	
    /**
     * Cette fonction permet d'obtenir le prochain numero de commande a
     * envoyer au serveur. Ce numero est de 0 au maximum. Si le numero devient
     * plus grand que le maximum, alors le numero de commande redevient 0.
     *
     * @return Number : Le numero de la commande
     */
    private function obtenirNumeroCommande():Number
    {
        // Declaration d'une variable qui va contenir le numero de la commande
        // a retourner
        var intNumeroCommande = intCompteurCommande;
        // Incrementer le compteur de commandes
        intCompteurCommande++;
        // Si le compteur de commandes est maintenant plus grand que la plus
        // grande valeur possible, alors on reinitialise le compteur a 0
        if (intCompteurCommande > MAX_COMPTEUR)
        {
            intCompteurCommande = 0;
        }
        return intNumeroCommande;
    }
	
	
    /**
     * Cette fonction permet de traiter l'evenement passe en parametres et
     * determiner quoi faire avec lui, soit on le lance tout de suite, soit
     * on le met dans une liste d'attente qu'on va traiter plus tard, soit
     * on l'ignore. Cette fonction s'assure de traiter les evenements dans
     * leur ordre respectif.
     *
     * @param XMLNode noeudCommande : Le noeud XML contenant les
     *                                informations sur l'evenement
     */
    private function traiterEvenement(noeudCommande:XMLNode)
    {
        //TODO: A continuer
        // Si le numero de la commande est celui de la derniere commande recue
        //if (noeudCommande.attributes.no <= intDerniereCommandeServeur)
        //{
		
            // Appeler la fonction qui va traiter immediatement l'evenement
            verifierEtEnvoyerEvenement(noeudCommande);
        //}
        // Sinon on doit ajouter l'evenement dans une liste qui sera videe lors
        // d'un retour de fonction
        //else
        //{
        //    lstEvenementsAVerifier.push(noeudCommande);
        //}
    }
	
	
    /**
     * Cette fonction permet de verifier que l'evenement passe en parametre
     * est acceptable et peut etre envoye a flash. S'il est acceptable, mais
     * dans certaines conditions, alors on l'ajoute dans les evenements recus
     * et on les traitera plus tard.
     *
     * @param XMLNode noeudCommande : Le noeud XML contenant les informations
     *                                sur l'evenement a verifier
     */
    private function verifierEtEnvoyerEvenement(noeudCommande:XMLNode)
    {
        // S'il n'y a pas de commande en traitement et que l'evenement
        // est acceptable, alors on envoie l'evenement. Si il y a
        // une commande en traitement et que l'evenement est acceptable
        // pour cette commande, alors on va l'envoyer
        if ((objCommandeEnTraitement == null &&
             ExtendedArray.fromArray(Etat.obtenirEvenementsAcceptablesEtat(intEtatClient)).contains(noeudCommande.attributes.nom) == true) ||
            (objCommandeEnTraitement != null &&
             ExtendedArray.fromArray(Etat.obtenirEvenementsAcceptablesCommande(intEtatClient, objCommandeEnTraitement.nom)).contains(noeudCommande.attributes.nom) == true))
        {
            // Envoyer l'evenement aux ecouteurs de cet evenement	
            //trace("Envoyer l,evenement aux ecouteurs de cet evenement");	
            envoyerEvenement(noeudCommande);
        }
        // Sinon, s'il y a une commande en traitement, on verifie les
        // cas pouvant causer des problemes de synchronisation (un
        // evenement arrive avant le retour de la requete) et on
        // ajoute l'evenement dans une liste d'evenements en attente
        else if (objCommandeEnTraitement != null &&
                 (ExtendedArray.fromArray(Etat.obtenirEvenementsAcceptablesAvant(intEtatClient, objCommandeEnTraitement.nom)).contains(noeudCommande.attributes.nom) == true ||
                  ExtendedArray.fromArray(Etat.obtenirEvenementsAcceptablesApres(intEtatClient, objCommandeEnTraitement.nom)).contains(noeudCommande.attributes.nom) == true))
        {
            // Ajouter l'evenement a la fin de la liste	
            //trace("Ajouter l,evenement a la fin de la liste");
            lstEvenementsRecus.push(noeudCommande);
        }
    }
	
    /**
     * Cette fonction permet de traiter la prochaine commande en la chargeant
     * en memoire. Si la prochaine commande n'est pas une commande valide
     * selon l'etat present du client, alors on l'ignore et on passe a l'autre.
     */
    private function traiterProchaineCommande()
    {
        // Declaration d'une variable qui va garder le code qui va permettre
        // de determiner si on accepte la commande ou non
        var bolCommandeAcceptee:Boolean = false;
        // Declaration d'une liste des commandes acceptables
        var lstCommandesAcceptables:ExtendedArray = ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient));
        // On boucle et passe a la prochaine commande tant qu'elle n'est
        // pas acceptable
        while (lstCommandesAEnvoyer.length >= 1 && bolCommandeAcceptee == false)
        {
            // Obtenir l'objet au debut de la liste des commandes a envoyer
            var objCommande:Object = lstCommandesAEnvoyer.shift();
            // Si la commande courante est dans la liste des commandes
            // acceptables, alors on peut la charger en memoire et commencer a
            // ecouter pour ses evenements
			trace("**Commande traitee: "+objCommande.nom+" **");
            if (lstCommandesAcceptables.containsByProperty(objCommande.nom, "nom") == true)
            {
		    	trace("la commande est acceptee");
                // La commande est acceptee
                bolCommandeAcceptee = true;
                // Passer tous les Delegate de la commande courante
                for (var i:Number = 0; i < objCommande.listeDelegate.length; i++)
                {
                    // Ajouter un ecouteur pour le Delegate courant
					//trace("ds traiterProchaineCommande  addEventListener  "+objCommande.listeDelegate[i].nom);
                    this.addEventListener(objCommande.listeDelegate[i].nom, objCommande.listeDelegate[i].delegate);
                    // Si on est rendu a passer les evenements, on va les
                    // ajouter dans la liste des Delegate (le premier est
                    // toujours le delegate pour la commande de retour
                    if (i > 0)
                    {
                        // Ajouter le Delegate de l'evenement courant dans
                        // la liste en memoire
                        lstDelegateEvenements[objCommande.listeDelegate[i].nom] = objCommande.listeDelegate[i].delegate;
                        // Si le tableau ne contient pas le nom du delegate,
                        // alors on doit l'ajouter, sinon on ne fait rien
                        if (lstDelegateEvenements.tableau.contains(objCommande.listeDelegate[i].nom) == false)
                        {
                            lstDelegateEvenements.tableau.push(objCommande.listeDelegate[i].nom);
                        }
                    }
                }
                // On changer la commande en cours de traitement, envoyer la
                // commande au serveur et demarrer le timer
                objCommandeEnTraitement = objCommande;
                //trace("Commande to server: "+ExtendedString.correctAmperstand(objCommande.objetXML.toString()));
                objSocketClient.send(ExtendedString.correctAmperstand(objCommande.objetXML.toString()));
                objTimerEnvoiCommandes.demarrer();
            }
        }
        // Si on n'a pas trouver aucune commande acceptable a traiter, alors
        // il n'y a plus de commande en traitement
        if (bolCommandeAcceptee == false)
        {
            objCommandeEnTraitement = null;
        }
    }
	
    /**
     * Cette fonction permet d'envoyer l'evenement de retour de commande,
     * d'enlever l'ecouteur pour le retour de la commande, d'envoyer tous
     * les evenements qui doivent etre envoyes tout dependant de si la commande
     * a ete effectuee avec succes ou non et finalement d'enlever les ecouteurs
     * des evenements ajoutes par cette commande en cas de retour d'erreur
     *
     * @param XMLNode noeudCommande : Le noeud de commande de retour
     * @param Object objetEvenement : L'evenement de retour de commande a
     *                                envoyer
     * @param Boolean accepteApres : Permet de savoir s'il faut laisser passer
     *                               les evenements qui arrivent apres ou avant
     *                               la commande de retour
     */
    private function envoyerEtMettreAJourEvenements(noeudCommande:XMLNode, objetEvenement:Object)
    {
        // Envoyer l'evenement de retour de la commande
        dispatchEvent(objetEvenement);
        // Enlever l'ecouteur pour l'evenement de retour de la commande
        this.removeEventListener(objCommandeEnTraitement.listeDelegate[0].nom,
                                 objCommandeEnTraitement.listeDelegate[0].delegate);
        // Declaration d'une variable qui va contenir un noeud XML d'evenement
        var objNoeudEvenement:XMLNode;
        // Boucler tant qu'il y a des evenements dans la liste d'evenements
        while (lstEvenementsRecus.length > 0)
        {
            // Retirer le premier element de la liste et le mettre dans une
            // variable
            objNoeudEvenement = XMLNode(lstEvenementsRecus.shift());
            // Si la commande a fonctionne avec succes ou qu'elle n'a pas
            // fonctionnee, mais que cet evenement n'est pas un evenement qui
            // a ete ajoute par la commande courante, alors on peut envoyer
            // l'evenement, sinon on ne fait que le retirer de la liste
            if (noeudCommande.attributes.type == "Reponse" ||
                (noeudCommande.attributes.type == "Erreur" && objCommandeEnTraitement.listeDelegate.containsByProperty(objNoeudEvenement.attributes.nom, "nom") == false))
            {
                // Declaration d'une variable qui va permettre de savoir si on
                // accepte les evenements apres le retour de la commande ou
                // avant
                var bolAccepteApres:Boolean = ExtendedArray.fromArray(Etat.obtenirEvenementsAcceptablesApres(intEtatClient, objCommandeEnTraitement.nom)).contains(objNoeudEvenement.attributes.nom);
                // Si on doit accepter les evenements apres l'arrivee du retour
                // de la commande du serveur
                if (bolAccepteApres == true)
                {
                    // Si le numero de l'evenement est plus grand ou egal au numero du
                    // retour de la commande, alors on va l'envoyer, sinon on le
                    // laisse faire
                    if ((noeudCommande.attributes.no <= Math.round(MAX_COMPTEUR / 2) &&
                         objNoeudEvenement.attributes.no >= noeudCommande.attributes.no &&
                         objNoeudEvenement.attributes.no <= noeudCommande.attributes.no + Math.round(MAX_COMPTEUR / 2)) ||
                        (noeudCommande.attributes.no > Math.round(MAX_COMPTEUR / 2) &&
                         (objNoeudEvenement.attributes.no <= ((noeudCommande.attributes.no + Math.round(MAX_COMPTEUR / 2)) % MAX_COMPTEUR) ||
                          objNoeudEvenement.attributes.no >= noeudCommande.attributes.no)))
                    {
                        envoyerEvenement(objNoeudEvenement);					
                    }
                }
                // Sinon si on doit accepter ceux avant le retour
                else
                {
                    // Si le numero de l'evenement est plus petit ou egal au numero du
                    // retour de la commande, alors on va l'envoyer, sinon on le
                    // laisse faire
                    if (!((noeudCommande.attributes.no <= Math.round(MAX_COMPTEUR / 2) &&
                         objNoeudEvenement.attributes.no > noeudCommande.attributes.no &&
                         objNoeudEvenement.attributes.no < noeudCommande.attributes.no + Math.round(MAX_COMPTEUR / 2)) ||
                        (noeudCommande.attributes.no > Math.round(MAX_COMPTEUR / 2) &&
                         (objNoeudEvenement.attributes.no < ((noeudCommande.attributes.no + Math.round(MAX_COMPTEUR / 2)) % MAX_COMPTEUR) ||
                          objNoeudEvenement.attributes.no > noeudCommande.attributes.no))))
                    {
                        envoyerEvenement(objNoeudEvenement);
                    }
                }
            }
        }
        // Si la commande n'a pas fonctionnee, alors on doit enlever les
        // ecouteurs pour les evenements qui ont ete ajoutes pour cette
        // commande
        if (noeudCommande.attributes.type == "Erreur")
        {
            // Passer tous les evenements de la liste d'evenements de la
            // commande en traitement et on va enlever leurs ecouteurs
            for (var i:Number = 1; i < objCommandeEnTraitement.listeDelegate.length; i++)
            {
                // Enlever le delegate de la liste pour l'evenement courant
                delete lstDelegateEvenements[objCommandeEnTraitement.listeDelegate[i].nom];
                // Enlever toutes les occurences du nom de delegate
                lstDelegateEvenements.tableau.remove(objCommandeEnTraitement.listeDelegate[i].nom);
                // Enlever l'ecouteur pour l'evenement courant
                this.removeEventListener(objCommandeEnTraitement.listeDelegate[i].nom,
                                         objCommandeEnTraitement.listeDelegate[i].delegate);
            }
        }
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut retirer les ecouteurs des evenements
        else if (noeudCommande.attributes.type == "Reponse")
        {
            // Passer la liste des delegate et enlever tous les handlers
            for (var i:Number = lstDelegateEvenements.tableau.length - 1; i >= 0; i--)
            {
                // Si l'evenement courant n'est pas accepte dans l'etat courant
                // (l'etat est deja modifie), alors on peut enlever l'ecouteur
                if (ExtendedArray.fromArray(Etat.obtenirEvenementsAcceptablesEtat(intEtatClient)).contains(lstDelegateEvenements.tableau[i]) == false)
                {
                    // Enlever l'ecouteur pour l'evenement courant
					this.removeEventListener(lstDelegateEvenements.tableau[i], lstDelegateEvenements[lstDelegateEvenements.tableau[i]]);
		    
                    // Supprimer l'objet delegate courant
                    delete lstDelegateEvenements[lstDelegateEvenements.tableau[i]];
                    lstDelegateEvenements.tableau.remove(lstDelegateEvenements.tableau[i]);
                }
            }
        }
    }
	
    /**
     * Cette fonction permet de transformer le noeud XML de l'evenement en un
     * objet evenement et d'envoyer cet evenement aux ecouteurs.
     *
     * @param XMLNode noeudEvenement : Le noeud de l'evenement a transformer et
     *                                 a envoyer aux ecouteurs
     */
    private function envoyerEvenement(noeudEvenement:XMLNode)
    {
        // Declaration d'un objet evenement qui va contenir les parametres
        // de l'evenement a envoyer
        var objEvenement:Object = new Object();
        // Declaration d'une reference vers la liste des noeuds parametres
        var lstChildNodes:Array = noeudEvenement.childNodes;
		
		// garder en memoire la position du win the game
		var xWinGame:Number;
		var yWinGame:Number;
		var nbTracks:Number;
		/*
		for(var j:Number = 0; j<lstChildNodes.length; j++)
		{
			trace(lstChildNodes[j]);
		}*/
		//trace("noeudEvenement "+noeudEvenement);
		
        // Definir le type d'evenement et le target
        objEvenement.type = noeudEvenement.attributes.nom;
        objEvenement.target = this;
        
        //Definir nom de joueur qui a gagne
        var nomGagner:String = noeudEvenement.childNodes[0].attributes.nom;
        //trace("nomGagner = "+nomGagner);
        
        // Passer tous les noeuds enfants (parametres) et creer chaque parametre
        // dans l'objet d'evenement
        for (var i:Number = 0; i < lstChildNodes.length; i++)
        {
			//trace("ds for envoyer evenement");
            // Declarer une chaine de caractere qui va garder le type courant
            var strNomType:String = String(lstChildNodes[i].attributes.type);
            // Si l'evenement n'est pas PartieDemarree, alors on peut simplement
            // aller chercher les valeurs des parametres, sinon il faut traiter
            // cet evenement differemment
			trace(i+" strNomType = "+strNomType);
            if (noeudEvenement.attributes.nom != "PartieDemarree")
            {
				if(noeudEvenement.attributes.nom == "JoueurDeplacePersonnage")
				{
			    	//trace("if = JoueurDeplacePersonnage,  avant le switch : "+strNomType);
					switch(strNomType)
			    	{
				    	case "NomUtilisateur":
				    		objEvenement["nomUtilisateur"] = lstChildNodes[i].firstChild.nodeValue;
				    		//trace("case NomUtilisateur  "+objEvenement.nomUtilisateur+ "   "+lstChildNodes[i].firstChild.nodeValue);
				    	break;
				    
				    	case "NouveauPointage":
				    		objEvenement["pointage"] = lstChildNodes[i].firstChild.nodeValue;
				    		//trace("case Pointage  "+objEvenement.pointage+ "   "+lstChildNodes[i].firstChild.nodeValue);
				    	break;
						
						case "Bonus":
				    		objEvenement["bonus"] = lstChildNodes[i].firstChild.nodeValue;
				    		//trace("case Pointage  "+objEvenement.pointage+ "   "+lstChildNodes[i].firstChild.nodeValue);
				    	break;
				    
						case "NouvelArgent":
				    		objEvenement["argent"] = lstChildNodes[i].firstChild.nodeValue;
				    		//trace("case Argent  "+objEvenement.argent+ "   "+lstChildNodes[i].firstChild.nodeValue);
				    	break;					
					
				    	case "Collision":
				    		objEvenement["collision"] = lstChildNodes[i].firstChild.nodeValue;
				    		//objEvenement["collision"] = "piece";
				    		//trace("case collision ds gestComm ligne 679 "+objEvenement.collision+ "   "+lstChildNodes[i].firstChild.nodeValue);
				    	break;

				    	case "NouvellePosition":
				    		objEvenement["nouvellePosition"] = new Object();
				    		objEvenement["nouvellePosition"].x = Number(lstChildNodes[i].firstChild.attributes.x);
				    		//trace("case NouvellePosition  "+objEvenement.nouvellePosition.x+"   "+Number(lstChildNodes[i].firstChild.attributes.x));	    
							objEvenement["nouvellePosition"].y = Number(lstChildNodes[i].firstChild.attributes.y);
				    		//trace("case NouvellePosition  "+objEvenement.nouvellePosition.y+"   "+Number(lstChildNodes[i].firstChild.attributes.y));
				    	break;
				    
				    	case "AnciennePosition":
				    		objEvenement["anciennePosition"] = new Object();
				    		objEvenement["anciennePosition"].x = Number(lstChildNodes[i].firstChild.attributes.x);
				    		//trace("case anciennePosition  "+objEvenement.anciennePosition.x+"   "+Number(lstChildNodes[i].firstChild.attributes.x));
				    		objEvenement["anciennePosition"].y = Number(lstChildNodes[i].firstChild.attributes.y);
				    		//trace("case anciennePosition  "+objEvenement.anciennePosition.y+"   "+Number(lstChildNodes[i].firstChild.attributes.y));
				    	break;
				    
			    	}// fin du switch
		    	}	// fin du if(noeudEvenement.attributes.nom == "JoueurDeplacePersonnage")
				
				/* else if(noeudEvenement.attributes.nom == "DeplacementWinTheGame")
				{
					//trouver la case actuelle du win the game, corriger le numero de la case
					//deplacer le win the game et corriger le numero de la case

					var p:Point = new Point(noeudEvenement.firstChild.attributes.x, noeudEvenement.firstChild.attributes.y);
					
					trace(p.obtenirX());
					trace(p.obtenirY());
					
					var winTheGame:WinTheGame = new WinTheGame(10+p.obtenirX()+p.obtenirY());
					var tabCases:Array = _level0.loader.contentHolder.planche.obtenirTableauDesCases();
					
					var oListener:Object = new Object();  
					var twMove1:Tween;
					var mMemoire:Number;
					var nMemoire:Number
								
					oListener.onMotionFinished = function():Void 
					{ 
						tabCases[mMemoire][nMemoire].obtenirClipCase()["winTheGame1"].removeMovieClip();
						
						for(var m:Number = 0; m < tabCases.length; m++)
						{
							for(var n:Number = 0; n < tabCases[m].length; n++)
							{
								if( (tabCases[m][n].obtenirL() == p.obtenirX()) && (tabCases[m][n].obtenirC() == p.obtenirY()) )
								{
									tabCases[m][n].definirType(tabCases[m][n].obtenirType()+41000);
									tabCases[m][n].definirWinTheGame(winTheGame);
									tabCases[m][n].obtenirClipCase().attachMovie("winTheGame", "winTheGame1", 100);
									twMove1 = new Tween(tabCases[m][n].obtenirClipCase()["winTheGame1"], "_y", Bounce.easeOut, -500,  tabCases[m][n].obtenirClipCase()["winTheGame1"]._y, 2, true);
								   
									if(intEtatClient != Etat.ATTENTE_REPONSE_QUESTION.no)
									{
								   		_level0.loader.contentHolder.planche.afficherCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
									}
								}
							}
						}
					}
					
					// si tu veux le faire disparaitre dans les airs, il faut se rappeler de son m et n.
					for(var m:Number = 0; m < tabCases.length; m++)
					{
						for(var n:Number = 0; n < tabCases[m].length; n++)
						{
							if(tabCases[m][n].obtenirWinTheGame() != null)
							{
								tabCases[m][n].effacerWinTheGame();   
								
								twMove1 = new Tween(tabCases[m][n].obtenirClipCase()["winTheGame1"], "_y", Bounce.easeIn, tabCases[m][n].obtenirClipCase()["winTheGame1"]._y, -500, 2, true);
								twMove1.addListener(oListener);
								
								mMemoire = m;
								nMemoire = n;
							}
						}
					}
				}	// fin du if(noeudEvenement.attributes.nom == "DeplacementWinTheGame") */
				
				else if(noeudEvenement.attributes.nom == "UtiliserObjet")
				{
					switch(strNomType)
			    	{
				    	case "joueurQuiUtilise":
							trace(strNomType + " " + lstChildNodes[i].firstChild.nodeValue);
				    		objEvenement["joueurQuiUtilise"] = lstChildNodes[i].firstChild.nodeValue;
				    		//trace("case NomUtilisateur  "+objEvenement.nomUtilisateur+ "   "+lstChildNodes[i].firstChild.nodeValue);
				    	break;
				    
				    	case "joueurAffecte":
							trace(strNomType);
							objEvenement["joueurAffecte"] = lstChildNodes[i].firstChild.nodeValue;
						break;
				    
						case "objetUtilise":
							trace(strNomType);
							var tabPersonnages:Array = _level0.loader.contentHolder.planche.obtenirTableauDesPersonnages();
							objEvenement["objetUtilise"] = lstChildNodes[i].firstChild.nodeValue;
							
							switch(objEvenement["objetUtilise"])
							{
								case "Banane":
									//definirIntEtatClient(10);
									
									//objEvenement["NouvellePositionX"] = noeudEvenement.firstChild.nextSibling.nextSibling.nextSibling.firstChild.firstChild.firstChild;
									//objEvenement["NouvellePositionY"] = noeudEvenement.firstChild.nextSibling.nextSibling.nextSibling.firstChild.firstChild.nextSibling.firstChild;
									/*
									if(_level0.loader.contentHolder.objGestionnaireEvenements.nomUtilisateur == objEvenement["joueurAffecte"]){
									   var twMove:Tween;
									   var guiBanane:MovieClip;
									   guiBanane = _level0.loader.contentHolder.attachMovie("GUI_banane", "banane", 9998);
									   guiBanane._y = 200;
									   guiBanane._x = 275;
									   _level0.loader.contentHolder["banane"].nomCible = objEvenement["joueurAffecte"];
									   _level0.loader.contentHolder["banane"].nomJoueurUtilisateur = objEvenement["joueurQuiUtilise"];
									
									   twMove = new Tween(guiBanane, "_alpha", Strong.easeOut, 40, 100, 1, true);
									}// end if*/
									
									/*
									if(String(objEvenement["joueurAffecte"]) == String(tabPersonnages[i].obtenirNom()))
									{
											tabPersonnages[i].slippingBanana();
									}*/
									//_level0.loader.contentHolder.planche.getPersonnageByName(objEvenement["joueurAffecte"]);
								break;
								
								case "PotionPetit":
									// lorsqu'on utilise une potionPetit,
									// on devient petit pour 30 secondes.
									trace("on utilise la potionPetit ici.");
									
									for(var i:Number = 0; i < tabPersonnages.length; i++)
									{
										if(String(objEvenement.joueurQuiUtilise) == String(tabPersonnages[i].obtenirNom()))
										{
											var xBonhomme:Number = tabPersonnages[i].obtenirImage()._xscale;
											var yBonhomme:Number = tabPersonnages[i]._yscale;
											tabPersonnages[i].shrinkBonhommeSpecial(tabPersonnages[i].obtenirImage(), 40, 40);
										}
									}
								break;
								
								case "PotionGros":
								// lorsqu'on utilise une potionGros,
								// on devient gros pour 30 secondes.
									trace("on utilise la potionGros ici.");
									
									for(var i:Number = 0; i < tabPersonnages.length; i++)
									{
										if(String(objEvenement.joueurQuiUtilise) == String(tabPersonnages[i].obtenirNom()))
										{
											var xBonhomme:Number = tabPersonnages[i].obtenirImage()._xscale;
											var yBonhomme:Number = tabPersonnages[i]._yscale;
											tabPersonnages[i].shrinkBonhommeSpecial(tabPersonnages[i].obtenirImage(), 150, 150);
										}
									}
								break;
								
								default:
									trace("Erreur : impossible...");
								break;
							}
				    		
						break;					
			    	} // fin du switch(strNomType)
				} // fin du if(noeudEvenement.attributes.nom == "UtiliserObjet")
				
		    	else if(noeudEvenement.attributes.nom == "PartieTerminee")
			    {
					setInterval(30000);
				   	//trace("if = partieTerminee,  avant le switch : "+strNomType);
					trace(noeudEvenement.firstChild);
	
					switch(strNomType)
				   	{
						case "StatistiqueJoueur":
					   		var lstChildNodesStatistique:Array = lstChildNodes[i].childNodes;
					   		
					   		objEvenement["statistiqueJoueur"] = new Array();
					   
				    		//trace("taille de la liste de stat :  "+lstChildNodesStatistique.length);
				    
				    		for(var j:Number =0; j < lstChildNodesStatistique.length; j++)
				    		{
					    		
					    		objEvenement.statistiqueJoueur.push({nomUtilisateur:lstChildNodesStatistique[j].attributes.utilisateur, userRole:lstChildNodesStatistique[j].attributes.role, pointage:lstChildNodesStatistique[j].attributes.pointage});	
				    		}
							
					/*		if(nomGagner != "") //noeudEvenement.firstChild.attributes.nom != "")
							{
								for(var j:Number =0; j < objEvenement.statistiqueJoueur.length; j++)
								{
									if(objEvenement.statistiqueJoueur[j].nomUtilisateur == nomGagner)//noeudEvenement.firstChild.attributes.nom)
									{
										if(_level0.loader.contentHolder.langue == "Francais")
										{
											objEvenement.statistiqueJoueur[j].pointage = "Gagnant";
										}
										else //en anglais
										{
											objEvenement.statistiqueJoueur[j].pointage = "Winner";
										}
									}
								}
							}*/
									
					    break;
							
						default:
							trace("ds switch(strNomType) pour Partie Terminee - valeur invalide");
						break
				    }	// fin du switch
			    } // fin du if(noeudEvenement.attributes.nom == "PartieTerminee")
				else if(noeudEvenement.attributes.nom == "NouvelleSalle")
				{
					switch(strNomType)
			    	{
				    	case "NoSalle":
							trace(strNomType + " " + lstChildNodes[i].firstChild.nodeValue);
				    		objEvenement["NoSalle"] = lstChildNodes[i].firstChild.nodeValue;
				       	break;
						case "NomSalle":
							trace(strNomType + " " + lstChildNodes[i].firstChild.nodeValue);
				    		objEvenement["NomSalle"] = lstChildNodes[i].firstChild.nodeValue;
				       	break;
						case "ProtegeeSalle":
							trace(strNomType + " " + lstChildNodes[i].firstChild.nodeValue);
				    		objEvenement["ProtegeeSalle"] = lstChildNodes[i].firstChild.nodeValue;
				       	break;
						case "CreatorUserName":
							trace(strNomType + " " + lstChildNodes[i].firstChild.nodeValue);
				    		objEvenement["CreatorUserName"] = lstChildNodes[i].firstChild.nodeValue;
				       	break;
						case "GameType":
							trace(strNomType + " " + lstChildNodes[i].firstChild.nodeValue);
				    		objEvenement["GameType"] = lstChildNodes[i].firstChild.nodeValue;
				       	break;
						case "MasterTime":
							trace(strNomType + " " + lstChildNodes[i].firstChild.nodeValue);
				    		objEvenement["MasterTime"] = lstChildNodes[i].firstChild.nodeValue;
				       	break;
						case "MaxNbPlayers":
							trace(strNomType + " " + lstChildNodes[i].firstChild.nodeValue);
				    		objEvenement["MaxNbPlayers"] = lstChildNodes[i].firstChild.nodeValue;
				       	break;
						case "RoomDescriptions":
							trace(strNomType + " " + lstChildNodes[i].firstChild.nodeValue);
				    		objEvenement["RoomDescriptions"] = lstChildNodes[i].firstChild.nodeValue;
				       	break;
					} // end switch
				
				} // fin NouvelleSalle
			    else	// donc (noeudEvenement.attributes.nom != "PartieDemarree" NI "JoueurDeplacePersonnage" NI "PartieTerminee")
			    {
				 	//trace("ds if ds for envoyer evenement  :  "+noeudEvenement.attributes.nom+"   "+lstChildNodes[i].firstChild.nodeValue+"   "+strNomType.substring(0, 1).toLowerCase() + strNomType.substring(1, strNomType.length));
					// Le firstChild va pointer vers un noeud texte
					objEvenement[strNomType.substring(0, 1).toLowerCase() + strNomType.substring(1, strNomType.length)] = lstChildNodes[i].firstChild.nodeValue;
			    }
				
	    	} // fin du if (noeudEvenement.attributes.nom != "PartieDemarree")
			
            else	//donc (noeudEvenement.attributes.nom == "PartieDemarree")
            {
				trace("ds else ds for envoyer evenement : "+strNomType);
                // Traiter les differents cas et creer leurs objets dans objEvenement
                switch (strNomType)
                {
					// Si le cas est positionWinTheGame, alors on initialise la position du WinTheGame
                    case "positionWinTheGame":
						xWinGame = lstChildNodes[i].attributes.x;
						yWinGame = lstChildNodes[i].attributes.y;
						nbTracks = lstChildNodes[i].attributes.tracks;
						_level0.loader.contentHolder.objGestionnaireEvenements.setPointageMinimalWinTheGame(lstChildNodes[i].attributes.pointageRequis);
						
						trace("xWinGame : " + xWinGame);
						trace("yWinGame : " + yWinGame);
						trace("nbTracks : " + nbTracks);
						trace("PointageRequis : " + lstChildNodes[i].attributes.pointageRequis);
					break;
					
                    // Si le cas est TempsPartie, alors on initialise le temps de la partie
                    case "TempsPartie":
                        // Le firstChild va pointer vers un noeud texte
                        objEvenement["tempsPartie"] = lstChildNodes[i].firstChild.nodeValue;
                    break;
					
                    // Si le cas est Taille, alors on initialise le plateau de jeu
                    case "Taille":
                        // Declaration de variables qui contiennent le nombre de lignes et
                        // le nombre de colonnes du plateau de jeu
                        var intNbLignes:Number = Number(lstChildNodes[i].firstChild.attributes.nbLignes);
                        var intNbColonnes:Number = Number(lstChildNodes[i].firstChild.attributes.nbColonnes);
                        // Creer un nouveau tableau dans le plateau de jeu
                        objEvenement.plateauJeu = new Array();
                        // Passer toutes les lignes et creer un tableau dans
                        // chaque ligne du plateau de jeu
                        for (var j:Number = 0; j < intNbLignes; j++)
                        {
                            // Creer un nouveau tableau pour la ligne courante
                            objEvenement.plateauJeu.push(new Array());
                            // Passer toutes les colonnes et pour chacune, on va
                            // initialiser la valeur dans le plateau de jeu
                            for (var k:Number = 0; k < intNbColonnes; k++)
                            {
                                // Mettre 0 aux coordonnees x, y courantes
                                objEvenement.plateauJeu[j].push(0);
                            }
                        }
                    break;
					
                    // Si le cas est PositionJoueurs, alors on cree la liste des
                    // positions des joueurs
                    case "PositionJoueurs":
                        // Creer un nouveau tableau pour la position des joueurs
                        objEvenement.positionJoueurs = new Array();
                        // Declaration d'une reference vers la liste des noeuds
                        // de position des joueurs
                        var lstChildNodesPosition:Array = lstChildNodes[i].childNodes;
                        // Passer tous les noeuds position et les ajouter dans
                        // l'objet d'evenement
                        for (var j:Number = 0; j < lstChildNodesPosition.length; j++)
                        {
                            // Mettre un objet contenant le nom du joueur et sa
                            // position x, y
                            objEvenement.positionJoueurs.push({nom:lstChildNodesPosition[j].attributes.nom, x:lstChildNodesPosition[j].attributes.x, y:lstChildNodesPosition[j].attributes.y});
                        }
                    break;
					
                    // Si le cas est PlateauJeu, alors on definit les valeurs dans
                    // chaque case du plateau
                    case "PlateauJeu":
                        // Declaration d'une reference vers la liste des noeuds
                        // de cases
                        var lstChildNodesCase:Array = lstChildNodes[i].childNodes;
                        // Passer tous les noeuds case et les ajouter dans
                        // le plateau de jeu
                        for (var j:Number = 0; j < lstChildNodesCase.length; j++)
                        {
                            // Declaration d'une variable qui va contenir la
                            // valeur a mettre dans la case
                            
							//Begin with the points for finish
							var intValeurCase:Number = Number(lstChildNodesCase[j].attributes.type);
							if( _level0.loader.contentHolder.objGestionnaireEvenements.typeDeJeu == "Tournament")
							{

								var isWin:Boolean = false;
							
								for(var i:Number = 0; i < nbTracks; i++)
								{
									if((xWinGame == lstChildNodesCase[j].attributes.x )&& (yWinGame - i == lstChildNodesCase[j].attributes.y))
									isWin = true;
								}
							
								//si la case contient le winTheGame, alors on ajoute 40 000 a la valeur de sa case.
								if(isWin)							
								{
									intValeurCase += 41001;
								}
							} // end if for const of WinTheGame if type of game is Tournament
							
                            // Si la case courante est une case speciale, alors
                            // on met ajoute 90 a la valeur de la case
                            if (lstChildNodesCase[j].nodeName == "caseSpeciale")
                            {
                                intValeurCase += 90;
                            }
                            // Si le noeud de case courant a un enfant, alors
                            // c'est qu'il y a un objet sur la case (piece,
                            // objetUtilisable ou magasin)
                            if (lstChildNodesCase[j].hasChildNodes() == true)
                            {
                                // Si l'objet est un magasin
                                if (lstChildNodesCase[j].firstChild.nodeName == "magasin")
                                {
                                    // On dit que c'est un magasin
                                    intValeurCase += 10000;
                                    // Si le magasin est Magasin1, alors on lui
                                    // donne le premier type
                                    if (lstChildNodesCase[j].firstChild.attributes.nom == "Magasin1")
                                    {
                                        // On dit qu'il y a un magasin de type 1
                                        intValeurCase += 100;
                                    }
                                    else if (lstChildNodesCase[j].firstChild.attributes.nom == "Magasin2")
                                    {
                                        // On dit qu'il y a un magasin de type 2
                                        intValeurCase += 200;
                                    }
									else if (lstChildNodesCase[j].firstChild.attributes.nom == "Magasin3")
                                    {
                                        // On dit qu'il y a un magasin de type 3
                                        intValeurCase += 300;
                                    }
                                }
                                // Si l'objet est un objet utilisable
                                else if (lstChildNodesCase[j].firstChild.nodeName == "objetUtilisable")
                                {
                                    // On dit que c'est un objet utilisable
                                    intValeurCase += 30000;

                                    if (lstChildNodesCase[j].firstChild.attributes.nom == "Livre")
                                    {
                                        intValeurCase += 100;
                                        //trace("Livre");
                                    }
									else if (lstChildNodesCase[j].firstChild.attributes.nom == "Papillon")
                                    {
                                        intValeurCase += 200;
                                    }
									else if (lstChildNodesCase[j].firstChild.attributes.nom == "Telephone")
                                    {
                                        intValeurCase += 300;
                                    }
									else if (lstChildNodesCase[j].firstChild.attributes.nom == "Boule")
                                    {
                                        intValeurCase += 400;
                                    }
									else if (lstChildNodesCase[j].firstChild.attributes.nom == "PotionGros")
                                    {
                                        intValeurCase += 500;
                                    }
									else if (lstChildNodesCase[j].firstChild.attributes.nom == "PotionPetit")
                                    {
                                        intValeurCase += 600;
                                    }
									else if (lstChildNodesCase[j].firstChild.attributes.nom == "Banane")
                                    {
                                        intValeurCase += 700;
                                    }
									else
									{
					    				trace("ds envoyeEvenement, le fameux else... c'est pas un de nos objet connus");
					    				//trace(lstChildNodesCase[j].firstChild.attributes.nom);
									}
                                    // Si l'objet est invisible, alors on met la valeur
                                    // en consequence
                                    if (Boolean(lstChildNodesCase[j].firstChild.attributes.visible) == false)
                                    {
                                        // L'objet est invisible
                                        intValeurCase += 5000;
                                        //trace("invisible");
                                    }
                                }
                                // Sinon c'est que c'est un piece
                                else
                                {
                                    // On dit que c'est une piece
									// ok pour ca faudra que je change (un mini-peu) mon code mais c'est correct (cote client)
                                    intValeurCase += (20000 + (Number(lstChildNodesCase[j].firstChild.attributes.valeur) * 100));
                                }
                            }
                            // Mettre la valeur calculee a la position x, y de la case
                            objEvenement.plateauJeu[lstChildNodesCase[j].attributes.x][lstChildNodesCase[j].attributes.y] = intValeurCase;
                        }
                    break;
                }
                // Il faut maintenant mettre a jour l'etat courant et commencer
                // a accepter les evenements propres a la partie
                intEtatClient = Etat.PARTIE_DEMARREE.no;
                //TODO: Il va falloir ameliorer ca car il se pourrait que
                // l'usager fasse une commande et que la commande se traite
                // il faut donc tres bien synchroniser les evenements
                // Appeler la fonction qui va envoyer tous les evenements et
                // retirer leurs ecouteurs
                //envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
                // Traiter la prochaine commande
                //traiterProchaineCommande();
            } //fin du (noeudEvenement.attributes.nom == "PartieDemarree")
        
		} // fin de la boucle for
		
		trace("fin Evenement envoye : " + objEvenement.type);
        // Envoyer l'evenement aux ecouteurs
        dispatchEvent(objEvenement);
    }
	
    /**
     * Cet evenement est appele a chaque fois que le timer a atteint la fin de
     * son temps. Cette methode n'est pas appelee si le timer est arrete
     * manuellement.
     */
    private function objTimerEnvoiCommandes_tempsEcoule()
    {
		//trace("Temps ecoule");
        // S'il y a bel et bien une commande en traitement, alors on renvoit
        // la commande et automatiquement le timer recommence son decompte
        // de TEMPS_TIMER/1000 secondes
        if (objCommandeEnTraitement != null)
        {
            // Augmenter le compteur d'erreurs
            intCompteurErreurs++;
            // Si le compteur d'erreurs a atteint 3, alors il y a un probleme,
            // on va laisser tomber la commande courante et on va en informer
            // l'interface
            if (intCompteurErreurs == 3)
            {
                // Arreter le socket XML, cela ne provoque pas l'appel de
                // onClose(), on doit donc appeler l'evenement a la main
                objSocketClient.close();
                objSocketClient_onClose(new Object());
            }
            else
            {
                // Envoyer la commande de nouveau au serveur
                objSocketClient.send(ExtendedString.correctAmperstand(objCommandeEnTraitement.objetXML.toString()));
            }
        }
    }
	
    /**
     * Cette methode permet de se connecter au serveur de jeu.
     *
     * @param Function connexionDelegate : Un pointeur sur la fonction
     *      permettant de retourner la reponse a Flash
     * @param String nomUtilisateur : Le nom d'utilisateur du joueur
     * @param String motDePasse : Le mot de passe du joueur permettant de
     *                            se connecter au serveur de jeu
     */
    public function connexion(connexionDelegate:Function, nomUtilisateur:String,
                              motDePasse:String, langue:String)
    {
        // Si on est connecte alors on peut continuer le code de la connexion
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("Connexion", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"Connexion", delegate:connexionDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
			// Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            var objNoeudParametreNomUtilisateur:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreNomUtilisateurText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(nomUtilisateur));
            var objNoeudParametreMotDePasse:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreMotDePasseText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(motDePasse));
            var objNoeudParametreLangue:XMLNode = objObjetXML.createElement("parametre");
            //>>>>>>>>>> THis is connextion: _level0.loader
            var objNoeudParametreLangueText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(langue));
            //var objNoeudParametreGameType:XMLNode = objObjetXML.createElement("parametre");
            //var objNoeudParametreGameTypeText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(_level0.gameType));
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "Connexion";
            objNoeudParametreNomUtilisateur.attributes.type = "NomUtilisateur";
            objNoeudParametreMotDePasse.attributes.type = "MotDePasse";
            objNoeudParametreLangue.attributes.type = "Langue";
            //objNoeudParametreGameType.attributes.type = "GameType";
            objNoeudParametreNomUtilisateur.appendChild(objNoeudParametreNomUtilisateurText);
            objNoeudParametreMotDePasse.appendChild(objNoeudParametreMotDePasseText);
            objNoeudParametreLangue.appendChild(objNoeudParametreLangueText);
            //objNoeudParametreGameType.appendChild(objNoeudParametreGameTypeText);
            objNoeudCommande.appendChild(objNoeudParametreNomUtilisateur);
            objNoeudCommande.appendChild(objNoeudParametreMotDePasse);
            objNoeudCommande.appendChild(objNoeudParametreLangue);
            //objNoeudCommande.appendChild(objNoeudParametreGameType);
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "Connexion";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }
	
    /**
     * Cette methode permet de deconnecter le joueur du serveur de jeu.
     *
     * @param Function deconnexionDelegate : Un pointeur sur la
     *          fonction permettant de dire que le joueur est deconnecte
     */
    public function deconnexion(deconnexionDelegate:Function)
    {
		trace(this.intEtatClient);
        // Si on a obtenu la liste des joueurs, alors on peut continuer le code
        // de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("Deconnexion", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"Deconnexion", delegate:deconnexionDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "Deconnexion";
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "Deconnexion";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }
	
    /**
     * Cette methode permet d'obtenir la liste des joueurs connectes au
     * serveur de jeu.
     *
     * @param Function obtenirListeJoueursDelegate : Un pointeur sur la
     *          fonction permettant de retourner la liste des joueurs
     * @param Function evenementJoueurConnecteDelegate : Un pointeur sur
     *          la fonction permettant de lancer un evenement
     * @param Function evenementJoueurDeconnecteDelegate : Un pointeur sur
     *          la fonction permettant de lancer un evenement
     */
    public function obtenirListeJoueurs(obtenirListeJoueursDelegate:Function,
                                        evenementJoueurConnecteDelegate:Function,
                                        evenementJoueurDeconnecteDelegate:Function)
    {
        // Si on est connecte alors on peut continuer le code de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("ObtenirListeJoueurs", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"ObtenirListeJoueurs", delegate:obtenirListeJoueursDelegate});
            // Ajouter les autres Delegate d'evenements
            lstDelegateCommande.push({nom:"JoueurConnecte", delegate:evenementJoueurConnecteDelegate});
            lstDelegateCommande.push({nom:"JoueurDeconnecte", delegate:evenementJoueurDeconnecteDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "ObtenirListeJoueurs";
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "ObtenirListeJoueurs";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on ne peut pas faire la commande tout de suite
        }
    }
	
    /**
     * Cette methode permet d'obtenir la liste des salles .
     *
     * @param Function obtenirListeSallesDelegate : Un pointeur sur la
     *          fonction permettant de retourner la liste des salles
     */
    public function obtenirListeSalles(obtenirListeSallesDelegate:Function, evenementNouvelleSalleDelegate:Function,
									   client:Number)
    {
        // Si on a obtenu la liste des joueurs, alors on peut continuer le code
        // de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("ObtenirListeSalles", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"ObtenirListeSalles", delegate:obtenirListeSallesDelegate});
			
			lstDelegateCommande.push({nom:"NouvelleSalle", delegate:evenementNouvelleSalleDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "ObtenirListeSalles";
            //*************************
			var objNoeudParametreClientType:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreClientTypeText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(String(client)));
            
            // Construire l'arbre du document XML
            
            objNoeudParametreClientType.attributes.type = "ClientType";
           
            objNoeudParametreClientType.appendChild(objNoeudParametreClientTypeText);
            
            objNoeudCommande.appendChild(objNoeudParametreClientType);
            
			//***************************
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "ObtenirListeSalles";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }
	
	//*********************************************************************
	 /**
     * Cette methode permet au joueur de creer une salle.
     *
     * @param Function createRoomDelegate : Un pointeur sur la
     *          fonction permettant au joueur de creer une salle
     * 
     * @params String - les params de la salle
     */
    public function createRoom(createRoomDelegate:Function,
                               nameRoom:String, description:String, pass:String, fromDate:String,
							   toDate:String, defaultTime:String, roomCategories:String)
    {
        // Si on a obtenu la liste des tables, alors on peut continuer le code
        // de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("CreateRoom", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"CreateRoom", delegate:createRoomDelegate});
            // Ajouter les autres Delegate d'evenements
            ////lstDelegateCommande.push({nom:"JoueurDemarrePartie", delegate:evenementJoueurDemarrePartieDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            
			var objNoeudParametreNameRoom:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreNameRoomText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(nameRoom));
			
			var objNoeudParametreDescription:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreDescriptionText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(String(description)));
			
			var objNoeudParametrePass:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametrePassText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(pass));
			
			var objNoeudParametreBeginDate:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreBeginDateText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(fromDate));
			
			var objNoeudParametreEndDate:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreEndDateText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(toDate));
			
			var objNoeudParametreDefaultTime:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreDefaultTimeText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(String(defaultTime)));
			
			var objNoeudParametreRoomCategories:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreRoomCategoriesText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(roomCategories));
			
			// Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "CreateRoom";
            objNoeudParametreNameRoom.attributes.type = "NameRoom";
            objNoeudParametreNameRoom.appendChild(objNoeudParametreNameRoomText);
			objNoeudParametreDescription.attributes.type = "Description";
            objNoeudParametreDescription.appendChild(objNoeudParametreDescriptionText);
			objNoeudParametrePass.attributes.type = "Password";
            objNoeudParametrePass.appendChild(objNoeudParametrePassText);
			objNoeudParametreBeginDate.attributes.type = "BeginDate";
            objNoeudParametreBeginDate.appendChild(objNoeudParametreBeginDateText);
			objNoeudParametreEndDate.attributes.type = "EndDate";
            objNoeudParametreEndDate.appendChild(objNoeudParametreEndDateText);
			objNoeudParametreDefaultTime.attributes.type = "DefaultTime";
            objNoeudParametreDefaultTime.appendChild(objNoeudParametreDefaultTimeText);
			objNoeudParametreRoomCategories.attributes.type = "RoomCategories";
            objNoeudParametreRoomCategories.appendChild(objNoeudParametreRoomCategoriesText);
			
			//trace ("GC : defT : " + defaultTime + " " + ExtendedString.encodeToUTF8(defaultTime));
						
            objNoeudCommande.appendChild(objNoeudParametreNameRoom);
			objNoeudCommande.appendChild(objNoeudParametreDescription);
            objNoeudCommande.appendChild(objNoeudParametrePass);
			objNoeudCommande.appendChild(objNoeudParametreBeginDate);
            objNoeudCommande.appendChild(objNoeudParametreEndDate);
			objNoeudCommande.appendChild(objNoeudParametreDefaultTime);
			objNoeudCommande.appendChild(objNoeudParametreRoomCategories);
					
			objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "CreateRoom";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
			//trace(objObjetXML);
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }
	//*********************************************************************
	
	//*********************************************************************
	 /**
     * Cette methode permet au joueur d'ontenir le rapport sur une salle.
     *
     * @param Function getReportDelegate : Un pointeur sur la
     *          fonction permettant au joueur d'ontenir le rapport sur une salle
     * 
     * @params String - les params de la salle
     */
    public function getReport(getReportDelegate:Function, idRoom:Number)
    {
        // Si on a obtenu la liste des tables, alors on peut continuer le code
        // de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("getReport", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"getReport", delegate:getReportDelegate});
            // Ajouter les autres Delegate d'evenements
            ////lstDelegateCommande.push({nom:"JoueurDemarrePartie", delegate:evenementJoueurDemarrePartieDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            
			var objNoeudParametreNameRoom:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreNameRoomText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(String(idRoom)));
			
						
			// Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "getReport";
            objNoeudParametreNameRoom.attributes.type = "IDRoom";
            objNoeudParametreNameRoom.appendChild(objNoeudParametreNameRoomText);
			
            objNoeudCommande.appendChild(objNoeudParametreNameRoom);
								
			objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "getReport";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
			//trace(objObjetXML);
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }
	//*********************************************************************
	
	
    /**
     * Cette methode permet au joueur d'entrer dans une salle.
     *
     * @param Function entrerSalleDelegate : Un pointeur sur la
     *          fonction permettant au joueur d'entrer dans une salle
     * @param String nomSalle : Le nom de la salle
     * @param String motDePasse : Le mot de passe pour entrer dans la salle
     *                            (ce mot de passe peut etre vide)
     */
    public function entrerSalle(entrerSalleDelegate:Function, idRoom:Number, motDePasse:String)
    {
        // Si on a obtenu la liste des salles, alors on peut continuer le code
        // de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("EntrerSalle", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"EntrerSalle", delegate:entrerSalleDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            var objNoeudParametreIdRoom:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreIdRoomText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(String(idRoom)));
            var objNoeudParametreMotDePasse:XMLNode = objObjetXML.createElement("parametre");
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "EntrerSalle";
            objNoeudParametreIdRoom.attributes.type = "RoomID";
            objNoeudParametreMotDePasse.attributes.type = "MotDePasse";
            objNoeudParametreIdRoom.appendChild(objNoeudParametreIdRoomText);
            // Si le mot de passe est vide ou a null, alors on n'a pas besoin
            // de le creer, sinon il faut l'ajouter
            if (motDePasse != null && motDePasse != "")
            {
                var objNoeudParametreMotDePasseText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(motDePasse));
                objNoeudParametreMotDePasse.appendChild(objNoeudParametreMotDePasseText);
            }
            objNoeudCommande.appendChild(objNoeudParametreIdRoom);
            objNoeudCommande.appendChild(objNoeudParametreMotDePasse);
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "EntrerSalle";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }
	
	
    /**
     * Cette methode permet au joueur de quitter la salle dans laquelle il se
     * trouve.
     *
     * @param Function quitterSalleDelegate : Un pointeur sur la
     *          fonction permettant de dire que le joueur a quitte la salle
     */
    public function quitterSalle(quitterSalleDelegate:Function)
    {
        // Si on est dans une salle, alors on peut continuer le code de la
        // fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("QuitterSalle", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"QuitterSalle", delegate:quitterSalleDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "QuitterSalle";
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "QuitterSalle";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }

	
    /**
     * Cette methode permet d'obtenir la liste des joueurs de la salle dans
     * laquelle le joueur se trouve .
     *
     * @param Function obtenirListeJoueursSalleDelegate : Un pointeur sur la
     *          fonction permettant de retourner la liste des joueurs de la salle
     * @param Function evenementJoueurEntreSalleDelegate : Un pointeur sur
     *          la fonction permettant de lancer un evenement
     * @param Function evenementJoueurQuitteSalleDelegate : Un pointeur sur
     *          la fonction permettant de lancer un evenement
     */
    public function obtenirListeJoueursSalle(obtenirListeJoueursSalleDelegate:Function,
                                        evenementJoueurEntreSalleDelegate:Function,
                                        evenementJoueurQuitteSalleDelegate:Function)
    {
        // Si on est dans le bon etat alors on peut continuer le code de la
        // fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("ObtenirListeJoueursSalle", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"ObtenirListeJoueursSalle", delegate:obtenirListeJoueursSalleDelegate});
            // Ajouter les autres Delegate d'evenements
            lstDelegateCommande.push({nom:"JoueurEntreSalle", delegate:evenementJoueurEntreSalleDelegate});
            lstDelegateCommande.push({nom:"JoueurQuitteSalle", delegate:evenementJoueurQuitteSalleDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "ObtenirListeJoueursSalle";
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "ObtenirListeJoueursSalle";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on ne peut pas faire la commande tout de suite
        }
    }
	
    /**
     * Cette methode permet d'obtenir la liste des tables de la salle courante.
     *
     * @param Function obtenirListeTablesDelegate : Un pointeur sur la
     *          fonction permettant de retourner la liste des tables
     * @param Function evenementJoueurEntreTableDelegate : Un pointeur sur
     *          la fonction permettant de lancer un evenement
     * @param Function evenementJoueurQuitteTableDelegate : Un pointeur sur
     *          la fonction permettant de lancer un evenement
     * @param Function evenementNouvelleTableDelegate : Un pointeur sur
     *          la fonction permettant de lancer un evenement
     * @param Function evenementTableDetruiteDelegate : Un pointeur sur
     *          la fonction permettant de lancer un evenement
     * @param String filtre : Le filtre a appliquer pour savoir quelles tables
     *                        retourner
     */
    public function obtenirListeTables(obtenirListeTablesDelegate:Function,
                                       evenementJoueurEntreTableDelegate:Function,
                                       evenementJoueurQuitteTableDelegate:Function,
                                       evenementNouvelleTableDelegate:Function,
                                       evenementTableDetruiteDelegate:Function,
                                       filtre:String)
    {
        // Si on a obtenu la liste des joueurs de la salle, alors on peut
        // continuer le code de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("ObtenirListeTables", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"ObtenirListeTables", delegate:obtenirListeTablesDelegate});
            // Ajouter les autres Delegate d'evenements
            lstDelegateCommande.push({nom:"JoueurEntreTable", delegate:evenementJoueurEntreTableDelegate});
            lstDelegateCommande.push({nom:"JoueurQuitteTable", delegate:evenementJoueurQuitteTableDelegate});
            lstDelegateCommande.push({nom:"NouvelleTable", delegate:evenementNouvelleTableDelegate});
            lstDelegateCommande.push({nom:"TableDetruite", delegate:evenementTableDetruiteDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            var objNoeudParametreFiltre:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreFiltreText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(filtre));
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "ObtenirListeTables";
            objNoeudParametreFiltre.attributes.type = "Filtre";
            objNoeudParametreFiltre.appendChild(objNoeudParametreFiltreText);
            objNoeudCommande.appendChild(objNoeudParametreFiltre);
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "ObtenirListeTables";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }
	//**************************************************************
	 public function obtenirListeTablesApres(obtenirListeTablesDelegate:Function, filtre:String)
    {
        // Si on a obtenu la liste des joueurs de la salle, alors on peut
        // continuer le code de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("ObtenirListeTables", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"ObtenirListeTables", delegate:obtenirListeTablesDelegate});
                      
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            var objNoeudParametreFiltre:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreFiltreText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(filtre));
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "ObtenirListeTables";
            objNoeudParametreFiltre.attributes.type = "Filtre";
            objNoeudParametreFiltre.appendChild(objNoeudParametreFiltreText);
            objNoeudCommande.appendChild(objNoeudParametreFiltre);
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "ObtenirListeTables";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }
	//******************************************************************************************
	
    /**
     * Cette methode permet au joueur de creer une table.
     *
     * @param Function creerTableDelegate : Un pointeur sur la
     *          fonction permettant au joueur de creer une table
     * @param Function evenementJoueurDemarrePartieDelegate : Un pointeur sur
     *          la fonction permettant de lancer un evenement
     * @param Number tempsPartie : Le temps de la partie
     */
    public function creerTable(creerTableDelegate:Function,
                               evenementJoueurDemarrePartieDelegate:Function,
                               tempsPartie:Number, tablName:String)
    {
        // Si on a obtenu la liste des tables, alors on peut continuer le code
        // de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("CreerTable", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"CreerTable", delegate:creerTableDelegate});
            // Ajouter les autres Delegate d'evenements
            lstDelegateCommande.push({nom:"JoueurDemarrePartie", delegate:evenementJoueurDemarrePartieDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            var objNoeudParametreTempsPartie:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreTempsPartieText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(String(tempsPartie)));
			
			var objNoeudParametreNomPartie:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreNomPartieText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(tablName));
			
			// Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "CreerTable";
            objNoeudParametreTempsPartie.attributes.type = "TempsPartie";
            objNoeudParametreTempsPartie.appendChild(objNoeudParametreTempsPartieText);
			objNoeudParametreNomPartie.attributes.type = "TableName";
            objNoeudParametreNomPartie.appendChild(objNoeudParametreNomPartieText);
			
            objNoeudCommande.appendChild(objNoeudParametreTempsPartie);
			 objNoeudCommande.appendChild(objNoeudParametreNomPartie);
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "CreerTable";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
			//trace(objObjetXML);
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }
	
    /**
     * Cette methode permet au joueur d'entrer dans une table.
     *
     * @param Function entrerTableDelegate : Un pointeur sur la
     *          fonction permettant au joueur d'entrer dans une table
     * @param Function evenementJoueurDemarrePartieDelegate : Un pointeur sur
     *          la fonction permettant de lancer un evenement
     * @param Number noTable : Le numero de la table
     */
    public function entrerTable(entrerTableDelegate:Function,
                                evenementJoueurDemarrePartieDelegate:Function,
                                noTable:Number)
    {
        // Si on a obtenu la liste des tables, alors on peut continuer le code
        // de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("EntrerTable", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"EntrerTable", delegate:entrerTableDelegate});
            // Ajouter les autres Delegate d'evenements
            lstDelegateCommande.push({nom:"JoueurDemarrePartie", delegate:evenementJoueurDemarrePartieDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            var objNoeudParametreNoTable:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreNoTableText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(String(noTable)));
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "EntrerTable";
            objNoeudParametreNoTable.attributes.type = "NoTable";
            objNoeudParametreNoTable.appendChild(objNoeudParametreNoTableText);
            objNoeudCommande.appendChild(objNoeudParametreNoTable);
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "EntrerTable";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }
	
    /**
     * Cette methode permet au joueur de quitter la table dans laquelle il se
     * trouve.
     *
     * @param Function quitterTableDelegate : Un pointeur sur la
     *          fonction permettant de dire que le joueur a quitte la table
     */
    public function quitterTable(quitterTableDelegate:Function)
    {
        // Si on est dans une table, alors on peut continuer le code de la
        // fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("QuitterTable", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"QuitterTable", delegate:quitterTableDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "QuitterTable";
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "QuitterTable";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }
	
	
	/**
     * Cette methode permet au joueur de demarrer la partie avant qu'il n'y ait
     * 4 joueurs a la table.
     *
     * @param Function demarrerMaintenantDelegate : Un pointeur sur la
     * fonction permettant de savoir si la commande a ete acceptee
	 * par le serveur.
     */
    public function demarrerMaintenant(demarrerMaintenantDelegate:Function, idPersonnage:Number, niveau:String)
    {
		
        // Si on est dans une table, alors on peut continuer le code de la
        // fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("DemarrerMaintenant", "nom") == true)
        {
			
		
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"DemarrerMaintenant", delegate:demarrerMaintenantDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
	    	var objNoeudParametreIdPersonnage:XMLNode = objObjetXML.createElement("parametre");
	    	var objNoeudParametreNiveau:XMLNode = objObjetXML.createElement("parametre");
	    
            var objNoeudParametreIdPersonnageText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(String(idPersonnage)));
	    	var objNoeudParametreNiveauText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(niveau));
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "DemarrerMaintenant";
	    
	    	objNoeudParametreIdPersonnage.attributes.type = "IdPersonnage";
            objNoeudParametreIdPersonnage.appendChild(objNoeudParametreIdPersonnageText);
	    
	    	objNoeudParametreNiveau.attributes.type = "NiveauJoueurVirtuel";  
	    	objNoeudParametreNiveau.appendChild(objNoeudParametreNiveauText);
	    
            objNoeudCommande.appendChild(objNoeudParametreIdPersonnage);
	    	objNoeudCommande.appendChild(objNoeudParametreNiveau);
	    
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "DemarrerMaintenant";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
				trace("commande DemarrerMaintenant traitee");
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }
	
	
    /**
     * Cette methode permet au joueur de demarrer une partie.
     *
     * @param Function demarrerPartieDelegate : Un pointeur sur la
     *          fonction permettant au joueur de demarrer une partie
     * @param Function evenementPartieDemarreeDelegate : Un pointeur sur
     *          la fonction permettant de lancer un evenement
     * @param Function evenementJoueurDeplacePersonnageDelegate : Un pointeur
     *          sur la fonction permettant de lancer un evenement
     * @param Funtion evenementSyncroniserTemps : Un pointeur sur la fonction
     *		permettant de syncroniser le temps.
     * @param Number idPersonnage : Le numero Id du personnage
     */
    public function demarrerPartie(demarrerPartieDelegate:Function,
                                   evenementPartieDemarreeDelegate:Function,
                                   evenementJoueurDeplacePersonnageDelegate:Function,
				                   evenementSynchroniserTempsDelegate:Function,
								   evenementUtiliserObjetDelegate:Function,
				                   evenementPartieTermineeDelegate:Function,
				                   idPersonnage:Number) 
    {
        // Si on est dans une table, alors on peut continuer le code de la
        // fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("DemarrerPartie", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"DemarrerPartie", delegate:demarrerPartieDelegate});
            // Ajouter les autres Delegate d'evenements
            lstDelegateCommande.push({nom:"PartieDemarree", delegate:evenementPartieDemarreeDelegate});
            //TODO: A enlever ou a penser s'il faut le laisser la
            lstDelegateCommande.push({nom:"JoueurDeplacePersonnage", delegate:evenementJoueurDeplacePersonnageDelegate});
	    	lstDelegateCommande.push({nom:"SynchroniserTemps", delegate:evenementSynchroniserTempsDelegate});
			lstDelegateCommande.push({nom:"UtiliserObjet", delegate:evenementUtiliserObjetDelegate});
	    
	    	lstDelegateCommande.push({nom:"PartieTerminee", delegate:evenementPartieTermineeDelegate});
            
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            var objNoeudParametreIdPersonnage:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreIdPersonnageText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(String(idPersonnage)));
		
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "DemarrerPartie";
            objNoeudParametreIdPersonnage.attributes.type = "IdPersonnage";
            objNoeudParametreIdPersonnage.appendChild(objNoeudParametreIdPersonnageText);
            objNoeudCommande.appendChild(objNoeudParametreIdPersonnage);
														
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "DemarrerPartie";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }

	
    /**
     * Cette methode permet au joueur de deplacer un personnage.
     *
     * @param Function deplacerPersonnageDelegate : Un pointeur sur la
     *          fonction permettant au joueur de deplacer son personnage
     * @param Point nouvellePosition : La nouvelle position du personnage
     */
    public function deplacerPersonnage(deplacerPersonnageDelegate:Function,
                                       nouvellePosition:Point)
    {
        trace("ds gestCom deplacerPersonnage");
        // Si la partie est commencee, alors on peut continuer le code de la
        // fonction
		trace("intEtatClient : " + intEtatClient);
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("DeplacerPersonnage", "nom") == true)
        {
        	trace("ds gestCom deplacerPersonnage    ds if");
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"DeplacerPersonnage", delegate:deplacerPersonnageDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            var objNoeudParametreNouvellePosition:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudPosition:XMLNode = objObjetXML.createElement("position");
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "DeplacerPersonnage";
            objNoeudParametreNouvellePosition.attributes.type = "NouvellePosition";
            objNoeudPosition.attributes.x = String(nouvellePosition.obtenirX());
            objNoeudPosition.attributes.y = String(nouvellePosition.obtenirY());
            objNoeudParametreNouvellePosition.appendChild(objNoeudPosition);
            objNoeudCommande.appendChild(objNoeudParametreNouvellePosition);
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "DeplacerPersonnage";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }
	
	
	/**
     * Cette methode permet au joueur d'acheter un objet.
     *
     * @param Function acheterObjetDelegate : Un pointeur sur la
     *          fonction permettant au joueur d'acheter un objet
     * @param Number idObj : le id de l'objet a acheter
     */
	public function acheterObjet(acheterObjetDelegate:Function,
                                       idObj:Number)
    {
        trace("ds gestCom acheterObjet");
        // Si la partie est commencee, alors on peut continuer le code de la
        // fonction
		trace("intEtatClient : " + intEtatClient);
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("AcheterObjet", "nom") == true)
        {
        	trace("ds gestCom acheterObjet    ds if");
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"AcheterObjet", delegate:acheterObjetDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
			var objNoeudParametreId:XMLNode = objObjetXML.createElement("parametre");
			var objNoeudParametreIdTexte:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(String(idObj)));
			objNoeudParametreId.attributes.type = "id";
			
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "AcheterObjet";
			objNoeudParametreId.appendChild(objNoeudParametreIdTexte);
			objNoeudCommande.appendChild(objNoeudParametreId);
	  		objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "AcheterObjet";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }
	
	/**
     * Cette methode permet au joueur d'utiliser un objet.
     *
     * @param Function utilserObjetDelegate : Un pointeur sur la
     *          fonction permettant au joueur d'utiliser un objet
     * @param Number nomObj : le nom de l'objet a acheter
     */
	public function utiliserObjet(acheterObjetDelegate:Function,
                                       idObj:Number, bananaName:String)
    {
        trace("ds gestCom utiliserObjet: " + bananaName);
        // Si la partie est commencee, alors on peut continuer le code de la
        // fonction
		trace("intEtatClient : " + intEtatClient);
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("UtiliserObjet", "nom") == true)
        {
     	    trace("ds gestCom utiliserObjet    ds if");
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"UtiliserObjet", delegate:acheterObjetDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            var objNoeudParametreId:XMLNode = objObjetXML.createElement("parametre");
			var objNoeudParametreTossName:XMLNode = objObjetXML.createElement("parametre");
			
			var objNoeudParametreIdTexte:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(String(idObj)));
			var objNoeudParametreTossNameTexte:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(bananaName));
			
			objNoeudParametreId.attributes.type = "id";
			objNoeudParametreTossName.attributes.type = "player";
            
			// Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "UtiliserObjet";
			
			objNoeudParametreId.appendChild(objNoeudParametreIdTexte);
			objNoeudParametreTossName.appendChild(objNoeudParametreTossNameTexte);
			
			objNoeudCommande.appendChild(objNoeudParametreId);
			objNoeudCommande.appendChild(objNoeudParametreTossName);
			
	  		objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "UtiliserObjet";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
			// Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement

            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }
	
	
	
	
    /**
     * Cette methode permet au joueur de repondre a une question posee.
     *
     * @param Function repondreQuestionDelegate : Un pointeur sur la
     *          fonction permettant au joueur de repondre a une question
     * @param String reponse : La reponse a la question
     */
    public function repondreQuestion(repondreQuestionDelegate:Function,
                                     reponse:String)
    {
        // Si on est dans une table, alors on peut continuer le code de la
        // fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("RepondreQuestion", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"RepondreQuestion", delegate:repondreQuestionDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            var objNoeudParametreReponse:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreReponseText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(reponse));
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "RepondreQuestion";
            objNoeudParametreReponse.attributes.type = "Reponse";
            objNoeudParametreReponse.appendChild(objNoeudParametreReponseText);
            objNoeudCommande.appendChild(objNoeudParametreReponse);
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "RepondreQuestion";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }
	//****************************************************************************************
	/**
     * Cette methode permet de detruire une question pose.
     *
     * @param Function cancelQuestionDelegate : Un pointeur sur la fonction de retour
     */
    public function cancelQuestion(cancelQuestionDelegate:Function)
    {
        // Si une question est pose alors utiliser la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("CancelQuestion", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"CancelQuestion", delegate:cancelQuestionDelegate});
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "CancelQuestion";
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "CancelQuestion";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecte
        }
    }
	
	
	//****************************************************************************************
    
    public function definirPointageApresMinigame(definirPointageApresMinigameDelegate:Function, 
     						  points:Number)
    {
	    trace("on est dans la fct defPointMini de gestComm");
	    
        // Si on est connecte alors on peut continuer le code de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("Pointage", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"Pointage", delegate:definirPointageApresMinigameDelegate});   
	  
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            var objNoeudParametreReponse:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreReponseText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(String(points)));
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "Pointage";
            objNoeudParametreReponse.attributes.type = "Pointage";
            objNoeudParametreReponse.appendChild(objNoeudParametreReponseText);
            objNoeudCommande.appendChild(objNoeudParametreReponse);
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "Pointage";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
				
                traiterProchaineCommande();
            }
        }
        else
        {
			trace("Non, ca marche pas dans Etat pour pointage minigame");
            // TODO: Dire qu'on ne peut pas faire la commande tout de suite
        }
    }


	public function definirArgentApresMinigame(definirArgentApresMinigameDelegate:Function, 
     						  argent:Number)
    {
	    trace("on est dans la fct defArgentMini de gestComm");
	    
        // Si on est connecte alors on peut continuer le code de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("Argent", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"Argent", delegate:definirArgentApresMinigameDelegate});   
	  
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            var objNoeudParametreReponse:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreReponseText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(String(argent)));
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "Argent";
            objNoeudParametreReponse.attributes.type = "Argent";
            objNoeudParametreReponse.appendChild(objNoeudParametreReponseText);
            objNoeudCommande.appendChild(objNoeudParametreReponse);
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "Argent";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande a envoyer courant a la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'elements dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter tres prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en memoire
				
                traiterProchaineCommande();
            }
        }
        else
        {
			trace("Non, ca marche pas dans Etat pour argent minigame");
            // TODO: Dire qu'on ne peut pas faire la commande tout de suite
        }
    }
    
	
    
    
    /**********************************************
     * Grandes lignes d'une fonction de retour
     **********************************************
     *
     * 1. Arreter le timer
     * 2. Construire un objet d'evenement selon le noeud XML
     * 3. Changer l'etat si la commande est reussie
     * 4. Envoyer l'evenement de retour de fonction
     * 5. Enlever l'ecouteur du retour de fonction
     * 6. Envoyer les evenement en attente
     * 7. Enlever les ecouteurs des evenements
     * 8. Traiter la prochaine commande
     *
     **********************************************/
    /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction connexion. On va egalement envoyer les evenements
     * qui attendent d'etre traites dans le tableau d'evenements et les retirer
     * du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoye et qui contient sa reponse
     */
    private function retourConnexion(noeudCommande:XMLNode)
    {
		trace("Retour Connexion");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant connecte
            intEtatClient = Etat.CONNECTE.no;
			
			//musique
			objEvenement.listeChansons = new Array();
	
			for(var iii:Number = 0; iii < noeudCommande.childNodes.length; iii++)	
			{
				var str_temp:String = noeudCommande.childNodes[iii].firstChild.nodeValue;
				objEvenement.listeChansons.push(str_temp);
				trace("Liste chansons : " + objEvenement.listeChansons[iii]);
			}
			
			//*****************************
			// Ajouter l'attribut role dans l'objet d'evenement
            objEvenement.userRoleMaster = Number(noeudCommande.firstChild.firstChild.nodeValue);
			trace("objEvenement.userRoleMaster : " + objEvenement.userRoleMaster);
	
        }
		else
		{
			trace("erreur connexion  : " + noeudCommande.attributes.type);
		}
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }

	
    /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction deconnexion. On va egalement envoyer les evenements
     * qui attendent d'etre traites dans le tableau d'evenements et les retirer
     * du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoye et qui contient sa reponse
     */
    private function retourDeconnexion(noeudCommande:XMLNode)
    {
		trace("Retour Deconnexion");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant connecte
            intEtatClient = Etat.DECONNECTE.no;
        }
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	
    /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction obtenirListeJoueurs. On va egalement envoyer les
     * evenements qui attendent d'etre traites dans le tableau d'evenements et
     * les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous a renvoye et qui contient sa reponse
     */
    private function retourObtenirListeJoueurs(noeudCommande:XMLNode)
    {
		trace("Retour ObtenirListeJoueurs");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le resultat est la liste des joueurs, alors on peut ajouter la
        // liste des joueurs dans l'objet a retourner
        if (objEvenement.resultat == "ListeJoueurs")
        {
            // Declaration d'une reference vers la liste des noeuds joueurs
            var lstChildNodes:Array = noeudCommande.firstChild.childNodes;
            // Creer un tableau ListeNomUtilisateurs qui va contenir les
            // objets joueurs
            objEvenement.listeNomUtilisateurs = new Array();
            // Passer tous les joueurs et les ajouter dans le tableau
            for (var i:Number = 0; i < lstChildNodes.length; i++)
            {
                // Ajouter l'objet joueur dans le tableau
                objEvenement.listeNomUtilisateurs.push({nom:lstChildNodes[i].attributes.nom});
                trace("jouer "+lstChildNodes[i].attributes.nom);
            }
        }
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = Etat.LISTE_JOUEURS_OBTENUE.no;
        }
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	
    /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction obtenirListeSalles. On va egalement envoyer les
     * evenements qui attendent d'etre traites dans le tableau d'evenements et
     * les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous a renvoye et qui contient sa reponse
     */
    private function retourObtenirListeSalles(noeudCommande:XMLNode)
    {
		trace("Retour ObtenirListeSalles");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
		
		// Si le resultat est la liste des salles, alors on peut ajouter la
        // liste des salles dans l'objet a retourner
        // Si le resultat est la liste des salles, alors on peut ajouter la
        // liste des salles dans l'objet a retourner
        if (objEvenement.resultat == "ListeSalles")
        {
            // Declaration d'une reference vers la liste des noeuds salles
            var lstChildNodes:Array = noeudCommande.firstChild.childNodes;
            // Creer un tableau ListeNomSalles qui va contenir les
            // objets salle
            objEvenement.listeNomSalles = new Array();
            
            // Passer toutes les salles et les ajouter dans le tableau
            for (var i:Number = 0; i < lstChildNodes.length; i++)
            {
                // Ajouter l'objet salle dans le tableau
                objEvenement.listeNomSalles.push({nom:lstChildNodes[i].attributes.nom,
                    possedeMotDePasse:Boolean(lstChildNodes[i].attributes.protegee == "true"),
                    descriptions:lstChildNodes[i].attributes.descriptions, 
                    maxnbplayers:lstChildNodes[i].attributes.maxnbplayers,
					idRoom:lstChildNodes[i].attributes.id,
                    typeDeJeu:lstChildNodes[i].attributes.typeDeJeu,
					userCreator:lstChildNodes[i].attributes.userCreator,
					masterTime:lstChildNodes[i].attributes.masterTime});
								  
            }
			
			// Creer un tableau ListeDescrSalles qui va contenir les
            // descriptions des objets salle
            objEvenement.listeDescrSalles = new Array();
			
            // Passer toutes les salles et les ajouter dans le tableau
          
			//Creer un tableau listeNumberoJSalles qui va contenir les noumero des joueurs dans salles
			objEvenement.listeNumberoJSalles = new Array();
			objEvenement.typeDeJeuAll = new Array();
			//
			for (var i:Number = 0; i < lstChildNodes.length; i++)
            {
                // Ajouter le numero de joueurs dans salle dans le tableau
                objEvenement.listeNumberoJSalles.push({maxnbplayers:lstChildNodes[i].attributes.maxnbplayers});
                objEvenement.typeDeJeuAll.push({typeDeJeu:lstChildNodes[i].attributes.typeDeJeu});
				
			}


        }
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = Etat.LISTE_SALLES_OBTENUE.no;
        }
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	
	//*******************************************************************
	 /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction CreateRoom. On va egalement envoyer les
     * evenements qui attendent d'etre traites dans le tableau d'evenements
     * et les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoye et qui contient sa reponse
     */
    private function retourCreateRoom(noeudCommande:XMLNode)
    {
		trace("Retour CreateRoom");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le resultat est le numero de la table, alors on peut
        // ajouter le numero de la table dans l'objet a retourner
        if (objEvenement.resultat == "OK")
        {
            /*// Ajouter l'attribut noTable dans l'objet d'evenement
            //objEvenement.noTable = Number(noeudCommande.firstChild.firstChild.nodeValue);
			
			//**********************************************************
			// Declaration d'une reference vers la liste des noeuds parametres
            var lstNoeudsParametre:Array = noeudCommande.childNodes;
			
            // Passer tous les parametres et les ajouter dans l'objet evenement
            for (var i:Number = 0; i < lstNoeudsParametre.length; i++)
            {
                // Faire la reference vers le noeud courant
                var objNoeudParametre:XMLNode = lstNoeudsParametre[i];
                // Determiner le type du parametre courant et creer l'element
                // correspondant dans l'objet evenement
		
				//trace("avant le switch   "+objNoeudParametre.attributes.type);
				trace("objNoeudParametre.attributes.type : " + objNoeudParametre.attributes.type);
                switch (objNoeudParametre.attributes.type)
                {
                    case "NoTable":
		    			objEvenement.noTable = Number(objNoeudParametre.firstChild.nodeValue);
                        break;
						
                    case "NameTable":
                        objEvenement.nameTable = objNoeudParametre.firstChild.nodeValue;
						break;
						                   
                }
            }
			//**********************************************************/
			
        }
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = Etat.LISTE_SALLES_OBTENUE.no;
        }
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }

	
	//*******************************************************************
	 /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction getReport. On va egalement envoyer les
     * evenements qui attendent d'etre traites dans le tableau d'evenements
     * et les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoye et qui contient sa reponse
     */
    private function retourGetReport(noeudCommande:XMLNode)
    {
		trace("Retour getReport");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom, report:noeudCommande.attributes.report};
        // Si le resultat est le numero de la table, alors on peut
        // ajouter le numero de la table dans l'objet a retourner
        if (objEvenement.resultat == "OK")
        {
            /*// Ajouter l'attribut noTable dans l'objet d'evenement
            //objEvenement.noTable = Number(noeudCommande.firstChild.firstChild.nodeValue);
			
			//**********************************************************
			// Declaration d'une reference vers la liste des noeuds parametres
            var lstNoeudsParametre:Array = noeudCommande.childNodes;
			
            // Passer tous les parametres et les ajouter dans l'objet evenement
            for (var i:Number = 0; i < lstNoeudsParametre.length; i++)
            {
                // Faire la reference vers le noeud courant
                var objNoeudParametre:XMLNode = lstNoeudsParametre[i];
                // Determiner le type du parametre courant et creer l'element
                // correspondant dans l'objet evenement
		
				//trace("avant le switch   "+objNoeudParametre.attributes.type);
				trace("objNoeudParametre.attributes.type : " + objNoeudParametre.attributes.type);
                switch (objNoeudParametre.attributes.type)
                {
                    case "NoTable":
		    			objEvenement.noTable = Number(objNoeudParametre.firstChild.nodeValue);
                        break;
						
                    case "NameTable":
                        objEvenement.nameTable = objNoeudParametre.firstChild.nodeValue;
						break;
						                   
                }
            }
			//**********************************************************/
			
        }
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = Etat.LISTE_SALLES_OBTENUE.no;
        }
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	//*******************************************************************
	
	
    /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction entrerSalle. On va egalement envoyer les evenements
     * qui attendent d'etre traites dans le tableau d'evenements et les retirer
     * du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoye et qui contient sa reponse
     */
    private function retourEntrerSalle(noeudCommande:XMLNode)
    {
		trace("Retour EntrerSalle");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = Etat.DANS_SALLE.no;
        }
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	
    /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction quitterSalle. On va egalement envoyer les
     * evenements qui attendent d'etre traites dans le tableau d'evenements et
     * les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoye et qui contient sa reponse
     */
    private function retourQuitterSalle(noeudCommande:XMLNode)
    {
		trace("Retour QuitterSalle");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = Etat.CONNECTE.no;
        }
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	
    /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction obtenirListeJoueursSalle. On va egalement envoyer les
     * evenements qui attendent d'etre traites dans le tableau d'evenements et
     * les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous a renvoye et qui contient sa reponse
     */
    private function retourObtenirListeJoueursSalle(noeudCommande:XMLNode)
    {
		trace("Retour ObtenirListeJoueursSalle");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le resultat est la liste des joueurs de la salle, alors on peut
        // ajouter la liste des joueurs dans l'objet a retourner
        if (objEvenement.resultat == "ListeJoueursSalle")
        {
            // Declaration d'une reference vers la liste des noeuds joueurs
            var lstChildNodes:Array = noeudCommande.firstChild.childNodes;
            // Creer un tableau ListeNomUtilisateurs qui va contenir les
            // objets joueurs
            objEvenement.listeNomUtilisateurs = new Array();
            // Passer tous les joueurs et les ajouter dans le tableau
            for (var i:Number = 0; i < lstChildNodes.length; i++)
            {
                // Ajouter l'objet joueur dans le tableau
                objEvenement.listeNomUtilisateurs.push({nom:lstChildNodes[i].attributes.nom});
            }
        }
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = Etat.LISTE_JOUEURS_SALLE_OBTENUE.no;
        }
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	
    /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction obtenirListeTables. On va egalement envoyer les
     * evenements qui attendent d'etre traites dans le tableau d'evenements et
     * les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous a renvoye et qui contient sa reponse
     */
    private function retourObtenirListeTables(noeudCommande:XMLNode)
    {
		trace("Retour ObtenirListeTables GCOM");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le resultat est la liste des tables, alors on peut
        // ajouter la liste des tables dans l'objet a retourner
        if (objEvenement.resultat == "ListeTables")
        {
            // Declaration d'une reference vers la liste des noeuds tables
            var lstTablesChildNodes:Array = noeudCommande.firstChild.childNodes;
            // Creer un tableau ListeTables qui va contenir les
            // objets tables et leurs joueurs
            objEvenement.listeTables = new Array();
            // Passer toutes les tables et les ajouter dans le tableau
            for (var i:Number = 0; i < lstTablesChildNodes.length; i++)
            {
                // Declaration d'une reference vers la liste des noeuds joueurs
                var lstJoueursChildNodes:Array = lstTablesChildNodes[i].childNodes;
                // Creer un objet qui va contenir les information sur la table
                var objTable:Object = new Object();
                // Definir les proprietes de la table et creer la liste des
                // joueurs
                objTable.no = lstTablesChildNodes[i].attributes.no;
                objTable.temps = lstTablesChildNodes[i].attributes.temps;
				objTable.tablName = lstTablesChildNodes[i].attributes.tablName;
                objTable.listeJoueurs = new Array();
                // Passer les joueurs de la table courante et les ajouter
                // dans l'objet table courant
                for (var j:Number = 0; j < lstJoueursChildNodes.length; j++)
                {
                    // Ajouter le joueur courant dans la liste des joueurs de
                    // la table
                    objTable.listeJoueurs.push({nom:lstJoueursChildNodes[j].attributes.nom});
					//trace("GCom : " + nom:lstJoueursChildNodes[j].attributes.nom);
                }
                // Ajouter l'objet table dans le tableau
                objEvenement.listeTables.push(objTable);
            }
        }
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = Etat.LISTE_TABLES_OBTENUE.no;
        }
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }

    
    /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous a renvoye et qui contient sa reponse
     */
    private function retourDefinirPointageApresMinigame(noeudCommande:XMLNode)
    {
		trace("dans retourDefinirPointageApresMinigame de gesComm");

        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le resultat est le pointage, alors on peut
        // ajouter ce pointage dans l'objet a retourner
        if (objEvenement.resultat == "Pointage")
        {
			// Declaration d'une reference vers la liste des noeuds
			var lstNoeudsParametre:Array = noeudCommande.childNodes;
			
			trace(lstNoeudsParametre);
			
			//Le seul et unique noeud est le pointage
			var objNoeudParametre:XMLNode = lstNoeudsParametre[0];
			
			objEvenement.pointage = objNoeudParametre.firstChild.nodeValue;
        }
        
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
    
    
	
	
	/**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous a renvoye et qui contient sa reponse
     */
    private function retourDefinirArgentApresMinigame(noeudCommande:XMLNode)
    {
		trace("dans retourDefinirArgentApresMinigame de gesComm");

        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le resultat est le pointage, alors on peut
        // ajouter ce pointage dans l'objet a retourner
        if (objEvenement.resultat == "Argent")
        {
			// Declaration d'une reference vers la liste des noeuds
			var lstNoeudsParametre:Array = noeudCommande.childNodes;
			
			trace(lstNoeudsParametre);
			
			//Le seul et unique noeud est le pointage
			var objNoeudParametre:XMLNode = lstNoeudsParametre[0];
			
			objEvenement.argent = objNoeudParametre.firstChild.nodeValue;
        }
        
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }

    
    /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction creerTable. On va egalement envoyer les
     * evenements qui attendent d'etre traites dans le tableau d'evenements
     * et les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoye et qui contient sa reponse
     */
    private function retourCreerTable(noeudCommande:XMLNode)
    {
		trace("Retour CreerTable");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le resultat est le numero de la table, alors on peut
        // ajouter le numero de la table dans l'objet a retourner
        if (objEvenement.resultat == "NoTable")
        {
            // Ajouter l'attribut noTable dans l'objet d'evenement
            //objEvenement.noTable = Number(noeudCommande.firstChild.firstChild.nodeValue);
			
			//**********************************************************
			// Declaration d'une reference vers la liste des noeuds parametres
            var lstNoeudsParametre:Array = noeudCommande.childNodes;
			
            // Passer tous les parametres et les ajouter dans l'objet evenement
            for (var i:Number = 0; i < lstNoeudsParametre.length; i++)
            {
                // Faire la reference vers le noeud courant
                var objNoeudParametre:XMLNode = lstNoeudsParametre[i];
                // Determiner le type du parametre courant et creer l'element
                // correspondant dans l'objet evenement
		
				//trace("avant le switch   "+objNoeudParametre.attributes.type);
				trace("objNoeudParametre.attributes.type : " + objNoeudParametre.attributes.type);
                switch (objNoeudParametre.attributes.type)
                {
                    case "NoTable":
		    			objEvenement.noTable = Number(objNoeudParametre.firstChild.nodeValue);
                        break;
						
                    case "NameTable":
                        objEvenement.nameTable = objNoeudParametre.firstChild.nodeValue;
						break;
						                   
                }
            }
			//*********************************************************
			
        }
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = Etat.DANS_TABLE.no;
        }
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }

	
    /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction entrerTable. On va egalement envoyer les
     * evenements qui attendent d'etre traites dans le tableau d'evenements
     * et les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoye et qui contient sa reponse
     */
    private function retourEntrerTable(noeudCommande:XMLNode)
    {
		trace("Retour EntrerTable");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le resultat est la liste des personnages choisis par les joueurs
        // de la table, alors on peut ajouter la liste des personnages dans
        // l'objet a retourner
        if (objEvenement.resultat == "ListePersonnageJoueurs")
        {
            // Declaration d'une reference vers la liste des noeuds personnage
            var lstChildNodes:Array = noeudCommande.firstChild.childNodes;
            // Creer un tableau ListePersonnageJoueurs qui va contenir les
            // objets personnages
            objEvenement.listePersonnageJoueurs = new Array();
            // Passer tous les personnages et les ajouter dans le tableau
            for (var i:Number = 0; i < lstChildNodes.length; i++)
            {
                // Ajouter l'objet joueur dans le tableau
                objEvenement.listePersonnageJoueurs.push({nom:lstChildNodes[i].attributes.nom,
                                                          idPersonnage:lstChildNodes[i].attributes.idPersonnage,
														  userRoles:lstChildNodes[i].attributes.role});
            }
        }
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = Etat.DANS_TABLE.no;
        }
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }

    /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction quitterTable. On va egalement envoyer les
     * evenements qui attendent d'etre traites dans le tableau d'evenements et
     * les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoye et qui contient sa reponse
     */
    private function retourQuitterTable(noeudCommande:XMLNode)
    {
		trace("Retour QuitterTable");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = Etat.LISTE_TABLES_OBTENUE.no;
        }
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	
    /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction demarrerPartie. On va egalement envoyer les
     * evenements qui attendent d'etre traites dans le tableau d'evenements
     * et les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoye et qui contient sa reponse
     */
    private function retourDemarrerPartie(noeudCommande:XMLNode)
    {
		trace("Retour DemarrerPartie GCom");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = Etat.ATTENTE_DEBUT_PARTIE.no;
        }
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
    
    
    /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction demarrerMaintenant. On va egalement envoyer les
     * evenements qui attendent d'etre traites dans le tableau d'evenements
     * et les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoye et qui contient sa reponse
     */
    private function retourDemarrerMaintenant(noeudCommande:XMLNode)
    {
		trace("retourDemarrerMaintenant");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};

        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
	}

    /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction deplacerPersonnage. On va egalement envoyer les
     * evenements qui attendent d'etre traites dans le tableau d'evenements
     * et les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoye et qui contient sa reponse
     */
    private function retourDeplacerPersonnage(noeudCommande:XMLNode)
    {
		trace("Retour DeplacerPersonnage");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le resultat est la question a poser, alors on peut ajouter la
        // question dans l'objet a retourner
        if (objEvenement.resultat == "Question")
        {
            // Declaration d'une reference vers le noeud de question
            var noeudQuestion:XMLNode = noeudCommande.firstChild.firstChild;
            // Creer la question dans l'objet d'evenement
            objEvenement.question = new Object();
            // Ajouter les parametres de la question dans l'objet d'evenement
            objEvenement.question.id = Number(noeudQuestion.attributes.id);
            objEvenement.question.type = noeudQuestion.attributes.type;
            objEvenement.question.url = noeudQuestion.attributes.url;
        }
		
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse" && noeudCommande.attributes.nom != "Banane")//FRANCOIS
        {
            // On est maintenant a l'autre etat
            intEtatClient = Etat.ATTENTE_REPONSE_QUESTION.no;
        }

        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	

	private function retourAcheterObjet(noeudCommande:XMLNode)
    {
		trace("ds gesComm Retour AcheterObjet");
		
		//trace(noeudCommande);
		//trace(noeudCommande.childNodes);
		trace(noeudCommande.firstChild);

        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le resultat est la question a poser, alors on peut ajouter la
        // question dans l'objet a retourner
     	
		//trace("avant gest comm : " + _level0.loader.contentHolder.planche.obtenirPerso().obtenirMagasin());

		if (objEvenement.resultat == "Ok")
        {
            var noeudArgent:XMLNode = noeudCommande.firstChild;
            // Creer la question dans l'objet d'evenement
            objEvenement.argent = new Object();
            // Ajouter les parametres de la question dans l'objet d'evenement
            objEvenement.argent.id = Number(noeudArgent.attributes.id);
            objEvenement.argent.type = noeudArgent.attributes.type;
			

			var noeudMagasin:XMLNode;
			noeudMagasin = noeudCommande.firstChild.nextSibling;
			
			//trace(noeudMagasin.attributes.id);
			//trace(objEvenement.argent.id);
			//_level0.loader.contentHolder.planche.obtenirPerso().(Number(noeudMagasin.attributes.id));
			//_level0.loader.contentHolder.planche.obtenirPerso().setVieuxID(objEvenement.argent.id);

        }

        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }

	
    /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction utiliserObjet. On va egalement envoyer les
     * evenements qui attendent d'etre traites dans le tableau d'evenements
     * et les retirer du tableau.
	 *
	 * pour chaque objet a utiliser envoye par le serveur, on va envoyer les 
	 * informations appropriees pour le gestionnaire d'evenements
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoye et qui contient sa reponse
     */
	private function retourUtiliserObjet(noeudCommande:XMLNode)
    {
		trace("ds gesComm Retour UtiliserObjet");

        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le resultat est la question a poser, alors on peut ajouter la
        // question dans l'objet a retourner
        if (objEvenement.resultat == "RetourUtiliserObjet")
        {
			switch(noeudCommande.attributes.type)
			{
				case "Livre":
					trace("noeudCommande.childNodes : " + noeudCommande.childNodes);
					trace("noeudCommande.firstChild.childNodes : " + noeudCommande.firstChild.childNodes);
		
					objEvenement.objetUtilise = new Object();
					// Ajouter les parametres de l'objet dans l'objet d'evenement
					objEvenement.objetUtilise.typeObjet = noeudCommande.attributes.type;
					objEvenement.objetUtilise.mauvaiseReponse = noeudCommande.firstChild.childNodes;
					
					trace(objEvenement.objetUtilise.type);
					trace(objEvenement.objetUtilise.mauvaiseReponse);
				break;
				
				case "Boule":
					objEvenement.objetUtilise = new Object();
					// Ajouter les parametres de l'objet dans l'objet d'evenement
					objEvenement.objetUtilise.typeObjet = noeudCommande.attributes.type;
					objEvenement.objetUtilise.url = noeudCommande.firstChild.firstChild.attributes.url;
					objEvenement.objetUtilise.type = noeudCommande.firstChild.firstChild.attributes.type;
					
					trace(objEvenement.objetUtilise.url);
					trace(objEvenement.objetUtilise.type);
				break;
				
				case "OK":
					objEvenement.objetUtilise = new Object();
					// Ajouter les parametres de l'objet dans l'objet d'evenement
					objEvenement.objetUtilise.typeObjet = noeudCommande.attributes.type;
					
					trace(objEvenement.objetUtilise.typeObjet);
				break;
				
				default:
					trace("pas objet valide ds retourUtiliserObj ds gestComm");
				break;
			}
        }
		
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	
	
    /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction repondreQuestion. On va egalement envoyer les
     * evenements qui attendent d'etre traites dans le tableau d'evenements
     * et les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoye et qui contient sa reponse
     */
    private function retourRepondreQuestion(noeudCommande:XMLNode)
    {
		trace("Retour RepondreQuestion ds gestComm");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le resultat est Deplacement choisis par les joueurs
        // de la table, alors on peut ajouter les parametres dans
        // l'objet a retourner
        if (objEvenement.resultat == "Deplacement")
        {
			trace(objEvenement);
			trace("Deplacement ds gestComm");
            // Declaration d'une reference vers la liste des noeuds parametres
            var lstNoeudsParametre:Array = noeudCommande.childNodes;
			
            // Passer tous les parametres et les ajouter dans l'objet evenement
            for (var i:Number = 0; i < lstNoeudsParametre.length; i++)
            {
                // Faire la reference vers le noeud courant
                var objNoeudParametre:XMLNode = lstNoeudsParametre[i];
                // Determiner le type du parametre courant et creer l'element
                // correspondant dans l'objet evenement
		
				//trace("avant le switch   "+objNoeudParametre.attributes.type);
				trace("objNoeudParametre.attributes.type : " + objNoeudParametre.attributes.type);
                switch (objNoeudParametre.attributes.type)
                {
                    case "DeplacementAccepte":
		    			if(objNoeudParametre.firstChild.nodeValue == "false")
		    			{
			     			trace("nodeValue est false");
                        	objEvenement.deplacementAccepte = Boolean(false);
		    			}
		    			else
		    			{
			    			trace("nodeValue n'est pas false");
			    			objEvenement.deplacementAccepte = Boolean(true);
		    			}
						trace("DeplacementAccepte ds gestComm  : "+objNoeudParametre.firstChild.nodeValue);
                        break;
						
                    case "Explication":
                        objEvenement.explication = objNoeudParametre.firstChild.nodeValue;
						trace("Explication ds gestComm");
                        break;
						
                    case "ObjetRamasse":
						trace("ObjetRamasse ds gestComm");
                        objEvenement.objetRamasse = null;
                        // Si le noeud courant a un enfant, alors on garde
                        // ses informations dans l'objet ramasse
                        if (objNoeudParametre.hasChildNodes() == true)
                        {
                            objEvenement.objetRamasse = new Object();
                            objEvenement.objetRamasse.id = Number(objNoeudParametre.firstChild.attributes.id);
							objEvenement.objetRamasse.type = objNoeudParametre.firstChild.attributes.type;
							objEvenement.collision = "objet";
							//var o:ObjetSurCase = new ObjetSurCase();
							//o.definirNom(objNoeudParametre.firstChild.attributes.type);
							_level0.loader.contentHolder.planche.obtenirPerso().ajouterObjet(objEvenement.objetRamasse.id, objEvenement.objetRamasse.type);
							
                            
                        }
                        break;
						
                    case "ObjetSubi":
                        objEvenement.objetSubi = null;
                        // Si le noeud courant a un enfant, alors on garde
                        // ses informations dans l'objet subi
                        if (objNoeudParametre.hasChildNodes() == true)
                        {
                            objEvenement.objetSubi = new Object();
                            objEvenement.objetSubi.id = Number(objNoeudParametre.firstChild.attributes.id);
                            objEvenement.objetSubi.type = objNoeudParametre.firstChild.attributes.type;
                        }
						trace("ObjetSubi ds gestComm");
                        break;
						
                    case "NouvellePosition":
                        objEvenement.nouvellePosition = new Object();
                        objEvenement.nouvellePosition.x = Number(objNoeudParametre.firstChild.attributes.x);
                        objEvenement.nouvellePosition.y = Number(objNoeudParametre.firstChild.attributes.y);
						trace("NouvellePosition ds gestComm   "+objEvenement.nouvellePosition.x);
                        break;
						
                    case "Pointage":
                        objEvenement.pointage = Number(objNoeudParametre.firstChild.nodeValue);
						trace("Pointage ds gestComm   "+objEvenement.pointage);
						break;
						
					case "Bonus":
                        objEvenement.bonus = Number(objNoeudParametre.firstChild.nodeValue);
						trace("Bonus ds gestComm   " + objEvenement.bonus);
						break;
		   
		    		case "Argent":
                        objEvenement.argent = Number(objNoeudParametre.firstChild.nodeValue);
						trace("Argent ds gestComm   "+objEvenement.argent);
						break;
		   
		            case "MoveVisibility":
                        objEvenement.moveVisibility = Number(objNoeudParametre.firstChild.nodeValue);
						trace("moveVisibility ds gestComm   " + objEvenement.moveVisibility);
						break;
		   
		   			case "Collision":  
						if (objNoeudParametre.hasChildNodes() == true)
                        {
							if(objNoeudParametre.firstChild.nodeValue == "magasin")
							{
								var objEvenementMagasin:Object;
								objEvenementMagasin = noeudCommande.firstChild.nextSibling;
								//trace(objEvenementMagasin);
								var lstObjMagasin:Array = objEvenementMagasin.childNodes;
								trace(lstObjMagasin);
								
								trace("************************");
								trace("objets du magasin");
								
								for(var j:Number = 0; j<lstObjMagasin.length; j++)
								{
									var objNoeudObjMagasin:XMLNode = lstObjMagasin[j];
									
									objEvenement["objet"+j] = new Object();
									objEvenement["objet"+j].cout = objNoeudObjMagasin.attributes.cout;
									objEvenement["objet"+j].id = objNoeudObjMagasin.attributes.id;
									objEvenement["objet"+j].type = objNoeudObjMagasin.attributes.type;
									trace("objet"+j+".cout :" + objEvenement["objet"+j].cout);
									trace("objet"+j+".id :" + objEvenement["objet"+j].id);
									trace("objet"+j+".type :" + objEvenement["objet"+j].type);
								}
								trace("************************");
								
								_level0.loader.contentHolder.planche.obtenirPerso().definirMagasin(lstObjMagasin);
								trace("gest comm : " + _level0.loader.contentHolder.planche.obtenirPerso().obtenirMagasin());

							}
							/*else if(objNoeudParametre.firstChild.nodeValue == "piece")
							{
														
								var o:ObjetSurCase = new ObjetSurCase();
								o.definirNom("pieceFixe");
								_level0.loader.contentHolder.planche.obtenirPerso().ajouterObjet(o, 999);	
										
							}*/

                        	objEvenement.collision =  String(objNoeudParametre.firstChild.nodeValue);
							//objEvenement.collision = "minigame";
							trace("collision ds gestComm "+objEvenement.collision+"   "+objNoeudParametre.firstChild.nodeValue);
						}
                        break;
                }
            }
        }
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = Etat.PARTIE_DEMARREE.no;
        }
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	
	//*********************************************************
	 /**
     * Cette methode permet de decortiquer le noeud de commande passe en
     * parametres et de lancer un evenement a ceux qui s'etaient ajoute comme
     * ecouteur a la fonction cancelQuestion. On va egalement envoyer les
     * evenements qui attendent d'etre traites dans le tableau d'evenements et
     * les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoye et qui contient sa reponse
     */
    private function returnCancelQuestion(noeudCommande:XMLNode)
    {
		trace("Retour CancelQuestion");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = Etat.PARTIE_DEMARREE.no;
        }
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	//*********************************************************
	
	function definirIntEtatClient(intEtat:Number)
	{
		this.intEtatClient = intEtat;
	}
	
	public function obtenirEtatClient():Number
	{
		return intEtatClient;
	}
}