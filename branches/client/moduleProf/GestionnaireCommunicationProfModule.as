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



import EtatProfModule;
import Timer;
import ExtendedArray;
import ExtendedString;
import DispatchingXMLSocket;
import mx.events.EventDispatcher;
import mx.utils.Delegate;

// TODO : Un moment donne il va falloir integrer le reply a un evenement
//        comme pour le ping
class GestionnaireCommunicationProfModule
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
     * de la classe GestionnaireCommunication qui prend le
     * gestionnaire de commandes en parametre.
     */
    function GestionnaireCommunicationProfModule(connexionPhysiqueDelegate:Function, 
												 deconnexionPhysiqueDelegate:Function, 
												 url_serveur:String, port:Number)
    {
        // Initialiser le dispatcher d'evenements (ajoute les fonctions
        // addEventListener, removeEventListener et dispatchEvent)
        EventDispatcher.initialize(this);
        // Garder en memoire le delegate de la fonction de connexion physique
        objConnexionPhysiqueDelegate = connexionPhysiqueDelegate;
        // Garder en memoire le delegate de la fonction de deconnexion physique
        objDeconnexionPhysiqueDelegate = deconnexionPhysiqueDelegate;
        // Au debut le joueur n'est pas connecte
        intEtatClient = EtatProfModule.NON_CONNECTE.no;
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
        // Ajouter l'ecouteur de l'evenement data:  DispatchingXMLSocket.as --> onData convertit en XML
        objSocketClient.addEventListener("data", Delegate.create(this, objSocketClient_onXML));
        // Ajouter l'ecouteur de l'evenement close
        objSocketClient.addEventListener("close", Delegate.create(this, objSocketClient_onClose));
        // Essayer de se connecter au serveur de jeu
		trace("attempting to connect to: " + url_serveur + ", on port: " + port);
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
		//trace("gestCom resultat de la connection :  "+objetEvenement.succes);
        if (objetEvenement.succes == true)
        {
            intEtatClient = EtatProfModule.DECONNECTE.no;
            // Ajouter l'ecouteur de l'evenement du timer
            objTimerEnvoiCommandes.addEventListener("timeout", objTimerDelegate);
        }
        else
        {
            intEtatClient = EtatProfModule.NON_CONNECTE.no;
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
        intEtatClient = EtatProfModule.NON_CONNECTE.no;
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
            this.removeEventListener(lstDelegateEvenements.tableau[i], 
									 lstDelegateEvenements[lstDelegateEvenements.tableau[i]]);
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
                        case "ConnexionProf":
                            retourConnexion(objNoeudCommande);
                            break;
					    case "Deconnexion":
                            retourDeconnexion(objNoeudCommande);
                            break;
                        case "ObtenirListeSallesProf":
                            retourObtenirListeSalles(objNoeudCommande);
							break;
						case "CreateRoom":
                            retourCreateRoom(objNoeudCommande);
                            break;
						case "UpdateRoom":
							retourUpdateRoom(objNoeudCommande);
							break;
						case "DeleteRoom":
							retourDeleteRoom(objNoeudCommande);
							break;
						case "ReportRoom":
                            retourReportRoom(objNoeudCommande);
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
					trace("find me in gesComm ! : "+objNoeudCommande.attributes.nom + " " + 
						  objNoeudCommande.attributes.noClient + " " + objCommandeEnTraitement.no);
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
             ExtendedArray.fromArray(EtatProfModule.obtenirEvenementsAcceptablesEtat(intEtatClient)).contains(noeudCommande.attributes.nom) == true) ||
            (objCommandeEnTraitement != null &&
             ExtendedArray.fromArray(EtatProfModule.obtenirEvenementsAcceptablesCommande(intEtatClient, objCommandeEnTraitement.nom)).contains(noeudCommande.attributes.nom) == true))
        {
            // Envoyer l'evenement aux ecouteurs de cet evenement	
            trace("Envoyer l'evenement aux ecouteurs de cet evenement " + noeudCommande.attributes.nom);	
            envoyerEvenement(noeudCommande);
        }
        // Sinon, s'il y a une commande en traitement, on verifie les
        // cas pouvant causer des problemes de synchronisation (un
        // evenement arrive avant le retour de la requete) et on
        // ajoute l'evenement dans une liste d'evenements en attente
        else if (objCommandeEnTraitement != null &&
                 (ExtendedArray.fromArray(EtatProfModule.obtenirEvenementsAcceptablesAvant(intEtatClient, objCommandeEnTraitement.nom)).contains(noeudCommande.attributes.nom) == true ||
                  ExtendedArray.fromArray(EtatProfModule.obtenirEvenementsAcceptablesApres(intEtatClient, objCommandeEnTraitement.nom)).contains(noeudCommande.attributes.nom) == true))
        {
            // Ajouter l'evenement a la fin de la liste	
            trace("Ajouter l,evenement a la fin de la liste"  + noeudCommande.attributes.nom);
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
        var lstCommandesAcceptables:ExtendedArray = ExtendedArray.fromArray(EtatProfModule.obtenirCommandesPossibles(intEtatClient));
        // On boucle et passe a la prochaine commande tant qu'elle n'est
        // pas acceptable
        while (lstCommandesAEnvoyer.length >= 1 && bolCommandeAcceptee == false)
        {
            // Obtenir l'objet au debut de la liste des commandes a envoyer
            var objCommande:Object = lstCommandesAEnvoyer.shift();
            // Si la commande courante est dans la liste des commandes
            // acceptables, alors on peut la charger en memoire et commencer a
            // ecouter pour ses evenements
			trace("**Commande traitee: " + objCommande.nom + " **");
            if (lstCommandesAcceptables.containsByProperty(objCommande.nom, "nom") == true)
            {
		    	trace("la commande est acceptee");
                // La commande est acceptee
                bolCommandeAcceptee = true;
                // Passer tous les Delegate de la commande courante
                for (var i in objCommande.listeDelegate)
                {
                    // Ajouter un ecouteur pour le Delegate courant
					trace("traiterProchaineCommande  addEventListener  "+objCommande.listeDelegate[i].nom);
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
		if (noeudCommande.attributes.type != "MiseAJour")
        {
		   // Enlever l'ecouteur pour l'evenement de retour de la commande
           this.removeEventListener(objCommandeEnTraitement.listeDelegate[0].nom,
                                 objCommandeEnTraitement.listeDelegate[0].delegate);
		}
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
                var bolAccepteApres:Boolean = ExtendedArray.fromArray(EtatProfModule.obtenirEvenementsAcceptablesApres(intEtatClient, objCommandeEnTraitement.nom)).contains(objNoeudEvenement.attributes.nom);
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
        // commande for 
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
                if (ExtendedArray.fromArray(EtatProfModule.obtenirEvenementsAcceptablesEtat(intEtatClient)).contains(lstDelegateEvenements.tableau[i]) == false)
                {
                    // Enlever l'ecouteur pour l'evenement courant
					this.removeEventListener(lstDelegateEvenements.tableau[i], lstDelegateEvenements[lstDelegateEvenements.tableau[i]]);
		    
                    // Supprimer l'objet delegate courant
                    delete lstDelegateEvenements[lstDelegateEvenements.tableau[i]];
                    lstDelegateEvenements.tableau.remove(lstDelegateEvenements.tableau[i]);
                }
            }
        }
		
		// Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut retirer les ecouteurs des evenements
        else if (noeudCommande.attributes.type == "MiseAJour")
        {
            // Passer la liste des delegate et enlever tous les handlers
            for (var i:Number = lstDelegateEvenements.tableau.length - 1; i >= 0; i--)
            {
                // Si l'evenement courant n'est pas accepte dans l'etat courant
                // (l'etat est deja modifie), alors on peut enlever l'ecouteur
                if (ExtendedArray.fromArray(EtatProfModule.obtenirEvenementsAcceptablesEtat(intEtatClient)).contains(lstDelegateEvenements.tableau[i]) == false)
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
		
				
        // Definir le type d'evenement et le target
        objEvenement.type = noeudEvenement.attributes.nom;
        objEvenement.target = this;
         
        
        // Passer tous les noeuds enfants (parametres) et creer chaque parametre
        // dans l'objet d'evenement
		//var count:Number = lstChildNodes.length;
		var i:Number = 0;
        for(var ii in lstChildNodes)
        {
			//trace("envoyer evenement");
            // Declarer une chaine de caractere qui va garder le type courant
            var strNomType:String = String(lstChildNodes[i].attributes.type);
            // Si l'evenement n'est pas PartieDemarree, alors on peut simplement
            // aller chercher les valeurs des parametres, sinon il faut traiter
            // cet evenement differemment
			trace(i + " strNomType = " + strNomType);
            
				
				 	//trace("envoyer evenement  :  "+noeudEvenement.attributes.nom+"   "+lstChildNodes[i].firstChild.nodeValue+"   "+strNomType.substring(0, 1).toLowerCase() + strNomType.substring(1, strNomType.length));
					// Le firstChild va pointer vers un noeud texte
					objEvenement[strNomType.substring(0, 1).toLowerCase() + strNomType.substring(1, strNomType.length)] = lstChildNodes[i].firstChild.nodeValue;
			   
				          									
         i++;
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
    public function connexion(connexionDelegate:Function, 
							  nomUtilisateur:String, motDePasse:String, abreviationLangue:String)
    {
        // Si on est connecte alors on peut continuer le code de la connexion
        if (ExtendedArray.fromArray(EtatProfModule.obtenirCommandesPossibles(intEtatClient)).containsByProperty("ConnexionProf", "nom") == true)
        {
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
			lstDelegateCommande.push({nom:"ConnexionProf", delegate:connexionDelegate});
            var intNumeroCommande:Number = obtenirNumeroCommande();
            var objObjetXML:XML = new XML();

			var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            
			var objNoeudParametreNomUtilisateur:XMLNode = objObjetXML.createElement("parametre");
			var objNoeudParametreNomUtilisateurText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(nomUtilisateur));
			objNoeudParametreNomUtilisateur.attributes.type = "NomUtilisateur";
			objNoeudParametreNomUtilisateur.appendChild(objNoeudParametreNomUtilisateurText);
			
            var objNoeudParametreMotDePasse:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreMotDePasseText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(motDePasse));
			objNoeudParametreMotDePasse.attributes.type = "MotDePasse";
            objNoeudParametreMotDePasse.appendChild(objNoeudParametreMotDePasseText);
			
            var objNoeudParametreLangue:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreLangueText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(abreviationLangue));
			objNoeudParametreLangue.attributes.type = "Langue";
            objNoeudParametreLangue.appendChild(objNoeudParametreLangueText);
			
			objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "ConnexionProf";
            objNoeudCommande.appendChild(objNoeudParametreNomUtilisateur);
            objNoeudCommande.appendChild(objNoeudParametreMotDePasse);
			objNoeudCommande.appendChild(objNoeudParametreLangue);

			objObjetXML.appendChild(objNoeudCommande);
			var objObjetCommande:Object = new Object();
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "ConnexionProf";
            objObjetCommande.objetXML = objObjetXML;
            objObjetCommande.listeDelegate = lstDelegateCommande;
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
			trace("Connection fails... GComm");
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
        if (ExtendedArray.fromArray(EtatProfModule.obtenirCommandesPossibles(intEtatClient)).containsByProperty("Deconnexion", "nom") == true)
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
     * Cette methode permet d'obtenir la liste des salles .
     *
     * @param Function obtenirListeSallesDelegate : Un pointeur sur la
     *          fonction permettant de retourner la liste des salles
     */
    public function obtenirListeSallesProf(obtenirListeSallesDelegate:Function)
    {
        // Si on a obtenu la liste des joueurs, alors on peut continuer le code
        // de la fonction
        if (ExtendedArray.fromArray(EtatProfModule.obtenirCommandesPossibles(intEtatClient)).containsByProperty("ObtenirListeSallesProf", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"ObtenirListeSallesProf", delegate:obtenirListeSallesDelegate});
						
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
            // Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "ObtenirListeSallesProf";
           	            			
            objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "ObtenirListeSallesProf";
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
     */
    public function deleteRoom(deleteRoomDelegate:Function,roomId:String)
    {
        // Si on a obtenu la liste des tables, alors on peut continuer le code
        // de la fonction
        if (ExtendedArray.fromArray(EtatProfModule.obtenirCommandesPossibles(intEtatClient)).containsByProperty("DeleteRoom", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"DeleteRoom", delegate:deleteRoomDelegate});
            
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
			var objNoeudParametreRoomId:XMLNode = objObjetXML.createElement("parametre");
            var objNoeudParametreRoomIdText:XMLNode = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(roomId));

			// Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "DeleteRoom";
            objNoeudParametreRoomId.attributes.type = "RoomId";
            objNoeudParametreRoomId.appendChild(objNoeudParametreRoomIdText);
            objNoeudCommande.appendChild(objNoeudParametreRoomId);

			objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "DeleteRoom";
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
     */
    public function createRoom(createRoomDelegate:Function, objRoom:Object)
    {
		traceDebugObj(objRoom,"objRoom",1);
        // Si on a obtenu la liste des tables, alors on peut continuer le code
        // de la fonction
        if (ExtendedArray.fromArray(EtatProfModule.obtenirCommandesPossibles(intEtatClient)).containsByProperty("CreateRoom", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"CreateRoom", delegate:createRoomDelegate});
            
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
			// Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "CreateRoom";
			var objNoeudParam:XMLNode;
			var objNoeudParamText:XMLNode;
			for (var paramType:String in objRoom)
			{
				if (paramType == "names" || paramType == "descriptions")
					for (var lang:String in objRoom[paramType]) {
						objNoeudParam = objObjetXML.createElement("parametre");
						objNoeudParamText = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(""+objRoom[paramType][lang]));
						objNoeudParam.attributes.type = paramType.substring(0,paramType.length-1); //remove the 's'
						objNoeudParam.attributes.language_id = (lang == "fr") ? "1" : "2"; // FIX THIS
						objNoeudParam.appendChild(objNoeudParamText);
						objNoeudCommande.appendChild(objNoeudParam);
					}
				else
				{
					objNoeudParam = objObjetXML.createElement("parametre");
					if (paramType == "endDate" && objRoom[paramType] == null)
						objNoeudParamText = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(""));
					else
						objNoeudParamText = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(""+objRoom[paramType]));
					objNoeudParam.attributes.type = paramType;
					objNoeudParam.appendChild(objNoeudParamText);
					objNoeudCommande.appendChild(objNoeudParam);
				}
			}
			
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
     * Cette methode permet au joueur de modifier une salle existante.
     *
     * @param Function updateRoomDelegate : Un pointeur sur la
     *          fonction permettant au joueur de modifier une salle
     * 
     */
    public function updateRoom(updateRoomDelegate:Function, objRoom:Object)
    {
		traceDebugObj(objRoom,"objRoom",1);
        // Si on a obtenu la liste des tables, alors on peut continuer le code
        // de la fonction
        if (ExtendedArray.fromArray(EtatProfModule.obtenirCommandesPossibles(intEtatClient)).containsByProperty("UpdateRoom", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"UpdateRoom", delegate:updateRoomDelegate});
            
            // Declaration d'une variable qui va contenir le numero de la commande
            // generee
            var intNumeroCommande:Number = obtenirNumeroCommande();
            // Creer l'objet XML qui va contenir la commande a envoyer au serveur
            var objObjetXML:XML = new XML();
            // Creer tous les noeuds de la commande
            var objNoeudCommande:XMLNode = objObjetXML.createElement("commande");
			// Construire l'arbre du document XML
            objNoeudCommande.attributes.no = String(intNumeroCommande);
            objNoeudCommande.attributes.nom = "UpdateRoom";
			var objNoeudParam:XMLNode;
			var objNoeudParamText:XMLNode;
			for (var paramType:String in objRoom)
			{
				if (paramType == "names" || paramType == "descriptions")
					for (var lang:String in objRoom[paramType]) {
						objNoeudParam = objObjetXML.createElement("parametre");
						objNoeudParamText = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(""+objRoom[paramType][lang]));
						objNoeudParam.attributes.type = paramType.substring(0,paramType.length-1); //remove the 's'
						objNoeudParam.attributes.language_id = (lang == "fr") ? "1" : "2"; // FIX THIS
						objNoeudParam.appendChild(objNoeudParamText);
						objNoeudCommande.appendChild(objNoeudParam);
					}
				else
				{
					objNoeudParam = objObjetXML.createElement("parametre");
					
					if (paramType == "endDate" && objRoom[paramType] == null)
						objNoeudParamText = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(""));
					else
						objNoeudParamText = objObjetXML.createTextNode(ExtendedString.encodeToUTF8(""+objRoom[paramType]));
					objNoeudParam.attributes.type = paramType;
					objNoeudParam.appendChild(objNoeudParamText);
					objNoeudCommande.appendChild(objNoeudParam);
				}
			}
			
			objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "UpdateRoom";
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
     * Cette methode permet au joueur d'obtenir le rapport sur une salle.
     *
     * @param Function reportRoomDelegate : Un pointeur sur la
     *          fonction permettant au joueur d'ontenir le rapport sur une salle
     * 
     */
    public function reportRoom(reportRoomDelegate:Function, idRoom:Number)
    {
        // Si on a obtenu la liste des tables, alors on peut continuer le code
        // de la fonction
        if (ExtendedArray.fromArray(EtatProfModule.obtenirCommandesPossibles(intEtatClient)).containsByProperty("ReportRoom", "nom") == true)
        {
            // Declaration d'un tableau dont le contenu est un Delegate
            var lstDelegateCommande:ExtendedArray = new ExtendedArray();
            // Ajouter le Delegate de retour dans le tableau des delegate pour
            // cette fonction
            lstDelegateCommande.push({nom:"ReportRoom", delegate:reportRoomDelegate});
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
            objNoeudCommande.attributes.nom = "ReportRoom";
            objNoeudParametreNameRoom.attributes.type = "RoomId";
            objNoeudParametreNameRoom.appendChild(objNoeudParametreNameRoomText);
            objNoeudCommande.appendChild(objNoeudParametreNameRoom);
								
			objObjetXML.appendChild(objNoeudCommande);
            // Declaration d'un nouvel objet qui va contenir les informations sur
            // la commande a traiter courante
            var objObjetCommande:Object = new Object();
            // Definir les proprietes de l'objet de la commande a ajouter dans le
            // tableau des commandes a envoyer
            objObjetCommande.no = intNumeroCommande;
            objObjetCommande.nom = "ReportRoom";
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
            intEtatClient = EtatProfModule.CONNECTE.no;
			
			objEvenement.listeChansons = new Array();
			objEvenement.keywordsMap = new Object();
			objEvenement.gameTypesMap = new Object();
	
	        // Declaration d'une reference vers la liste des noeuds parametres
            var lstNoeudsParametre:Array = noeudCommande.childNodes;
			
            // Passer tous les parametres et les ajouter dans l'objet evenement
			var count:Number = lstNoeudsParametre.length;
            for (var i:Number = 0; i < count; i++)
            {
                var objNoeudParametre:XMLNode = lstNoeudsParametre[i];
                switch (objNoeudParametre.attributes.type)
                {
                    case "musique":
		    			objEvenement.listeChansons.push(objNoeudParametre.firstChild.nodeValue);
						//trace("Liste chansons : " + objEvenement.listeChansons[i]);
                        break;
                    case "userRole":
						objEvenement.userRoleMaster = Number(objNoeudParametre.firstChild.nodeValue);
			            //trace("objEvenement.userRoleMaster : " + objEvenement.userRoleMaster);
						break;
					case "keyword":
						objEvenement.keywordsMap[objNoeudParametre.attributes.id] = objNoeudParametre.firstChild.nodeValue;
						//trace("objEvenement.keyword: " + objEvenement.keywordsMap[objNoeudParametre.attributes.id]);
						break;
					case "gameType":
						objEvenement.gameTypesMap[objNoeudParametre.attributes.id]=objNoeudParametre.firstChild.nodeValue;
						//trace("objEvenement.gameType: " + objEvenement.gameTypesMap[objNoeudParametre.attributes.id]);
						break;
				}
            }
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
            intEtatClient = EtatProfModule.DECONNECTE.no;
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
		trace("Retour ObtenirListeSallesProf");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
		
		// Si le resultat est la liste des salles, alors on peut ajouter la
        // liste des salles dans l'objet a retourner
        if (objEvenement.resultat == "ListeSalles")
        {
            // Declaration d'une reference vers la liste des noeuds salles
            var lstNoeudSalle:Array = noeudCommande.firstChild.childNodes;
            objEvenement.listeNomSalles = new Array();
            
            // Passer toutes les salles et les ajouter dans le tableau
			var count:Number = lstNoeudSalle.length;
            for (var i:Number = 0; i < count; i++)
            {
				var noeudSalle:XMLNode = lstNoeudSalle[i];
				var lstParam:Array = noeudSalle.childNodes;
				var n:Number = lstParam.length;
				var salleCourante:Object = new Object({names:new Object(), descriptions:new Object()});
				salleCourante.roomId = noeudSalle.attributes.id;
				for (var j:Number=0; j<n; j++) {
					var param:XMLNode = lstParam[j];
					if (param.nodeName == "keywordIds") salleCourante.keywordIds = param.firstChild.nodeValue.split(",");
					else if (param.nodeName=="gameTypeIds")salleCourante.gameTypeIds=param.firstChild.nodeValue.split(",");
					else if (param.nodeName == "beginDate")salleCourante.beginDate = param.firstChild.nodeValue;
					else if (param.nodeName == "endDate")salleCourante.endDate = param.firstChild.nodeValue;
					else if (param.nodeName == "masterTime")salleCourante.masterTime = param.firstChild.nodeValue;
					else if (param.nodeName == "password")salleCourante.password = param.firstChild.nodeValue;
					else if (param.nodeName == "name")
						salleCourante.names[param.attributes.language] = param.firstChild.nodeValue;
					else if (param.nodeName == "description")
						salleCourante.descriptions[param.attributes.language] = param.firstChild.nodeValue;
				}
				objEvenement.listeNomSalles.push(salleCourante);
            }
					
        }
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = EtatProfModule.LISTE_SALLES_OBTENUE.no;
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
           
			
        }
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = EtatProfModule.LISTE_SALLES_OBTENUE.no;
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
     * ecouteur a la fonction UpdateRoom. On va egalement envoyer les
     * evenements qui attendent d'etre traites dans le tableau d'evenements
     * et les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoye et qui contient sa reponse
     */
    private function retourUpdateRoom(noeudCommande:XMLNode)
    {
		trace("Retour UpdateRoom");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le resultat est le numero de la table, alors on peut
        // ajouter le numero de la table dans l'objet a retourner
        if (objEvenement.resultat == "OK")
        {
           
			
        }
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = EtatProfModule.LISTE_SALLES_OBTENUE.no;
        }
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }

	private function retourDeleteRoom(noeudCommande:XMLNode)
    {
		trace("Retour DeleteRoom");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        // Si le resultat est le numero de la table, alors on peut
        // ajouter le numero de la table dans l'objet a retourner
		trace("resultat: " + objEvenement.resultat);
		if (objEvenement.resultat == "OK")
        {
           
			
        }
        // Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = EtatProfModule.LISTE_SALLES_OBTENUE.no;
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
     * ecouteur a la fonction reportRoom. On va egalement envoyer les
     * evenements qui attendent d'etre traites dans le tableau d'evenements
     * et les retirer du tableau.
     *
     * @param XMLNode noeudCommande : Le noeud de commande que le serveur
     *                                nous renvoye et qui contient sa reponse
     */
    private function retourReportRoom(noeudCommande:XMLNode)
    {
		trace("Retour ReportRoom");
        // Construire l'objet evenement pour le retour de la fonction
        var objEvenement:Object = {type:objCommandeEnTraitement.listeDelegate[0].nom, target:this,
                                   resultat:noeudCommande.attributes.nom};
        
		// La classe RoomReport convertit le noeud en un object plus facile a manipuler.
        if (objEvenement.resultat == "OK")
            objEvenement.report = new RoomReport(noeudCommande.firstChild);

		// Si le retour de la fonction est une reponse positive et non une
        // erreur, alors on peut passer a l'autre etat
        if (noeudCommande.attributes.type == "Reponse")
        {
            // On est maintenant a l'autre etat
            intEtatClient = EtatProfModule.LISTE_SALLES_OBTENUE.no;
        }
        // Appeler la fonction qui va envoyer tous les evenements et
        // retirer leurs ecouteurs
        envoyerEtMettreAJourEvenements(noeudCommande, objEvenement);
        // Traiter la prochaine commande
        traiterProchaineCommande();
    }
	//*******************************************************************
	
		   
	
	function definirIntEtatClient(intEtat:Number)
	{
		this.intEtatClient = intEtat;
	}
	
	public function obtenirEtatClient():Number
	{
		return intEtatClient;
	}
	
	
	function traceDebugObj(obj:Object,strName:String,depth:Number)
	{
		for (var sss:String in obj)
		{
			if (obj[sss] instanceof Object && depth>0)
				traceDebugObj(obj[sss],strName+"["+sss+"]",depth-1);
			else
				trace(strName+"["+sss+"] = " + obj[sss]);
		}
	}
}