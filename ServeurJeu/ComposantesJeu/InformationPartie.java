package ServeurJeu.ComposantesJeu;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.ComposantesJeu.Cases.CaseCouleur;
import ServeurJeu.ComposantesJeu.Joueurs.HumainPlayerBrainiacState;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;
import ServeurJeu.ComposantesJeu.Joueurs.Joueur;
import ServeurJeu.ComposantesJeu.Joueurs.HumainPlayerBananaState;
import ServeurJeu.ComposantesJeu.Objets.Objet;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.*;
import ServeurJeu.ComposantesJeu.Objets.Pieces.Piece;
import ClassesRetourFonctions.RetourVerifierReponseEtMettreAJourPlateauJeu;
import ServeurJeu.ControleurJeu;
import java.util.LinkedList;

/**
 * @author Jean-François Brind'Amour
 */
public class InformationPartie
{
    // Déclaration d'une référence vers le gestionnaire de bases de données
    private final GestionnaireBD objGestionnaireBD;
    // Déclaration d'une référence vers le gestionnaire d'evenements
    private final GestionnaireEvenements objGestionnaireEv;
    // Déclaration d'une référence vers un joueur humain correspondant à cet
    // objet d'information de partie
    private final JoueurHumain objJoueurHumain;
    // Déclaration d'une référence vers la table courante
    private final Table objTable;
    // Déclaration d'une variable qui va contenir le numéro Id du personnage
    private int intIdPersonnage;
    // Déclaration d'une variable qui va contenir le pointage de la
    // partie du joueur possèdant cet objet
    private int intPointage;
    // Combien d'argent ce joueur a-t-il?
    private int intArgent;
    // Déclaration d'une position du joueur dans le plateau de jeu
    private Point objPositionJoueur;
    // Déclaration d'un point qui va garder la position où le joueur
    // veut aller
    private Point objPositionJoueurDesiree;
    // Déclaration d'une liste de questions qui ont été répondues
    // par le joueur
    private final LinkedList<InformationQuestion> lstQuestionsRepondues;
    // Déclaration d'une variable qui va garder la question qui est
    // présentement posée au joueur. S'il n'y en n'a pas, alors il y a
    // null dans cette variable
    private Question objQuestionCourante;
    // Déclaration d'une liste d'objets utilisables ramassés par le joueur
    private final HashMap<Integer, ObjetUtilisable> lstObjetsUtilisablesRamasses;
    // Déclaration de la boîte de question personnelle au joueur possèdant
    // cet objet
    private final BoiteQuestions objBoiteQuestions;
    // object that describe and manipulate 
    // the Banana state of the player
    private HumainPlayerBananaState bananaState;
    // object that describe and manipulate 
    // the Braniac state of the player
    private HumainPlayerBrainiacState brainiacState;
    // to not get twice bonus
    // used in course ou tournament types of game
    private boolean wasOnFinish;
    // The number of cases on that user can to move. At the begining is set to 3.
    // After 3 correct answers add one unity. Not bigger than 6, but
    // in the case of Braniac is possible to have 7 cases. 
    private int moveVisibility;
    // Number for bonus in Tournament type of game
    // Bonus is given while arrived at finish line and is calculated
    // as number of rested game time(in sec)
    private int tournamentBonus;
    // the color of the clothes in the player's picture
    // user can change it in the frame 3 of the client
    // if we use default color it will remain = 0
    private String clothesColor;
    // used to count how many times the QuestionsBox is filled
    // if is filled after
    private int countFillBox;
   
    // relative time of the last change of players points
    // used for finish statistics
    private int pointsFinalTime;
    
    // used to calculate the statistics on the answers
	private int goodAnswersStats;
	private int countQuestions;
	private int countGoodAnswers;

    /**
     * Constructeur de la classe InformationPartie qui permet d'initialiser
     * les propriétés de la partie et de faire la référence vers la table.
     * @param gestionnaireEv
     * @param gestionnaireBD
     * @param joueur
     * @param tableCourante
     */
    public InformationPartie(GestionnaireEvenements gestionnaireEv, GestionnaireBD gestionnaireBD, JoueurHumain joueur, Table tableCourante) {

        // Faire la référence vers le gestionnaire de base de données
        objGestionnaireBD = gestionnaireBD;

        // Faire la référence vers le gestionnaire d'evenements
        objGestionnaireEv = gestionnaireEv;

        // Faire la référence vers le joueur humain courant
        objJoueurHumain = joueur;

        // Définir les propriétés de l'objet InformationPartie
        //intPointage = 0;

        //intIdPersonnage = 0;

        // Faire la référence vers la table courante
        objTable = tableCourante;

        // charge money from DB if is permited
        //intArgent = 0;
        if (objTable.getRegles().isBolMoneyPermit()) {
            intArgent = objGestionnaireBD.getPlayersMoney(joueur.obtenirCleJoueur());
        }

        // Au départ, le joueur est nul part
        //objPositionJoueur = null;

        // Au départ, le joueur ne veut aller nul part
        //objPositionJoueurDesiree = null;

        // Au départ, aucune question n'est posée au joueur
        //objQuestionCourante = null;

        // Créer la liste des questions qui ont été répondues
        lstQuestionsRepondues = new LinkedList<InformationQuestion>();

        // Créer la liste des objets utilisables qui ont été ramassés
        lstObjetsUtilisablesRamasses = new HashMap<Integer, ObjetUtilisable>();

        //wasOnFinish = false;

        moveVisibility = 3;
        //tournamentBonus = 0;

        // set the color to default
        clothesColor = "0";

        // Brainiac state
        this.brainiacState = new HumainPlayerBrainiacState(joueur);

        // Banana state
        this.bananaState = new HumainPlayerBananaState(joueur);

        String language = joueur.obtenirProtocoleJoueur().getLang();
        this.objBoiteQuestions = new BoiteQuestions(language, objGestionnaireBD.transmitUrl(language));
        
        //countQuestions = 0;
		//countGoodAnswers = 0;
        
    }// fin constructeur

    public void destruction() {
       
        this.brainiacState.destruction();
        this.bananaState.destruction();
        this.brainiacState = null;
        this.bananaState = null;
      /*   objGestionnaireBD = null;
        objGestionnaireEv = null;
        objJoueurHumain = null;
        objTable = null;*/
    }

    /**
     * @return the tournamentBonus
     */
    public int getTournamentBonus() {
        return tournamentBonus;
    }

    /**
     * @param tournamentBonus the tournamentBonus to set
     */
    public void setTournamentBonus(int tournamentBonus) {
        this.tournamentBonus = tournamentBonus;
    }

    /**
     * Cette fonction permet de retourner la référence vers la table courante
     * du joueur.
     *
     * @return Table : La référence vers la table de cette partie
     */
    public Table obtenirTable() {
        return objTable;
    }

    /**
     * Cette fonction permet de retourner le pointage du joueur.
     *
     * @return int : Le pointage du joueur courant
     */
    public int obtenirPointage() {
        return intPointage;
    }

    /**
     * Cette fonction permet de redéfinir le pointage du joueur.
     *
     * @param pointage Le pointage du joueur courant
     */
    public void definirPointage(int pointage) {
        intPointage = pointage;
    }

    /**
     * Cette fonction permet de retourner l'argent du joueur.
     *
     * @return int : L'argent du joueur courant
     */
    public int obtenirArgent() {
        return intArgent;
    }

    /**
     * Cette fonction permet de redéfinir l'argent du joueur.
     *
     * @param argent L'argent du joueur courant
     */
    public void definirArgent(int argent) {
        intArgent = argent;
    }

    /**
     * Cette fonction permet de retourner le Id du personnage du joueur.
     *
     * @return int : Le Id du personnage choisi par le joueur
     */
    public int obtenirIdPersonnage() {
        return intIdPersonnage;
    }

    /**
     * Cette fonction permet de redéfinir le personnage choisi par le joueur.
     *
     * @param idPersonnage Le numéro Id du personnage choisi pour cette partie
     */
    public void definirIdPersonnage(int idPersonnage) {
        intIdPersonnage = idPersonnage;
    }

    /**
     * Cette fonction permet de retourner la position du joueur dans le
     * plateau de jeu.
     *
     * @return Point : La position du joueur dans le plateau de jeu
     */
    public Point obtenirPositionJoueur() {
        return objPositionJoueur;
    }

    /**
     * Cette fonction permet de redéfinir la nouvelle position du joueur.
     *
     * @param positionJoueur La position du joueur
     */
    public void definirPositionJoueur(Point positionJoueur) {
        objPositionJoueur = positionJoueur;
    }

    /**
     * Cette fonction retourne une liste contenant les questions posées au
     * joueur avec l'information associée à la réponse donnée (valide/invalide/non-répondue
     * et le temps requis pour répondre)
     *
     * @return La liste des questions qui ont été répondues
     */
    public LinkedList<InformationQuestion> obtenirListeQuestionsRepondues() {
        return lstQuestionsRepondues;
    }

    public boolean questionDejaPosee(int cle) {
        for (InformationQuestion iq : lstQuestionsRepondues)
            if (iq.obtenirQuestionId() == cle)
                return true;
        return false;
    }
    /**
     * Cette fonction permet de retourner la question qui est présentement
     * posée au joueur.
     *
     * @return Question La question qui est présentement posée au joueur
     */
    public Question obtenirQuestionCourante() {
        return objQuestionCourante;
    }

    /**
     * Cette fonction permet de redéfinir la question présentement posée
     * au joueur.
     *
     * @param questionCourante La question qui est présentement	posée au joueur
     */
    public void definirQuestionCourante(Question questionCourante) {
        objQuestionCourante = questionCourante;
    }

    /**
     * Cette fonction détermine si le déplacement vers une certaine
     * case est permis ou non. Pour être permis, il faut que le déplacement
     * désiré soit en ligne droite, qu'il n'y ait pas de trous le séparant
     * de sa position désirée et que la distance soit acceptée comme niveau
     * de difficulté pour la salle. La distance minimale à parcourir est 1.
     *
     * @param nouvellePosition La position vers laquelle le joueur veut aller
     * @return boolean : true si le déplacement est permis false sinon
     */
    public boolean deplacementEstPermis(Point nouvellePosition) {
        boolean bolEstPermis = true;

        // Si la position de départ est la même que celle d'arrivée, alors
        // il y a une erreur, car le personnage doit faire un déplacement d'au
        // moins 1 case
        if (nouvellePosition.x == objPositionJoueur.x && nouvellePosition.y == objPositionJoueur.y) {
            bolEstPermis = false;
        }

        // Déterminer si la position désirée est en ligne droite par rapport
        // à la position actuelle
        if (bolEstPermis == true && nouvellePosition.x != objPositionJoueur.x && nouvellePosition.y != objPositionJoueur.y) {
            bolEstPermis = false;
        }

        // Si la distance parcourue dépasse le nombre de cases maximal possible, alors il y a une erreur
        // If we are in the Brainiac maximal cases = + 1
        if (this.brainiacState.isInBrainiac()) {

            if (bolEstPermis == true && ((nouvellePosition.x != objPositionJoueur.x && Math.abs(nouvellePosition.x - objPositionJoueur.x) > objTable.getRegles().obtenirDeplacementMaximal() + 1) ||
                    (nouvellePosition.y != objPositionJoueur.y && Math.abs(nouvellePosition.y - objPositionJoueur.y) > objTable.getRegles().obtenirDeplacementMaximal() + 1))) {
                bolEstPermis = false;
            }
        } else {

            if (bolEstPermis == true && ((nouvellePosition.x != objPositionJoueur.x && Math.abs(nouvellePosition.x - objPositionJoueur.x) > objTable.getRegles().obtenirDeplacementMaximal()) ||
                    (nouvellePosition.y != objPositionJoueur.y && Math.abs(nouvellePosition.y - objPositionJoueur.y) > objTable.getRegles().obtenirDeplacementMaximal()))) {
                bolEstPermis = false;
            }
        }

        // Si le déplacement est toujours permis jusqu'a maintenant, alors on
        // va vérifier qu'il n'y a pas de trous séparant le joueur de la
        // position qu'il veut aller
        if (bolEstPermis == true) {
            // Si on se déplace vers la gauche
            if (nouvellePosition.x != objPositionJoueur.x && nouvellePosition.x > objPositionJoueur.x) {
                // On commence le déplacement à la case juste à gauche de la
                // position courante
                int i = objPositionJoueur.x + 1;

                // On boucle tant qu'on n'a pas atteint la case de destination
                // et qu'on a pas eu de trous
                while (i <= nouvellePosition.x && bolEstPermis == true) {
                    // S'il n'y a aucune case à la position courante, alors on
                    // a trouvé un trou et le déplacement n'est pas possible
                    if (objTable.getCase(i, objPositionJoueur.y) == null) {
                        bolEstPermis = false;
                    }

                    i++;
                }
            } // Si on se déplace vers la droite
            else if (nouvellePosition.x != objPositionJoueur.x && nouvellePosition.x < objPositionJoueur.x) {
                // On commence le déplacement à la case juste à droite de la
                // position courante
                int i = objPositionJoueur.x - 1;

                // On boucle tant qu'on n'a pas atteint la case de destination
                // et qu'on a pas eu de trous
                while (i >= nouvellePosition.x && bolEstPermis == true) {
                    // S'il n'y a aucune case à la position courante, alors on
                    // a trouvé un trou et le déplacement n'est pas possible
                    if (objTable.obtenirPlateauJeuCourant()[i][objPositionJoueur.y] == null) {
                        bolEstPermis = false;
                    }

                    i--;
                }
            } // Si on se déplace vers le bas
            else if (nouvellePosition.y != objPositionJoueur.y && nouvellePosition.y > objPositionJoueur.y) {
                // On commence le déplacement à la case juste en bas de la
                // position courante
                int i = objPositionJoueur.y + 1;

                // On boucle tant qu'on n'a pas atteint la case de destination
                // et qu'on a pas eu de trous
                while (i <= nouvellePosition.y && bolEstPermis == true) {
                    // S'il n'y a aucune case à la position courante, alors on
                    // a trouvé un trou et le déplacement n'est pas possible
                    if (objTable.obtenirPlateauJeuCourant()[objPositionJoueur.x][i] == null) {
                        bolEstPermis = false;
                    }

                    i++;
                }
            } // Si on se déplace vers le haut
            else if (nouvellePosition.y != objPositionJoueur.y && nouvellePosition.y < objPositionJoueur.y) {
                // On commence le déplacement à la case juste en haut de la
                // position courante
                int i = objPositionJoueur.y - 1;

                // On boucle tant qu'on n'a pas atteint la case de destination
                // et qu'on a pas eu de trous
                while (i >= nouvellePosition.y && bolEstPermis == true) {
                    // S'il n'y a aucune case à la position courante, alors on
                    // a trouvé un trou et le déplacement n'est pas possible
                    if (objTable.obtenirPlateauJeuCourant()[objPositionJoueur.x][i] == null) {
                        bolEstPermis = false;
                    }

                    i--;
                }
            }
        }

        return bolEstPermis;
    } // fin méthode

    /**
     * Cette fonction permet de trouver une question selon la difficulté
     * et le type de question à poser.
     *
     * @param nouvellePosition La position où le joueur désire se déplacer
     * @param doitGenererNoCommandeRetour Permet de savoir si on doit générer un
     *        numéro de commande à retourner
     * @return La question trouvée, s'il n'y a pas eu de déplacement, alors la
     *         question retournée est null
     */
    public Question trouverQuestionAPoser(Point nouvellePosition, boolean doitGenererNoCommandeRetour) {
        int intDifficulte = 0;
        Question objQuestionTrouvee = null;

        // Si la position en x est différente de celle désirée, alors
        // c'est qu'il y a eu un déplacement sur l'axe des x
        if (objPositionJoueur.x != nouvellePosition.x) {
            intDifficulte = Math.abs(nouvellePosition.x - objPositionJoueur.x);
        } // Si la position en y est différente de celle désirée, alors
        // c'est qu'il y a eu un déplacement sur l'axe des y
        else if (objPositionJoueur.y != nouvellePosition.y) {
            intDifficulte = Math.abs(nouvellePosition.y - objPositionJoueur.y);
        }

        //System.out.println("Difficulte de la question : " + intDifficulte);   // test

        // if is under Banana effects
        if (this.bananaState.isUnderBananaEffects() && intDifficulte < 6) {
            intDifficulte++;
        }
        // if is under Brainiac effects
        if (this.brainiacState.isInBrainiac() && intDifficulte > 1) {
            intDifficulte--;
        }

        // to be sure...
        if (intDifficulte > 6) {
            intDifficulte = 6;
        }
        if (intDifficulte < 1) {
            intDifficulte = 1;
        }

        //System.out.println("Difficulte de la question : " + intDifficulte);   // test

        do {
            // find a question
            objQuestionTrouvee = trouverQuestion(intDifficulte);
            //System.out.println("question : " + intDifficulte + " " + objQuestionTrouvee);

            // S'il y a eu une question trouvée, alors on l'ajoute dans la liste
            // des questions posées et on la garde en mémoire pour pouvoir ensuite
            // traiter la réponse du joueur, on va aussi garder la position que le
            // joueur veut se déplacer
            if (objQuestionTrouvee != null) {
                lstQuestionsRepondues.add(new InformationQuestion(objQuestionTrouvee.obtenirCodeQuestion(), objTable.obtenirTempsRestant()));
                objQuestionCourante = objQuestionTrouvee;
                objPositionJoueurDesiree = nouvellePosition;

            } else if (objQuestionTrouvee == null && objBoiteQuestions.dontHaveQuestions()) {
                objGestionnaireBD.remplirBoiteQuestions(objJoueurHumain, countFillBox);
                countFillBox++;
            }

        } while (objQuestionTrouvee == null && countFillBox < 10); // must find right number for countFillBox
		
        if(objQuestionTrouvee == null)
        {
        	// en théorie on ne devrait plus entrer dans ce if
        	System.out.println( "ça va mal : aucune question" );
        }

        // Si on doit générer le numéro de commande de retour, alors
        // on le génére, sinon on ne fait rien (ça devrait toujours
        // être vrai, donc on le génére tout le temps)
        if (doitGenererNoCommandeRetour == true) {
            // Générer un nouveau numéro de commande qui sera
            // retourné au client
            objJoueurHumain.obtenirProtocoleJoueur().genererNumeroReponse();
        }
        
        if(objQuestionTrouvee != null)
           this.incrementQuestions();
        return objQuestionTrouvee;
    }// end method

    /**
     * Cette fonction essaie de piger une question du niveau de dificulté proche
     * de intDifficulte, si on y arrive pas, ça veut dire qu'il ne
     * reste plus de questions de niveau de difficulté proche
     * de intDifficulte
     *
     * @param intCategorieQuestion
     * @return la question trouver ou null si aucune question n'a pu être pigée
     */
    private Question trouverQuestion(int intDifficulte) {
        Question objQuestionTrouvee = null;
        do {
            // pour le premier on voir la catégorie et difficulté demandées
            objQuestionTrouvee = getObjBoiteQuestions().pigerQuestion(intDifficulte);
            //System.out.println("trouve1 " + objQuestionTrouvee.obtenirCodeQuestion());

            //après pour les difficultés moins grands
            int intDifficulteTemp = intDifficulte;

            while (objQuestionTrouvee == null && intDifficulteTemp > 0) {
                intDifficulteTemp--;
                objQuestionTrouvee = getObjBoiteQuestions().pigerQuestion(intDifficulteTemp);

            }// fin while

            //après pour les difficultés plus grands
            intDifficulteTemp = intDifficulte;
            while (objQuestionTrouvee == null && intDifficulteTemp < 7) {
                intDifficulteTemp++;
                objQuestionTrouvee = getObjBoiteQuestions().pigerQuestion(intDifficulteTemp);

            }// fin while

            // to not repeat questions
            if (objQuestionTrouvee != null && questionDejaPosee(objQuestionTrouvee.obtenirCodeQuestion())) {
                //objBoiteQuestions.popQuestion(objQuestionTrouvee);
                objQuestionTrouvee = null;
            }

        } while (objQuestionTrouvee == null && !objBoiteQuestions.dontHaveQuestions());

        return objQuestionTrouvee;

    }// fin méthode

    /**
     * Méthode appelée quand le joueur utilise la boule de cristal.  On trouve
     * une nouvelle question à posée plus facile.
     * @param tempsReponse le nombre de seconde écoulées depuis que la question
     *        courante à été posée.
     * @param doitGenererNoCommandeRetour Permet de savoir si on doit générer un
     *        numéro de commande à retourner
     * @return La question trouvée, s'il n'y a pas eu de déplacement, alors la
     *         question retournée est null
     */
    public Question trouverQuestionAPoserCristall(int tempsReponse, boolean doitGenererNoCommandeRetour) {
        // Déclarations de variables qui vont contenir la catégorie de question
        // à poser, la difficulté et la question à retourner
        //***************************************************************************************
        int oldQuestion = objQuestionCourante.obtenirCodeQuestion();
        int intDifficulte = objQuestionCourante.obtenirDifficulte();
        Question objQuestionTrouvee = null;
        // to be sure...
        if (intDifficulte > 6) {
            intDifficulte = 6;
        }
        if (intDifficulte < 1) {
            intDifficulte = 1;
        }
        //System.out.println("Difficulte de la question : " + intDifficulte);   // test
        do {
            // find a question
            objQuestionTrouvee = trouverQuestionCristall(intDifficulte, oldQuestion);

            // S'il y a eu une question trouvée, alors on l'ajoute dans la liste
            // des questions posées et on la garde en mémoire pour pouvoir ensuite
            // traiter la réponse du joueur, on va aussi garder la position que le
            // joueur veut se déplacer
            if (objQuestionTrouvee != null) {
                lstQuestionsRepondues.getLast().definirTempsRequis(tempsReponse);
                lstQuestionsRepondues.add(new InformationQuestion(objQuestionTrouvee.obtenirCodeQuestion(),objTable.obtenirTempsRestant()));
                objQuestionCourante = objQuestionTrouvee;
                //objBoiteQuestions.popQuestion(objQuestionTrouvee);
            } else if (objQuestionTrouvee == null && objBoiteQuestions.dontHaveQuestions()) {
                objGestionnaireBD.remplirBoiteQuestions(objJoueurHumain, countFillBox);
                countFillBox++;
            }
        } while (objQuestionTrouvee == null && countFillBox < 10);

        if (objQuestionTrouvee == null) {
            // en théorie on ne devrait plus entrer dans ce if
            System.out.println("ça va mal : aucune question");
        }

        // Si on doit générer le numéro de commande de retour, alors
        // on le génére, sinon on ne fait rien (ça devrait toujours
        // être vrai, donc on le génére tout le temps)
        if (doitGenererNoCommandeRetour == true) {
            // Générer un nouveau numéro de commande qui sera
            // retourné au client
            objJoueurHumain.obtenirProtocoleJoueur().genererNumeroReponse();
        }

        if(objQuestionTrouvee != null)
            this.incrementQuestions();
        return objQuestionTrouvee;
    }// end methode

    /**
     * Created for the case of Cristall
     * Cette fonction essaie de piger une question du niveau de dificulté proche
     * de intDifficulte, si on y arrive pas, ça veut dire qu'il ne
     * reste plus de questions de niveau de difficulté proche
     * de intDifficulte
     *
     * @param intDifficulte
     * @return la question trouver ou null si aucune question n'a pu être pigée
     */
    private Question trouverQuestionCristall(int intDifficulte, int codeOld) {

        Question objQuestionTrouvee = null;

        // to not get the same question
        do {
            // pour le premier on voir la catégorie et difficulté demandées
            objQuestionTrouvee = getObjBoiteQuestions().pigerQuestionCristall(intDifficulte, codeOld);


            //après pour les difficultés moins grands
            int intDifficulteTemp = intDifficulte;

            while (objQuestionTrouvee == null && intDifficulteTemp > 0) {
                intDifficulteTemp--;
                objQuestionTrouvee = getObjBoiteQuestions().pigerQuestionCristall(intDifficulteTemp, codeOld);

            }// fin while

            //au pire cas les difficultés plus grands
            intDifficulteTemp = intDifficulte;

            while (objQuestionTrouvee == null && intDifficulteTemp < 7) {
                intDifficulteTemp++;
                objQuestionTrouvee = getObjBoiteQuestions().pigerQuestionCristall(intDifficulteTemp, codeOld);

            }// fin while

            // to not repeat questions
            if (objQuestionTrouvee != null && questionDejaPosee(objQuestionTrouvee.obtenirCodeQuestion())) {
                //objBoiteQuestions.popQuestion(objQuestionTrouvee);
                objQuestionTrouvee = null;
            }


        } while (objQuestionTrouvee == null && !objBoiteQuestions.dontHaveQuestions());
        //System.out.println(" verification " + objQuestionTrouvee);

        return objQuestionTrouvee;

    }// fin méthode

    /**
     * Cette méthode appelle
     * {@code verifierReponseEtMettreAJourPlateauJeu(reponse,tempsReponse,this.objPositionJoueurDesiree,this.objJoueurHumain)}
     * puis définie sa question courante à la valeur null.
     * @param reponse La réponse du joueur
     * @param tempsReponse Le temps prit pour répondre à la question
     * @return Un objet contenant toutes les valeurs à retourner au client
     */
    public RetourVerifierReponseEtMettreAJourPlateauJeu verifierReponseEtMettreAJourPlateauJeu(String reponse, int tempsReponse) {

        RetourVerifierReponseEtMettreAJourPlateauJeu objRetour =
            verifierReponseEtMettreAJourPlateauJeu(reponse, tempsReponse, objPositionJoueurDesiree, objJoueurHumain);

        //getObjBoiteQuestions().popQuestion(objQuestionCourante);
        objQuestionCourante = null;

        return objRetour;
    }

    /**
     * Cette fonction met à jour le plateau de jeu si le joueur a bien répondu
     * à la question. Les objets sur la nouvelle case sont enlevés et le pointage et l'argent
     * du joueur sont mis à jour. Utilisé par les joueurs humains et les joueurs virtuels
     * @param reponse La réponse du joueur
     * @param tempsReponse Le temps prit pour répondre à la question
     * @param objPositionDesiree La nouvelle position desiree
     * @param objJoueur  Le joueur pour qui la méthode doit être exécutée
     * @return Un objet contenant toutes les valeurs à retourner au client
     *
     */
    public static RetourVerifierReponseEtMettreAJourPlateauJeu verifierReponseEtMettreAJourPlateauJeu(
            String reponse, int tempsReponse, Point objPositionDesiree, Joueur objJoueur) {

        // Déclaration de l'objet de retour
        RetourVerifierReponseEtMettreAJourPlateauJeu objRetour = null;

        int intPointageCourant;
        int intArgentCourant;
        int bonus = 0;
        Table table;
        HashMap<Integer, ObjetUtilisable> objListeObjetsUtilisablesRamasses;
        Point positionJoueur;
        Question objQuestion;
        String nomJoueur;
        boolean bolReponseEstBonne;
        boolean boolWasOnFinish = false;
        int intNouveauPointage = 0;
        int deplacementJoueur = 0;
        boolean stopTheGame = false;




        // Obtenir les divers informations à utiliser dépendamment de si
        // la fonction s'applique à un joueur humain ou un joueur virtuel
        if (objJoueur instanceof JoueurHumain) {
            InformationPartie objPartieCourante = ((JoueurHumain)objJoueur).obtenirPartieCourante();

            // Obtenir les informations du joueur humain
            intPointageCourant = objPartieCourante.obtenirPointage();
            intArgentCourant = objPartieCourante.obtenirArgent();
            bonus = objPartieCourante.getTournamentBonus();
            table = objPartieCourante.obtenirTable();
            objListeObjetsUtilisablesRamasses = objPartieCourante.obtenirListeObjets();
            positionJoueur = objPartieCourante.obtenirPositionJoueur();
            objQuestion = objPartieCourante.obtenirQuestionCourante();
            nomJoueur = ((JoueurHumain)objJoueur).obtenirNomUtilisateur();
            boolWasOnFinish = objPartieCourante.wasOnFinish;


            // Si la position en x est différente de celle désirée, alors
            // c'est qu'il y a eu un déplacement sur l'axe des x
            if (positionJoueur.x != objPositionDesiree.x) {
                deplacementJoueur = Math.abs(objPositionDesiree.x - positionJoueur.x);
            } // Si la position en y est différente de celle désirée, alors
            // c'est qu'il y a eu un déplacement sur l'axe des y
            else if (positionJoueur.y != objPositionDesiree.y) {
                deplacementJoueur = Math.abs(objPositionDesiree.y - positionJoueur.y);
            }

            if (deplacementJoueur == 1 && objPartieCourante.bananaState.isUnderBananaEffects()) {
                intNouveauPointage -= 1;
            }

            // If we're in debug mode, accept any answer
            if (ControleurJeu.modeDebug)
                bolReponseEstBonne = true;
            else
                bolReponseEstBonne = Question.reponseEstValide(reponse, objQuestion.getStringAnswer());

            InformationQuestion iq = objPartieCourante.lstQuestionsRepondues.getLast();
            iq.definirTempsRequis(tempsReponse);
            iq.definirValiditee(bolReponseEstBonne?InformationQuestion.RIGHT_ANSWER:InformationQuestion.WRONG_ANSWER);
            
            // to have statistics 
            if(bolReponseEstBonne) 
		    	 objPartieCourante.incrementGoodAnswers();


        } else {
            JoueurVirtuel objJoueurVirtuel = (JoueurVirtuel)objJoueur;

            // Obtenir les informations du joueur virtuel
            intPointageCourant = objJoueurVirtuel.obtenirPointage();
            intArgentCourant = objJoueurVirtuel.obtenirArgent();
            table = objJoueurVirtuel.obtenirTable();
            objListeObjetsUtilisablesRamasses = objJoueurVirtuel.obtenirListeObjetsRamasses();
            positionJoueur = objJoueurVirtuel.obtenirPositionJoueur();

            // Si la position en x est différente de celle désirée, alors
            // c'est qu'il y a eu un déplacement sur l'axe des x
            if (positionJoueur.x != objPositionDesiree.x) {
                deplacementJoueur = Math.abs(objPositionDesiree.x - positionJoueur.x);
            } // Si la position en y est différente de celle désirée, alors
            // c'est qu'il y a eu un déplacement sur l'axe des y
            else if (positionJoueur.y != objPositionDesiree.y) {
                deplacementJoueur = Math.abs(objPositionDesiree.y - positionJoueur.y);
            }

            if (deplacementJoueur == 1 && objJoueurVirtuel.getBananaState().isUnderBananaEffects()) {
                intNouveauPointage -= 1;
            }

            // Pas de question pour les joueurs virtuels
            objQuestion = null;
            nomJoueur = objJoueurVirtuel.obtenirNom();

            // On appelle jamais cette fonction si le joueur virtuel rate
            // la question
            bolReponseEstBonne = true;

        }

        // Le nouveau pointage est initialement le pointage courant
        intNouveauPointage += intPointageCourant;

        int intNouvelArgent = intArgentCourant;

        // Déclaration d'une référence vers l'objet ramassé
        ObjetUtilisable objObjetRamasse = null;

        // Déclaration d'une référence vers l'objet subi
        ObjetUtilisable objObjetSubi = null;

        String collision = "";

        // Déclaration d'une référence vers le magasin recontré
        Magasin objMagasinRencontre = null;

        // Si la réponse est bonne, alors on modifie le plateau de jeu
        if (bolReponseEstBonne) {

            // Calculer le nouveau pointage du joueur
            switch (deplacementJoueur) {
                case 1:
                    intNouveauPointage += 2;
                    break;
                case 2:
                    intNouveauPointage += 3;
                    break;
                case 3:
                    intNouveauPointage += 5;
                    break;
                case 4:
                    intNouveauPointage += 8;
                    break;
                case 5:
                    intNouveauPointage += 13;
                    break;
                case 6:
                    intNouveauPointage += 21;
                    break;
                case 7:
                    intNouveauPointage += 34;
                    break;
            }



            // Si la case de destination est une case de couleur, alors on
            // vérifie l'objet qu'il y a dessus et si c'est un objet utilisable,
            // alors on l'enlève et on le donne au joueur, sinon si c'est une
            // pièce on l'enlève et on met à jour le pointage du joueur, sinon
            // on ne fait rien
            Case objCaseDestination = table.getCase(objPositionDesiree.x, objPositionDesiree.y);
            if (objCaseDestination instanceof CaseCouleur) {
                // Faire la référence vers la case de couleur
                CaseCouleur objCaseCouleurDestination = (CaseCouleur)objCaseDestination;

                // S'il y a un objet sur la case, alors on va faire l'action
                // tout dépendant de l'objet (pièce, objet utilisable ou autre)
                if (objCaseCouleurDestination.obtenirObjetCase() != null) {
                    // Si l'objet est un objet utilisable, alors on l'ajoute à
                    // la liste des objets utilisables du joueur
                    if (objCaseCouleurDestination.obtenirObjetCase() instanceof ObjetUtilisable) {

                        if (objCaseCouleurDestination.obtenirObjetCase() instanceof Brainiac) {

                            // put the player on the Brainiac state
                            if (objJoueur instanceof JoueurHumain) {
                                ((JoueurHumain)objJoueur).obtenirPartieCourante().getBrainiacState().putTheOneBrainiac();
                                table.preparerEvenementUtiliserObjet(((JoueurHumain)objJoueur).obtenirNomUtilisateur(), ((JoueurHumain)objJoueur).obtenirNomUtilisateur(), "Brainiac", "");

                            } else if (objJoueur instanceof JoueurVirtuel) {
                                ((JoueurVirtuel)objJoueur).getBrainiacState().putTheOneBrainiac();
                                table.preparerEvenementUtiliserObjet(((JoueurVirtuel)objJoueur).obtenirNom(), ((JoueurVirtuel)objJoueur).obtenirNom(), "Brainiac", "");

                            }

                            // Enlever l'objet de la case du plateau de jeu
                            objCaseCouleurDestination.definirObjetCase(null);

                            // On va dire aux clients qu'il y a eu collision avec cet objet
                            collision = "Brainiac";

                        } else {
                            // Faire la référence vers l'objet utilisable
                            ObjetUtilisable objObjetUtilisable = (ObjetUtilisable)objCaseCouleurDestination.obtenirObjetCase();

                            // Garder la référence vers l'objet utilisable pour l'ajouter à l'objet de retour
                            objObjetRamasse = objObjetUtilisable;

                            // Ajouter l'objet ramassé dans la liste des objets du joueur courant
                            objListeObjetsUtilisablesRamasses.put(new Integer(objObjetUtilisable.obtenirId()), objObjetUtilisable);

                            // Enlever l'objet de la case du plateau de jeu
                            objCaseCouleurDestination.definirObjetCase(null);

                            // On va dire aux clients qu'il y a eu collision avec cet objet
                            collision = objObjetUtilisable.obtenirTypeObjet();
                        }

                    } else if (objCaseCouleurDestination.obtenirObjetCase() instanceof Piece) {

                        // Faire la référence vers la pièce
                        Piece objPiece = (Piece)objCaseCouleurDestination.obtenirObjetCase();

                        // Mettre à jour l'argent du joueur
                        intNouvelArgent += objPiece.obtenirMonnaie();

                        // Enlever la pièce de la case du plateau de jeu
                        objCaseCouleurDestination.definirObjetCase(null);

                        collision = "piece";

                        // TODO: Il faut peut-être lancer un algo qui va placer
                        // 		 les pièces sur le plateau de jeu s'il n'y en n'a
                        //		 plus

                    } else if (objCaseCouleurDestination.obtenirObjetCase() instanceof Magasin) {
                        // Définir la collision
                        collision = "magasin";

                        // Définir la référence vers le magasin rencontré
                        objMagasinRencontre = (Magasin)objCaseCouleurDestination.obtenirObjetCase();
                    }
                }

                // S'il y a un objet à subir sur la case, alors on va faire une
                // certaine action (TODO: à compléter)
                if (objCaseCouleurDestination.obtenirObjetArme() != null) {
                    // Faire la référence vers l'objet utilisable
                    ObjetUtilisable objObjetUtilisable = (ObjetUtilisable)objCaseCouleurDestination.obtenirObjetArme();

                    // Garder la référence vers l'objet utilisable à subir
                    objObjetSubi = objObjetUtilisable;

                    //TODO: Faire une certaine action au joueur

                    // Enlever l'objet subi de la case
                    objCaseCouleurDestination.definirObjetArme(null);
                }

                //***********************************
                //for gametype tourmnament - bonus for finish line
                if (table.getGameType().equals("Tournament") || table.getGameType().equals("Course")) {
                    int tracks = table.getRegles().getNbTracks();
                    Point objPoint = new Point(table.getNbLines() - 1, table.getNbColumns() - 1);
                    Point objPointFinish = new Point();

                    // On vérifie d'abord si le joueur a atteint le WinTheGame;
                    boolean isOnThePointsOfFinish = false;


                    if (objJoueur instanceof JoueurHumain) {

                        for (int i = 0; i < tracks; i++) {
                            objPointFinish.setLocation(objPoint.x, objPoint.y - i);
                            if (objPositionDesiree.equals(objPointFinish)) {
                                isOnThePointsOfFinish = true;
                            }
                        }


                        if (isOnThePointsOfFinish && !boolWasOnFinish && table.getGameType().equals("Tournament")) {
                            ((JoueurHumain)objJoueur).obtenirPartieCourante().wasOnFinish = true;
                            bonus = table.obtenirTempsRestant();
                            intNouveauPointage += bonus;
                        } else if (isOnThePointsOfFinish && !boolWasOnFinish && table.getGameType().equals("Course")) {
                            ((JoueurHumain)objJoueur).obtenirPartieCourante().wasOnFinish = true;
                            bonus = table.obtenirTempsRestant();
                            intNouveauPointage += bonus;
                            // if all the humains is on the finish line we stop the game
                            if (table.isAllTheHumainsOnTheFinish((JoueurHumain)objJoueur)) {
                                stopTheGame = true;
                            }
                        }
                    } else if (objJoueur instanceof JoueurVirtuel) {
                        boolWasOnFinish = ((JoueurVirtuel)objJoueur).isPlayerNotArrivedOnce();
                        for (int i = 0; i < tracks; i++) {
                            objPointFinish.setLocation(objPoint.x, objPoint.y - i);
                            if (objPositionDesiree.equals(objPointFinish)) {
                                isOnThePointsOfFinish = true;
                            }
                        }

                        if (isOnThePointsOfFinish && boolWasOnFinish) {
                            ((JoueurVirtuel)objJoueur).setPlayerNotArrivedOnce(false);
                            bonus = table.obtenirTempsRestant();
                            intNouveauPointage += bonus;
                        }
                    }

                }
                //************************************  end bonus
            }



            // Créer l'objet de retour
            objRetour = new RetourVerifierReponseEtMettreAJourPlateauJeu(bolReponseEstBonne, intNouveauPointage, intNouvelArgent, bonus);
            objRetour.definirObjetRamasse(objObjetRamasse);
            objRetour.definirObjetSubi(objObjetSubi);
            objRetour.definirNouvellePosition(objPositionDesiree);
            objRetour.definirCollision(collision);
            objRetour.definirMagasin(objMagasinRencontre);

            synchronized (table.obtenirListeJoueurs()) {
                // Préparer l'événement de deplacement de personnage.
                // Cette fonction va passer les joueurs et créer un
                // InformationDestination pour chacun et ajouter l'événement
                // dans la file de gestion d'événements
                table.preparerEvenementJoueurDeplacePersonnage(nomJoueur, collision, positionJoueur, objPositionDesiree, intNouveauPointage, intNouvelArgent, bonus, "");

            }

            // Modifier la position, le pointage et l'argent et moveVisibility
            if (objJoueur instanceof JoueurHumain) {
                InformationPartie infoPartie = ((JoueurHumain)objJoueur).obtenirPartieCourante();
                infoPartie.definirPositionJoueur(objPositionDesiree);
                infoPartie.definirPointage(intNouveauPointage);
                infoPartie.setPointsFinalTime(table.obtenirTempsRestant());
                infoPartie.definirArgent(intNouvelArgent);
                infoPartie.setTournamentBonus(bonus);
                infoPartie.setMoveVisibility(infoPartie.getMoveVisibility() + 1);

            } else if (objJoueur instanceof JoueurVirtuel) {
                ((JoueurVirtuel)objJoueur).definirPositionJoueurVirtuel(objPositionDesiree);
                ((JoueurVirtuel)objJoueur).definirPointage(intNouveauPointage);
                ((JoueurVirtuel)objJoueur).setPointsFinalTime(table.obtenirTempsRestant());
                ((JoueurVirtuel)objJoueur).definirArgent(intNouvelArgent);
            }
        } else { //bolReponseEstBonne == false
            //((JoueurHumain)objJoueur).obtenirPartieCourante().setRunningAnswers(0);
            ((JoueurHumain)objJoueur).obtenirPartieCourante().setMoveVisibility(((JoueurHumain)objJoueur).obtenirPartieCourante().getMoveVisibility() - 1);

            // Créer l'objet de retour
            objRetour = new RetourVerifierReponseEtMettreAJourPlateauJeu(bolReponseEstBonne, intNouveauPointage, intNouvelArgent, bonus);

            // La question sera nulle pour les joueurs virtuels
            if (objQuestion != null) {
                objRetour.definirExplications(objQuestion.obtenirURLExplication());
            }
        }

        if (stopTheGame) {
            table.arreterPartie(""); //to do - cleaner end of game!!!!
        }
        return objRetour;

    }// end method

    /**
     * This method is used to cancel the question.
     * The first use is for Banana - to cancel question if banana is applied
     * when used read the question.
     *
     */
    public void cancelPosedQuestion() {
        //getObjBoiteQuestions().popQuestion(objQuestionCourante);
        lstQuestionsRepondues.removeLast();
        objQuestionCourante = null;
    }

    /*
     * Retourne une référence vers la liste des objets ramassés
     */
    public HashMap<Integer, ObjetUtilisable> obtenirListeObjets() {
        return lstObjetsUtilisablesRamasses;
    }

    public void ajouterObjetUtilisableListe(ObjetUtilisable objObjetUtilisable) {
        lstObjetsUtilisablesRamasses.put(new Integer(objObjetUtilisable.obtenirId()), objObjetUtilisable);
    }

    /*
     * Aller chercher une référence vers un objet de la liste des objets selon
     * son id
     */
    public ObjetUtilisable obtenirObjetUtilisable(int intObjetId) {
           	
    	for(ObjetUtilisable objObjet:lstObjetsUtilisablesRamasses.values())
    	{
    		if (objObjet instanceof ObjetUtilisable && objObjet.obtenirId() == intObjetId)
    		{
    		   return objObjet;
            }
    	}
        return null;
    }

    /*
     * Détermine si le joueur possède un certain objet, permet
     * de valider l'information envoyé par le client lorsqu'il utiliser l'objet
     */
    public boolean joueurPossedeObjet(int id) {
    	
    	for(ObjetUtilisable objObjet:lstObjetsUtilisablesRamasses.values())
    	{
    		if (objObjet instanceof ObjetUtilisable && objObjet.obtenirId() == id)
    		{
    		   return true;
            }
    	}
        return false;
    }

    public GestionnaireEvenements obtenirGestionnaireEvenements() {
        return objGestionnaireEv;
    }

    public void enleverObjet(int intIdObjet, String strTypeObjet) {
        lstObjetsUtilisablesRamasses.remove(intIdObjet);
    }

    public Objet obtenirObjetCaseCourante() {
        // L'objet à retourné
        Objet objObjet = null;

        Case objCaseJoueur = objTable.getCase(objPositionJoueur.x, objPositionJoueur.y);

        // Si c'est une case couleur, retourner l'objet, sinon on va retourner null
        if (objCaseJoueur instanceof CaseCouleur) {
            objObjet = ((CaseCouleur)objCaseJoueur).obtenirObjetCase();
        }

        return objObjet;

    }

    public Point obtenirPositionJoueurDesiree() {
        return objPositionJoueurDesiree;
    }

    public GestionnaireBD obtenirGestionnaireBD() {
        return objGestionnaireBD;
    }

    /**
     * @return the bananaState
     */
    public HumainPlayerBananaState getBananaState() {
        return bananaState;
    }

    public int obtenirDistanceAuFinish() {
        Point objPoint = objTable.getPositionPointFinish();
        return Math.abs(objPositionJoueur.x - objPoint.x) + Math.abs(objPositionJoueur.y - objPoint.y);
    }

    /*
    public void setObjBoiteQuestions(BoiteQuestions objBoiteQuestions) {
    this.objBoiteQuestions = objBoiteQuestions;
    }*/
    public BoiteQuestions getObjBoiteQuestions() {
        return objBoiteQuestions;
    }

    /**
     * @return the moveVisibility
     */
    public int getMoveVisibility() {
        return moveVisibility;
    }

    /**
     * @param moveV the moveVisibility to set
     */
    public void setMoveVisibility(int moveV) {
        this.moveVisibility = moveV;

        if (this.moveVisibility > 7 && this.brainiacState.isInBrainiac()) {
            this.moveVisibility = 7;
        } else if (this.moveVisibility > 6 && this.brainiacState.isInBrainiac() == false) {
            this.moveVisibility = 6;
        } else if (this.moveVisibility < 1) {
            this.moveVisibility = 1;
        }
    }

    public void setClothesColor(String string) {
        this.clothesColor = string;
    }

    public String getClothesColor() {
        return clothesColor;
    }

    /**
     * @return the brainiacState
     */
    public HumainPlayerBrainiacState getBrainiacState() {
        return brainiacState;
    }

	/**
	 * @param goodAnswersStats the goodAnswersStats to set
	 */
	public void setGoodAnswersStats(int goodAnswersStats) {
		this.goodAnswersStats = goodAnswersStats;
	}

	/**
	 * @return the goodAnswersStats
	 */
	public int getGoodAnswersStats() {
		return goodAnswersStats;
	}

	public void incrementQuestions() {
		this.countQuestions++;
	}
	
	public void incrementGoodAnswers() {
		this.countGoodAnswers++;
		//System.out.println("goodAnswers : " + this.countGoodAnswers);
	}
	
	public int getCountQuestions() {
		return countQuestions;
	}

	public int getCountGoodAnswers() {
		return countGoodAnswers;
	}

	/**
     * @param pointsFinalTime the pointsFinalTime to set
     */
    public void setPointsFinalTime(int pointsFinalTime) {
        this.pointsFinalTime = pointsFinalTime;
    }

    /**
     * @return the pointsFinalTime
     */
    public int getPointsFinalTime() {
        return pointsFinalTime;
    }
} // end class

