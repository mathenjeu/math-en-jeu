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
// TODO : Un moment donné il va falloir intégrer le reply à un événement
//        comme pour le ping
class GestionnaireCommunication
{
    // Déclaration d'une fonction qui va prendre en paramètre un écouteur
    // et qui va le garder en mémoire et l'appeler lorsque nécessaire
    public var addEventListener:Function;
    // Déclaration d'une fonction qui va permettre d'enlever une fonction
    // qui écoute l'événement
    public var removeEventListener:Function;
    // Déclaration d'une fonction qui va permettre d'envoyer un événement
    // aux fonctions qui ont été ajoutées par addEventListener
    private var dispatchEvent:Function;
    // Déclaration d'un objet DispatchingXMLSocket qui va servir à communiquer
    // avec le serveur de jeu
    private var objSocketClient:DispatchingXMLSocket;
    // Déclaration d'une contante gardant le délai que le timer doit attendre
    // pour déterminer si une commande envoyée s'est perdue en chemin
    private var TEMPS_TIMER:Number = 8000;
    // Déclaration d'un delegate qui va pointer vers la fonction qui gère
    // l'événement de temps écoulé
    private var objTimerDelegate:Function;
    // Déclaration d'un Timer qui va servir à vérifier si une commande
    // a bien été envoyée au serveur (il faut que le serveur ait renvoyé
    // un accusé réception de notre commande)
    private var objTimerEnvoiCommandes:Timer;
    // Déclaration d'un delegate qui va pointer vers la fonction qui gère
    // l'événement de connexion physique
    private var objConnexionPhysiqueDelegate:Function;
    // Déclaration d'un delegate qui va pointer vers la fonction qui gère
    // l'événement de déconnexion physique
    private var objDeconnexionPhysiqueDelegate:Function;
    // Déclaration d'une variable qui va permettre de garder l'état courant du
    // client (les valeurs sont définies dans la classe Etat)
    private var intEtatClient:Number;
    // Déclaration d'un tableau où chaque élément est un objet ayant les
    // champs no pour le numéro de la commande, nom pour le nom de la commande,
    // objectXML pour garder la commande à envoyer en XML et listeDelegate pour
    // la liste des fonctions à appeler lors de l'arrivée des événements.
    // listeDelegate est un tableau ExtendedArray et non une liste associative.
    private var lstCommandesAEnvoyer:ExtendedArray;
    // Déclaration d'une liste dont les indices sont le nom de la commande ou
    // événement à appeler et le contenu est un Delegate. Cette liste contient
    // un élément spécial qui est identifié par la chaîne "tableau" et qui
    // a comme valeur un ExtendedArray contenant tous les noms clés de cette
    // liste
    private var lstDelegateEvenements:Object;
    // Déclaration d'un tableau où le contenu est un objet XMLNode
    private var lstEvenementsRecus:ExtendedArray;
    // Déclaration d'un tableau où le contenu est un objet XMLNode
    // Cette liste contient les événements à vérifier et à envoyer
    private var lstEvenementsAVerifier:ExtendedArray;
    // Déclaration d'une contante gardant le maximum possible pour le
    // compteur de commandes du client
    private var MAX_COMPTEUR:Number = 100;
    // Déclaration d'une variable qui va servir de compteur pour envoyer des
    // commandes serveur de jeu (sa valeur maximale est 100, après 100 on
    // recommence à 0)
    private var intCompteurCommande:Number;
    // Déclaration d'une variable qui va contenir le dernier numéro envoyé
    // par le serveur. Cette variable va servir à traiter les événements
    // reçus dans l'ordre. Ce compteur va également jusqu'à 100
    private var intDerniereCommandeServeur:Number;
    // Déclaration d'une variable qui va contenir le nombre d'erreurs de
    // suite étant survenues. Après 3 erreurs, un message est envoyé au
    // client
    private var intCompteurErreurs:Number;
    // Déclaration d'une référence vers un objet qui garde en mémoire le
    // numéro de la commande par le champ no, le nom de la commande par
    // le champ nom, l'objet XML de la commande par le champ objectXML
    // et la liste des Delegate pour cette commande. C'est la commande
    // qui est présentement en train de se faire traiter (il ne peut y
    // en avoir qu'une seule). Si cette référence est à null, c'est
    // qu'aucune commande n'est présentement en traitement
    private var objCommandeEnTraitement:Object;
	
	
    /**
     * Constructeur de la classe GestionnaireCommunication qui prend le
     * gestionnaire de commandes en paramètre.
     */
    function GestionnaireCommunication(connexionPhysiqueDelegate:Function, deconnexionPhysiqueDelegate:Function, url_serveur:String, port:Number)
    {
        // Initialiser le dispatcher d'événements (ajoute les fonctions
        // addEventListener, removeEventListener et dispatchEvent)
        EventDispatcher.initialize(this);
        // Garder en mémoire le delegate de la fonction de connexion physique
        objConnexionPhysiqueDelegate = connexionPhysiqueDelegate;
        // Garder en mémoire le delegate de la fonction de déconnexion physique
        objDeconnexionPhysiqueDelegate = deconnexionPhysiqueDelegate;
        // Au début le joueur n'est pas connecté
        intEtatClient = Etat.NON_CONNECTE.no;
        // Créer un nouveau tableau d'objets
        lstCommandesAEnvoyer = new ExtendedArray();
        // Créer une nouvelle liste d'objets
        lstDelegateEvenements = new Object();
        // Créer l'élément tableau dans la liste qui va contenir les noms
        // clés dans un tableau
        lstDelegateEvenements.tableau = new ExtendedArray();
        // Créer un nouveau tableau d'objets
        lstEvenementsRecus = new ExtendedArray();
        // Créer un nouveau tableau d'objets
        lstEvenementsAVerifier = new ExtendedArray();
        // Initialiser le compteur à 0
        intCompteurCommande = 0;
        // Initialiser le compteur à -1
        intDerniereCommandeServeur = -1;
        // Au départ, aucune commande n'est en traitement
        objCommandeEnTraitement = null;
        // Le temps pour recevoir un accusé réception sera de TEMPS_TIMER/1000 secondes
        objTimerEnvoiCommandes = new Timer(TEMPS_TIMER);
        // Garder en mémoire le delegate pour l'événement de temps écoulé
        objTimerDelegate = Delegate.create(this, objTimerEnvoiCommandes_tempsEcoule);
        // Initialiser le compteur d'erreurs à 0
        intCompteurErreurs = 0;
        // Créer un nouveauDispatching XMLSocket qui va permettre de
        // communiquer avec le serveur en java
        objSocketClient = new DispatchingXMLSocket();
        // Ajouter l'écouteur de l'événement connect
        objSocketClient.addEventListener("connect", Delegate.create(this, objSocketClient_onConnect));
        // Ajouter l'écouteur de l'événement xml
        objSocketClient.addEventListener("xml", Delegate.create(this, objSocketClient_onXML));
        // Ajouter l'écouteur de l'événement close
        objSocketClient.addEventListener("close", Delegate.create(this, objSocketClient_onClose));
        // Essayer de se connecter au serveur de jeu
		objSocketClient.connect(url_serveur, port);
    }
	
    /**
     * Cet événement est appelé lorsque la connexion a réussi ou échoué.
     *
     * @param Object objetEvenement : L'objet contenant les propriétés de
     *                                l'événement
     */
    private function objSocketClient_onConnect(objetEvenement:Object)
    {
        // Si la connexion a réussi, alors on change l'état à DECONNECTE,
        // sinon on le remet à NON_CONNECTE (normalement il devrait déjà être
        // à cette valeur)
		//trace("ds gestCom résultat de la connection :  "+objetEvenement.succes);
        if (objetEvenement.succes == true)
        {
            intEtatClient = Etat.DECONNECTE.no;
            // Ajouter l'écouteur de l'événement du timer
            objTimerEnvoiCommandes.addEventListener("timeout", objTimerDelegate);
        }
        else
        {
            intEtatClient = Etat.NON_CONNECTE.no;
        }
        // Ajouter l'écouteur de l'événement de connexion physique
        this.addEventListener("ConnexionPhysique", objConnexionPhysiqueDelegate);
        // Lancer l'événement permettant d'indiquer à Flash si la connexion a
        // réussie ou non
        dispatchEvent({type:"ConnexionPhysique", target:this, resultat:objetEvenement.succes});
        // Enlever l'écouteur de l'événement de connexion physique
        this.removeEventListener("ConnexionPhysique", objConnexionPhysiqueDelegate);
    }
	
    /**
     * Cet événement est appelé lorsque la connexion a été coupée.
     */
    private function objSocketClient_onClose(objetEvenement:Object)
    {
        // Arreter le timer s'il est actif
        if (objTimerEnvoiCommandes.estActif() == true)
        {
            objTimerEnvoiCommandes.arreter();
        }
        // Enlever l'écouteur pour le timer
        objTimerEnvoiCommandes.removeEventListener("timeout", objTimerDelegate);
        // Le client n'est plus connecté physiquement au serveur de jeu
        intEtatClient = Etat.NON_CONNECTE.no;
        // Réinitialiser le compteur d'erreurs à 0
        intCompteurErreurs = 0;
        // S'il y a une commande en traitement, alors on doit arrêter
        // l'écouteur pour le retour de la commande
        if (objCommandeEnTraitement != null)
        {
            // Enlever l'écouteur pour le retour de la commande qui est en
            // traitement
            this.removeEventListener(objCommandeEnTraitement.listeDelegate[0].nom, objCommandeEnTraitement.listeDelegate[0].delegate);
            // Dire qu'il n'y a plus de commandes en traitement
            objCommandeEnTraitement = null;
        }
        // Vider le tableau contenant les événements en attente d'être envoyés
        lstEvenementsRecus.clear();
        // Vider le tableau contenant les événements à vérifier et à envoyer
        lstEvenementsAVerifier.clear();
        // Vider le tableau des commandes à traiter (on ne peut pas traiter
        // d'autres commandes)
        lstCommandesAEnvoyer.clear();
        // Passer la liste des delegate et enlever tous les handlers
        for (var i:Number = lstDelegateEvenements.tableau.length - 1; i >= 0; i--)
        {
            // Enlever l'écouteur pour l'événement courant
            this.removeEventListener(lstDelegateEvenements.tableau[i], lstDelegateEvenements[lstDelegateEvenements.tableau[i]]);
            // Supprimer l'objet delegate courant
            delete lstDelegateEvenements[lstDelegateEvenements.tableau[i]];
            lstDelegateEvenements.tableau.remove(lstDelegateEvenements.tableau[i]);
        }
        // Ajouter l'écouteur de l'événement de déconnexion physique
        this.addEventListener("DeconnexionPhysique", objDeconnexionPhysiqueDelegate);
        // Lancer l'événement permettant d'indiquer à Flash qu'il y a eu une
        // déconnexion physique
        dispatchEvent({type:"DeconnexionPhysique", target:this});
        // Enlever l'écouteur de l'événement de déconnexion physique
        this.removeEventListener("DeconnexionPhysique", objDeconnexionPhysiqueDelegate);
    }
	
    /**
     * Cet événement est appelé lorsqu'un message XML est reçu par le client.
     * Un document XML est passé en paramètres. Si une commande reçue n'est
     * pas connue par le client, alors celle-ci est ignorée.
     *
     * @param Object objetEvenement : L'objet contenant les propriétés de
     *                                l'événement
     */
    private function objSocketClient_onXML(objetEvenement:Object)
    {
        // Déclaration d'une référence vers le noeud de commande reçu
        var objNoeudCommande:XMLNode = objetEvenement.donnees.firstChild;
		
        // Si le noeud de commande est null alors on ne fait rien
        if (objNoeudCommande != null)
        {
            // Si le nom du noeud de commande est ping, alors il faut renvoyer
            // immédiatement le même objet XML au serveur
            if (objNoeudCommande.nodeName == "ping")
            {
                objSocketClient.send(objetEvenement.donnees);
            }
            // Si le message reçu est une commande, alors on va déterminer
            // laquelle est-ce et on va appeler la fonction adéquate avec les
            // bons paramètres
            else if (objNoeudCommande.nodeName == "commande")
            {
                //TODO: À améliorer
                // Mettre à jour le numéro de la dernière commande envoyée par
                // le serveur seulement si ce numéro est le suivant qui devrait
                // arriver
                if ((intDerniereCommandeServeur + 1) % MAX_COMPTEUR == objNoeudCommande.attributes.no)
                {
                    intDerniereCommandeServeur = objNoeudCommande.attributes.no;
                }
                // Si la commande reçue est un événement, alors on va vérifier
                // si on accepte l'événement ou non et si on doit le garder en
                // attente ou le traiter immédiatement
                if (objNoeudCommande.attributes.type == "Evenement")
                {
					trace("C'est un évenement: " + objNoeudCommande.attributes.nom);
					
                    // Appeler une fonction qui va traiter l'événement
                    // (l'envoyer tout de suite, l'ajouter dans une liste en
                    // attendant ou l'ignorer)
                    traiterEvenement(objNoeudCommande);
                }
                // Sinon s'il y a une commande en traitement (s'il n'y en n'a
                // pas on fait rien), si la commande reçue contient bien un
                // numéro de client (notre numéro que le serveur doit avoir
                // renvoyé) et si ce numéro égale le numéro de notre commande
                // courante, alors on va savoir que c'est vraiment la réponse
                // à la commande courante qu'on a envoyée au serveur
                else if (objCommandeEnTraitement != null &&
                         objNoeudCommande.attributes.noClient != undefined &&
                         objNoeudCommande.attributes.noClient == objCommandeEnTraitement.no &&
                         objNoeudCommande.attributes.nom != "ParametrePasBon" &&
                         objNoeudCommande.attributes.nom != "CommandeNonReconnue")
                {
                    // Arrêter le timer puisque la commande s'est bien rendue
                    // au serveur
                    objTimerEnvoiCommandes.arreter();
                    // Réinitialiser le compteur d'erreurs à 0
                    intCompteurErreurs = 0;
                    // Aiguiller la réponse du serveur vers la bonne fonction
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
						default:
							trace("reponse du serveur: "+objCommandeEnTraitement.nom);
						break;
                        // TODO: Ajouter d'autres cas
                    }
                }
				else
					trace("find me in gesComm sucker! : "+objNoeudCommande.attributes.nom);
            }
        }
    }
	
    /**
     * Cette fonction permet d'obtenir le prochain numéro de commande à
     * envoyer au serveur. Ce numéro est de 0 au maximum. Si le numéro devient
     * plus grand que le maximum, alors le numéro de commande redevient 0.
     *
     * @return Number : Le numéro de la commande
     */
    private function obtenirNumeroCommande():Number
    {
        // Déclaration d'une variable qui va contenir le numéro de la commande
        // à retourner
        var intNumeroCommande = intCompteurCommande;
        // Incrémenter le compteur de commandes
        intCompteurCommande++;
        // Si le compteur de commandes est maintenant plus grand que la plus
        // grande valeur possible, alors on réinitialise le compteur à 0
        if (intCompteurCommande > MAX_COMPTEUR)
        {
            intCompteurCommande = 0;
        }
        return intNumeroCommande;
    }
	
	
    /**
     * Cette fonction permet de traiter l'événement passé en paramètres et
     * déterminer quoi faire avec lui, soit on le lance tout de suite, soit
     * on le met dans une liste d'attente qu'on va traiter plus tard, soit
     * on l'ignore. Cette fonction s'assure de traiter les événements dans
     * leur ordre respectif.
     *
     * @param XMLNode noeudCommande : Le noeud XML contenant les
     *                                informations sur l'événement
     */
    private function traiterEvenement(noeudCommande:XMLNode)
    {
        //TODO: À continuer
        // Si le numéro de la commande est celui de la dernière commande reçue
        //if (noeudCommande.attributes.no <= intDerniereCommandeServeur)
        //{
		
            // Appeler la fonction qui va traiter immédiatement l'événement
            verifierEtEnvoyerEvenement(noeudCommande);
        //}
        // Sinon on doit ajouter l'événement dans une liste qui sera vidée lors
        // d'un retour de fonction
        //else
        //{
        //    lstEvenementsAVerifier.push(noeudCommande);
        //}
    }
	
	
    /**
     * Cette fonction permet de vérifier que l'événement passé en paramètre
     * est acceptable et peut être envoyé à flash. S'il est acceptable, mais
     * dans certaines conditions, alors on l'ajoute dans les événements reçus
     * et on les traitera plus tard.
     *
     * @param XMLNode noeudCommande : Le noeud XML contenant les informations
     *                                sur l'événement à vérifier
     */
    private function verifierEtEnvoyerEvenement(noeudCommande:XMLNode)
    {
        // S'il n'y a pas de commande en traitement et que l'événement
        // est acceptable, alors on envoie l'événement. Si il y a
        // une commande en traitement et que l'événement est acceptable
        // pour cette commande, alors on va l'envoyer
        if ((objCommandeEnTraitement == null &&
             ExtendedArray.fromArray(Etat.obtenirEvenementsAcceptablesEtat(intEtatClient)).contains(noeudCommande.attributes.nom) == true) ||
            (objCommandeEnTraitement != null &&
             ExtendedArray.fromArray(Etat.obtenirEvenementsAcceptablesCommande(intEtatClient, objCommandeEnTraitement.nom)).contains(noeudCommande.attributes.nom) == true))
        {
            // Envoyer l'événement aux écouteurs de cet événement		
            envoyerEvenement(noeudCommande);
        }
        // Sinon, s'il y a une commande en traitement, on vérifie les
        // cas pouvant causer des problèmes de synchronisation (un
        // événement arrive avant le retour de la requête) et on
        // ajoute l'événement dans une liste d'événements en attente
        else if (objCommandeEnTraitement != null &&
                 (ExtendedArray.fromArray(Etat.obtenirEvenementsAcceptablesAvant(intEtatClient, objCommandeEnTraitement.nom)).contains(noeudCommande.attributes.nom) == true ||
                  ExtendedArray.fromArray(Etat.obtenirEvenementsAcceptablesApres(intEtatClient, objCommandeEnTraitement.nom)).contains(noeudCommande.attributes.nom) == true))
        {
            // Ajouter l'événement à la fin de la liste	
            lstEvenementsRecus.push(noeudCommande);
        }
    }
	
    /**
     * Cette fonction permet de traiter la prochaine commande en la chargeant
     * en mémoire. Si la prochaine commande n'est pas une commande valide
     * selon l'état présent du client, alors on l'ignore et on passe à l'autre.
     */
    private function traiterProchaineCommande()
    {
        // Déclaration d'une variable qui va garder le code qui va permettre
        // de déterminer si on accepte la commande ou non
        var bolCommandeAcceptee:Boolean = false;
        // Déclaration d'une liste des commandes acceptables
        var lstCommandesAcceptables:ExtendedArray = ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient));
        // On boucle et passe à la prochaine commande tant qu'elle n'est
        // pas acceptable
        while (lstCommandesAEnvoyer.length >= 1 && bolCommandeAcceptee == false)
        {
            // Obtenir l'objet au début de la liste des commandes à envoyer
            var objCommande:Object = lstCommandesAEnvoyer.shift();
            // Si la commande courante est dans la liste des commandes
            // acceptables, alors on peut la charger en mémoire et commencer à
            // écouter pour ses événements
			trace("**Commande traitee: "+objCommande.nom+" **");
            if (lstCommandesAcceptables.containsByProperty(objCommande.nom, "nom") == true)
            {
		    	trace("la commande est acceptée");
                // La commande est acceptée
                bolCommandeAcceptee = true;
                // Passer tous les Delegate de la commande courante
                for (var i:Number = 0; i < objCommande.listeDelegate.length; i++)
                {
                    // Ajouter un écouteur pour le Delegate courant
					//trace("ds traiterProchaineCommande  addEventListener  "+objCommande.listeDelegate[i].nom);
                    this.addEventListener(objCommande.listeDelegate[i].nom, objCommande.listeDelegate[i].delegate);
                    // Si on est rendu à passer les événements, on va les
                    // ajouter dans la liste des Delegate (le premier est
                    // toujours le delegate pour la commande de retour
                    if (i > 0)
                    {
                        // Ajouter le Delegate de l'événement courant dans
                        // la liste en mémoire
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
                // commande au serveur et démarrer le timer
                objCommandeEnTraitement = objCommande;
                objSocketClient.send(ExtendedString.correctAmperstand(objCommande.objetXML.toString()));
                objTimerEnvoiCommandes.demarrer();
            }
        }
        // Si on n'a pas trouver aucune commande acceptable à traiter, alors
        // il n'y a plus de commande en traitement
        if (bolCommandeAcceptee == false)
        {
            objCommandeEnTraitement = null;
        }
    }
	
    /**
     * Cette fonction permet d'envoyer l'événement de retour de commande,
     * d'enlever l'écouteur pour le retour de la commande, d'envoyer tous
     * les événements qui doivent être envoyés tout dépendant de si la commande
     * a été effectuée avec succès ou non et finalement d'enlever les écouteurs
     * des événements ajoutés par cette commande en cas de retour d'erreur
     *
     * @param XMLNode noeudCommande : Le noeud de commande de retour
     * @param Object objetEvenement : L'événement de retour de commande à
     *                                envoyer
     * @param Boolean accepteApres : Permet de savoir s'il faut laisser passer
     *                               les événements qui arrivent après ou avant
     *                               la commande de retour
     */
    private function envoyerEtMettreAJourEvenements(noeudCommande:XMLNode, objetEvenement:Object)
    {
        // Envoyer l'événement de retour de la commande
        dispatchEvent(objetEvenement);
        // Enlever l'écouteur pour l'événement de retour de la commande
        this.removeEventListener(objCommandeEnTraitement.listeDelegate[0].nom,
                                 objCommandeEnTraitement.listeDelegate[0].delegate);
        // Déclaration d'une variable qui va contenir un noeud XML d'événement
        var objNoeudEvenement:XMLNode;
        // Boucler tant qu'il y a des événements dans la liste d'événements
        while (lstEvenementsRecus.length > 0)
        {
            // Retirer le premier élément de la liste et le mettre dans une
            // variable
            objNoeudEvenement = XMLNode(lstEvenementsRecus.shift());
            // Si la commande a fonctionné avec succès ou qu'elle n'a pas
            // fonctionnée, mais que cet événement n'est pas un événement qui
            // a été ajouté par la commande courante, alors on peut envoyer
            // l'événement, sinon on ne fait que le retirer de la liste
            if (noeudCommande.attributes.type == "Reponse" ||
                (noeudCommande.attributes.type == "Erreur" && objCommandeEnTraitement.listeDelegate.containsByProperty(objNoeudEvenement.attributes.nom, "nom") == false))
            {
                // Déclaration d'une variable qui va permettre de savoir si on
                // accepte les événements après le retour de la commande ou
                // avant
                var bolAccepteApres:Boolean = ExtendedArray.fromArray(Etat.obtenirEvenementsAcceptablesApres(intEtatClient, objCommandeEnTraitement.nom)).contains(objNoeudEvenement.attributes.nom);
                // Si on doit accepter les événements après l'arrivée du retour
                // de la commande du serveur
                if (bolAccepteApres == true)
                {
                    // Si le numéro de l'événement est plus grand ou égal au numéro du
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
                    // Si le numéro de l'événement est plus petit ou égal au numéro du
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
        // Si la commande n'a pas fonctionnée, alors on doit enlever les
        // écouteurs pour les événements qui ont été ajoutés pour cette
        // commande
        if (noeudCommande.attributes.type == "Erreur")
        {
            // Passer tous les événements de la liste d'événements de la
            // commande en traitement et on va enlever leurs écouteurs
            for (var i:Number = 1; i < objCommandeEnTraitement.listeDelegate.length; i++)
            {
                // Enlever le delegate de la liste pour l'événement courant
                delete lstDelegateEvenements[objCommandeEnTraitement.listeDelegate[i].nom];
                // Enlever toutes les occurences du nom de delegate
                lstDelegateEvenements.tableau.remove(objCommandeEnTraitement.listeDelegate[i].nom);
                // Enlever l'écouteur pour l'événement courant
                this.removeEventListener(objCommandeEnTraitement.listeDelegate[i].nom,
                                         objCommandeEnTraitement.listeDelegate[i].delegate);
            }
        }
        // Si le retour de la fonction est une réponse positive et non une
        // erreur, alors on peut retirer les écouteurs des événements
        else if (noeudCommande.attributes.type == "Reponse")
        {
            // Passer la liste des delegate et enlever tous les handlers
            for (var i:Number = lstDelegateEvenements.tableau.length - 1; i >= 0; i--)
            {
                // Si l'événement courant n'est pas accepté dans l'état courant
                // (l'état est déjà modifié), alors on peut enlever l'écouteur
                if (ExtendedArray.fromArray(Etat.obtenirEvenementsAcceptablesEtat(intEtatClient)).contains(lstDelegateEvenements.tableau[i]) == false)
                {
                    // Enlever l'écouteur pour l'événement courant
					this.removeEventListener(lstDelegateEvenements.tableau[i], lstDelegateEvenements[lstDelegateEvenements.tableau[i]]);
		    
                    // Supprimer l'objet delegate courant
                    delete lstDelegateEvenements[lstDelegateEvenements.tableau[i]];
                    lstDelegateEvenements.tableau.remove(lstDelegateEvenements.tableau[i]);
                }
            }
        }
    }
	
    /**
     * Cette fonction permet de transformer le noeud XML de l'événement en un
     * objet événement et d'envoyer cet événement aux écouteurs.
     *
     * @param XMLNode noeudEvenement : Le noeud de l'événement à transformer et
     *                                 à envoyer aux écouteurs
     */
    private function envoyerEvenement(noeudEvenement:XMLNode)
    {
        // Déclaration d'un objet événement qui va contenir les paramètres
        // de l'événement à envoyer
        var objEvenement:Object = new Object();
        // Déclaration d'une référence vers la liste des noeuds paramètres
        var lstChildNodes:Array = noeudEvenement.childNodes;
		
		// garder en mémoire la position du win the game
		var xWinGame:Number;
		var yWinGame:Number;
		/*
		for(var j:Number = 0; j<lstChildNodes.length; j++)
		{
			trace(lstChildNodes[j]);
		}*/
		
        // Définir le type d'événement et le target
        objEvenement.type = noeudEvenement.attributes.nom;
        objEvenement.target = this;
        // Passer tous les noeuds enfants (paramètres) et créer chaque paramètre
        // dans l'objet d'événement
        for (var i:Number = 0; i < lstChildNodes.length; i++)
        {
			//trace("ds for envoyer evenement");
            // Déclarer une chaîne de caractère qui va garder le type courant
            var strNomType:String = String(lstChildNodes[i].attributes.type);
            // Si l'événement n'est pas PartieDemarree, alors on peut simplement
            // aller chercher les valeurs des paramètres, sinon il faut traiter
            // cet événement différemment
		
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
				
				else if(noeudEvenement.attributes.nom == "DeplacementWinTheGame")
				{
					//trouver la case actuelle du win the game, corriger le numéro de la case
					//déplacer le win the game et corriger le numéro de la case

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
				}	// fin du if(noeudEvenement.attributes.nom == "DeplacementWinTheGame")
				
				else if(noeudEvenement.attributes.nom == "UtiliserObjet")
				{
					switch(strNomType)
			    	{
				    	case "joueurQuiUtilise":
							trace(strNomType);
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
									definirIntEtatClient(10);
									
									objEvenement["NouvellePositionX"] = noeudEvenement.firstChild.nextSibling.nextSibling.nextSibling.firstChild.firstChild.firstChild;
									objEvenement["NouvellePositionY"] = noeudEvenement.firstChild.nextSibling.nextSibling.nextSibling.firstChild.firstChild.nextSibling.firstChild;
									
									var twMove:Tween;
									var guiBanane:MovieClip;
									guiBanane = _level0.loader.contentHolder.attachMovie("GUI_banane", "banane", 9998);
									guiBanane._y = 200;
									guiBanane._x = 275;
									_level0.loader.contentHolder["banane"].nomCible = objEvenement["joueurAffecte"];
									
									twMove = new Tween(guiBanane, "_alpha", Strong.easeOut, 40, 100, 1, true);
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
					    		objEvenement.statistiqueJoueur.push({nomUtilisateur:lstChildNodesStatistique[j].attributes.utilisateur, pointage:lstChildNodesStatistique[j].attributes.pointage});	
				    		}
							
							if(noeudEvenement.firstChild.attributes.nom != "")
							{
								for(var j:Number =0; j < objEvenement.statistiqueJoueur.length; j++)
								{
									if(objEvenement.statistiqueJoueur[j].nomUtilisateur == noeudEvenement.firstChild.attributes.nom)
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
							}
									
					    break;
							
						default:
							trace("ds switch(strNomType) pour Partie Terminée - valeur invalide");
						break
				    }	// fin du switch
			    } // fin du if(noeudEvenement.attributes.nom == "PartieTerminee")
			    
				else	// donc (noeudEvenement.attributes.nom != "PartieDemarree" NI "JoueurDeplacePersonnage" NI "PartieTerminee")
			    {
				 	//trace("ds if ds for envoyer evenement  :  "+noeudEvenement.attributes.nom+"   "+lstChildNodes[i].firstChild.nodeValue+"   "+strNomType.substring(0, 1).toLowerCase() + strNomType.substring(1, strNomType.length));
					// Le firstChild va pointer vers un noeud texte
					objEvenement[strNomType.substring(0, 1).toLowerCase() + strNomType.substring(1, strNomType.length)] = lstChildNodes[i].firstChild.nodeValue;
			    }
				
	    	} // fin du if (noeudEvenement.attributes.nom != "PartieDemarree")
			
            else	//donc (noeudEvenement.attributes.nom == "PartieDemarree")
            {
				//trace("ds else ds for envoyer evenement : "+strNomType);
                // Traiter les différents cas et créer leurs objets dans objEvenement
                switch (strNomType)
                {
					// Si le cas est positionWinTheGame, alors on initialise la position du WinTheGame
                    case "positionWinTheGame":
						xWinGame = lstChildNodes[i].attributes.x;
						yWinGame = lstChildNodes[i].attributes.y;
						_level0.loader.contentHolder.objGestionnaireEvenements.setPointageMinimalWinTheGame(lstChildNodes[i].attributes.pointageRequis);
						
						trace("xWinGame : " + xWinGame);
						trace("yWinGame : " + yWinGame);
						trace("PointageRequis : " + lstChildNodes[i].attributes.pointageRequis);
					break;
					
                    // Si le cas est TempsPartie, alors on initialise le temps de la partie
                    case "TempsPartie":
                        // Le firstChild va pointer vers un noeud texte
                        objEvenement["tempsPartie"] = lstChildNodes[i].firstChild.nodeValue;
                    break;
					
                    // Si le cas est Taille, alors on initialise le plateau de jeu
                    case "Taille":
                        // Déclaration de variables qui contiennent le nombre de lignes et
                        // le nombre de colonnes du plateau de jeu
                        var intNbLignes:Number = Number(lstChildNodes[i].firstChild.attributes.nbLignes);
                        var intNbColonnes:Number = Number(lstChildNodes[i].firstChild.attributes.nbColonnes);
                        // Créer un nouveau tableau dans le plateau de jeu
                        objEvenement.plateauJeu = new Array();
                        // Passer toutes les lignes et créer un tableau dans
                        // chaque ligne du plateau de jeu
                        for (var j:Number = 0; j < intNbLignes; j++)
                        {
                            // Créer un nouveau tableau pour la ligne courante
                            objEvenement.plateauJeu.push(new Array());
                            // Passer toutes les colonnes et pour chacune, on va
                            // initialiser la valeur dans le plateau de jeu
                            for (var k:Number = 0; k < intNbColonnes; k++)
                            {
                                // Mettre 0 aux coordonnées x, y courantes
                                objEvenement.plateauJeu[j].push(0);
                            }
                        }
                    break;
					
                    // Si le cas est PositionJoueurs, alors on crée la liste des
                    // positions des joueurs
                    case "PositionJoueurs":
                        // Créer un nouveau tableau pour la position des joueurs
                        objEvenement.positionJoueurs = new Array();
                        // Déclaration d'une référence vers la liste des noeuds
                        // de position des joueurs
                        var lstChildNodesPosition:Array = lstChildNodes[i].childNodes;
                        // Passer tous les noeuds position et les ajouter dans
                        // l'objet d'événement
                        for (var j:Number = 0; j < lstChildNodesPosition.length; j++)
                        {
                            // Mettre un objet contenant le nom du joueur et sa
                            // position x, y
                            objEvenement.positionJoueurs.push({nom:lstChildNodesPosition[j].attributes.nom, x:lstChildNodesPosition[j].attributes.x, y:lstChildNodesPosition[j].attributes.y});
                        }
                    break;
					
                    // Si le cas est PlateauJeu, alors on définit les valeurs dans
                    // chaque case du plateau
                    case "PlateauJeu":
                        // Déclaration d'une référence vers la liste des noeuds
                        // de cases
                        var lstChildNodesCase:Array = lstChildNodes[i].childNodes;
                        // Passer tous les noeuds case et les ajouter dans
                        // le plateau de jeu
                        for (var j:Number = 0; j < lstChildNodesCase.length; j++)
                        {
                            // Déclaration d'une variable qui va contenir la
                            // valeur à mettre dans la case
                            var intValeurCase:Number = Number(lstChildNodesCase[j].attributes.type);
							
							//si la case contient le winTheGame, alors on ajoute 40 000 à la valeur de sa case.
							if(xWinGame == lstChildNodesCase[j].attributes.x && yWinGame == lstChildNodesCase[j].attributes.y)
							{
								intValeurCase += 41000;
							}
							
                            // Si la case courante est une case speciale, alors
                            // on met ajoute 90 à la valeur de la case
                            if (lstChildNodesCase[j].nodeName == "caseSpeciale")
                            {
                                intValeurCase += 90;
                            }
                            // Si le noeud de case courant a un enfant, alors
                            // c'est qu'il y a un objet sur la case (pièce,
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
                                    // en conséquence
                                    if (Boolean(lstChildNodesCase[j].firstChild.attributes.visible) == false)
                                    {
                                        // L'objet est invisible
                                        intValeurCase += 5000;
                                        //trace("invisible");
                                    }
                                }
                                // Sinon c'est que c'est un pièce
                                else
                                {
                                    // On dit que c'est une pièce
									// ok pour ça faudra que je change (un mini-peu) mon code mais c'est correct (coté client)
                                    intValeurCase += (20000 + (Number(lstChildNodesCase[j].firstChild.attributes.valeur) * 100));
                                }
                            }
                            // Mettre la valeur calculée à la position x, y de la case
                            objEvenement.plateauJeu[lstChildNodesCase[j].attributes.x][lstChildNodesCase[j].attributes.y] = intValeurCase;
                        }
                    break;
                }
                // Il faut maintenant mettre à jour l'état courant et commencer
                // à accepter les événements propres à la partie
                intEtatClient = Etat.PARTIE_DEMARREE.no;
                //TODO: Il va falloir améliorer ça car il se pourrait que
                // l'usager fasse une commande et que la commande se traite
                // il faut donc très bien synchroniser les événements
                // Appeler la fonction qui va envoyer tous les événements et
                // retirer leurs écouteurs
                //envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
                // Traiter la prochaine commande
                //traiterProchaineCommande();
            } //fin du (noeudEvenement.attributes.nom == "PartieDemarree")
        
		} // fin de la boucle for
		
		trace("fin Evenement envoye : " + objEvenement.type);
        // Envoyer l'événement aux écouteurs
        dispatchEvent(objEvenement);
    }
	
    /**
     * Cet événement est appelé à chaque fois que le timer a atteint la fin de
     * son temps. Cette méthode n'est pas appelée si le timer est arrêté
     * manuellement.
     */
    private function objTimerEnvoiCommandes_tempsEcoule()
    {
		//trace("Temps ecoule");
        // S'il y a bel et bien une commande en traitement, alors on renvoit
        // la commande et automatiquement le timer recommence son décompte
        // de TEMPS_TIMER/1000 secondes
        if (objCommandeEnTraitement != null)
        {
            // Augmenter le compteur d'erreurs
            intCompteurErreurs++;
            // Si le compteur d'erreurs a atteint 3, alors il y a un problème,
            // on va laisser tomber la commande courante et on va en informer
            // l'interface
            if (intCompteurErreurs == 3)
            {
                // Arrêter le socket XML, cela ne provoque pas l'appel de
                // onClose(), on doit donc appeler l'événement à la main
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
     * Cette méthode permet de se connecter au serveur de jeu.
     *
     * @param Function connexionDelegate : Un pointeur sur la fonction
     *      permettant de retourner la réponse à Flash
     * @param String nomUtilisateur : Le nom d'utilisateur du joueur
     * @param String motDePasse : Le mot de passe du joueur permettant de
     *                            se connecter au serveur de jeu
     */
    public function connexion(connexionDelegate:Function, nomUtilisateur:String,
                              motDePasse:String)
    {
        // Si on est connecté alors on peut continuer le code de la connexion
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("Connexion", "nom") == true)
        {
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"Connexion", delegate:connexionDelegate});
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
			// Créer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            var objNoeudParametreNomUtilisateur:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreNomUtilisateurText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(nomUtilisateur));
            var objNoeudParametreMotDePasse:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreMotDePasseText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(motDePasse));
            var objNoeudParametreLangue:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreLangueText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(_level0.loader.contentHolder.langue));
            var objNoeudParametreGameType:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreGameTypeText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(_level0.loader.contentHolder.gameType));
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "Connexion";
            objNoeudParametreNomUtilisateur.attributes.type = "NomUtilisateur";
            objNoeudParametreMotDePasse.attributes.type = "MotDePasse";
            objNoeudParametreLangue.attributes.type = "Langue";
            objNoeudParametreGameType.attributes.type = "GameType";
            objNoeudParametreNomUtilisateur.appendChild(objNoeudParametreNomUtilisateurText);
            objNoeudParametreMotDePasse.appendChild(objNoeudParametreMotDePasseText);
            objNoeudParametreLangue.appendChild(objNoeudParametreLangueText);
            objNoeudParametreGameType.appendChild(objNoeudParametreGameTypeText);
            objNoeudCommande.appendChild(objNoeudParametreNomUtilisateur);
            objNoeudCommande.appendChild(objNoeudParametreMotDePasse);
            objNoeudCommande.appendChild(objNoeudParametreLangue);
            objNoeudCommande.appendChild(objNoeudParametreGameType);
            objObjetXML.appendChild(objNoeudCommande);
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "Connexion";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecté
        }
    }
	
    /**
     * Cette méthode permet de déconnecter le joueur du serveur de jeu.
     *
     * @param Function deconnexionDelegate : Un pointeur sur la
     *          fonction permettant de dire que le joueur est déconnecté
     */
    public function deconnexion(deconnexionDelegate:Function)
    {
		trace(this.intEtatClient);
        // Si on a obtenu la liste des joueurs, alors on peut continuer le code
        // de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("Deconnexion", "nom") == true)
        {
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"Deconnexion", delegate:deconnexionDelegate});
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
            // Créer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "Deconnexion";
            objObjetXML.appendChild(objNoeudCommande);
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "Deconnexion";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecté
        }
    }
	
    /**
     * Cette méthode permet d'obtenir la liste des joueurs connectés au
     * serveur de jeu.
     *
     * @param Function obtenirListeJoueursDelegate : Un pointeur sur la
     *          fonction permettant de retourner la liste des joueurs
     * @param Function evenementJoueurConnecteDelegate : Un pointeur sur
     *          la fonction permettant de lancer un événement
     * @param Function evenementJoueurDeconnecteDelegate : Un pointeur sur
     *          la fonction permettant de lancer un événement
     */
    public function obtenirListeJoueurs(obtenirListeJoueursDelegate:Function,
                                        evenementJoueurConnecteDelegate:Function,
                                        evenementJoueurDeconnecteDelegate:Function)
    {
        // Si on est connecté alors on peut continuer le code de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("ObtenirListeJoueurs", "nom") == true)
        {
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"ObtenirListeJoueurs", delegate:obtenirListeJoueursDelegate});
            // Ajouter les autres Delegate d'événements
            lstDelegateCommande.push({nom:"JoueurConnecte", delegate:evenementJoueurConnecteDelegate});
            lstDelegateCommande.push({nom:"JoueurDeconnecte", delegate:evenementJoueurDeconnecteDelegate});
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
            // Créer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "ObtenirListeJoueurs";
            objObjetXML.appendChild(objNoeudCommande);
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "ObtenirListeJoueurs";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on ne peut pas faire la commande tout de suite
        }
    }
	
    /**
     * Cette méthode permet d'obtenir la liste des salles .
     *
     * @param Function obtenirListeSallesDelegate : Un pointeur sur la
     *          fonction permettant de retourner la liste des salles
     */
    public function obtenirListeSalles(obtenirListeSallesDelegate:Function)
    {
        // Si on a obtenu la liste des joueurs, alors on peut continuer le code
        // de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("ObtenirListeSalles", "nom") == true)
        {
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"ObtenirListeSalles", delegate:obtenirListeSallesDelegate});
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
            // Créer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "ObtenirListeSalles";
            objObjetXML.appendChild(objNoeudCommande);
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "ObtenirListeSalles";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecté
        }
    }
	
    /**
     * Cette méthode permet au joueur d'entrer dans une salle.
     *
     * @param Function entrerSalleDelegate : Un pointeur sur la
     *          fonction permettant au joueur d'entrer dans une salle
     * @param String nomSalle : Le nom de la salle
     * @param String motDePasse : Le mot de passe pour entrer dans la salle
     *                            (ce mot de passe peut être vide)
     */
    public function entrerSalle(entrerSalleDelegate:Function, nomSalle:String, motDePasse:String)
    {
        // Si on a obtenu la liste des salles, alors on peut continuer le code
        // de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("EntrerSalle", "nom") == true)
        {
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"EntrerSalle", delegate:entrerSalleDelegate});
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
            // Créer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            var objNoeudParametreNomSalle:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreNomSalleText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(nomSalle));
            var objNoeudParametreMotDePasse:XMLNode = objObjetXML.createElement("parametre");
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "EntrerSalle";
            objNoeudParametreNomSalle.attributes.type = "NomSalle";
            objNoeudParametreMotDePasse.attributes.type = "MotDePasse";
            objNoeudParametreNomSalle.appendChild(objNoeudParametreNomSalleText);
            // Si le mot de passe est vide ou à null, alors on n'a pas besoin
            // de le créer, sinon il faut l'ajouter
            if (motDePasse != null && motDePasse != "")
            {
                var objNoeudParametreMotDePasseText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(motDePasse));
                objNoeudParametreMotDePasse.appendChild(objNoeudParametreMotDePasseText);
            }
            objNoeudCommande.appendChild(objNoeudParametreNomSalle);
            objNoeudCommande.appendChild(objNoeudParametreMotDePasse);
            objObjetXML.appendChild(objNoeudCommande);
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "EntrerSalle";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecté
        }
    }
	
	
    /**
     * Cette méthode permet au joueur de quitter la salle dans laquelle il se
     * trouve.
     *
     * @param Function quitterSalleDelegate : Un pointeur sur la
     *          fonction permettant de dire que le joueur a quitté la salle
     */
    public function quitterSalle(quitterSalleDelegate:Function)
    {
        // Si on est dans une salle, alors on peut continuer le code de la
        // fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("QuitterSalle", "nom") == true)
        {
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"QuitterSalle", delegate:quitterSalleDelegate});
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
            // Créer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "QuitterSalle";
            objObjetXML.appendChild(objNoeudCommande);
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "QuitterSalle";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecté
        }
    }

	
    /**
     * Cette méthode permet d'obtenir la liste des joueurs de la salle dans
     * laquelle le joueur se trouve .
     *
     * @param Function obtenirListeJoueursSalleDelegate : Un pointeur sur la
     *          fonction permettant de retourner la liste des joueurs de la salle
     * @param Function evenementJoueurEntreSalleDelegate : Un pointeur sur
     *          la fonction permettant de lancer un événement
     * @param Function evenementJoueurQuitteSalleDelegate : Un pointeur sur
     *          la fonction permettant de lancer un événement
     */
    public function obtenirListeJoueursSalle(obtenirListeJoueursSalleDelegate:Function,
                                        evenementJoueurEntreSalleDelegate:Function,
                                        evenementJoueurQuitteSalleDelegate:Function)
    {
        // Si on est dans le bon état alors on peut continuer le code de la
        // fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("ObtenirListeJoueursSalle", "nom") == true)
        {
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"ObtenirListeJoueursSalle", delegate:obtenirListeJoueursSalleDelegate});
            // Ajouter les autres Delegate d'événements
            lstDelegateCommande.push({nom:"JoueurEntreSalle", delegate:evenementJoueurEntreSalleDelegate});
            lstDelegateCommande.push({nom:"JoueurQuitteSalle", delegate:evenementJoueurQuitteSalleDelegate});
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
            // Créer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "ObtenirListeJoueursSalle";
            objObjetXML.appendChild(objNoeudCommande);
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "ObtenirListeJoueursSalle";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on ne peut pas faire la commande tout de suite
        }
    }
	
    /**
     * Cette méthode permet d'obtenir la liste des tables de la salle courante.
     *
     * @param Function obtenirListeTablesDelegate : Un pointeur sur la
     *          fonction permettant de retourner la liste des tables
     * @param Function evenementJoueurEntreTableDelegate : Un pointeur sur
     *          la fonction permettant de lancer un événement
     * @param Function evenementJoueurQuitteTableDelegate : Un pointeur sur
     *          la fonction permettant de lancer un événement
     * @param Function evenementNouvelleTableDelegate : Un pointeur sur
     *          la fonction permettant de lancer un événement
     * @param Function evenementTableDetruiteDelegate : Un pointeur sur
     *          la fonction permettant de lancer un événement
     * @param String filtre : Le filtre à appliquer pour savoir quelles tables
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
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"ObtenirListeTables", delegate:obtenirListeTablesDelegate});
            // Ajouter les autres Delegate d'événements
            lstDelegateCommande.push({nom:"JoueurEntreTable", delegate:evenementJoueurEntreTableDelegate});
            lstDelegateCommande.push({nom:"JoueurQuitteTable", delegate:evenementJoueurQuitteTableDelegate});
            lstDelegateCommande.push({nom:"NouvelleTable", delegate:evenementNouvelleTableDelegate});
            lstDelegateCommande.push({nom:"TableDetruite", delegate:evenementTableDetruiteDelegate});
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
            // Créer tous les noeuds de la commande
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
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "ObtenirListeTables";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecté
        }
    }
	
    /**
     * Cette méthode permet au joueur de créer une table.
     *
     * @param Function creerTableDelegate : Un pointeur sur la
     *          fonction permettant au joueur de créer une table
     * @param Function evenementJoueurDemarrePartieDelegate : Un pointeur sur
     *          la fonction permettant de lancer un événement
     * @param Number tempsPartie : Le temps de la partie
     */
    public function creerTable(creerTableDelegate:Function,
                               evenementJoueurDemarrePartieDelegate:Function,
                               tempsPartie:Number)
    {
        // Si on a obtenu la liste des tables, alors on peut continuer le code
        // de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("CreerTable", "nom") == true)
        {
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"CreerTable", delegate:creerTableDelegate});
            // Ajouter les autres Delegate d'événements
            lstDelegateCommande.push({nom:"JoueurDemarrePartie", delegate:evenementJoueurDemarrePartieDelegate});
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
            // Créer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            var objNoeudParametreTempsPartie:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreTempsPartieText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(String(tempsPartie)));
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "CreerTable";
            objNoeudParametreTempsPartie.attributes.type = "TempsPartie";
            objNoeudParametreTempsPartie.appendChild(objNoeudParametreTempsPartieText);
            objNoeudCommande.appendChild(objNoeudParametreTempsPartie);
            objObjetXML.appendChild(objNoeudCommande);
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "CreerTable";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
			//trace(objObjetXML);
            // Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecté
        }
    }
	
    /**
     * Cette méthode permet au joueur d'entrer dans une table.
     *
     * @param Function entrerTableDelegate : Un pointeur sur la
     *          fonction permettant au joueur d'entrer dans une table
     * @param Function evenementJoueurDemarrePartieDelegate : Un pointeur sur
     *          la fonction permettant de lancer un événement
     * @param Number noTable : Le numéro de la table
     */
    public function entrerTable(entrerTableDelegate:Function,
                                evenementJoueurDemarrePartieDelegate:Function,
                                noTable:Number)
    {
        // Si on a obtenu la liste des tables, alors on peut continuer le code
        // de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("EntrerTable", "nom") == true)
        {
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"EntrerTable", delegate:entrerTableDelegate});
            // Ajouter les autres Delegate d'événements
            lstDelegateCommande.push({nom:"JoueurDemarrePartie", delegate:evenementJoueurDemarrePartieDelegate});
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
            // Créer tous les noeuds de la commande
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
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "EntrerTable";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecté
        }
    }
	
    /**
     * Cette méthode permet au joueur de quitter la table dans laquelle il se
     * trouve.
     *
     * @param Function quitterTableDelegate : Un pointeur sur la
     *          fonction permettant de dire que le joueur a quitté la table
     */
    public function quitterTable(quitterTableDelegate:Function)
    {
        // Si on est dans une table, alors on peut continuer le code de la
        // fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("QuitterTable", "nom") == true)
        {
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"QuitterTable", delegate:quitterTableDelegate});
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
            // Créer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "QuitterTable";
            objObjetXML.appendChild(objNoeudCommande);
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "QuitterTable";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecté
        }
    }
	
	
	/**
     * Cette méthode permet au joueur de demarrer la partie avant qu'il n'y ait
     * 4 joueurs a la table.
     *
     * @param Function demarrerMaintenantDelegate : Un pointeur sur la
     * fonction permettant de savoir si la commande a ete acceptee
	 * par le serveur.
     */
    public function demarrerMaintenant(demarrerMaintenantDelegate:Function, idPersonnage:Number, niveau:String)
    {
		//trace("OUI OUI OUI! DEMARRER MAINTENANT!!!!  OUI OUI OUI!");
        // Si on est dans une table, alors on peut continuer le code de la
        // fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("DemarrerMaintenant", "nom") == true)
        {
			//trace("OK, tout est sous controle mon chum!");
		
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"DemarrerMaintenant", delegate:demarrerMaintenantDelegate});
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
            // Créer tous les noeuds de la commande
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
	    
	    	objNoeudParametreNiveau.attributes.type = "NiveauJoueurVirtuel";  //JoueurVirtuel
	    	objNoeudParametreNiveau.appendChild(objNoeudParametreNiveauText);
	    
            objNoeudCommande.appendChild(objNoeudParametreIdPersonnage);
	    	objNoeudCommande.appendChild(objNoeudParametreNiveau);
	    
            objObjetXML.appendChild(objNoeudCommande);
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "DemarrerMaintenant";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
                traiterProchaineCommande();
				trace("commande DemarrerMaintenant traitee");
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecté
        }
    }
	
	
    /**
     * Cette méthode permet au joueur de démarrer une partie.
     *
     * @param Function demarrerPartieDelegate : Un pointeur sur la
     *          fonction permettant au joueur de démarrer une partie
     * @param Function evenementPartieDemarreeDelegate : Un pointeur sur
     *          la fonction permettant de lancer un événement
     * @param Function evenementJoueurDeplacePersonnageDelegate : Un pointeur
     *          sur la fonction permettant de lancer un événement
     * @param Funtion evenementSyncroniserTemps : Un pointeur sur la fonction
     *		permettant de syncroniser le temps.
     * @param Number idPersonnage : Le numéro Id du personnage
     */
    public function demarrerPartie(demarrerPartieDelegate:Function,
                                   evenementPartieDemarreeDelegate:Function,
                                   evenementJoueurDeplacePersonnageDelegate:Function,
				evenementSynchroniserTempsDelegate:Function,
				evenementPartieTermineeDelegate:Function,
				idPersonnage:Number) 
    {
        // Si on est dans une table, alors on peut continuer le code de la
        // fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("DemarrerPartie", "nom") == true)
        {
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"DemarrerPartie", delegate:demarrerPartieDelegate});
            // Ajouter les autres Delegate d'événements
            lstDelegateCommande.push({nom:"PartieDemarree", delegate:evenementPartieDemarreeDelegate});
            //TODO: À enlever ou à penser s'il faut le laisser là
            lstDelegateCommande.push({nom:"JoueurDeplacePersonnage", delegate:evenementJoueurDeplacePersonnageDelegate});
	    	lstDelegateCommande.push({nom:"SynchroniserTemps", delegate:evenementSynchroniserTempsDelegate});
	    
	    	lstDelegateCommande.push({nom:"PartieTerminee", delegate:evenementPartieTermineeDelegate});
            
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
            // Créer tous les noeuds de la commande
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
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "DemarrerPartie";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecté
        }
    }

	
    /**
     * Cette méthode permet au joueur de déplacer un personnage.
     *
     * @param Function deplacerPersonnageDelegate : Un pointeur sur la
     *          fonction permettant au joueur de déplacer son personnage
     * @param Point nouvellePosition : La nouvelle position du personnage
     */
    public function deplacerPersonnage(deplacerPersonnageDelegate:Function,
                                       nouvellePosition:Point)
    {
        trace("ds gestCom deplacerPersonnage");
        // Si la partie est commencée, alors on peut continuer le code de la
        // fonction
		trace("intEtatClient : " + intEtatClient);
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("DeplacerPersonnage", "nom") == true)
        {
        	trace("ds gestCom deplacerPersonnage    ds if");
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"DeplacerPersonnage", delegate:deplacerPersonnageDelegate});
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
            // Créer tous les noeuds de la commande
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
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "DeplacerPersonnage";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecté
        }
    }
	
	
	/**
     * Cette méthode permet au joueur d'acheter un objet.
     *
     * @param Function acheterObjetDelegate : Un pointeur sur la
     *          fonction permettant au joueur d'acheter un objet
     * @param Number idObj : le id de l'objet à acheter
     */
	public function acheterObjet(acheterObjetDelegate:Function,
                                       idObj:Number)
    {
        trace("ds gestCom acheterObjet");
        // Si la partie est commencée, alors on peut continuer le code de la
        // fonction
		trace("intEtatClient : " + intEtatClient);
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("AcheterObjet", "nom") == true)
        {
        	trace("ds gestCom acheterObjet    ds if");
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"AcheterObjet", delegate:acheterObjetDelegate});
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
            // Créer tous les noeuds de la commande
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
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "AcheterObjet";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecté
        }
    }
	
	/**
     * Cette méthode permet au joueur d'utiliser un objet.
     *
     * @param Function utilserObjetDelegate : Un pointeur sur la
     *          fonction permettant au joueur d'utiliser un objet
     * @param Number nomObj : le nom de l'objet à acheter
     */
	public function utiliserObjet(acheterObjetDelegate:Function,
                                       idObj:Number)
    {
        trace("ds gestCom utiliserObjet");
        // Si la partie est commencée, alors on peut continuer le code de la
        // fonction
		trace("intEtatClient : " + intEtatClient);
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("UtiliserObjet", "nom") == true)
        {
     	    trace("ds gestCom utiliserObjet    ds if");
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"UtiliserObjet", delegate:acheterObjetDelegate});
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
            // Créer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            var objNoeudParametreId:XMLNode = objObjetXML.createElement("parametre");
			var objNoeudParametreIdTexte:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(String(idObj)));
			objNoeudParametreId.attributes.type = "id";
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "UtiliserObjet";
			objNoeudParametreId.appendChild(objNoeudParametreIdTexte);
			objNoeudCommande.appendChild(objNoeudParametreId);
	  		objObjetXML.appendChild(objNoeudCommande);
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "UtiliserObjet";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
			// Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement

            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecté
        }
    }
	
	
	
	
    /**
     * Cette méthode permet au joueur de répondre à une question posée.
     *
     * @param Function repondreQuestionDelegate : Un pointeur sur la
     *          fonction permettant au joueur de répondre à une question
     * @param String reponse : La réponse à la question
     */
    public function repondreQuestion(repondreQuestionDelegate:Function,
                                     reponse:String)
    {
        // Si on est dans une table, alors on peut continuer le code de la
        // fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("RepondreQuestion", "nom") == true)
        {
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"RepondreQuestion", delegate:repondreQuestionDelegate});
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
            // Créer tous les noeuds de la commande
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
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "RepondreQuestion";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
                traiterProchaineCommande();
            }
        }
        else
        {
            // TODO: Dire qu'on n'est pas connecté
        }
    }
    
    public function definirPointageApresMinigame(definirPointageApresMinigameDelegate:Function, 
     						  points:Number)
    {
	    trace("on est dans la fct defPointMini de gestComm");
	    
        // Si on est connecté alors on peut continuer le code de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("Pointage", "nom") == true)
        {
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"Pointage", delegate:definirPointageApresMinigameDelegate});   
	  
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
            // Créer tous les noeuds de la commande
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
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "Pointage";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
				
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
	    
        // Si on est connecté alors on peut continuer le code de la fonction
        if (ExtendedArray.fromArray(Etat.obtenirCommandesPossibles(intEtatClient)).containsByProperty("Argent", "nom") == true)
        {
            // Déclaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"Argent", delegate:definirArgentApresMinigameDelegate});   
	  
            // Déclaration d'une variable qui va contenir le numéro de la commande
            // générée
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Créer l'objet XML qui va contenir la commande à envoyer au serveur
            var objObjetXML:XML = new XML();
            // Créer tous les noeuds de la commande
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
            // Déclaration d'un nouvel objet qui va contenir les informations sur
            // la commande à traiter courante
            var objObjetCommande:Object = new Object();
            // Définir les propriétés de l'objet de la commande à ajouter dans le
            // tableau des commandes à envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "Argent";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
            // Ajouter l'objet de commande à envoyer courant à la fin du tableau
            var intNbElements:Number = lstCommandesAEnvoyer.push(objObjetCommande);
            // Si le nombre d'éléments dans la liste est de 1 (celui qu'on vient
            // juste d'ajouter) et qu'il n'y aucune commande en traitement, alors
            // on peut envoyer la commande pour la faire traiter (normalement, il
            // ne devrait y avoir aucune commande en traitement), sinon alors elle
            // va se faire traiter très prochainement
            if (intNbElements == 1 && objCommandeEnTraitement == null)
            {
                // Appeler la fonction qui va permettre de traiter la prochaine
                // commande et de charger tout ce qu'il faut en mémoire
				
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
     * 1. Arrêter le timer
     * 2. Construire un objet d'événement selon le noeud XML
     * 3. Changer l'état si la commande est réussie
     * 4. Envoyer l'événement de retour de fonction
     * 5. Enlever l'écouteur du retour de fonction
     * 6. Envoyer les événement en attente
     * 7. Enlever les écouteurs des événements
     * 8. Traiter la prochaine commande
     *
     **********************************************/
    /**
     * Cette méthode permet de décortiquer le noeud de commande passé en
     * paramètres et de lancer un événement à ceux qui s'étaient ajouté comme
     * écouteur à la fonction connexion. On va également envoyer les événements
     * qui attendent d'être traités dans le tableau d'événements et les retirer
     * du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoyé et qui contient sa réponse
     */
    private function retourConnexion(noeudCommande:XMLNode)
    {
		trace("Retour Connexion");
        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le retour de la fonction est une réponse positive et non une
        // erreur, alors on peut passer à l'autre état
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant connecté
            intEtatClient = Etat.CONNECTE.no;
			
			//musique
			objEvenement.listeChansons = new Array();
	
			for(var iii:Number = 0; iii < noeudCommande.childNodes.length; iii++)	
			{
				var str_temp:String = noeudCommande.childNodes[iii].firstChild.nodeValue;
				objEvenement.listeChansons.push(str_temp);
				trace("Liste chansons : " + objEvenement.listeChansons[iii]);
			}
	
        }
		else
		{
			trace("erreur connexion  : "+noeudCommande.attributes.type);
		}
        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }

	
    /**
     * Cette méthode permet de décortiquer le noeud de commande passé en
     * paramètres et de lancer un événement à ceux qui s'étaient ajouté comme
     * écouteur à la fonction deconnexion. On va également envoyer les événements
     * qui attendent d'être traités dans le tableau d'événements et les retirer
     * du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoyé et qui contient sa réponse
     */
    private function retourDeconnexion(noeudCommande:XMLNode)
    {
		trace("Retour Deconnexion");
        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le retour de la fonction est une réponse positive et non une
        // erreur, alors on peut passer à l'autre état
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant connecté
            intEtatClient = Etat.DECONNECTE.no;
        }
        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	
    /**
     * Cette méthode permet de décortiquer le noeud de commande passé en
     * paramètres et de lancer un événement à ceux qui s'étaient ajouté comme
     * écouteur à la fonction obtenirListeJoueurs. On va également envoyer les
     * événements qui attendent d'être traités dans le tableau d'événements et
     * les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous a renvoyé et qui contient sa réponse
     */
    private function retourObtenirListeJoueurs(noeudCommande:XMLNode)
    {
		trace("Retour ObtenirListeJoueurs");
        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le résultat est la liste des joueurs, alors on peut ajouter la
        // liste des joueurs dans l'objet à retourner
        if (objEvenement.resultat == "ListeJoueurs")
        {
            // Déclaration d'une référence vers la liste des noeuds joueurs
            var lstChildNodes:Array = noeudCommande.firstChild.childNodes;
            // Créer un tableau ListeNomUtilisateurs qui va contenir les
            // objets joueurs
            objEvenement.listeNomUtilisateurs = new Array();
            // Passer tous les joueurs et les ajouter dans le tableau
            for (var i:Number = 0; i < lstChildNodes.length; i++)
            {
                // Ajouter l'objet joueur dans le tableau
                objEvenement.listeNomUtilisateurs.push({nom:lstChildNodes[i].attributes.nom});
            }
        }
        // Si le retour de la fonction est une réponse positive et non une
        // erreur, alors on peut passer à l'autre état
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant à l'autre état
            intEtatClient = Etat.LISTE_JOUEURS_OBTENUE.no;
        }
        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	
    /**
     * Cette méthode permet de décortiquer le noeud de commande passé en
     * paramètres et de lancer un événement à ceux qui s'étaient ajouté comme
     * écouteur à la fonction obtenirListeSalles. On va également envoyer les
     * événements qui attendent d'être traités dans le tableau d'événements et
     * les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous a renvoyé et qui contient sa réponse
     */
    private function retourObtenirListeSalles(noeudCommande:XMLNode)
    {
		trace("Retour ObtenirListeSalles");
        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le résultat est la liste des salles, alors on peut ajouter la
        // liste des salles dans l'objet à retourner
        if (objEvenement.resultat == "ListeSalles")
        {
            // Déclaration d'une référence vers la liste des noeuds salles
            var lstChildNodes:Array = noeudCommande.firstChild.childNodes;
            // Créer un tableau ListeNomSalles qui va contenir les
            // objets salle
            objEvenement.listeNomSalles = new Array();
            // Passer toutes les salles et les ajouter dans le tableau
            for (var i:Number = 0; i < lstChildNodes.length; i++)
            {
                // Ajouter l'objet salle dans le tableau
                objEvenement.listeNomSalles.push({nom:lstChildNodes[i].attributes.nom,
                                                  possedeMotDePasse:Boolean(lstChildNodes[i].attributes.protegee == "true")});
            }
        }
        // Si le retour de la fonction est une réponse positive et non une
        // erreur, alors on peut passer à l'autre état
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant à l'autre état
            intEtatClient = Etat.LISTE_SALLES_OBTENUE.no;
        }
        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	
    /**
     * Cette méthode permet de décortiquer le noeud de commande passé en
     * paramètres et de lancer un événement à ceux qui s'étaient ajouté comme
     * écouteur à la fonction entrerSalle. On va également envoyer les événements
     * qui attendent d'être traités dans le tableau d'événements et les retirer
     * du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoyé et qui contient sa réponse
     */
    private function retourEntrerSalle(noeudCommande:XMLNode)
    {
		trace("Retour EntrerSalle");
        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le retour de la fonction est une réponse positive et non une
        // erreur, alors on peut passer à l'autre état
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant à l'autre état
            intEtatClient = Etat.DANS_SALLE.no;
        }
        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	
    /**
     * Cette méthode permet de décortiquer le noeud de commande passé en
     * paramètres et de lancer un événement à ceux qui s'étaient ajouté comme
     * écouteur à la fonction quitterSalle. On va également envoyer les
     * événements qui attendent d'être traités dans le tableau d'événements et
     * les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoyé et qui contient sa réponse
     */
    private function retourQuitterSalle(noeudCommande:XMLNode)
    {
		trace("Retour QuitterSalle");
        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le retour de la fonction est une réponse positive et non une
        // erreur, alors on peut passer à l'autre état
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant à l'autre état
            intEtatClient = Etat.CONNECTE.no;
        }
        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	
    /**
     * Cette méthode permet de décortiquer le noeud de commande passé en
     * paramètres et de lancer un événement à ceux qui s'étaient ajouté comme
     * écouteur à la fonction obtenirListeJoueursSalle. On va également envoyer les
     * événements qui attendent d'être traités dans le tableau d'événements et
     * les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous a renvoyé et qui contient sa réponse
     */
    private function retourObtenirListeJoueursSalle(noeudCommande:XMLNode)
    {
		trace("Retour ObtenirListeJoueursSalle");
        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le résultat est la liste des joueurs de la salle, alors on peut
        // ajouter la liste des joueurs dans l'objet à retourner
        if (objEvenement.resultat == "ListeJoueursSalle")
        {
            // Déclaration d'une référence vers la liste des noeuds joueurs
            var lstChildNodes:Array = noeudCommande.firstChild.childNodes;
            // Créer un tableau ListeNomUtilisateurs qui va contenir les
            // objets joueurs
            objEvenement.listeNomUtilisateurs = new Array();
            // Passer tous les joueurs et les ajouter dans le tableau
            for (var i:Number = 0; i < lstChildNodes.length; i++)
            {
                // Ajouter l'objet joueur dans le tableau
                objEvenement.listeNomUtilisateurs.push({nom:lstChildNodes[i].attributes.nom});
            }
        }
        // Si le retour de la fonction est une réponse positive et non une
        // erreur, alors on peut passer à l'autre état
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant à l'autre état
            intEtatClient = Etat.LISTE_JOUEURS_SALLE_OBTENUE.no;
        }
        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	
    /**
     * Cette méthode permet de décortiquer le noeud de commande passé en
     * paramètres et de lancer un événement à ceux qui s'étaient ajouté comme
     * écouteur à la fonction obtenirListeTables. On va également envoyer les
     * événements qui attendent d'être traités dans le tableau d'événements et
     * les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous a renvoyé et qui contient sa réponse
     */
    private function retourObtenirListeTables(noeudCommande:XMLNode)
    {
		trace("Retour ObtenirListeTables");
        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le résultat est la liste des tables, alors on peut
        // ajouter la liste des tables dans l'objet à retourner
        if (objEvenement.resultat == "ListeTables")
        {
            // Déclaration d'une référence vers la liste des noeuds tables
            var lstTablesChildNodes:Array = noeudCommande.firstChild.childNodes;
            // Créer un tableau ListeTables qui va contenir les
            // objets tables et leurs joueurs
            objEvenement.listeTables = new Array();
            // Passer toutes les tables et les ajouter dans le tableau
            for (var i:Number = 0; i < lstTablesChildNodes.length; i++)
            {
                // Déclaration d'une référence vers la liste des noeuds joueurs
                var lstJoueursChildNodes:Array = lstTablesChildNodes[i].childNodes;
                // Créer un objet qui va contenir les information sur la table
                var objTable:Object = new Object();
                // Définir les propriétés de la table et créer la liste des
                // joueurs
                objTable.no = lstTablesChildNodes[i].attributes.no;
                objTable.temps = lstTablesChildNodes[i].attributes.temps;
                objTable.listeJoueurs = new Array();
                // Passer les joueurs de la table courante et les ajouter
                // dans l'objet table courant
                for (var j:Number = 0; j < lstJoueursChildNodes.length; j++)
                {
                    // Ajouter le joueur courant dans la liste des joueurs de
                    // la table
                    objTable.listeJoueurs.push({nom:lstJoueursChildNodes[j].attributes.nom});
                }
                // Ajouter l'objet table dans le tableau
                objEvenement.listeTables.push(objTable);
            }
        }
        // Si le retour de la fonction est une réponse positive et non une
        // erreur, alors on peut passer à l'autre état
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant à l'autre état
            intEtatClient = Etat.LISTE_TABLES_OBTENUE.no;
        }
        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }

    
    /**
     * Cette méthode permet de décortiquer le noeud de commande passé en
     * paramètres et de lancer un événement à ceux qui s'étaient ajouté comme
     * écouteur à la fonction
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous a renvoyé et qui contient sa réponse
     */
    private function retourDefinirPointageApresMinigame(noeudCommande:XMLNode)
    {
		trace("dans retourDefinirPointageApresMinigame de gesComm");

        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le résultat est le pointage, alors on peut
        // ajouter ce pointage dans l'objet à retourner
        if (objEvenement.resultat == "Pointage")
        {
			// Déclaration d'une référence vers la liste des noeuds
			var lstNoeudsParametre:Array = noeudCommande.childNodes;
			
			trace(lstNoeudsParametre);
			
			//Le seul et unique noeud est le pointage
			var objNoeudParametre:XMLNode = lstNoeudsParametre[0];
			
			objEvenement.pointage = objNoeudParametre.firstChild.nodeValue;
        }
        
        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
    
    
	
	
	/**
     * Cette méthode permet de décortiquer le noeud de commande passé en
     * paramètres et de lancer un événement à ceux qui s'étaient ajouté comme
     * écouteur à la fonction
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous a renvoyé et qui contient sa réponse
     */
    private function retourDefinirArgentApresMinigame(noeudCommande:XMLNode)
    {
		trace("dans retourDefinirArgentApresMinigame de gesComm");

        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le résultat est le pointage, alors on peut
        // ajouter ce pointage dans l'objet à retourner
        if (objEvenement.resultat == "Argent")
        {
			// Déclaration d'une référence vers la liste des noeuds
			var lstNoeudsParametre:Array = noeudCommande.childNodes;
			
			trace(lstNoeudsParametre);
			
			//Le seul et unique noeud est le pointage
			var objNoeudParametre:XMLNode = lstNoeudsParametre[0];
			
			objEvenement.argent = objNoeudParametre.firstChild.nodeValue;
        }
        
        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }

    
    /**
     * Cette méthode permet de décortiquer le noeud de commande passé en
     * paramètres et de lancer un événement à ceux qui s'étaient ajouté comme
     * écouteur à la fonction creerTable. On va également envoyer les
     * événements qui attendent d'être traités dans le tableau d'événements
     * et les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoyé et qui contient sa réponse
     */
    private function retourCreerTable(noeudCommande:XMLNode)
    {
		trace("Retour CreerTable");
        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le résultat est le numéro de la table, alors on peut
        // ajouter le numéro de la table dans l'objet à retourner
        if (objEvenement.resultat == "NoTable")
        {
            // Ajouter l'attribut noTable dans l'objet d'événement
            objEvenement.noTable = Number(noeudCommande.firstChild.firstChild.nodeValue);
        }
        // Si le retour de la fonction est une réponse positive et non une
        // erreur, alors on peut passer à l'autre état
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant à l'autre état
            intEtatClient = Etat.DANS_TABLE.no;
        }
        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }

	
    /**
     * Cette méthode permet de décortiquer le noeud de commande passé en
     * paramètres et de lancer un événement à ceux qui s'étaient ajouté comme
     * écouteur à la fonction entrerTable. On va également envoyer les
     * événements qui attendent d'être traités dans le tableau d'événements
     * et les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoyé et qui contient sa réponse
     */
    private function retourEntrerTable(noeudCommande:XMLNode)
    {
		trace("Retour EntrerTable");
        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le résultat est la liste des personnages choisis par les joueurs
        // de la table, alors on peut ajouter la liste des personnages dans
        // l'objet à retourner
        if (objEvenement.resultat == "ListePersonnageJoueurs")
        {
            // Déclaration d'une référence vers la liste des noeuds personnage
            var lstChildNodes:Array = noeudCommande.firstChild.childNodes;
            // Créer un tableau ListePersonnageJoueurs qui va contenir les
            // objets personnages
            objEvenement.listePersonnageJoueurs = new Array();
            // Passer tous les personnages et les ajouter dans le tableau
            for (var i:Number = 0; i < lstChildNodes.length; i++)
            {
                // Ajouter l'objet joueur dans le tableau
                objEvenement.listePersonnageJoueurs.push({nom:lstChildNodes[i].attributes.nom,
                                                          idPersonnage:lstChildNodes[i].attributes.idPersonnage});
            }
        }
        // Si le retour de la fonction est une réponse positive et non une
        // erreur, alors on peut passer à l'autre état
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant à l'autre état
            intEtatClient = Etat.DANS_TABLE.no;
        }
        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }

    /**
     * Cette méthode permet de décortiquer le noeud de commande passé en
     * paramètres et de lancer un événement à ceux qui s'étaient ajouté comme
     * écouteur à la fonction quitterTable. On va également envoyer les
     * événements qui attendent d'être traités dans le tableau d'événements et
     * les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoyé et qui contient sa réponse
     */
    private function retourQuitterTable(noeudCommande:XMLNode)
    {
		trace("Retour QuitterTable");
        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le retour de la fonction est une réponse positive et non une
        // erreur, alors on peut passer à l'autre état
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant à l'autre état
            intEtatClient = Etat.LISTE_TABLES_OBTENUE.no;
        }
        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	
    /**
     * Cette méthode permet de décortiquer le noeud de commande passé en
     * paramètres et de lancer un événement à ceux qui s'étaient ajouté comme
     * écouteur à la fonction demarrerPartie. On va également envoyer les
     * événements qui attendent d'être traités dans le tableau d'événements
     * et les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoyé et qui contient sa réponse
     */
    private function retourDemarrerPartie(noeudCommande:XMLNode)
    {
		trace("Retour DemarrerPartie");
        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le retour de la fonction est une réponse positive et non une
        // erreur, alors on peut passer à l'autre état
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant à l'autre état
            intEtatClient = Etat.ATTENTE_DEBUT_PARTIE.no;
        }
        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
    
    
    /**
     * Cette méthode permet de décortiquer le noeud de commande passé en
     * paramètres et de lancer un événement à ceux qui s'étaient ajouté comme
     * écouteur à la fonction demarrerMaintenant. On va également envoyer les
     * événements qui attendent d'être traités dans le tableau d'événements
     * et les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoyé et qui contient sa réponse
     */
    private function retourDemarrerMaintenant(noeudCommande:XMLNode)
    {
		trace("retourDemarrerMaintenant");
        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};

        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
	}

    /**
     * Cette méthode permet de décortiquer le noeud de commande passé en
     * paramètres et de lancer un événement à ceux qui s'étaient ajouté comme
     * écouteur à la fonction deplacerPersonnage. On va également envoyer les
     * événements qui attendent d'être traités dans le tableau d'événements
     * et les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoyé et qui contient sa réponse
     */
    private function retourDeplacerPersonnage(noeudCommande:XMLNode)
    {
		trace("Retour DeplacerPersonnage");
        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le résultat est la question à poser, alors on peut ajouter la
        // question dans l'objet à retourner
        if (objEvenement.resultat == "Question")
        {
            // Déclaration d'une référence vers le noeud de question
            var noeudQuestion:XMLNode = noeudCommande.firstChild.firstChild;
            // Créer la question dans l'objet d'événement
            objEvenement.question = new Object();
            // Ajouter les paramètres de la question dans l'objet d'événement
            objEvenement.question.id = Number(noeudQuestion.attributes.id);
            objEvenement.question.type = noeudQuestion.attributes.type;
            objEvenement.question.url = noeudQuestion.attributes.url;
        }
		
        // Si le retour de la fonction est une réponse positive et non une
        // erreur, alors on peut passer à l'autre état
        if (noeudCommande.attributes.type == "Reponse" && noeudCommande.attributes.nom != "Banane")//FRANCOIS
        {
            // On est maintenant à l'autre état
            intEtatClient = Etat.ATTENTE_REPONSE_QUESTION.no;
        }

        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
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

        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le résultat est la question à poser, alors on peut ajouter la
        // question dans l'objet à retourner
     	
		//trace("avant gest comm : " + _level0.loader.contentHolder.planche.obtenirPerso().obtenirMagasin());

		if (objEvenement.resultat == "Ok")
        {
            var noeudArgent:XMLNode = noeudCommande.firstChild;
            // Créer la question dans l'objet d'événement
            objEvenement.argent = new Object();
            // Ajouter les paramètres de la question dans l'objet d'événement
            objEvenement.argent.id = Number(noeudArgent.attributes.id);
            objEvenement.argent.type = noeudArgent.attributes.type;
			

			var noeudMagasin:XMLNode;
			noeudMagasin = noeudCommande.firstChild.nextSibling;
			
			//trace(noeudMagasin.attributes.id);
			//trace(objEvenement.argent.id);
			_level0.loader.contentHolder.planche.obtenirPerso().setNouveauID(Number(noeudMagasin.attributes.id));
			_level0.loader.contentHolder.planche.obtenirPerso().setVieuxID(objEvenement.argent.id);

        }

        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }

	
    /**
     * Cette méthode permet de décortiquer le noeud de commande passé en
     * paramètres et de lancer un événement à ceux qui s'étaient ajouté comme
     * écouteur à la fonction utiliserObjet. On va également envoyer les
     * événements qui attendent d'être traités dans le tableau d'événements
     * et les retirer du tableau.
	 *
	 * pour chaque objet à utiliser envoyé par le serveur, on va envoyer les 
	 * informations appropriées pour le gestionnaire d'événements
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoyé et qui contient sa réponse
     */
	private function retourUtiliserObjet(noeudCommande:XMLNode)
    {
		trace("ds gesComm Retour UtiliserObjet");

        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le résultat est la question à poser, alors on peut ajouter la
        // question dans l'objet à retourner
        if (objEvenement.resultat == "RetourUtiliserObjet")
        {
			switch(noeudCommande.attributes.type)
			{
				case "Livre":
					trace("noeudCommande.childNodes : " + noeudCommande.childNodes);
					trace("noeudCommande.firstChild.childNodes : " + noeudCommande.firstChild.childNodes);
		
					objEvenement.objetUtilise = new Object();
					// Ajouter les paramètres de l'objet dans l'objet d'événement
					objEvenement.objetUtilise.typeObjet = noeudCommande.attributes.type;
					objEvenement.objetUtilise.mauvaiseReponse = noeudCommande.firstChild.childNodes;
					
					trace(objEvenement.objetUtilise.type);
					trace(objEvenement.objetUtilise.mauvaiseReponse);
				break;
				
				case "Boule":
					objEvenement.objetUtilise = new Object();
					// Ajouter les paramètres de l'objet dans l'objet d'événement
					objEvenement.objetUtilise.typeObjet = noeudCommande.attributes.type;
					objEvenement.objetUtilise.url = noeudCommande.firstChild.firstChild.attributes.url;
					objEvenement.objetUtilise.type = noeudCommande.firstChild.firstChild.attributes.type;
					
					trace(objEvenement.objetUtilise.url);
					trace(objEvenement.objetUtilise.type);
				break;
				
				case "OK":
					objEvenement.objetUtilise = new Object();
					// Ajouter les paramètres de l'objet dans l'objet d'événement
					objEvenement.objetUtilise.typeObjet = noeudCommande.attributes.type;
					
					trace(objEvenement.objetUtilise.typeObjet);
				break;
				
				default:
					trace("pas objet valide ds retourUtiliserObj ds gestComm");
				break;
			}
        }
		
        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	
	
    /**
     * Cette méthode permet de décortiquer le noeud de commande passé en
     * paramètres et de lancer un événement à ceux qui s'étaient ajouté comme
     * écouteur à la fonction repondreQuestion. On va également envoyer les
     * événements qui attendent d'être traités dans le tableau d'événements
     * et les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoyé et qui contient sa réponse
     */
    private function retourRepondreQuestion(noeudCommande:XMLNode)
    {
		trace("Retour RepondreQuestion ds gestComm");
        // Construire l'objet événement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le résultat est Deplacement choisis par les joueurs
        // de la table, alors on peut ajouter les paramètres dans
        // l'objet à retourner
        if (objEvenement.resultat == "Deplacement")
        {
			trace(objEvenement);
			trace("Deplacement ds gestComm");
            // Déclaration d'une référence vers la liste des noeuds paramètres
            var lstNoeudsParametre:Array = noeudCommande.childNodes;
			
            // Passer tous les paramètres et les ajouter dans l'objet événement
            for (var i:Number = 0; i < lstNoeudsParametre.length; i++)
            {
                // Faire la référence vers le noeud courant
                var objNoeudParametre:XMLNode = lstNoeudsParametre[i];
                // Déterminer le type du paramètre courant et créer l'élément
                // correspondant dans l'objet événement
		
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
                        // ses informations dans l'objet ramassé
                        if (objNoeudParametre.hasChildNodes() == true)
                        {
                            objEvenement.objetRamasse = new Object();
                            objEvenement.objetRamasse.id = Number(objNoeudParametre.firstChild.attributes.id);
							
							var o:ObjetSurCase = new ObjetSurCase();
							o.definirNom(objNoeudParametre.firstChild.attributes.type);
							_level0.loader.contentHolder.planche.obtenirPerso().ajouterObjet(o, objEvenement.objetRamasse.id);
							
                            objEvenement.objetRamasse.type = objNoeudParametre.firstChild.attributes.type;
							objEvenement.collision = "objet";
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
		   
		    		case "Argent":
                        objEvenement.argent = Number(objNoeudParametre.firstChild.nodeValue);
						trace("Argent ds gestComm   "+objEvenement.argent);
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
									//trace("objet"+j+".cout :" + objEvenement["objet"+j].cout);
									//trace("objet"+j+".id :" + objEvenement["objet"+j].id);
									//trace("objet"+j+".type :" + objEvenement["objet"+j].type);
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
        // Si le retour de la fonction est une réponse positive et non une
        // erreur, alors on peut passer à l'autre état
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant à l'autre état
            intEtatClient = Etat.PARTIE_DEMARREE.no;
        }
        // Appeler la fonction qui va envoyer tous les événements et
        // retirer leurs écouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	
	function definirIntEtatClient(intEtat:Number)
	{
		this.intEtatClient = intEtat;
	}
	
	public function obtenirEtatClient():Number
	{
		return intEtatClient;
	}
}