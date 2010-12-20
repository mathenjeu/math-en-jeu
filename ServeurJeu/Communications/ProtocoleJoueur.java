package ServeurJeu.Communications;

import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ClassesRetourFonctions.RetourVerifierReponseEtMettreAJourPlateauJeu;
import ClassesUtilitaires.UtilitaireEncodeurDecodeur;
import ClassesUtilitaires.UtilitaireNombres;
import ClassesUtilitaires.UtilitaireXML;
import Enumerations.Commande;
import Enumerations.Filtre;
import Enumerations.TypeQuestion;
import Enumerations.RetourFonctions.ResultatAuthentification;
import Enumerations.RetourFonctions.ResultatDemarrerPartie;
import Enumerations.RetourFonctions.ResultatEntreeTable;
import ServeurJeu.ControleurJeu;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.ComposantesJeu.InformationPartie;
import ServeurJeu.ComposantesJeu.Question;
import ServeurJeu.ComposantesJeu.RapportDeSalle;
import ServeurJeu.ComposantesJeu.Salle;
import ServeurJeu.ComposantesJeu.Table;
import ServeurJeu.ComposantesJeu.Joueurs.Joueur;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;
import ServeurJeu.ComposantesJeu.Objets.Objet;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.ObjetUtilisable;
import ServeurJeu.Configuration.GestionnaireMessages;
import ServeurJeu.Evenements.EvenementPartieDemarree;
import ServeurJeu.Evenements.EvenementSynchroniserTemps;
import ServeurJeu.Evenements.InformationDestination;
import ServeurJeu.Monitoring.Moniteur;

/**
 * @author Jean-François Brind'Amour
 * 
 */
public class ProtocoleJoueur implements Runnable
{
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
    // Cette variable permet de savoir s'il faut arrêter le thread ou non
    private boolean bolStopThread;
    // Déclaration d'une référence vers un joueur humain correspondant à ce
    // protocole
    private JoueurHumain objJoueurHumain;
    // Déclaration d'une variable qui va servir de compteur pour envoyer des
    // commandes ou événements au joueur de ce ProtocoleJoueur (sa valeur
    // maximale est MAX_COMPTEUR, après MAX_COMPTEUR on recommence à 0)
    private int intCompteurCommande;
    // Déclaration d'une contante gardant le maximum possible pour le
    // compteur de commandes du serveur de jeu
    private final int MAX_COMPTEUR = 100;
    // Déclaration d'une variable qui va contenir le numéro de commande à
    // retourner au client ayant fait une requête au serveur
    private int intNumeroCommandeReponse;
    private static final Logger objLogger = Logger.getLogger(ProtocoleJoueur.class);
    // On obtiendra la langue du joueur pour pouvoir construire la boîte de questions
    private String langue;
    // Déclaration d'une variable qui va permettre de savoir si le joueur
    // en en train de joueur une partie ou non. Cet état sera utile car on
    // ne déconnectera pas un joeur en train de joueur via le vérification de connexion
    private boolean bolEnTrainDeJouer;
    // time in seconds as reference to calculate time of reponse
    private int lastQuestionTime;

    /**
     * Constructeur de la classe ProtocoleJoueur qui permet de garder une 
     * référence vers le contrôleur de jeu, vers le gestionnaire des
     * communications et vers le socket du joueur demandant la connexion
     * et de s'assurer qu'il n'y a pas de délai.
     *
     * @param controleur   : Le contrôleur du jeu
     * @param verificateur : Le vérificateur des connexions
     * @param socketJoueur : Le canal de communication associé au joueur
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
        //bolEnTrainDeJouer = false;
        objLogger.info(GestionnaireMessages.message("protocole.connexion").replace("$$CLIENT$$", socketJoueur.getInetAddress().toString()));

        try {
            // for games or gui interactive application it must be set to true to
        	// sent all the packets as soon as possible without buffering
            objSocketJoueur.setTcpNoDelay(true);
            
        } catch (SocketException se) {
            objLogger.error(GestionnaireMessages.message("protocole.canal_ferme"));

            // Arrêter le thread
            bolStopThread = true;
        }

    }

    /**
     * Cette méthode est appelée automatiquement par le thread du joueur et elle
     * permet d'exécuter le protocole du joueur courant.
     *
     * @synchronism Cette méthode n'a pas besoin d'être synchronisée
     */
    public void run() {
        try {
        	
        	
        	objSocketJoueur.setSoLinger(true, 3000);
        	objSocketJoueur.setKeepAlive(true);
        	
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
            	
                try{
                      Thread.sleep(100);
                }catch(InterruptedException ie )
                {
                	objLogger.error("Error to did thead to sleep..." + ie.getMessage());
                	Thread.currentThread().interrupt();

                }

                // Déclaration d'une variable qui va servir de marqueur
                // pour savoir où on en est rendu dans la lecture
                int intMarqueur = 0;

                // Déclaration d'une variable qui va contenir le nombre de
                // bytes réellement lus dans le canal
                int intBytesLus = objCanalReception.read(byttBuffer);

                // Si le nombre de bytes lus est -1, alors c'est que le
                // stream a été fermé, il faut donc terminer le thread
                if (intBytesLus == -1) {
                    objLogger.error("Une erreur est survenue: nombre d'octets lus = -1");
                    bolStopThread = true;
                }

                // Passer tous les bytes lus dans le canal de réception et
                // découper le message en chaîne de commandes selon le byte
                // 0 marquant la fin d'une commande
                for (int i = 0; i < intBytesLus; i++) {
                    // Si le byte courant est le byte de fin de message (EOM)
                    // alors c'est qu'une commande vient de finir, on va donc
                    // traiter la commande reçue
                    if (byttBuffer[i] == (byte)0) {
                        // Créer une chaîne temporaire qui va garder la chaîne
                        // de caractères lue jusqu'à maintenant
                        String strChaineAccumulee = new String(byttBuffer,
                                intMarqueur, i - intMarqueur);

                        // Ajouter la chaîne courante à la chaîne de commande
                        strMessageRecu.append(strChaineAccumulee);

                        // On appelle une fonction qui va traiter le message reçu du
                        // client et mettre le résultat à retourner dans une variable
                        objLogger.info(GestionnaireMessages.message("protocole.message_recu") + strMessageRecu);

                        // If we're in debug mode (can be set in mathenjeu.xml), print communications
                        GregorianCalendar calendar = new GregorianCalendar();
                        if (ControleurJeu.modeDebug) {
                            String timeB = "" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
                            System.out.println("(" + timeB + ") Reçu:  " + strMessageRecu);
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
                // caractères lus, alors c'est qu'on n'a pas encore reçu
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
                objLogger.error(ioe.getMessage() + " on close the socket!");
            }

            // to be sure...
            this.bolStopThread = true;
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
                //System.out.println("! Joueur deconnecter - Protocole ");          

            }

            // Enlever le protocole du joueur courant de la liste des
            // protocoles de joueurs
            objGestionnaireCommunication.supprimerProtocoleJoueur(this);
        }

        String inetAddress = objSocketJoueur.getInetAddress() == null ? "[?.?.?.?]" : objSocketJoueur.getInetAddress().toString();
        objLogger.info(GestionnaireMessages.message("protocole.fin_thread").replace("$$CLIENT$$", inetAddress));
    }

    // On vérifie si le joueur est bel et bien dans la BD et si son mot de passe est correct
    // S'il y a erreur:  on ajoute l'erreur à l'attribut "nom" du noeudCommande et on retourne true
    // Sinon: on returne false
    private boolean erreurAuthentification(Element noeudCommande, String nomUtilisateur, String motDePasse) {
        ResultatAuthentification resultatAuthentification =
                                 objControleurJeu.authentifierJoueur(this, nomUtilisateur, motDePasse, true);
        switch (resultatAuthentification) {
            case JoueurDejaConnecte:
            case JoueurNonConnu:
                noeudCommande.setAttribute("nom", resultatAuthentification.toString());
                return true;
            case Succes:
                return false;
        }
        return false;
    }

    // On vérifie si le joueur est _au moins_ un prof (prof ou admin)
    // Si non: on met "JoueurNonAutorise" à l'attribut "nom" du noeudCommade et on retourne true
    // Si oui: on retourne false
    private boolean erreurUserRole(Element noeudCommande, String nomUtilisateur, String motDePasse) {
        if (objControleurJeu.obtenirGestionnaireBD().getUserRole(nomUtilisateur, motDePasse) == 1) {
            // Le joeur n'est pas un prof ni un admin
            noeudCommande.setAttribute("nom", "JoueurNonAutorise");
            return true;
        }
        return false;
    }

    //On vérifie si le joueur est connecté
    //Si non: on met "JoueurNonConnecte" à l'attribut "nom" du noeudCommande et on retourne true
    //Si oui: on retourne false
    private boolean erreurJoueurNonConnecte(Element noeudCommande) {
        if (objJoueurHumain == null) {
            noeudCommande.setAttribute("nom", "JoueurNonConnecte");
            return true;
        }
        return false;
    }

    //On vérifie si le joueur est déconnecté (indiquant qu'il est dans la liste des joueurs déconectés)
    //Si non: on met "JoueurNonDeconnecte" à l'attribut "nom" du noeudCommande et on retourne true
    //Si oui: on retourne false
    private boolean erreurJoueurNonDeconnecte(Element noeudCommande) {
        if (!objControleurJeu.estJoueurDeconnecte(objJoueurHumain.obtenirNomUtilisateur())) {
            noeudCommande.setAttribute("nom", "JoueurNonDeconnecte");
            return true;
        }
        return false;
    }

    //On vérifie si le joueur est à une table
    //Si pas de table: on met "JoueurPasDansTable" à l'attribut "nom" du noeudCommande et on retourne true
    //Si table: on retourne false
    private boolean erreurJoueurPasDansTable(Element noeudCommande) {
        if (objJoueurHumain.obtenirPartieCourante() == null) {
            noeudCommande.setAttribute("nom", "JoueurPasDansTable");
            return true;
        }
        return false;
    }

    //On vérifie si le joueur est à une table
    //Si table: on met "JoueurDansTable" à l'attribut "nom" du noeudCommande et on retourne true
    //Si pas de table: on retourne false
    private boolean erreurJoueurDansTable(Element noeudCommande) {
        if (objJoueurHumain.obtenirPartieCourante() != null) {
            noeudCommande.setAttribute("nom", "JoueurDansTable");
            return true;
        }
        return false;
    }

    //On vérifie si la partie est commencée
    //Si non: on met "PartiePasDemarree" à l'attribut "nom" du noeudCommande et on retourne true
    //Si oui: on retourne false
    private boolean erreurPartiePasDemarree(Element noeudCommande) {
        if (objJoueurHumain.obtenirPartieCourante().obtenirTable().estCommencee() == false) {
            noeudCommande.setAttribute("nom", "PartiePasDemarree");
            return true;
        }
        return false;
    }

    //On vérifie si le joueur possède une question courante.
    //Si non: on met messageErreur à l'attribut "nom" du noeudCommande et on retourne true
    //Si oui: on retourne false
    //On vérifie si le joueur à une question courante.
    //Si non: on met <messageErreur> à l'attribut "nom" du noeudCommande et on retourne true
    //Si oui: on retourne false
    private boolean erreurPasDeQuestion(Element noeudCommande, String messageErreur) {
        if (objJoueurHumain.obtenirPartieCourante().obtenirQuestionCourante() == null) {
            noeudCommande.setAttribute("nom", messageErreur);
            return true;
        }
        return false;
    }

    //On vérifie si le joueur possède une question courante.
    //Si oui: on met messageErreur à l'attribut "nom" du noeudCommande et on retourne true
    //Si non: on retourne false
    private boolean erreurQuestionPresente(Element noeudCommande, String messageErreur) {
        if (objJoueurHumain.obtenirPartieCourante().obtenirQuestionCourante() != null) {
            noeudCommande.setAttribute("nom", messageErreur);
            return true;
        }
        return false;
    }

    //On vérifie si le joueur est dans une salle
    //Si non: on met "JoueurDansSalle" à l'attribut "nom" du noeudCommande et on retourne true
    //Si oui: on retourne false
    private boolean erreurJoueurDansSalle(Element noeudCommande) {
        if (objJoueurHumain.obtenirSalleCourante() != null) {
            noeudCommande.setAttribute("nom", "JoueurDansSalle");
            return true;
        }
        return false;
    }

    //On vérifie si le joueur est dans une salle
    //Si oui: on met "JoueurPasDansSalle" à l'attribut "nom" du noeudCommande et on retourne true
    //Si non: on retourne false
    private boolean erreurJoueurPasDansSalle(Element noeudCommande) {
        if (objJoueurHumain.obtenirSalleCourante() == null) {
            noeudCommande.setAttribute("nom", "JoueurPasDansSalle");
            return true;
        }
        return false;
    }

    //On vérifie si la salle spécifiée existe
    //Si non: on met "SalleNonExistante" à l'attribut "nom" du noeudCommande et on retourne true
    //Si oui: on retourne false
    private boolean erreurSalleNonExistante(Element noeudCommande, int roomId) {
        if (!objControleurJeu.salleExiste(roomId)) {
            noeudCommande.setAttribute("nom", "SalleNonExistante");
            return true;
        }
        return false;
    }

    //On vérifie si les règles permettent le chat
    //Si non: on met "ChatNonPermis" à  l'attribut "nom" du noeudCommande et on retourne true
    //Si oui: on retourne false
    private boolean erreurChatNonPermis(Element noeudCommande) {
        if (!objJoueurHumain.obtenirPartieCourante().obtenirTable().getRegles().obtenirPermetChat()) {
            noeudCommande.setAttribute("nom", "ChatNonPermis");
            return true;
        }
        return false;
    }

    private void traiterCommandeConnexion(Element noeudEntree, Element noeudCommande) {
        String nomUtilisateur = obtenirValeurParametre(noeudEntree, "NomUtilisateur").getNodeValue();
        String motDePasse = obtenirValeurParametre(noeudEntree, "MotDePasse").getNodeValue();

        if (erreurAuthentification(noeudCommande, nomUtilisateur, motDePasse)) {
            return;
        }
        //** L'authentification est valide le joueur est maintenant connecté    **/


        //Aucune erreure! On change le type du noeudCommande à  "Reponse"
        //On renvoi le role du joueur (normal,admin,prof) et une liste 
        //d'url de chanson
        Document docSortie = noeudCommande.getOwnerDocument();
        noeudCommande.setAttribute("type", "Reponse");
        if (objControleurJeu.estJoueurDeconnecte(nomUtilisateur)) {
            // Le joueur a été déconnecté et tente de se reconnecter.
            // Il faut lui envoyer une réponse spéciale lui
            // permettant de choisir s'il veut se reconnecter
            noeudCommande.setAttribute("nom", "OkEtPartieDejaCommencee");
        } else 
            noeudCommande.setAttribute("nom", "Musique");

        langue = obtenirValeurParametre(noeudEntree, "Langue").getNodeValue();
        bolEnTrainDeJouer = false;

        // Créer le noeud paramètre contenant le role de joueur
        Element objNoeudParametreRoleJoueur = docSortie.createElement("parametre");
        objNoeudParametreRoleJoueur.setAttribute("type", "userRole");
        objNoeudParametreRoleJoueur.appendChild(docSortie.createTextNode("" + objJoueurHumain.getRole()));
        noeudCommande.appendChild(objNoeudParametreRoleJoueur);

        // On va envoyer dans le noeud la liste de chansons que le joueur pourrait aimer
        ArrayList<Object> liste = objControleurJeu.obtenirGestionnaireBD().obtenirListeURLsMusique(objJoueurHumain);
        for (int i = 0; i < liste.size(); i++) {
            Element objNoeudParametreMusique = docSortie.createElement("parametre");
            objNoeudParametreMusique.setAttribute("type", "musique");
            objNoeudParametreMusique.appendChild(docSortie.createTextNode((String)liste.get(i)));
            noeudCommande.appendChild(objNoeudParametreMusique);
        }

    }

    private void traiterCommandeConnexionProf(Element noeudEntree, Element noeudCommande) {

        String nomUtilisateur = obtenirValeurParametre(noeudEntree, "NomUtilisateur").getNodeValue();
        String motDePasse = obtenirValeurParametre(noeudEntree, "MotDePasse").getNodeValue();

        if (erreurAuthentification(noeudCommande, nomUtilisateur, motDePasse)) {
            return;
        }
        //** L'authentification est valide le joueur est maintenant connecté    **/
        if (erreurUserRole(noeudCommande, nomUtilisateur, motDePasse)) {
            return;
        }

        //Aucune erreure! On change le type du noeudCommande à  "Reponse"
        //On renvoi le role du joueur (normal,admin,prof), 
        //          une liste d'url de chanson, 
        //          une map de [keyword_id]-->[keyword_name,keyword_group_id,keyword_group_name]
        //          une map de [game_type_id]-->[game_type_name]
        Document docSortie = noeudCommande.getOwnerDocument();
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "DBInfo");
        langue = obtenirValeurParametre(noeudEntree, "Langue").getNodeValue();
        bolEnTrainDeJouer = false;


        // Créer le noeud paramètre contenant le role de joueur
        Element objNoeudParametreRoleJoueur = docSortie.createElement("parametre");
        objNoeudParametreRoleJoueur.setAttribute("type", "userRole");
        objNoeudParametreRoleJoueur.appendChild(docSortie.createTextNode("" + objJoueurHumain.getRole()));
        noeudCommande.appendChild(objNoeudParametreRoleJoueur);

        // On va envoyer dans le noeud la liste de chansons que le professeur pourrait aimer
        ArrayList<Object> liste = objControleurJeu.obtenirGestionnaireBD().obtenirListeURLsMusique(objJoueurHumain);
        for (int i = 0; i < liste.size(); i++) {
            Element objNoeudParametreMusique = docSortie.createElement("parametre");
            objNoeudParametreMusique.setAttribute("type", "musique");
            objNoeudParametreMusique.appendChild(docSortie.createTextNode((String)liste.get(i)));
            noeudCommande.appendChild(objNoeudParametreMusique);
        }

        // On envoi dans le noeud la map des [keyword_id-->name,group_id,group_name] dans la langue de la connection
        // Le format XML est: <parametre type="keyword" id="id">name,group_id,group_name</parametre>
        for (Map.Entry<Integer, String> keywordEntry: objControleurJeu.getKeywordsMap(langue).entrySet()) {
            Element objNoeudParametreKeyword = docSortie.createElement("parametre");
            objNoeudParametreKeyword.setAttribute("type", "keyword");
            objNoeudParametreKeyword.setAttribute("id", keywordEntry.getKey().toString());
            objNoeudParametreKeyword.appendChild(docSortie.createTextNode(keywordEntry.getValue()));
            noeudCommande.appendChild(objNoeudParametreKeyword);
        }

        // On envoir dans le noeud la map des [game_type_id-->game_type]
        // Note: Les 'game_type' sont défini en francais seulement dans la BD.
        // Le format XML est: <parametre type="gameType" id="2">Tournament</parametre>
        for (Map.Entry<Integer, String> gameTypeEntry: objControleurJeu.getGameTypesMap().entrySet()) {
            Element objNoeudParametreGameType = docSortie.createElement("parametre");
            objNoeudParametreGameType.setAttribute("type", "gameType");
            objNoeudParametreGameType.setAttribute("id", gameTypeEntry.getKey().toString());
            objNoeudParametreGameType.appendChild(docSortie.createTextNode(gameTypeEntry.getValue()));
            noeudCommande.appendChild(objNoeudParametreGameType);
        }
    }

    // Un joueur déconnecté revient mais décide de ne pas rejoindre sa partie abandonnée
    private void traiterCommandeNePasRejoindrePartie(Element noeudCommande) {

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurNonDeconnecte(noeudCommande)) {
            return;
        }

        // On enlève le joueur déconnecté de la liste des joueurs déconnectés
        // on ne l'enlève pas de la liste des joueurs déconnectés de la table car on ne
        // vérifie qu'avec la liste des joueurs déconnectés du controleur de jeu
        objControleurJeu.enleverJoueurDeconnecte(objJoueurHumain.obtenirNomUtilisateur());
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "Ok");
    }

    // Un joueur déconnecté tente de rejoindre une partie déjà  commencée
    private void traiterCommandeRejoindrePartie(Element noeudEntree, Element noeudCommande) {

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurNonDeconnecte(noeudCommande)) {
            return;
        }

        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "Ok");

        // On renvoie l'état du jeu au joueur pour qu'il puisse reprendre sa partie
        JoueurHumain objAncientJoueurHumain =
                     objControleurJeu.obtenirJoueurHumainJoueurDeconnecte(objJoueurHumain.obtenirNomUtilisateur());

        // Envoyer la liste des joueurs
        envoyerListeJoueurs(objAncientJoueurHumain, noeudEntree.getAttribute("no"));

        // Faire en sorte que le joueur est correctement considéré en train de jouer
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
        envoyerPointage(objAncientJoueurHumain, noeudEntree.getAttribute("no"));

        // envoyer l'argent au joueur
        envoyerArgent(objAncientJoueurHumain, noeudEntree.getAttribute("no"));

        sendTableNumber(objAncientJoueurHumain, noeudEntree.getAttribute("no"));

        // Envoyer la liste des items du joueur qui
        // se reconnecte
        envoyerItemsJoueurDeconnecte(objAncientJoueurHumain, noeudEntree.getAttribute("no"));

        // Synchroniser temps
        envoyerSynchroniserTemps(objAncientJoueurHumain);


    }

    private void traiterCommandeDeconnexion(Element noeudCommande) {

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }

        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "Ok");

        // Si le joueur humain a été défini dans le protocole, alors
        // c'est qu'il a réussi à  se connecter au serveur de jeu, il
        // faut donc aviser le contrà´leur de jeu pour qu'il enlève
        // le joueur du serveur de jeu (il faut obtenir un numéro
        // de commandes de cette fonction)
        //System.out.println("Player " + objJoueurHumain.obtenirNomUtilisateur() + " ask for cancel");
        objControleurJeu.deconnecterJoueur(objJoueurHumain, true, false);
    }

    private void traiterCommandObtenirListeJoueurs(Element noeudCommande) {

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }

        // Il n'y a pas eu d'erreurs on va retourner une liste de joueurs
        genererNumeroReponse();

        Document docSortie = noeudCommande.getOwnerDocument();
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "ListeJoueurs");

        // Créer le noeud pour le paramètre contenant la liste des joueurs
        Element objNoeudParametreListeJoueurs = docSortie.createElement("parametre");
        objNoeudParametreListeJoueurs.setAttribute("type", "ListeNomUtilisateurs");
        HashMap<String, JoueurHumain> lstListeJoueurs = objControleurJeu.obtenirListeJoueurs();
        synchronized (lstListeJoueurs) {
            for (JoueurHumain objJoueur: lstListeJoueurs.values()) {
                Element objNoeudJoueur = docSortie.createElement("joueur");
                objNoeudJoueur.setAttribute("nom", objJoueur.obtenirNomUtilisateur());
                objNoeudParametreListeJoueurs.appendChild(objNoeudJoueur);
            }
        }
        noeudCommande.appendChild(objNoeudParametreListeJoueurs);
    }

    private void traiterCommandeObtenirListeSalles(Element noeudEntree, Element noeudCommande) {

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }

        // Il n'y a pas eu d'erreurs on va retourner une liste des salles actives
        genererNumeroReponse();

        Document docSortie = noeudCommande.getOwnerDocument();
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "ListeSalles");

        String roomsType = obtenirValeurParametre(noeudEntree, "RoomsType").getNodeValue();

        // Créer le noeud pour le paramètre contenant la liste des salles à  retourner
        Element objNoeudParametreListeSalles = docSortie.createElement("parametre");
        objNoeudParametreListeSalles.setAttribute("type", "ListeNomSalles");
        synchronized (objControleurJeu.obtenirListeSalles()) {
            for (Salle objSalle: objControleurJeu.obtenirListeSalles(this.langue, roomsType).values()) {
                //On crée un noeud pour chaque salle
                Element objNoeudSalle = docSortie.createElement("salle");
                objNoeudSalle.setAttribute("nom", objSalle.getRoomName(this.langue));
                objNoeudSalle.setAttribute("id", Integer.toString(objSalle.getRoomId()));
                objNoeudSalle.setAttribute("protegee", Boolean.toString(objSalle.protegeeParMotDePasse()));
                objNoeudSalle.setAttribute("descriptions", objSalle.getRoomDescription(this.langue));
                objNoeudSalle.setAttribute("gameTypes", objSalle.getGameTypeIds().toString());
                objNoeudSalle.setAttribute("userCreator", objSalle.getCreatorUsername());
                objNoeudSalle.setAttribute("masterTime", Integer.toString(objSalle.getMasterTime()));
                objNoeudParametreListeSalles.appendChild(objNoeudSalle);
            }
        }
        noeudCommande.appendChild(objNoeudParametreListeSalles);
    }

    private void traiterCommandeObtenirListeSallesProf(Element noeudEntree, Element noeudCommande) {

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }

        // Il n'y a pas eu d'erreurs on va retourner une liste des salles pour ce prof
        genererNumeroReponse();

        Document docSortie = noeudCommande.getOwnerDocument();
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "ListeSalles");

        // Créer le noeud pour le paramètre contenant la liste des salles à  retourner
        // Un noeud est construit pour chaque salle avec la structure XML suivante:
        // <salle id="25">                                                   (room_id)
        //    <name language_id="xx">Les math au III siecle</name>           (name and description in language 'xx')
        //    <description language_id="xx">Description fr...</description>  (add one <name> and one <description> child for each language)
        //    <password>12345</password>                                     (room password, can be empty)
        //    <beginDate>2010-08-21 18:11:31</beginDate>                     (format: )
        //    <endDate>2010-08-23 18:11:31</endDate>                         (   see GestionnaireBD.mejFormatDateHeure)
        //    <masterTime>30</masterTime>                                    (game length in minutes)
        //    <keywordIds>1,5,6,27,66</keywordIds>                           (keyword_id for the room, comma separated)
        //    <gameTypeIds>1,3</gameTypeIds>                                 (game types ids for the room, comma separated)
        // </salle>
        Element objNoeudParametreListeSalles = docSortie.createElement("parametre");
        objNoeudParametreListeSalles.setAttribute("type", "ListeSalles");
        synchronized (objControleurJeu.obtenirListeSalles()) {
            for (Salle objSalle: objControleurJeu.obtenirListeSallesCreateur(objJoueurHumain.obtenirNomUtilisateur()).values()) {
                Element objNoeudSalle = docSortie.createElement("salle");
                objNoeudSalle.setAttribute("id", Integer.toString(objSalle.getRoomId()));
                //Créer les enfants <name>
                for (Map.Entry<Integer, String> nameEntry: objSalle.getNameMap().entrySet()) {
                    Element objNoeudName = docSortie.createElement("name");
                    objNoeudName.setAttribute("language", objControleurJeu.getLanguageShortName(nameEntry.getKey()));
                    objNoeudName.appendChild(docSortie.createTextNode(nameEntry.getValue()));
                    objNoeudSalle.appendChild(objNoeudName);
                }
                //Créer les enfants <description>
                for (Map.Entry<Integer, String> descriptionEntry: objSalle.getDescriptionMap().entrySet()) {
                    Element objNoeudDescription = docSortie.createElement("description");
                    objNoeudDescription.setAttribute("language", objControleurJeu.getLanguageShortName(descriptionEntry.getKey()));
                    objNoeudDescription.appendChild(docSortie.createTextNode(descriptionEntry.getValue()));
                    objNoeudSalle.appendChild(objNoeudDescription);
                }
                //Créer tous les autres enfants
                Element objNoeudPwd = docSortie.createElement("password"); //peut être = "", mais sera présent quand même
                objNoeudPwd.appendChild(docSortie.createTextNode(objSalle.getPassword()));
                objNoeudSalle.appendChild(objNoeudPwd);
                Element objNoeudBDate = docSortie.createElement("beginDate"); //Ne peut pas être = ""
                objNoeudBDate.appendChild(docSortie.createTextNode(GestionnaireBD.mejFormatDateHeure.format(objSalle.getBeginDate())));
                objNoeudSalle.appendChild(objNoeudBDate);
                Element objNoeudEDate = docSortie.createElement("endDate"); //Peut être = "", mais sera présent quand même
                if (objSalle.getEndDate() != null) {
                    objNoeudEDate.appendChild(docSortie.createTextNode(GestionnaireBD.mejFormatDateHeure.format(objSalle.getEndDate())));
                } else {
                    objNoeudEDate.appendChild(docSortie.createTextNode(""));
                }
                objNoeudSalle.appendChild(objNoeudEDate);
                Element objNoeudTime = docSortie.createElement("masterTime"); //en minutes
                objNoeudTime.appendChild(docSortie.createTextNode("" + objSalle.getMasterTime()));
                objNoeudSalle.appendChild(objNoeudTime);
                Element objNoeudKeywordIds = docSortie.createElement("keywordIds");//une liste de keyword_id séparés par des virgules
                String k_ids = "";
                for (Integer keyword_id: objSalle.getKeywordIds()) {
                    k_ids += keyword_id + ",";
                }
                if (k_ids.endsWith(",")) {
                    k_ids = k_ids.substring(0, k_ids.length() - 1);
                }
                objNoeudKeywordIds.appendChild(docSortie.createTextNode(k_ids));
                objNoeudSalle.appendChild(objNoeudKeywordIds);
                Element objNoeudGameTypes = docSortie.createElement("gameTypeIds");//une liste de game_type_id séparés par des virgules
                String g_types = "";
                for (Integer gameTypeId: objSalle.getGameTypeIds()) {
                    g_types += gameTypeId + ",";
                }
                if (g_types.endsWith(",")) {
                    g_types = g_types.substring(0, g_types.length() - 1);
                }
                objNoeudGameTypes.appendChild(docSortie.createTextNode(g_types));
                objNoeudSalle.appendChild(objNoeudGameTypes);
                objNoeudParametreListeSalles.appendChild(objNoeudSalle);
            }
        }
        noeudCommande.appendChild(objNoeudParametreListeSalles);
    }

    private void traiterCommandeCreateRoom(Element noeudEntree, Element noeudCommande) {

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }

        // Il n'y a pas eu d'erreurs on va créer la salle à  l'ajouter à la BD et au contrôleur de jeu
        String password = "";
        String beginDate = "";
        String endDate = "";
        String masterTime = "";
        String gameTypeIds = "";
        String fullStats = "";
        String keywordIds = "";
        NodeList noeudsParam = noeudEntree.getChildNodes();
        TreeMap<Integer, String> names = new TreeMap<Integer, String>();
        TreeMap<Integer, String> descriptions = new TreeMap<Integer, String>();
        for (int i = 0; i < noeudsParam.getLength(); i++) {
            String paramType = noeudsParam.item(i).getAttributes().getNamedItem("type").getNodeValue();
            if (paramType.equals("password")) {
                if (noeudsParam.item(i).getChildNodes().getLength() == 0) {
                    password = "";
                } else {
                    password = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
                }
            } else if (paramType.equals("beginDate")) {
                beginDate = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
            } else if (paramType.equals("endDate")) {
                if (noeudsParam.item(i).getChildNodes().item(0) != null) {
                    endDate = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
                } else {
                    endDate = "";
                }
            } else if (paramType.equals("masterTime")) {
                masterTime = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
            } else if (paramType.equals("gameTypeIds")) {
                gameTypeIds = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
            } else if (paramType.equals("fullStats")) {
                fullStats = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
            } else if (paramType.equals("keywordIds")) {
                keywordIds = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
            } else {
                String lid = noeudsParam.item(i).getAttributes().getNamedItem("language_id").getNodeValue();
                if (paramType.equals("name")) {
                    names.put(new Integer(lid), noeudsParam.item(i).getChildNodes().item(0).getNodeValue());
                } else {
                    descriptions.put(new Integer(lid), noeudsParam.item(i).getChildNodes().item(0).getNodeValue());
                }
            }
        }

        String roomType = fullStats.equals("1")?"profsType":"General";
        Date dateBeginDate = new Date();
        Date dateEndDate = new Date();
        try {
            dateBeginDate = GestionnaireBD.mejFormatDate.parse(beginDate);
            dateEndDate = GestionnaireBD.mejFormatDate.parse(endDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TreeSet<Integer> setKeywordIds = new TreeSet<Integer>();
        for (String id: keywordIds.split(",")) {
            setKeywordIds.add(Integer.parseInt(id));
        }
        TreeSet<Integer> setGameTypeIds = new TreeSet<Integer>();
        for (String id: gameTypeIds.split(",")) {
            setGameTypeIds.add(Integer.parseInt(id));
        }

        //add room to the DB
        int roomId = objControleurJeu.obtenirGestionnaireBD().putNewRoom(
                password,
                objJoueurHumain.obtenirCleJoueur(),
                names, descriptions,
                beginDate, endDate,
                Integer.parseInt(masterTime),
                fullStats.equals("1")?true:false,
                keywordIds, gameTypeIds);


        objControleurJeu.ajouterNouvelleSalle(new Salle(
                objControleurJeu,
                roomId, password, objJoueurHumain.obtenirNomUtilisateur(), roomType,
                dateBeginDate, dateEndDate, Integer.parseInt(masterTime),
                names, descriptions, setKeywordIds, setGameTypeIds));

        genererNumeroReponse();
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "OK");
    }

    private void traiterCommandeUpdateRoom(Element noeudEntree, Element noeudCommande) {
        //TODO: first check if game are currently being played in this room.  If so,
        //      lock the room so that new games cannot be started and wait until all
        //      games are finished to update.

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }

        // Il n'y a pas eu d'erreurs on va modifier la salle dans la BD et dans le contrôleur de jeu

        String roomId = "";
        String password = "";
        String beginDate = "";
        String endDate = "";
        String masterTime = "";
        String gameTypeIds = "";
        String fullStats = "";
        String keywordIds = "";
        NodeList noeudsParam = noeudEntree.getChildNodes();
        TreeMap<Integer, String> names = new TreeMap<Integer, String>();
        TreeMap<Integer, String> descriptions = new TreeMap<Integer, String>();
        for (int i = 0; i < noeudsParam.getLength(); i++) {
            String paramType = noeudsParam.item(i).getAttributes().getNamedItem("type").getNodeValue();
            if (paramType.equals("roomId")) {
                roomId = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
            } else if (paramType.equals("password")) {
                if (noeudsParam.item(i).getChildNodes().getLength() == 0) {
                    password = "";
                } else {
                    password = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
                }
            } else if (paramType.equals("beginDate")) {
                beginDate = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
            } else if (paramType.equals("endDate")) {
                if (noeudsParam.item(i).getChildNodes().item(0) != null) {
                    endDate = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
                } else {
                    endDate = "";
                }
            } else if (paramType.equals("masterTime")) {
                masterTime = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
            } else if (paramType.equals("gameTypeIds")) {
                gameTypeIds = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
            } else if (paramType.equals("fullStats")) {
                fullStats = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
            } else if (paramType.equals("keywordIds")) {
                keywordIds = noeudsParam.item(i).getChildNodes().item(0).getNodeValue();
            } else {
                String lid = noeudsParam.item(i).getAttributes().getNamedItem("language_id").getNodeValue();
                if (paramType.equals("name")) {
                    names.put(new Integer(lid), noeudsParam.item(i).getChildNodes().item(0).getNodeValue());
                } else {
                    descriptions.put(new Integer(lid), noeudsParam.item(i).getChildNodes().item(0).getNodeValue());
                }
            }
        }
        String roomType = fullStats.equals("1")?"profsType":"General";
        Date dateBeginDate = new Date();
        Date dateEndDate = new Date();
        try {
            dateBeginDate = GestionnaireBD.mejFormatDate.parse(beginDate);
            dateEndDate = GestionnaireBD.mejFormatDate.parse(endDate);
        } catch (Exception e) {
        }

        TreeSet<Integer> setKeywordIds = new TreeSet<Integer>();
        for (String id: keywordIds.split(",")) {
            setKeywordIds.add(Integer.parseInt(id));
        }
        TreeSet<Integer> setGameTypeIds = new TreeSet<Integer>();
        for (String id: gameTypeIds.split(",")) {
            setGameTypeIds.add(Integer.parseInt(id));
        }

        //update room in the DB
        objControleurJeu.obtenirGestionnaireBD().updateRoom(
                Integer.parseInt(roomId),
                password,
                names, descriptions,
                beginDate, endDate,
                Integer.parseInt(masterTime),
                keywordIds, gameTypeIds);

        //This replaces the old room with the new one.
        objControleurJeu.ajouterNouvelleSalle(new Salle(
                objControleurJeu,
                Integer.parseInt(roomId), password, objJoueurHumain.obtenirNomUtilisateur(), roomType,
                dateBeginDate, dateEndDate, Integer.parseInt(masterTime),
                names, descriptions, setKeywordIds, setGameTypeIds));



        genererNumeroReponse();
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "OK");

    }

    private void traiterCommandeDeleteRoom(Element noeudEntree, Element noeudCommande) {

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }

        // Il n'y a pas eu d'erreurs on va enlever la salle de la BD et du contrôleur de jeu
        int roomId = Integer.parseInt(obtenirValeurParametre(noeudEntree, "RoomId").getNodeValue());
        objControleurJeu.obtenirGestionnaireBD().deleteRoom(roomId);
        objControleurJeu.closeRoom(roomId);

        genererNumeroReponse();
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "OK");

    }

    private void traiterCommandeReportBugQuestion(Element noeudEntree, Element noeudCommande) {

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansTable(noeudCommande)) {
            return;
        }
        if (erreurPasDeQuestion(noeudCommande, "PasDeQuestion")) {
            return;
        }

        // Il n'y a pas eu d'erreurs on va ajouter le rapport bug à  la BD
        String errorDescription = obtenirValeurParametre(noeudEntree, "Description").getNodeValue();
        int langue_id = this.langue.equals("fr") ? 1 : 2;
        //add the info to the DB
        objControleurJeu.obtenirGestionnaireBD().reportBugQuestion(
                objJoueurHumain.obtenirCleJoueur(),
                objJoueurHumain.obtenirPartieCourante().obtenirQuestionCourante().obtenirCodeQuestion(),
                langue_id,
                errorDescription);

        genererNumeroReponse();
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "OK");
    }

    private void traiterCommandeReportRoom(Element noeudEntree, Element noeudCommande) {

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }

        // Il n'y a pas eu d'erreurs on construit le rapport et le renvoi au joueur

        int roomId = Integer.parseInt(obtenirValeurParametre(noeudEntree, "RoomId").getNodeValue());

        //Crée le rapport à  partir de l'information contenu dans la BD
        RapportDeSalle rapport = objControleurJeu.obtenirGestionnaireBD().createRoomReport(roomId);

        genererNumeroReponse();
        Document docSortie = noeudCommande.getOwnerDocument();
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "OK");
        noeudCommande.appendChild(rapport.createXML(docSortie));
    }

    private void traiterCommandeEntrerSalle(Element noeudEntree, Element noeudCommande) {

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurDansSalle(noeudCommande)) {
            return;
        }

        // Déclaration d'une variable qui va contenir le noeud
        // du nom de la salle dans laquelle le client veut entrer
        int roomId = Integer.parseInt(obtenirValeurParametre(noeudEntree, "RoomID").getNodeValue());
        String motDePasse = obtenirValeurParametre(noeudEntree, "MotDePasse").getNodeValue();
        
        if (erreurSalleNonExistante(noeudCommande, roomId)) {
            return;
        }

        //On tente d'ajouter le joueur dans la salle désirée en utilisant le mot de passe
        //(true signifie générer un numéro de commande pour l'événement qui est envoyé)
        //La méthode retourne true ou false dépendemment du succès de l'opération.
        if (!objControleurJeu.entrerSalle(objJoueurHumain, roomId, motDePasse, true)) {
            noeudCommande.setAttribute("nom", "MauvaisMotDePasseSalle");
            return;
        }

        //** Le joueur est maintenant dans la salle **/

        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "Ok");
    }

    private void traiterCommandeQuitterSalle(Element noeudCommande) {

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansSalle(noeudCommande)) {
            return;
        }

        // Appeler la méthode pour quitter la salle
        objJoueurHumain.obtenirSalleCourante().quitterSalle(objJoueurHumain, true, true);
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "Ok");
    }

    private void traiterCommandeObtenirListeJoueursSalle(Element noeudCommande) {

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansSalle(noeudCommande)) {
            return;
        }

        // Il n'y a pas eu d'erreurs et il va falloir retourner
        // une liste de joueurs
        genererNumeroReponse();
        Document docSortie = noeudCommande.getOwnerDocument();
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "ListeJoueursSalle");


        // Créer le noeud pour le paramètre contenant la liste
        // des joueurs à  retourner
        Element objNoeudParametreListeJoueurs = docSortie.createElement("parametre");
        objNoeudParametreListeJoueurs.setAttribute("type", "ListeNomUtilisateurs");
        HashMap<String, JoueurHumain> lstListeJoueurs = objJoueurHumain.obtenirSalleCourante().obtenirListeJoueurs();
        synchronized (lstListeJoueurs) {
            for (JoueurHumain objJoueur: lstListeJoueurs.values()) {
                // Créer le noeud du joueur courant
                Element objNoeudJoueur = docSortie.createElement("joueur");
                objNoeudJoueur.setAttribute("nom", objJoueur.obtenirNomUtilisateur());
                objNoeudParametreListeJoueurs.appendChild(objNoeudJoueur);
            }
        }
        noeudCommande.appendChild(objNoeudParametreListeJoueurs);
    }

    private void traiterCommandeObtenirListeTables(Element noeudEntree, Element noeudCommande) {
        // Cette partie de code est synchronisée de telle manière
        // que le client peut recevoir des événements d'entrée/sortie
        // de table avant le no de retour, un peu après, ou
        // complétement après, dans tous les cas, le client doit
        // s'occuper d'arranger tout ça et de ne rien faire si des
        // événements arrivent après le no de retour et que ça ne
        // change rien à  la liste, car c'est normal

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansSalle(noeudCommande)) {
            return;
        }

        // Obtenir la valeur du paramètre Filtre
        String strFiltre = obtenirValeurParametre(noeudEntree, "Filtre").getNodeValue();

        // Le filtre n'a pas une valeur valide
        if (Filtre.estUnMembre(strFiltre) == false) {
            noeudCommande.setAttribute("nom", "FiltreNonConnu");
            return;
        }

        // Il n'y a pas eu d'erreurs et il va falloir retourner
        // une liste de tables
        genererNumeroReponse();
        Document docSortie = noeudCommande.getOwnerDocument();
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "ListeTables");


        // Créer le noeud pour le paramètre contenant la liste
        // des tables à  retourner
        Element objNoeudParametreListeTables = docSortie.createElement("parametre");
        objNoeudParametreListeTables.setAttribute("type", "ListeTables");
        HashMap<Integer, Table> lstListeTables = objJoueurHumain.obtenirSalleCourante().obtenirListeTables();
        synchronized (lstListeTables) {
            // Passer toutes les tables et créer un noeud pour
            // chaque table et l'ajouter au noeud de paramètre
            for (Table objTable: lstListeTables.values()) {
                HashMap<String, JoueurHumain> lstListeJoueurs = objTable.obtenirListeJoueurs();
                synchronized (lstListeJoueurs) { //It's very dangerous to grab two ressources at once: deadlock.
                    if (strFiltre.equals(Filtre.IncompletesNonCommencees) && (objTable.estComplete() || objTable.estCommencee())) {
                        continue;
                    }
                    if (strFiltre.equals(Filtre.IncompletesCommencees) && (objTable.estComplete() || !objTable.estCommencee())) {
                        continue;
                    }
                    if (strFiltre.equals(Filtre.CompletesNonCommencees) && (!objTable.estComplete() || objTable.estCommencee())) {
                        continue;
                    }
                    if (strFiltre.equals(Filtre.CompletesCommencees) && (!objTable.estComplete() && !objTable.estCommencee())) {
                        continue;
                    }
                    // Créer le noeud de la table courante
                    Element objNoeudTable = docSortie.createElement("table");
                    objNoeudTable.setAttribute("no", Integer.toString(objTable.obtenirNoTable()));
                    objNoeudTable.setAttribute("temps", Integer.toString(objTable.obtenirTempsTotal()));
                    objNoeudTable.setAttribute("tablName", objTable.getTableName());
                    objNoeudTable.setAttribute("maxNbPlayers", Integer.toString(objTable.getMaxNbPlayers()));
                    objNoeudTable.setAttribute("gameType", objTable.getGameType());
                    for (JoueurHumain objJoueur: lstListeJoueurs.values()) {
                        // Créer le noeud du joueur courant
                        Element objNoeudJoueur = docSortie.createElement("joueur");
                        objNoeudJoueur.setAttribute("nom", objJoueur.obtenirNomUtilisateur());
                        objNoeudJoueur.setAttribute("idPersonnage", Integer.toString(objJoueur.obtenirPartieCourante().obtenirIdPersonnage()));
                        objNoeudJoueur.setAttribute("cloColor", objJoueur.obtenirPartieCourante().getClothesColor());
                        objNoeudTable.appendChild(objNoeudJoueur);
                    }
                    objNoeudParametreListeTables.appendChild(objNoeudTable);
                }
            }
            noeudCommande.appendChild(objNoeudParametreListeTables);
        }
    }

    private void traiterCommandeCreerTable(Element noeudEntree, Element noeudCommande) {
        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansSalle(noeudCommande)) {
            return;
        }
        if (erreurJoueurDansTable(noeudCommande)) {
            return;
        }

        // Il n'y a pas eu d'erreurs
        Document docSortie = noeudCommande.getOwnerDocument();
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "NoTable");

        int intTempsPartie = Integer.parseInt(obtenirValeurParametre(noeudEntree, "TempsPartie").getNodeValue());
        int intNbLines = Integer.parseInt(obtenirValeurParametre(noeudEntree, "NbLines").getNodeValue());
        int intNbColumns = Integer.parseInt(obtenirValeurParametre(noeudEntree, "NbColumns").getNodeValue());

        String name = obtenirValeurParametre(noeudEntree, "TableName").getNodeValue();
        String type = obtenirValeurParametre(noeudEntree, "GameType").getNodeValue();
        //System.out.println("Protocole - create table : " + intNbColumns + " " + intNbLines + " " + type);

        // Appeler la méthode permettant de créer la nouvelle
        // table et d'entrer le joueur dans cette table
        int intNoTable = objJoueurHumain.obtenirSalleCourante().creerTable(
                objJoueurHumain, intTempsPartie, true, name, intNbLines, intNbColumns, type);

        name = objJoueurHumain.obtenirPartieCourante().obtenirTable().getTableName();
        // Ajouter le noeud paramètre du numéro de la table
        Element objNoeudParametreNoTable = docSortie.createElement("parametre");
        objNoeudParametreNoTable.setAttribute("type", "NoTable");
        objNoeudParametreNoTable.appendChild(docSortie.createTextNode(Integer.toString(intNoTable)));
        noeudCommande.appendChild(objNoeudParametreNoTable);

        // Ajouter le noeud paramètre du nom de la table
        Element objNoeudParametreNameTable = docSortie.createElement("parametre");
        objNoeudParametreNameTable.setAttribute("type", "NameTable");
        objNoeudParametreNameTable.appendChild(docSortie.createTextNode(name));
        noeudCommande.appendChild(objNoeudParametreNameTable);

        // Ajouter le noeud pour le paramètre contenant la couleur du joueur
        Element objNoeudParametreColorPersonnage = docSortie.createElement("parametre");
        objNoeudParametreColorPersonnage.setAttribute("type", "Color");
        objNoeudParametreColorPersonnage.appendChild(docSortie.createTextNode(objJoueurHumain.obtenirPartieCourante().getClothesColor()));
        noeudCommande.appendChild(objNoeudParametreColorPersonnage);

        // Ajouter le noeud pour le paramètre contenant la nombre maximum de joueur
        Element objNoeudParametreMaxNbPlayers = docSortie.createElement("parametre");
        objNoeudParametreMaxNbPlayers.setAttribute("type", "MaxNbPlayers");
        objNoeudParametreMaxNbPlayers.appendChild(docSortie.createTextNode("" + objJoueurHumain.obtenirPartieCourante().obtenirTable().getMaxNbPlayers()));
        noeudCommande.appendChild(objNoeudParametreMaxNbPlayers);
    }

    private void traiterCommandeEntrerTable(Element noeudEntree, Element noeudCommande) {

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansSalle(noeudCommande)) {
            return;
        }
        if (erreurJoueurDansTable(noeudCommande)) {
            return;
        }

        Document docSortie = noeudCommande.getOwnerDocument();


        // Obtenir le numéro de la table dans laquelle le joueur
        // veut entrer et le garder en mémoire dans une variable
        int intNoTable = Integer.parseInt(obtenirValeurParametre(noeudEntree, "NoTable").getNodeValue());
        // Déclaration d'une nouvelle liste de personnages
        JoueurHumain[] listInit = objJoueurHumain.obtenirSalleCourante().obtenirTable(intNoTable).remplirListePersonnageJoueurs();

        // Appeler la méthode permettant d'entrer dans la table
        ResultatEntreeTable resultatEntreeTable =
                            objJoueurHumain.obtenirSalleCourante().entrerTable(objJoueurHumain, intNoTable, true);

        switch (resultatEntreeTable) {
            case Succes:
                break;
            default:
                noeudCommande.setAttribute("nom", resultatEntreeTable.toString());
                return;
        }

        //** Le joueur est maintenant à  la table, car resultatEntreeTable == Succes **/

        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "ListePersonnageJoueurs");

        // Créer le noeud pour le paramètre contenant la liste
        // des personnages à  retourner
        Element objNoeudParametreListePersonnageJoueurs = docSortie.createElement("parametre");
        objNoeudParametreListePersonnageJoueurs.setAttribute("type", "ListePersonnageJoueurs");
        for (int i = 0; i < listInit.length; i++) {
            Element objNoeudPersonnage = docSortie.createElement("personnage");
            objNoeudPersonnage.setAttribute("nom", listInit[i].obtenirNomUtilisateur());
            objNoeudPersonnage.setAttribute("idPersonnage", ((Integer)listInit[i].obtenirPartieCourante().obtenirIdPersonnage()).toString());
            objNoeudPersonnage.setAttribute("role", ((Integer)listInit[i].getRole()).toString());
            objNoeudPersonnage.setAttribute("clothesColor", listInit[i].obtenirPartieCourante().getClothesColor());
            objNoeudParametreListePersonnageJoueurs.appendChild(objNoeudPersonnage);
        }
        noeudCommande.appendChild(objNoeudParametreListePersonnageJoueurs);

        // Créer le noeud pour le paramètre contenant la la couleur du joueur
        Element objNoeudParametreColorPersonnage = docSortie.createElement("parametre");
        objNoeudParametreColorPersonnage.setAttribute("type", "Color");
        objNoeudParametreColorPersonnage.appendChild(docSortie.createTextNode(objJoueurHumain.obtenirPartieCourante().getClothesColor()));
        noeudCommande.appendChild(objNoeudParametreColorPersonnage);
    }

    private void traiterCommandeQuitterTable(Element noeudCommande) {

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansSalle(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansTable(noeudCommande)) {
            return;
        }

        // Appeler la méthode pour quitter la table
        objJoueurHumain.obtenirPartieCourante().obtenirTable().quitterTable(objJoueurHumain, true, true);

        //** Le joueur à  maintenant quitter la table **/
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "Ok");
    }

    private void traiterCommandeDemarrerMaintenant(Element noeudEntree, Element noeudCommande) {

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansSalle(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansTable(noeudCommande)) {
            return;
        }

        // On n'a pas besoin de valider qu'il n'y aucune partie de
        // commencée, car le joueur doit obligatoirement être dans
        // la table pour démarrer la partie et comme il ne peut entrer
        // si une partie est en cours, alors c'est certain qu'il n'y
        // aura pas de parties en cours

        // Obtenir le paramètre pour le joueur virtuel
        // choix possible: "Aucun", "Facile", "Intermediaire", "Difficile"
        String strParamJoueurVirtuel = obtenirValeurParametre(noeudEntree, "NiveauJoueurVirtuel").getNodeValue();

        // Appeler la méthode permettant de démarrer une partie
        ResultatDemarrerPartie resultatDemarrerPartie =
                               objJoueurHumain.obtenirPartieCourante().obtenirTable().demarrerMaintenant(
                objJoueurHumain, true, strParamJoueurVirtuel);

        objLogger.info(GestionnaireMessages.message("protocole.resultat") + resultatDemarrerPartie);

        switch (resultatDemarrerPartie) {
            case Succes:
                break;
            default:// Il y avait déjà  une partie en cours
                objLogger.error(GestionnaireMessages.message("protocole.erreur_code: ") + resultatDemarrerPartie);
                noeudCommande.setAttribute("nom", resultatDemarrerPartie.toString());
                return;
        }

        // Si le résultat du démarrage de partie est Succes alors le
        // joueur est maintenant en attente
        bolEnTrainDeJouer = true;
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "DemarrerMaintenant");
    }

    private void traiterCommandeDemarrerPartie(Element noeudEntree, Element noeudCommande) {

        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansSalle(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansTable(noeudCommande)) {
            return;
        }

        // On n'a pas besoin de valider qu'il n'y aucune partie de
        // commencée, car le joueur doit obligatoirement être dans
        // la table pour démarrer la partie et comme il ne peut entrer
        // si une partie est en cours, alors c'est certain qu'il n'y
        // aura pas de parties en cours


        // Obtenir le numéro Id du personnage choisi et le garder
        // en mémoire dans une variable
        int intIdDessin = Integer.parseInt(obtenirValeurParametre(noeudEntree, "IdDessin").getNodeValue());

        // Appeler la méthode permettant de démarrer une partie
        // et garder son résultat dans une variable
        ResultatDemarrerPartie resultatDemarrerPartie =
                               objJoueurHumain.obtenirPartieCourante().obtenirTable().demarrerPartie(
                objJoueurHumain, intIdDessin, true);

        switch (resultatDemarrerPartie) {
            case Succes:
                break;
            default:
                noeudCommande.setAttribute("nom", resultatDemarrerPartie.toString());
                return;
        }

        int idPersonnage = objJoueurHumain.obtenirPartieCourante().obtenirIdPersonnage();
        bolEnTrainDeJouer = true;
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "Ok");
        noeudCommande.setAttribute("id", Integer.toString(idPersonnage));
    }

    private void traiterCommandeDeplacerPersonnage(Element noeudEntree, Element noeudCommande) {
        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansSalle(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansTable(noeudCommande)) {
            return;
        }
        if (erreurPartiePasDemarree(noeudCommande)) {
            return;
        }
        if (erreurQuestionPresente(noeudCommande, "QuestionPasRepondue")) {
            return;
        }

        // Obtenir la position x, y où le joueur souhaite se déplacer
        Node objNoeudNouvellePosition = obtenirValeurParametre(noeudEntree, "NouvellePosition");
        Point objNouvellePosition =
              new Point(
                Integer.parseInt(objNoeudNouvellePosition.getAttributes().getNamedItem("x").getNodeValue()),
                Integer.parseInt(objNoeudNouvellePosition.getAttributes().getNamedItem("y").getNodeValue()));

        // Si le déplacement n'est pas permis, alors il y a une erreur
        if (objJoueurHumain.obtenirPartieCourante().deplacementEstPermis(objNouvellePosition) == false) {
            noeudCommande.setAttribute("nom", "DeplacementNonAutorise");
            return;
        }


        // Trouver la question à poser selon la difficulté et
        // le type de case sur laquelle on veut se diriger
        Question objQuestionAPoser = objJoueurHumain.obtenirPartieCourante().trouverQuestionAPoser(objNouvellePosition, true);

        // Il n'y a pas eu d'erreurs
        Document docSortie = noeudCommande.getOwnerDocument();
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "Question");

        // Créer le noeud paramètre de la question
        Element objNoeudParametreQuestion = docSortie.createElement("parametre");
        objNoeudParametreQuestion.setAttribute("type", "Question");
        // Si aucune question n'a été trouvée, alors c'est que
        // le joueur ne s'est pas déplacé, on ne renvoit donc
        // que le paramètre sans la question, sinon on renvoit
        // également l'information sur la question
        if (objQuestionAPoser != null) {
            // Créer un noeud texte contenant l'information sur la question
            Element objNoeudQuestion = docSortie.createElement("question");
            objNoeudQuestion.setAttribute("id", Integer.toString(objQuestionAPoser.obtenirCodeQuestion()));
            objNoeudQuestion.setAttribute("type", TypeQuestion.getValue(objQuestionAPoser.obtenirTypeQuestion()));
            objNoeudQuestion.setAttribute("url", objQuestionAPoser.obtenirURLQuestion());
            objNoeudParametreQuestion.appendChild(objNoeudQuestion);
        }
        noeudCommande.appendChild(objNoeudParametreQuestion);

        lastQuestionTime = objJoueurHumain.obtenirPartieCourante().obtenirTable().obtenirTempsRestant();
        //System.out.println("Protocol for : " + lastQuestionTime);
    }

    private void traiterCommandeRepondreQuestion(Element noeudEntree, Element noeudCommande) {
        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansSalle(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansTable(noeudCommande)) {
            return;
        }
        if (erreurPartiePasDemarree(noeudCommande)) {
            return;
        }
        if (erreurPasDeQuestion(noeudCommande, "DeplacementNonDemande")) {
            return;
        }
        //Il n'y a pas eu d'erreurs

        //Premièrement on calcul les statistiques        
        InformationPartie infoPartie = objJoueurHumain.obtenirPartieCourante();
        int timer = lastQuestionTime - infoPartie.obtenirTable().obtenirTempsRestant();
        String strReponse = obtenirValeurParametre(noeudEntree, "Reponse").getNodeValue();
        RetourVerifierReponseEtMettreAJourPlateauJeu objRetour = infoPartie.verifierReponseEtMettreAJourPlateauJeu(strReponse, timer);

        //Ensuite on s'occupe du noeud de retour
        genererNumeroReponse();
        Document docSortie = noeudCommande.getOwnerDocument();
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "Deplacement");

        //Le paramètre DeplacementAccepte est true/false dépendemment de si le déplacement
        //désiré par le joueur à été accepté.
        Element objNoeudParametreDeplacementAccepte = docSortie.createElement("parametre");
        objNoeudParametreDeplacementAccepte.setAttribute("type", "DeplacementAccepte");
        objNoeudParametreDeplacementAccepte.appendChild(docSortie.createTextNode(Boolean.toString(objRetour.deplacementEstAccepte())));
        noeudCommande.appendChild(objNoeudParametreDeplacementAccepte);

        //Le paramètre Pointage contient le nouveau pointage pour le joueur.
        Element objNoeudParametrePointage = docSortie.createElement("parametre");
        objNoeudParametrePointage.setAttribute("type", "Pointage");
        objNoeudParametrePointage.appendChild(docSortie.createTextNode(Integer.toString(objRetour.obtenirNouveauPointage())));
        noeudCommande.appendChild(objNoeudParametrePointage);

        //Le paramètre Argent contient la nouvelle somme d'argent disponible au joueur.
        Element objNoeudParametreArgent = docSortie.createElement("parametre");
        objNoeudParametreArgent.setAttribute("type", "Argent");
        objNoeudParametreArgent.appendChild(docSortie.createTextNode(Integer.toString(objRetour.obtenirNouvelArgent())));
        noeudCommande.appendChild(objNoeudParametreArgent);

        //Le paramètre MoveVisibility contient la distance de déplacement maximum (en nombre de case) pour le joueur.
        Element objNoeudParametreVisibility = docSortie.createElement("parametre");
        objNoeudParametreVisibility.setAttribute("type", "MoveVisibility");
        objNoeudParametreVisibility.appendChild(docSortie.createTextNode(Integer.toString(objJoueurHumain.obtenirPartieCourante().getMoveVisibility())));
        noeudCommande.appendChild(objNoeudParametreVisibility);

        //Le paramètre Bonus contient le nombre de point boni pour un joueur arrivant à la ligne d'arrivée dans
        //une partie de type 'Tournoi'.  C'est une fonction du temps restant.
        Element objNoeudParametreBonus = docSortie.createElement("parametre");
        objNoeudParametreBonus.setAttribute("type", "Bonus");
        objNoeudParametreBonus.appendChild(docSortie.createTextNode(Integer.toString(objJoueurHumain.obtenirPartieCourante().getTournamentBonus())));
        noeudCommande.appendChild(objNoeudParametreBonus);

        // Si le déplacement est accepté, alors on crée les
        // noeuds spécifiques au succès de la réponse
        if (objRetour.deplacementEstAccepte()) {
            // S'il y a un objet qui a été ramassé, alors on peut
            // créer son noeud enfant, sinon on n'en crée pas
            if (objRetour.obtenirObjetRamasse() != null) {
                Element objNoeudParametreObjetRamasse = docSortie.createElement("parametre");
                objNoeudParametreObjetRamasse.setAttribute("type", "ObjetRamasse");
                Element objNoeudObjetRamasse = docSortie.createElement("objetRamasse");
                objNoeudObjetRamasse.setAttribute("id", Integer.toString(objRetour.obtenirObjetRamasse().obtenirId()));
                objNoeudObjetRamasse.setAttribute("type", objRetour.obtenirObjetRamasse().getClass().getSimpleName());
                objNoeudParametreObjetRamasse.appendChild(objNoeudObjetRamasse);
                noeudCommande.appendChild(objNoeudParametreObjetRamasse);
                objJoueurHumain.obtenirPartieCourante().ajouterObjetUtilisableListe(objRetour.obtenirObjetRamasse());
            }

            // Si le joueur a subi un objet, alors on peut créer
            // son noeud enfant, sinon on n'en crée pas
            if (objRetour.obtenirObjetSubi() != null) {
                Element objNoeudParametreObjetSubi = docSortie.createElement("parametre");
                objNoeudParametreObjetSubi.setAttribute("type", "ObjetSubi");
                Element objNoeudObjetSubi = docSortie.createElement("objetSubi");
                objNoeudObjetSubi.setAttribute("id", Integer.toString(objRetour.obtenirObjetSubi().obtenirId()));
                objNoeudObjetSubi.setAttribute("type", objRetour.obtenirObjetSubi().getClass().getSimpleName());
                objNoeudParametreObjetSubi.appendChild(objNoeudObjetSubi);
                noeudCommande.appendChild(objNoeudParametreObjetSubi);
            }

            // Si le joueur est arrivé sur un magasin, alors on lui
            // renvoie la liste des objets que le magasin vend
            if (objRetour.obtenirCollision().equals("magasin")) {
                Magasin objMagasin = objRetour.obtenirMagasin();
                creerListeObjetsMagasin(objMagasin, docSortie, noeudCommande);
            }

            // On ajoute la nouvelle position (x,y) au noeud de commande.
            Element objNoeudParametreNouvellePosition = docSortie.createElement("parametre");
            objNoeudParametreNouvellePosition.setAttribute("type", "NouvellePosition");
            Element objNoeudNouvellePosition = docSortie.createElement("position");
            objNoeudNouvellePosition.setAttribute("x", Integer.toString(objRetour.obtenirNouvellePosition().x));
            objNoeudNouvellePosition.setAttribute("y", Integer.toString(objRetour.obtenirNouvellePosition().y));
            objNoeudParametreNouvellePosition.appendChild(objNoeudNouvellePosition);
            noeudCommande.appendChild(objNoeudParametreNouvellePosition);

            // On ajoute le type de collision causé par le mouvement au noeud de commande.
            // Le joueur entre en collision avec ce qui se trouve sur la nouvelle case,
            // si la case est vide collision="", sinon collision est une string décrivant
            // le type de collision.
            Element objNoeudParametreCollision = docSortie.createElement("parametre");
            objNoeudParametreCollision.setAttribute("type", "Collision");
            objNoeudParametreCollision.appendChild(docSortie.createTextNode(objRetour.obtenirCollision()));
            noeudCommande.appendChild(objNoeudParametreCollision);

        } else {
            // Le déplacement à été refusé et on explique pourquoi.
            Element objNoeudParametreExplication = docSortie.createElement("parametre");
            objNoeudParametreExplication.setAttribute("type", "Explication");
            objNoeudParametreExplication.appendChild(docSortie.createTextNode(objRetour.obtenirExplications()));
            noeudCommande.appendChild(objNoeudParametreExplication);
        }
    }

    private void traiterCommandeCancelQuestion(Element noeudCommande) {
        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansSalle(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansTable(noeudCommande)) {
            return;
        }
        if (erreurPartiePasDemarree(noeudCommande)) {
            return;
        }
        if (erreurPasDeQuestion(noeudCommande, "PasDeQuestion")) {
            return;
        }
        //Il n'y a pas eu d'erreurs

        objJoueurHumain.obtenirPartieCourante().cancelPosedQuestion();
        genererNumeroReponse();
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "OK");
    }

    private void traiterCommandePointage(Element noeudEntree, Element noeudCommande) {
        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansSalle(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansTable(noeudCommande)) {
            return;
        }
        if (erreurPartiePasDemarree(noeudCommande)) {
            return;
        }

        // Obtenir pointage
        int pointage = Integer.parseInt(obtenirValeurParametre(noeudEntree, "Pointage").getNodeValue());
        int nouveauPointage = objJoueurHumain.obtenirPartieCourante().obtenirPointage();
        nouveauPointage += pointage;
        objJoueurHumain.obtenirPartieCourante().definirPointage(nouveauPointage);
        objJoueurHumain.obtenirPartieCourante().setPointsFinalTime(objJoueurHumain.obtenirPartieCourante().obtenirTable().obtenirTempsRestant());

        //	Il n'y a pas eu d'erreurs
        Document docSortie = noeudCommande.getOwnerDocument();
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "Pointage");

        Element objNoeudParametrePointage = docSortie.createElement("parametre");
        objNoeudParametrePointage.setAttribute("type", "Pointage");
        objNoeudParametrePointage.appendChild(docSortie.createTextNode(Integer.toString(nouveauPointage)));
        noeudCommande.appendChild(objNoeudParametrePointage);

        // Préparer un événement pour les autres joueurs de la table
        // pour qu'il se tienne à jour du pointage de ce joueur
        objJoueurHumain.obtenirPartieCourante().obtenirTable().preparerEvenementMAJPointage(
                objJoueurHumain.obtenirNomUtilisateur(),
                objJoueurHumain.obtenirPartieCourante().obtenirPointage());
    }

    private void traiterCommandeArgent(Element noeudEntree, Element noeudCommande) {
        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansSalle(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansTable(noeudCommande)) {
            return;
        }
        if (erreurPartiePasDemarree(noeudCommande)) {
            return;
        }

        int argent = Integer.parseInt(obtenirValeurParametre(noeudEntree, "Argent").getNodeValue());
        int nouvelArgent = objJoueurHumain.obtenirPartieCourante().obtenirArgent();
        nouvelArgent += argent;
        objJoueurHumain.obtenirPartieCourante().definirArgent(nouvelArgent);

        //	Il n'y a pas eu d'erreurs
        Document docSortie = noeudCommande.getOwnerDocument();
        noeudCommande.setAttribute("type", "Reponse");
        noeudCommande.setAttribute("nom", "Argent");

        Element objNoeudParametreArgent = docSortie.createElement("parametre");
        objNoeudParametreArgent.setAttribute("type", "Argent");
        objNoeudParametreArgent.appendChild(docSortie.createTextNode(Integer.toString(nouvelArgent)));
        noeudCommande.appendChild(objNoeudParametreArgent);

        // Préparer un événement pour les autres joueurs de la table
        // pour qu'il se tienne à jour de l'argent de ce joueur
        objJoueurHumain.obtenirPartieCourante().obtenirTable().preparerEvenementMAJArgent(
                objJoueurHumain.obtenirNomUtilisateur(),
                objJoueurHumain.obtenirPartieCourante().obtenirArgent());
    }

    private void traiterCommandeUtiliserObjet(Element noeudEntree, Element noeudCommande) {
        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansSalle(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansTable(noeudCommande)) {
            return;
        }
        if (erreurPartiePasDemarree(noeudCommande)) {
            return;
        }

        InformationPartie infoPartie = objJoueurHumain.obtenirPartieCourante();
        int timer = lastQuestionTime - infoPartie.obtenirTable().obtenirTempsRestant();

        // Obtenir l'id de l'objet a utilisé
        int intIdObjet = Integer.parseInt(obtenirValeurParametre(noeudEntree, "id").getNodeValue());
        String playerName = obtenirValeurParametre(noeudEntree, "player").getNodeValue();

        ObjetUtilisable objObjetUtilise = infoPartie.obtenirObjetUtilisable(intIdObjet);
        String strTypeObjet = objObjetUtilise.obtenirTypeObjet();

        Document docSortie = noeudCommande.getOwnerDocument();
        noeudCommande.setAttribute("nom", "RetourUtiliserObjet");
        objJoueurHumain.enleverObjet(intIdObjet, strTypeObjet);

        // Dépendamment du type de l'objet, on effectue le traitement approprié
        if (strTypeObjet.equals("Livre")) {
            // Le livre est utilisé lorsqu'un joueur se fait poser une question
            // à choix de réponse. Le serveur renvoie alors une mauvaise réponse
            // à la question, et le client fera disparaître ce choix de réponse
            // parmi les choix possibles pour le joueur.
            // On obtient une mauvaise réponse à la dernière question posée
            String mauvaiseReponse = infoPartie.getObjBoiteQuestions().obtenirMauvaiseReponse(infoPartie.obtenirQuestionCourante());

            // Créer le noeud contenant le choix de réponse si c'était une question à choix de réponse
            Element objNoeudParametreMauvaiseReponse = docSortie.createElement("parametre");
            objNoeudParametreMauvaiseReponse.setAttribute("type", "MauvaiseReponse");
            objNoeudParametreMauvaiseReponse.appendChild(docSortie.createTextNode(mauvaiseReponse));
            noeudCommande.setAttribute("type", "Livre");
            noeudCommande.appendChild(objNoeudParametreMauvaiseReponse);

        } else if (strTypeObjet.equals("Boule")) {
            // La boule permettra à un joueur de changer de question si celle
            // qu'il s'est fait envoyer ne lui tente pas
            Question nouvelleQuestion = infoPartie.trouverQuestionAPoserCristall(timer, true);

            // On prépare l'envoi des informations sur la nouvelle question
            Element objNoeudParametreNouvelleQuestion = docSortie.createElement("parametre");
            objNoeudParametreNouvelleQuestion.setAttribute("type", "nouvelleQuestion");
            Element objNoeudParametreQuestion = docSortie.createElement("question");
            objNoeudParametreQuestion.setAttribute("id", Integer.toString(nouvelleQuestion.obtenirCodeQuestion()));
            objNoeudParametreQuestion.setAttribute("type", TypeQuestion.getValue(nouvelleQuestion.obtenirTypeQuestion()));
            objNoeudParametreQuestion.setAttribute("url", nouvelleQuestion.obtenirURLQuestion());
            objNoeudParametreNouvelleQuestion.appendChild(objNoeudParametreQuestion);
            noeudCommande.setAttribute("type", "Boule");
            noeudCommande.appendChild(objNoeudParametreNouvelleQuestion);

            lastQuestionTime = infoPartie.obtenirTable().obtenirTempsRestant();
            //System.out.println("Protocol for Cristall : " + lastQuestionTime);
        } else if (strTypeObjet.equals("PotionGros")) {
            // La PotionGros fait grossir le joueur
            noeudCommande.setAttribute("type", "OK");
            infoPartie.obtenirTable().preparerEvenementUtiliserObjet(
                    objJoueurHumain.obtenirNomUtilisateur(),
                    objJoueurHumain.obtenirNomUtilisateur(),
                    "PotionGros",
                    "");

        } else if (strTypeObjet.equals("PotionPetit")) {
            // La PotionPetit fait rapetisser le joueur
            noeudCommande.setAttribute("type", "OK");
            infoPartie.obtenirTable().preparerEvenementUtiliserObjet(
                    objJoueurHumain.obtenirNomUtilisateur(),
                    objJoueurHumain.obtenirNomUtilisateur(),
                    "PotionPetit",
                    "");

        } else if (strTypeObjet.equals("Banane")) {
            // La partie ici ne fait que sélectionner le joueur qui sera affecté
            // Le reste se fait dans Banane.java
            noeudCommande.setAttribute("type", "OK");
            boolean estHumain = false; //Le joueur choisi est'il humain?

            // On obtient la liste des joueurs humains, puis la liste des joueurs virtuels
            HashMap<String, JoueurHumain> listeJoueursHumains = infoPartie.obtenirTable().obtenirListeJoueurs();
            for (JoueurHumain objJoueur: listeJoueursHumains.values()) {
                if (objJoueur.obtenirNomUtilisateur().equals(playerName)) {
                    estHumain = true;
                    break;
                }
            }

            infoPartie.obtenirTable().preparerEvenementUtiliserObjet(
                    objJoueurHumain.obtenirNomUtilisateur(),
                    playerName,
                    "Banane",
                    "");

            //System.out.println("Protocole joueur 4189 Banane " + objJoueurHumain.obtenirNomUtilisateur() + " " + playerName);
            if (estHumain) {
                JoueurHumain joueur = infoPartie.obtenirTable().obtenirJoueurHumainParSonNom(playerName);
                if (joueur != null) {
                    joueur.obtenirPartieCourante().getBananaState().startBanana();
                }
                
            }else{
            	infoPartie.obtenirTable().obtenirJoueurVirtuelParSonNom(playerName).getBananaState().startBanana();
            }

        } else if (strTypeObjet.equals("Brainiac")) {
            //all is did on the time when the player go on the case
        }
    }// end method

    private void traiterCommandeAcheterObjet(Element noeudEntree, Element noeudCommande) {
        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansSalle(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansTable(noeudCommande)) {
            return;
        }
        if (erreurPartiePasDemarree(noeudCommande)) {
            return;
        }

        // Obtenir l'id de l'objet a acheté
        int intIdObjet = Integer.parseInt(obtenirValeurParametre(noeudEntree, "id").getNodeValue());

        // Aller chercher l'objet sur la case où le joueur se trouve présentement (peut retourner null)
        Objet objObjet = objJoueurHumain.obtenirPartieCourante().obtenirObjetCaseCourante();
        Table objTable = objJoueurHumain.obtenirPartieCourante().obtenirTable();
        Document docSortie = noeudCommande.getOwnerDocument();

        // Vérifier si l'objet est un magasin
        if (objObjet instanceof Magasin) {
            // Synchronisme sur l'objet magasin
            synchronized (objObjet) {
                // Vérifier si le magasin vend l'objet avec id = intIdObjet
                if (((Magasin)objObjet).objetExiste(intIdObjet)) {
                    ObjetUtilisable objObjetVoulu = ((Magasin)objObjet).obtenirObjet(intIdObjet);

                    // Vérifier si assez de points pour acheter cet objet
                    if (objJoueurHumain.obtenirPartieCourante().obtenirArgent() < objObjetVoulu.obtenirPrix()) {
                        // Le joueur n'a pas assez de points pour acheter cet objet
                        noeudCommande.setAttribute("nom", "PasAssezDArgent");
                    } else {
                        // Acheter l'objet
                        Integer idProchainObjet = objTable.getAndIncrementNewIdObject();
                        //System.out.println("New - " + idProchainObjet);
                        ObjetUtilisable objObjetAcheter = ((Magasin)objObjet).acheterObjet(intIdObjet, idProchainObjet);

                        // L'ajouter à la liste des objets du joueur et en défrayer les coûts.
                        objJoueurHumain.obtenirPartieCourante().ajouterObjetUtilisableListe(objObjetAcheter);
                        objJoueurHumain.obtenirPartieCourante().definirArgent(objJoueurHumain.obtenirPartieCourante().obtenirArgent() - objObjetAcheter.obtenirPrix());

                        // Préparer un événement pour les autres joueurs de la table
                        // pour qu'il se tienne à jour de l'argent de ce joueur
                        objJoueurHumain.obtenirPartieCourante().obtenirTable().preparerEvenementMAJArgent(
                                objJoueurHumain.obtenirNomUtilisateur(),
                                objJoueurHumain.obtenirPartieCourante().obtenirArgent());

                        // Retourner une réponse positive au joueur
                        noeudCommande.setAttribute("type", "Reponse");
                        noeudCommande.setAttribute("nom", "Ok");
                        // Ajouter l'objet acheté dans la réponse
                        Element objNoeudObjetAchete = docSortie.createElement("objetAchete");
                        objNoeudObjetAchete.setAttribute("type", objObjetAcheter.obtenirTypeObjet());
                        objNoeudObjetAchete.setAttribute("id", Integer.toString(intIdObjet));
                        objNoeudObjetAchete.setAttribute("newId", Integer.toString(idProchainObjet));
                        noeudCommande.appendChild(objNoeudObjetAchete);

                        Element objNoeudParametreArgent = docSortie.createElement("parametre");
                        objNoeudParametreArgent.setAttribute("type", "Argent");
                        objNoeudParametreArgent.appendChild(docSortie.createTextNode(Integer.toString(objJoueurHumain.obtenirPartieCourante().obtenirArgent())));
                        noeudCommande.appendChild(objNoeudParametreArgent);
                    }
                } else {
                    // Ce magasin ne vend pas cet objet (l'objet peut avoir été acheté entre-temps)
                    noeudCommande.setAttribute("nom", "ObjetInexistant");
                }
            } //synchronisme objObjet
        } else {
            // Le joueur n'est pas sur un magasin
            noeudCommande.setAttribute("nom", "PasDeMagasin");
        }
    }

    private void traiterCommandeChatMessage(Element noeudEntree, Element noeudCommande) {
        if (erreurJoueurNonConnecte(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansSalle(noeudCommande)) {
            return;
        }
        if (erreurJoueurPasDansTable(noeudCommande)) {
            return;
        }
        if (erreurChatNonPermis(noeudCommande)) {
            return;
        }

        //  Il n'y a pas eu d'erreurs
        noeudCommande.setAttribute("type", "OK");
        // Obtenir le message à envoyer à tous et le nom du joueur qui l'envoie
        String messageAEnvoyer = obtenirValeurParametre(noeudEntree, "messageAEnvoyer").getNodeValue();
        String nomJoueur = objJoueurHumain.obtenirNomUtilisateur();
        // On prépare l'événement qui enverra le message à tous
        objJoueurHumain.obtenirPartieCourante().obtenirTable().preparerEvenementMessageChat(
                nomJoueur,
                messageAEnvoyer);
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
     * 				traitement de la commande seulement dans le cas où
     * 				on doit passer les éléments d'une liste et qu'il
     * 				peut y avoir des modifications de cette liste par
     * 				un autre joueur. Dans les autres cas, ce sont les
     * 				fonctions appelées qui vont être synchronisées.
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
        // Initialement, on définit les attributs type et nom comme étant Erreur
        // et Commande respectivement pour dire qu'il y a une erreur avec la
        // commande (la commande n'est pas connue) -> Ces attributs seront
        // modifiés par la suite s'il y a d'autres erreurs. Par contre, on ne
        // définit pas tout de suite le numéro de commande à envoyer au client
        objNoeudCommande.setAttribute("type", "Erreur");
        objNoeudCommande.setAttribute("nom", "CommandeNonReconnue");
        //System.out.println("Commande: " + message);
        Commande commande;
        // Si la commande est un ping et qu'il a bel et bien un numéro, alors
        // on peut appeler la méthode du vérificateur de connexions pour lui
        // dire qu'on a reçu un ping, il ne faut rien retourner au client
        if (objDocumentXMLEntree.getChildNodes().getLength() == 1 &&
                objDocumentXMLEntree.getChildNodes().item(0).getNodeName().equals("ping") &&
                objDocumentXMLEntree.getChildNodes().item(0).hasAttributes() == true &&
                objDocumentXMLEntree.getChildNodes().item(0).getAttributes().getNamedItem("numero") != null) {
            // TODO Modifier cette partie pour que la confirmation du ping soit le même
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
                (commande = Commande.get(objNoeudCommandeEntree.getAttribute("nom"))) != null) {
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
                switch (commande) {
                    case Connexion:
                        traiterCommandeConnexion(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case ConnexionProf:
                        traiterCommandeConnexionProf(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case NePasRejoindrePartie:
                        traiterCommandeNePasRejoindrePartie(objNoeudCommande);
                        break;
                    case RejoindrePartie:
                        traiterCommandeRejoindrePartie(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case Deconnexion:
                        traiterCommandeDeconnexion(objNoeudCommande);
                        break;
                    case ObtenirListeJoueurs:
                        traiterCommandObtenirListeJoueurs(objNoeudCommande);
                        break;
                    case ObtenirListeSalles: //ces deux commandes sont identiques
                    case ObtenirListeSallesRetour:
                        traiterCommandeObtenirListeSalles(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case ObtenirListeSallesProf:
                        traiterCommandeObtenirListeSallesProf(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case CreateRoom:
                        traiterCommandeCreateRoom(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case UpdateRoom:
                        traiterCommandeUpdateRoom(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case DeleteRoom:
                        traiterCommandeDeleteRoom(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case ReportBugQuestion:
                        traiterCommandeReportBugQuestion(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case ReportRoom:
                        traiterCommandeReportRoom(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case EntrerSalle:
                        traiterCommandeEntrerSalle(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case QuitterSalle:
                        traiterCommandeQuitterSalle(objNoeudCommande);
                        break;
                    case ObtenirListeJoueursSalle:
                        traiterCommandeObtenirListeJoueursSalle(objNoeudCommande);
                        break;
                    case ObtenirListeTables:
                        traiterCommandeObtenirListeTables(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case CreerTable:
                        traiterCommandeCreerTable(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case EntrerTable:
                        traiterCommandeEntrerTable(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case QuitterTable:
                        traiterCommandeQuitterTable(objNoeudCommande);
                        break;
                    case DemarrerMaintenant:
                        traiterCommandeDemarrerMaintenant(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case DemarrerPartie:
                        traiterCommandeDemarrerPartie(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case DeplacerPersonnage:
                        traiterCommandeDeplacerPersonnage(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case RepondreQuestion:
                        traiterCommandeRepondreQuestion(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case CancelQuestion:
                        traiterCommandeCancelQuestion(objNoeudCommande);
                        break;
                    case Pointage:
                        traiterCommandePointage(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case Argent:
                        traiterCommandeArgent(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case UtiliserObjet:
                        traiterCommandeUtiliserObjet(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case AcheterObjet:
                        traiterCommandeAcheterObjet(objNoeudCommandeEntree, objNoeudCommande);
                        break;
                    case ChatMessage:
                        traiterCommandeChatMessage(objNoeudCommandeEntree, objNoeudCommande);
                        break;
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
            // Retourner le document XML ne contenant pas l'entête XML ajoutée
            // par défaut par le transformateur
            return UtilitaireXML.transformerDocumentXMLEnString(objDocumentXMLSortie);
        } else {
            // Si on ne doit rien retourner, alors on retourne null
            return null;
        }
    }// fin méthode

    /**
     * Cette méthode permet d'envoyer le message passé en paramètre au
     * client (joueur). Deux threads ne peuvent écrire sur le socket en même
     * temps.
     *
     * @param message le message à envoyer au client
     * @throws IOException : Si on ne peut pas obtenir l'accès en
     * 						 écriture sur le canal de communication
     */
    public void envoyerMessage(String message) throws IOException {
        Moniteur.obtenirInstance().debut("ProtocoleJoueur.envoyerMessage");
        // Synchroniser cette partie de code pour empêcher 2 threads d'envoyer
        // un message en même temps sur le canal d'envoi du socket
        synchronized (objSocketJoueur) {
            // Créer le canal qui permet d'envoyer des données sur le canal
            // de communication entre le client et le serveur
            OutputStream objCanalEnvoi = objSocketJoueur.getOutputStream();

            String chainetemp = UtilitaireEncodeurDecodeur.encodeToUTF8(message);

            if (chainetemp.contains("ping") == false) {
                objLogger.info(GestionnaireMessages.message("protocole.message_envoye") + chainetemp);
            }
            // écrire le message sur le canal d'envoi au client
            objCanalEnvoi.write(message.getBytes("UTF8"));

            // écrire le byte 0 sur le canal d'envoi pour signifier la fin du message
            objCanalEnvoi.write((byte)0);

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
     * La plupart des commandes peuvent être validées en utilisant une
     * technique identique.  Pour cette raison on invoque souvent une méthode
     * satellite pour faire la vérfication.
     *
     * @param Element noeudCommande : le noeud de comande à valider
     * @return boolean true   si le noeud de commande et tous ses enfants
     *                        sont valide
     * 				   false  sinon
     */
    private boolean commandeEstValide(Element noeudCommande) {

        Commande cmd = Commande.get(noeudCommande.getAttribute("nom"));

        //Commande inconnue
        if (cmd == null) {
            return false;
        }

        switch (cmd) {
            //Ce groupe contient des commandes qui ne demande aucun paramètre.  On utilise
            //la méthode satellite qui ne fait que tester si le nombre de paramètres est
            //bel et bien zéro.
            case NePasRejoindrePartie:
            case RejoindrePartie:
            case ObtenirListeJoueurs:
            case ObtenirListeSallesProf:
            case ObtenirListeJoueursSalle:
            case CloseRoom:
            case QuitterTable:
            case QuitterSalle:
            case CancelQuestion:
            case Deconnexion: return validerCommande(noeudCommande, new String[0]);

            //Ce groupe contient aussi des commandes pour lesquelles on peut utiliser la
            //méthode satellite.  Le dernier argument de la méthode satellite indique
            //si les paramètres doivent tous être des nombres positifs.  Quand l'argument
            //est absent, on assume 'false'.
            case ConnexionProf:
            case Connexion: return validerCommande(noeudCommande, new String[]{"NomUtilisateur", "Langue", "MotDePasse"});
            case ObtenirListeSallesRetour:
            case ObtenirListeSalles: return validerCommande(noeudCommande, new String[]{"RoomsType"});
            case ReportBugQuestion: return validerCommande(noeudCommande, new String[]{"Description"});
            case ObtenirListeTables: return validerCommande(noeudCommande, new String[]{"Filtre"});
            case RepondreQuestion: return validerCommande(noeudCommande, new String[]{"Reponse"});
            case DeleteRoom:
            case ReportRoom: return validerCommande(noeudCommande, new String[]{"RoomId"}, true);
            case EntrerTable: return validerCommande(noeudCommande, new String[]{"NoTable"}, true);
            case DemarrerPartie: return validerCommande(noeudCommande, new String[]{"IdDessin"}, true);
            case Pointage: return validerCommande(noeudCommande, new String[]{"Pointage"}, true);
            case Argent: return validerCommande(noeudCommande, new String[]{"Argent"}, true);
            case AcheterObjet: return validerCommande(noeudCommande, new String[]{"Id"}, true);
            case ChatMessage: return validerCommande(noeudCommande, new String[]{"ChatMessage"}, true);

            //DemarrerMaintenant est spécial parce que le paramètre requiert une méthode spécialisée
            //pour valider la valeur de son attribut
            case DemarrerMaintenant:
                if (!validerCommande(noeudCommande, new String[]{"NiveauJoueurVirtuel"}))
                    return false;
                return JoueurVirtuel.validerParamNiveau(obtenirValeurParametre(noeudCommande, "NiveauJoueurVirtuel").getNodeValue());

            //UtiliserObjet est spécial parce que un le paramètre 'id' est numérique alors que
            //'player' ne l'est pas.
            case UtiliserObjet:
                if (!validerCommande(noeudCommande, new String[]{"player", "id"}))
                    return false;
                return UtilitaireNombres.isPositiveNumber(obtenirValeurParametre(noeudCommande, "id").getNodeValue());
            //EntrerSalle est spécial parce que un le paramètre 'RoomID' est numérique alors que
            //'MotDePasse' ne l'est pas.
            case EntrerSalle:
                if (!validerCommande(noeudCommande, new String[]{"RoomID", "MotDePasse"})) {
                    return false;
                }
                return UtilitaireNombres.isPositiveNumber(obtenirValeurParametre(noeudCommande, "RoomID").getNodeValue());
            //CreerTable est spécial parce que les paramètres 'TempsPartie', 'NbLines' et 'NbColumns'
            //sont numériques alors que 'TableName' et 'GameType' ne le sont pas
            case CreerTable:
                if (!validerCommande(noeudCommande, new String[]{"TempsPartie", "NbLines", "NbColumns", "TableName", "GameType"}))
                    return false;
                if (!UtilitaireNombres.isPositiveNumber(obtenirValeurParametre(noeudCommande, "TempsPartie").getNodeValue())) return false;
                if (!UtilitaireNombres.isPositiveNumber(obtenirValeurParametre(noeudCommande, "NbLines").getNodeValue())) return false;
                return UtilitaireNombres.isPositiveNumber(obtenirValeurParametre(noeudCommande, "NbColumns").getNodeValue());

            //Ce groupe est spécial parce que leur commande contienne des paramètres à 1 attribut
            //et d'autres à 2 attributs
            case CreateRoom:
            case UpdateRoom:
                return validerCommandeCreateRoomOrUpdateRoom(noeudCommande, cmd);

            //DeplacerPersonnage est spécial parce que son parametre contient un sous-noeud
            case DeplacerPersonnage:
                return validerCommandeDeplacerPersonnage(noeudCommande);

            default: //Commande inconnue
                return false;
        }
    }

    //Cette méthode invoque la méthode du même nom à 3 arguments en ajoutant l'argument
    //'false' en troisième place pour signifier que les paramètres contenus dans le
    //noeudCommande ne sont pas nécessairement des nombres positifs.
    private boolean validerCommande(Element noeudCommande, String[] typesPermis) {
        return validerCommande(noeudCommande, typesPermis, false);
    }

    //Cette méthode détermine si la liste d'enfants du noeudCommande a la forme:
    //     <parametre type="xxx_1">yyy_1</parametre>
    //     <parametre type="xxx_2">yyy_2</parametre>
    //                   ...
    //     <parametre type="xxx_k">yyy_k</parametre>
    //       ou xxx_i est parmis les Strings du tableau 'typePermis'
    //          yyy_i est un TextNode.  Si 'sontDesNombres'=true, yyy_i doit aussi être un nombre positif.
    //          k == typesPermis.length
    private boolean validerCommande(Element noeudCommande, String[] typesPermis, boolean sontDesNombres) {
        //Sanity check
        if (noeudCommande == null) {
            return false;
        }

        //Note: La documentation dit à propos de getChildNodes()
        //  "If there are no children, this is a NodeList containing no nodes."
        int n = noeudCommande.getChildNodes().getLength();

        // On vérifie d'abord si le nombre d'enfants du noeud de commande est correct
        if (n != typesPermis.length) {
            return false;
        }

        if (n == 0) {
            return true;
        }

        // On crée un tableau de boolean pour savoir si chaque paramètres est
        // présent exactement une fois.
        boolean[] paramEstPresent = new boolean[n];
        for (int i = 0; i < n; i++) {
            paramEstPresent[i] = false;
        }

        // On passe tous les paramètres un par un pour les valider, pour être valide
        // un paramètre dois avoir le format _exact_:
        //    <parametre type="xxx">yyy</parametre>
        //       ou xxx est une des noms dans le tableau 'typesPermis'
        //       et yyy est un TextNode
        for (int i = 0; i < n; i++) {
            Node objNoeudCourant = noeudCommande.getChildNodes().item(i);
            //Le nom du noeud n'est pas 'parametre'
            if (!objNoeudCourant.getNodeName().equals("parametre"))
                return false;

            //Nombre d'attributs inadéquats
            if (objNoeudCourant.getAttributes().getLength() != 1)
                return false;

            //L'attribut n'est pas nommé 'type'
            if (objNoeudCourant.getAttributes().getNamedItem("type") == null)
                return false;

            //S'il n'y a pas d'enfant c'est que la valeur est nulle, on modifie
            //le noeud en insérant un enfant vide, ce qui devrait en théorie
            //avoir été fait par le client.  En agissant de cette manière on
            //évite de devoir traiter la valeur null de façon spéciale.
            if (objNoeudCourant.getChildNodes().getLength() == 0)
                objNoeudCourant.appendChild(objNoeudCourant.getOwnerDocument().createTextNode(""));

            //Mauvais nombre de sous-noeud
            if (objNoeudCourant.getChildNodes().getLength() != 1)
                return false;
            
            //Le noeud enfant n'est pas un TextNode
            if (!objNoeudCourant.getChildNodes().item(0).getNodeName().equals("#text"))
                return false;

            //Ici on vérifie si la valeur de l'attribut 'type' est valide (parmi 'typesPermis')
            String paramName = objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue();
            for (int j = 0; j < n; j++) {
                if (paramName.equals(typesPermis[j])) {
                    if (paramEstPresent[j])
                        return false; //ce type a déjà été rencontré

                    if (sontDesNombres && 
                            !UtilitaireNombres.isPositiveNumber(objNoeudCourant.getChildNodes().item(0).getNodeValue()))
                        return false;

                    paramEstPresent[j] = true;
                    break;
                }
                if (j == n - 1)
                    return false; //le type n'est pas parmi 'typesPermis'
            }
        }
        return true;
    }

    private boolean validerCommandeCreateRoomOrUpdateRoom(Element noeudCommande, Commande cmd) {
        NodeList noeudsParametre = noeudCommande.getChildNodes();
        TreeSet<String> paramFound = new TreeSet<String>();
        TreeSet<String> langFound = new TreeSet<String>();
        for (int i = 0; i < noeudsParametre.getLength(); i++) {
            Node objNoeudParam = noeudsParametre.item(i);
            if (!objNoeudParam.getNodeName().equals("parametre")) {
                return false;
            }
            Node paramType, paramLid;
            String strType = "", strLid = "";
            switch (objNoeudParam.getAttributes().getLength()) {
                case 1: //Deal with parameters that do not specify a language
                    paramType = objNoeudParam.getAttributes().getNamedItem("type");
                    if (paramType == null) {
                        return false;
                    }
                    strType = paramType.getNodeValue();
                    if (cmd == Commande.UpdateRoom && strType.equals("roomId") && paramFound.add("roomId")) {
                        continue;
                    }
                    if (strType.equals("beginDate") && paramFound.add("beginDate")) {
                        continue;
                    }
                    if (strType.equals("endDate") && paramFound.add("endDate")) {
                        continue;
                    }
                    if (strType.equals("password") && paramFound.add("password")) {
                        continue;
                    }
                    if (strType.equals("masterTime") && paramFound.add("masterTime")) {
                        continue;
                    }
                    if (strType.equals("keywordIds") && paramFound.add("keywordIds")) {
                        continue;
                    }
                    if (strType.equals("gameTypeIds") && paramFound.add("gameTypeIds")) {
                        continue;
                    }
                    if (strType.equals("fullStats") && paramFound.add("fullStats")) {
                        continue;
                    }
                    return false;

                case 2: //Deal with parameters that specify a language
                    paramType = objNoeudParam.getAttributes().getNamedItem("type");
                    if (paramType == null) {
                        return false;
                    }
                    paramLid = objNoeudParam.getAttributes().getNamedItem("language_id");
                    if (paramLid == null) {
                        return false;
                    }
                    strType = paramType.getNodeValue();
                    strLid = paramLid.getNodeValue();
                    langFound.add(strLid);
                    if (strType.equals("name") && paramFound.add("n" + strLid)) {
                        continue;
                    }
                    if (strType.equals("description") && paramFound.add("d" + strLid)) {
                        continue;
                    }
                    return false;
                default:
                    return false;
            }
        }
        //System.out.println("Param found: " + paramFound);
        if (cmd == Commande.CreateRoom) {
            return (paramFound.size() == 7 + 2 * langFound.size());
        }
        if (cmd == Commande.UpdateRoom) {
            return (paramFound.size() == 8 + 2 * langFound.size());
        }
        return false;
    }

    private boolean validerCommandeDeplacerPersonnage(Element noeudCommande) {
        // Si le nom de la commande est DeplacerPersonnage, alors il doit y avoir
        //1 paramètre position contenant les coordonnées x, y
        if (noeudCommande.getChildNodes().getLength() != 1) {
            return false;
        }

        Node objNoeudCourant = noeudCommande.getChildNodes().item(0);

        // Si le noeud enfant n'est pas un paramètre, ou qu'il n'a
        // pas exactement 1 attribut, ou que le nom de cet attribut
        // n'est pas type, ou que le noeud n'a pas de valeurs, alors
        // il y a une erreur dans la structure
        if (objNoeudCourant.getNodeName().equals("parametre") == false) {
            return false;
        }
        if (objNoeudCourant.getAttributes().getLength() != 1) {
            return false;
        }
        if (objNoeudCourant.getAttributes().getNamedItem("type") == null) {
            return false;
        }
        if (objNoeudCourant.getAttributes().getNamedItem("type").getNodeValue().equals("NouvellePosition") == false) {
            return false;
        }
        if (objNoeudCourant.getChildNodes().getLength() != 1) {
            return false;
        }
        Node noeudPosition = objNoeudCourant.getChildNodes().item(0);
        if (!noeudPosition.getNodeName().equals("position")) {
            return false;
        }
        if (noeudPosition.getAttributes().getLength() != 2) {
            return false;
        }
        Node x = noeudPosition.getAttributes().getNamedItem("x");
        if (x == null) {
            return false;
        }
        Node y = noeudPosition.getAttributes().getNamedItem("y");
        if (y == null) {
            return false;
        }
        if (!UtilitaireNombres.isPositiveNumber(x.getNodeValue())) {
            return false;
        }
        if (!UtilitaireNombres.isPositiveNumber(y.getNodeValue())) {
            return false;
        }
        return true;
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
     * @param numeroPing le numéro du ping, c'est le numéro qui va servir à
     *        identifier le ping
     */
    public void envoyerEvenementPing(int numeroPing) {
        try {
            // Envoyer le message ping au joueur
            envoyerMessage("<ping numero=\"" + numeroPing + "\"/>");
        } catch (IOException e) {

            objLogger.info(GestionnaireMessages.message("protocole.erreur_ping"));
            objLogger.error(e.getMessage() + " ping  is not sent");
        }
    }

    /**
     * Cette méthode permet d'arrêter le thread et de fermer le socket du
     * client. Si le joueur était connecté à une table, une salle ou au serveur
     * de jeu, alors il sera complétement déconnecté.
     */
    public void arreterProtocoleJoueur() {
        
        try {
            // On tente de fermer le canal de réception. Cela va provoquer
            // une erreur dans le thread et le joueur va être déconnecté et
            // le thread va arrêter
            objCanalReception.close();
        } catch (IOException ioe) {
            objLogger.error(ioe.getMessage() +  " close reception canal");
        }

        try {
            // On tente de fermer le socket liant le client au serveur. Cela
            // va provoquer une erreur dans le thread et le joueur va être
            // déconnecté et le thread va arrêter
            objSocketJoueur.close();
            
        } catch (IOException ioe) {
            objLogger.error(ioe.getMessage() + "close socket in protocole");
        }
        
        Thread.currentThread().interrupt();

    }
    
    public int obtenirLastQuestionTime() {
        return lastQuestionTime;
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
     * @param joueur Le joueur humain auquel faire la référence
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
        // Obtenir la référence vers la table où le joueur était
        Table objTable = ancientJoueur.obtenirPartieCourante().obtenirTable();

        // Créer un tableau des joueurs, on a "+ 1" car le joueur
        // déconnecté n'était plus dans cette liste
        Joueur lstJoueursTable[] = new Joueur[objTable.obtenirListeJoueurs().size() + objTable.getNombreJoueursVirtuels() + 1];

        int j = 0;
        // Passer tous les positions des joueurs et les ajouter à la liste locale
        for (JoueurHumain joueur: objTable.obtenirListeJoueurs().values()) {
            lstJoueursTable[j] = joueur;
            j++;
        }

        // Ajouter les joueurs virtuels
        ArrayList<JoueurVirtuel> lstJoueursVirtuels = ancientJoueur.obtenirPartieCourante().obtenirTable().obtenirListeJoueursVirtuels();

        if (lstJoueursVirtuels != null) {
            for (int i = 0; i < lstJoueursVirtuels.size(); i++) {

                JoueurVirtuel objJoueurVirtuel = (JoueurVirtuel)lstJoueursVirtuels.get(i);

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
     * quel avatar il avait. à noter que ce message est différent de envoyer
     * liste des joueurs pour une table, il faut aussi envoyer les joueurs 
     * virtuels et s'envoyer soi-même (?) pour que le joueur qui se reconnecte
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
        for (JoueurHumain joueurHumain: lstJoueurs.values()) {
            Element objNoeudJoueur = objDocumentXML.createElement("joueur");
            objNoeudJoueur.setAttribute("nom", joueurHumain.obtenirNomUtilisateur());
            objNoeudJoueur.setAttribute("id", Integer.toString(joueurHumain.obtenirPartieCourante().obtenirIdPersonnage()));
            objNoeudJoueur.setAttribute("role", Integer.toString(joueurHumain.getRole()));
            objNoeudJoueur.setAttribute("pointage", Integer.toString(joueurHumain.obtenirPartieCourante().obtenirPointage()));
            // Ajouter le noeud de l'item au noeud du paramètre
            objNoeudParametreListeJoueurs.appendChild(objNoeudJoueur);
        }

        // -----------------------
        // S'ajouter soi-même
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
                JoueurVirtuel objJoueurVirtuel = (JoueurVirtuel)lstJoueursVirtuels.get(i);

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
    }// end method

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
	
	// Create another param for the game type
	Element objNoeudParametreMoveStep = objDocumentXML.createElement("parametre");

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
	objNoeudParametreMoveStep.setAttribute("type", "MoveStep");
	objNoeudParametreMoveStep.setAttribute("valeur", Integer.toString(ancientJoueur.obtenirPartieCourante().getMoveVisibility()));
	
	//System.out.println("Move " + ancientJoueur.obtenirPartieCourante().getMoveVisibility());

	// Ajouter les noeuds paramètres au noeud de commande dans
	// le document de sortie
	objNoeudCommande.appendChild(objNoeudParametreNbTable);
	objNoeudCommande.appendChild(objNoeudParametreMaxPlayers);
	objNoeudCommande.appendChild(objNoeudParametreGameType);
	objNoeudCommande.appendChild(objNoeudParametreMoveStep);
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
    }// end method

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
        for (ObjetUtilisable objItem: lstListeItems.values()) {
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
                ObjetUtilisable objObjetEnVente = (ObjetUtilisable)lstObjetsEnVente.get(i);

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

                // Ajouter l'attribut pour le coût de l'objet
                objNoeudObjet.setAttribute("cout", Integer.toString(intPrixObjet));

                // Ajouter l'attribut pour l'id de l'objet
                objNoeudObjet.setAttribute("id", Integer.toString(intObjetId));

                // Maintenant ajouter cet objet à la liste
                objNoeudObjetsMagasin.appendChild(objNoeudObjet);
            }
        }
        objNoeudCommande.appendChild(objNoeudObjetsMagasin);
    }//end method


    public String getLang() {
        return langue;
    }
}// end class

