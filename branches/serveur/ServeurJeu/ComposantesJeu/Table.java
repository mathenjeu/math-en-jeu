package ServeurJeu.ComposantesJeu;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;

import ClassesUtilitaires.UtilitaireNombres;
import Enumerations.Colors;
import Enumerations.RetourFonctions.ResultatDemarrerPartie;
import ServeurJeu.ControleurJeu;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.ComposantesJeu.InformationPartie;
import ServeurJeu.ComposantesJeu.Joueurs.Joueur;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;
import ServeurJeu.ComposantesJeu.Joueurs.ParametreIA;
import ServeurJeu.ComposantesJeu.Joueurs.StatisticsPlayer;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.Configuration.GestionnaireConfiguration;
import ServeurJeu.Evenements.EvenementJoueurDemarrePartie;
import ServeurJeu.Evenements.EvenementJoueurDeplacePersonnage;
import ServeurJeu.Evenements.EvenementJoueurEntreTable;
import ServeurJeu.Evenements.EvenementJoueurQuitteTable;
import ServeurJeu.Evenements.EvenementJoueurRejoindrePartie;
import ServeurJeu.Evenements.EvenementMAJArgent;
import ServeurJeu.Evenements.EvenementMAJPointage;
import ServeurJeu.Evenements.EvenementMessageChat;
import ServeurJeu.Evenements.EvenementPartieDemarree;
import ServeurJeu.Evenements.EvenementPartieTerminee;
import ServeurJeu.Evenements.EvenementSynchroniserTemps;
import ServeurJeu.Evenements.EvenementUtiliserObjet;
import ServeurJeu.Evenements.EventPlayerPictureCanceled;
import ServeurJeu.Evenements.EventPlayerSelectedPicture;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.Evenements.InformationDestination;
import ServeurJeu.Temps.GestionnaireTemps;
import ServeurJeu.Temps.Minuterie;
import ServeurJeu.Temps.ObservateurMinuterie;
import ServeurJeu.Temps.ObservateurSynchroniser;
import ServeurJeu.Temps.TacheSynchroniser;

/**
 * @author Jean-Fran�ois Brind'Amour
 */
public class Table implements ObservateurSynchroniser, ObservateurMinuterie
{
    // D�claration d'une r�f�rence vers le gestionnaire d'�v�nements
    private GestionnaireEvenements objGestionnaireEvenements;
    // points for Finish WinTheGame
    private ArrayList<Point> lstPointsFinish = new ArrayList<Point>();
    // D�claration d'une r�f�rence vers le contr�leur de jeu
    private ControleurJeu objControleurJeu;
    // D�claration d'une r�f�rence vers le gestionnaire de bases de donn�es
    private final GestionnaireBD objGestionnaireBD;
    // D�claration d'une r�f�rence vers la salle parente dans laquelle se
    // trouve cette table
    private final Salle objSalle;
    // Cette variable va contenir le num�ro de la table
    private final int intNoTable;
    // D�claration d'une constante qui d�finit le nombre maximal de joueurs
    // dans une table
    private int MAX_NB_PLAYERS;
    // Cette variable va contenir le nom d'utilisateur du cr�ateur de cette table
    private String strNomUtilisateurCreateur;
    // D�claration d'une variable qui va garder le temps total d�fini pour
    // cette table
    private final int intTempsTotal;
    // Cet objet est une liste des joueurs qui sont pr�sentement sur cette table
    private HashMap<String, JoueurHumain> lstJoueurs;
    // Cet objet est une liste des joueurs qui attendent de joueur une partie
    private HashMap<String, JoueurHumain> lstJoueursEnAttente;
    // D�claration d'une variable qui va permettre de savoir si la partie est
    // commenc�e ou non
    private boolean bolEstCommencee;
    // D�claration d'une variable qui va permettre d'arr�ter la partie en laissant
    // l'�tat de la partie � "commenc�e" tant que les joueurs sont � l'�cran des pointages
    private boolean bolEstArretee;
    // D�claration d'un tableau � 2 dimensions qui va contenir les informations
    // sur les cases du jeu
    private Case[][] objttPlateauJeu;
    private GestionnaireTemps objGestionnaireTemps;
    private TacheSynchroniser objTacheSynchroniser;
    private Minuterie objMinuterie;
    // Cet objet est une liste des joueurs virtuels qui jouent sur cette table
    private ArrayList<JoueurVirtuel> lstJoueursVirtuels;
    // Cette variable indique le nombre de joueurs virtuels sur la table
    private int intNombreJoueursVirtuels;
    // Cette liste contient le nom des joueurs qui ont �t� d�connect�s
    // dans cette table, ce qui nous permettra, lorsqu'une partie se termine, de
    // faire la mise � jour de la liste des joueurs d�connect�s du gestionnaire
    // de communication
    private LinkedList<String> lstJoueursDeconnectes;
    private Date objDateDebutPartie;
    // D�claration d'une variable qui permettra de cr�er des id pour les objets
    // On va initialis� cette variable lorsque le plateau de jeu sera cr��
    private Integer objProchainIdObjet;
    // Name of the table
    private String tableName;
    // nb lines on the game board
    private int nbLines;
    // nb columns on the game board
    private int nbColumns;
    // list of colors for the players clothes
    // after use of one color it is removed from the list
    // automaticaly - randomly is done to players
    private LinkedList<Integer> colors;
    // list of idPerso used to calculate idPersonnage
    // limits - from 0 to 11 for now, but can be changed if
    // maxNumbersofPlayers will be changed to be higher then 12
    // when player got out from table it must return his idPerso in the list
    private final LinkedList<Integer> idPersos;
    // Contient le type de jeu (ex. mathEnJeu)
    private final String gameType;
    // Cet objet permet de d�terminer les r�gles de jeu pour cette salle
    private final Regles objRegles;
    private GenerateurPartie gameFactory;

    /**
     * Constructeur de la classe Table qui permet d'initialiser les membres
     * priv�s de la table.
     *
     * @param salleParente La salle dans laquelle se trouve cette table
     * @param noTable Le num�ro de la table
     * @param joueur
     * @param tempsPartie Le temps de la partie en minute
     * @param name
     * @param intNbLines
     * @param intNbColumns
     * @param gameType
     */
    public Table(Salle salleParente, int noTable, JoueurHumain joueur, int tempsPartie,
                 String name, int intNbLines, int intNbColumns, String gameType) {
        super();


        objControleurJeu = salleParente.getObjControleurJeu();
        objGestionnaireBD = salleParente.getObjControleurJeu().obtenirGestionnaireBD();
        objGestionnaireEvenements = new GestionnaireEvenements();

        objSalle = salleParente;
        intNoTable = noTable;
        this.gameType = gameType;

        // D�finir les r�gles de jeu pour la table
        objRegles = new Regles();

        setTableName(name);

        this.nbLines = intNbLines;
        this.nbColumns = intNbColumns;

        intTempsTotal = tempsPartie;

        // Cr�er une nouvelle liste de joueurs
        lstJoueurs = new HashMap<String, JoueurHumain>();
        lstJoueursEnAttente = new HashMap<String, JoueurHumain>();
        strNomUtilisateurCreateur = joueur.obtenirNomUtilisateur();

        // Au d�part, aucune partie ne se joue sur la table
        bolEstCommencee = false;
        bolEstArretee = true;

        // initialaise gameboard - set null
        //objttPlateauJeu = null;

        objGestionnaireTemps = objControleurJeu.obtenirGestionnaireTemps();
        objTacheSynchroniser = objControleurJeu.obtenirTacheSynchroniser();

        // Au d�part, on consid�re qu'il n'y a que des joueurs humains.
        // Lorsque l'on d�marrera une partie dans laPartieCommence(), on cr�era
        // autant de joueurs virtuels que intNombreJoueursVirtuels (qui devra donc
        // �tre affect� du bon nombre au pr�alable)
        //intNombreJoueursVirtuels = 0;
        //lstJoueursVirtuels = null;

        // Cette liste sera modifi� si jamais un joueur est d�connect�
        lstJoueursDeconnectes = new LinkedList<String>();
        
        // Cr�er un thread pour le GestionnaireEvenements
        Thread threadEvenements = new Thread(objGestionnaireEvenements, "GestEven table ");
        // D�marrer le thread du gestionnaire d'�v�nements
        threadEvenements.start();

        // fill the list of colors
        this.colors = new LinkedList<Integer>();

        this.idPersos = new LinkedList<Integer>();

        creation(); // create the gameFactory

    }

    public void creation() {
        objGestionnaireBD.chargerReglesTable(objRegles, gameType, objSalle.getRoomId());
        MAX_NB_PLAYERS = objRegles.getMaxNbPlayers();
        ///System.out.println("We test Colors in the table  : " );
        this.setColors();
        this.setIdPersos();
        
        try {
            this.gameFactory = (GenerateurPartie)Class.forName("ServeurJeu.ComposantesJeu.GenerateurPartie" + gameType).newInstance();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void destruction() {
        System.out.println("table - wipeout the table - destruction");
        arreterPartie("");
       
    }// end method

    /**
     * Cette fonction permet au joueur d'entrer dans la table courante.
     * On suppose que le joueur n'est pas dans une autre table, que la table
     * courante n'est pas compl�te et qu'il n'y a pas de parties en cours.
     * Cette fonction va avoir pour effet de connecter le joueur dans la table
     * courante.
     * @param joueur Le joueur demandant d'entrer dans la table
     * @param doitGenererNoCommandeRetour Permet de savoir si on doit g�n�rer un
     *        num�ro de commande pour le retour de l'appel de fonction
     * @throws NullPointerException : Si la liste listePersonnageJoueurs est nulle
     *
     * Synchronisme Cette fonction est synchronis�e pour �viter que deux
     *              joueurs puissent entrer ou quitter la table en m�me temps.
     *              On n'a pas � s'inqui�ter que le joueur soit modifi�
     * 	            pendant le temps qu'on ex�cute cette fonction. De plus
     *              on n'a pas � rev�rifier que la table existe bien (car
     *              elle ne peut �tre supprim�e en m�me temps qu'un joueur
     *              entre dans la table), qu'elle n'est pas compl�te ou
     *              qu'une partie est en cours (car toutes les fonctions
     *              permettant de changer �a sont synchronis�es).
     */
    public void entrerTableAutres(JoueurHumain joueur, boolean doitGenererNoCommandeRetour) throws NullPointerException {
        //System.out.println("start table: " + System.currentTimeMillis());
        //Emp�cher d'autres thread de toucher � la liste des joueurs de
        //cette table pendant l'ajout du nouveau joueur dans cette table
        synchronized (lstJoueurs) {
            // Ajouter ce nouveau joueur dans la liste des joueurs de cette table
            lstJoueurs.put(joueur.obtenirNomUtilisateur(), joueur);
        }
        // Le joueur est maintenant entr� dans la table courante (il faut
        // cr�er un objet InformationPartie qui va pointer sur la table
        // courante)
        joueur.definirPartieCourante(new InformationPartie(objGestionnaireEvenements, joueur, this));
        
        // init players colors
        int color = this.getOneColor();
        joueur.obtenirPartieCourante().setClothesColor(color);

        // Si on doit g�n�rer le num�ro de commande de retour, alors
        // on le g�n�re, sinon on ne fait rien
        if (doitGenererNoCommandeRetour == true) {
            // G�n�rer un nouveau num�ro de commande qui sera
            // retourn� au client
            joueur.obtenirProtocoleJoueur().genererNumeroReponse();
        }

        // Emp�cher d'autres thread de toucher � la liste des joueurs de
        // cette salle pendant qu'on parcourt tous les joueurs de la salle
        // pour leur envoyer un �v�nement
        synchronized (getObjSalle().obtenirListeJoueurs()) {
            // Pr�parer l'�v�nement de nouveau joueur dans la table.
            // Cette fonction va passer les joueurs et cr�er un
            // InformationDestination pour chacun et ajouter l'�v�nement
            // dans la file de gestion d'�v�nements
            preparerEvenementJoueurEntreTable(joueur.obtenirNomUtilisateur(), joueur.getRole(), color);
        }
        //System.out.println("end table : " + System.currentTimeMillis());
    }// end methode

    /**
     * Cette m�thode permet au joueur pass� en param�tres de quitter la table.
     * On suppose que le joueur est dans la table.
     *
     * @param joueur Le joueur demandant de quitter la table
     * @param doitGenererNoCommandeRetour Permet de savoir si on doit g�n�rer un
     *        num�ro de commande pour le retour de l'appel de fonction
     * @param detruirePartieCourante
     *
     * Synchronisme Cette fonction est synchronis�e sur la liste des tables
     *              puis sur la liste des joueurs de cette table, car il se
     *              peut qu'on doive d�truire la table si c'est le dernier
     *              joueur et qu'on va modifier la liste des joueurs de cette
     *              table, car le joueur quitte la table. Cela �vite que des
     *              joueurs entrent ou quittent une table en m�me temps.
     *              On n'a pas � s'inqui�ter que le joueur soit modifi�
     *              pendant le temps qu'on ex�cute cette fonction. Si on
     *              inverserait les synchronisations, �a pourrait cr�er un
     *              deadlock avec les personnes entrant dans la salle.

     */
    public void quitterTable(JoueurHumain joueur, boolean doitGenererNoCommandeRetour, boolean detruirePartieCourante) {
        // Emp�cher d'autres thread de toucher � la liste des joueurs de
        // cette table pendant que le joueur quitte cette table
        synchronized (lstJoueurs) {
            // Enlever le joueur de la liste des joueurs de cette table
            lstJoueurs.remove(joueur.obtenirNomUtilisateur());
            lstJoueursEnAttente.remove(joueur.obtenirNomUtilisateur());
            colors.add(joueur.obtenirPartieCourante().resetColor()); 
            // Le joueur est maintenant dans aucune table
            if (detruirePartieCourante == true) {
                joueur.obtenirPartieCourante().destruction();
                joueur.definirPartieCourante(null);
            }

            // Si on doit g�n�rer le num�ro de commande de retour, alors
            // on le g�n�re, sinon on ne fait rien (�a se peut que ce soit
            // faux)
            if (doitGenererNoCommandeRetour == true) {
                // G�n�rer un nouveau num�ro de commande qui sera
                // retourn� au client
                joueur.obtenirProtocoleJoueur().genererNumeroReponse();
            }

            // Emp�cher d'autres thread de toucher � la liste des joueurs de
            // cette salle pendant qu'on parcourt tous les joueurs de la salle
            // pour leur envoyer un �v�nement
            synchronized (getObjSalle().obtenirListeJoueurs()) {
                // Pr�parer l'�v�nement qu'un joueur a quitt� la table.
                // Cette fonction va passer les joueurs et cr�er un
                // InformationDestination pour chacun et ajouter l'�v�nement
                // dans la file de gestion d'�v�nements
                preparerEvenementJoueurQuitteTable(joueur.obtenirNomUtilisateur());
            }
            // Emp�cher d'autres thread de toucher � la liste des tables de
            // cette salle pendant que le joueur quitte cette table
            synchronized (getObjSalle().obtenirListeTables()) {
                // S'il ne reste aucun joueur dans la table et que la partie
                // est termin�e, alors on doit d�truire la table
                if ((lstJoueurs.isEmpty() && bolEstArretee == true) || (joueur.obtenirNomUtilisateur().equals(strNomUtilisateurCreateur) && !bolEstCommencee)) {
                    //Arreter le gestionnaire de temps
                    //objGestionnaireTemps.arreterGestionnaireTemps();
                    // D�truire la table courante et envoyer les �v�nements
                    // appropri�s
                    //System.out.println("live the table - lst " + lstJoueurs.size());
                    getObjSalle().detruireTable(this);

                }
            }
        }// !!!!!!!

    }// end method

    /**
     * Cette m�thode permet au joueur pass� en param�tres de recommencer la partie.
     * On suppose que le joueur est dans la table.
     *
     * @param joueur Le joueur demandant de recommencer
     * @param doitGenererNoCommandeRetour Permet de savoir si on doit g�n�rer un
     *        num�ro de commande pour le retour de l'appel de fonction
     *
     * Synchronisme Cette fonction est synchronis�e sur la liste des tables
     * 	            puis sur la liste des joueurs de cette table, car il se
     *              peut qu'on doive d�truire la table si c'est le dernier
     *              joueur et qu'on va modifier la liste des joueurs de cette
     *              table, car le joueur quitte la table. Cela �vite que des
     *              joueurs entrent ou quittent une table en m�me temps.
     *              On n'a pas � s'inqui�ter que le joueur soit modifi�
     *              pendant le temps qu'on ex�cute cette fonction. Si on
     *              inverserait les synchronisations, �a pourrait cr�er un
     *              deadlock avec les personnes entrant dans la salle.
     */
    public void restartGame(JoueurHumain joueur, boolean doitGenererNoCommandeRetour) {
    	// to get back perso's clothes color 
        //returned to the list when get out from game
        joueur.obtenirPartieCourante().setClothesColor(colors.getLast());
    	
    	// Emp�cher d'autres thread de toucher � la liste des tables de
        // cette salle pendant que le joueur entre dans cette table
        synchronized (getObjSalle().obtenirListeTables()) {
            // Emp�cher d'autres thread de toucher � la liste des joueurs de
            // cette table pendant que le joueur et ajouter a la table
            synchronized (lstJoueurs) {
                // ajouter le joueur a la liste des joueurs de cette table
                lstJoueurs.put(joueur.obtenirNomUtilisateur(), joueur);

                // Si on doit g�n�rer le num�ro de commande de retour, alors
                // on le g�n�re, sinon on ne fait rien (�a se peut que ce soit
                // faux)
                if (doitGenererNoCommandeRetour == true) {
                    // G�n�rer un nouveau num�ro de commande qui sera
                    // retourn� au client
                    joueur.obtenirProtocoleJoueur().genererNumeroReponse();
                }

                preparerEvenementJoueurRejoindrePartie(joueur);

            }
            synchronized (lstJoueursDeconnectes) {
                lstJoueursDeconnectes.remove(joueur);
            }
        }        
    }

    /**
     * Cette m�thode permet au joueur pass� en param�tres de d�marrer la partie.
     * On suppose que le joueur est dans la table.
     * @param joueur Le joueur demandant de d�marrer la partie
     * @param idDessin Le num�ro Id du personnage choisi par le joueur
     * @param doitGenererNoCommandeRetour Permet de savoir si on doit g�n�rer un
     *        num�ro de commande pour le retour de l'appel de fonction
     * Synchronisme Cette fonction est synchronis�e sur la liste des joueurs
     *              en attente, car il se peut qu'on ajouter ou retirer des
     *              joueurs de la liste en attente en m�me temps. On n'a pas
     *              � s'inqui�ter que le m�me joueur soit mis dans la liste
     *              des joueurs en attente par un autre thread.
     * @return
     */
    public ResultatDemarrerPartie demarrerPartie(JoueurHumain joueur, int idDessin, boolean doitGenererNoCommandeRetour) {
        // Cette variable va permettre de savoir si le joueur est maintenant
        // attente ou non
        ResultatDemarrerPartie resultatDemarrerPartie;

        // Emp�cher d'autres thread de toucher � la liste des joueurs en attente
        // de cette table pendant que le joueur tente de d�marrer la partie
        synchronized (lstJoueursEnAttente) {
            // Si une partie est en cours alors on va retourner PartieEnCours
            if (bolEstCommencee == true) {
                resultatDemarrerPartie = ResultatDemarrerPartie.PartieEnCours;
            } // Sinon si le joueur est d�j� en attente, alors on va retourner
            // DejaEnAttente
            else if (lstJoueursEnAttente.containsKey(joueur.obtenirNomUtilisateur()) == true) {
                resultatDemarrerPartie = ResultatDemarrerPartie.DejaEnAttente;
            } else {
                // La commande s'est effectu�e avec succ�s
                resultatDemarrerPartie = ResultatDemarrerPartie.Succes;

                // Ajouter le joueur dans la liste des joueurs en attente
                lstJoueursEnAttente.put(joueur.obtenirNomUtilisateur(), joueur);

                int idPersonnage = this.getOneIdPersonnage(idDessin);

                //System.out.println("idPersonnage demarrePartie : " + idPersonnage);

                // Garder en m�moire le Id du personnage choisi par le joueur et son dessin
                joueur.obtenirPartieCourante().setIdDessin(idDessin);
                joueur.obtenirPartieCourante().definirIdPersonnage(idPersonnage);
               
                // Si on doit g�n�rer le num�ro de commande de retour, alors
                // on le g�n�re, sinon on ne fait rien (�a se peut que ce soit
                // faux)
                if (doitGenererNoCommandeRetour == true) {
                    // G�n�rer un nouveau num�ro de commande qui sera
                    // retourn� au client
                    joueur.obtenirProtocoleJoueur().genererNumeroReponse();
                }

                // Emp�cher d'autres thread de toucher � la liste des joueurs de
                // cette table pendant qu'on parcourt tous les joueurs de la table
                // pour leur envoyer un �v�nement
                synchronized (lstJoueurs) {
                    // Pr�parer l'�v�nement de joueur en attente.
                    // Cette fonction va passer les joueurs et cr�er un
                    // InformationDestination pour chacun et ajouter l'�v�nement
                    // dans la file de gestion d'�v�nements
                    preparerEvenementJoueurDemarrePartie(joueur.obtenirNomUtilisateur(), idPersonnage);
                }

                // Si le nombre de joueurs en attente est maintenant le nombre
                // de joueurs que �a prend pour joueur au jeu, alors on lance
                // un �v�nement qui indique que la partie est commenc�e
                if (lstJoueursEnAttente.size() == MAX_NB_PLAYERS) {
                    laPartieCommence("Aucun");
                }
            }
        }
        return resultatDemarrerPartie;
    }

    /**
     * This method will cancel the picture used by the player
     * (action initiate by the player). He will choose another one 
     * @param player
     * @param doitGenererNoCommandeRetour
     */
    public void cancelPicture(JoueurHumain player, boolean doitGenererNoCommandeRetour)
    {
    	 // get id from user to find the id of the picture
    	 int idPersonnage = player.obtenirPartieCourante().obtenirIdPersonnage();
    	 //int idDessin = player.obtenirPartieCourante().getIdDessin();
    	 //cancel the carrent ids
    	 player.obtenirPartieCourante().definirIdPersonnage(0);
    	 player.obtenirPartieCourante().setIdDessin(0);
    	 this.getBackOneIdPersonnage(idPersonnage);
    	     	 
         //System.out.println("idPersonnage demarrePartie : " + idPersonnage);
         
         // Si on doit g�n�rer le num�ro de commande de retour, alors
         // on le g�n�re, sinon on ne fait rien (�a se peut que ce soit
         // faux)
         if (doitGenererNoCommandeRetour == true) {
             // G�n�rer un nouveau num�ro de commande qui sera
             // retourn� au client
             player.obtenirProtocoleJoueur().genererNumeroReponse();
         }

         // Emp�cher d'autres thread de toucher � la liste des joueurs de
         // cette table pendant qu'on parcourt tous les joueurs de la table
         // pour leur envoyer un �v�nement
         synchronized (lstJoueurs) {
             // Pr�parer l'�v�nement de joueur en attente.
             // Cette fonction va passer les joueurs et cr�er un
             // InformationDestination pour chacun et ajouter l'�v�nement
             // dans la file de gestion d'�v�nements
             prepareEventPlayerCanceledPicture(player.obtenirNomUtilisateur(), idPersonnage);
         }
    }
    
    /**
     * This method will put on the system the picture selected  by the player
     * (action initiate by the player).  
     * @param player
     * @param doitGenererNoCommandeRetour
     */
    public void setNewPicture(JoueurHumain humainPlayer, int idDessin,
			boolean doitGenererNoCommandeRetour) {
    	int idPersonnage = this.getOneIdPersonnage(idDessin);

        //System.out.println("idPersonnage demarrePartie : " + idPersonnage);

        // Garder en m�moire le Id du personnage choisi par le joueur et son dessin
    	humainPlayer.obtenirPartieCourante().setIdDessin(idDessin);
    	humainPlayer.obtenirPartieCourante().definirIdPersonnage(idPersonnage);
       
        // Si on doit g�n�rer le num�ro de commande de retour, alors
        // on le g�n�re, sinon on ne fait rien (�a se peut que ce soit
        // faux)
        if (doitGenererNoCommandeRetour == true) {
            // G�n�rer un nouveau num�ro de commande qui sera
            // retourn� au client
        	humainPlayer.obtenirProtocoleJoueur().genererNumeroReponse();
        }

        // Emp�cher d'autres thread de toucher � la liste des joueurs de
        // cette table pendant qu'on parcourt tous les joueurs de la table
        // pour leur envoyer un �v�nement
        synchronized (lstJoueurs) {
            // Pr�parer l'�v�nement de joueur en attente.
            // Cette fonction va passer les joueurs et cr�er un
            // InformationDestination pour chacun et ajouter l'�v�nement
            // dans la file de gestion d'�v�nements
            prepareEventPlayerSelectedNewPicture(humainPlayer.obtenirNomUtilisateur(), idPersonnage);
        }
		
	}
    	
	/**
     *
     * @param joueur
     * @param doitGenererNoCommandeRetour
     * @param strParamJoueurVirtuel
     * @return
     */
    public ResultatDemarrerPartie demarrerMaintenant(JoueurHumain joueur, boolean doitGenererNoCommandeRetour, String strParamJoueurVirtuel) {
        // Lorsqu'on fait d�marr� maintenant, le nombre de joueurs sur la
        // table devient le nombre de joueurs demand�, lorsqu'ils auront tous
        // fait OK, la partie d�marrera

        ResultatDemarrerPartie resultatDemarrerPartie;
        synchronized (lstJoueursEnAttente) {
            // Si une partie est en cours alors on va retourner PartieEnCours
            if (bolEstCommencee == true) {
                resultatDemarrerPartie = ResultatDemarrerPartie.PartieEnCours;
            } //TODO si joueur pas en attente?????
            else {
                // La commande s'est effectu�e avec succ�s
                resultatDemarrerPartie = ResultatDemarrerPartie.Succes;

                // Ajouter le joueur dans la liste des joueurs en attente
                //lstJoueursEnAttente.put(joueur.obtenirNomUtilisateur(), joueur);

                // Garder en m�moire le Id du personnage choisi par le joueur
                //joueur.obtenirPartieCourante().definirIdPersonnage(idPersonnage);


                // Si on doit g�n�rer le num�ro de commande de retour, alors
                // on le g�n�re, sinon on ne fait rien (�a se peut que ce soit
                // faux)
                if (doitGenererNoCommandeRetour == true) {
                    // G�n�rer un nouveau num�ro de commande qui sera
                    // retourn� au client
                    joueur.obtenirProtocoleJoueur().genererNumeroReponse();
                }

                // Si le nombre de joueurs en attente est maintenant le nombre
                // de joueurs que �a prend pour joueur au jeu, alors on lance
                // un �v�nement qui indique que la partie est commenc�e

                laPartieCommence(strParamJoueurVirtuel);

            }
        }
        return resultatDemarrerPartie;
    }


    /* Cette fonction permet d'obtenir un tableau contenant intNombreJoueurs
     * noms de joueurs virtuels diff�rents
     */
    private String[] obtenirNomsJoueursVirtuels(int intNombreJoueurs) {
        
    	// Initialiser les noms des joueurs virtuels        
        String[] tNomsTemp = GestionnaireConfiguration.obtenirInstance().obtenirString("joueurs-virtuels.noms").split("/");
	        	
        // Obtenir le nombre de noms dans la banque
        int intQuantiteBanque = tNomsTemp.length;

        // D�claration d'un tableau pour m�langer les indices de noms
        int tIndexNom[] = new int[intQuantiteBanque];

        // Permet d'�changer des indices du tableau pour m�langer
        int intTemp;
        int intA;
        int intB;

        // Pr�parer le tableau pour le m�lange
        for (int i = 0; i < tIndexNom.length; i++) {
            tIndexNom[i] = i;
        }

        // M�langer les noms
        for (int i = 0; i < intNombreJoueurs; i++) {
            intA = i;
            intB = objControleurJeu.genererNbAleatoire(intQuantiteBanque);

            intTemp = tIndexNom[intA];
            tIndexNom[intA] = tIndexNom[intB];
            tIndexNom[intB] = intTemp;
        }

        // Cr�er le tableau de retour
        String tRetour[] = new String[intNombreJoueurs];

        // Choisir au hasard o� aller chercher les indices
        int intDepart = objControleurJeu.genererNbAleatoire(intQuantiteBanque);

        // Remplir le tableau avec les valeurs trouv�es
        for (int i = 0; i < intNombreJoueurs; i++) {
            tRetour[i] = new String(tNomsTemp[(i + intDepart) % intQuantiteBanque]);
        }

        return tRetour;
    }

    /**
     * Method used to start the game
     * @param strParamJoueurVirtuel
     */
    private void laPartieCommence(String strParamJoueurVirtuel) {
        // Cr�er une nouvelle liste qui va garder les points des
        // cases libres (n'ayant pas d'objets dessus)
        ArrayList<Point> lstPointsCaseLibre = new ArrayList<Point>();


        // Cr�er un tableau de points qui va contenir la position
        // des joueurs
        Point[] objtPositionsJoueurs;

        // Contient les noms des joueurs virtuels
        String tNomsJoueursVirtuels[] = null;

        // Contiendra le dernier ID des objets
        objProchainIdObjet = new Integer(0);

        //TODO: Peut-�tre devoir synchroniser cette partie, il
        //      faut voir avec les autres bouts de code qui
        // 		v�rifient si la partie est commenc�e (c'est OK
        //		pour entrerTable)
        // Changer l'�tat de la table pour dire que maintenant une
        // partie est commenc�e
        bolEstCommencee = true;

        // Change l'�tat de la table pour dire que la partie
        // n'est pas arr�t�e (note: bolEstCommencee restera � true
        // pendant que les joueurs sont � l'�cran de pointage)
        bolEstArretee = false;

        // G�n�rer le plateau de jeu selon les r�gles de la table et
        // garder le plateau en m�moire dans la table
        objttPlateauJeu = getGameFactory().genererPlateauJeu(lstPointsCaseLibre, lstPointsFinish, this);

        // D�finir le prochain id pour les objets
        objProchainIdObjet++;

        // Obtenir la position des joueurs de cette table
        int nbJoueur = lstJoueursEnAttente.size(); //TODO a v�rifier

        // Contient le niveau de difficult� que le joueur d�sire pour
        // les joueurs virtuels
        // on obtient la difficult� par d�faut � partir du fichier de configuration
        GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();

        int intDifficulteJoueurVirtuel = config.obtenirNombreEntier("joueurs-virtuels.difficulte_defaut");


        // Obtenir le nombre de joueurs virtuel requis
        // V�rifier d'abord le param�tre envoyer par le joueur
        if (strParamJoueurVirtuel.equals("Aucun")) {
            intNombreJoueursVirtuels = 0;
        } else {
            // Le joueur veut des joueurs virtuels
            if (strParamJoueurVirtuel.equals("Facile")) {
                intDifficulteJoueurVirtuel = ParametreIA.DIFFICULTE_FACILE;
            } else if (strParamJoueurVirtuel.equals("Intermediaire")) {
                intDifficulteJoueurVirtuel = ParametreIA.DIFFICULTE_MOYEN;
            } else if (strParamJoueurVirtuel.equals("Difficile")) {
                intDifficulteJoueurVirtuel = ParametreIA.DIFFICULTE_DIFFICILE;
            } else if (strParamJoueurVirtuel.equals("TresDifficile")) {
                intDifficulteJoueurVirtuel = ParametreIA.DIFFICULTE_TRES_DIFFICILE;
            }

            // D�terminer combien de joueurs virtuels on veut
            int maxNombreJoueursVirtuels = getRegles().getNbVirtualPlayers();
            if (nbJoueur < getRegles().getNbTracks()) {
                intNombreJoueursVirtuels = maxNombreJoueursVirtuels;
                while (maxNombreJoueursVirtuels + nbJoueur > getRegles().getNbTracks()) {
                    intNombreJoueursVirtuels--;
                    maxNombreJoueursVirtuels--;
                }
            }

        }

        objtPositionsJoueurs = this.getGameFactory().genererPositionJoueurs(this, nbJoueur + intNombreJoueursVirtuels, lstPointsCaseLibre);
        // Cr�ation d'une nouvelle liste
        Joueur[] lstJoueursParticipants = new Joueur[nbJoueur + intNombreJoueursVirtuels];

        // Obtenir un it�rateur pour l'ensemble contenant les personnages
        Iterator<JoueurHumain> objIterateurListeJoueurs = lstJoueursEnAttente.values().iterator();

        // S'il y a des joueurs virtuels, alors on va cr�er une nouvelle liste
        // qui contiendra ces joueurs
        if (intNombreJoueursVirtuels > 0) {
            lstJoueursVirtuels = new ArrayList<JoueurVirtuel>();

            // Aller trouver les noms des joueurs virtuels
            tNomsJoueursVirtuels = obtenirNomsJoueursVirtuels(intNombreJoueursVirtuels);
        }

        // Cette variable permettra d'affecter aux joueurs virtuels des id
        // de personnage diff�rents de ceux des joueurs humains
        int intIdPersonnage = 1;
        int position = 0;

        // Passer toutes les positions des joueurs et les d�finir
        for (int i = 0; i < objtPositionsJoueurs.length; i++) {

            // On doit affecter certains positions aux joueurs humains et d'autres aux joueurs
            // virtuels. La grandeur de objtPositionsJoueurs est nbJoueur + intNombreJoueursVirtuels
            if (i < nbJoueur) {

                // Comme les positions sont g�n�r�es al�atoirement, on
                // se fou un peu duquel on va d�finir la position en
                // premier, on va donc passer simplement la liste des
                // joueurs
                // Cr�er une r�f�rence vers le joueur courant
                // dans la liste (pas besoin de v�rifier s'il y en a un
                // prochain, car on a g�n�r� la position des joueurs
                // selon cette liste
                JoueurHumain objJoueur = objIterateurListeJoueurs.next();

                if (objJoueur.getRole() == 2) {
                    // D�finir la position du joueur master
                    objJoueur.obtenirPartieCourante().definirPositionJoueur(objtPositionsJoueurs[objtPositionsJoueurs.length - 1]);

                    // Ajouter la position du master dans la liste
                    //lstPositionsJoueurs.put(objJoueur.obtenirNomUtilisateur(), objtPositionsJoueurs[objtPositionsJoueurs.length - 1]);

                    position--;
                } else {

                    // D�finir la position du joueur courant
                    objJoueur.obtenirPartieCourante().definirPositionJoueur(objtPositionsJoueurs[position]);

                    // Ajouter la position du joueur dans la liste
                    //lstPositionsJoueurs.put(objJoueur.obtenirNomUtilisateur(), objtPositionsJoueurs[position]);
                }

                lstJoueursParticipants[i] = objJoueur;



            } else {
                int IDdess;
                boolean weHaveThisNumber;
                // to have differents pictures for the virtual players
                do {
                	weHaveThisNumber = false;
                	IDdess = objControleurJeu.genererNbAleatoire(11) + 1;
                    
                    for(JoueurVirtuel joueur:lstJoueursVirtuels)
                    {
                    	if(joueur.getIdDessin() == IDdess)
                    		weHaveThisNumber = true;
                    }
                } while (weHaveThisNumber);

                // On se rendra ici seulement si intNombreJoueursVirtuels > 0
                // C'est ici qu'on cr�e les joueurs virtuels, ils vont commencer
                // � jouer plus loin

                // Ajouter un joueur virtuel dans la table
                intIdPersonnage = 10000 + 100 * IDdess + 50 + i;

                // Utiliser le prochaine id de personnage libre
                while (!idPersonnageEstLibre(intIdPersonnage)) {
                    // Incr�menter le id du personnage en esp�rant en trouver un autre
                    intIdPersonnage++;
                }

                // to have virtual players of all difficulty levels
                intDifficulteJoueurVirtuel = objControleurJeu.genererNbAleatoire(4);
                //System.out.println("Virtuel : " + intDifficulteJoueurVirtuel);

                // Cr�� le joueur virtuel selon le niveau de difficult� d�sir�
                JoueurVirtuel objJoueurVirtuel = new JoueurVirtuel(tNomsJoueursVirtuels[i - nbJoueur],
                        intDifficulteJoueurVirtuel, this, intIdPersonnage);

                // D�finir sa position
                objJoueurVirtuel.definirPositionJoueurVirtuel(objtPositionsJoueurs[position]);

                // Ajouter le joueur virtuel � la liste
                lstJoueursVirtuels.add(objJoueurVirtuel);

                // Ajouter le joueur virtuel � la liste des positions, liste qui sera envoy�e
                // aux joueurs humains
                //lstPositionsJoueurs.put(objJoueurVirtuel.obtenirNom(), objtPositionsJoueurs[position]);
                lstJoueursParticipants[i] = objJoueurVirtuel;

                // Pour le prochain joueur virtuel
                intIdPersonnage++;
                
                int color = this.getOneColor();
                //System.out.println("colors: " + color);
                objJoueurVirtuel.setClothesColor(color);
                objJoueurVirtuel.setIdDessin(IDdess);


            }
            position++;
        }

        // On peut maintenant vider la liste des joueurs en attente
        // car elle ne nous sert plus � rien
        lstJoueursEnAttente.clear();


        // Maintenant pour tous les joueurs, s'il y a des joueurs
        // virtuels de pr�sents, on leur envoit un message comme
        // quoi les joueurs virtuels sont pr�ts
        if (intNombreJoueursVirtuels > 0) {
            synchronized (lstJoueursVirtuels) {
                for (int i = 0; i < lstJoueursVirtuels.size(); i++) {
                    // Pr�parer l'�v�nement de joueur en attente.
                    // Cette fonction va passer les joueurs et cr�er un
                    // InformationDestination pour chacun et ajouter l'�v�nement
                    // dans la file de gestion d'�v�nements
                    JoueurVirtuel objJoueurVirtuel = lstJoueursVirtuels.get(i);
                    preparerEvenementJoueurEntreTable(objJoueurVirtuel.obtenirNom(), 1, objJoueurVirtuel.getClothesColor());
                    preparerEvenementJoueurDemarrePartie(objJoueurVirtuel.obtenirNom(), objJoueurVirtuel.obtenirIdPersonnage());
                }
            }
        }

        // Pr�parer l'�v�nement que la partie est commenc�e.
        // Cette fonction va passer les joueurs et cr�er un
        // InformationDestination pour chacun et ajouter l'�v�nement
        // dans la file de gestion d'�v�nements
        preparerEvenementPartieDemarree(lstJoueursParticipants);
        

        int tempsStep = 1;
        objTacheSynchroniser.ajouterObservateur(this);
        objMinuterie = new Minuterie(intTempsTotal * 60, tempsStep);
        objMinuterie.ajouterObservateur(this);
        objGestionnaireTemps.ajouterTache(objMinuterie, tempsStep);

        // Obtenir la date � ce moment pr�cis
        objDateDebutPartie = new Date();

        // D�marrer tous les joueurs virtuels
        if (intNombreJoueursVirtuels > 0) {
            for (int i = 0; i < lstJoueursVirtuels.size(); i++) {
                Thread threadJoueurVirtuel = new Thread(lstJoueursVirtuels.get(i), "Virtuel");
                threadJoueurVirtuel.start();
            }

        }
        
    }// end method

    public void arreterPartie(String joueurGagnant) {

        // bolEstArretee permet de savoir si cette fonction a d�j� �t� appel�e
        // de plus, bolEstArretee et bolEstCommencee permettent de conna�tre
        // l'�tat de la partie
        if (bolEstArretee == false) {
            objTacheSynchroniser.enleverObservateur(this);
            objGestionnaireTemps.enleverTache(objMinuterie);
            objMinuterie = null;
            objGestionnaireTemps = null;
            objTacheSynchroniser = null;

            // Arr�ter la partie
            bolEstArretee = true;

            // S'il y a au moins un joueur qui a compl�t� la partie,
            // alors on ajoute les informations de cette partie dans la BD
            if (lstJoueurs.size() > 0) {
                // Sert � d�terminer le meilleur score pour cette partie
                int meilleurPointage = 0;

                TreeSet<StatisticsPlayer> ourResults = new TreeSet<StatisticsPlayer>();

                // Parcours des joueurs virtuels pour trouver le meilleur pointage
                if (lstJoueursVirtuels != null) {
                    for (int i = 0; i < lstJoueursVirtuels.size(); i++) {
                        JoueurVirtuel objJoueurVirtuel = lstJoueursVirtuels.get(i);
                        if (objJoueurVirtuel.obtenirPointage() > meilleurPointage) {
                            meilleurPointage = objJoueurVirtuel.obtenirPointage();
                        }
                        ourResults.add(new StatisticsPlayer(objJoueurVirtuel.obtenirNom(), objJoueurVirtuel.obtenirPointage(), objJoueurVirtuel.getPointsFinalTime()));
                    }
                }

                synchronized (lstJoueurs) {
                    // Parcours des joueurs pour trouver le meilleur pointage
                    int cleJoueurGagnant = 0; //0 veut dire un joueur virtuel gagne.
                    for (JoueurHumain objJoueurHumain: lstJoueurs.values()) {
                        InformationPartie infoPartie = objJoueurHumain.obtenirPartieCourante();
                        if (infoPartie.obtenirPointage() > meilleurPointage) {
                            meilleurPointage = infoPartie.obtenirPointage();
                        }
                        
                        ourResults.add(new StatisticsPlayer(objJoueurHumain.obtenirNomUtilisateur(), infoPartie.obtenirPointage(), infoPartie.getPointsFinalTime()));
                        
                        if (!joueurGagnant.equals("")) {
                            if (objJoueurHumain.obtenirNomUtilisateur().equalsIgnoreCase(joueurGagnant))
                                cleJoueurGagnant = objJoueurHumain.obtenirCleJoueur();
                        }
                        else if (ourResults.last().getUsername().equalsIgnoreCase(objJoueurHumain.obtenirNomUtilisateur()))
                            cleJoueurGagnant = objJoueurHumain.obtenirCleJoueur();
                        
                        //System.out.println("table - Joueur H " + " " + objJoueurHumain.obtenirNomUtilisateur() + " " + infoPartie.getPointsFinalTime() + " " + ourResults.size());
                    }

                    // Ajouter la partie dans la BD
                    int clePartie = objGestionnaireBD.ajouterInfosPartieTerminee(
                            objSalle.getRoomId(), gameType, objDateDebutPartie, intTempsTotal, cleJoueurGagnant);


                    preparerEvenementPartieTerminee(ourResults, joueurGagnant);

                    // Parcours des joueurs pour mise � jour de la BD et
                    // pour ajouter les infos de la partie compl�t�e
                    for (JoueurHumain joueur: lstJoueurs.values()) {
                        joueur.obtenirPartieCourante().getObjGestionnaireBD().mettreAJourJoueur(intTempsTotal);
                        // if the game was with the permission to use user's money from DB
                        if (joueur.obtenirPartieCourante().obtenirTable().getRegles().isBolMoneyPermit()) {
                        	joueur.obtenirPartieCourante().getObjGestionnaireBD().setNewPlayersMoney();
                        }
                        boolean estGagnant = joueur.obtenirCleJoueur() == cleJoueurGagnant;
                        objGestionnaireBD.ajouterInfosJoueurPartieTerminee(clePartie, joueur, estGagnant);
                        if(joueur.getRole() > 1)
                        	joueur.obtenirPartieCourante().writeInfo();

                    }
                } //// end sinchro
            }

            synchronized(lstJoueursVirtuels){
            	// Arr�ter les threads des joueurs virtuels
            	if (intNombreJoueursVirtuels > 0) {
            		int n = lstJoueursVirtuels.size();
            		for (int i = 0; i < n; i++) {
            			((JoueurVirtuel)lstJoueursVirtuels.get(i)).arreterThread();

            		}
            		lstJoueursVirtuels.clear();
            		//System.out.println("table - etape 1 lst Virtuels " + lstJoueursVirtuels.size());
            	}
            }

            // wipeout players from the table
            if (!lstJoueurs.isEmpty()) {
               /*
                synchronized (lstJoueurs)
                {
               
                Iterator<JoueurHumain> iteratorJoueursHumains = lstJoueurs.values().iterator();
                while (iteratorJoueursHumains.hasNext())
                {
                JoueurHumain objJoueurHumain = (JoueurHumain)iteratorJoueursHumains.next();
                objJoueurHumain.obtenirPartieCourante().destruction();
                objJoueurHumain.definirPartieCourante(null);
                }
                lstJoueurs.clear();

                }*/

                //System.out.println("table - etape 1 lst Humains " + lstJoueurs.size());
            }

            // Enlever les joueurs d�connect�s de cette table de la
            // liste des joueurs d�connect�s du serveur pour �viter
            // qu'ils ne se reconnectent et tentent de rejoindre une partie termin�e
            for (int i = 0; i < lstJoueursDeconnectes.size(); i++) {
                objControleurJeu.enleverJoueurDeconnecte(lstJoueursDeconnectes.get(i));
            }

            // Enlever les joueurs d�connect�s de cette table
            lstJoueursDeconnectes = new LinkedList<String>();
            //System.out.println("table - etape 1 " + lstJoueurs.size());
            // Si jamais les joueurs humains sont tous d�connect�s, alors
            // il faut d�truire la table ici
            if (lstJoueurs.isEmpty()) {
                // D�truire la table courante et envoyer les �v�nements
                // appropri�s
                //System.out.println("table - etape - is empty");
                getObjSalle().detruireTable(this);
            }
        }// end if bolEstArretee

        if (bolEstArretee == true) {

            //objGestionnaireEvenements.arreterGestionnaireEvenements();
            //this.objGestionnaireEvenements = null;
            //strNomUtilisateurCreateur = null;
            //objSalle = null;
            //objControleurJeu = null;
            //this.objGestionnaireBD = null;
            this.objttPlateauJeu = null;
            this.gameFactory = null;
            //System.out.println("table - etape 2");
        }
        //System.out.println("table - end of method");
    }// end method

    /**
     * If all the other players than that in param is on the points of Finish line
     * @param joueurHumain
     * @return
     */
    public boolean isAllTheHumainsOnTheFinish(JoueurHumain joueurHumain) {
        boolean isAllPlayers = true;
        int tracks = getRegles().getNbTracks();

        synchronized (lstJoueurs) {
            // Pass all players to find their position
            for (JoueurHumain objJoueurHumain: lstJoueurs.values()) {
                if (!objJoueurHumain.obtenirNomUtilisateur().equals(joueurHumain.obtenirNomUtilisateur())) {
                    Point pozJoueur = objJoueurHumain.obtenirPartieCourante().obtenirPositionJoueur();
                    Point objPoint = new Point(getNbLines() - 1, getNbColumns() - 1);
                    Point objPointFinish = new Point();
                    boolean isOn = false;
                    for (int i = 0; i < tracks; i++) {
                        objPointFinish.setLocation(objPoint.x, objPoint.y - i);
                        if (pozJoueur.equals(objPointFinish)) {
                            isOn = true;
                        }
                    }
                    if (!isOn) {
                        isAllPlayers = false;
                    }
                }
            }
        }


        //System.out.println(isAllPlayers + " isAll");
        return isAllPlayers;

    }

    /**
     * Cette fonction permet de retourner le num�ro de la table courante.
     *
     * @return int : Le num�ro de la table
     */
    public int obtenirNoTable() {
        return intNoTable;
    }

    /**
     * Cette fonction permet de retourner la liste des joueurs. La vraie liste
     * est retourn�e.
     *
     * @return TreeMap : La liste des joueurs se trouvant dans la table courante
     *
     * Synchronisme : Cette fonction n'est pas synchronis�e ici, mais elle doit
     * 				  l'�tre par l'appelant de cette fonction tout d�pendant
     * 				  du traitement qu'elle doit faire
     */
    public HashMap<String, JoueurHumain> obtenirListeJoueurs() {
        return lstJoueurs;
    }

    /**
     * Cette fonction permet de retourner la liste des joueurs qui sont en
     * attente de jouer une partie. La vraie liste est retourn�e.
     *
     * @return TreeMap : La liste des joueurs en attente se trouvant dans la
     * 					 table courante
     *
     * Synchronisme : Cette fonction n'est pas synchronis�e ici, mais elle doit
     * 				  l'�tre par l'appelant de cette fonction tout d�pendant
     * 				  du traitement qu'elle doit faire
     */
    public HashMap<String, JoueurHumain> obtenirListeJoueursEnAttente() {
        return lstJoueursEnAttente;
    }

    /**
     * Cette fonction permet de retourner le temps total des parties de cette
     * table.
     *
     * @return int : Le temps total des parties de cette table
     */
    public int obtenirTempsTotal() {
        return intTempsTotal;
    }

    /**
     * Cette fonction permet de d�terminer si la table est compl�te ou non
     * (elle est compl�te si le nombre de joueurs dans cette table �gale le
     * nombre de joueurs maximum par table).
     *
     * @return boolean : true si la table est compl�te
     * 					 false sinon
     *
     * Synchronisme : Cette fonction est synchronis�e car il peut s'ajouter de
     * 				  nouveaux joueurs ou d'autres peuvent quitter pendant la
     * 				  v�rification.
     */
    public boolean estComplete() {
        // Emp�cher d'autres Thread de toucher � la liste des joueurs de cette
        // table pendant qu'on fait la v�rification (un TreeMap n'est pas
        // synchronis�)
        synchronized (lstJoueurs) {
            // Si la taille de la liste de joueurs �gale le nombre maximal de
            // joueurs alors la table est compl�te, sinon elle ne l'est pas
            return (lstJoueurs.size() == MAX_NB_PLAYERS);
        }
    }

    /**
     * Cette fonction permet de d�terminer si une partie est commenc�e ou non.
     *
     * @return boolean : true s'il y a une partie en cours
     * 					 false sinon
     */
    public boolean estCommencee() {
        return bolEstCommencee;
    }

    /**
     * Cette fonction retourne le plateau de jeu courant.
     *
     * @return Case[][] : Le plateau de jeu courant,
     * 					  null s'il n'y a pas de partie en cours
     */
    public Case[][] obtenirPlateauJeuCourant() {
        return objttPlateauJeu;
    }

    /**
     * Cette fonction retourne une case du plateau de jeu courant.
     *
     * @param playerX la coordon�e x de la position du joueur
     * @param playerY la coordon�e y de la position du joueur
     * @return Case La case du plateau de jeu correspondant � la position du
     *         joueur.
     */
    public Case getCase(int playerX, int playerY) {
        return objttPlateauJeu[playerX][playerY];
    }

    /**
     * Cette m�thode permet de remplir la liste des personnages des joueurs
     * ou les cl�s seront le id d'utilisateur du joueur et le contenu le
     * num�ro du personnage. On suppose que le joueur courant n'est pas
     * encore dans la liste.
     *
     * @throws NullPointerException : Si la liste des personnages est � nulle
     */

    public JoueurHumain[] remplirListePersonnageJoueurs() throws NullPointerException {
        JoueurHumain[] humains = new JoueurHumain[lstJoueurs.size()];
        int iter = 0;
        // Passer tous les joueurs de la table et leur envoyer un �v�nement
        for (JoueurHumain objJoueur: lstJoueurs.values()) {
            humains[iter++] = objJoueur;
        }
        return humains;
    }

    /**
     * Cette m�thode permet de pr�parer l'�v�nement de l'entr�e d'un joueur
     * dans la table courante. Cette m�thode va passer tous les joueurs
     * de la salle courante et pour ceux devant �tre avertis (tous sauf le
     * joueur courant pass� en param�tre), on va obtenir un num�ro de commande,
     * on va cr�er un InformationDestination et on va ajouter l'�v�nement dans
     * la file d'�v�nements du gestionnaire d'�v�nements. Lors de l'appel
     * de cette fonction, la liste des joueurs est synchronis�e.
     * @param colorS
     *
     * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
     * 								  vient d'entrer dans la table
     *
     * Synchronisme : Cette fonction n'est pas synchronis�e ici, mais elle l'est
     * 				  par l'appelant (entrerTable).
     */
    private void preparerEvenementJoueurEntreTable(String nomUtilisateur, int role, int cloColor) {
        // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement
        // aux joueurs qu'un joueur est entr� dans la table
        EvenementJoueurEntreTable joueurEntreTable = new EvenementJoueurEntreTable(intNoTable, nomUtilisateur, role, cloColor);

        // Passer tous les joueurs de la salle et leur envoyer un �v�nement
        for (JoueurHumain objJoueur: getObjSalle().obtenirListeJoueurs().values()) {
            // Si le nom d'utilisateur du joueur courant n'est pas celui
            // qui vient d'entrer dans la table, alors on peut envoyer un
            // �v�nement � cet utilisateur
            if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false) {
                // Obtenir un num�ro de commande pour le joueur courant, cr�er
                // un InformationDestination et l'ajouter � l'�v�nement
                joueurEntreTable.ajouterInformationDestination(
                        new InformationDestination(
                        objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                        objJoueur.obtenirProtocoleJoueur()));
            }
        }

        // Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
        objGestionnaireEvenements.ajouterEvenement(joueurEntreTable);
    }

    /**
     * Cette m�thode permet de pr�parer l'�v�nement du d�part d'un joueur
     * de la table courante. Cette m�thode va passer tous les joueurs
     * de la salle courante et pour ceux devant �tre avertis (tous sauf le
     * joueur courant pass� en param�tre), on va obtenir un num�ro de commande,
     * on va cr�er un InformationDestination et on va ajouter l'�v�nement dans
     * la file d'�v�nements du gestionnaire d'�v�nements. Lors de l'appel
     * de cette fonction, la liste des joueurs est synchronis�e.
     *
     * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
     * 								  vient de quitter la table
     *
     * Synchronisme : Cette fonction n'est pas synchronis�e ici, mais elle l'est
     * 				  par l'appelant (quitterTable).
     */
    private void preparerEvenementJoueurQuitteTable(String nomUtilisateur) {
        // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement
        // aux joueurs qu'un joueur a quitt� la table
        EvenementJoueurQuitteTable joueurQuitteTable = new EvenementJoueurQuitteTable(intNoTable, nomUtilisateur);

        // Passer tous les joueurs de la salle et leur envoyer un �v�nement
        for (JoueurHumain objJoueur: getObjSalle().obtenirListeJoueurs().values()) {
            // Si le nom d'utilisateur du joueur courant n'est pas celui
            // qui vient de quitter la table, alors on peut envoyer un
            // �v�nement � cet utilisateur
            if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false) {
                // Obtenir un num�ro de commande pour le joueur courant, cr�er
                // un InformationDestination et l'ajouter � l'�v�nement
                joueurQuitteTable.ajouterInformationDestination(
                        new InformationDestination(
                        objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                        objJoueur.obtenirProtocoleJoueur()));
            }
        }

        // Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
        objGestionnaireEvenements.ajouterEvenement(joueurQuitteTable);
    }

    /**
     * Cette m�thode permet de pr�parer l'�v�nement du d�marrage d'une partie
     * de la table courante. Cette m�thode va passer tous les joueurs
     * de la table courante et pour ceux devant �tre avertis (tous sauf le
     * joueur courant pass� en param�tre), on va obtenir un num�ro de commande,
     * on va cr�er un InformationDestination et on va ajouter l'�v�nement dans
     * la file d'�v�nements du gestionnaire d'�v�nements. Lors de l'appel
     * de cette fonction, la liste des joueurs est synchronis�e.
     * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
     * 								  vient de d�marrer la partie
     * @param int idPersonnage : Le num�ro Id du personnage choisi par le joueur
     *
     * Synchronisme : Cette fonction n'est pas synchronis�e ici, mais elle l'est
     * 				  par l'appelant (demarrerPartie).
     */
    private void preparerEvenementJoueurDemarrePartie(String nomUtilisateur, int idPersonnage) {
        // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement
        // aux joueurs qu'un joueur d�marr� une partie
        EvenementJoueurDemarrePartie joueurDemarrePartie = new EvenementJoueurDemarrePartie(nomUtilisateur, idPersonnage);

        // Passer tous les joueurs de la table et leur envoyer un �v�nement
        for (JoueurHumain objJoueur: lstJoueurs.values()) {
            // Si le nom d'utilisateur du joueur courant n'est pas celui
            // qui vient de d�marrer la partie, alors on peut envoyer un
            // �v�nement � cet utilisateur
            if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false) {
                // Obtenir un num�ro de commande pour le joueur courant, cr�er
                // un InformationDestination et l'ajouter � l'�v�nement
                joueurDemarrePartie.ajouterInformationDestination(
                        new InformationDestination(
                        objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                        objJoueur.obtenirProtocoleJoueur()));
            }
        }

        // Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
        objGestionnaireEvenements.ajouterEvenement(joueurDemarrePartie);
    }
    
    /**
     * Cette m�thode permet de pr�parer l'�v�nement du cancelation du dessin
     * choisi par joueur avant. Cette m�thode va passer tous les joueurs
     * de la table courante et pour ceux devant �tre avertis (tous sauf le
     * joueur courant pass� en param�tre), on va obtenir un num�ro de commande,
     * on va cr�er un InformationDestination et on va ajouter l'�v�nement dans
     * la file d'�v�nements du gestionnaire d'�v�nements. Lors de l'appel
     * de cette fonction, la liste des joueurs est synchronis�e.
     * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
     * 								  vient de d�marrer la partie
     * @param int idPersonnage : Le num�ro Id du personnage annuler par le joueur
     *
     * Synchronisme : Cette fonction n'est pas synchronis�e ici, mais elle l'est
     * 				  par l'appelant (playerCanceledPicture).
     */
    private void prepareEventPlayerCanceledPicture(String playerName,
			int idPersonnage) {
    	// Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement
        // aux joueurs qu'un joueur d�marr� une partie
        EventPlayerPictureCanceled canceledPicture = new EventPlayerPictureCanceled(playerName, idPersonnage);

        // Passer tous les joueurs de la table et leur envoyer un �v�nement
        for (JoueurHumain objJoueur: lstJoueurs.values()) {
            // Si le nom d'utilisateur du joueur courant n'est pas celui
            // qui vient de d�marrer la partie, alors on peut envoyer un
            // �v�nement � cet utilisateur
            if (objJoueur.obtenirNomUtilisateur().equals(playerName) == false) {
                // Obtenir un num�ro de commande pour le joueur courant, cr�er
                // un InformationDestination et l'ajouter � l'�v�nement
                canceledPicture.ajouterInformationDestination(
                        new InformationDestination(
                        objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                        objJoueur.obtenirProtocoleJoueur()));
            }
        }

        // Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
        objGestionnaireEvenements.ajouterEvenement(canceledPicture);
		
	}

   
    
    private void prepareEventPlayerSelectedNewPicture(
			String playerName, int idPersonnage) {
    	// Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement
        // aux joueurs qu'un joueur d�marr� une partie
        EventPlayerSelectedPicture selectedNewPicture = new EventPlayerSelectedPicture(playerName, idPersonnage);

        // Passer tous les joueurs de la table et leur envoyer un �v�nement
        for (JoueurHumain objJoueur: lstJoueurs.values()) {
            // Si le nom d'utilisateur du joueur courant n'est pas celui
            // qui vient de d�marrer la partie, alors on peut envoyer un
            // �v�nement � cet utilisateur
            if (objJoueur.obtenirNomUtilisateur().equals(playerName) == false) {
                // Obtenir un num�ro de commande pour le joueur courant, cr�er
                // un InformationDestination et l'ajouter � l'�v�nement
                selectedNewPicture.ajouterInformationDestination(
                        new InformationDestination(
                        objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                        objJoueur.obtenirProtocoleJoueur()));
            }
        }

        // Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
        objGestionnaireEvenements.ajouterEvenement(selectedNewPicture);
		
		
	}

    
    /**
     * Cette m�thode permet de pr�parer l'�v�nement du d�marrage de partie
     * de la table courante. Cette m�thode va passer tous les joueurs
     * de la table courante et on va obtenir un num�ro de commande, on va
     * cr�er un InformationDestination et on va ajouter l'�v�nement dans
     * la file d'�v�nements du gestionnaire d'�v�nements. Lors de l'appel
     * de cette fonction, la liste des joueurs est synchronis�e.
     * @param playersListe
     *
     * @param TreeMap : La liste contenant les positions des joueurs
     *
     * Synchronisme : Cette fonction n'est pas synchronis�e ici, mais elle l'est
     * 				  par l'appelant (demarrerPartie).
     */
    private void preparerEvenementPartieDemarree(Joueur[] playersListe) {
        // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement
        // aux joueurs de la table qu'un joueur a d�marr� une partie
        EvenementPartieDemarree partieDemarree = new EvenementPartieDemarree(this, playersListe);

        // Passer tous les joueurs de la salle et leur envoyer un �v�nement
        for (JoueurHumain objJoueur: lstJoueurs.values()) {
            // Obtenir un num�ro de commande pour le joueur courant, cr�er
            // un InformationDestination et l'ajouter � l'�v�nement de la
            // table
            partieDemarree.ajouterInformationDestination(
                    new InformationDestination(
                    objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                    objJoueur.obtenirProtocoleJoueur()));
        }

        // Ajouter les nouveaux �v�nements cr��s dans la liste d'�v�nements
        // � traiter
        objGestionnaireEvenements.ajouterEvenement(partieDemarree);
    }

    /**
     *
     * @param nomUtilisateur
     * @param nouveauPointage
     */
    public void preparerEvenementMAJPointage(String nomUtilisateur, int nouveauPointage) {
        // Cr�er un nouveal �v�nement qui va permettre d'envoyer l'�v�nment
        // aux joueurs pour signifier une modification du pointage
        EvenementMAJPointage majPointage = new EvenementMAJPointage(nomUtilisateur, nouveauPointage);

        // Passser tous les joueurs de la table et leur envoyer l'�v�nement
        // NOTE: On omet d'envoyer au joueur nomUtilisateur �tant donn�
        //       qu'il connait d�j� son pointage
        for (JoueurHumain objJoueur: lstJoueurs.values()) {
            // Si le nom d'utilisateur du joueur n'est pas nomUtilisateur, alors
            // on peut envoyer un �v�nement � cet utilisateur
            if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false) {
                // Obtenir un num�ro de commande pour le joueur courant, cr�er
                // un InformationDestination et l'ajouter � l'�v�nement
                majPointage.ajouterInformationDestination(
                        new InformationDestination(
                        objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                        objJoueur.obtenirProtocoleJoueur()));
            }
        }

        // Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
        objGestionnaireEvenements.ajouterEvenement(majPointage);
    }

    /**
     * Used to inform another players that one player is back to the game
     * We need to give them his user name and his points
     * @param nomUtilisateur le nom du joueur qui rejoint la partie
     * @param idPersonnage La cl� id du personnage du joueur qui rejoint la partie
     * @param points Le pointage du joueur qui rejoint la partie
     */
    public void preparerEvenementJoueurRejoindrePartie(JoueurHumain player) {
        // Cr�er un nouveal �v�nement qui va permettre d'envoyer l'�v�nment
        // aux joueurs pour signifier une modification du pointage
        EvenementJoueurRejoindrePartie maPartie = new EvenementJoueurRejoindrePartie(player.obtenirNomUtilisateur(),
                player.obtenirPartieCourante().obtenirIdPersonnage(), player.obtenirPartieCourante().obtenirPointage(),
                player.getRole(), player.obtenirPartieCourante().getClothesColor());

        // Passser tous les joueurs de la table et leur envoyer l'�v�nement
        // NOTE: On omet d'envoyer au joueur nomUtilisateur �tant donn�
        //       qu'il connait d�j� son etat
        for (JoueurHumain objJoueur: lstJoueurs.values()) {
            // Si le nom d'utilisateur du joueur n'est pas nomUtilisateur, alors
            // on peut envoyer un �v�nement � cet utilisateur
            if (objJoueur.equals(player) == false) {
                // Obtenir un num�ro de commande pour le joueur courant, cr�er
                // un InformationDestination et l'ajouter � l'�v�nement
                maPartie.ajouterInformationDestination(
                        new InformationDestination(
                        objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                        objJoueur.obtenirProtocoleJoueur()));
            }
        }

        // Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
        objGestionnaireEvenements.ajouterEvenement(maPartie);
    }

    public void preparerEvenementMAJArgent(String nomUtilisateur, int nouvelArgent) {
        // Cr�er un nouveal �v�nement qui va permettre d'envoyer l'�v�nment
        // aux joueurs pour signifier une modification de l'argent
        EvenementMAJArgent majArgent = new EvenementMAJArgent(nomUtilisateur, nouvelArgent);

        // Passser tous les joueurs de la table et leur envoyer l'�v�nement
        // NOTE: On omet d'envoyer au joueur nomUtilisateur �tant donn�
        //       qu'il connait d�j� son argent
        for (JoueurHumain objJoueur: lstJoueurs.values()) {
            // Si le nom d'utilisateur du joueur n'est pas nomUtilisateur, alors
            // on peut envoyer un �v�nement � cet utilisateur
            if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false) {
                // Obtenir un num�ro de commande pour le joueur courant, cr�er
                // un InformationDestination et l'ajouter � l'�v�nement
                majArgent.ajouterInformationDestination(
                        new InformationDestination(
                        objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                        objJoueur.obtenirProtocoleJoueur()));
            }
        }

        // Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
        objGestionnaireEvenements.ajouterEvenement(majArgent);
    }

    /**
     *
     * @param joueurQuiUtilise
     * @param joueurAffecte
     * @param objetUtilise
     * @param autresInformations
     */
    public void preparerEvenementUtiliserObjet(String joueurQuiUtilise, String joueurAffecte, String objetUtilise, String autresInformations) {
        // M�me chose que la fonction pr�c�dente, mais envoie plut�t les informations quant � l'utilisation d'un objet dont tous devront �tre au courant
        EvenementUtiliserObjet utiliserObjet = new EvenementUtiliserObjet(joueurQuiUtilise, joueurAffecte, objetUtilise, autresInformations);
        for (JoueurHumain objJoueur: lstJoueurs.values()) {
            utiliserObjet.ajouterInformationDestination(
                    new InformationDestination(
                    objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                    objJoueur.obtenirProtocoleJoueur()));
        }
        objGestionnaireEvenements.ajouterEvenement(utiliserObjet);
    }

    public void preparerEvenementMessageChat(String joueurQuiEnvoieLeMessage, String messageAEnvoyer) {
        // Meme chose que la fonction pr�c�dente, mais envoie plut�t un message de la part d'un joueur � tous les joueurs de la table
        EvenementMessageChat messageChat = new EvenementMessageChat(joueurQuiEnvoieLeMessage, messageAEnvoyer);
        for (JoueurHumain objJoueur: lstJoueurs.values()) {
            messageChat.ajouterInformationDestination(
                    new InformationDestination(
                    objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                    objJoueur.obtenirProtocoleJoueur()));
        }
        objGestionnaireEvenements.ajouterEvenement(messageChat);
    }

    /**
     * Method that is used to prepare event of move of the player
     * @param nomUtilisateur
     * @param collision
     * @param oldPosition
     * @param positionJoueur
     * @param nouveauPointage
     * @param nouvelArgent
     * @param bonus
     * @param objetUtilise
     */
    public void preparerEvenementJoueurDeplacePersonnage(String nomUtilisateur, String collision,
                                                         Point oldPosition, Point positionJoueur, int nouveauPointage, int nouvelArgent, int bonus, String objetUtilise) {
        // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement
        // aux joueurs qu'un joueur se deplace

        EvenementJoueurDeplacePersonnage joueurDeplacePersonnage = new EvenementJoueurDeplacePersonnage(nomUtilisateur,
                oldPosition, positionJoueur, collision, nouveauPointage, nouvelArgent, bonus);

        // Passer tous les joueurs de la table et leur envoyer un �v�nement
        for (JoueurHumain objJoueur: lstJoueurs.values()) {
            // Si le nom d'utilisateur du joueur courant n'est pas celui
            // qui vient de d�marrer la partie, alors on peut envoyer un
            // �v�nement � cet utilisateur
            if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false) {
                // Obtenir un num�ro de commande pour le joueur courant, cr�er
                // un InformationDestination et l'ajouter � l'�v�nement
                joueurDeplacePersonnage.ajouterInformationDestination(
                        new InformationDestination(
                        objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                        objJoueur.obtenirProtocoleJoueur()));
            }
        }
        // Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
        objGestionnaireEvenements.ajouterEvenement(joueurDeplacePersonnage);
    }

    /**
     *
     */
    private void preparerEvenementSynchroniser() {
        //Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement
        // aux joueurs de la table
        EvenementSynchroniserTemps synchroniser = new EvenementSynchroniserTemps(objMinuterie.obtenirTempsActuel());

        // Passer tous les joueurs de la salle et leur envoyer un �v�nement
        for (JoueurHumain objJoueur: lstJoueurs.values()) {
            // Obtenir un num�ro de commande pour le joueur courant, cr�er
            // un InformationDestination et l'ajouter � l'�v�nement de la
            // table
            synchroniser.ajouterInformationDestination(
                    new InformationDestination(
                    objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                    objJoueur.obtenirProtocoleJoueur()));
        }

        // Ajouter les nouveaux �v�nements cr��s dans la liste d'�v�nements
        // � traiter
        objGestionnaireEvenements.ajouterEvenement(synchroniser);
    }

    /**
     *
     * @param ourResults
     * @param joueurGagnant
     */
    private void preparerEvenementPartieTerminee(TreeSet<StatisticsPlayer> ourResults, String joueurGagnant) {
        // joueurGagnant r�f�re � la personne qui a atteint le WinTheGame (s'il y a lieu)

        // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement
        // aux joueurs de la table
        EvenementPartieTerminee partieTerminee = new EvenementPartieTerminee(this, ourResults, joueurGagnant);
        // Passer tous les joueurs de la salle et leur envoyer un �v�nement
        for (JoueurHumain objJoueur: lstJoueurs.values()) {
            // Obtenir un num�ro de commande pour le joueur courant, cr�er
            // un InformationDestination et l'ajouter � l'�v�nement de la
            // table
            partieTerminee.ajouterInformationDestination(
                    new InformationDestination(
                    objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                    objJoueur.obtenirProtocoleJoueur()));
        }

        // Ajouter les nouveaux �v�nements cr��s dans la liste d'�v�nements
        // � traiter
        objGestionnaireEvenements.ajouterEvenement(partieTerminee);
    }

    public void tempsEcoule() {
        arreterPartie("");
    }

    public int getObservateurMinuterieId() {
        return obtenirNoTable();
    }

    public void synchronise() {
        synchronized (lstJoueurs) {
            preparerEvenementSynchroniser();
        }
    }

    public int getObservateurSynchroniserId() {
        return obtenirNoTable();
    }

    public boolean estArretee() {
        return bolEstArretee;
    }

    public int obtenirTempsRestant() {
        if (objMinuterie == null) {
            return intTempsTotal;
        } else {
            return objMinuterie.obtenirTempsActuel();
        }

    }

    // return a percents of elapsed time
    public int getRelativeTime() {
        if (objMinuterie == null) {
            return 0;
        } else {
            return (intTempsTotal * 60 - objMinuterie.obtenirTempsActuel()) * 180 / (intTempsTotal * 60);
        }

    }

    /* Cette fonction permet de d�finir le nombre de joueurs virtuels que l'on
     * veut pour cette table
     * @param: nb -> Nouveau nombre de joueurs virtuels
     */
    public void setNombreJoueursVirtuels(int nb) {
        intNombreJoueursVirtuels = nb;
    }

    /* Cette fonction permet d'obtenir le nombre de joueurs virtuels pour
     * cette table
     */
    public int getNombreJoueursVirtuels() {
        return intNombreJoueursVirtuels;
    }

    public ArrayList<JoueurVirtuel> obtenirListeJoueursVirtuels() {
        return lstJoueursVirtuels;
    }

    /*
     * Lorsqu'un joueur est d�connect� d'une partie en cours, on appelle
     * cette fonction qui se charge de conserver les r�f�rences vers
     * les informations pour ce joueur
     */
    public void ajouterJoueurDeconnecte(JoueurHumain joueurHumain) {
        lstJoueursDeconnectes.add(joueurHumain.obtenirNomUtilisateur());
    }

    public LinkedList<String> obtenirListeJoueursDeconnectes() {
        return lstJoueursDeconnectes;
    }

    public Integer obtenirProchainIdObjet() {
        synchronized (objProchainIdObjet) {
            return objProchainIdObjet;
        }
    }

    /**
     * Used to return the current valid id for the objects in
     * Magasins(Shop) and automaticaly icrement to the new value
     * @return
     */
    public Integer getAndIncrementNewIdObject() {
        synchronized (objProchainIdObjet) {
            this.objProchainIdObjet++;
            return this.objProchainIdObjet - 1;
        }
    }

    public void setObjProchainIdObjet(Integer objProchainIdObjet) {
        synchronized (this.objProchainIdObjet) {
            this.objProchainIdObjet = objProchainIdObjet;
        }
    }

    /**
     * Aller chercher dans la liste des joueurs sur cette table
     * les ID des personnages choisi et v�rifier si le id intID est
     * d�j� choisi
     *
     * Cette fonction v�rifie dans la liste des joueurs et non dans
     * la liste des joueurs en attente
     */
    private boolean idPersonnageEstLibre(int intID) {
        synchronized (lstJoueurs) {
            // Parcourir la liste des joueurs et v�rifier si le id est libre
            for (JoueurHumain objJoueurHumain: lstJoueurs.values()) {
                if (objJoueurHumain.obtenirPartieCourante().obtenirIdPersonnage() == intID) {
                    return false;
                }
            }
        }
        // Si on se rend ici, on a parcouru tous les joueurs et on n'a pas
        // trouv� ce id de personnage, donc le id est libre
        return true;
    }

    /**
     * Aller chercher dans la liste des joueurs en attente
     * les ID des personnages choisi et v�rifier si le id intID est
     * d�j� choisi
     *
     * Cette fonction v�rifie dans la liste des joueurs en attente
     * la liste des joueurs (doit donc �tre utilis� avant que la partie commence)
     *
     * @param intID
     * @return
     */
    public boolean idPersonnageEstLibreEnAttente(int intID) {
        synchronized (lstJoueursEnAttente) {
            // Parcourir la liste des joueurs et v�rifier le id
            for (JoueurHumain objJoueurHumain: lstJoueursEnAttente.values()) {
                // V�rifier le id
                if (objJoueurHumain.obtenirPartieCourante().obtenirIdPersonnage() == intID) {
                    // D�j� utilis�
                    return false;
                }
            }
        }

        // Si on se rend ici, on a parcouru tous les joueurs et on n'a pas
        // trouv� ce id de personnage, donc le id est libre
        return true;
    }// end method

    /**
     * @param username
     * @return Humain player
     */
    public JoueurHumain obtenirJoueurHumainParSonNom(String username) {
        synchronized (lstJoueurs) {
            for (JoueurHumain j: lstJoueurs.values()) {
                if (username.equals(j.obtenirNomUtilisateur())) {
                    return j;
                }
            }
            return null;
        }
    }

    /**
     *
     * @param username
     * @return Virtual Player
     */
    public JoueurVirtuel obtenirJoueurVirtuelParSonNom(String username) {
        for (int i = 0; i < lstJoueursVirtuels.size(); i++) {
            JoueurVirtuel j = (JoueurVirtuel)lstJoueursVirtuels.get(i);
            //System.out.println(username + " compare " + j.obtenirNom());
            if (username.equals(j.obtenirNom())) {
                return j;
            }
        }
        return (JoueurVirtuel)null;
    }

    
    public Salle getObjSalle() {
        return objSalle;
    }

    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName) {
        if (tableName.trim().equals("")) {
            this.tableName = "" + this.intNoTable;
        } else {
            this.tableName = tableName;
        }
    }

    /**
     * @return the nbLines
     */
    public int getNbLines() {
        return nbLines;
    }

    /**
     * @param nbLines the nbLines to set
     */
    public void setNbLines(int nbLines) {
        this.nbLines = nbLines;
    }

    /**
     * @return the nbColumns
     */
    public int getNbColumns() {
        return nbColumns;
    }

    /**
     * @param nbColumns the nbColumns to set
     */
    public void setNbColumns(int nbColumns) {
        this.nbColumns = nbColumns;
    }

    public Point getPositionPointFinish() {
        Random objRandom = new Random();

        return lstPointsFinish.get(objRandom.nextInt(lstPointsFinish.size() - 1));
    }

    public boolean checkPositionPointsFinish(Point objPoint) {
        boolean isTrue = false;
        for (int i = 0; i < lstPointsFinish.size(); i++) {
            isTrue = objPoint.equals(lstPointsFinish.get(i));
            if (isTrue) {
                return isTrue;
            }
        }

        return isTrue;
    }

    /**
     * @return the lstPointsFinish
     */
    public ArrayList<Point> getLstPointsFinish() {
        return lstPointsFinish;
    }

    /**
     * @param lstPointsFinish the lstPointsFinish to set
     */
    public void setLstPointsFinish(ArrayList<Point> lstPointsFinish) {
        this.lstPointsFinish = lstPointsFinish;
    }

    /**
     *  set the list of colors for the user clothes
     */
    private void setColors() {
        for (int i = 1; i <= 12; i++) {
            colors.add(i);
            //System.out.println("Colors : " + i);
        }



    }// end methode

    /**
     * get one color from the list
     * it is automatically eliminated from the list
     * @return 
     */
    public int getOneColor() {
        // default color - black or white?
        int color = 0;
        synchronized (colors) {
            // Let's choose a colors among the possible ones
            if (colors != null && colors.size() > 0) {
                int intRandom = UtilitaireNombres.genererNbAleatoire(colors.size());
                color = colors.remove(intRandom); 
                

            } else {
                //objLogger.error(GestionnaireMessages.message("colors_liste_empty"));
            }
            
            return color;
        }
    }

    /**
     *
     * @param joueur
     * @param doitGenererNoCommandeRetour
     */
    public void entrerTable(JoueurHumain joueur, boolean doitGenererNoCommandeRetour) {
        //System.out.println("start table: " + System.currentTimeMillis());
        // Emp�cher d'autres thread de toucher � la liste des joueurs de
        // cette table pendant l'ajout du nouveau joueur dans cette table
        synchronized (lstJoueurs) {
            // Ajouter ce nouveau joueur dans la liste des joueurs de cette table
            lstJoueurs.put(joueur.obtenirNomUtilisateur(), joueur);
        }

        // Le joueur est maintenant entr� dans la table courante (il faut
        // cr�er un objet InformationPartie qui va pointer sur la table
        // courante)
        joueur.definirPartieCourante(new InformationPartie(objGestionnaireEvenements, joueur, this));
        // 0 - because it's first time that we fill the QuestionsBox
        // after we'll cut the level of questions by this number
        joueur.obtenirPartieCourante().getObjGestionnaireBD().remplirBoiteQuestions(0);
        
        int color = getOneColor();
        joueur.obtenirPartieCourante().setClothesColor(color);

        // Si on doit g�n�rer le num�ro de commande de retour, alors
        // on le g�n�re, sinon on ne fait rien
        if (doitGenererNoCommandeRetour == true) {
            // G�n�rer un nouveau num�ro de commande qui sera
            // retourn� au client
            joueur.obtenirProtocoleJoueur().genererNumeroReponse();
        }

        // Emp�cher d'autres thread de toucher � la liste des joueurs de
        // cette salle pendant qu'on parcourt tous les joueurs de la salle
        // pour leur envoyer un �v�nement
        synchronized (getObjSalle().obtenirListeJoueurs()) {
            // Pr�parer l'�v�nement de nouveau joueur dans la table.
            // Cette fonction va passer les joueurs et cr�er un
            // InformationDestination pour chacun et ajouter l'�v�nement
            // dans la file de gestion d'�v�nements
            preparerEvenementJoueurEntreTable(joueur.obtenirNomUtilisateur(), joueur.getRole(), color);
        }


    }

    /**
     * Return the max number of tlayers that can be on
     * the table. In general and course types of the game the player
     * number is filled with the virtuals till this constant
     * @return
     */
    public int getMaxNbPlayers() {
        return this.MAX_NB_PLAYERS;
    }

    /**
     * use one id from list of idPersos and create idPersonnage
     * idPerso is removed from the list
     * @param idDessin
     * @return the idPersonnage
     */
    public int getOneIdPersonnage(int idDessin) {
        synchronized (idPersos) {
            int idPersonnage = this.idPersos.poll();
            
            idPersonnage += 10000 + idDessin * 100;
            return idPersonnage;
        }
    }

    /**
     * if player leave the table he return the idPerso
     * that is get back to the list
     * @param idPersonnage
     */
    public void getBackOneIdPersonnage(int idPersonnage) {
        synchronized (idPersos) {
            this.idPersos.add((idPersonnage - 10000) % 100);
        }
    }

    /**
     *  the idPersos to set
     */
    public void setIdPersos() {
        for (int i = 0; i < 12; i++) {
            this.idPersos.add(i);
        }
    }

    public Regles getRegles() {
        return objRegles;
    }

    public String getGameType() {
        return gameType;
    }

    public GenerateurPartie getGameFactory() {
        return gameFactory;
    }

    /**
     * @return the objGestionnaireBD
     */
    public GestionnaireBD getObjGestionnaireBD() {
        return objGestionnaireBD;
    }

    public GestionnaireEvenements getObjGestionnaireEvenements() {
        return objGestionnaireEvenements;
    }

    public ControleurJeu getObjControleurJeu() {
        return objControleurJeu;
    }
	
}// end class

