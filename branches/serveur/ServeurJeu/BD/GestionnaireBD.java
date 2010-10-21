package ServeurJeu.BD;

import java.sql.*;

import org.apache.log4j.Logger;
import Enumerations.Visibilite;
import ServeurJeu.ComposantesJeu.BoiteQuestions;
import ServeurJeu.ComposantesJeu.Language;
import ServeurJeu.ComposantesJeu.Salle;
import ServeurJeu.ComposantesJeu.Question;
import ServeurJeu.ControleurJeu;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesCaseSpeciale;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesMagasin;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesObjetUtilisable;
import ServeurJeu.Configuration.GestionnaireConfiguration;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.text.SimpleDateFormat;
import ServeurJeu.Configuration.GestionnaireMessages;
import java.util.Set;

/**
 * @author Jean-François Brind'Amour
 * 
 * last changes Oloieri Lilian 11.05.2010
 */
public class GestionnaireBD {
    public static final SimpleDateFormat mejFormatDate = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat mejFormatHeure = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat mejFormatDateHeure = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // Déclaration d'une référence vers le contrôleur de jeu
    private final ControleurJeu objControleurJeu;
    // Objet Connection nécessaire pour le contact avec le serveur MySQL
    private Connection connexion;
    // Objet Statement nécessaire pour envoyer une requète au serveur MySQL
    private Statement requete;
    static private Logger objLogger = Logger.getLogger(GestionnaireBD.class);
    private final Object DB_LOCK = new Object();

    /**
     * Constructeur de la classe GestionnaireBD qui permet de garder la
     * référence vers le contrôleur de jeu
     * @param controleur le controleur de jeu du serveur {@code Maitre}.
     */
    public GestionnaireBD(ControleurJeu controleur) {
        super();

        GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();

        // Garder la référence vers le contrôleur de jeu
        objControleurJeu = controleur;

        //Création du driver JDBC
        try {
            String driver = config.obtenirString("gestionnairebd.jdbc-driver");
            Class.forName(driver);
        } catch (Exception e) {
            // Une erreur est survenue lors de l'instanciation du pilote
            objLogger.error(GestionnaireMessages.message("bd.erreur_creer_pilote1"));
            objLogger.error(GestionnaireMessages.message("bd.erreur_creer_pilote2"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
            return;
        }
        connexionDB();
    }

    /**
     * Cette fonction permet d'initialiser une connexion avec le serveur MySQL
     * et de créer un objet requète
     */
    public void connexionDB() {
        GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();

        String hote = config.obtenirString("gestionnairebd.hote");
        String utilisateur = config.obtenirString("gestionnairebd.utilisateur");
        String motDePasse = config.obtenirString("gestionnairebd.mot-de-passe");

        // établissement de la connexion avec la base de données
        try {
            connexion = DriverManager.getConnection(hote, utilisateur, motDePasse);
        } catch (SQLException e) {
            // Une erreur est survenue lors de la connexion ˆ la base de données
            objLogger.error(GestionnaireMessages.message("bd.erreur_connexion"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
            return;
        }

        // Création de l'objet "requète"
        try {
            requete = connexion.createStatement();
        } catch (SQLException e) {
            // Une erreur est survenue lors de la création d'une requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_creer_requete"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
            return;
        }

    }

    /**
     * Cette fonction permet de chercher dans la BD si le joueur dont le nom    ***
     * d'utilisateur et le mot de passe sont passés en paramètres existe.
     *
     * @param String nomUtilisateur : Le nom d'utilisateur du joueur
     * @param String motDePasse : Le mot de passe du joueur
     * @return true  : si le joueur existe et que son mot de passe est correct
     * 		   false : si le joueur n'existe pas ou que son mot de passe n'est
     * 				   pas correct
     */
    public boolean joueurExiste(String nomUtilisateur, String motDePasse) {

        GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
        String codeErreur = config.obtenirString("gestionnairebd.code_erreur_inactivite");

        int count = 0;	//compteur du nombre d'essai de la requète

        //boucler la requète jusqu'à 5 fois si la connexion à MySQL
        //a été interrompu du à un manque d'activité de la connexion
        while (count < 5) {
            try {
                if (count != 0) {
                    connexionDB();
                }
                synchronized (DB_LOCK) {
                    ResultSet rs = requete.executeQuery("SELECT * FROM user WHERE username = '" + nomUtilisateur + "' AND password = '" + motDePasse + "';");
                    return rs.next();
                }
            } catch (SQLException e) {
                //on vérifie l'état de l'exception
                //si l'état est égal au codeErreur on peut réesayer la connexion
                if (e.getSQLState().equals(codeErreur)) {
                    count++;
                } else {
                    // Une erreur est survenue lors de l'exécution de la requète
                    objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
                    objLogger.error(GestionnaireMessages.message("bd.trace"));
                    objLogger.error(e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * This methode fill columns with the actual date for the player with the blocked account
     */
    public void controlPlayerAccount() {

        //  SQL for update
        String strSQL = "UPDATE user SET last_access_date = CURDATE(), last_access_time = CURTIME() where last_access_date LIKE '1111-01-01' OR last_access_time LIKE '55:55:55';";

        try {

            synchronized (DB_LOCK) {
                requete.executeUpdate(strSQL);
            }
        } catch (Exception e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_ajout_infos_update_user_control_account") + e.getMessage());
        }


    }//end methode

    /**
     * Cette fonction permet de chercher dans la BD le joueur et de remplir  ***
     * les champs restants du joueur.
     *
     * @param JoueurHumain joueur : Le joueur duquel il faut trouver les
     * 								informations et les définir dans l'objet
     */
    public void remplirInformationsJoueur(JoueurHumain joueur) {
        int cle = 0;
        int role = 0;
        int niveau = 1; // default level is 1 - generic level
        String langue;

        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT user.user_id,last_name,name,role_id, level_id, short_name FROM user,language " +
                        " WHERE user.language_id=language.language_id AND username = '" + joueur.obtenirNomUtilisateur() +
                        "';"); //
                if (rs.next()) {

                    String prenom = rs.getString("last_name");
                    String nom = rs.getString("name");
                    cle = rs.getInt("user_id");
                    role = rs.getInt("role_id");
                    niveau = rs.getInt("level_id");
                    langue = rs.getString("short_name");

                    joueur.definirPrenom(prenom);
                    joueur.definirNomFamille(nom);
                    joueur.definirCleJoueur(cle);
                    joueur.definirLangue(langue);
                    joueur.setRole(role);
                    joueur.definirCleNiveau(niveau);
                }
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_remplir_info_joueur"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }


        fillConnectedUser(cle);

    }//end methode

    /**
     * this fonction fill the fields in DB (user.last_access_time,lasr_access_time)
     * with incorrect information to indicate that user is connected
     *
     */
    public void fillConnectedUser(int userId) {


        //  SQL for update
        String strSQL = "UPDATE user SET last_access_date = '1111-01-01', last_access_time = '55:55:55' where user_id = " + userId + ";";

        try {

            synchronized (DB_LOCK) {
                requete.executeUpdate(strSQL);
            }
        } catch (Exception e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_ajout_infos_update_user_connected") + e.getMessage());
        }


    }// end methode

    /**
     * this fonction fill the fields in DB (user.last_access_time,lasr_access_time)
     * with the current date at the end of game
     */
    public void fillEndDate(int userId) {


        //  SQL for update
        String strSQL = "UPDATE user SET last_access_date = CURDATE(), last_access_time = CURTIME() where user_id = " + userId + ";";

        try {

            synchronized (DB_LOCK) {
                requete.executeUpdate(strSQL);
            }
        } catch (Exception e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_ajout_infos_update_user_game_endtime") + e.getMessage());
        }


    }// end methode

    /**
     * La fonction rempli la boiteQuestions avec des questions que correspond
     * a niveaux scolaires du joueur
     * This function fills a Question box with the questions of player's level 
     * for each category and player's lang 
     */
    public void remplirBoiteQuestions(JoueurHumain objJoueurHumain, int countFillQuestions) {
        //System.out.println("start boite: " + System.currentTimeMillis());
        // Pour tenir compte de la langue
        int cleLang = 1;
        BoiteQuestions boite = objJoueurHumain.obtenirPartieCourante().getObjBoiteQuestions();
        String URL = boite.obtenirLangue().getURLQuestionsAnswers();
        Language language = boite.obtenirLangue();
        String langue = language.getLanguage();
        if (langue.equalsIgnoreCase("fr")) {
            cleLang = 1;
        } else if (langue.equalsIgnoreCase("en")) {
            cleLang = 2;
        }

        // pour keywords des questions on utilise room's keywords 
        //Integer[] key = player.obtenirSalleCourante().getKeywords();

        // to not fill the Box with the same questions
        int niveau = objJoueurHumain.obtenirCleNiveau() - countFillQuestions;
        // it's little risk for that, but to be sure....
        if (niveau < 0) {
            niveau = objJoueurHumain.obtenirCleNiveau() + 1;
        }
        int room_id = objJoueurHumain.obtenirSalleCourante().getRoomId();


        String strRequeteSQL = "SELECT  question.answer_type_id, answer.is_right,question.question_id," +
                " question_info.question_flash_file, question_info.feedback_flash_file, question_level.value" +
                " FROM question_info, question_level, question, answer " +
                " WHERE  question.question_id = question_level.question_id " +
                " AND question.question_id = question_info.question_id " +
                " AND question.question_id = answer.question_id " +
                " AND question_info.language_id = " + cleLang +
                " and question_level.level_id = " + niveau +
                " AND question.question_id IN (SELECT question.question_id FROM question, questions_keywords " +
                " WHERE question.question_id = questions_keywords.question_id AND questions_keywords.keyword_id IN (SELECT rooms_keywords.keyword_id FROM rooms_keywords WHERE room_id = " + room_id + ")) " +
                " AND question.answer_type_id IN (1,4,5) " +
                " AND question_info.is_valid = 1 " +
                " and question_level.value > 0 " +
                " and question_info.question_flash_file is not NULL " +
                " and question_info.feedback_flash_file is not NULL ";

        remplirBoiteQuestionsMC(boite, strRequeteSQL, URL);

        String strRequeteSQL_SA = "SELECT DISTINCT a.answer_latex, qi.question_id, qi.question_flash_file, qi.feedback_flash_file, ql.value " +
                "FROM question q, question_info qi, question_level ql, answer_info a, questions_keywords " +
                "where  q.question_id = ql.question_id " +
                " AND q.question_id = qi.question_id " +
                " AND q.question_id = a.question_id " +
                " AND q.question_id = questions_keywords.question_id " +
                " AND questions_keywords.keyword_id IN (SELECT keyword_id FROM rooms_keywords WHERE room_id = " + room_id +
                ") and q.answer_type_id = 3 " +
                " AND qi.language_id = " + cleLang +
                " and ql.level_id = " + niveau +
                " and ql.value > 0 " +
                " and qi.is_valid = 1 " +
                " and qi.question_flash_file is not NULL" +
                " and qi.feedback_flash_file is not NULL";

        remplirBoiteQuestionsSA(boite, strRequeteSQL_SA, URL);

        String strRequeteSQL_TF = "SELECT DISTINCT a.is_right,qi.question_id, qi.question_flash_file, qi.feedback_flash_file, ql.value " +
                " FROM question q, question_info qi, question_level ql, answer a, questions_keywords " +
                "where  q.question_id = ql.question_id " +
                " AND q.question_id = qi.question_id " +
                " AND q.question_id = a.question_id " +
                " AND q.question_id = questions_keywords.question_id " +
                " AND questions_keywords.keyword_id IN (SELECT keyword_id FROM rooms_keywords WHERE room_id = " + room_id +
                ") and q.answer_type_id = 2 " +
                " AND qi.language_id = " + cleLang +
                " and ql.level_id = " + niveau +
                " and ql.value > 0 " +
                " and qi.is_valid = 1 " +
                " and qi.question_flash_file is not NULL" +
                " and qi.feedback_flash_file is not NULL";

        remplirBoiteQuestionsTF(boite, strRequeteSQL_TF, URL);

        //System.out.println("end boite: " + System.currentTimeMillis());
    }// fin méthode

    // This function follows one of the two previous functions. It queries the database and
    // does the actual filling of the question box with questions of type MULTIPLE_CHOICE.
    private void remplirBoiteQuestionsMC(BoiteQuestions boiteQuestions, String strRequeteSQL, String URL) {
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery(strRequeteSQL);
                rs.setFetchSize(5);
                //int countQuestionId = 0;
                int codeQuestionTemp = 0;
                int countReponse = 0;
                while (rs.next()) {

                    int codeQuestion = rs.getInt("question_id");
                    if (codeQuestionTemp != codeQuestion) {
                        //countQuestionId = 0;
                        countReponse = 0;
                    }
                    int condition = rs.getInt("is_right");
                    //countQuestionId++;
                    countReponse++;
                    if (condition == 1) {
                        int typeQuestion = rs.getInt("answer_type_id");
                        //int keyword_id1 = rs.getInt( "keyword_id1" );
                        //int keyword_id2 = rs.getInt( "keyword_id2" );
                        String question = rs.getString("question_flash_file");
                        String explication = rs.getString("feedback_flash_file");
                        int difficulte = rs.getInt("value");
                        String reponse = "" + countReponse;

                        System.out.println("MC : question "  + codeQuestion + " " + reponse + " "+ difficulte );

                        // System.out.println(URL+explication);
                        //System.out.println("MC1: " + System.currentTimeMillis());
                        boiteQuestions.ajouterQuestion(new Question(codeQuestion, typeQuestion, difficulte, URL + question, reponse, URL + explication));
                        //System.out.println("MC2: " + System.currentTimeMillis());
                    }
                    codeQuestionTemp = codeQuestion;
                }
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        } catch (RuntimeException e) {
            //Une erreur est survenue lors de la recherche de la prochaine question
            objLogger.error(GestionnaireMessages.message("bd.erreur_prochaine_question_MC"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
    }// fin méthode

    // This function follows one of the two previous functions. It queries the database and
    // does the actual filling of the question box with questions of type SHORT_ANSWER.
    private void remplirBoiteQuestionsSA(BoiteQuestions boiteQuestions, String strRequeteSQL, String URL) {
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery(strRequeteSQL);
                rs.setFetchSize(5);
                while (rs.next()) {
                    int codeQuestion = rs.getInt("question_id");
                    //int keyword_id1 = rs.getInt( "keyword_id1" );
                    //int keyword_id2 = rs.getInt( "keyword_id2" );
                    int typeQuestion = 3;//rs.getString( "tag" );
                    String question = rs.getString("question_flash_file");
                    String reponse = rs.getString("answer_latex");
                    String explication = rs.getString("feedback_flash_file");
                    int difficulte = rs.getInt("value");

                    System.out.println("SA : question " + codeQuestion + " " + reponse + " " + difficulte);

                    //String URL = boiteQuestions.obtenirLangue().getURLQuestionsAnswers();
                    // System.out.println(URL+explication);
                    // System.out.println("SA1: " + System.currentTimeMillis());
                    boiteQuestions.ajouterQuestion(new Question(codeQuestion, typeQuestion, difficulte, URL + question, reponse, URL + explication));
                    //System.out.println("SA2: " + System.currentTimeMillis());
                }
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        } catch (RuntimeException e) {
            //Une erreur est survenue lors de la recherche de la prochaine question
            objLogger.error(GestionnaireMessages.message("bd.erreur_prochaine_question_SA"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
    }// fin méthode

    // This function follows one of the two previous functions. It queries the database and
    // does the actual filling of the question box with questions of type TRUE_OR_FALSE.
    private void remplirBoiteQuestionsTF(BoiteQuestions boiteQuestions, String strRequeteSQL, String URL) {
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery(strRequeteSQL);
                rs.setFetchSize(5);
                while (rs.next()) {
                    int codeQuestion = rs.getInt("question_id");
                    //int keyword_id1 = rs.getInt( "keyword_id1" );
                    //int keyword_id2 = rs.getInt( "keyword_id2" );
                    int typeQuestion = 2;   //rs.getString( "tag" );
                    String question = rs.getString("question_flash_file");
                    String reponse = rs.getString("is_right");
                    String explication = rs.getString("feedback_flash_file");
                    int difficulte = rs.getInt("value");

                    System.out.println("TF : question " + codeQuestion + " " + reponse + " " + difficulte);

                    //String URL = boiteQuestions.obtenirLangue().getURLQuestionsAnswers();
                    // System.out.println(URL+explication);
                    // System.out.println("TF1: " + System.currentTimeMillis());
                    boiteQuestions.ajouterQuestion(new Question(codeQuestion, typeQuestion, difficulte, URL + question, reponse, URL + explication));
                    //System.out.println("TF2: " + System.currentTimeMillis());
                }
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        } catch (RuntimeException e) {
            //Une erreur est survenue lors de la recherche de la prochaine question
            objLogger.error(GestionnaireMessages.message("bd.erreur_prochaine_question_TF"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
    }// fin méthode

    /** This function queries the DB to find the player's musical preferences  ***
     * and returns a Vector containing URLs of MP3s the player might like
     * @param player
     * @return
     */
    public ArrayList<Object> obtenirListeURLsMusique(JoueurHumain player) {
        ArrayList<Object> liste = new ArrayList<Object>();

        String URLMusique = GestionnaireConfiguration.obtenirInstance().obtenirString("musique.url");
        String strRequeteSQL = "SELECT music_file.filename FROM music_file  WHERE  music_file.level_id = ";
        // we use levels[0] - because all levels has the same value
        strRequeteSQL += "(Select user.level_id from user where user_id = ";
        strRequeteSQL += player.obtenirCleJoueur();
        strRequeteSQL += ");";

        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery(strRequeteSQL);
                while (rs.next()) {
                    liste.add(URLMusique + rs.getString("filename"));
                }
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        } catch (RuntimeException e) {
            // Ce n'est pas le bon message d'erreur mais ce n'est pas grave
            objLogger.error(GestionnaireMessages.message("bd.error_music"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
        return liste;
    }

    // This method updates a player's information in the DB  ***
    public void mettreAJourJoueur(JoueurHumain joueur, int tempsTotal) {
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT number_of_completed_game, best_score, total_time_played FROM user WHERE username = '" + joueur.obtenirNomUtilisateur() + "';");
                if (rs.next()) {
                    int partiesCompletes = rs.getInt("number_of_completed_game") + 1;
                    int meilleurPointage = rs.getInt("best_score");
                    int pointageActuel = joueur.obtenirPartieCourante().obtenirPointage();
                    if (meilleurPointage < pointageActuel) {
                        meilleurPointage = pointageActuel;
                    }

                    int tempsPartie = tempsTotal + rs.getInt("total_time_played");

                    //mise-a-jour
                    requete.executeUpdate("UPDATE user SET number_of_completed_game =" + partiesCompletes + ",best_score =" + meilleurPointage + ",total_time_played =" + tempsPartie + " WHERE username = '" + joueur.obtenirNomUtilisateur() + "';");
                }
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cette méthode permet de fermer la connexion de base de données qui
     * est ouverte.
     */
    public void arreterGestionnaireBD() {
        try {
            connexion.close();
        } catch (SQLException e) {
            // Une erreur est survenue lors de la fermeture de la connexion
            objLogger.error(GestionnaireMessages.message("bd.erreur_fermeture_conn"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /* Cette fonction permet d'ajouter les information sur une partie dans ***
     * la base de données dans la table partie.
     *
     * Retour: la clé de partie qui servira pour la table partieJoueur
     */
    public int ajouterInfosPartieTerminee(Date dateDebut, int dureePartie) {
        String strDate = mejFormatDate.format(dateDebut);
        String strHeure = mejFormatHeure.format(dateDebut);

        // Création du SQL pour l'ajout
        String strSQL = "INSERT INTO game(date, hour, duration) VALUES ('" +
                strDate + "','" + strHeure + "'," + dureePartie + ")";

        try {

            synchronized (DB_LOCK) {

                // Ajouter l'information pour cette partie
                requete.executeUpdate(strSQL, Statement.RETURN_GENERATED_KEYS);

                // Aller chercher la clé de partie qu'on vient d'ajouter
                ResultSet rs = requete.getGeneratedKeys();

                // On retourne la clé de partie
                rs.next();
                return rs.getInt("GENERATED_KEY");
            }
        } catch (Exception e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_ajout_infos") + e.getMessage());
        }

        // Au cas où il y aurait erreur, on retourne -1
        return -1;
    }

    /**
     * Cette fonction permet d'ajouter les informations sur une partie pour  ***
     * un joueur dans la table partieJoueur;
     *
     */
    public void ajouterInfosJoueurPartieTerminee(int clePartie, JoueurHumain joueur, boolean gagner) {
        int intGagner = 0;
        if (gagner == true) {
            intGagner = 1;
        }

        int cleJoueur = joueur.obtenirCleJoueur();
        int pointage = joueur.obtenirPartieCourante().obtenirPointage();
        int room_id = 0;
        String statistics = "";

        double percents = 0.0;
        if (joueur.obtenirPartieCourante().getCountQuestions() != 0) {
            percents = (double) (joueur.obtenirPartieCourante().getCountGoodAnswers() * 100) / joueur.obtenirPartieCourante().getCountQuestions();
        }

        //System.out.println("percents1 : " + percents);


        try {

            synchronized (DB_LOCK) {
                //String langue = joueur.obtenirProtocoleJoueur().langue;
                room_id = joueur.obtenirSalleCourante().getRoomId();

                // Création du SQL pour l'ajout
                String strSQL = "INSERT INTO game_user(game_id, user_id, score, has_won, questions_answers, room_id, stats) VALUES " +
                        "(" + clePartie + "," + cleJoueur + "," + pointage + "," + intGagner + ",'" + statistics + "'," + room_id + "," + percents + ");";
                // Ajouter l'information pour ce joueur
                requete.executeUpdate(strSQL);
            }
        } catch (Exception e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_ajout_infos_update") + e.getMessage());
        }

        fillEndDate(cleJoueur);

    }// end methode

    /**
     * Methode used to update in DB the player's money ****
     * @param cleJoueur
     * @param newMoney
     */
    public void setNewPlayersMoney(int cleJoueur, int newMoney) {
        // Update the money in player's account
        String strMoney = " UPDATE user SET money = " + newMoney + " WHERE user_id = " + cleJoueur + ";";

        try {

            synchronized (DB_LOCK) {
                // Ajouter l'information pour ce joueur
                requete.executeUpdate(strMoney);
            }
        } catch (Exception e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_ajout_infos_update_money") + e.getMessage());
        }

    }//end methode

    /**
     * Methode used to charge to player's money from DB for current game ***
     * option can be disabled with
     * @param userId
     */
    public int getPlayersMoney(int userId) {
        int money = 0;
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT user.money  FROM user WHERE user_id = " + userId + ";");
                if (rs.next()) {
                    money = rs.getInt("money");
                }
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_get_money"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
        return money;
    }// end methode

    public int getUserRole(String username, String password) {
        int role = 0;
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT role_id FROM user WHERE username='" + username + "' AND password='" + password+"'" );
                if (rs.next())
                    role = rs.getInt("role_id");
            }
        } catch(SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
        return role;
    }
    
    public Map<String, Object> getRoomInfo(int roomId) throws SQLException {
        Map<String, Object> roomData = new TreeMap<String, Object>();
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery(
                        "SELECT room.password, user.username, user.role_id, beginDate, endDate, masterTime, room_info.language_id,room_info.name,room_info.description " +
                        "FROM room_info, room, user, game_type " +
                        "WHERE room.room_id = " + roomId + " " +
                        "AND room.room_id = room_info.room_id " +
                        "AND user.user_id = room.user_id");
                int row = 0;
                Map<Integer, String> names = new TreeMap<Integer, String>();
                Map<Integer, String> descriptions = new TreeMap<Integer, String>();
                while (rs.next()) {
                    if (row == 0) {
                        roomData.put("password", rs.getString("password"));
                        roomData.put("username", rs.getString("username"));
                        roomData.put("beginDate", rs.getTimestamp("beginDate"));
                        roomData.put("endDate", rs.getTimestamp("endDate"));
                        roomData.put("masterTime", rs.getInt("masterTime"));
                        int role_id = rs.getInt("role_id");
                        roomData.put("roomType", role_id == 3 ? "profsType" : "General");
                        roomData.put("names", names);
                        roomData.put("descriptions", descriptions);
                    }
                    int language_id = rs.getInt("language_id");
                    names.put(language_id, rs.getString("room_info.name"));
                    descriptions.put(language_id, rs.getString("room_info.description"));
                    row++;
                }
            }
        } catch (SQLException e) {
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_room_info"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
            throw (e);
        }
        return roomData;
    }

    public Set<Integer> getRoomKeywordIds(int roomId) throws SQLException
    {
            Set<Integer> ids = new TreeSet<Integer>();
            try
            {
                    synchronized (DB_LOCK)
                    {
                            ResultSet rs = requete.executeQuery("SELECT keyword_id FROM rooms_keywords WHERE room_id=" + roomId);
                            while (rs.next())
                                    ids.add(rs.getInt("keyword_id"));
                    }
            } catch (SQLException e)
            {
                    objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_room_keywordss"));
                    objLogger.error(GestionnaireMessages.message("bd.trace"));
                    objLogger.error(e.getMessage());
                    e.printStackTrace();
                    throw (e);
            }
            return ids;
    }

    public Set<Integer> getRoomGameTypeIds(int roomId) throws SQLException
    {
            Set<Integer> types = new TreeSet<Integer>();
            try
            {
                    synchronized (DB_LOCK)
                    {
                            ResultSet rs = requete.executeQuery("SELECT game_type_id " +
                                    "FROM room_game_types " +
                                    "WHERE room_id=" + roomId);
                            while (rs.next())
                                    types.add(rs.getInt("game_type_id"));
                    }
            } catch(SQLException e)
            {
                    objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_game_types"));
                    objLogger.error(GestionnaireMessages.message("bd.trace"));
                    objLogger.error(e.getMessage());
                    e.printStackTrace();
                    throw(e);
            }
            return types;
    }

    /**
     * Méthode utilisé pour charger les salles
     */
    public void fillsRooms()
    {
        ArrayList<Integer> rooms = new ArrayList<Integer>();
        //find all rooms  and fill in ArrayList
        try {
            synchronized (DB_LOCK)
            {
                    ResultSet rs = requete.executeQuery("SELECT room.room_id FROM room where (beginDate < NOW() AND endDate > NOW()) OR beginDate is NULL OR endDate is NULL;");
                    while (rs.next())
                            rooms.add(rs.getInt("room.room_id"));
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }

        // create the rooms by this list of rooms ID  and put them in the ControleurJeu
        fillRoomList(rooms);

    }//end methode fillsRooms

    /**
     * Methode satellite for fillsRooms()
     * @param roomIds
     */
    public void fillRoomList(ArrayList<Integer> roomIds)
    {
        synchronized(DB_LOCK) {

            for (int roomId : roomIds)
            {
                    Salle objSalle;
                    try
                    {
                            objSalle = new Salle(objControleurJeu, roomId);
                            objControleurJeu.ajouterNouvelleSalle(objSalle);
                            objControleurJeu.preparerEvenementNouvelleSalle(objSalle);
                    } catch (SQLException e) {
                            //Une erreur est survenue lors de la construction de la salle avec id 'roomId'
                            objLogger.error(GestionnaireMessages.message("bd.erreur_construction_salle"));
                            objLogger.error(GestionnaireMessages.message("bd.trace"));
                            objLogger.error(e.getMessage());
                            e.printStackTrace();
                    } catch (RuntimeException e) {
                            //Une erreur est survenue lors de la recherche de la prochaine salle
                            objLogger.error(GestionnaireMessages.message("bd.erreur_prochaine_salle"));
                            objLogger.error(GestionnaireMessages.message("bd.trace"));
                            objLogger.error(e.getMessage());
                            e.printStackTrace();
                    }
            }
        }
    }
    /**
     * @param objReglesSalle
     */
    //@SuppressWarnings("unchecked")
    public void chargerReglesTable(Regles objReglesTable, String gameType, int roomId) {

        int gameTypeID = 1; // default type - mathEnJeu
        if (gameType.equals("Tournament")) {
            gameTypeID = 2;
        }
        if (gameType.equals("Course")) {
            gameTypeID = 3;
        }
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT rule.*  FROM rule WHERE rule.rule_id = " + gameTypeID + ";");
                while (rs.next()) {
                    boolean shownumber = rs.getBoolean("show_nb_questions");
                    boolean chat = rs.getBoolean("chat");
                    boolean money = rs.getBoolean("money_permit");
                    Float ratioTrous = Float.parseFloat(rs.getString("hole_ratio"));
                    Float ratioMagasins = Float.parseFloat(rs.getString("shop_ratio"));
                    Float ratioCasesSpeciales = Float.parseFloat(rs.getString("special_square_ratio"));
                    Float ratioPieces = Float.parseFloat(rs.getString("coin_ratio"));
                    Float ratioObjetsUtilisables = Float.parseFloat(rs.getString("object_ratio"));
                    int valeurPieceMax = rs.getInt("max_coin_value");
                    int tempsMin = rs.getInt("minimal_time");
                    int tempsMax = rs.getInt("maximal_time");
                    int deplacementMax = rs.getInt("max_movement");
                    int maxShopObjects = rs.getInt("max_object_shop");
                    int maxNbPlayers = rs.getInt("maxNbPlayers");
                    //int maxNbObjectsAndMoney = rs.getInt( "max_object_coin" );
                    int nbTracks = rs.getInt("nbTracks");
                    int nbVirtualPlayers = rs.getInt("nbVirtualPlayers");


                    //objReglesSalle.setMaxNbObjectsAndMoney(maxNbObjectsAndMoney);
                    objReglesTable.setMaxNbPlayers(maxNbPlayers);
                    objReglesTable.setShowNumber(shownumber);
                    objReglesTable.setBolMoneyPermit(money);
                    objReglesTable.definirPermetChat(chat);
                    objReglesTable.definirRatioTrous(ratioTrous);
                    objReglesTable.definirRatioMagasins(ratioMagasins);
                    objReglesTable.definirRatioCasesSpeciales(ratioCasesSpeciales);
                    objReglesTable.definirRatioPieces(ratioPieces);
                    objReglesTable.definirRatioObjetsUtilisables(ratioObjetsUtilisables);
                    objReglesTable.definirValeurPieceMaximale(valeurPieceMax);
                    objReglesTable.definirTempsMinimal(tempsMin);
                    objReglesTable.definirTempsMaximal(tempsMax);
                    objReglesTable.definirDeplacementMaximal(deplacementMax);
                    objReglesTable.setIntMaxSaledObjects(maxShopObjects);
                    objReglesTable.setNbTracks(nbTracks);
                    objReglesTable.setNbVirtualPlayers(nbVirtualPlayers);

                }
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_rules_charging"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }

        // charger autres regles
        TreeSet magasins = objReglesTable.obtenirListeMagasinsPossibles();
        //TreeSet casesCouleur = objReglesTable.obtenirListeCasesCouleurPossibles();
        //TreeSet casesSpeciale = objReglesTable.obtenirListeCasesSpecialesPossibles();
        TreeSet<ReglesObjetUtilisable> objetsUtilisables = objReglesTable.obtenirListeObjetsUtilisablesPossibles();

        this.chargerReglesMagasins(magasins, roomId);
        //this.chargerReglesCasesCouleur(casesCouleur, roomId);
        //this.chargerReglesCasesSpeciale(casesSpeciale, roomId);
        this.chargerReglesObjetsUtilisables(objetsUtilisables, roomId);

    }// fin méthode chargerReglesSalle

    /**
     * Méthode utilisée pour charger la liste des objets utilisables  ***
     * @param objetsUtilisables
     * @param roomId
     * @param langId
     */
    private void chargerReglesObjetsUtilisables(TreeSet<ReglesObjetUtilisable> objetsUtilisables, int roomId) {
        try {
            synchronized (DB_LOCK) {
                ResultSet rst = requete.executeQuery("SELECT room_object.priority, object_info.name " +
                        " FROM room_object, object_info " +
                        " WHERE room_object.room_id = " + roomId +
                        " AND room_object.object_id = object_info.object_id " +
                        " AND object_info.language_id = " + 1 +
                        ";");
                while (rst.next()) {
                    Integer tmp1 = rst.getInt("priority");
                    String tmp2 = rst.getString("name");

                    objetsUtilisables.add(new ReglesObjetUtilisable(tmp1, tmp2, Visibilite.Aleatoire));

                }
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_objects_rules_"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }// fin catch

    }// fin méthode

    /**
     * Méthode utilisée pour charger la liste des cases spéciales    ***
     * @param casesSpeciale
     * @param roomId
     */
    private void chargerReglesCasesSpeciale(TreeSet<ReglesCaseSpeciale> casesSpeciale, int roomId) {
        try {
            synchronized (DB_LOCK) {
                ResultSet rst = requete.executeQuery("SELECT special_square_rule.priority, special_square_rule.type " +
                        " FROM special_square_rule " +
                        " WHERE special_square_rule.room_id = " + roomId +
                        ";");
                while (rst.next()) {
                    Integer tmp1 = rst.getInt("priority");
                    Integer tmp2 = rst.getInt("type");

                    casesSpeciale.add(new ReglesCaseSpeciale(tmp1, tmp2));

                }
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }// fin catch

    }// fin méthode

    /**
     * Méthode utilisée pour charger la liste des magasins dans les Regles du partie ***
     * @param magasins 
     * @param roomId
     */
    private void chargerReglesMagasins(TreeSet<ReglesMagasin> magasins, int roomId) {

        try {
            synchronized (DB_LOCK) {
                ResultSet rst = requete.executeQuery("SELECT room_shop.priority, shop_info.name " +
                        " FROM room_shop, shop_info " +
                        " WHERE shop_info.language_id = " + 1 +
                        " AND room_shop.shop_id = shop_info.shop_id " +
                        " AND  room_shop.room_id = " + roomId +
                        ";");
                while (rst.next()) {

                    String tmp2 = rst.getString("name");
                    Integer tmp1 = rst.getInt("priority");
                    magasins.add(new ReglesMagasin(tmp1, tmp2));

                }
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
    }// fin méthode

    /**
     * Methode used to charge  
     * @param user's language 
     * @return URL of Questions-Answers on server
     */
    public String transmitUrl(String language) {
        String url = "";
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT language.url FROM language " +
                        " WHERE language.short_name = '" + language + "';");
                while (rs.next()) {
                    url = rs.getString("url");
                }
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
        return url;
    }//end methode

    /**
     * Used to control if room has a language  ***
     * @param salle
     * @param language
     * @param Boulean
     * @return
     */
    public Boolean roomLangControl(Salle salle, String language) {
        Boolean existe = false;

        // Pour tenir compte de la langue
        int cleLang = 1;

        if (language.equalsIgnoreCase("fr")) {
            cleLang = 1;
        } else if (language.equalsIgnoreCase("en")) {
            cleLang = 2;
        }
        int key = salle.getRoomId();
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT name FROM room_info " +
                        " WHERE  room_info.room_id = '" + key +
                        "' AND room_info.language_id = '" + cleLang + "' ;");
                if (rs.next()) {
                    String name = rs.getString("name");
                    existe = true;
                }
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }

        return existe;
    }//end methode

    /**
     * Methode used to fill store with objects to sell   ***
     * @param nomMagasin
     * @param listObjects
     */
    public void fillShopObjects(String nomMagasin, ArrayList<String> listObjects) {


        try {
            synchronized (DB_LOCK) {
                ResultSet rst = requete.executeQuery("SELECT object_info.name " +
                        " FROM shop_info, shop_object, object_info " +
                        " WHERE shop_info.name = '" + nomMagasin +
                        "' AND shop_info.shop_id = shop_object.shop_id " +
                        " AND  shop_object.object_id = object_info.object_id ;");
                while (rst.next()) {

                    String object = rst.getString("name");

                    listObjects.add(object);

                }
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
    }// end methode


    /*
     * Methode used to get from DB table rule the new dimention of the game board
     * and set it in the Regles

    public void getNewTableDimentions()
    {
    String roomName = objSalle.getRoomName("");
    Regles objReglesSalle = objSalle.getRegles();
    boolean exist = false;
    String nomFr = "";
    String nomEng = "";
    for(int i = 0; i < roomName.length(); i++)
    {
    if(roomName.charAt(i) == '/')
    exist = true;
    }

    if(exist)
    {
    StringTokenizer nomSalle = new StringTokenizer(roomName, "/");
    nomFr = nomSalle.nextToken().trim();
    nomEng = nomSalle.nextToken().trim();
    }

    Integer tmp1 = 0;
    Integer tmp2 = 0;

    try
    {
    synchronized( DB_LOCK )
    {
    ResultSet rst = requete.executeQuery( "SELECT minimal_time, maximal_time FROM rule " +
    " where rule_id IN (SELECT rule_id FROM room,room_info where room.room_id = room_info.room_id " +
    " AND (room_info.name = '" + nomFr + "' OR room_info.name = '" + nomEng + "' OR room_info.name = '" + roomName + "'));");
    if(rst.next())
    {
    tmp1 = rst.getInt( "minimal_time" );
    tmp2 = rst.getInt( "maximal_time" );

    }
    }
    }
    catch (SQLException e)
    {
    // Une erreur est survenue lors de l'exécution de la requète
    objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_update_table_dimentions"));
    objLogger.error(GestionnaireMessages.message("bd.trace"));
    objLogger.error( e.getMessage() );
    e.printStackTrace();
    }// fin catch

    if(tmp1 > 0)
    objReglesSalle.definirTempsMinimal(tmp1);
    if(tmp2 > 0)
    objReglesSalle.definirTempsMaximal(tmp2);
    }//end methode */
    //******************************************************************
    //  Bloc used to put new room in DB from room created in profModule
    //******************************************************************
    /**
     * Method used to put new room in DB from room created in profModule
     * put it in room table
     */
    public int updateRoom(int room_id, String password,
            TreeMap<Integer,String> names, TreeMap<Integer,String> descriptions,
            String beginDate, String endDate,
            int masterTime,
            String keywordIds, String gameTypeIds) {

        String strBeginDate = (beginDate==null || beginDate.isEmpty())?"NULL":"'"+beginDate+"'";
        String strEndDate = (endDate==null || endDate.isEmpty())?"NULL":"'"+endDate+"'";
        String strSQL = "UPDATE room SET " +
                "password='"+password+"'," +
                "beginDate="+strBeginDate+"," +
                "endDate="+strEndDate+","+
                "masterTime="+masterTime + " " +
                "WHERE room_id=" + room_id;
        try {
            synchronized (DB_LOCK) {
                // Ajouter l'information pour cette partie
                requete.executeUpdate(strSQL);
            }
        } catch (Exception e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_adding_rooms_modProf") + e.getMessage());
        }

        //add information of the room to other tables of DB
        deleteAllAssociatedRoomInfo(room_id);
        for (Integer language_id : names.keySet())
            putNewRoomInfo(room_id, language_id, names.get(language_id), descriptions.get(language_id));
        putNewRoomGameTypes(room_id, gameTypeIds);
        putNewRoomKeywords(room_id, keywordIds);

        //System.out.println(room_id);

        return room_id;
    }// end methode

    public void deleteAllAssociatedRoomInfo(int room_id) {
        try {
            synchronized (DB_LOCK) {
                // Ajouter l'information pour cette partie
                requete.executeUpdate("DELETE FROM room_info WHERE room_id=" + room_id);
                requete.executeUpdate("DELETE FROM rooms_keywords WHERE room_id=" + room_id);
                requete.executeUpdate("DELETE FROM room_game_types WHERE room_id=" + room_id);
            }
        } catch (Exception e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_delete_rooms_modProf") + e.getMessage());
        }
    }
    /**
     * Method used to put new room in DB from room created in profModule
     * put it in room table
     */
    public int putNewRoom(String password, int user_id,
            TreeMap<Integer,String> names, TreeMap<Integer,String> descriptions,
            String beginDate, String endDate,
            int masterTime,
            String keywordIds, String gameTypeIds) {

        int room_id = 0;
        String strBeginDate = (beginDate==null || beginDate.isEmpty())?"NULL":"'"+beginDate+"'";
        String strEndDate = (endDate==null || endDate.isEmpty())?"NULL":"'"+endDate+"'";

        String strSQL = "INSERT INTO room (password, user_id, rule_id, beginDate, endDate, masterTime) VALUES ('" +
                password + "'," + 
                user_id + ",1," +
                strBeginDate + "," +
                strEndDate + "," +
                masterTime + ")";
        try {
            synchronized (DB_LOCK) {
                // Ajouter l'information pour cette partie
                requete.executeUpdate(strSQL, Statement.RETURN_GENERATED_KEYS);

                // Aller chercher la clé de la salle qu'on vient d'ajouter
                ResultSet rs = requete.getGeneratedKeys();

                // On retourne la clé de partie
                rs.next();
                room_id = rs.getInt("GENERATED_KEY");
            }
        } catch (Exception e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_adding_rooms_modProf") + e.getMessage());
        }

        //add information of the room to other tables of DB
        for (Integer language_id : names.keySet())
            putNewRoomInfo(room_id, language_id, names.get(language_id), descriptions.get(language_id));
        putNewRoomGameTypes(room_id, gameTypeIds);
        putNewRoomKeywords(room_id, keywordIds);

        //System.out.println(room_id);

        return room_id;
    }// end methode

    /**
     * Method satellite to putNewRoom() used to put new room in DB from room created in profModule
     * put gameTypes in room_game_types table
     */
    private void putNewRoomKeywords(int room_id, String keywordIds) {

        LinkedList<Integer> roomKeywords = new LinkedList<Integer>();
        StringTokenizer ids = new StringTokenizer(keywordIds, ",");

        while (ids.hasMoreTokens()) {
            roomKeywords.addLast(Integer.parseInt(ids.nextToken()));
        }

        int length = roomKeywords.size();
        // Création du SQL pour l'ajout
        PreparedStatement prepStatement = null;
        try {
            prepStatement = connexion.prepareStatement("INSERT INTO rooms_keywords (room_id, keyword_id) VALUES ( ? , ?);");

            for (int i = 0; i < length; i++) {

                // Ajouter l'information pour cette salle
                prepStatement.setInt(1, room_id);
                prepStatement.setInt(2, roomKeywords.removeFirst());

                prepStatement.addBatch();//executeUpdate();

            }
            prepStatement.executeBatch();

        } catch (Exception e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_adding_rooms_Keywords") + e.getMessage());
        }
    }// end method

    /**
     * Method satellite to putNewRoom() used to put new room in DB from room created in profModule
     * put gameTypes in room_game_types table
     */
    private void putNewRoomGameTypes(int room_id, String gameTypes) {

        ArrayList<Integer> roomAllowedTypes = new ArrayList<Integer>();
        StringTokenizer types = new StringTokenizer(gameTypes, ",");

        while (types.hasMoreTokens()) {
            roomAllowedTypes.add(Integer.parseInt(types.nextToken()));
        }

        int length = roomAllowedTypes.size();
        // Création du SQL pour l'ajout
        PreparedStatement prepStatement = null;
        try {
            prepStatement = connexion.prepareStatement("INSERT INTO room_game_types (room_id, game_type_id) VALUES ( ? , ?);");

            for (int i = 0; i < length; i++) {

                // Ajouter l'information pour cette salle
                prepStatement.setInt(1, room_id);
                prepStatement.setInt(2, roomAllowedTypes.get(i));

                prepStatement.addBatch();//executeUpdate();

            }
            prepStatement.executeBatch();

        } catch (Exception e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_adding_rooms_gameTypes") + e.getMessage());
        }
    }

    /**
     * Method satellite to putNewRoom() used to put new room in DB from room created in profModule
     * put infos in room_info table
     */
    private void putNewRoomInfo(int room_id, int lang_id, String name, String roomDesc) {

        // Création du SQL pour l'ajout
        String strSQL = "INSERT INTO room_info (room_id, language_id, name, description) VALUES (" +
                room_id + "," + lang_id + ",\"" + name + "\",\"" + roomDesc + "\");";
        try {
            synchronized (DB_LOCK) {
                // Ajouter l'information pour cette salle
                requete.executeUpdate(strSQL);
            }
        } catch (Exception e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_adding_rooms_infosTable") + e.getMessage());
        }


    }// end methode

    /**
     * Method satellite to putNewRoom() used to put new room in DB from room created in profModule
     * put infos in special_square_rule table
     * @throws SQLException
     */
    private void putNewRoomSpecialSquare(int room_id) {

        PreparedStatement prepStatement = null;
        try {
            prepStatement = connexion.prepareStatement("INSERT INTO special_square_rule (room_id, type, priority) VALUES ( ? , ?, ?);");

            for (int i = 0; i < 5; i++) {

                // Ajouter l'information pour cette salle
                prepStatement.setInt(1, room_id);
                prepStatement.setInt(2, i + 1);
                prepStatement.setInt(3, i + 1);
                prepStatement.addBatch();//executeUpdate();

            }
            prepStatement.executeBatch();

        } catch (Exception e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_adding_rooms_specialSquare") + e.getMessage());
        }


    }// end methode

    /**
     * Method satellite to putNewRoom() used to put new room in DB from room created in profModule
     * put infos in room_object table
     * @throws SQLException
     */
    private void putNewRoomObjects(int room_id) {

        PreparedStatement prepStatement = null;
        try {
            prepStatement = connexion.prepareStatement("INSERT INTO room_object (room_id, object_id, priority) VALUES ( ? , ?, ?);");



            // Ajouter l'information pour cette salle
            prepStatement.setInt(1, room_id);
            prepStatement.setInt(2, 1);
            prepStatement.setInt(3, 1);
            prepStatement.addBatch();

            prepStatement.setInt(1, room_id);
            prepStatement.setInt(2, 3);
            prepStatement.setInt(3, 2);
            prepStatement.addBatch();

            prepStatement.setInt(1, room_id);
            prepStatement.setInt(2, 7);
            prepStatement.setInt(3, 3);
            prepStatement.addBatch();

            prepStatement.executeBatch();

        } catch (Exception e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_adding_rooms_objects") + e.getMessage());
        }


    }// end methode

    /**
     * Method satellite to putNewRoom() used to put new room in DB from room created in profModule
     * put infos in room_shop table
     * @throws SQLException
     */
    private void putNewRoomShops(int room_id) {

        PreparedStatement prepStatement = null;
        try {
            prepStatement = connexion.prepareStatement("INSERT INTO room_shop (room_id, shop_id, priority) VALUES ( ? , ?, ?);");


            for (int i = 0; i < 3; i++) {

                // Ajouter l'information pour cette salle
                prepStatement.setInt(1, room_id);
                prepStatement.setInt(2, i + 1);
                prepStatement.setInt(3, i + 1);
                prepStatement.addBatch();//executeUpdate();

            }
            prepStatement.executeBatch();

        } catch (Exception e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_adding_rooms_specialSquare") + e.getMessage());
        }


    }// end methode

    public boolean deleteRoom(int room_id) {
        int numDeleted = 0;
        try {
            synchronized(DB_LOCK) {
                numDeleted = requete.executeUpdate("DELETE FROM room WHERE room_id="+room_id);
            }
        } catch (SQLException e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_deleting_room") + e.getMessage());
        }
        return numDeleted!=0;
    }
    //******************************************************************
    /**
     * Methode used to create the report for a room
     * used for the moduleProf
     * @param roomName
     */
    public String createRoomReport(int room_id, String langue) {
        StringBuffer report = new StringBuffer();
        int user_id = 0;
        int score = 0;
        String questions_answers = "";
        Boolean won = null;
        String first_name = "";
        String last_name = "";
        String username = "";
        String roomName;
        if (langue.equals("fr")) {
            roomName = this.getRoomName(room_id, 1) + "\n";
            report.append("Salle: " + roomName);
        } else if (langue.equals("en")) {
            roomName = this.getRoomName(room_id, 2) + "\n";
            report.append("Room: " + roomName);
        }

        try {

            synchronized (DB_LOCK) {

                ResultSet rs = requete.executeQuery("SELECT game_user.user_id,name,last_name,username,score,questions_answers,has_won " +
                        "FROM game_user, user " +
                        "WHERE room_id = '" + room_id + "' AND game_user.user_id = user.user_id;");
                while (rs.next()) {
                    user_id = rs.getInt("user_id");
                    score = rs.getInt("score");
                    questions_answers = rs.getString("questions_answers");
                    won = rs.getBoolean("has_won");
                    first_name = rs.getString("name");
                    last_name = rs.getString("last_name");
                    username = rs.getString("username");
                    //report.append(user_id + score + questions_answers);
                    makeReport(report, user_id, score, questions_answers, won, langue, first_name, last_name, username);
                }

            }
        } catch (Exception e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_create_report") + e.getMessage());
        }


        return report.toString();

    }// end methode

    /**
     * Methode satellite to getReport
     * @param report
     * @param user_id
     * @param score
     * @param answers
     * @param won
     * @param langue
     */
    private void makeReport(StringBuffer report, int user_id, int score, String answers, Boolean won, String langue, String first_name, String last_name, String username) {
        StringTokenizer allAnswers = new StringTokenizer(answers, "/-/");

        String levels = "- ";
        String answersDescription = "- ";

        if (allAnswers.hasMoreTokens()) {
            levels = allAnswers.nextToken();
        }

        if (allAnswers.hasMoreTokens()) {
            answersDescription = allAnswers.nextToken();
        }
        if (langue.equals("fr")) {

            report.append("Joueur : " + first_name + " - " + last_name + " (Alias: " + username + ")\n");
            report.append("Le pointage pour cette partie : " + score + "  (gagnant: " + (won ? "oui" : "non") + ")\n");
            report.append("Niveaux: " + levels + "\n");
            report.append("Détails: (légende: q:numero de la question, r: réponse, c:correct, t:temps)\n " + answersDescription + "\n\n");
        } else if (langue.equals("en")) {
            report.append("User : " + first_name + " " + last_name + " (Alias: " + username + ")\n");
            report.append("Points : " + score + ". He won : " + won + "\n");
            report.append("Levels : " + levels + "\n");
            report.append("Details : (legend: q:number of question, r:answer, c:correct, t:time)\n" + answersDescription + "\n\n");
        }

    }// end methode

    /**
     * Methode satellite to the makeReport()
     */
    private String getRoomName(int roomId, int langue) {
        String roomName = "";
        try {

            synchronized (DB_LOCK) {

                ResultSet rs = requete.executeQuery("SELECT name FROM room_info WHERE room_id = '" + roomId + "' AND language_id = '" + langue + "';");
                while (rs.next()) {
                    roomName = rs.getString("name");
                }

            }
        } catch (Exception e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_create_report_get_name") + e.getMessage());
        }
        return roomName;

    }// end methode

    /**
     * Return a map of [room_id --> objSalle] for the specified user.
     * This method retrieves the info from the DB without touching any of
     * the server's lists of active rooms.
     * @param userId the DB id of the user for which to retrieve the rooms
     * @return a map of [room_id --> objSalle] filled with the rooms created
     *         by the user whose DB id was specified.
     */
    public Map<Integer,Salle> getRoomsForUserId(int userId) {
        Map<Integer, Salle> roomMap = new TreeMap<Integer,Salle>();
        //find all rooms  and fill in ArrayList
        try {
                synchronized (DB_LOCK) {
                    ResultSet rs = requete.executeQuery("SELECT room.room_id FROM room WHERE user_id = '" + userId + "';");
                    ArrayList<Integer> roomIds = new ArrayList<Integer>();
                    while (rs.next())
                        roomIds.add(rs.getInt("room.room_id"));
                    for (Integer roomId : roomIds)
                        roomMap.put(roomId, new Salle(objControleurJeu, roomId));
                }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
        return roomMap;
    }// end methode

    public String controlPWD(String clientPWD) {
        String encodedPWD = "";
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT PASSWORD('" + clientPWD + "') AS password;");

                if (rs.next()) {
                    encodedPWD = rs.getString("password");
                }
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete _PWD"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        } catch (RuntimeException e) {
            //Une erreur est survenue lors de la recherche de la prochaine salle
            objLogger.error(GestionnaireMessages.message("bd.erreur_PWD"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }

        //System.out.println(encodedPWD);
        return encodedPWD;
    }

    public void reportBugQuestion(int user_id, int question, int language_id,
            String errorDescription) {
        try {
            synchronized (DB_LOCK) {
                // Ajouter l'information pour cette salle
                requete.executeUpdate("INSERT INTO questions_with_error (question_id, user_id, language_id, description) VALUES ( " + question + " ," + user_id + " , " + language_id + " ,'" + errorDescription + "');");
            }
        } catch (Exception e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_adding_questions_errors") + e.getMessage());
        }


    }// end methode

    /**
     * Generates a map of keywords
     *        language_id --> [keyword_id->keyword_name,group_id,is_group_head]
     */
    public TreeMap<Integer, TreeMap<Integer, String>> getKeywordsMap() {
        TreeMap<Integer, TreeMap<Integer, String>> keywords = new TreeMap<Integer, TreeMap<Integer, String>>();
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery(
                        "SELECT keyword_info.*,group_info.group_id,group_info.name FROM keyword " +
                        "LEFT JOIN keyword_info ON keyword_info.keyword_id=keyword.keyword_id " +
                        "LEFT JOIN group_info ON group_info.group_id = keyword.group_id AND group_info.language_id = keyword_info.language_id");
                while (rs.next()) {
                    int kid = rs.getInt("keyword_info.keyword_id");
                    int lid = rs.getInt("keyword_info.language_id");
                    int gid = rs.getInt("group_info.group_id");
                    String gname = rs.getString("group_info.name");
                    String kname = rs.getString("keyword_info.name");
                    TreeMap<Integer, String> kmap = keywords.get(lid);
                    if (kmap == null) {
                        kmap = new TreeMap<Integer, String>();
                        keywords.put(lid, kmap);
                    }
                    kmap.put(kid, kname+","+gid+","+gname);
                }
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_creer_keywords_map"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
        return keywords;
    } // end method

    /**
     * Generates a map of languages
     *         language_id --> [language_id->name]
     *
     * e.g. 0 --> {{1,fr},{2,en}}
     *      1 --> {{1,francais},{2,anglais}}
     *      2 --> {{1,French},{2,English}}
     */
    public TreeMap<Integer, TreeMap<Integer, String>> getLanguagesMap() {
        TreeMap<Integer, TreeMap<Integer, String>> languages = new TreeMap<Integer, TreeMap<Integer, String>>();
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT language.short_name,language_info.* " +
                        "FROM language LEFT JOIN language_info ON language.language_id=language_info.language_id");
                languages.put(0, new TreeMap<Integer,String>());
                while (rs.next()) {
                    String shortName = rs.getString("language.short_name");
                    int lid = rs.getInt("language_info.language_id");
                    int tlid = rs.getInt("language_info.translation_language_id");
                    String name = rs.getString("language_info.name");
                    languages.get(0).put(lid, shortName); //put overwrites if key is already present so this is ok, if slightly inefficient
                    TreeMap<Integer, String> tlmap = languages.get(tlid);
                    if (tlmap == null) {
                        tlmap = new TreeMap<Integer, String>();
                        languages.put(tlid, tlmap);
                    }
                    tlmap.put(lid, name);
                }
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_creer_langues_map"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
        return languages;
    } // end method

    /**
     * Generates a game type map
     *     game_type_id --> name
     */
    public TreeMap<Integer, String> getGameTypesMap() {
        TreeMap<Integer, String> gameTypes = new TreeMap<Integer, String>();
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT * FROM game_type");
                while (rs.next()) {
                    int id = rs.getInt("game_type_id");
                    String name = rs.getString("name");
                    gameTypes.put(id, name);
                }
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requète
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_creer_gameType_map"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
        return gameTypes;
    } // end method

   
}// end class

