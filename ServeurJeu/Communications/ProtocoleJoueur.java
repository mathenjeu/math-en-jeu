package ServeurJeu.Communications;

import java.net.Socket;
import java.net.SocketException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.GregorianCalendar;
import java.awt.Point;
import Enumerations.Filtre;
import Enumerations.TypeQuestion;
import ClassesUtilitaires.UtilitaireXML;
import ClassesUtilitaires.UtilitaireEncodeurDecodeur;
import ClassesUtilitaires.UtilitaireNombres;
import Enumerations.Commande;
import Enumerations.RetourFonctions.ResultatAuthentification;
import Enumerations.RetourFonctions.ResultatEntreeTable;
import Enumerations.RetourFonctions.ResultatDemarrerPartie;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.ControleurJeu;
import ServeurJeu.ComposantesJeu.Salle;
import ServeurJeu.ComposantesJeu.Table;
import ServeurJeu.ComposantesJeu.Joueurs.Joueur;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;
import ClassesRetourFonctions.RetourVerifierReponseEtMettreAJourPlateauJeu;
import ServeurJeu.Monitoring.Moniteur;
import ServeurJeu.Temps.GestionnaireTemps;
import ServeurJeu.Temps.TacheSynchroniser;
import ServeurJeu.Evenements.EvenementSynchroniserTemps;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.Evenements.EvenementPartieDemarree;
import ServeurJeu.Evenements.InformationDestination;
import ServeurJeu.ComposantesJeu.Question;
import ServeurJeu.ComposantesJeu.Objets.Objet;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.*;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin;
import ServeurJeu.Configuration.GestionnaireMessages;
import java.util.Calendar;
import java.util.Map.Entry;
import java.util.TreeSet;

/**
 * @author Jean-François Brind'Amour
 * 
 */
public class ProtocoleJoueur implements Runnable {

    // Déclaration d'une référence vers le contrôleur de jeu
    private final ControleurJeu objControleurJeu;
    // Déclaration d'une référence vers le gestionnaire des communications
    private final GestionnaireCommunication objGestionnaireCommunication;
    // Déclaration d'une référence vers le vérificateur des connexions
    private final VerificateurConnexions objVerificateurConnexions;
    // Cet objet permet de garder une référence vers le canal de communication
    // entre le serveur et le client (joueur) courant
    private final Socket objSocketJoueur;
    // Déclaration d'un canal de réception
    private InputStream objCanalReception;
    // Cette variable permet de savoir s'il faut arrèter le thread ou non
    private boolean bolStopThread;
    // Déclaration d'une référence vers un joueur humain correspondant à ce
    // protocole
    private JoueurHumain objJoueurHumain;
    // Déclaration d'une variable qui va servir de compteur pour envoyer des
    // commandes ou événements au joueur de ce ProtocoleJoueur (sa valeur
    // maximale est 100, après 100 on recommence à 0)
    private int intCompteurCommande;
    // Déclaration d'une contante gardant le maximum possible pour le
    // compteur de commandes du serveur de jeu
    private final int MAX_COMPTEUR = 100;
    // Déclaration d'une variable qui va contenir le numéro de commande à
    // retourner au client ayant fait une requète au serveur
    private int intNumeroCommandeReponse;
    private final GestionnaireTemps objGestionnaireTemps;
    private final TacheSynchroniser objTacheSynchroniser;
    private static final Logger objLogger = Logger.getLogger(ProtocoleJoueur.class);
    // On obtiendra la langue du joueur pour pouvoir construire la boîte de questions
    private String langue;
    // Déclaration d'une variable qui va permettre de savoir si le joueur
    // en en train de joueur une partie ou non. Cet état sera utile car on
    // ne déconnectera pas un joeur en train de joueur via le vérification de connexion
    private boolean bolEnTrainDeJouer;
    //String with statistics of players answers
    private StringBuffer questionsAnswers;
    // time in seconds as reference to calculate time of reponse
    private int lastQuestionTime;

    /**
     * Constructeur de la classe ProtocoleJoueur qui permet de garder une 
     * référence vers le contrôleur de jeu, vers le gestionnaire des
     * communications et vers le socket du joueur demandant la connexion
     * et de s'assurer qu'il n'y a pas de délai.
     *
     * @param ControleurJeu controleur : Le contrôleur du jeu
     * @param GestionnaireCommunication communication : Le gestionnaire des
     * 							communications entre les clients et le serveur
     * @param VerificateurConnexions verificateur : Le vérificateur des connexions
     * @param Socket socketJoueur : Le canal de communication associé au joueur
     */
    public ProtocoleJoueur(ControleurJeu controleur, VerificateurConnexions verificateur, Socket socketJoueur) {
        super();


        // Initialiser les valeurs du ProtocoleJoueur courant
        objControleurJeu = controleur;
        objGestionnaireCommunication = controleur.obtenirGestionnaireCommunication();
        objVerificateurConnexions = verificateur;
        objSocketJoueur = socketJoueur;
        //objJoueurHumain = null;
        //bolStopThread = false;
        //intCompteurCommande = 0;
        intNumeroCommandeReponse = -1;
        objGestionnaireTemps = controleur.obtenirGestionnaireTemps();
        objTacheSynchroniser = controleur.obtenirTacheSynchroniser();
        //bolEnTrainDeJouer = false;
        objLogger.info(GestionnaireMessages.message("protocole.connexion").replace("$$CLIENT$$", socketJoueur.getInetAddress().toString()));

        questionsAnswers = new StringBuffer();

        try {
            // etant donné que ce sont seulement de petits messages qui sont
            // envoyés entre le client et le serveur, alors il n'est pas
            // nécessaire d'attendre un délai supplémentaire
            objSocketJoueur.setTcpNoDelay(false);
        } catch (SocketException se) {
            objLogger.error(GestionnaireMessages.message("protocole.canal_ferme"));

            // Arrèter le thread
            bolStopThread = true;
        }

    }

    /**
     * Cette méthode est appelée automatiquement par le thread du joueur et elle
     * permet d'exécuter le protocole du joueur courant.
     *
     * @synchronism Cette méthode n'a pas besoin d'ètre synchronisée
     */
    public void run() {
        // Cette variable nous permettra de savoir, lors de l'interception
        // d'une erreur, si c'était une erreu de communication, auquel cas
        // si le joueur était en train de jouer une partie, sa partie
        // sera sauvegardée et il pourra la continuer s'il se reconnecte
        boolean bolErreurSocket = false;
        try {
            // Créer le canal qui permet de recevoir des données sur le canal
            // de communication entre le client et le serveur
            objCanalReception = objSocketJoueur.getInputStream();

            // Cette objet va contenir le message envoyé par le client au serveur
            StringBuffer strMessageRecu = new StringBuffer();

            // Création d'un tableau de 1024 bytes qui va servir à lire sur le canal
            byte[] byttBuffer = new byte[1024];

            // Boucler et obtenir les messages du client (joueur), puis les
            // traiter tant que le client n'a pas décidé de quitter (ou que la
            // connexion ne s'est pas déconnectée)
            while (bolStopThread == false) {
                Thread.sleep(400);
                // Déclaration d'une variable qui va servir de marqueur
                // pour savoir oè on en est rendu dans la lecture
                int intMarqueur = 0;
                
                // Déclaration d'une variable qui va contenir le nombre de
                // bytes réellement lus dans le canal
                int intBytesLus = objCanalReception.read(byttBuffer);

                // Si le nombre de bytes lus est -1, alors c'est que le
                // stream a été fermé, il faut donc terminer le thread
                if (intBytesLus == -1) {
                    objLogger.error("Une erreur est survenue: nombre d'octets lus = -1");
                    bolErreurSocket = true;
                    bolStopThread = true;
                }

                // Passer tous les bytes lus dans le canal de réception et
                // découper le message en cha”ne de commandes selon le byte
                // 0 marquant la fin d'une commande
                for (int i = 0; i < intBytesLus; i++) {
                    // Si le byte courant est le byte de fin de message (EOM)
                    // alors c'est qu'une commande vient de finir, on va donc
                    // traiter la commande reèue
                    if (byttBuffer[i] == (byte) 0) {
                        // Créer une chaîne temporaire qui va garder la chaîne
                        // de caractères lue jusqu'à maintenant
                        String strChaineAccumulee = new String(byttBuffer,
                                intMarqueur, i - intMarqueur);

                        // Ajouter la chaîne courante à la chaîne de commande
                        strMessageRecu.append(strChaineAccumulee);

                        // On appelle une fonction qui va traiter le message reèu du
                        // client et mettre le résultat à retourner dans une variable
                        objLogger.info(GestionnaireMessages.message("protocole.message_recu") + strMessageRecu);

                        // If we're in debug mode (can be set in mathenjeu.xml), print communications
                        GregorianCalendar calendar = new GregorianCalendar();
                        if (ControleurJeu.modeDebug) {
                            String timeB = "" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
                            System.out.println("(" + timeB + ") Reèu:  " + strMessageRecu);
                        }

                        String strMessageAEnvoyer = traiterCommandeJoueur(strMessageRecu.toString());

                        // If we're in debug mode (can be set in mathenjeu.xml), print communications

                        if (ControleurJeu.modeDebug) {
                            String timeA = "" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
                            System.out.println("(" + timeA + ") Envoi: " + strMessageAEnvoyer);
                        }

                        // On remet la variable contenant le numéro de commande
                        // à retourner à -1, pour dire qu'il n'est pas initialisé
                        intNumeroCommandeReponse = -1;

                        // On renvoit une réponse au client seulement si le
                        // message n'est pas à null
                        if (strMessageAEnvoyer != null) {
                            // On appelle la méthode qui permet de renvoyer un
                            // message au client
                            envoyerMessage(strMessageAEnvoyer);
                        }

                        // Vider la chaîne contenant les commandes à traiter
                        strMessageRecu.setLength(0);

                        // Mettre le marqueur à l'endroit courant pour
                        // pouvoir ensuite recommancer une nouvelle chaîne
                        // de commande à partir d'ici
                        intMarqueur = i + 1;
                    }
                }

                // Si le marqueur est toujours plus petit que le nombre de
                // caractères lus, alors c'est qu'on n'a pas encore reèu
                // le marqueur de fin de message EOM (byte 0)
                if (intMarqueur < intBytesLus) {
                    // On garde la partie du message non terminé dans la
                    // chaîne qui va contenir le message à traiter lorsqu'on
                    // recevra le EOM
                    strMessageRecu.append(new String(byttBuffer, intMarqueur, intBytesLus - intMarqueur));
                }
            }
        } catch (IOException ioe) {
            objLogger.error(GestionnaireMessages.message("protocole.erreur_reception"));
            objLogger.error(ioe.getMessage());
            bolErreurSocket = true;
        } catch (TransformerConfigurationException tce) {
            objLogger.error(GestionnaireMessages.message("protocole.erreurXML_transformer"));
            objLogger.error(tce.getMessage());
        } catch (TransformerException te) {
            objLogger.error(GestionnaireMessages.message("protocole.erreurXML_conversion"));
            objLogger.error(te.getMessage());
        } catch (Exception e) {
            objLogger.error(GestionnaireMessages.message("protocole.erreur_thread"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                // On tente de fermer le canal de réception
                objCanalReception.close();
            } catch (IOException ioe) {
                objLogger.error(ioe.getMessage());
            }

            try {
                // On tente de fermer le socket liant le client au serveur
                objSocketJoueur.close();
            } catch (IOException ioe) {
                objLogger.error(ioe.getMessage());
            }

            // Si le joueur humain a été défini dans le protocole, alors
            // c'est qu'il a réussi à se connecter au serveur de jeu, il
            // faut donc aviser le contrôleur de jeu pour qu'il enlève
            // le joueur du serveur de jeu
            if (objJoueurHumain != null) {
                // Informer le contrôleur de jeu que la connexion avec le
                // client (joueur) a été fermée (on ne doit pas obtenir de
                // numéro de commande de cette fonction, car on ne retournera
                // rien du tout)
                objControleurJeu.deconnecterJoueur(objJoueurHumain, false, true);
                System.out.println("! Joueur deconnecter - Protocole ");
                //Thread.currentThread().interrupt();
            }

            // Enlever le protocole du joueur courant de la liste des
            // protocoles de joueurs
            objGestionnaireCommunication.supprimerProtocoleJoueur(this);
        }

        String inetAddress = objSocketJoueur.getInetAddress() == null ? "[?.?.?.?]" : objSocketJoueur.getInetAddress().toString();
        objLogger.info(GestionnaireMessages.message("protocole.fin_thread").replace("$$CLIENT$$", inetAddress));
    }

    /**
     * Cette méthode permet de traiter le message de commande passé en 
     * paramètres et de retourner le message à renvoyer au client.
     *
     * @param String message : le message de commande à traiter (en format XML)
     * @return String : le message à renvoyer au client (en format XML)
     * 		   			null si on ne doit rien retourner au client
     * @throws TransformerConfigurationException : S'il y a une erreur dans la
     * 					configuration du transformeur
     * @throws TransformerException : S'il y a une erreur lors de la conversion
     * 					d'un document XML en une chaîne de code XML
     * @synchronism Cette fonction est synchronisée lorsque nécessaire.
     * 	     		La plupart du temps, on doit synchroniser le
     * 				traitement de la commande seulement dans le cas oè
     * 				on doit passer les éléments d'une liste et qu'il
     * 				peut y avoir des modifications de cette liste par
     * 				un autre joueur. Dans les autres cas, ce sont les
     * 				fonctions appelées qui vont ètre synchronisées.
     */
    private String traiterCommandeJoueur(String message) throws TransformerConfigurationException, TransformerException {
        Moniteur.obtenirInstance().debut("ProtocoleJoueur.traiterCommandeJoueur");

        // Déclaration d'une variable qui permet de savoir si on doit retourner
        // une commande au client ou si ce n'était qu'une réponse du client
        boolean bolDoitRetournerCommande = true;

        // Créer un nouveau Document qui va contenir le code XML du message
        // passé en paramètres
        Document objDocumentXMLEntree = UtilitaireXML.obtenirDocumentXML(message);

        // Créer un nouveau Document qui va contenir le code XML à retourner
        // au client
        Document objDocumentXMLSortie = UtilitaireXML.obtenirDocumentXML();

        // Déclarer une référence vers le premier noeud de la commande
        // du client. Ce noeud est le noeud commande
        Element objNoeudCommandeEntree = objDocumentXMLEntree.getDocumentElement();
        
        // Créer le noeud de commande à retourner
        Element objNoeudCommande = objDocumentXMLSortie.createElement("commande");
        //System.out.println("Commande: " + message);
        // Initialement, on définit les attributs type et nom comme étant Erreur
        // et Commande respectivement pour dire qu'il y a une erreur avec la
        // commande (la commande n'est pas connue) -> Ces attributs seront
        // modifiés par la suite s'il y a d'autres erreurs. Par contre, on ne
        // définit pas tout de suite le numéro de commande à envoyer au client
        objNoeudCommande.setAttribute("type", "Erreur");
        objNoeudCommande.setAttribute("nom", "CommandeNonReconnue");
        System.out.println("Commande: " + message);
        // Si la commande est un ping et qu'il a bel et bien un numéro, alors
        // on peut appeler la méthode du vérificateur de connexions pour lui
        // dire qu'on a reèu un ping, il ne faut rien retourner au client
        if (objDocumentXMLEntree.getChildNodes().getLength() == 1 &&
                objDocumentXMLEntree.getChildNodes().item(0).getNodeName().equals("ping") &&
                objDocumentXMLEntree.getChildNodes().item(0).hasAttributes() == true &&
                objDocumentXMLEntree.getChildNodes().item(0).getAttributes().getNamedItem("numero") != null) {
            // TODO Modifier cette partie pour que la confirmation du ping soit le mème
            // principe pour tous les autres événements sauf que le ping ne renvoit pas
            // de commande au client
            // On ne retourne aucune commande au client
            bolDoitRetournerCommande = false;

            // Appeler la méthode du vérificateur de connexions permettant de
            // dire qu'on vient de recevoir une réponse à un ping de la part
            // d'un client
            objVerificateurConnexions.confirmationPing(this,
                    Integer.parseInt(objDocumentXMLEntree.getChildNodes().item(0).getAttributes().getNamedItem("numero").getNodeValue()));
        }// fin if
        // if flash security control
        else if (objDocumentXMLEntree.getChildNodes().getLength() == 1 &&
                objDocumentXMLEntree.getChildNodes().item(0).getNodeName().equals("policy-file-request")) {

            //objDocumentXMLSortie.removeChild(objNoeudCommande);
            objNoeudCommande = objDocumentXMLSortie.createElement("cross-domain-policy");
            Element objNoeudCommandeIntern = objDocumentXMLSortie.createElement("allow-access-from");
            objNoeudCommandeIntern.setAttribute("domain", "*");
            objNoeudCommandeIntern.setAttribute("to-ports", "*");
            objNoeudCommande.appendChild(objNoeudCommandeIntern);

        } //end else if
        // S'il n'y a pas de noeud commande dans le document XML, alors il y a
        // une erreur, sinon on peut traiter le contenu du message
        else if (objDocumentXMLEntree.getChildNodes().getLength() == 1 &&
                objDocumentXMLEntree.getChildNodes().item(0).getNodeName().equals("commande") &&
                objDocumentXMLEntree.getChildNodes().item(0).hasAttributes() == true &&
                objDocumentXMLEntree.getChildNodes().item(0).getAttributes().getNamedItem("nom") != null &&
                objDocumentXMLEntree.getChildNodes().item(0).getAttributes().getNamedItem("no") != null &&
                Commande.estUnMembre(objNoeudCommandeEntree.getAttribute("nom")) == true) {
            // une commande avec le numéro de commande envoyé par le client
            // Avant de continuer les vérifications, on va pouvoir retourner
            objNoeudCommande.setAttribute("noClient", objNoeudCommandeEntree.getAttribute("no"));

            // Si le noeud de commande n'a pas une structure valide ou ne
            // respecte pas tous les paramètres nécessaires pour le type
            // commande, alors il y a une erreur, sinon on peut traiter cette
            // commande (donc on ne fait rien puisque l'erreur est déjà
            // définie comme étant une erreur de paramètres)
            if (commandeEstValide(objNoeudCommandeEntree) == false) {
                // L'erreur est qu'un ou plusieurs des paramètres n'est pas bon
                // (soit par le nombre, soit le type, ...)
                objNoeudCommande.setAttribute("nom", "ParametrePasBon");
            } else {

                // Pour chaque commande, on va faire certaines validations.
                // On va ensuite traiter la demande du client
                if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.Connexion)) {
                    // Si le joueur est déjà connecté au serveur de jeu, alors
                    // il y a une erreur, sinon on peut valider les informations
                    // sur ce joueur pour ensuite le connecter (mème si cette
                    // vérification est faite lors de l'authentification, il vaut
                    // mieux la faire immédiatement, car èa réduit de beaucoup
                    // les chances que ce joueur se connecte juste après cette
                    // validation)
                    if (objControleurJeu.joueurEstConnecte(obtenirValeurParametre(objNoeudCommandeEntree,
                            "NomUtilisateur").getNodeValue()) == true) {
                        // Le joueur est déjà connecté au serveur de jeu
                        objNoeudCommande.setAttribute("nom", "JoueurDejaConnecte");
                    } else {
                        // On vérifie si le joueur est bel et bien dans la BD et si
                        // son mot de passe est correct
                        String strResultatAuthentification =
                                objControleurJeu.authentifierJoueur(this,
                                obtenirValeurParametre(objNoeudCommandeEntree, "NomUtilisateur").getNodeValue(),
                                obtenirValeurParametre(objNoeudCommandeEntree, "MotDePasse").getNodeValue(), true);

                        // Si le résultat de l'authentification est true alors le
                        // joueur est maintenant connecté
                        if (strResultatAuthentification.equals(ResultatAuthentification.Succes)) {

                            langue = obtenirValeurParametre(objNoeudCommandeEntree, "Langue").getNodeValue();
                            // gameType = obtenirValeurParametre(objNoeudCommandeEntree, "GameType").getNodeValue();

                            if (objControleurJeu.estJoueurDeconnecte(obtenirValeurParametre(objNoeudCommandeEntree,
                                    "NomUtilisateur").getNodeValue())) {

                                // Le joueur a été déconnecté et tente de se reconnecter.
                                // Il faut lui envoyer une réponse spéciale lui
                                // permettant de choisir s'il veut se reconnecter

                                bolEnTrainDeJouer = false;

                                objNoeudCommande.setAttribute("type", "Reponse");
                                objNoeudCommande.setAttribute("nom", "OkEtPartieDejaCommencee");

                                // On va envoyer dans le noeud la liste de chansons que le joueur pourrait aimer
                                ArrayList<Object> liste = objControleurJeu.obtenirGestionnaireBD().obtenirListeURLsMusique(objJoueurHumain);
                                for (int i = 0; i < liste.size(); i++) {
                                    Element objNoeudParametreMusique = objDocumentXMLSortie.createElement("parametre");

                                    // On ajoute un attribut type qui va contenir le type  paramètre
                                    objNoeudParametreMusique.setAttribute("type", "musique");

                                    Text objNoeudTexteMusique = objDocumentXMLSortie.createTextNode((String) liste.get(i));
                                    //System.out.println("Musique  " + (String)liste.get(i));
                                    objNoeudParametreMusique.appendChild(objNoeudTexteMusique);
                                    objNoeudCommande.appendChild(objNoeudParametreMusique);
                                }
                                //**************************************
                                // Créer le noeud de pour le paramètre contenant le role de joueur
                                Element objNoeudParametreRoleJoueur = objDocumentXMLSortie.createElement("parametre");

                                // On ajoute un attribut type qui va contenir le type
                                // du paramètre
                                objNoeudParametreRoleJoueur.setAttribute("type", "userRole");

                                // Créer un noeud texte contenant le role du joueur
                                Text objNoeudTexteRole = objDocumentXMLSortie.createTextNode(Integer.toString(objJoueurHumain.getRole()));

                                // Ajouter le noeud texte au noeud du paramètre
                                objNoeudParametreRoleJoueur.appendChild(objNoeudTexteRole);

                                // Ajouter le noeud paramètre au noeud de commande dans
                                // le document de sortie
                                objNoeudCommande.appendChild(objNoeudParametreRoleJoueur);

                                //******************************************
                            } else {
                                bolEnTrainDeJouer = false;

                                // Il n'y a pas eu d'erreurs
                                objNoeudCommande.setAttribute("type", "Reponse");
                                objNoeudCommande.setAttribute("nom", "Musique");

                                // On va envoyer dans le noeud la liste de chansons que le joueur pourrait aimer
                                ArrayList<Object> liste = objControleurJeu.obtenirGestionnaireBD().obtenirListeURLsMusique(objJoueurHumain);
                                for (int i = 0; i < liste.size(); i++) {
                                    Element objNoeudParametreMusique = objDocumentXMLSortie.createElement("parametre");

                                    // On ajoute un attribut type qui va contenir le type  paramètre
                                    objNoeudParametreMusique.setAttribute("type", "musique");

                                    Text objNoeudTexteMusique = objDocumentXMLSortie.createTextNode((String) liste.get(i));
                                    //System.out.println("Musique  " + (String)liste.get(i));
                                    objNoeudParametreMusique.appendChild(objNoeudTexteMusique);
                                    objNoeudCommande.appendChild(objNoeudParametreMusique);
                                }

                                //**************************************
                                // Créer le noeud de pour le paramètre contenant le role de joueur
                                Element objNoeudParametreRoleJoueur = objDocumentXMLSortie.createElement("parametre");

                                // On ajoute un attribut type qui va contenir le type  paramètre
                                objNoeudParametreRoleJoueur.setAttribute("type", "userRole");

                                // Créer un noeud texte contenant le role du joueur
                                Text objNoeudTexteRole = objDocumentXMLSortie.createTextNode(Integer.toString(objJoueurHumain.getRole()));

                                // Ajouter le noeud texte au noeud du paramètre
                                objNoeudParametreRoleJoueur.appendChild(objNoeudTexteRole);

                                // Ajouter le noeud paramètre au noeud de commande dans
                                // le document de sortie
                                objNoeudCommande.appendChild(objNoeudParametreRoleJoueur);

                                //**************************
                            }//fin else
                        } else if (strResultatAuthentification.equals(ResultatAuthentification.JoueurDejaConnecte)) {
                            // Le joueur est déjà connecté au serveur de jeu
                            objNoeudCommande.setAttribute("nom", "JoueurDejaConnecte");
                        } else {
                            // Sinon la connexion est refusée
                            objNoeudCommande.setAttribute("nom", "JoueurNonConnu");
                        }

                    }

                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.ConnexionProf)) {
                    // Si le joueur est déjà connecté au serveur de jeu, alors
                    // il y a une erreur, sinon on peut valider les informations
                    // sur ce joueur pour ensuite le connecter (mème si cette
                    // vérification est faite lors de l'authentification, il vaut
                    // mieux la faire immédiatement, car èa réduit de beaucoup
                    // les chances que ce joueur se connecte juste après cette
                    // validation)
                    String username = obtenirValeurParametre(objNoeudCommandeEntree,"NomUtilisateur").getNodeValue();
                    String password = obtenirValeurParametre(objNoeudCommandeEntree,"MotDePasse").getNodeValue();
                    if (objControleurJeu.joueurEstConnecte(username)) {
                        // Le joueur est déjà connecté au serveur de jeu
                        objNoeudCommande.setAttribute("nom", "JoueurDejaConnecte");
                    } else if (objControleurJeu.obtenirGestionnaireBD().getUserRole(username, password) == 1) {
                        // Le joeur n'est pas un prof ni un admin
                        objNoeudCommande.setAttribute("nom", "JoueurNonAutorise");
                    } else {
                        // On vérifie si le joueur est bel et bien dans la BD et si
                        // son mot de passe est correct
                        String strResultatAuthentification = objControleurJeu.authentifierJoueur(this,username,password,true);

                        if (strResultatAuthentification.equals(ResultatAuthentification.Succes)) {
                            // Si le résultat de l'authentification est 'Succes' alors le
                            // joueur est maintenant connecté
                            langue = obtenirValeurParametre(objNoeudCommandeEntree, "Langue").getNodeValue();
                            bolEnTrainDeJouer = false;

                            // Il n'y a pas eu d'erreurs
                            objNoeudCommande.setAttribute("type", "Reponse");
                            objNoeudCommande.setAttribute("nom", "DBInfo");

                            // Créer le noeud pour le paramètre contenant le role de joueur
                            // Le format XML est: <parametre type="userRole">2</parametre>  (2==admin, 3==prof)
                            Element objNoeudParametreRoleJoueur = objDocumentXMLSortie.createElement("parametre");
                            objNoeudParametreRoleJoueur.setAttribute("type", "userRole");
                            Text objNoeudTexteRole = objDocumentXMLSortie.createTextNode(Integer.toString(objJoueurHumain.getRole()));
                            objNoeudParametreRoleJoueur.appendChild(objNoeudTexteRole);
                            objNoeudCommande.appendChild(objNoeudParametreRoleJoueur);

                            // On va envoyer dans le noeud la liste de chansons que le joueur pourrait aimer
                            // Le format XML est: <parametre type="musique">Les violons d'acadie.mp3</parametre>
                            ArrayList<Object> liste = objControleurJeu.obtenirGestionnaireBD().obtenirListeURLsMusique(objJoueurHumain);
                            for (int i = 0; i < liste.size(); i++) {
                                Element objNoeudParametreMusique = objDocumentXMLSortie.createElement("parametre");
                                objNoeudParametreMusique.setAttribute("type", "musique");
                                Text objNoeudTexteMusique = objDocumentXMLSortie.createTextNode((String) liste.get(i));
                                objNoeudParametreMusique.appendChild(objNoeudTexteMusique);
                                objNoeudCommande.appendChild(objNoeudParametreMusique);
                            }

                            // On envoi dans le noeud la map des [keyword_id-->name] dans la langue de la connection
                            // Le format XML est: <parametre type="keyword" id="2">Arithmetic</parametre>
                            for (Map.Entry<Integer,String> keywordEntry : objControleurJeu.getKeywordsMap(langue).entrySet()) {
                                Element objNoeudParametreKeyword = objDocumentXMLSortie.createElement("parametre");
                                objNoeudParametreKeyword.setAttribute("type","keyword");
                                objNoeudParametreKeyword.setAttribute("id", keywordEntry.getKey().toString());
                                Text objNoeudTexteKeyword = objDocumentXMLSortie.createTextNode(keywordEntry.getValue());
                                objNoeudParametreKeyword.appendChild(objNoeudTexteKeyword);
                                objNoeudCommande.appendChild(objNoeudParametreKeyword);
                            }

                            // On envoir dans le noeud la map des [game_type_id-->game_type]
                            // Note: Les 'game_type' sont défini en francais seulement dans la BD.
                            // Le format XML est: <parametre type="gameType" id="2">Tournament</parametre>
                            for (Map.Entry<Integer,String> gameTypeEntry : objControleurJeu.getGameTypesMap().entrySet()) {
                                Element objNoeudParametreGameType = objDocumentXMLSortie.createElement("parametre");
                                objNoeudParametreGameType.setAttribute("type","gameType");
                                objNoeudParametreGameType.setAttribute("id", gameTypeEntry.getKey().toString());
                                Text objNoeudTexteGameType = objDocumentXMLSortie.createTextNode(gameTypeEntry.getValue());
                                objNoeudParametreGameType.appendChild(objNoeudTexteGameType);
                                objNoeudCommande.appendChild(objNoeudParametreGameType);
                            }

                        } else if (strResultatAuthentification.equals(ResultatAuthentification.JoueurDejaConnecte)) {
                            // Le joueur est déjà connecté au serveur de jeu
                            objNoeudCommande.setAttribute("nom", "JoueurDejaConnecte");
                        } else {
                            // Sinon la connexion est refusée
                            objNoeudCommande.setAttribute("nom", "JoueurNonConnu");
                        }

                    }

                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.NePasRejoindrePartie)) {
                    // Un joueur déconnecté décide qu'il ne veut pas rejoindre sa partie abandonnée
                    if (objJoueurHumain != null) {
                        if (objControleurJeu.estJoueurDeconnecte(objJoueurHumain.obtenirNomUtilisateur()) == true) {
                            // Ici on enlève le joueur déconnecté de la liste des joueurs déconnectés
                            // on ne l'enlève pas de la liste des joueurs déconnectés de la table car on ne
                            // vérifie qu'avec la liste des joueurs déconnectés du controleur de jeu
                            objControleurJeu.enleverJoueurDeconnecte(objJoueurHumain.obtenirNomUtilisateur());
                            objNoeudCommande.setAttribute("type", "Reponse");
                            objNoeudCommande.setAttribute("nom", "Ok");
                        }
                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.RejoindrePartie)) {
                    // Un joueur déconnecté tente de rejoindre une partie déjà commencée
                    if (objJoueurHumain != null) {
                        // on controle que le joueur est dans la liste des joueurs deconnectes
                        if (objControleurJeu.estJoueurDeconnecte(objJoueurHumain.obtenirNomUtilisateur()) == true) {
                            // Ici, on renvoie l'état du jeu au joueur pour
                            // qu'il puisse reprendre sa partie
                            JoueurHumain objAncientJoueurHumain = objControleurJeu.obtenirJoueurHumainJoueurDeconnecte(objJoueurHumain.obtenirNomUtilisateur());

                            // Envoyer la liste des joueurs
                            envoyerListeJoueurs(objAncientJoueurHumain, objNoeudCommandeEntree.getAttribute("no"));

                            // Faire en sorte que le joueur est correctement
                            // considéré en train de jouer

                            objJoueurHumain = objAncientJoueurHumain;
                            objAncientJoueurHumain.setObjProtocoleJoueur(this);
                            objAncientJoueurHumain.obtenirProtocoleJoueur().definirJoueur(objAncientJoueurHumain);
                            bolEnTrainDeJouer = true;



                            objJoueurHumain.obtenirPartieCourante().obtenirTable().restartGame(objJoueurHumain, true);

                            // Enlever le joueur de la liste des joueurs déconnectés
                            objControleurJeu.enleverJoueurDeconnecte(objJoueurHumain.obtenirNomUtilisateur());
                            Salle objSalle = objJoueurHumain.obtenirPartieCourante().obtenirTable().getObjSalle();
                            objControleurJeu.entrerSalle(objJoueurHumain, objSalle.getRoomId(), objSalle.getPassword(), false);

                            // Envoyer le plateau de jeu, la liste des joueurs,
                            // leurs ids personnage et leurs positions au joueur
                            // qui se reconnecte
                            envoyerPlateauJeu(objAncientJoueurHumain);

                            // envoyer le pointage au joueur
                            envoyerPointage(objAncientJoueurHumain, objNoeudCommandeEntree.getAttribute("no"));

                            // envoyer l'argent au joueur
                            envoyerArgent(objAncientJoueurHumain, objNoeudCommandeEntree.getAttribute("no"));

                            sendTableNumber(objAncientJoueurHumain, objNoeudCommandeEntree.getAttribute("no"));


                            // Envoyer la liste des items du joueur qui
                            // se reconnecte
                            envoyerItemsJoueurDeconnecte(objAncientJoueurHumain, objNoeudCommandeEntree.getAttribute("no"));

                            // Synchroniser temps
                            envoyerSynchroniserTemps(objAncientJoueurHumain);

                            objNoeudCommande.setAttribute("type", "Reponse");
                            objNoeudCommande.setAttribute("nom", "Ok");

                        }
                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.Deconnexion)) {
                    // Si le joueur humain a été défini dans le protocole, alors
                    // c'est qu'il a réussi à se connecter au serveur de jeu, il
                    // faut donc aviser le contrôleur de jeu pour qu'il enlève
                    // le joueur du serveur de jeu
                    if (objJoueurHumain != null) {
                        // Informer le contrôleur de jeu que la connexion avec le
                        // client (joueur) a été fermée (il faut obtenir un numéro
                        // de commandes de cette fonction)
                        System.out.println("Player " + objJoueurHumain.obtenirNomUtilisateur() + " ask for cancel");
                        objControleurJeu.deconnecterJoueur(objJoueurHumain, true, false);


                        // Il n'y a pas eu d'erreurs
                        objNoeudCommande.setAttribute("type", "Reponse");
                        objNoeudCommande.setAttribute("nom", "Ok");
                    } else {
                        // Le joueur n'est pas connecté
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.ObtenirListeJoueurs)) {
                    // Si le joueur est connecté au serveur de jeu, alors on va
                    // retourner au client la liste des joueurs connectés
                    if (objJoueurHumain != null) {
                        // Il n'y a pas eu d'erreurs et il va falloir retourner
                        // une liste de joueurs
                        objNoeudCommande.setAttribute("type", "Reponse");
                        objNoeudCommande.setAttribute("nom", "ListeJoueurs");

                        // Créer le noeud de pour le paramètre contenant la liste
                        // des joueurs à retourner
                        Element objNoeudParametreListeJoueurs = objDocumentXMLSortie.createElement("parametre");

                        // On ajoute un attribut type qui va contenir le type
                        // du paramètre
                        objNoeudParametreListeJoueurs.setAttribute("type", "ListeNomUtilisateurs");

                        // Obtenir la liste des joueurs connectés au serveur de jeu
                        HashMap<String, JoueurHumain> lstListeJoueurs = objControleurJeu.obtenirListeJoueurs();

                        // Empècher d'autres thread de toucher à la liste des
                        // joueurs connectés au serveur de jeu
                        synchronized (lstListeJoueurs) {
                            // Générer un nouveau numéro de commande qui sera
                            // retourné au client
                            genererNumeroReponse();
                            for (JoueurHumain objJoueur : lstListeJoueurs.values()) {
                                Element objNoeudJoueur = objDocumentXMLSortie.createElement("joueur");
                                objNoeudJoueur.setAttribute("nom", objJoueur.obtenirNomUtilisateur());
                                objNoeudParametreListeJoueurs.appendChild(objNoeudJoueur);
                            }
                        }
                        // Ajouter le noeud paramètre au noeud de commande dans
                        // le document de sortie
                        objNoeudCommande.appendChild(objNoeudParametreListeJoueurs);
                    } else {
                        // Sinon, il y a une erreur car le joueur doit ètre connecté
                        // pour pouvoir avoir accès à la liste des joueurs
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.ObtenirListeSalles)) {
                    // Si le joueur est connecté au serveur de jeu, alors on va
                    // retourner au client la liste des salles actives
                    if (objJoueurHumain != null) {
                        // Il n'est pas nécessaire de synchroniser cette partie
                        // du code car on n'ajoute ou retire jamais de salles
                        // Il n'y a pas eu d'erreurs et il va falloir retourner
                        // une liste de salles
                        objNoeudCommande.setAttribute("type", "Reponse");
                        objNoeudCommande.setAttribute("nom", "ListeSalles");

                        String roomsType = (obtenirValeurParametre(objNoeudCommandeEntree, "RoomsType")).getNodeValue();

                        // Créer le noeud pour le paramètre contenant la liste
                        // des salles à retourner
                        Element objNoeudParametreListeSalles = objDocumentXMLSortie.createElement("parametre");

                        // On ajoute un attribut type qui va contenir le type
                        // du paramètre
                        objNoeudParametreListeSalles.setAttribute("type", "ListeNomSalles");

                        HashMap<Integer, Salle> lstListeSalles = new HashMap<Integer, Salle>();

                        // take list of rooms from Controleur
                        lstListeSalles = objControleurJeu.obtenirListeSalles(this.langue, roomsType);

                        // Générer un nouveau numéro de commande qui sera
                        // retourné au client
                        genererNumeroReponse();
                        synchronized (lstListeSalles) {
                            for (Salle objSalle : lstListeSalles.values()) {
                                // Créer le noeud de la salle courante
                                Element objNoeudSalle = objDocumentXMLSortie.createElement("salle");
                                objNoeudSalle.setAttribute("nom", objSalle.getRoomName(this.langue));
                                objNoeudSalle.setAttribute("id", Integer.toString(objSalle.getRoomId()));
                                // On ajoute un attribut protegee qui va contenir
                                // une valeur booléenne permettant de savoir si la
                                // salle est protégée par un mot de passe ou non
                                objNoeudSalle.setAttribute("protegee", Boolean.toString(objSalle.protegeeParMotDePasse()));
                                objNoeudSalle.setAttribute("descriptions", objSalle.getRoomDescription(this.langue));
                                String gameTypes = objSalle.getGameTypeIds().toString();
                                objNoeudSalle.setAttribute("gameTypes", gameTypes);
                                objNoeudSalle.setAttribute("userCreator", objSalle.getCreatorUsername());
                                objNoeudSalle.setAttribute("masterTime", Integer.toString(objSalle.getMasterTime()));
                                // Ajouter le noeud de la salle au noeud du paramètre
                                objNoeudParametreListeSalles.appendChild(objNoeudSalle);
                            }
                        }
                        // Ajouter le noeud paramètre au noeud de commande dans
                        // le document de sortie
                        objNoeudCommande.appendChild(objNoeudParametreListeSalles);
                    } else {
                        // Sinon, il y a une erreur car le joueur doit ètre connecté
                        // pour pouvoir avoir accès à la liste des salles
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.ObtenirListeSallesProf)) {
                    // Si le joueur est connecté au serveur de jeu, alors on va
                    // retourner au client la liste des salles actives
                    if (objJoueurHumain != null) {
                        // Il n'est pas nécessaire de synchroniser cette partie
                        // du code car on n'ajoute ou retire jamais de salles
                        // Il n'y a pas eu d'erreurs et il va falloir retourner
                        // une liste de salles
                        objNoeudCommande.setAttribute("type", "Reponse");
                        objNoeudCommande.setAttribute("nom", "ListeSalles");

                        // Créer le noeud pour le paramètre contenant la liste
                        // des salles à retourner
                        Element objNoeudParametreListeSalles = objDocumentXMLSortie.createElement("parametre");

                        // On ajoute un attribut type qui va contenir le type
                        // du paramètre
                        objNoeudParametreListeSalles.setAttribute("type", "ListeSalles");

                        Map<Integer,Salle> lstListeSalles = objControleurJeu.obtenirGestionnaireBD().getRoomsForUserId(objJoueurHumain.obtenirCleJoueur());

                        // Générer un nouveau numéro de commande qui sera
                        // retourné au client
                        genererNumeroReponse();
                        for (Salle objSalle: lstListeSalles.values()) {
                            // Créer le noeud de la salle courante la structure XML est:
                            // <salle id="25">                                                   (room_id)
                            //    <name language_id="1">Les math au III siecle</name>            (name in language 'language_id')
                            //    <name language_id="2">Math in the III century</name>           (  order & number of language doesn't matter )
                            //    <description language_id="1">Description fr...</description>   (descritpion in language 'language_id)
                            //    <description language_id="2">Description en...</description>   (  order & number of language doesn't matter }
                            //    <password>12345</password>                                     (room password, can be empty)
                            //    <beginDate>2010-08-21 18:11:31</beginDate>                     (format: )
                            //    <endDate>2010-08-23 18:11:31</endDate>                         (   see GestionnaireBD.mejFormatDateHeure)
                            //    <masterTime>30</masterTime>                                    (game length in minutes)
                            //    <keywordIds>1,5,6,27,66</keywordIds>                           (keyword_id for the room, comma separated)
                            //    <gameTypeIds>1,3</gameTypeIds>                                 (game types ids for the room, comma separated)
                            // </salle>
                            Element objNoeudSalle = objDocumentXMLSortie.createElement("salle");
                            objNoeudSalle.setAttribute("id", Integer.toString(objSalle.getRoomId()));
                            for (Map.Entry<Integer, String> nameEntry: objSalle.getNameMap().entrySet()) {
                                Element objNoeudName = objDocumentXMLSortie.createElement("name");
                                String langShortName = objControleurJeu.getLanguageShortName(nameEntry.getKey());
                                objNoeudName.setAttribute("language", langShortName);
                                objNoeudName.appendChild(objDocumentXMLSortie.createTextNode(nameEntry.getValue()));
                                objNoeudSalle.appendChild(objNoeudName);
                            }
                            for (Map.Entry<Integer, String> descriptionEntry: objSalle.getDescriptionMap().entrySet()) {
                                Element objNoeudDescription = objDocumentXMLSortie.createElement("description");
                                String langShortName = objControleurJeu.getLanguageShortName(descriptionEntry.getKey());
                                objNoeudDescription.setAttribute("language", langShortName);
                                objNoeudDescription.appendChild(objDocumentXMLSortie.createTextNode(descriptionEntry.getValue()));
                                objNoeudSalle.appendChild(objNoeudDescription);
                            }
                            Element objNoeudPwd = objDocumentXMLSortie.createElement("password");
                            objNoeudPwd.appendChild(objDocumentXMLSortie.createTextNode(objSalle.getPassword()));
                            objNoeudSalle.appendChild(objNoeudPwd);
                            Element objNoeudBDate = objDocumentXMLSortie.createElement("beginDate");
                            objNoeudBDate.appendChild(objDocumentXMLSortie.createTextNode(GestionnaireBD.mejFormatDateHeure.format(objSalle.getBeginDate())));
                            objNoeudSalle.appendChild(objNoeudBDate);
                            Element objNoeudEDate = objDocumentXMLSortie.createElement("endDate");
                            objNoeudEDate.appendChild(objDocumentXMLSortie.createTextNode(GestionnaireBD.mejFormatDateHeure.format(objSalle.getEndDate())));
                            objNoeudSalle.appendChild(objNoeudEDate);
                            Element objNoeudTime = objDocumentXMLSortie.createElement("masterTime");
                            objNoeudTime.appendChild(objDocumentXMLSortie.createTextNode(""+objSalle.getMasterTime()));
                            objNoeudSalle.appendChild(objNoeudTime);
                            Element objNoeudKeywordIds = objDocumentXMLSortie.createElement("keywordIds");
                            String k_ids = "";
                            for (Integer keyword_id: objSalle.getKeywordIds())
                                k_ids += keyword_id + ",";
                            if (k_ids.endsWith(","))
                                k_ids = k_ids.substring(0,k_ids.length()-1);
                            objNoeudKeywordIds.appendChild(objDocumentXMLSortie.createTextNode(k_ids));
                            objNoeudSalle.appendChild(objNoeudKeywordIds);
                            Element objNoeudGameTypes = objDocumentXMLSortie.createElement("gameTypeIds");
                            String g_types = "";
                            for (Integer gameTypeId: objSalle.getGameTypeIds())
                                g_types += gameTypeId + ",";
                            if (g_types.endsWith(","))
                                g_types = g_types.substring(0,g_types.length()-1);
                            objNoeudGameTypes.appendChild(objDocumentXMLSortie.createTextNode(g_types));
                            objNoeudSalle.appendChild(objNoeudGameTypes);
                            objNoeudParametreListeSalles.appendChild(objNoeudSalle);
                        }
                        // Ajouter le noeud paramètre au noeud de commande dans
                        // le document de sortie
                        objNoeudCommande.appendChild(objNoeudParametreListeSalles);
                    } else {
                        // Sinon, il y a une erreur car le joueur doit ètre connecté
                        // pour pouvoir avoir accès à la liste des salles
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.ObtenirListeSallesRetour)) {
                    // Si le joueur est connecté au serveur de jeu, alors on va
                    // retourner au client la liste des salles actives
                    if (objJoueurHumain != null) {
                        // Il n'est pas nécessaire de synchroniser cette partie
                        // du code car on n'ajoute ou retire jamais de salles
                        // Il n'y a pas eu d'erreurs et il va falloir retourner
                        // une liste de salles
                        objNoeudCommande.setAttribute("type", "Reponse");
                        objNoeudCommande.setAttribute("nom", "ListeSalles");

                        String roomsType = (obtenirValeurParametre(objNoeudCommandeEntree, "RoomsType")).getNodeValue();

                        // Créer le noeud pour le paramètre contenant la liste
                        // des salles à retourner
                        Element objNoeudParametreListeSalles = objDocumentXMLSortie.createElement("parametre");

                        // On ajoute un attribut type qui va contenir le type
                        // du paramètre
                        objNoeudParametreListeSalles.setAttribute("type", "ListeNomSalles");

                        HashMap<Integer, Salle> lstListeSalles = new HashMap<Integer, Salle>();

                        // take list of rooms from Controleur
                        lstListeSalles = objControleurJeu.obtenirListeSalles(this.langue, roomsType);

                        // Générer un nouveau numéro de commande qui sera
                        // retourné au client
                        genererNumeroReponse();
                        synchronized (lstListeSalles) {
                            for (Salle objSalle : lstListeSalles.values()) {
                                Element objNoeudSalle = objDocumentXMLSortie.createElement("salle");

                                objNoeudSalle.setAttribute("nom", objSalle.getRoomName(this.langue));
                                objNoeudSalle.setAttribute("id", Integer.toString(objSalle.getRoomId()));
                                // On ajoute un attribut protegee qui va contenir
                                // une valeur booléenne permettant de savoir si la
                                // salle est protégée par un mot de passe ou non
                                objNoeudSalle.setAttribute("protegee", Boolean.toString(objSalle.protegeeParMotDePasse()));
                                objNoeudSalle.setAttribute("descriptions", objSalle.getRoomDescription(this.langue));
                                String gameTypes = objSalle.getGameTypeIds().toString();
                                objNoeudSalle.setAttribute("gameTypes", gameTypes);
                                objNoeudSalle.setAttribute("userCreator", objSalle.getCreatorUsername());
                                objNoeudSalle.setAttribute("masterTime", Integer.toString(objSalle.getMasterTime()));

                                // Ajouter le noeud de la salle au noeud du paramètre
                                objNoeudParametreListeSalles.appendChild(objNoeudSalle);

                            }
                        }
                        // Ajouter le noeud paramètre au noeud de commande dans
                        // le document de sortie
                        objNoeudCommande.appendChild(objNoeudParametreListeSalles);
                    } else {
                        // Sinon, il y a une erreur car le joueur doit ètre connecté
                        // pour pouvoir avoir accès à la liste des salles
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.CreateRoom)) {
                    // Si le joueur est connecté au serveur de jeu, alors on va
                    // cree la salle
                    
                    if (objJoueurHumain != null) {
                        String password = "";
                        String beginDate =  "";
                        String endDate = "";
                        String masterTime = "";
                        String gameTypeIds = "";
                        String keywordIds = "";
                        NodeList noeudsParam = objNoeudCommandeEntree.getChildNodes();
                        TreeMap<Integer,String> names = new TreeMap<Integer,String>();
                        TreeMap<Integer,String> descriptions = new TreeMap<Integer,String>();
                        for (int i=0; i<noeudsParam.getLength(); i++) {
                            String paramType = noeudsParam.item(i).getAttributes().getNamedItem("type").getNodeValue();
                            if (paramType.equals("password")) {
                                if (noeudsParam.item(i).getChildNodes().getLength() == 0) password = "";
                                else password = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
                            }
                            else if (paramType.equals("beginDate")) beginDate = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
                            else if (paramType.equals("endDate")) endDate = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
                            else if (paramType.equals("masterTime")) masterTime = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
                            else if (paramType.equals("gameTypeIds")) gameTypeIds = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
                            else if (paramType.equals("keywordIds")) keywordIds = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
                            else {
                                String lid = noeudsParam.item(i).getAttributes().getNamedItem("language_id").getNodeValue();
                                if (paramType.equals("name")) names.put(new Integer(lid), noeudsParam.item(i).getChildNodes().item(0).getNodeValue());
                                else descriptions.put(new Integer(lid), noeudsParam.item(i).getChildNodes().item(0).getNodeValue());
                            }
                        }

                        //add room to the DB too
                        objControleurJeu.obtenirGestionnaireBD().putNewRoom(
                                password,
                                objJoueurHumain.obtenirCleJoueur(),
                                names, descriptions,
                                beginDate, endDate,
                                Integer.parseInt(masterTime),
                                keywordIds, gameTypeIds);

                        // Il n'y a pas eu d'erreurs et il va falloir retourner
                        // une liste des salles ?
                        objNoeudCommande.setAttribute("type", "Reponse");
                        objNoeudCommande.setAttribute("nom", "OK");


                        // Générer un nouveau numéro de commande qui sera
                        // retourné au client
                        genererNumeroReponse();

                    } else {
                        // Sinon, il y a une erreur car le joueur doit ètre connecté
                        // pour pouvoir cree des salles
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.UpdateRoom)) {
                    // Si le joueur est connecté au serveur de jeu, alors on va
                    // modifier la salle
                    if (objJoueurHumain != null) {
                        String roomId = "";
                        String password = "";
                        String beginDate =  "";
                        String endDate = "";
                        String masterTime = "";
                        String gameTypeIds = "";
                        String keywordIds = "";
                        NodeList noeudsParam = objNoeudCommandeEntree.getChildNodes();
                        TreeMap<Integer,String> names = new TreeMap<Integer,String>();
                        TreeMap<Integer,String> descriptions = new TreeMap<Integer,String>();
                        for (int i=0; i<noeudsParam.getLength(); i++) {
                            String paramType = noeudsParam.item(i).getAttributes().getNamedItem("type").getNodeValue();
                            if (paramType.equals("roomId")) roomId = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
                            else if (paramType.equals("password")) {
                                if (noeudsParam.item(i).getChildNodes().getLength() == 0) password = "";
                                else password = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
                            }
                            else if (paramType.equals("beginDate")) beginDate = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
                            else if (paramType.equals("endDate")) endDate = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
                            else if (paramType.equals("masterTime")) masterTime = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
                            else if (paramType.equals("gameTypeIds")) gameTypeIds = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
                            else if (paramType.equals("keywordIds")) keywordIds = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
                            else {
                                String lid = noeudsParam.item(i).getAttributes().getNamedItem("language_id").getNodeValue();
                                if (paramType.equals("name")) names.put(new Integer(lid), noeudsParam.item(i).getChildNodes().item(0).getNodeValue());
                                else descriptions.put(new Integer(lid), noeudsParam.item(i).getChildNodes().item(0).getNodeValue());
                            }
                        }

                        //update room to the DB too
                        objControleurJeu.obtenirGestionnaireBD().updateRoom(
                                Integer.parseInt(roomId),
                                password,
                                names, descriptions,
                                beginDate, endDate,
                                Integer.parseInt(masterTime),
                                keywordIds, gameTypeIds);

                        // Il n'y a pas eu d'erreurs et il va falloir retourner
                        // une liste des salles ?
                        objNoeudCommande.setAttribute("type", "Reponse");
                        objNoeudCommande.setAttribute("nom", "OK");


                        // Générer un nouveau numéro de commande qui sera
                        // retourné au client
                        genererNumeroReponse();

                    } else {
                        // Sinon, il y a une erreur car le joueur doit ètre connecté
                        // pour pouvoir cree des salles
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.ReportBugQuestion)) {
                    // Si le joueur est connecté au serveur de jeu, alors on va reporter le bug
                    if (objJoueurHumain != null) {


                        Node objDescription = obtenirValeurParametre(objNoeudCommandeEntree, "Description");
                        String errorDescription = "";
                        if (objDescription != null) {
                            errorDescription = objDescription.getNodeValue();
                        }

                        int langue_id;
                        if (this.langue == "fr") {
                            langue_id = 1;
                        } else {
                            langue_id = 2;
                        }


                        //add the info to the DB
                        objControleurJeu.obtenirGestionnaireBD().reportBugQuestion(objJoueurHumain.obtenirCleJoueur(),
                                objJoueurHumain.obtenirPartieCourante().obtenirQuestionCourante().obtenirCodeQuestion(),
                                langue_id, errorDescription);

                        // Il n'y a pas eu d'erreurs et il va falloir retourner
                        // une liste des salles ?
                        objNoeudCommande.setAttribute("type", "Reponse");
                        objNoeudCommande.setAttribute("nom", "OK");


                        // Générer un nouveau numéro de commande qui sera
                        // retourné au client
                        genererNumeroReponse();

                    } else {
                        // Sinon, il y a une erreur car le joueur doit ètre connecté
                        // pour pouvoir cree des salles
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    }

                } //****************************************************************************
                else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.DeleteRoom)) {
                    // Si le joueur est connecté au serveur de jeu, alors on va
                    // effacer la salle
                    if (objJoueurHumain != null) {
                        Node objRoomId = obtenirValeurParametre(objNoeudCommandeEntree, "RoomId");
                        int room_id = Integer.parseInt(objRoomId.getNodeValue());
                        boolean isDeleted = objControleurJeu.obtenirGestionnaireBD().deleteRoom(room_id);
                        // Il n'y a pas eu d'erreurs et il va falloir retourner
                        // une liste des salles ?
                        objNoeudCommande.setAttribute("type", "Reponse");
                        objNoeudCommande.setAttribute("nom", "OK");
                        // Générer un nouveau numéro de commande qui sera
                        // retourné au client
                        genererNumeroReponse();
                    } else {
                        // Sinon, il y a une erreur car le joueur doit ètre connecté
                        // pour pouvoir effacer une salle
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    }
                }
                else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.ReportRoom)) {
                    // Si le joueur est connecté au serveur de jeu, alors on va
                    // cree la salle
                    if (objJoueurHumain != null) {
                        // Déclaration d'une variable qui va contenir le noeud
                        // du nom de la salle a creer le rapport
                        Node objRoomID = obtenirValeurParametre(objNoeudCommandeEntree, "RoomId");
                        int room_id = Integer.parseInt(objRoomID.getNodeValue());
                        //add report from DB
                        String report = objControleurJeu.obtenirGestionnaireBD().createRoomReport(room_id, this.langue);
                        System.out.println(report);

                        // Il n'y a pas eu d'erreurs et il va falloir retourner
                        // une liste des salles ?
                        objNoeudCommande.setAttribute("type", "Reponse");
                        objNoeudCommande.setAttribute("nom", "OK");
                        objNoeudCommande.setAttribute("report", report);

                        // Générer un nouveau numéro de commande qui sera
                        // retourné au client
                        genererNumeroReponse();

                    } else {
                        // Sinon, il y a une erreur car le joueur doit ètre connecté
                        // pour pouvoir voir le rapport
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    }

                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.CloseRoom)) {
                    // TODO ????????????????
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.EntrerSalle)) {
                    // Si le joueur est connecté, alors on peut faire d'autre
                    // vérifications, sinon il y a une erreur
                    if (objJoueurHumain != null) {
                        // Déclaration d'une variable qui va contenir le noeud
                        // du nom de la salle dans laquelle le client veut entrer
                        Node objRoomID = obtenirValeurParametre(objNoeudCommandeEntree, "RoomID");

                        // Déclaration d'une variable qui va contenir le noeud
                        // du mot de passe permettant d'accéder à la salle (s'il
                        // n'y en a pas, alors le mot de passe sera vide)
                        Node objMotDePasse = obtenirValeurParametre(objNoeudCommandeEntree, "MotDePasse");

                        // Déclaration d'une variable qui va contenir le mot de
                        // passe pour accéder à la salle (peut ètre vide)
                        String strMotDePasse = "";
                        // Si le noeud du mot de passe n'est pas null alors il y
                        // a un mot de passe pour la salle
                        if (objMotDePasse != null) {
                            // Garder le mot de passe en mémoire
                            strMotDePasse = objMotDePasse.getNodeValue();
                        }


                        // Il n'est pas nécessaire de synchroniser ces vérifications
                        // car un protocole ne peut pas exécuter plus qu'une fonction
                        // à la fois, donc les valeurs ne peuvent ètre modifiées par
                        // deux threads à la fois
                        // Si la salle n'existe pas dans le serveur de jeu, alors il
                        // y a une erreur
                        if (objControleurJeu.salleExiste(Integer.parseInt(objRoomID.getNodeValue())) == false) {
                            // La salle n'existe pas
                            objNoeudCommande.setAttribute("nom", "SalleNonExistante");
                        } // Si le joueur courant se trouve déjà dans une salle,
                        // alors il y a une erreur (pas besoin de synchroniser
                        // cette validation, car un seul thread peut modifier cet
                        // objet)
                        else if (objJoueurHumain.obtenirSalleCourante() != null) {
                            // Le joueur est déjà dans une salle
                            objNoeudCommande.setAttribute("nom", "JoueurDansSalle");
                        } else {
                            // Déclaration d'une variable qui va permettre de
                            // savoir si le le joueur a réussi à entrer dans
                            // la salle (donc que le mot de passe était le bon)
                            boolean bolResultatEntreeSalle = objControleurJeu.entrerSalle(objJoueurHumain,
                                    Integer.parseInt(objRoomID.getNodeValue()), strMotDePasse, true);


                            // Si le joueur a réussi à entrer
                            if (bolResultatEntreeSalle) {
                                // Il n'y a pas eu d'erreurs
                                objNoeudCommande.setAttribute("type", "Reponse");
                                objNoeudCommande.setAttribute("nom", "Ok");

                            } else {
                                // Le mot de passe pour entrer dans la salle
                                // n'est pas le bon
                                objNoeudCommande.setAttribute("nom", "MauvaisMotDePasseSalle");
                            }
                        }
                    } else {
                        // Le joueur doit ètre connecté au serveur de jeu pour
                        // pouvoir entrer dans une salle
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.QuitterSalle)) {
                    // Si le joueur n'est pas connecté, alors il y a une erreur
                    if (objJoueurHumain == null) {
                        // Le joueur n'est pas connecté
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    } // Si le joueur n'est pas dans aucune salle, alors il y a
                    // une erreur
                    else if (objJoueurHumain.obtenirSalleCourante() == null) {
                        // Le joueur n'est pas dans aucune salle
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansSalle");
                    } else {
                        // Appeler la méthode pour quitter la salle
                        objJoueurHumain.obtenirSalleCourante().quitterSalle(objJoueurHumain, true, true);

                        // Il n'y a pas eu d'erreurs
                        objNoeudCommande.setAttribute("type", "Reponse");
                        objNoeudCommande.setAttribute("nom", "Ok");
                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.ObtenirListeJoueursSalle)) {
                    // Si le joueur n'est pas connecté au serveur de jeu, alors il
                    // y a une erreur
                    if (objJoueurHumain == null) {
                        // Le joueur ne peut pas accéder à la liste des joueurs
                        // s'il n'est pas connecté au serveur de jeu
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    } // Si le joueur n'est connecté à aucune salle, alors il ne
                    // peut pas obtenir la liste des joueurs dans cette salle
                    else if (objJoueurHumain.obtenirSalleCourante() == null) {
                        // Le joueur ne peut pas accéder à la liste des joueurs
                        // s'il n'est pas dans une salle
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansSalle");
                    } else {
                        // Il n'y a pas eu d'erreurs et il va falloir retourner
                        // une liste de joueurs
                        objNoeudCommande.setAttribute("type", "Reponse");
                        objNoeudCommande.setAttribute("nom", "ListeJoueursSalle");

                        // Créer le noeud de pour le paramètre contenant la liste
                        // des joueurs à retourner
                        Element objNoeudParametreListeJoueurs = objDocumentXMLSortie.createElement("parametre");

                        // On ajoute un attribut type qui va contenir le type
                        // du paramètre
                        objNoeudParametreListeJoueurs.setAttribute("type", "ListeNomUtilisateurs");

                        // Obtenir la liste des joueurs se trouvant dans la
                        // salle courante
                        HashMap<String, JoueurHumain> lstListeJoueurs = objJoueurHumain.obtenirSalleCourante().obtenirListeJoueurs();

                        // Empècher d'autres thread de toucher à la liste des
                        // joueurs se trouvant dans la salle courante
                        synchronized (lstListeJoueurs) {
                            // Passer tous les joueurs connectés et créer un noeud
                            // pour chaque joueur et l'ajouter au noeud de paramètre
                            genererNumeroReponse();
                            for (JoueurHumain objJoueur : lstListeJoueurs.values()) {
                                // Créer le noeud du joueur courant
                                Element objNoeudJoueur = objDocumentXMLSortie.createElement("joueur");
                                objNoeudJoueur.setAttribute("nom", objJoueur.obtenirNomUtilisateur());
                                objNoeudParametreListeJoueurs.appendChild(objNoeudJoueur);
                            }
                        }

                        // Ajouter le noeud paramètre au noeud de commande dans
                        // le document de sortie
                        objNoeudCommande.appendChild(objNoeudParametreListeJoueurs);

                        //objNoeudCommande.setAttribute("chatPermis", Boolean.toString(obtenirJoueurHumain().obtenirSalleCourante().getRegles().obtenirPermetChat()));
                    }

                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.ObtenirListeTables)) {
                    // Cette partie de code est synchronisée de telle manière
                    // que le client peut recevoir des événements d'entrée/sortie
                    // de table avant le no de retour, un peu après, ou
                    // complètement après, dans tous les cas, le client doit
                    // s'occuper d'arranger tout èa et de ne rien faire si des
                    // événements arrivent après le no de retour et que èa ne
                    // change rien à la liste, car c'est normal

                    // Obtenir la valeur du paramètre Filtre et le garder en
                    // mémoire dans une variable
                    String strFiltre = obtenirValeurParametre(objNoeudCommandeEntree, "Filtre").getNodeValue();
                    //strFiltre = "Toutes";
                    // Si le joueur n'est pas connecté au serveur de jeu, alors il
                    // y a une erreur
                    if (objJoueurHumain == null) {
                        // Le joueur ne peut pas accéder à la liste des tables
                        // s'il n'est pas connecté au serveur de jeu
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    } // Si le joueur n'est connecté à aucune salle, alors il ne
                    // peut pas obtenir la liste des tables dans cette salle
                    else if (objJoueurHumain.obtenirSalleCourante() == null) {
                        // Le joueur ne peut pas accéder à la liste des tables
                        // s'il n'est pas dans une salle
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansSalle");
                    } // Si le paramètre Filtre n'est pas l'un des éléments de
                    // l'énumération des filtres, alors il y a une erreur
                    else if (Filtre.estUnMembre(strFiltre) == false) {
                        // Le filtre n'a pas une valeur valide
                        objNoeudCommande.setAttribute("nom", "FiltreNonConnu");
                    } else {
                        // Il n'y a pas eu d'erreurs et il va falloir retourner
                        // une liste de tables
                        objNoeudCommande.setAttribute("type", "Reponse");

                        objNoeudCommande.setAttribute("nom", "ListeTables");

                        // Créer le noeud pour le paramètre contenant la liste
                        // des tables à retourner
                        Element objNoeudParametreListeTables = objDocumentXMLSortie.createElement("parametre");

                        // On ajoute un attribut type qui va contenir le type
                        // du paramètre
                        objNoeudParametreListeTables.setAttribute("type", "ListeTables");

                        // Obtenir la liste des tables se trouvant dans la
                        // salle courante
                        HashMap<Integer, Table> lstListeTables = objJoueurHumain.obtenirSalleCourante().obtenirListeTables();

                        // Empècher d'autres thread de toucher à la liste des
                        // tables se trouvant dans la salle courante
                        synchronized (lstListeTables) {
                            // Générer un nouveau numéro de commande qui sera
                            // retourné au client
                            genererNumeroReponse();
                            // Passer toutes les tables et créer un noeud pour
                            // chaque table et l'ajouter au noeud de paramètre
                            for (Table objTable : lstListeTables.values()) {
                                HashMap<String,JoueurHumain> lstListeJoueurs = objTable.obtenirListeJoueurs();
                                // Empècher d'autres thread de toucher à la liste des
                                // joueurs de la table courante
                                synchronized (lstListeJoueurs) {
                                    //TODO: Peut-ètre va-t-il falloir ajouter
                                    // des validations supplémentaires ici lorsqu'une
                                    // partie débutera ou se terminera
                                    // Si la table est une de celles qui doivent ètre
                                    // retournées selon le filtre, alors on continue


                                    if (strFiltre.equals(Filtre.Toutes) ||
                                            (strFiltre.equals(Filtre.IncompletesNonCommencees) && objTable.estComplete() == false && objTable.estCommencee() == false) ||
                                            (strFiltre.equals(Filtre.IncompletesCommencees) && objTable.estComplete() == false && objTable.estCommencee() == true) ||
                                            (strFiltre.equals(Filtre.CompletesNonCommencees) && objTable.estComplete() == true && objTable.estCommencee() == false) ||
                                            (strFiltre.equals(Filtre.CompletesCommencees) && objTable.estComplete() == true && objTable.estCommencee() == true)) {

                                        // Créer le noeud de la table courante
                                        Element objNoeudTable = objDocumentXMLSortie.createElement("table");

                                        // On ajoute un attribut no qui va contenir le
                                        // numéro de la table
                                        objNoeudTable.setAttribute("no", Integer.toString(objTable.obtenirNoTable()));
                                        // On ajoute un attribut temps qui va contenir le
                                        // temps des parties qui se déroulent sur cette table
                                        objNoeudTable.setAttribute("temps", Integer.toString(objTable.obtenirTempsTotal()));
                                        objNoeudTable.setAttribute("tablName", objTable.getTableName());
                                        objNoeudTable.setAttribute("maxNbPlayers", Integer.toString(objTable.getMaxNbPlayers()));
                                        objNoeudTable.setAttribute("gameType", objTable.getGameType());

                                        // Passer tous les joueurs et créer un noeud pour
                                        // chaque joueur et l'ajouter au noeud de la table
                                        // courante
                                        for (JoueurHumain objJoueur : lstListeJoueurs.values()) {
                                            // Créer le noeud du joueur courant
                                            Element objNoeudJoueur = objDocumentXMLSortie.createElement("joueur");
                                            // On ajoute un attribut nom qui va contenir le
                                            // nom d'utilisateur du joueur
                                            objNoeudJoueur.setAttribute("nom", objJoueur.obtenirNomUtilisateur());
                                            // On ajoute un attribut nom qui va contenir le
                                            // id du 'perso' utiliser par joueur
                                            objNoeudJoueur.setAttribute("idPersonnage", Integer.toString(objJoueur.obtenirPartieCourante().obtenirIdPersonnage()));
                                            // On ajoute un attribut nom qui va contenir le
                                            // la couleur utiliser par joueur
                                            objNoeudJoueur.setAttribute("cloColor", objJoueur.obtenirPartieCourante().getClothesColor());
                                            // Ajouter le noeud du joueur au noeud de la table
                                            objNoeudTable.appendChild(objNoeudJoueur);
                                        }

                                        // Ajouter le noeud de la table au noeud du paramètre
                                        objNoeudParametreListeTables.appendChild(objNoeudTable);
                                    }
                                }
                            }
                        }

                        // Ajouter le noeud paramètre au noeud de commande dans
                        // le document de sortie
                        objNoeudCommande.appendChild(objNoeudParametreListeTables);

                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.CreerTable)) {
                    // Il n'est pas nécessaire de synchroniser ces vérifications
                    // car un protocole ne peut pas exécuter plus qu'une fonction
                    // à la fois, donc les valeurs ne peuvent ètre modifiées par
                    // deux threads à la fois
                    // Si le joueur n'est pas connecté au serveur de jeu, alors il
                    // y a une erreur
                    if (objJoueurHumain == null) {
                        // Le joueur ne peut pas accéder à la liste des joueurs
                        // s'il n'est pas connecté au serveur de jeu
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    } // Si le joueur n'est connecté à aucune salle, alors il ne
                    // peut pas créer de tables
                    else if (objJoueurHumain.obtenirSalleCourante() == null) {
                        // Le joueur ne peut pas créer de nouvelles tables
                        // s'il n'est pas dans une salle
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansSalle");
                    } //TODO: Il va falloir synchroniser cette validation lorsqu'on va
                    // avoir codé la commande SortirJoueurTable -> èa va ressembler au
                    // processus d'authentification
                    // Si le joueur est dans une table, alors il ne
                    // peut pas créer de tables, il faut qu'il sorte avant
                    else if (objJoueurHumain.obtenirPartieCourante() != null) {
                        // Le joueur ne peut pas créer de nouvelles tables
                        // s'il est déjà dans une table
                        objNoeudCommande.setAttribute("nom", "JoueurDansTable");
                    } else {
                        // Il n'y a pas eu d'erreurs
                        objNoeudCommande.setAttribute("type", "Reponse");

                        objNoeudCommande.setAttribute("nom", "NoTable");

                        // Déclaration d'une variable qui va contenir le temps
                        // de la partie que le client veut créer
                        int intTempsPartie = Integer.parseInt(obtenirValeurParametre(objNoeudCommandeEntree, "TempsPartie").getNodeValue());

                        int intNbLines;
                        try {

                            intNbLines = Integer.parseInt(obtenirValeurParametre(objNoeudCommandeEntree, "NbLines").getNodeValue());

                        } catch (NumberFormatException e) {

                            intNbLines = 8;

                        }

                        int intNbColumns;
                        try {

                            intNbColumns = Integer.parseInt(obtenirValeurParametre(objNoeudCommandeEntree, "NbColumns").getNodeValue());

                        } catch (NumberFormatException e) {

                            intNbColumns = 14;

                        }



                        String name = "";
                        if (obtenirValeurParametre(objNoeudCommandeEntree, "TableName") != null) //.getNodeValue()
                        {
                            name = obtenirValeurParametre(objNoeudCommandeEntree, "TableName").getNodeValue();
                        }

                        String type = "Course";  // default
                        if (obtenirValeurParametre(objNoeudCommandeEntree, "GameType") != null) //.getNodeValue()
                        {
                            type = obtenirValeurParametre(objNoeudCommandeEntree, "GameType").getNodeValue();
                        }

                        //System.out.println("Protocole - create table : " + intNbColumns + " " + intNbLines + " " + type);

                        // Appeler la méthode permettant de créer la nouvelle
                        // table et d'entrer le joueur dans cette table
                        int intNoTable = objJoueurHumain.obtenirSalleCourante().creerTable(objJoueurHumain,
                                intTempsPartie, true, name, intNbLines, intNbColumns, type);

                        name = objJoueurHumain.obtenirPartieCourante().obtenirTable().getTableName();

                        // Créer le noeud paramètre du numéro de la table
                        Element objNoeudParametreNoTable = objDocumentXMLSortie.createElement("parametre");

                        // Créer un noeud texte contenant le numéro de la table
                        Text objNoeudTexteNoTable = objDocumentXMLSortie.createTextNode(Integer.toString(intNoTable));

                        // Définir l'attribut type pour le noeud paramètre
                        objNoeudParametreNoTable.setAttribute("type", "NoTable");

                        // Ajouter le noeud texte au noeud paramètre
                        objNoeudParametreNoTable.appendChild(objNoeudTexteNoTable);

                        //***********************************************************
                        // Créer le noeud paramètre du nom de la table
                        Element objNoeudParametreNameTable = objDocumentXMLSortie.createElement("parametre");

                        // Créer un noeud texte contenant le nom de la table
                        Text objNoeudTexteNameTable = objDocumentXMLSortie.createTextNode(name);

                        // Définir l'attribut type pour le noeud paramètre
                        objNoeudParametreNameTable.setAttribute("type", "NameTable");

                        // Ajouter le noeud texte au noeud paramètre
                        objNoeudParametreNameTable.appendChild(objNoeudTexteNameTable);

                        // Ajouter le noeud paramètre au noeud de commande
                        objNoeudCommande.appendChild(objNoeudParametreNoTable);
                        objNoeudCommande.appendChild(objNoeudParametreNameTable);

                        // Créer le noeud pour le paramètre contenant la liste
                        // des personnages à retourner
                        Element objNoeudParametreColorPersonnage = objDocumentXMLSortie.createElement("parametre");

                        // On ajoute un attribut type qui va contenir le type
                        // du paramètre
                        objNoeudParametreColorPersonnage.setAttribute("type", "Color");

                        String color = objJoueurHumain.obtenirPartieCourante().getClothesColor();

                        // Créer un noeud texte contenant le role du joueur
                        Text objNoeudTexteColor = objDocumentXMLSortie.createTextNode(color);

                        // Ajouter le noeud texte au noeud du paramètre
                        objNoeudParametreColorPersonnage.appendChild(objNoeudTexteColor);

                        // Ajouter le noeud paramètre au noeud de commande dans
                        // le document de sortie
                        objNoeudCommande.appendChild(objNoeudParametreColorPersonnage);

                        //***************************
                        // Créer le noeud pour le paramètre contenant la liste
                        // des personnages à retourner
                        Element objNoeudParametreMaxNbPlayers = objDocumentXMLSortie.createElement("parametre");

                        // On ajoute un attribut type qui va contenir le type
                        // du paramètre
                        objNoeudParametreMaxNbPlayers.setAttribute("type", "MaxNbPlayers");

                        int number = objJoueurHumain.obtenirPartieCourante().obtenirTable().getMaxNbPlayers();

                        // Créer un noeud texte contenant le role du joueur
                        Text objNoeudTexteMaxNbPlayers = objDocumentXMLSortie.createTextNode(Integer.toString(number));

                        // Ajouter le noeud texte au noeud du paramètre
                        objNoeudParametreMaxNbPlayers.appendChild(objNoeudTexteMaxNbPlayers);

                        // Ajouter le noeud paramètre au noeud de commande dans
                        // le document de sortie
                        objNoeudCommande.appendChild(objNoeudParametreMaxNbPlayers);
                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.EntrerTable)) {
                    // Il n'est pas nécessaire de synchroniser ces vérifications
                    // car un protocole ne peut pas exécuter plus qu'une fonction
                    // à la fois, donc les valeurs ne peuvent ètre modifiées par
                    // deux threads à la fois

                    // Si le joueur n'est pas connecté au serveur de jeu, alors il
                    // y a une erreur
                    if (objJoueurHumain == null) {

                        // Le joueur ne peut pas entrer dans une table
                        // s'il n'est pas connecté au serveur de jeu
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    } // Si le joueur n'est connecté à aucune salle, alors il ne
                    // peut pas entrer dans une table
                    else if (objJoueurHumain.obtenirSalleCourante() == null) {
                        // Le joueur ne peut pas entrer dans une table
                        // s'il n'est pas dans une salle
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansSalle");
                    } //TODO: Il va falloir synchroniser cette validation lorsqu'on va
                    // avoir codé la commande SortirJoueurTable -> èa va ressembler au
                    // processus d'authentification
                    // Si le joueur est dans une table, alors il ne
                    // peut pas entrer dans une autre table sans sortir de celle
                    // dans laquelle il se trouve présentement
                    else if (objJoueurHumain.obtenirPartieCourante() != null) {
                        // Le joueur ne peut pas entrer dans une table
                        // s'il est déjà dans une table
                        objNoeudCommande.setAttribute("nom", "JoueurDansTable");
                    } else {
                        // Obtenir le numéro de la table dans laquelle le joueur
                        // veut entrer et le garder en mémoire dans une variable
                        int intNoTable = Integer.parseInt(obtenirValeurParametre(objNoeudCommandeEntree, "NoTable").getNodeValue());

                        // Déclaration d'une nouvelle liste de personnages
                        //TreeMap<String, Integer> lstPersonnageJoueurs = new TreeMap<String, Integer>();
                        // Déclaration d'une nouvelle liste de role des joueurs
                        //TreeMap<String, Integer> lstRoleJoueurs = new TreeMap<String, Integer>();

                        //test
                        JoueurHumain[] listInit = objJoueurHumain.obtenirSalleCourante().obtenirTable(intNoTable).remplirListePersonnageJoueurs();

                        // Appeler la méthode permettant d'entrer dans la
                        // table et garder son résultat dans une variable
                        String strResultatEntreeTable = objJoueurHumain.obtenirSalleCourante().entrerTable(objJoueurHumain,
                                intNoTable, true);


                        // Si le résultat de l'entrée dans la table est true alors le
                        // joueur est maintenant dans la table
                        if (strResultatEntreeTable.equals(ResultatEntreeTable.Succes)) {
                            // Il n'y a pas eu d'erreurs, mais on doit retourner
                            // la liste des joueurs avec leur idPersonnage
                            objNoeudCommande.setAttribute("type", "Reponse");

                            objNoeudCommande.setAttribute("nom", "ListePersonnageJoueurs");

                            // Créer le noeud pour le paramètre contenant la liste
                            // des personnages à retourner
                            Element objNoeudParametreListePersonnageJoueurs = objDocumentXMLSortie.createElement("parametre");

                            // On ajoute un attribut type qui va contenir le type
                            // du paramètre
                            objNoeudParametreListePersonnageJoueurs.setAttribute("type", "ListePersonnageJoueurs");


                            // Passer tous les personnages et créer un noeud pour
                            // chaque id de personnage et l'ajouter au noeud de paramètre
                            for (int i = 0; i < listInit.length; i++) {
                                // Créer le noeud pour le joueur courant
                                Element objNoeudPersonnage = objDocumentXMLSortie.createElement("personnage");

                                // Définir le nom d'utilisateur du joueur ainsi que le id du personnage
                                objNoeudPersonnage.setAttribute("nom", listInit[i].obtenirNomUtilisateur());
                                objNoeudPersonnage.setAttribute("idPersonnage", ((Integer) listInit[i].obtenirPartieCourante().obtenirIdPersonnage()).toString());
                                objNoeudPersonnage.setAttribute("role", ((Integer) listInit[i].getRole()).toString());
                                objNoeudPersonnage.setAttribute("clothesColor", listInit[i].obtenirPartieCourante().getClothesColor());

                                // Ajouter le noeud du personnage au noeud de paramètre
                                objNoeudParametreListePersonnageJoueurs.appendChild(objNoeudPersonnage);
                            }
                            /*

                            // Déclaration d'un compteur
                            int i = 1;
                            int MAX_NB_PLAYERS = objJoueurHumain.obtenirPartieCourante().obtenirTable().getMaxNbPlayers();

                            // Boucler tant qu'on n'a pas atteint le nombre maximal de
                            // joueurs moins le joueur courant car on ne le met pas dans la liste
                            while (listInit.length + i < MAX_NB_PLAYERS)
                            {
                            // Créer le noeud pour le joueur courant
                            Element objNoeudPersonnage = objDocumentXMLSortie.createElement("personnage");

                            // Définir le nom d'utilisateur du joueur ainsi que le id du personnage
                            objNoeudPersonnage.setAttribute("nom", "Inconnu" + i);
                            objNoeudPersonnage.setAttribute("idPersonnage", (new Integer(0)).toString());
                            objNoeudPersonnage.setAttribute("role",  (new Integer(0)).toString());
                            objNoeudPersonnage.setAttribute("clothesColor",  (new Integer(0).toString()));

                            // Ajouter le noeud du personnage au noeud de paramètre
                            objNoeudParametreListePersonnageJoueurs.appendChild(objNoeudPersonnage);
                            i++;
                            }*/

                            // Ajouter le noeud de paramètres au noeud de commande
                            objNoeudCommande.appendChild(objNoeudParametreListePersonnageJoueurs);

                            // Créer le noeud pour le paramètre contenant la liste
                            // des personnages à retourner
                            Element objNoeudParametreColorPersonnage = objDocumentXMLSortie.createElement("parametre");

                            // On ajoute un attribut type qui va contenir le type
                            // du paramètre
                            objNoeudParametreColorPersonnage.setAttribute("type", "Color");

                            // Créer un noeud texte contenant le role du joueur
                            Text objNoeudTexteColor = objDocumentXMLSortie.createTextNode(objJoueurHumain.obtenirPartieCourante().getClothesColor());

                            // Ajouter le noeud texte au noeud du paramètre
                            objNoeudParametreColorPersonnage.appendChild(objNoeudTexteColor);

                            // Ajouter le noeud paramètre au noeud de commande dans
                            // le document de sortie
                            objNoeudCommande.appendChild(objNoeudParametreColorPersonnage);
                        } else if (strResultatEntreeTable.equals(ResultatEntreeTable.TableNonExistante)) {
                            // La table n'existe plus
                            objNoeudCommande.setAttribute("nom", "TableNonExistante");
                        } else if (strResultatEntreeTable.equals(ResultatEntreeTable.TableComplete)) {
                            // La table est complète
                            objNoeudCommande.setAttribute("nom", "TableComplete");
                        } else {
                            // Une partie est déjà commencée
                            objNoeudCommande.setAttribute("nom", "PartieEnCours");
                        }
                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.QuitterTable)) {
                    // Si le joueur n'est pas connecté, alors il y a une erreur
                    if (objJoueurHumain == null) {
                        // Le joueur n'est pas connecté
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    } // Si le joueur n'est pas dans aucune salle, alors il y a
                    // une erreur
                    else if (objJoueurHumain.obtenirSalleCourante() == null) {
                        // Le joueur n'est pas dans aucune salle
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansSalle");
                    } //TODO: Il va falloir synchroniser cette validation lorsqu'on va
                    // avoir codé la commande SortirJoueurTable -> èa va ressembler au
                    // processus d'authentification
                    // Si le joueur n'est pas dans aucune table, alors il y a
                    // une erreur
                    else if (objJoueurHumain.obtenirPartieCourante() == null) {
                        // Le joueur n'est pas dans aucune table
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansTable");
                    } else {
                        // Appeler la méthode pour quitter la table
                        objJoueurHumain.obtenirPartieCourante().obtenirTable().quitterTable(objJoueurHumain, true, true);

                        // Il n'y a pas eu d'erreurs
                        objNoeudCommande.setAttribute("type", "Reponse");
                        objNoeudCommande.setAttribute("nom", "Ok");
                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.DemarrerMaintenant)) {
                    // Il n'est pas nécessaire de synchroniser ces vérifications
                    // car un protocole ne peut pas exécuter plus qu'une fonction
                    // à la fois, donc les valeurs ne peuvent ètre modifiées par
                    // deux threads à la fois
                    // Si le joueur n'est pas connecté au serveur de jeu, alors il
                    // y a une erreur
                    if (objJoueurHumain == null) {
                        // Le joueur ne peut pas démarrer une partie
                        // s'il n'est pas connecté au serveur de jeu
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    } // Si le joueur n'est connecté à aucune salle, alors il ne
                    // peut pas démarrer une partie
                    else if (objJoueurHumain.obtenirSalleCourante() == null) {
                        // Le joueur ne peut pas démarrer une partie
                        // s'il n'est pas dans une salle
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansSalle");
                    } //TODO: Il va falloir synchroniser cette validation lorsqu'on va
                    // avoir codé la commande SortirJoueurTable -> èa va ressembler au
                    // processus d'authentification
                    // Si le joueur n'est pas dans aucune table, alors il y a
                    // une erreur
                    else if (objJoueurHumain.obtenirPartieCourante() == null) {
                        // Le joueur ne peut pas démarrer une partie
                        // s'il n'est dans aucune table
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansTable");
                    } // On n'a pas besoin de valider qu'il n'y aucune partie de
                    // commencée, car le joueur doit obligatoirement ètre dans
                    // la table pour démarrer la partie et comme il ne peut entrer
                    // si une partie est en cours, alors c'est certain qu'il n'y
                    // aura pas de parties en cours
                    else {
                        // Obtenir le numéro Id du personnage choisi et le garder
                        // en mémoire dans une variable

                        //objLogger.info( GestionnaireMessages.message("protocole.personnage") + intIdPersonnage );

                        try {
                            // Obtenir le paramètre pour le joueur virtuel
                            // choix possible: "Aucun", "Facile", "Intermediaire", "Difficile"
                            String strParamJoueurVirtuel = null;
                            if (obtenirValeurParametre(objNoeudCommandeEntree, "NiveauJoueurVirtuel") != null) {
                                strParamJoueurVirtuel = obtenirValeurParametre(objNoeudCommandeEntree, "NiveauJoueurVirtuel").getNodeValue();
                                //System.out.println(strParamJoueurVirtuel);
                            } else {
                                // Valeur par défaut
                                strParamJoueurVirtuel = "Intermediaire";
                            }


                            // Appeler la méthode permettant de démarrer une partie
                            // et garder son résultat dans une variable
                            String strResultatDemarrerPartie = objJoueurHumain.obtenirPartieCourante().obtenirTable().demarrerMaintenant(objJoueurHumain,
                                    true, strParamJoueurVirtuel);

                            objLogger.info(GestionnaireMessages.message("protocole.resultat") + strResultatDemarrerPartie);

                            // Si le résultat du démarrage de partie est Succes alors le
                            // joueur est maintenant en attente
                            if (strResultatDemarrerPartie.equals(ResultatDemarrerPartie.Succes)) {
                                bolEnTrainDeJouer = true;

                                // Il n'y a pas eu d'erreurs
                                objNoeudCommande.setAttribute("type", "Reponse");
                                objNoeudCommande.setAttribute("nom", "DemarrerMaintenant");
                            } else if (strResultatDemarrerPartie.equals(ResultatDemarrerPartie.PartieEnCours)) {
                                // Il y avait déjà une partie en cours
                                objNoeudCommande.setAttribute("nom", "PartieEnCours");
                            } else {
                                objLogger.error(GestionnaireMessages.message("protocole.erreur_code") + strResultatDemarrerPartie);
                                objNoeudCommande.setAttribute("nom", "");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.DemarrerPartie)) {
                    // Il n'est pas nécessaire de synchroniser ces vérifications
                    // car un protocole ne peut pas exécuter plus qu'une fonction
                    // à la fois, donc les valeurs ne peuvent ètre modifiées par
                    // deux threads à la fois

                    // Si le joueur n'est pas connecté au serveur de jeu, alors il
                    // y a une erreur
                    if (objJoueurHumain == null) {
                        // Le joueur ne peut pas démarrer une partie
                        // s'il n'est pas connecté au serveur de jeu
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    } // Si le joueur n'est connecté à aucune salle, alors il ne
                    // peut pas démarrer une partie
                    else if (objJoueurHumain.obtenirSalleCourante() == null) {
                        // Le joueur ne peut pas démarrer une partie
                        // s'il n'est pas dans une salle
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansSalle");
                    } //TODO: Il va falloir synchroniser cette validation lorsqu'on va
                    // avoir codé la commande SortirJoueurTable -> èa va ressembler au
                    // processus d'authentification
                    // Si le joueur n'est pas dans aucune table, alors il y a
                    // une erreur
                    else if (objJoueurHumain.obtenirPartieCourante() == null) {
                        // Le joueur ne peut pas démarrer une partie
                        // s'il n'est dans aucune table
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansTable");
                    } // On n'a pas besoin de valider qu'il n'y aucune partie de
                    // commencée, car le joueur doit obligatoirement ètre dans
                    // la table pour démarrer la partie et comme il ne peut entrer
                    // si une partie est en cours, alors c'est certain qu'il n'y
                    // aura pas de parties en cours
                    else {
                        // Obtenir le numéro Id du personnage choisi et le garder
                        // en mémoire dans une variable
                        int intIdDessin = Integer.parseInt(obtenirValeurParametre(objNoeudCommandeEntree, "IdDessin").getNodeValue());

                        // Appeler la méthode permettant de démarrer une partie
                        // et garder son résultat dans une variable
                        String strResultatDemarrerPartie = objJoueurHumain.obtenirPartieCourante().obtenirTable().demarrerPartie(objJoueurHumain,
                                intIdDessin, true);
                        int idPersonnage = objJoueurHumain.obtenirPartieCourante().obtenirIdPersonnage();


                        // Si le résultat du démarrage de partie est Succes alors le
                        // joueur est maintenant en attente
                        if (strResultatDemarrerPartie.equals(ResultatDemarrerPartie.Succes)) {
                            bolEnTrainDeJouer = true;

                            // Il n'y a pas eu d'erreurs
                            objNoeudCommande.setAttribute("type", "Reponse");
                            objNoeudCommande.setAttribute("nom", "Ok");
                            objNoeudCommande.setAttribute("id", Integer.toString(idPersonnage));

                        } else if (strResultatDemarrerPartie.equals(ResultatDemarrerPartie.PartieEnCours)) {
                            // Il y avait déjà une partie en cours
                            objNoeudCommande.setAttribute("nom", "PartieEnCours");
                        } else {
                            // Le joueur était déjà en attente
                            objNoeudCommande.setAttribute("nom", "DejaEnAttente");
                        }

                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.DeplacerPersonnage)) {
                    // Faire la référence vers le noeud gardant l'information
                    // sur la nouvelle position du joueur
                    Node objNoeudNouvellePosition = obtenirValeurParametre(objNoeudCommandeEntree, "NouvellePosition");

                    // Obtenir la position x, y oè le joueur souhaite se déplacer
                    Point objNouvellePosition = new Point(Integer.parseInt(objNoeudNouvellePosition.getAttributes().getNamedItem("x").getNodeValue()), Integer.parseInt(objNoeudNouvellePosition.getAttributes().getNamedItem("y").getNodeValue()));

                    // Si le joueur n'est pas connecté au serveur de jeu, alors il
                    // y a une erreur
                    if (objJoueurHumain == null) {
                        // Le joueur ne peut pas déplacer son personnage
                        // s'il n'est pas connecté au serveur de jeu
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    } // Si le joueur n'est connecté à aucune salle, alors il ne
                    // peut pas déplacer son personnage
                    else if (objJoueurHumain.obtenirSalleCourante() == null) {
                        // Le joueur ne peut pas déplacer son personnage
                        // s'il n'est pas dans une salle
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansSalle");
                    } //TODO: Il va falloir synchroniser cette validation lorsqu'on va
                    // avoir codé la commande SortirJoueurTable -> èa va ressembler au
                    // processus d'authentification
                    // Si le joueur n'est pas dans aucune table, alors il y a
                    // une erreur
                    else if (objJoueurHumain.obtenirPartieCourante() == null) {
                        // Le joueur ne peut pas déplacer son personnage
                        // s'il n'est dans aucune table
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansTable");
                    } // Si la partie n'est pas commencée, alors il y a une erreur
                    else if (objJoueurHumain.obtenirPartieCourante().obtenirTable().estCommencee() == false) {

                        // Le joueur ne peut pas déplacer son personnage
                        // si la partie n'est pas commencée
                        objNoeudCommande.setAttribute("nom", "PartiePasDemarree");
                    } // Si une question a déjà été posée au client, alors il y a
                    // une erreur
                    else if (objJoueurHumain.obtenirPartieCourante().obtenirQuestionCourante() != null) {
                        // Le joueur ne peut pas déplacer son personnage
                        // si une question lui a déjà été posée
                        objNoeudCommande.setAttribute("nom", "QuestionPasRepondue");
                    } // Si le déplacement n'est pas permis, alors il y a une erreur
                    else if (objJoueurHumain.obtenirPartieCourante().deplacementEstPermis(objNouvellePosition) == false) {
                        // Le joueur ne peut pas déplacer son personnage
                        // si une question lui a déjà été posée
                        objNoeudCommande.setAttribute("nom", "DeplacementNonAutorise");
                    } else {
                        // Trouver la question à poser selon la difficulté et
                        // le type de case sur laquelle on veut se diriger
                        Question objQuestionAPoser = objJoueurHumain.obtenirPartieCourante().trouverQuestionAPoser(objNouvellePosition, true);

                        // Il n'y a pas eu d'erreurs
                        objNoeudCommande.setAttribute("type", "Reponse");
                        objNoeudCommande.setAttribute("nom", "Question");

                        // Créer le noeud paramètre de la question
                        Element objNoeudParametreQuestion = objDocumentXMLSortie.createElement("parametre");

                        // Définir les attributs pour le noeud paramètre et question

                        objNoeudParametreQuestion.setAttribute("type", "Question");

                        // Si aucune question n'a été trouvée, alors c'est que
                        // le joueur ne s'est pas déplacé, on ne renvoit donc
                        // que le paramètre sans la question, sinon on renvoit
                        // également l'information sur la question
                        if (objQuestionAPoser != null) {
                            // Créer un noeud texte contenant l'information sur la question
                            Element objNoeudQuestion = objDocumentXMLSortie.createElement("question");
                            objNoeudQuestion.setAttribute("id", Integer.toString(objQuestionAPoser.obtenirCodeQuestion()));
                            objNoeudQuestion.setAttribute("type", TypeQuestion.getValue(objQuestionAPoser.obtenirTypeQuestion()));
                            objNoeudQuestion.setAttribute("url", objQuestionAPoser.obtenirURLQuestion());


                            //collect players questions for DB
                            collectPlayerAnswers(objQuestionAPoser);
                            // for statistics
                            objJoueurHumain.obtenirPartieCourante().incrementQuestions();

                            // Ajouter le noeud question au noeud paramètre
                            objNoeudParametreQuestion.appendChild(objNoeudQuestion);
                        }

                        // Ajouter le noeud paramètre au noeud de commande
                        objNoeudCommande.appendChild(objNoeudParametreQuestion);

                        //*******************************************************
                        lastQuestionTime = objJoueurHumain.obtenirPartieCourante().obtenirTable().obtenirTempsRestant();
                        //System.out.println("Protocol for : " + lastQuestionTime);
                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.RepondreQuestion)) {
                    // Obtenir la réponse du joueur
                    String strReponse = obtenirValeurParametre(objNoeudCommandeEntree, "Reponse").getNodeValue();

                    //objJoueurHumain.obtenirPartieCourante().obtenirQuestionCourante();
                    //collect players answers for DB
                    if (objJoueurHumain.obtenirPartieCourante().obtenirQuestionCourante() != null) {
                        collectPlayerAnswers(strReponse, Question.reponseEstValide(strReponse,
                                objJoueurHumain.obtenirPartieCourante().obtenirQuestionCourante().getStringAnswer()));
                    } else {
                        objLogger.error(GestionnaireMessages.message("boite.pas_de_question"));
                    }



                    int timer = lastQuestionTime - objJoueurHumain.obtenirPartieCourante().obtenirTable().obtenirTempsRestant();
                    //collect time of reponse for DB
                    collectPlayerAnswers(timer);
                    //System.out.println("reponse :Protocol :" + timer);


                    // Si le joueur n'est pas connecté au serveur de jeu, alors il
                    // y a une erreur
                    if (objJoueurHumain == null) {
                        // Le joueur ne peut pas répondre à une question
                        // s'il n'est pas connecté au serveur de jeu
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    } // Si le joueur n'est connecté à aucune salle, alors il ne
                    // peut pas répondre à aucune question
                    else if (objJoueurHumain.obtenirSalleCourante() == null) {
                        // Le joueur ne peut pas répondre à aucune question
                        // s'il n'est pas dans une salle
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansSalle");
                    } //TODO: Il va falloir synchroniser cette validation lorsqu'on va
                    // avoir codé la commande SortirJoueurTable -> èa va ressembler au
                    // processus d'authentification
                    // Si le joueur n'est pas dans aucune table, alors il y a
                    // une erreur
                    else if (objJoueurHumain.obtenirPartieCourante() == null) {
                        // Le joueur ne peut pas répondre à aucune question
                        // s'il n'est dans aucune table
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansTable");
                    } // Si la partie n'est pas commencée, alors il y a une erreur
                    else if (objJoueurHumain.obtenirPartieCourante().obtenirTable().estCommencee() == false) {
                        // Le joueur ne peut pas répondre à aucune question
                        // si la partie n'est pas commencée
                        objNoeudCommande.setAttribute("nom", "PartiePasDemarree");
                    } // Si une question n'a pas déjà été posée au client, alors
                    // il y a une erreur
                    else if (objJoueurHumain.obtenirPartieCourante().obtenirQuestionCourante() == null) {
                        // Le joueur ne peut pas répondre à une question
                        // si une question ne lui a pas déjà été posée
                        objNoeudCommande.setAttribute("nom", "DeplacementNonDemande");
                    } else {
                        // Vérifier si la réponse est bonne et obtenir un objet
                        // contenant toutes les informations à retourner
                        RetourVerifierReponseEtMettreAJourPlateauJeu objRetour = objJoueurHumain.obtenirPartieCourante().verifierReponseEtMettreAJourPlateauJeu(strReponse, true);

                        //objJoueurHumain.obtenirPartieCourante()
                        // Il n'y a pas eu d'erreurs
                        objNoeudCommande.setAttribute("type", "Reponse");
                        objNoeudCommande.setAttribute("nom", "Deplacement");

                        // Créer les noeuds paramètres et enfants et construire
                        // le document XML de retour
                        Element objNoeudParametreDeplacementAccepte = objDocumentXMLSortie.createElement("parametre");
                        Element objNoeudParametrePointage = objDocumentXMLSortie.createElement("parametre");
                        Element objNoeudParametreArgent = objDocumentXMLSortie.createElement("parametre");
                        Element objNoeudParametreVisibility = objDocumentXMLSortie.createElement("parametre");
                        Element objNoeudParametreBonus = objDocumentXMLSortie.createElement("parametre");


                        Text objNoeudTexteDeplacementAccepte = objDocumentXMLSortie.createTextNode(Boolean.toString(objRetour.deplacementEstAccepte()));
                        Text objNoeudTextePointage = objDocumentXMLSortie.createTextNode(Integer.toString(objRetour.obtenirNouveauPointage()));
                        Text objNoeudTexteArgent = objDocumentXMLSortie.createTextNode(Integer.toString(objRetour.obtenirNouvelArgent()));
                        Text objNoeudTexteVisibility = objDocumentXMLSortie.createTextNode(Integer.toString(objJoueurHumain.obtenirPartieCourante().getMoveVisibility()));
                        Text objNoeudTexteBonus = objDocumentXMLSortie.createTextNode(Integer.toString(objJoueurHumain.obtenirPartieCourante().getTournamentBonus()));

                        //System.out.println(":ICI Bonus retour reponse : " + objJoueurHumain.obtenirPartieCourante().getTournamentBonus());

                        objNoeudParametreDeplacementAccepte.setAttribute("type", "DeplacementAccepte");
                        objNoeudParametrePointage.setAttribute("type", "Pointage");
                        objNoeudParametreArgent.setAttribute("type", "Argent");
                        objNoeudParametreVisibility.setAttribute("type", "MoveVisibility");
                        objNoeudParametreBonus.setAttribute("type", "Bonus");


                        objNoeudParametreDeplacementAccepte.appendChild(objNoeudTexteDeplacementAccepte);
                        objNoeudParametrePointage.appendChild(objNoeudTextePointage);
                        objNoeudParametreArgent.appendChild(objNoeudTexteArgent);
                        objNoeudParametreVisibility.appendChild(objNoeudTexteVisibility);
                        objNoeudParametreBonus.appendChild(objNoeudTexteBonus);


                        objNoeudCommande.appendChild(objNoeudParametreDeplacementAccepte);

                        // Si le déplacement est accepté, alors on crée les
                        // noeuds spécifiques au succès de la réponse
                        if (objRetour.deplacementEstAccepte() == true) {
                            // work with movevisibility

                            Element objNoeudParametreObjetRamasse = objDocumentXMLSortie.createElement("parametre");
                            Element objNoeudParametreObjetSubi = objDocumentXMLSortie.createElement("parametre");
                            Element objNoeudParametreNouvellePosition = objDocumentXMLSortie.createElement("parametre");
                            Element objNoeudParametreCollision = objDocumentXMLSortie.createElement("parametre");


                            objNoeudParametreObjetRamasse.setAttribute("type", "ObjetRamasse");
                            objNoeudParametreObjetSubi.setAttribute("type", "ObjetSubi");
                            objNoeudParametreNouvellePosition.setAttribute("type", "NouvellePosition");
                            objNoeudParametreCollision.setAttribute("type", "Collision");


                            // S'il y a un objet qui a été ramassé, alors on peut
                            // créer son noeud enfant, sinon on n'en crée pas
                            if (objRetour.obtenirObjetRamasse() != null) {
                                Element objNoeudObjetRamasse = objDocumentXMLSortie.createElement("objetRamasse");
                                objNoeudObjetRamasse.setAttribute("id", Integer.toString(objRetour.obtenirObjetRamasse().obtenirId()));
                                objNoeudObjetRamasse.setAttribute("type", objRetour.obtenirObjetRamasse().getClass().getSimpleName());
                                objNoeudParametreObjetRamasse.appendChild(objNoeudObjetRamasse);
                                objNoeudCommande.appendChild(objNoeudParametreObjetRamasse);
                                objJoueurHumain.obtenirPartieCourante().ajouterObjetUtilisableListe(objRetour.obtenirObjetRamasse());
                            }

                            // Si le joueur a subi un objet, alors on peut créer
                            // son noeud enfant, sinon on n'en crée pas
                            if (objRetour.obtenirObjetSubi() != null) {
                                Element objNoeudObjetSubi = objDocumentXMLSortie.createElement("objetSubi");
                                objNoeudObjetSubi.setAttribute("id", Integer.toString(objRetour.obtenirObjetSubi().obtenirId()));
                                objNoeudObjetSubi.setAttribute("type", objRetour.obtenirObjetSubi().getClass().getSimpleName());
                                objNoeudParametreObjetSubi.appendChild(objNoeudObjetSubi);
                                objNoeudCommande.appendChild(objNoeudParametreObjetSubi);
                            }

                            // Si le joueur est arrivé sur un magasin, alors on lui
                            // renvoie la liste des objets que le magasin vend
                            if (objRetour.obtenirCollision().equals("magasin")) {
                                // Aller chercher une référence vers le magasin
                                // que le joueur visite
                                Magasin objMagasin = objRetour.obtenirMagasin();

                                // Créer la liste des objets directement dans le
                                // document XML de sortie
                                creerListeObjetsMagasin(objMagasin, objDocumentXMLSortie, objNoeudCommande);

                                /*
                                Element objNoeudParametreTypeMagasin = objDocumentXMLSortie.createElement("parametre");
                                objNoeudParametreTypeMagasin.setAttribute("type", "TypeMagasin");
                                Text objNoeudTexteTypeMagasin = objDocumentXMLSortie.createTextNode(Integer.toString(objMagasin.type));
                                objNoeudParametreTypeMagasin.appendChild(objNoeudTexteTypeMagasin);
                                objNoeudCommande.appendChild(objNoeudParametreTypeMagasin);
                                 */
                            }

                            Element objNoeudNouvellePosition = objDocumentXMLSortie.createElement("position");
                            objNoeudNouvellePosition.setAttribute("x", Integer.toString(objRetour.obtenirNouvellePosition().x));
                            objNoeudNouvellePosition.setAttribute("y", Integer.toString(objRetour.obtenirNouvellePosition().y));
                            objNoeudParametreNouvellePosition.appendChild(objNoeudNouvellePosition);
                            objNoeudCommande.appendChild(objNoeudParametreNouvellePosition);
                            Text objNoeudTexteCollision = objDocumentXMLSortie.createTextNode(objRetour.obtenirCollision());
                            objNoeudParametreCollision.appendChild(objNoeudTexteCollision);
                            objNoeudCommande.appendChild(objNoeudParametreCollision);
                        } else {
                            // Créer le noeud explications
                            Element objNoeudParametreExplication = objDocumentXMLSortie.createElement("parametre");
                            Text objNoeudTexteExplication = objDocumentXMLSortie.createTextNode(objRetour.obtenirExplications());
                            objNoeudParametreExplication.setAttribute("type", "Explication");
                            objNoeudParametreExplication.appendChild(objNoeudTexteExplication);
                            objNoeudCommande.appendChild(objNoeudParametreExplication);
                            objNoeudCommande.appendChild(objNoeudParametreVisibility);
                        }

                        // Ajouter les noeuds paramètres au noeud de commande
                        objNoeudCommande.appendChild(objNoeudParametrePointage);
                        objNoeudCommande.appendChild(objNoeudParametreArgent);
                        objNoeudCommande.appendChild(objNoeudParametreVisibility);
                        objNoeudCommande.appendChild(objNoeudParametreBonus);
                    }
                } //////////////////////////////////////////////////////////////////////////////////
                else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.CancelQuestion)) {

                    // Si le joueur n'est pas connecté au serveur de jeu, alors il
                    // y a une erreur
                    if (objJoueurHumain == null) {
                        // Le joueur ne peut pas répondre à une question
                        // s'il n'est pas connecté au serveur de jeu
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    } // Si le joueur n'est connecté à aucune salle, alors il ne
                    // peut pas répondre à aucune question
                    else if (objJoueurHumain.obtenirSalleCourante() == null) {

                        // Le joueur ne peut pas répondre à aucune question
                        // s'il n'est pas dans une salle
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansSalle");
                    } //TODO: Il va falloir synchroniser cette validation lorsqu'on va
                    // avoir codé la commande SortirJoueurTable -> èa va ressembler au
                    // processus d'authentification
                    // Si le joueur n'est pas dans aucune table, alors il y a
                    // une erreur
                    else if (objJoueurHumain.obtenirPartieCourante() == null) {
                        // Le joueur ne peut pas répondre à aucune question
                        // s'il n'est dans aucune table
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansTable");
                    } // Si la partie n'est pas commencée, alors il y a une erreur
                    else if (objJoueurHumain.obtenirPartieCourante().obtenirTable().estCommencee() == false) {
                        // Le joueur ne peut pas répondre à aucune question
                        // si la partie n'est pas commencée
                        objNoeudCommande.setAttribute("nom", "PartiePasDemarree");
                    } else {

                        objJoueurHumain.obtenirPartieCourante().cancelPosedQuestion(true);

                        //	Il n'y a pas eu d'erreurs
                        objNoeudCommande.setAttribute("type", "Reponse");
                        objNoeudCommande.setAttribute("nom", "OK");

                    }
                } /////////////////////////////////////////////////////////////////////////////////
                else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.Pointage)) {
                    // Obtenir pointage
                    int pointage = Integer.parseInt(obtenirValeurParametre(objNoeudCommandeEntree, "Pointage").getNodeValue());

                    // Si le joueur n'est pas connecté au serveur de jeu, alors il
                    // y a une erreur
                    if (objJoueurHumain == null) {
                        // Le joueur ne peut pas répondre à une question
                        // s'il n'est pas connecté au serveur de jeu
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    } // Si le joueur n'est connecté à aucune salle, alors il ne
                    // peut pas répondre à aucune question
                    else if (objJoueurHumain.obtenirSalleCourante() == null) {

                        // Le joueur ne peut pas répondre à aucune question
                        // s'il n'est pas dans une salle
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansSalle");
                    } //TODO: Il va falloir synchroniser cette validation lorsqu'on va
                    // avoir codé la commande SortirJoueurTable -> èa va ressembler au
                    // processus d'authentification
                    // Si le joueur n'est pas dans aucune table, alors il y a
                    // une erreur
                    else if (objJoueurHumain.obtenirPartieCourante() == null) {
                        // Le joueur ne peut pas répondre à aucune question
                        // s'il n'est dans aucune table
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansTable");
                    } // Si la partie n'est pas commencée, alors il y a une erreur
                    else if (objJoueurHumain.obtenirPartieCourante().obtenirTable().estCommencee() == false) {
                        // Le joueur ne peut pas répondre à aucune question
                        // si la partie n'est pas commencée
                        objNoeudCommande.setAttribute("nom", "PartiePasDemarree");
                    } else {
                        int nouveauPointage = objJoueurHumain.obtenirPartieCourante().obtenirPointage();
                        nouveauPointage += pointage;
                        objJoueurHumain.obtenirPartieCourante().definirPointage(nouveauPointage);
                        objJoueurHumain.obtenirPartieCourante().setPointsFinalTime(objJoueurHumain.obtenirPartieCourante().obtenirTable().obtenirTempsRestant());

                        //	Il n'y a pas eu d'erreurs
                        objNoeudCommande.setAttribute("type", "Reponse");
                        objNoeudCommande.setAttribute("nom", "Pointage");

                        Element objNoeudParametrePointage = objDocumentXMLSortie.createElement("parametre");
                        Text objNoeudTextePointage = objDocumentXMLSortie.createTextNode(Integer.toString(nouveauPointage));
                        objNoeudParametrePointage.setAttribute("type", "Pointage");
                        objNoeudParametrePointage.appendChild(objNoeudTextePointage);
                        objNoeudCommande.appendChild(objNoeudParametrePointage);

                        // Préparer un événement pour les autres joueurs de la table
                        // pour qu'il se tienne à jour du pointage de ce joueur
                        objJoueurHumain.obtenirPartieCourante().obtenirTable().preparerEvenementMAJPointage(objJoueurHumain.obtenirNomUtilisateur(),
                                objJoueurHumain.obtenirPartieCourante().obtenirPointage());
                    }
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.Argent)) {
                    // Obtenir argent
                    int argent = Integer.parseInt(obtenirValeurParametre(objNoeudCommandeEntree, "Argent").getNodeValue());

                    // Si le joueur n'est pas connecté au serveur de jeu, alors il y a une erreur
                    if (objJoueurHumain == null) {
                        // Le joueur ne peut pas répondre à une question
                        // s'il n'est pas connecté au serveur de jeu
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    } // Si le joueur n'est connecté à aucune salle, alors il ne
                    // peut pas répondre à aucune question
                    else if (objJoueurHumain.obtenirSalleCourante() == null) {
                        // Le joueur ne peut pas répondre à aucune question
                        // s'il n'est pas dans une salle
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansSalle");
                    } //TODO: Il va falloir synchroniser cette validation lorsqu'on va
                    // avoir codé la commande SortirJoueurTable -> èa va ressembler au
                    // processus d'authentification
                    // Si le joueur n'est pas dans aucune table, alors il y a
                    // une erreur
                    else if (objJoueurHumain.obtenirPartieCourante() == null) {
                        // Le joueur ne peut pas répondre à aucune question
                        // s'il n'est dans aucune table
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansTable");
                    } // Si la partie n'est pas commencée, alors il y a une erreur
                    else if (objJoueurHumain.obtenirPartieCourante().obtenirTable().estCommencee() == false) {

                        // Le joueur ne peut pas répondre à aucune question
                        // si la partie n'est pas commencée
                        objNoeudCommande.setAttribute("nom", "PartiePasDemarree");
                    } else {
                        int nouvelArgent = objJoueurHumain.obtenirPartieCourante().obtenirArgent();
                        nouvelArgent += argent;
                        objJoueurHumain.obtenirPartieCourante().definirArgent(nouvelArgent);

                        //	Il n'y a pas eu d'erreurs
                        objNoeudCommande.setAttribute("type", "Reponse");
                        objNoeudCommande.setAttribute("nom", "Argent");

                        Element objNoeudParametreArgent = objDocumentXMLSortie.createElement("parametre");
                        Text objNoeudTexteArgent = objDocumentXMLSortie.createTextNode(Integer.toString(nouvelArgent));
                        objNoeudParametreArgent.setAttribute("type", "Argent");
                        objNoeudParametreArgent.appendChild(objNoeudTexteArgent);
                        objNoeudCommande.appendChild(objNoeudParametreArgent);

                        // Préparer un événement pour les autres joueurs de la table
                        // pour qu'il se tienne à jour de l'argent de ce joueur
                        objJoueurHumain.obtenirPartieCourante().obtenirTable().preparerEvenementMAJArgent(objJoueurHumain.obtenirNomUtilisateur(),
                                objJoueurHumain.obtenirPartieCourante().obtenirArgent());
                    }

                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.UtiliserObjet)) {
                    traiterCommandeUtiliserObjet(objNoeudCommandeEntree, objNoeudCommande, objDocumentXMLEntree, objDocumentXMLSortie, bolDoitRetournerCommande);
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.AcheterObjet)) {
                    traiterCommandeAcheterObjet(objNoeudCommandeEntree, objNoeudCommande, objDocumentXMLEntree, objDocumentXMLSortie, bolDoitRetournerCommande);
                } else if (objNoeudCommandeEntree.getAttribute("nom").equals(Commande.ChatMessage)) {
                    // Si le joueur n'est pas connecté au serveur de jeu
                    if (objJoueurHumain == null) {
                        objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
                    } // Si le joueur n'est connecté à aucune salle
                    else if (objJoueurHumain.obtenirSalleCourante() == null) {
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansSalle");
                    } // Si le joueur n'est pas dans aucune table
                    else if (objJoueurHumain.obtenirPartieCourante() == null) {
                        objNoeudCommande.setAttribute("nom", "JoueurPasDansTable");
                    } else if (!this.obtenirJoueurHumain().obtenirPartieCourante().obtenirTable().getRegles().obtenirPermetChat()) {
                        objNoeudCommande.setAttribute("nom", "ChatNonPermis");
                    } else {
                        //  Il n'y a pas eu d'erreurs
                        objNoeudCommande.setAttribute("type", "OK");

                        // Obtenir le message à envoyer à tous et le nom du joueur qui l'envoie
                        String messageAEnvoyer = obtenirValeurParametre(objNoeudCommandeEntree, "messageAEnvoyer").getNodeValue();
                        String nomJoueur = this.obtenirJoueurHumain().obtenirNomUtilisateur();

                        // On prépare l'événement qui enverra le message à tous
                        this.obtenirJoueurHumain().obtenirPartieCourante().obtenirTable().preparerEvenementMessageChat(nomJoueur, messageAEnvoyer);
                    }
                }
            }
        }
        Moniteur.obtenirInstance().fin();

        // Si on doit retourner une commande alors on ajoute le noeud de commande
        // et on retourne le code XML de la commande. Si le numéro de commande
        // n'avait pas été généré, alors on le génère
        if (bolDoitRetournerCommande == true) {
            // Si le numéro de commande à envoyer au client n'a pas encore
            // été défini, alors on le définit, puis on ajoute l'attribut
            // no du noeud de commande
            if (intNumeroCommandeReponse == -1) {
                // Générer un nouveau numéro de commande à renvoyer
                genererNumeroReponse();
            }

            // Définir le numéro de la commande à retourner
            objNoeudCommande.setAttribute("no", Integer.toString(intNumeroCommandeReponse));

            // Ajouter le noeud de commande au noeud racine dans le document de sortie
            objDocumentXMLSortie.appendChild(objNoeudCommande);
            if (objNoeudCommande.getAttribute("nom").equals("CommandeNonReconnue")) {
                System.out.println("AHHHHHHHHHHHHH " + objNoeudCommandeEntree.getAttribute("nom"));
            }
            // Retourner le document XML ne contenant pas l'entète XML ajoutée
            // par défaut par le transformateur
            return UtilitaireXML.transformerDocumentXMLEnString(objDocumentXMLSortie);
        } else {
            // Si on ne doit rien retourner, alors on retourne null
            return null;
        }
    }// fin méthode

    /**
     * Cette méthode permet d'envoyer le message passé en paramètre au
     * client (joueur). Deux threads ne peuvent écrire sur le socket en mème
     * temps.
     *
     * @param String message : le message à envoyer au client
     * @throws IOException : Si on ne peut pas obtenir l'accès en
     * 						 écriture sur le canal de communication
     */
    public void envoyerMessage(String message) throws IOException {
        Moniteur.obtenirInstance().debut("ProtocoleJoueur.envoyerMessage");

        // Synchroniser cette partie de code pour empècher 2 threads d'envoyer
        // un message en mème temps sur le canal d'envoi du socket
        synchronized (objSocketJoueur) {
            // Créer le canal qui permet d'envoyer des données sur le canal
            // de communication entre le client et le serveur
            OutputStream objCanalEnvoi = objSocketJoueur.getOutputStream();

            String chainetemp = UtilitaireEncodeurDecodeur.encodeToUTF8(message);

            if (chainetemp.contains("ping") == false) {
                objLogger.info(GestionnaireMessages.message("protocole.message_envoye") + chainetemp);
            }
            // ƒcrire le message sur le canal d'envoi au client
            objCanalEnvoi.write(message.getBytes("UTF8"));

            // ƒcrire le byte 0 sur le canal d'envoi pour signifier la fin du message
            objCanalEnvoi.write((byte) 0);

            // Envoyer le message sur le canal d'envoi
            objCanalEnvoi.flush();
            objLogger.info(GestionnaireMessages.message("protocole.confirmation") + objSocketJoueur.getInetAddress().toString());
        }
        Moniteur.obtenirInstance().fin();

    }// fin méthode

    /**
     * Cette méthode permet de déterminer si le noeud de commande passé en
     * paramètres ne contient que des paramètres valides et que chacun de
     * ces paramètres contient bien ce qu'il doit contenir. On suppose que le
     * noeud passé en paramètres est bel et bien un noeud de commande et qu'il
     * possède un attribut nom.
     *
     * @param Element noeudCommande : le noeud de comande à valider
     * @return boolean : true si le noeud de commande et tous ses enfants sont
     * 				     	  corrects  false sinon
     */
    private boolean commandeEstValide(Element noeudCommande) {
        // Déclaration d'une variable qui va permettre de savoir si la
        // commande est valide ou non
        boolean bolCommandeValide = false;

        // Si le nom de la commande est Connexion, alors il doit y avoir
        // 3 paramètres correspondants au nom d'utilisateur du joueur et
        // à son mot de passe et la langue
        if (noeudCommande.getAttribute("nom").equals(Commande.Connexion)) {
            // Si le nombre d'enfants du noeud de commande est de 3, alors
            // le nombre de paramètres est correct et on peut continuer
            if (noeudCommande.getChildNodes().getLength() == 3) {
                // Déclarer une variable qui va permettre de savoir si les
                // noeuds enfants sont valides
                boolean bolNoeudValide = true;

                // Déclaration d'un compteur
                int i = 0;

                // Passer tous les noeuds enfants et vérifier qu'ils sont bien
                // des paramètres avec le type approprié
                while (i < noeudCommande.getChildNodes().getLength() &&
                        bolNoeudValide == true) {
                    // Faire la référence vers le noeud enfant courant
                    Node objNoeudCourant = noeudCommande.getChildNodes().item(i);

                    // Si le noeud courant n'est pas un paramètre, ou qu'il n'a
                    // pas exactement 1 attribut, ou que le nom de cet attribut
                    // n'est pas type, ou que le noeud n'a pas de valeurs, alors
                    // il y a une erreur dans la structure
                    if (objNoeudCourant.getNodeName().equals("parametre") == false ||
                            objNoeudCourant.getAttributes().getLength() != 1 ||
                            objNoeudCourant.getAttributes().getNamedItem("type") == null ||
                            (objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("NomUtilisateur") == false &&
                            objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("Langue") == false &&
                            //objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("GameType") == false &&
                            objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("MotDePasse") == false) ||
                            objNoeudCourant.getChildNodes().getLength() != 1 ||
                            objNoeudCourant.getChildNodes().item(0).getNodeName().equals("#text") == false) {
                        bolNoeudValide = false;
                    }

                    i++;
                }
                // Si les enfants du noeud courant sont tous valides alors la
                // commande est valide
                bolCommandeValide = bolNoeudValide;
            }
        } // Si le nom de la commande est ConnexionProf, alors il doit y avoir
        // 3 paramètres correspondants au nom d'utilisateur du joueur, à son mot
        // de passe et à la langue pour la connexion.
        else if (noeudCommande.getAttribute("nom").equals(Commande.ConnexionProf)) {
            NodeList noeudsParametre = noeudCommande.getChildNodes();
            // Si le nombre d'enfants du noeud de commande est de 3, alors
            // le nombre de paramètres est correct et on peut continuer
            if (noeudsParametre.getLength() == 3) {
                int hash = 0;
                // Passer tous les noeuds enfants et vérifier qu'ils sont bien
                // des paramètres avec le type approprié
                for(int i=0; i<3; i++) {
                    Node objNoeudParam = noeudsParametre.item(i);
                    if (!objNoeudParam.getNodeName().equals("parametre")) break;
                    if (objNoeudParam.getAttributes().getLength() != 1) break;
                    Node paramType = objNoeudParam.getAttributes().getNamedItem("type");
                    if (paramType == null) break;
                    String paramValue = paramType.getNodeValue();
                    if (paramValue.equals("NomUtilisateur")) { hash |= 0x1; continue; }
                    if (paramValue.equals("MotDePasse")) { hash |= 0x2; continue; }
                    if (paramValue.equals("Langue")) {hash |= 0x4;  continue; }
                    break;
                }
                // Si les enfants du noeudCommande sont tous valides alors la
                // commande est valide.  Un hash de 0x7 veut dire que les trois
                // parametres sont bel et bien different (un hash différent pourrait
                // être calculé par exemple si le paramètre 'MotDePasse' est présent
                // deux fois)
                bolCommandeValide = (hash == 0x7);
            }
        } // Si le nom de la commande est NePasRejoindrePartie, alors il ne doit pas y avoir
        // de paramètres
        else if (noeudCommande.getAttribute("nom").equals(Commande.NePasRejoindrePartie)) {
            // Si le nombre d'enfants du noeud de commande est de 0, alors
            // il n'y a vraiment aucun paramètres
            if (noeudCommande.getChildNodes().getLength() == 0) {
                bolCommandeValide = true;
            }
        } // Si le nom de la commande est RejoindrePartie, alors il ne doit pas y avoir
        // de paramètres
        else if (noeudCommande.getAttribute("nom").equals(Commande.RejoindrePartie)) {
            // Si le nombre d'enfants du noeud de commande est de 0, alors
            // il n'y a vraiment aucun paramètres
            if (noeudCommande.getChildNodes().getLength() == 0) {
                bolCommandeValide = true;
            }
        } // Si le nom de la commande est Deconnexion, alors il ne doit pas y avoir
        // de paramètres
        else if (noeudCommande.getAttribute("nom").equals(Commande.Deconnexion)) {
            // Si le nombre d'enfants du noeud de commande est de 0, alors
            // il n'y a vraiment aucun paramètres
            if (noeudCommande.getChildNodes().getLength() == 0) {
                bolCommandeValide = true;
            }
        } // Si le nom de la commande est ObtenirListeJoueurs, alors il ne doit
        // pas y avoir de paramètres
        else if (noeudCommande.getAttribute("nom").equals(Commande.ObtenirListeJoueurs)) {
            // Si le nombre d'enfants du noeud de commande est de 0, alors
            // il n'y a vraiment aucun paramètres
            if (noeudCommande.getChildNodes().getLength() == 0) {
                bolCommandeValide = true;
            }
        } // Si le nom de la commande est ObtenirListeSalles, alors il doit y avoir 1 paramètre
        else if (noeudCommande.getAttribute("nom").equals(Commande.ObtenirListeSalles)) {
            // Si le nombre d'enfants du noeud de commande est de 1, alors c'est correct
            if (noeudCommande.getChildNodes().getLength() == 1) {
                bolCommandeValide = true;
            }
        } // Si le nom de la commande est ObtenirListeSalles, alors il doit y avoir 0 paramètre
        else if (noeudCommande.getAttribute("nom").equals(Commande.ObtenirListeSallesProf)) {
            // Si le nombre d'enfants du noeud de commande est 0, alors c'est correct
            if (noeudCommande.getChildNodes().getLength() == 0) {
                bolCommandeValide = true;
            }
        } // Si le nom de la commande est ObtenirListeSallesRetour, alors il doit y avoir 2 paramètres
        else if (noeudCommande.getAttribute("nom").equals(Commande.ObtenirListeSallesRetour)) {
            // Si le nombre d'enfants du noeud de commande est de 1, alors c'est correct
            if (noeudCommande.getChildNodes().getLength() == 1) {
                bolCommandeValide = true;
            }
        } // we have 3 parameteres in the commande Node
        else if (noeudCommande.getAttribute("nom").equals(Commande.ReportBugQuestion)) {
            if (noeudCommande.getChildNodes().getLength() == 1) {
                bolCommandeValide = true;
            }
        } // TODO
        else if (noeudCommande.getAttribute("nom").equals(Commande.CreateRoom)) {
            NodeList noeudsParametre = noeudCommande.getChildNodes();
            TreeSet<String> paramFound = new TreeSet<String>();
            TreeSet<String> langFound = new TreeSet<String>();
            bolCommandeValide = true;
            for (int i=0; bolCommandeValide && i<noeudsParametre.getLength(); i++) {
                Node objNoeudParam = noeudsParametre.item(i);
                if (!objNoeudParam.getNodeName().equals("parametre")) { bolCommandeValide = false; break; }
                Node paramType,paramLid;
                String strType="", strLid="";
                switch(objNoeudParam.getAttributes().getLength())
                {
                    case 1:
                       paramType = objNoeudParam.getAttributes().getNamedItem("type");
                       if (paramType == null) break;
                       strType = paramType.getNodeValue();
                       if (strType.equals("beginDate") && paramFound.add("beginDate")) continue;
                       if (strType.equals("endDate") && paramFound.add("endDate")) continue;
                       if (strType.equals("password") && paramFound.add("password")) continue;
                       if (strType.equals("masterTime") && paramFound.add("masterTime")) continue;
                       if (strType.equals("keywordIds") && paramFound.add("keywordIds")) continue;
                       if (strType.equals("gameTypeIds") && paramFound.add("gameTypeIds")) continue;
                       break;
                    case 2:
                        paramType = objNoeudParam.getAttributes().getNamedItem("type");
                        if (paramType == null) break;
                        paramLid = objNoeudParam.getAttributes().getNamedItem("language_id");
                        if (paramLid == null) break;
                        strType = paramType.getNodeValue();
                        strLid = paramLid.getNodeValue();
                        langFound.add(strLid);
                        if (strType.equals("name") && paramFound.add("n"+strLid)) continue;
                        if (strType.equals("description") && paramFound.add("d"+strLid)) continue;
                        break;
                    default:
                        break;
                }
                bolCommandeValide = false;
            }
            //System.out.println("Param found: " + paramFound);
            if (paramFound.size() != 6+2*langFound.size())
                bolCommandeValide = false;
        }
        else if (noeudCommande.getAttribute("nom").equals(Commande.UpdateRoom)) {
            NodeList noeudsParametre = noeudCommande.getChildNodes();
            TreeSet<String> paramFound = new TreeSet<String>();
            TreeSet<String> langFound = new TreeSet<String>();
            bolCommandeValide = true;
            for (int i=0; bolCommandeValide && i<noeudsParametre.getLength(); i++) {
                Node objNoeudParam = noeudsParametre.item(i);
                if (!objNoeudParam.getNodeName().equals("parametre")) { bolCommandeValide = false; break; }
                Node paramType,paramLid;
                String strType="", strLid="";
                switch(objNoeudParam.getAttributes().getLength())
                {
                    case 1:
                       paramType = objNoeudParam.getAttributes().getNamedItem("type");
                       if (paramType == null) break;
                       strType = paramType.getNodeValue();
                       if (strType.equals("roomId") && paramFound.add("roomId")) continue;
                       if (strType.equals("beginDate") && paramFound.add("beginDate")) continue;
                       if (strType.equals("endDate") && paramFound.add("endDate")) continue;
                       if (strType.equals("password") && paramFound.add("password")) continue;
                       if (strType.equals("masterTime") && paramFound.add("masterTime")) continue;
                       if (strType.equals("keywordIds") && paramFound.add("keywordIds")) continue;
                       if (strType.equals("gameTypeIds") && paramFound.add("gameTypeIds")) continue;
                       break;
                    case 2:
                        paramType = objNoeudParam.getAttributes().getNamedItem("type");
                        if (paramType == null) break;
                        paramLid = objNoeudParam.getAttributes().getNamedItem("language_id");
                        if (paramLid == null) break;
                        strType = paramType.getNodeValue();
                        strLid = paramLid.getNodeValue();
                        langFound.add(strLid);
                        if (strType.equals("name") && paramFound.add("n"+strLid)) continue;
                        if (strType.equals("description") && paramFound.add("d"+strLid)) continue;
                        break;
                    default:
                        break;
                }
                bolCommandeValide = false;
            }
            if (paramFound.size() != 7+2*langFound.size())
                bolCommandeValide = false;
        }
        else if (noeudCommande.getAttribute("nom").equals(Commande.DeleteRoom)) {
            // Si le nombre d'enfants du noeud de commande est de 1, et que le
            // format est: <parametre type="RoomId">342</parametre>
            // alors on peut continuer.
            if (noeudCommande.getChildNodes().getLength() == 1) {
                Node objNoeudParamRoomId = noeudCommande.getFirstChild();
                if (objNoeudParamRoomId.getNodeName().equals("parametre") &&
                        objNoeudParamRoomId.getAttributes().getLength() == 1 &&
                        objNoeudParamRoomId.getAttributes().getNamedItem("type") != null &&
                        objNoeudParamRoomId.getAttributes().getNamedItem("type").getNodeValue().equals("RoomId"))
                    bolCommandeValide = true;
            }
        }
        else if (noeudCommande.getAttribute("nom").equals(Commande.ReportRoom)) {
            // Si le nombre d'enfants du noeud de commande est de 1, et que le
            // format est: <parametre type="RoomId">342</parametre>
            // alors on peut continuer.
            if (noeudCommande.getChildNodes().getLength() == 1) {
                Node objNoeudParamRoomId = noeudCommande.getFirstChild();
                if (objNoeudParamRoomId.getNodeName().equals("parametre") &&
                        objNoeudParamRoomId.getAttributes().getLength() == 1 &&
                        objNoeudParamRoomId.getAttributes().getNamedItem("type") != null &&
                        objNoeudParamRoomId.getAttributes().getNamedItem("type").getNodeValue().equals("RoomId"))
                    bolCommandeValide = true;
            }
        } else if (noeudCommande.getAttribute("nom").equals(Commande.CloseRoom)) {
            // Si le nombre d'enfants du noeud de commande est de 0, alors
            // il n'y a vraiment aucun paramètres TODO realy 0 ???
            if (noeudCommande.getChildNodes().getLength() == 0) {
                bolCommandeValide = true;
            }
        } // Si le nom de la commande est EntrerSalle, alors il doit y avoir 2 paramètres
        else if (noeudCommande.getAttribute("nom").equals(Commande.EntrerSalle)) {
            // Si le nombre d'enfants du noeud de commande est de 2, alors
            // le nombre de paramètres est correct et on peut continuer
            if (noeudCommande.getChildNodes().getLength() == 2) // est changer avec "niveaux"
            {
                // Déclarer une variable qui va permettre de savoir si les
                // noeuds enfants sont valides
                boolean bolNoeudValide = true;

                // Déclaration d'un compteur
                int i = 0;

                // Passer tous les noeuds enfants et vérifier qu'ils sont bien
                // des paramètres avec le type approprié
                while (i < noeudCommande.getChildNodes().getLength() && bolNoeudValide == true) {
                    // Faire la référence vers le noeud enfant courant
                    Node objNoeudCourant = noeudCommande.getChildNodes().item(i);

                    // Si le noeud courant n'est pas un paramètre, ou qu'il n'a
                    // pas exactement 1 attribut, ou que le nom de cet attribut
                    // n'est pas type, ou que le noeud n'a pas de valeurs, alors
                    // il y a une erreur dans la structure (le deuxième paramètre
                    // peut avoir aucune valeur)
                    if (objNoeudCourant.getNodeName().equals("parametre") == false ||
                            objNoeudCourant.getAttributes().getLength() != 1 ||
                            objNoeudCourant.getAttributes().getNamedItem("type") == null ||
                            /*	objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("NomSalle") == false ||
                            objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("Niveaux") == false || */
                            (objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("NomSalle") &&
                            objNoeudCourant.getChildNodes().getLength() != 1) ||
                            (objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("MotDePasse") &&
                            objNoeudCourant.getChildNodes().getLength() > 1) ||
                            (objNoeudCourant.getChildNodes().getLength() == 1 &&
                            objNoeudCourant.getChildNodes().item(0).getNodeName().equals("#text") == false)) {
                        bolNoeudValide = false;
                    }
                    i++;
                }

                // Si les enfants du noeud courant sont tous valides alors la
                // commande est valide
                bolCommandeValide = bolNoeudValide;
            }
        } // Si le nom de la commande est QuitterSalle, alors il ne doit pas y avoir
        // de paramètres
        else if (noeudCommande.getAttribute("nom").equals(Commande.QuitterSalle)) {
            // Si le nombre d'enfants du noeud de commande est de 0, alors
            // il n'y a vraiment aucun paramètres
            if (noeudCommande.getChildNodes().getLength() == 0) {
                bolCommandeValide = true;
            }
        } // Si le nom de la commande est ObtenirListeJoueursSalle, alors il ne
        // doit pas y avoir de paramètres
        else if (noeudCommande.getAttribute("nom").equals(Commande.ObtenirListeJoueursSalle)) {
            // Si le nombre d'enfants du noeud de commande est de 0, alors
            // il n'y a vraiment aucun paramètres
            if (noeudCommande.getChildNodes().getLength() == 0) {
                bolCommandeValide = true;
            }
        } // Si le nom de la commande est ObtenirListeTables, alors il ne doit
        // pas y avoir de paramètres
        else if (noeudCommande.getAttribute("nom").equals(Commande.ObtenirListeTables)) {
            // Si le nombre d'enfants du noeud de commande est de 1, alors
            // le nombre de paramètres est correct et on peut continuer
            if (noeudCommande.getChildNodes().getLength() == 1) {
                // Déclarer une variable qui va permettre de savoir si le
                // noeud enfant est valide
                boolean bolNoeudValide = true;

                // Faire la référence vers le noeud enfant courant
                Node objNoeudCourant = noeudCommande.getChildNodes().item(0);

                // Si le noeud enfant n'est pas un paramètre, ou qu'il n'a
                // pas exactement 1 attribut, ou que le nom de cet attribut
                // n'est pas type, ou que le noeud n'a pas de valeurs, alors
                // il y a une erreur dans la structure

                if (objNoeudCourant.getNodeName().equals("parametre") == false ||
                        objNoeudCourant.getAttributes().getLength() != 1 ||
                        objNoeudCourant.getAttributes().getNamedItem("type") == null ||
                        objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("Filtre") == false ||
                        objNoeudCourant.getChildNodes().getLength() != 1 ||
                        objNoeudCourant.getChildNodes().item(0).getNodeName().equals("#text") == false) {
                    bolNoeudValide = false;
                }

                // Si l'enfant du noeud courant est valide alors la commande
                // est valide

                bolCommandeValide = bolNoeudValide;
            }
        } // Si le nom de la commande est CreerTable, alors il doit y avoir 1 paramètre
        else if (noeudCommande.getAttribute("nom").equals(Commande.CreerTable)) {
            // Si le nombre d'enfants du noeud de commande est de 1, alors
            // le nombre de paramètres est correct et on peut continuer
            if (noeudCommande.getChildNodes().getLength() == 5) {
                // Déclarer une variable qui va permettre de savoir si le
                // noeud enfant est valide

                boolean bolNoeudValide = true;

                // Faire la référence vers le noeud enfant courant
                Node objNoeudCourant = noeudCommande.getChildNodes().item(0);

                // Si le noeud enfant n'est pas un paramètre, ou qu'il n'a
                // pas exactement 1 attribut, ou que le nom de cet attribut
                // n'est pas type, ou que le noeud n'a pas de valeurs, alors
                // il y a une erreur dans la structure
                if (objNoeudCourant.getNodeName().equals("parametre") == false ||
                        objNoeudCourant.getAttributes().getLength() != 1 ||
                        objNoeudCourant.getAttributes().getNamedItem("type") == null ||
                        objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("TempsPartie") == false ||
                        objNoeudCourant.getChildNodes().getLength() != 1 ||
                        objNoeudCourant.getChildNodes().item(0).getNodeName().equals("#text") == false ||
                        UtilitaireNombres.isPositiveNumber(objNoeudCourant.getChildNodes().item(0).getNodeValue()) == false) {
                    bolNoeudValide = false;
                }

                // Si l'enfant du noeud courant est valide alors la commande
                // est valide
                bolCommandeValide = bolNoeudValide;
            }
        } // Si le nom de la commande est EntrerTable, alors il doit y avoir 1 paramètre
        else if (noeudCommande.getAttribute("nom").equals(Commande.EntrerTable)) {
            // Si le nombre d'enfants du noeud de commande est de 1, alors
            // le nombre de paramètres est correct et on peut continuer
            if (noeudCommande.getChildNodes().getLength() == 1) {
                // Déclarer une variable qui va permettre de savoir si le
                // noeud enfant est valide
                boolean bolNoeudValide = true;

                // Faire la référence vers le noeud enfant courant
                Node objNoeudCourant = noeudCommande.getChildNodes().item(0);

                // Si le noeud enfant n'est pas un paramètre, ou qu'il n'a
                // pas exactement 1 attribut, ou que le nom de cet attribut
                // n'est pas type, ou que le noeud n'a pas de valeurs, alors
                // il y a une erreur dans la structure
                if (objNoeudCourant.getNodeName().equals("parametre") == false ||
                        objNoeudCourant.getAttributes().getLength() != 1 ||
                        objNoeudCourant.getAttributes().getNamedItem("type") == null ||
                        objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("NoTable") == false ||
                        objNoeudCourant.getChildNodes().getLength() != 1 ||
                        objNoeudCourant.getChildNodes().item(0).getNodeName().equals("#text") == false ||
                        UtilitaireNombres.isPositiveNumber(objNoeudCourant.getChildNodes().item(0).getNodeValue()) == false) {
                    bolNoeudValide = false;
                }

                // Si l'enfant du noeud courant est valide alors la commande
                // est valide
                bolCommandeValide = bolNoeudValide;
            }
        } // Si le nom de la commande est QuitterTable, alors il ne doit pas y avoir
        // de paramètres
        else if (noeudCommande.getAttribute("nom").equals(Commande.QuitterTable)) {
            // Si le nombre d'enfants du noeud de commande est de 0, alors
            // il n'y a vraiment aucun paramètres
            if (noeudCommande.getChildNodes().getLength() == 0) {
                bolCommandeValide = true;
            }
        } // Si le nom de la commande est DemarrerPartie, alors il doit y avoir 1 paramètre
        else if (noeudCommande.getAttribute("nom").equals(Commande.DemarrerPartie)) {
            // Si le nombre d'enfants du noeud de commande est de 1, alors
            // le nombre de paramètres est correct et on peut continuer

            if (noeudCommande.getChildNodes().getLength() == 1) {
                // Déclarer une variable qui va permettre de savoir si le
                // noeud enfant est valide
                boolean bolNoeudValide = true;

                // Faire la référence vers le noeud enfant courant
                Node objNoeudCourant = noeudCommande.getChildNodes().item(0);

                // Si le noeud enfant n'est pas un paramètre, ou qu'il n'a
                // pas exactement 1 attribut, ou que le nom de cet attribut
                // n'est pas type, ou que le noeud n'a pas de valeurs, alors
                // il y a une erreur dans la structure
                if (objNoeudCourant.getNodeName().equals("parametre") == false ||
                        objNoeudCourant.getAttributes().getLength() != 1 ||
                        objNoeudCourant.getAttributes().getNamedItem("type") == null ||
                        objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("IdDessin") == false ||
                        objNoeudCourant.getChildNodes().getLength() != 1 ||
                        objNoeudCourant.getChildNodes().item(0).getNodeName().equals("#text") == false ||
                        UtilitaireNombres.isPositiveNumber(objNoeudCourant.getChildNodes().item(0).getNodeValue()) == false) {
                    bolNoeudValide = false;
                }

                /*
                //validation du deuxième noeud (NiveauJoueurVirtuel)
                objNoeudCourant = noeudCommande.getChildNodes().item(1);

                // Si le noeud enfant n'est pas un paramètre, ou qu'il n'a
                // pas exactement 1 attribut, ou que le nom de cet attribut
                // n'est pas type, ou que le noeud n'a pas de valeurs, alors
                // il y a une erreur dans la structure
                if (objNoeudCourant.getNodeName().equals("parametre") == false ||
                objNoeudCourant.getAttributes().getLength() != 1 ||
                objNoeudCourant.getAttributes().getNamedItem("type") == null ||
                objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("ClothesColor") == false ||
                objNoeudCourant.getChildNodes().getLength() != 1)
                {
                bolNoeudValide = false;
                }*/

                // Si l'enfant du noeud courant est valide alors la commande
                // est valide
                bolCommandeValide = bolNoeudValide;
            }
        } // Si le nom de la commande est DemarrerMaintenant, alors il doit y avoir 2 paramètres
        else if (noeudCommande.getAttribute("nom").equals(Commande.DemarrerMaintenant)) {
            // Si le nombre d'enfants du noeud de commande est de 2, alors
            // le nombre de paramètres est correct et on peut continuer
            if (noeudCommande.getChildNodes().getLength() == 1) {
                // Déclarer une variable qui va permettre de savoir si le
                // noeud enfant est valide
                boolean bolNoeudValide = true;

                // Faire la référence vers le noeud enfant courant
                Node objNoeudCourant = noeudCommande.getChildNodes().item(0);

                String valeurParam = objNoeudCourant.getChildNodes().item(0).getNodeValue();

                //System.out.println(JoueurVirtuel.validerParamNiveau(valeurParam));
                if (objNoeudCourant.getNodeName().equals("parametre") == false ||
                        objNoeudCourant.getAttributes().getLength() != 1 ||
                        objNoeudCourant.getAttributes().getNamedItem("type") == null ||
                        objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("NiveauJoueurVirtuel") == false ||
                        objNoeudCourant.getChildNodes().getLength() != 1 ||
                        objNoeudCourant.getChildNodes().item(0).getNodeName().equals("#text") == false ||
                        JoueurVirtuel.validerParamNiveau(valeurParam) == false) {
                    bolNoeudValide = false;
                }

                // Si l'enfant du noeud courant est valide alors la commande
                // est valide
                bolCommandeValide = bolNoeudValide;
            }
        } // Si le nom de la commande est DeplacerPersonnage, alors il doit y avoir
        // 1 paramètre position contenant les coordonnées x, y
        else if (noeudCommande.getAttribute("nom").equals(Commande.DeplacerPersonnage)) {
            // Si le nombre d'enfants du noeud de commande est de 1, alors
            // le nombre de paramètres est correct et on peut continuer
            if (noeudCommande.getChildNodes().getLength() == 1) {
                // Déclarer une variable qui va permettre de savoir si les
                // noeuds enfants sont valides
                boolean bolNoeudValide = true;

                // Faire la référence vers le noeud enfant courant
                Node objNoeudCourant = noeudCommande.getChildNodes().item(0);

                // Si le noeud enfant n'est pas un paramètre, ou qu'il n'a
                // pas exactement 1 attribut, ou que le nom de cet attribut
                // n'est pas type, ou que le noeud n'a pas de valeurs, alors
                // il y a une erreur dans la structure
                if (objNoeudCourant.getNodeName().equals("parametre") == false ||
                        objNoeudCourant.getAttributes().getLength() != 1 ||
                        objNoeudCourant.getAttributes().getNamedItem("type") == null ||
                        objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("NouvellePosition") == false ||
                        objNoeudCourant.getChildNodes().getLength() != 1 ||
                        objNoeudCourant.getChildNodes().item(0).getNodeName().equals("position") == false ||
                        objNoeudCourant.getChildNodes().item(0).getAttributes().getLength() != 2 ||
                        objNoeudCourant.getChildNodes().item(0).getAttributes().getNamedItem("x") == null ||
                        objNoeudCourant.getChildNodes().item(0).getAttributes().getNamedItem("y") == null ||
                        UtilitaireNombres.isPositiveNumber(objNoeudCourant.getChildNodes().item(0).getAttributes().getNamedItem("x").getNodeValue()) == false ||
                        UtilitaireNombres.isPositiveNumber(objNoeudCourant.getChildNodes().item(0).getAttributes().getNamedItem("y").getNodeValue()) == false) {
                    bolNoeudValide = false;
                }

                // Si l'enfant du noeud courant est valide alors la commande
                // est valide
                bolCommandeValide = bolNoeudValide;
            }
        } // Si le nom de la commande est RepondreQuestion, alors il doit y avoir 1 paramètre
        else if (noeudCommande.getAttribute("nom").equals(Commande.RepondreQuestion)) {
            // Si le nombre d'enfants du noeud de commande est de 1, alors
            // le nombre de paramètres est correct et on peut continuer
            if (noeudCommande.getChildNodes().getLength() == 1) {
                // Déclarer une variable qui va permettre de savoir si le
                // noeud enfant est valide
                boolean bolNoeudValide = true;

                // Faire la référence vers le noeud enfant courant
                Node objNoeudCourant = noeudCommande.getChildNodes().item(0);

                // Si le noeud enfant n'est pas un paramètre, ou qu'il n'a
                // pas exactement 1 attribut, ou que le nom de cet attribut
                // n'est pas type, ou que le noeud n'a pas de valeurs, alors
                // il y a une erreur dans la structure
                if (objNoeudCourant.getNodeName().equals("parametre") == false ||
                        objNoeudCourant.getAttributes().getLength() != 1 ||
                        objNoeudCourant.getAttributes().getNamedItem("type") == null ||
                        objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("Reponse") == false ||
                        objNoeudCourant.getChildNodes().getLength() != 1 ||
                        objNoeudCourant.getChildNodes().item(0).getNodeName().equals("#text") == false) {
                    bolNoeudValide = false;
                }

                // Si l'enfant du noeud courant est valide alors la commande
                // est valide
                bolCommandeValide = bolNoeudValide;
            }
        } // Si le nom de la commande est CancelQuestion, alors il ne doit pas y avoir
        // de paramètres
        else if (noeudCommande.getAttribute("nom").equals(Commande.CancelQuestion)) {
            // Si le nombre d'enfants du noeud de commande est de 0, alors
            // il n'y a vraiment aucun paramètres
            if (noeudCommande.getChildNodes().getLength() == 0) {
                bolCommandeValide = true;
            }
        } //Si le nom de la commande est Pointage, alors il doit y avoir 1 paramètre
        else if (noeudCommande.getAttribute("nom").equals(Commande.Pointage)) {
            // Si le nombre d'enfants du noeud de commande est de 1, alors
            // le nombre de paramètres est correct et on peut continuer
            if (noeudCommande.getChildNodes().getLength() == 1) {
                // Déclarer une variable qui va permettre de savoir si le
                // noeud enfant est valide
                boolean bolNoeudValide = true;

                // Faire la référence vers le noeud enfant courant
                Node objNoeudCourant = noeudCommande.getChildNodes().item(0);

                // Si le noeud enfant n'est pas un paramètre, ou qu'il n'a
                // pas exactement 1 attribut, ou que le nom de cet attribut
                // n'est pas type, ou que le noeud n'a pas de valeurs, alors
                // il y a une erreur dans la structure
                if (objNoeudCourant.getNodeName().equals("parametre") == false ||
                        objNoeudCourant.getAttributes().getLength() != 1 ||
                        objNoeudCourant.getAttributes().getNamedItem("type") == null ||
                        objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("Pointage") == false ||
                        objNoeudCourant.getChildNodes().getLength() != 1 ||
                        objNoeudCourant.getChildNodes().item(0).getNodeName().equals("#text") == false ||
                        UtilitaireNombres.isPositiveNumber(objNoeudCourant.getChildNodes().item(0).getNodeValue()) == false) {
                    bolNoeudValide = false;
                }

                // Si l'enfant du noeud courant est valide alors la commande
                // est valide
                bolCommandeValide = bolNoeudValide;
            }
        } //Si le nom de la commande est Argent, alors il doit y avoir 1 paramètre
        else if (noeudCommande.getAttribute("nom").equals(Commande.Argent)) {
            // Si le nombre d'enfants du noeud de commande est de 1, alors
            // le nombre de paramètres est correct et on peut continuer
            if (noeudCommande.getChildNodes().getLength() == 1) {
                // Déclarer une variable qui va permettre de savoir si le
                // noeud enfant est valide
                boolean bolNoeudValide = true;

                // Faire la référence vers le noeud enfant courant
                Node objNoeudCourant = noeudCommande.getChildNodes().item(0);

                // Si le noeud enfant n'est pas un paramètre, ou qu'il n'a
                // pas exactement 1 attribut, ou que le nom de cet attribut
                // n'est pas type, ou que le noeud n'a pas de valeurs, alors
                // il y a une erreur dans la structure
                if (objNoeudCourant.getNodeName().equals("parametre") == false ||
                        objNoeudCourant.getAttributes().getLength() != 1 ||
                        objNoeudCourant.getAttributes().getNamedItem("type") == null ||
                        objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("Argent") == false ||
                        objNoeudCourant.getChildNodes().getLength() != 1 ||
                        objNoeudCourant.getChildNodes().item(0).getNodeName().equals("#text") == false ||
                        UtilitaireNombres.isPositiveNumber(objNoeudCourant.getChildNodes().item(0).getNodeValue()) == false) {
                    bolNoeudValide = false;
                }

                // Si l'enfant du noeud courant est valide alors la commande
                // est valide
                bolCommandeValide = bolNoeudValide;
            }
        } // Si le nom de la commande est AcheterObjet, il doit y voir un paramètre
        else if (noeudCommande.getAttribute("nom").equals(Commande.AcheterObjet)) {
            if (noeudCommande.getChildNodes().getLength() == 1) {
                // Déclarer une variable qui va permettre de savoir si le
                // noeud enfant est valide
                boolean bolNoeudValide = true;

                // Faire la référence vers le noeud enfant courant
                Node objNoeudCourant = noeudCommande.getChildNodes().item(0);

                // Si le noeud enfant n'est pas un paramètre, ou qu'il n'a
                // pas exactement 1 attribut, ou que le nom de cet attribut
                // n'est pas type, ou que le noeud n'a pas de valeurs, alors
                // il y a une erreur dans la structure
                if (objNoeudCourant.getNodeName().equals("parametre") == false ||
                        objNoeudCourant.getAttributes().getLength() != 1 ||
                        objNoeudCourant.getAttributes().getNamedItem("type") == null ||
                        objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("id") == false ||
                        objNoeudCourant.getChildNodes().getLength() != 1 ||
                        objNoeudCourant.getChildNodes().item(0).getNodeName().equals("#text") == false ||
                        UtilitaireNombres.isPositiveNumber(objNoeudCourant.getChildNodes().item(0).getNodeValue()) == false) {
                    bolNoeudValide = false;
                }

                // Si l'enfant du noeud courant est valide alors la commande
                // est valide
                bolCommandeValide = bolNoeudValide;
            }
        } // Si le nom de la commande est UtiliserObjet, il doit y voir un paramètre
        else if (noeudCommande.getAttribute("nom").equals(Commande.UtiliserObjet)) {
            if (noeudCommande.getChildNodes().getLength() == 2) {
                // Déclarer une variable qui va permettre de savoir si le
                // noeud enfant est valide
                boolean bolNoeudValide = true;

                // Faire la référence vers le noeud enfant courant
                Node objNoeudCourant = noeudCommande.getChildNodes().item(0);

                // Si le noeud enfant n'est pas un paramètre, ou qu'il n'a
                // pas exactement 1 attribut, ou que le nom de cet attribut
                // n'est pas type, ou que le noeud n'a pas de valeurs, alors
                // il y a une erreur dans la structure
                if (objNoeudCourant.getNodeName().equals("parametre") == false ||
                        objNoeudCourant.getAttributes().getLength() != 1 ||
                        objNoeudCourant.getAttributes().getNamedItem("type") == null ||
                        objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("id") == false ||
                        objNoeudCourant.getChildNodes().getLength() != 1 ||
                        objNoeudCourant.getChildNodes().item(0).getNodeName().equals("#text") == false ||
                        UtilitaireNombres.isPositiveNumber(objNoeudCourant.getChildNodes().item(0).getNodeValue()) == false) {
                    bolNoeudValide = false;
                }

                // Si l'enfant du noeud courant est valide alors la commande
                // est valide
                bolCommandeValide = bolNoeudValide;
            }
        } else if (noeudCommande.getAttribute("nom").equals(Commande.ChatMessage)) {
            // Si le nombre d'enfants du noeud de commande est de 1, alors
            // le nombre de paramètres est correct et on peut continuer
            if (noeudCommande.getChildNodes().getLength() == 1) {
                // Déclarer une variable qui va permettre de savoir si le
                // noeud enfant est valide
                boolean bolNoeudValide = true;

                // Faire la référence vers le noeud enfant courant
                Node objNoeudCourant = noeudCommande.getChildNodes().item(0);

                // Si le noeud enfant n'est pas un paramètre, ou qu'il n'a
                // pas exactement 1 attribut, ou que le nom de cet attribut
                // n'est pas type, ou que le noeud n'a pas de valeurs, alors
                // il y a une erreur dans la structure
                if (objNoeudCourant.getNodeName().equals("parametre") == false ||
                        objNoeudCourant.getAttributes().getLength() != 1 ||
                        objNoeudCourant.getAttributes().getNamedItem("type") == null ||
                        objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("ChatMessage") == false ||
                        objNoeudCourant.getChildNodes().getLength() != 1 ||
                        objNoeudCourant.getChildNodes().item(0).getNodeName().equals("#text") == false ||
                        UtilitaireNombres.isPositiveNumber(objNoeudCourant.getChildNodes().item(0).getNodeValue()) == false) {
                    bolNoeudValide = false;
                }

                // Si l'enfant du noeud courant est valide alors la commande
                // est valide
                bolCommandeValide = bolNoeudValide;
            }
        }
        return bolCommandeValide;
    }

    /**
     * Cette fonction permet de retourner le noeud correspondant à la valeur
     * du paramètre dont le nom est passé en paramètres. On recherche d'abord
     * le noeud parametre parmi les noeuds enfants du noeud de commande passé
     * en paramètres puis une fois qu'on a trouvé le bon, on retourne son noeud
     * enfant. On suppose que la structure est conforme et que la valeur du
     * paramètre est un seul noeud (soit un noeud texte ou une liste).
     *
     * @param Element noeudCommande : le noeud de comande dans lequel chercher
     * 								  le bon paramètre
     * @param String nomParametre : le nom du paramètre à chercher
     * @return Node : le noeud contenant la valeur du paramètre (soit un noeud
     * 				  texte ou un noeud contenant une liste)
     */
    private Node obtenirValeurParametre(Element noeudCommande, String nomParametre) {
        // Déclaration d'une variable qui va contenir le noeud représentant
        // la valeur du paramètre
        Node objValeurParametre = null;

        // Déclaration d'un compteur
        int i = 0;

        // Déclaration d'une variable qui va nous permettre de savoir si on a
        // trouvé la valeur du paramètre recherché
        boolean bolTrouve = false;

        // Passer tous les noeuds enfants (paramètres) du noeud de commande et
        // boucler tant qu'on n'a pas trouver le bon paramètre
        while (i < noeudCommande.getChildNodes().getLength() && bolTrouve == false) {
            // Garder une référence vers le noeud courant
            Node objNoeudCourant = noeudCommande.getChildNodes().item(i);

            // Si le noeud courant a l'attribut type dont la valeur est passée
            // en paramètres, alors on l'a trouvé, on va garder une référence
            // vers la valeur du noeud courant
            if (objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals(nomParametre)) {
                bolTrouve = true;

                // Garder la référence vers le noeud enfant (il est le seul et
                // il est soit un noeud texte ou un noeud représentant une liste)
                objValeurParametre = objNoeudCourant.getChildNodes().item(0);
            }

            i++;
        }

        return objValeurParametre;
    }

    /**
     * Cette méthode permet de générer un nouveau numéro de commande à retourner
     * en réponse au client.
     */
    public void genererNumeroReponse() {
        // Modifier le numéro de commande à retourner au client
        intNumeroCommandeReponse = obtenirNumeroCommande();
    }

    /**
     * Cette fonction permet de retourner le numéro de la commande courante et
     * d'augmenter le compteur de commandes. Le numéro de commande permet au
     * client de savoir quel événement est arrivé avant quel autre.
     *
     * @return int : le numéro de la commande
     */
    public int obtenirNumeroCommande() {
        // Déclaration d'une variable qui va contenir le numéro de la commande
        // à retourner
        int intNumeroCommande = intCompteurCommande;

        // Incrémenter le compteur de commandes
        intCompteurCommande++;

        // Si le compteur de commandes est maintenant plus grand que la plus
        // grande valeur possible, alors on réinitialise le compteur à 0
        if (intCompteurCommande > MAX_COMPTEUR) {
            intCompteurCommande = 0;
        }

        return intNumeroCommande;
    }

    /**
     * Cette méthode permet d'envoyer un événement ping au joueur courant
     * pour savoir s'il est toujours connecté au serveur de jeu.
     *
     * @param : int numeroPing : le numéro du ping, c'est le numéro qui
     * 							 va servir à identifier le ping
     */
    public void envoyerEvenementPing(int numeroPing) {
        try {
            // Envoyer le message ping au joueur
            envoyerMessage("<ping numero=\"" + numeroPing + "\"/>");
        } catch (IOException e) {

            objLogger.info(GestionnaireMessages.message("protocole.erreur_ping"));
            objLogger.error(e.getMessage());
        }
    }

    /**
     * Cette méthode permet d'arrèter le thread et de fermer le socket du
     * client. Si le joueur était connecté à une table, une salle ou au serveur
     * de jeu, alors il sera complètement déconnecté.
     */
    public void arreterProtocoleJoueur() {
        Thread.currentThread().interrupt();
        try {
            // On tente de fermer le canal de réception. Cela va provoquer
            // une erreur dans le thread et le joueur va ètre déconnecté et
            // le thread va arrèter
            objCanalReception.close();
        } catch (IOException ioe) {
            objLogger.error(ioe.getMessage());
        }

        try {
            // On tente de fermer le socket liant le client au serveur. Cela
            // va provoquer une erreur dans le thread et le joueur va ètre
            // déconnecté et le thread va arrèter
            objSocketJoueur.close();
        } catch (IOException ioe) {
            objLogger.error(ioe.getMessage());
        }


    }

    /**
     * Cette fonction permet de retourner l'adresse IP du joueur courant.
     *
     * @return String : L'adresse IP du joueur courant
     */
    public String obtenirAdresseIP() {
        // Retourner l'adresse IP du joueur
        return objSocketJoueur.getInetAddress().getHostAddress();
    }

    /**
     * Cette fonction permet de retourner le port du joueur courant.
     *
     * @return String : Le port du joueur courant
     */
    public String obtenirPort() {
        // Retourner le port du joueur
        return Integer.toString(objSocketJoueur.getPort());
    }

    /**
     * Cette méthode permet de définir la nouvelle référence vers un joueur
     * humain.
     *
     * @param JoueurHumain joueur : Le joueur humain auquel faire la référence
     */
    public void definirJoueur(JoueurHumain joueur) {
        // Faire la référence vers le joueur humain
        objJoueurHumain = joueur;
    }

    public JoueurHumain obtenirJoueurHumain() {
        // Retourner une référence vers le joueur humain
        return objJoueurHumain;
    }

    public boolean isPlaying() {
        return bolEnTrainDeJouer;
    }

    public void definirEnTrainDeJoueur(boolean nouvelleValeur) {
        bolEnTrainDeJouer = nouvelleValeur;
    }

    /* 
     * Permet d'envoyer le plateau de jeu à un joueur qui rejoint une partie
     */
    private void envoyerPlateauJeu(JoueurHumain ancientJoueur) {
        // Obtenir la référence vers la table oè le joueur était
        Table objTable = ancientJoueur.obtenirPartieCourante().obtenirTable();

        // Créer un tableau des joueurs, on a "+ 1" car le joueur
        // déconnecté n'était plus dans cette liste
        Joueur lstJoueursTable[] = new Joueur[objTable.obtenirListeJoueurs().size() + objTable.getNombreJoueursVirtuels() + 1];

        int j = 0;
        // Passer tous les positions des joueurs et les ajouter à la liste locale
        for (JoueurHumain joueur : objTable.obtenirListeJoueurs().values()) {
            lstJoueursTable[j] = joueur;
            j++;
        }

        // Ajouter les joueurs virtuels
        ArrayList<JoueurVirtuel> lstJoueursVirtuels = ancientJoueur.obtenirPartieCourante().obtenirTable().obtenirListeJoueursVirtuels();

        if (lstJoueursVirtuels != null) {
            for (int i = 0; i < lstJoueursVirtuels.size(); i++) {

                JoueurVirtuel objJoueurVirtuel = (JoueurVirtuel) lstJoueursVirtuels.get(i);

                lstJoueursTable[j] = objJoueurVirtuel;
                j++;


            }
        }

        // Créer l'événement contenant toutes les informations sur le plateau et
        // la partie
        EvenementPartieDemarree objEvenementPartieDemarree = new EvenementPartieDemarree(objTable, lstJoueursTable);//this.obtenirJoueurHumain().obtenirPartieCourante().obtenirTable());


        // Créer l'objet information destination pour envoyer l'information à ce joueur
        InformationDestination objInformationDestination = new InformationDestination(obtenirNumeroCommande(), this);

        // Envoyer l'événement
        objEvenementPartieDemarree.ajouterInformationDestination(objInformationDestination);

        objEvenementPartieDemarree.envoyerEvenement();

    }

    /*
     * Permet d'envoyer la liste des joueurs à un joueur qui rejoint une partie
     * La liste inclut le joueur qui rejoint la partie car il doit connaître
     * quel avatar il avait. Ë noter que ce message est différent de envoyer
     * liste des joueurs pour une table, il faut aussi envoyer les joueurs 
     * virtuels et s'envoyer soi-mème (?) pour que le joueur qui se reconnecte
     * sache quel avatar il avait choisit
     */
    private void envoyerListeJoueurs(JoueurHumain ancientJoueur, String no) {
        /*
         * <commande nom="ListeJoueurs" no="0" type="MiseAJour">
         * <parametre type="ListeJoueurs"><joueur nom="Joueur1" id="1"/>
         * <joueur nom="Joueur2 id="2"/></parametre></commande>
         *
         */

        // Déclaration d'une variable qui va contenir le code XML à envoyer
        String strCodeXML = "";

        // Appeler une fonction qui va créer un document XML dans lequel
        // on peut ajouter des noeuds
        Document objDocumentXML = UtilitaireXML.obtenirDocumentXML();

        // Créer le noeud de commande à retourner
        Element objNoeudCommande = objDocumentXML.createElement("commande");

        // Créer le noeud du paramètre
        Element objNoeudParametre = objDocumentXML.createElement("parametre");

        // Envoyer une liste des joueurs
        objNoeudCommande.setAttribute("noClient", no);
        objNoeudCommande.setAttribute("type", "MiseAJour");
        objNoeudCommande.setAttribute("nom", "ListeJoueurs");


        // Créer le noeud pour le paramètre contenant la liste
        // des joueurs à retourner
        Element objNoeudParametreListeJoueurs = objDocumentXML.createElement("parametre");

        // On ajoute un attribut type qui va contenir le type
        // du paramètre
        objNoeudParametreListeJoueurs.setAttribute("type", "ListeJoueurs");

        // Obtenir la liste des joueurs que l'on doit envoyer
        HashMap<String, JoueurHumain> lstJoueurs = ancientJoueur.obtenirPartieCourante().obtenirTable().obtenirListeJoueurs();

        // Générer un nouveau numéro de commande qui sera
        // retourné au client
        genererNumeroReponse();
        // Passer toutes les joueurs et créer un noeud pour
        // chaque joueur et l'ajouter au noeud de paramètre
        for (JoueurHumain joueurHumain : lstJoueurs.values()) {
            Element objNoeudJoueur = objDocumentXML.createElement("joueur");
            objNoeudJoueur.setAttribute("nom", joueurHumain.obtenirNomUtilisateur());
            objNoeudJoueur.setAttribute("id", Integer.toString(joueurHumain.obtenirPartieCourante().obtenirIdPersonnage()));
            objNoeudJoueur.setAttribute("role", Integer.toString(joueurHumain.getRole()));
            objNoeudJoueur.setAttribute("pointage", Integer.toString(joueurHumain.obtenirPartieCourante().obtenirPointage()));
            // Ajouter le noeud de l'item au noeud du paramètre
            objNoeudParametreListeJoueurs.appendChild(objNoeudJoueur);
        }

        // -----------------------
        // S'ajouter soi-mème
        // Créer le noeud
        Element objNoeudJoueur = objDocumentXML.createElement("joueur");

        // On ajoute les attributs nom et id identifiant le joueur
        objNoeudJoueur.setAttribute("nom", ancientJoueur.obtenirNomUtilisateur());
        objNoeudJoueur.setAttribute("id", Integer.toString(ancientJoueur.obtenirPartieCourante().obtenirIdPersonnage()));
        objNoeudJoueur.setAttribute("role", Integer.toString(ancientJoueur.getRole()));
        objNoeudJoueur.setAttribute("pointage", Integer.toString(ancientJoueur.obtenirPartieCourante().obtenirPointage()));

        // Ajouter le noeud de l'item au noeud du paramètre
        objNoeudParametreListeJoueurs.appendChild(objNoeudJoueur);

        // ----------------------------
        // Ajouter les joueurs virtuels
        ArrayList<JoueurVirtuel> lstJoueursVirtuels = ancientJoueur.obtenirPartieCourante().obtenirTable().obtenirListeJoueursVirtuels();

        if (lstJoueursVirtuels != null) {
            for (int i = 0; i < lstJoueursVirtuels.size(); i++) {
                // Créer le noeud
                objNoeudJoueur = objDocumentXML.createElement("joueur");
                JoueurVirtuel objJoueurVirtuel = (JoueurVirtuel) lstJoueursVirtuels.get(i);

                // On ajoute les attributs nom et id identifiant le joueur
                objNoeudJoueur.setAttribute("nom", objJoueurVirtuel.obtenirNom());
                objNoeudJoueur.setAttribute("id", Integer.toString(objJoueurVirtuel.obtenirIdPersonnage()));
                objNoeudJoueur.setAttribute("role", Integer.toString(1));
                objNoeudJoueur.setAttribute("pointage", Integer.toString(objJoueurVirtuel.obtenirPointage()));

                // Ajouter le noeud de l'item au noeud du paramètre
                objNoeudParametreListeJoueurs.appendChild(objNoeudJoueur);
            }
        }

        // Ajouter le noeud paramètre au noeud de commande dans
        // le document de sortie
        objNoeudCommande.appendChild(objNoeudParametreListeJoueurs);

        // Ajouter le noeud de commande au noeud racine dans le document
        objDocumentXML.appendChild(objNoeudCommande);

        try {
            // Transformer le XML en string
            strCodeXML = ClassesUtilitaires.UtilitaireXML.transformerDocumentXMLEnString(objDocumentXML);

            // Envoyer le message string
            envoyerMessage(strCodeXML);
        } catch (Exception e) {
            objLogger.error(e.getMessage());
        }
    }

    /*
     * Permet d'envoyer un événement pour synchroniser le temps
     * Utiliser lorsque le joueur rejoint une partie après une déconnexion
     */
    private void envoyerSynchroniserTemps(JoueurHumain ancientJoueur) {
        EvenementSynchroniserTemps synchroniser = new EvenementSynchroniserTemps(ancientJoueur.obtenirPartieCourante().obtenirTable().obtenirTempsRestant());

        // Créer l'objet information destination pour envoyer l'information à ce joueur
        InformationDestination objInformationDestination = new InformationDestination(obtenirNumeroCommande(), this);
        synchroniser.ajouterInformationDestination(objInformationDestination);
        synchroniser.envoyerEvenement();

    }

    /**
     * Permet d'envoyer le pointage à un joueur qui se reconnecte
     * @param no 
     */
    private void envoyerPointage(JoueurHumain ancientJoueur, String no) {
        /*
         * <commande nom="Pointage" no="0" type="MiseAJour">
         * <parametre type="Pointage" valeur="123"></parametre></commande>
         */

        // Déclaration d'une variable qui va contenir le code XML à envoyer
        String strCodeXML = "";

        // Appeler une fonction qui va créer un document XML dans lequel
        // on peut ajouter des noeuds
        Document objDocumentXML = UtilitaireXML.obtenirDocumentXML();

        // Créer le noeud de commande à retourner
        Element objNoeudCommande = objDocumentXML.createElement("commande");

        // Créer le noeud du paramètre
        Element objNoeudParametre = objDocumentXML.createElement("parametre");

        // Envoyer une liste des joueurs
        objNoeudCommande.setAttribute("noClient", no);
        objNoeudCommande.setAttribute("type", "MiseAJour");
        objNoeudCommande.setAttribute("nom", "Pointage");
        objNoeudParametre.setAttribute("type", "Pointage");
        objNoeudParametre.setAttribute("valeur", Integer.toString(ancientJoueur.obtenirPartieCourante().obtenirPointage()));

        // Ajouter le noeud paramètre au noeud de commande dans
        // le document de sortie
        objNoeudCommande.appendChild(objNoeudParametre);

        // Ajouter le noeud de commande au noeud racine dans le document
        objDocumentXML.appendChild(objNoeudCommande);

        try {
            // Transformer le XML en string
            strCodeXML = ClassesUtilitaires.UtilitaireXML.transformerDocumentXMLEnString(objDocumentXML);

            // Envoyer le message string
            envoyerMessage(strCodeXML);
        } catch (Exception e) {
            objLogger.error(e.getMessage());
        }
    }

    /*
     * Permet d'envoyer l'argent à un joueur qui se reconnecte
     */
    private void envoyerArgent(JoueurHumain ancientJoueur, String no) {

        /*
         * <commande nom="Argent" no="0" type="MiseAJour">
         * <parametre type="Argent" valeur="123"></parametre></commande>
         *
         */

        // Déclaration d'une variable qui va contenir le code XML à envoyer
        String strCodeXML = "";

        // Appeler une fonction qui va créer un document XML dans lequel
        // on peut ajouter des noeuds
        Document objDocumentXML = UtilitaireXML.obtenirDocumentXML();

        // Créer le noeud de commande à retourner
        Element objNoeudCommande = objDocumentXML.createElement("commande");

        // Créer le noeud du paramètre
        Element objNoeudParametre = objDocumentXML.createElement("parametre");

        // Envoyer une liste des joueurs
        objNoeudCommande.setAttribute("noClient", no);
        objNoeudCommande.setAttribute("type", "MiseAJour");
        objNoeudCommande.setAttribute("nom", "Argent");
        objNoeudParametre.setAttribute("type", "Argent");
        objNoeudParametre.setAttribute("valeur", Integer.toString(ancientJoueur.obtenirPartieCourante().obtenirArgent()));

        // Ajouter le noeud paramètre au noeud de commande dans
        // le document de sortie
        objNoeudCommande.appendChild(objNoeudParametre);

        // Ajouter le noeud de commande au noeud racine dans le document
        objDocumentXML.appendChild(objNoeudCommande);

        try {
            // Transformer le XML en string
            strCodeXML = ClassesUtilitaires.UtilitaireXML.transformerDocumentXMLEnString(objDocumentXML);

            // Envoyer le message string
            envoyerMessage(strCodeXML);
        } catch (Exception e) {
            objLogger.error(e.getMessage());
        }
    }

    /*
     * Permet d'envoyer le numero de la table à un joueur qui se reconnecte
     */
    private void sendTableNumber(JoueurHumain ancientJoueur, String no) {

        /*
         * <commande nom="Argent" no="0" type="MiseAJour">
         * <parametre type="Argent" valeur="123"></parametre></commande>
         *
         */

        // Déclaration d'une variable qui va contenir le code XML à envoyer
        String strCodeXML = "";

        // Appeler une fonction qui va créer un document XML dans lequel
        // on peut ajouter des noeuds
        Document objDocumentXML = UtilitaireXML.obtenirDocumentXML();

        // Créer le noeud de commande à retourner
        Element objNoeudCommande = objDocumentXML.createElement("commande");

	// Créer le noeud du paramètre
	Element objNoeudParametreNbTable = objDocumentXML.createElement("parametre");

	// Create another param for the max nb players in this game
	Element objNoeudParametreMaxPlayers = objDocumentXML.createElement("parametre");

	// Create another param for the game type
	Element objNoeudParametreGameType = objDocumentXML.createElement("parametre");

	// send the id of the game and max nb players in this game
	objNoeudCommande.setAttribute("noClient", no);
	objNoeudCommande.setAttribute("type", "MiseAJour");
	objNoeudCommande.setAttribute("nom", "Table");
	objNoeudParametreNbTable.setAttribute("type", "Table");
	objNoeudParametreNbTable.setAttribute("valeur", Integer.toString(ancientJoueur.obtenirPartieCourante().obtenirTable().obtenirNoTable()));
	objNoeudParametreMaxPlayers.setAttribute("type", "MaxPlayers");
	objNoeudParametreMaxPlayers.setAttribute("valeur", Integer.toString(ancientJoueur.obtenirPartieCourante().obtenirTable().getMaxNbPlayers()));
	objNoeudParametreGameType.setAttribute("type", "GameType");
	objNoeudParametreGameType.setAttribute("valeur", ancientJoueur.obtenirPartieCourante().obtenirTable().getGameType());

	// Ajouter les noeuds paramètres au noeud de commande dans
	// le document de sortie
	objNoeudCommande.appendChild(objNoeudParametreNbTable);
	objNoeudCommande.appendChild(objNoeudParametreMaxPlayers);
	objNoeudCommande.appendChild(objNoeudParametreGameType);
	// Ajouter le noeud de commande au noeud racine dans le document
	objDocumentXML.appendChild(objNoeudCommande);
        try {
            // Transformer le XML en string
            strCodeXML = ClassesUtilitaires.UtilitaireXML.transformerDocumentXMLEnString(objDocumentXML);

            // Envoyer le message string
            envoyerMessage(strCodeXML);
        } catch (Exception e) {
            objLogger.error(e.getMessage());
        }
    }

    /*
     * Permet d'envoyer la liste des items d'un joueur qui rejoint une partie
     */
    private void envoyerItemsJoueurDeconnecte(JoueurHumain ancientJoueur, String no) {
        /*
         * <commande nom="ListeItems" no="0" type="MiseAJour">
         * <parametre type="ListeItems"><item nom="item1"/>
         * <item nom="item2"/></parametre></commande>
         *
         */

        // Déclaration d'une variable qui va contenir le code XML à envoyer
        String strCodeXML = "";

        // Appeler une fonction qui va créer un document XML dans lequel 
        // on peut ajouter des noeuds
        Document objDocumentXML = UtilitaireXML.obtenirDocumentXML();

        // Créer le noeud de commande à retourner
        Element objNoeudCommande = objDocumentXML.createElement("commande");

        // Créer le noeud du paramètre
        Element objNoeudParametre = objDocumentXML.createElement("parametre");

        // Envoyer une liste des items
        objNoeudCommande.setAttribute("noClient", no);
        objNoeudCommande.setAttribute("type", "MiseAJour");
        objNoeudCommande.setAttribute("nom", "ListeObjets");

        // Créer le noeud pour le paramètre contenant la liste
        // des items à retourner
        Element objNoeudParametreListeItems = objDocumentXML.createElement("parametre");

        // On ajoute un attribut type qui va contenir le type
        // du paramètre
        objNoeudParametreListeItems.setAttribute("type", "ListeObjets");

        // Obtenir la liste des items du joueur déconnecté
        HashMap<Integer, ObjetUtilisable> lstListeItems = ancientJoueur.obtenirPartieCourante().obtenirListeObjets();

        // Générer un nouveau numéro de commande qui sera
        // retourné au client
        genererNumeroReponse();

        // Passer toutes les items et créer un noeud pour
        // chaque item et l'ajouter au noeud de paramètre
        for (ObjetUtilisable objItem : lstListeItems.values()) {
            // Créer le noeud de la table courante
            Element objNoeudItem = objDocumentXML.createElement("objet");

            // On ajoute un attribut id qui va contenir le
            // numéro identifiant l'item
            objNoeudItem.setAttribute("id", Integer.toString(objItem.obtenirId()));

            // On ajoute le type de l'item
            objNoeudItem.setAttribute("type", objItem.getClass().getSimpleName());

            // Ajouter le noeud de l'item au noeud du paramètre
            objNoeudParametreListeItems.appendChild(objNoeudItem);
        }

        // Ajouter le noeud paramètre au noeud de commande dans
        // le document de sortie
        objNoeudCommande.appendChild(objNoeudParametreListeItems);

        // Ajouter le noeud de commande au noeud racine dans le document
        objDocumentXML.appendChild(objNoeudCommande);

        try {
            // Transformer le XML en string
            strCodeXML = ClassesUtilitaires.UtilitaireXML.transformerDocumentXMLEnString(objDocumentXML);

            // Envoyer le message string
            envoyerMessage(strCodeXML);
        } catch (Exception e) {
            objLogger.error(e.getMessage());
        }
    }

    /*
     * Cette fonction permet de traiter le message "AcheterObjet"
     */
    private void traiterCommandeAcheterObjet(Element objNoeudCommandeEntree, Element objNoeudCommande, Document objDocumentXMLEntree, Document objDocumentXMLSortie, boolean bolDoitRetournerCommande) {
        // Obtenir l'id de l'objet a acheté
        int intIdObjet = Integer.parseInt(obtenirValeurParametre(objNoeudCommandeEntree, "id").getNodeValue());

        // Si le joueur n'est pas connecté au serveur de jeu, alors il
        // y a une erreur
        if (objJoueurHumain == null) {
            // Le joueur ne peut pas acheter un objet
            // s'il n'est pas connecté au serveur de jeu
            objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
        } // Si le joueur n'est connecté à aucune salle, alors il ne
        // peut pas acheter un objet
        else if (objJoueurHumain.obtenirSalleCourante() == null) {
            // Le joueur ne peut pas acheter un objet
            // s'il n'est pas dans une salle
            objNoeudCommande.setAttribute("nom", "JoueurPasDansSalle");
        } //TODO: Il va falloir synchroniser cette validation lorsqu'on va
        // avoir codé la commande SortirJoueurTable -> èa va ressembler au
        // processus d'authentification
        // Si le joueur n'est dans aucune table, alors il y a
        // une erreur
        else if (objJoueurHumain.obtenirPartieCourante() == null) {
            // Le joueur ne peut pas acheter un objet
            // s'il n'est dans aucune table
            objNoeudCommande.setAttribute("nom", "JoueurPasDansTable");
        } // Si la partie n'est pas commencée, alors il y a une erreur
        else if (objJoueurHumain.obtenirPartieCourante().obtenirTable().estCommencee() == false) {
            // Le joueur ne peut pas acheter un objet
            // si la partie n'est pas commencée
            objNoeudCommande.setAttribute("nom", "PartiePasDemarree");
        } else {
            // Aller chercher l'objet sur la case oè le joueur se trouve
            // présentement (peut retourner null)
            Objet objObjet = objJoueurHumain.obtenirPartieCourante().obtenirObjetCaseCourante();
            Table objTable = objJoueurHumain.obtenirPartieCourante().obtenirTable();

            // Vérifier si l'objet est un magasin
            if (objObjet instanceof Magasin) {
                // Synchronisme sur l'objet magasin
                synchronized (objObjet) {
                    // Vérifier si le magasin vend l'objet avec id = intIdObjet
                    if (((Magasin) objObjet).objetExiste(intIdObjet)) {
                        // Aller chercher l'objet voulu
                        ObjetUtilisable objObjetVoulu = ((Magasin) objObjet).obtenirObjet(intIdObjet);

                        // Vérifier si assez de points pour acheter cet objet
                        if (objJoueurHumain.obtenirPartieCourante().obtenirArgent() < objObjetVoulu.obtenirPrix()) {
                            // Le joueur n'a pas assez de points pour acheter cet objet
                            objNoeudCommande.setAttribute("nom", "PasAssezDArgent");
                        } else {
                            // Acheter l'objet
                            Integer idProchainObjet = objTable.getAndIncrementNewIdObject();
                            System.out.println("New - " + idProchainObjet);
                            ObjetUtilisable objObjetAcheter = ((Magasin) objObjet).acheterObjet(intIdObjet, idProchainObjet);

                            // L'ajouter à la liste des objets du joueur
                            objJoueurHumain.obtenirPartieCourante().ajouterObjetUtilisableListe(objObjetAcheter);

                            // Défrayer les coéts
                            objJoueurHumain.obtenirPartieCourante().definirArgent(objJoueurHumain.obtenirPartieCourante().obtenirArgent() - objObjetAcheter.obtenirPrix());

                            // Préparer un événement pour les autres joueurs de la table
                            // pour qu'il se tienne à jour de l'argent de ce joueur
                            objJoueurHumain.obtenirPartieCourante().obtenirTable().preparerEvenementMAJArgent(objJoueurHumain.obtenirNomUtilisateur(),
                                    objJoueurHumain.obtenirPartieCourante().obtenirArgent());

                            // Retourner une réponse positive au joueur
                            objNoeudCommande.setAttribute("type", "Reponse");
                            objNoeudCommande.setAttribute("nom", "Ok");

                            // Ajouter l'objet acheté dans la réponse
                            Element objNoeudObjetAchete = objDocumentXMLSortie.createElement("objetAchete");
                            objNoeudObjetAchete.setAttribute("type", objObjetAcheter.obtenirTypeObjet());
                            objNoeudObjetAchete.setAttribute("id", Integer.toString(intIdObjet));
                            objNoeudObjetAchete.setAttribute("newId", Integer.toString(idProchainObjet));
                            objNoeudCommande.appendChild(objNoeudObjetAchete);

                            /*
                            // Ajouter l'id du nouvel objet dans la réponse
                            Element objNoeudNouveauID = objDocumentXMLSortie.createElement("nouveauID");
                            objNoeudNouveauID.setAttribute("id", Integer.toString(idProchainObjet-1));  //TODO  why -1
                            objNoeudCommande.appendChild(objNoeudNouveauID);*/

                            Element objNoeudParametreArgent = objDocumentXMLSortie.createElement("parametre");
                            Text objNoeudTexteArgent = objDocumentXMLSortie.createTextNode(Integer.toString(objJoueurHumain.obtenirPartieCourante().obtenirArgent()));
                            objNoeudParametreArgent.setAttribute("type", "Argent");
                            objNoeudParametreArgent.appendChild(objNoeudTexteArgent);
                            objNoeudCommande.appendChild(objNoeudParametreArgent);
                        }
                    } else {
                        // Ce magasin ne vend pas cet objet (l'objet peut avoir
                        // été acheté entre-temps)
                        objNoeudCommande.setAttribute("nom", "ObjetInexistant");
                    }
                }
            } else {
                // Le joueur n'est pas sur un magasin
                objNoeudCommande.setAttribute("nom", "PasDeMagasin");
            }
        }
    }

    /*
     * Cette fonction permet de traiter le message "UtiliserObjet"
     */
    private void traiterCommandeUtiliserObjet(Element objNoeudCommandeEntree, Element objNoeudCommande, Document objDocumentXMLEntree, Document objDocumentXMLSortie, boolean bolDoitRetournerCommande) {
        // Obtenir l'id de l'objet a utilisé
        int intIdObjet = Integer.parseInt(obtenirValeurParametre(objNoeudCommandeEntree, "id").getNodeValue());


        // Si le joueur n'est pas connecté au serveur de jeu, alors il
        // y a une erreur
        if (obtenirValeurParametre(objNoeudCommandeEntree, "player").getNodeValue() == null) {
            // Le joueur ne peut pas utiliser un objet
            // s'il n'est pas connecté au serveur de jeu
            objNoeudCommande.setAttribute("nom", "InvalidePlayer");
        }
        // Si le joueur n'est pas connecté au serveur de jeu, alors il
        // y a une erreur
        if (objJoueurHumain == null) {
            // Le joueur ne peut pas utiliser un objet
            // s'il n'est pas connecté au serveur de jeu
            objNoeudCommande.setAttribute("nom", "JoueurNonConnecte");
        } // Si le joueur n'est connecté à aucune salle, alors il ne
        // peut pas utiliser un objet
        else if (objJoueurHumain.obtenirSalleCourante() == null) {
            // Le joueur ne peut pas utiliser un objet
            // s'il n'est pas dans une salle
            objNoeudCommande.setAttribute("nom", "JoueurPasDansSalle");
        } //TODO: Il va falloir synchroniser cette validation lorsqu'on va
        // avoir codé la commande SortirJoueurTable -> èa va ressembler au
        // processus d'authentification
        // Si le joueur n'est dans aucune table, alors il y a
        // une erreur
        else if (objJoueurHumain.obtenirPartieCourante() == null) {
            // Le joueur ne peut pas utiliser un objet
            // s'il n'est dans aucune table
            objNoeudCommande.setAttribute("nom", "JoueurPasDansTable");
        } // Si la partie n'est pas commencée, alors il y a une erreur
        else if (objJoueurHumain.obtenirPartieCourante().obtenirTable().estCommencee() == false) {
            // Le joueur ne peut pas utiliser un objet
            // si la partie n'est pas commencée
            objNoeudCommande.setAttribute("nom", "PartiePasDemarree");
        } else if (objJoueurHumain.obtenirPartieCourante().joueurPossedeObjet(intIdObjet) == false) {
            // Le joueur ne possède pas cet objet
            objNoeudCommande.setAttribute("nom", "ObjetInvalide");
        } else {
            String playerName = obtenirValeurParametre(objNoeudCommandeEntree, "player").getNodeValue();
            // Obtenir l'objet
            ObjetUtilisable objObjetUtilise = objJoueurHumain.obtenirPartieCourante().obtenirObjetUtilisable(intIdObjet);

            // Obtenir le type de l'objet a utilisé
            String strTypeObjet = objObjetUtilise.obtenirTypeObjet();

            // On prépare la réponse
            objNoeudCommande.setAttribute("nom", "RetourUtiliserObjet");

            // De faèon générale, on n'a pas à envoyer de réponse
            bolDoitRetournerCommande = false;

            // Enlever l'objet de la liste des objets du joueur
            objJoueurHumain.enleverObjet(intIdObjet, strTypeObjet);

            // Dépendamment du type de l'objet, on effectue le traitement approprié
            if (strTypeObjet.equals("Livre")) {
                // Le livre est utilisé lorsqu'un joueur se fait poser une question
                // à choix de réponse. Le serveur renvoie alors une mauvaise réponse
                // à la question, et le client fera disparaître ce choix de réponse
                // parmi les choix possibles pour le joueur.
                // On obtient une mauvaise réponse à la dernière question posée
                String mauvaiseReponse = objJoueurHumain.obtenirPartieCourante().getObjBoiteQuestions().obtenirMauvaiseReponse(objJoueurHumain.obtenirPartieCourante().obtenirQuestionCourante());

                // Créer le noeud contenant le choix de réponse si c'était une question à choix de réponse
                Element objNoeudParametreMauvaiseReponse = objDocumentXMLSortie.createElement("parametre");
                Text objNoeudTexteMauvaiseReponse = objDocumentXMLSortie.createTextNode(mauvaiseReponse);
                objNoeudParametreMauvaiseReponse.setAttribute("type", "MauvaiseReponse");
                objNoeudParametreMauvaiseReponse.appendChild(objNoeudTexteMauvaiseReponse);
                objNoeudCommande.setAttribute("type", "Livre");
                objNoeudCommande.appendChild(objNoeudParametreMauvaiseReponse);
                bolDoitRetournerCommande = true;

                collectPlayerAnswers("Book");

            } else if (strTypeObjet.equals("Boule")) {
                // La boule permettra à un joueur de changer de question si celle
                // qu'il s'est fait envoyer ne lui tente pas
                // On trouve une nouvelle question à poser

                // last true - to find a question of less difficulty than used now
                Question nouvelleQuestion = objJoueurHumain.obtenirPartieCourante().trouverQuestionAPoserCristall(objJoueurHumain, true);
                /*
                // Si on est tombé sur la mème question, on recommence jusqu'à 10 fois
                int essais=0;
                while(nouvelleQuestion.obtenirCodeQuestion() == oldQuestion && essais < 10)
                {
                nouvelleQuestion = objJoueurHumain.obtenirPartieCourante().trouverQuestionAPoser(objJoueurHumain.obtenirPartieCourante().obtenirQuestionCourante().obtenirDifficulte(), true);
                essais++;
                } */

                // On prépare l'envoi des informations sur la nouvelle question
                Element objNoeudParametreNouvelleQuestion = objDocumentXMLSortie.createElement("parametre");
                objNoeudParametreNouvelleQuestion.setAttribute("type", "nouvelleQuestion");
                Element objNoeudParametreQuestion = objDocumentXMLSortie.createElement("question");
                objNoeudParametreQuestion.setAttribute("id", Integer.toString(nouvelleQuestion.obtenirCodeQuestion()));
                objNoeudParametreQuestion.setAttribute("type", TypeQuestion.getValue(nouvelleQuestion.obtenirTypeQuestion()));
                objNoeudParametreQuestion.setAttribute("url", nouvelleQuestion.obtenirURLQuestion());
                objNoeudParametreNouvelleQuestion.appendChild(objNoeudParametreQuestion);
                objNoeudCommande.setAttribute("type", "Boule");
                objNoeudCommande.appendChild(objNoeudParametreNouvelleQuestion);
                bolDoitRetournerCommande = true;

                //******************************************************************
                //int poz = questionsAnswers.lastIndexOf("*");
                //int end = questionsAnswers.length();
                //questionsAnswers.delete(poz,end-1);
                collectPlayerAnswers("Cristall");
                collectPlayerAnswers(nouvelleQuestion);

                lastQuestionTime = objJoueurHumain.obtenirPartieCourante().obtenirTable().obtenirTempsRestant();
                //System.out.println("Protocol for Cristall : " + lastQuestionTime);
            } else if (strTypeObjet.equals("PotionGros")) {
                // La PotionGros fait grossir le joueur
                objNoeudCommande.setAttribute("type", "OK");
                objJoueurHumain.obtenirPartieCourante().obtenirTable().preparerEvenementUtiliserObjet(objJoueurHumain.obtenirNomUtilisateur(), objJoueurHumain.obtenirNomUtilisateur(), "PotionGros", "");
            } else if (strTypeObjet.equals("PotionPetit")) {
                // La PotionPetit fait rapetisser le joueur
                objNoeudCommande.setAttribute("type", "OK");
                objJoueurHumain.obtenirPartieCourante().obtenirTable().preparerEvenementUtiliserObjet(objJoueurHumain.obtenirNomUtilisateur(), objJoueurHumain.obtenirNomUtilisateur(), "PotionPetit", "");
            } else if (strTypeObjet.equals("Banane")) {

                // La partie ici ne fait que sélectionner le joueur qui sera affecté
                // Le reste se fait dans Banane.java
                objNoeudCommande.setAttribute("type", "OK");

                boolean estHumain = false; //Le joueur choisi est'il humain?

                // On obtient la liste des joueurs humains, puis la liste des joueurs virtuels
                HashMap<String, JoueurHumain> listeJoueursHumains = objJoueurHumain.obtenirPartieCourante().obtenirTable().obtenirListeJoueurs();
                for (JoueurHumain objJoueur : listeJoueursHumains.values()) {
                    if (objJoueur.obtenirNomUtilisateur().equals(playerName))
                        estHumain = true;
                }

                objJoueurHumain.obtenirPartieCourante().obtenirTable().preparerEvenementUtiliserObjet(objJoueurHumain.obtenirNomUtilisateur(), playerName, "Banane", "");
                //System.out.println("Protocole joueur 4189 Banane " + objJoueurHumain.obtenirNomUtilisateur() + " " + playerName);
                if (estHumain) {
                    JoueurHumain joueur = objJoueurHumain.obtenirPartieCourante().obtenirTable().obtenirJoueurHumainParSonNom(playerName);
                    if (joueur != null) {
                        joueur.obtenirPartieCourante().getBananaState().bananaIsTossed();
                    }
                    //Banane.utiliserBanane(objJoueurHumain, playerName, estHumain);
                }
            } else if (strTypeObjet.equals("Brainiac")) {
                //all is did on the time when the player go on the case
            }


        }
    }// end method

    /* Cette procédure permet de créer la liste des objets en vente
     * dans un magasin. On appelle cette méthode lorsqu'un joueur répond
     * à une question et tombe sur un magasin. On lui envoie donc la liste
     * des objets en vente.
     * 
     * @param Magasin objMagasin: Le magasin en question
     * @param Document objDocumentXMLSortie: Le document XML dans lequel ajouter
     *                                       les informations
     */
    private void creerListeObjetsMagasin(Magasin objMagasin, Document objDocumentXMLSortie, Element objNoeudCommande) {
        // Créer l'élément objetsMagasin
        Element objNoeudObjetsMagasin = objDocumentXMLSortie.createElement("objetsMagasin");

        synchronized (objMagasin) {
            // Obtenir la liste des objets en vente au magasin
            ArrayList<ObjetUtilisable> lstObjetsEnVente = objMagasin.obtenirListeObjetsUtilisables();

            // Créer le message XML en parcourant la liste des objets en vente
            for (int i = 0; i < lstObjetsEnVente.size(); i++) {
                // Aller chercher cet objet
                ObjetUtilisable objObjetEnVente = (ObjetUtilisable) lstObjetsEnVente.get(i);

                // Aller chercher le type de l'objet (son type en String)
                String strNomObjet = objObjetEnVente.obtenirTypeObjet();

                // Aller chercher le prix de l'objet
                int intPrixObjet = objObjetEnVente.obtenirPrix();

                // Aller chercher l'id de l'objet
                int intObjetId = objObjetEnVente.obtenirId();

                // Créer un élément pour cet objet
                Element objNoeudObjet = objDocumentXMLSortie.createElement("objet");

                // Ajouter l'attribut type de l'objet
                objNoeudObjet.setAttribute("type", strNomObjet);

                // Ajouter l'attribut pour le coét de l'objet
                objNoeudObjet.setAttribute("cout", Integer.toString(intPrixObjet));

                // Ajouter l'attribut pour l'id de l'objet
                objNoeudObjet.setAttribute("id", Integer.toString(intObjetId));

                // Maintenant ajouter cet objet à la liste
                objNoeudObjetsMagasin.appendChild(objNoeudObjet);
            }
        }
        objNoeudCommande.appendChild(objNoeudObjetsMagasin);
    }//end method

    /*
     * Methode that collect players answers in a string
     */
    private void collectPlayerAnswers(int reponseTime) {
        questionsAnswers.append(" t:" + reponseTime + "||");

    }// end methode

    /*
     * Methode that collect players answers in a string
     */
    private void collectPlayerAnswers(String strReponse, boolean valide) {
        questionsAnswers.append(" r:" + strReponse + " c:" + valide);

    }// end methode

    /*
     * Methode that collect players answers in a string
     */
    private void collectPlayerAnswers(String object) {
        if (object.equalsIgnoreCase("Cristall")) {
            questionsAnswers.append(": Cristall ||");
        } else if (object.equalsIgnoreCase("Book")) {
            questionsAnswers.append(": Book");
        }

    }// end methode

    /*
     *Methode that collect players questions in the same string before inserting it in  DB 
     */
    private void collectPlayerAnswers(Question question) {
        questionsAnswers.append(" q:" + question.obtenirCodeQuestion());
    }//end methode

    // getter 
    public String getQuestionsAnswers() {
        return questionsAnswers.toString();
    }

    public String getLang() {
        return langue;
    }
}// end class

