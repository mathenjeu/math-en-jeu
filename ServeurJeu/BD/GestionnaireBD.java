package ServeurJeu.BD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import Enumerations.Visibilite;
import ServeurJeu.ControleurJeu;
import ServeurJeu.ComposantesJeu.InformationPartie;
import ServeurJeu.ComposantesJeu.InformationQuestion;
import ServeurJeu.ComposantesJeu.RapportDeSalle;
import ServeurJeu.ComposantesJeu.Salle;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesCaseSpeciale;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesMagasin;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesObjetUtilisable;
import ServeurJeu.Configuration.GestionnaireConfiguration;
import ServeurJeu.Configuration.GestionnaireMessages;

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
    // Objet Statement nécessaire pour envoyer une requête au serveur MySQL
    private Statement requete;
    static private Logger objLogger = Logger.getLogger(GestionnaireBD.class);
    private Object DB_LOCK = new Object();

    /**
     * Constructeur de la classe GestionnaireBD qui permet de garder la
     * référence vers le contrôleur de jeu
     * @param controleur le controleur de jeu du serveur {@code Maitre}.
     */
    public GestionnaireBD(ControleurJeu controleur) {
        super();

        // Garder la référence vers le contrôleur de jeu
        objControleurJeu = controleur;

        // get connection from the pool
        DBConnectionsPoolManager pool = DBConnectionsPoolManager.getInstance();
        connexion = pool.getConnection();
        
        // Création de l'objet "requête"
        this.takeStatement();  
    }

    /**
     * Method used to release the problematic connection
     * and to take another one
     */
    public void getNewConnection(){
    	this.releaseConnection();
    	DBConnectionsPoolManager pool = DBConnectionsPoolManager.getInstance();
        connexion = pool.getConnection();
		this.takeStatement();
    }
    
    
    /**
     * Cette fonction permet de créer un objet requête
     */
    public void takeStatement() {

    	// Création de l'objet "requête"
    	try {
    		requete = connexion.createStatement();
    	} catch (SQLException e) {
    		// Une erreur est survenue lors de la création d'une requête
    		objLogger.error(GestionnaireMessages.message("bd.erreur_creer_requete"));
    		objLogger.error(GestionnaireMessages.message("bd.trace"));
    		objLogger.error(e.getMessage());
    		this.getNewConnection();    		
    		return;
    	}

    }
    
    /**
     * 
     */
    public void releaseConnection(){
    	try {
			this.requete.close();
		} catch (SQLException e) {
			objLogger.error(GestionnaireMessages.message("bd.erreur_fermer_requete"));			
		}
    	DBConnectionsPoolManager pool = DBConnectionsPoolManager.getInstance();
    	pool.freeConnection(this.connexion);
    	this.connexion = null;
    }
    
    public void finalize(){
    	this.releaseConnection();
    	this.DB_LOCK = null;
    }


    /**
     * Cette fonction permet de chercher dans la BD si le joueur dont le nom    ***
     * d'utilisateur et le mot de passe sont passés en paramètres existe.
     *
     * @param nomUtilisateur Le nom d'utilisateur du joueur
     * @param motDePasse Le mot de passe du joueur
     * @return <ul>
     *           <li> null s'il n'y a pas de rangée dans la BD avec
     *                user.username = 'nomUtilisateur' ET user.password='motDePasse'</li>
     *           <li> user.username si la BD contient cette rangée.  On retourne
     *                le username parce qu'on veut que la capitalisation soit correcte</li>
     *         </ul>
     */
    public String getUsername(String nomUtilisateur, String motDePasse) {

        GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
        String codeErreur = config.obtenirString("gestionnairebd.code_erreur_inactivite");
        String name = "";

        int count = 0;	//compteur du nombre d'essai de la requête

        //boucler la requête jusqu'à 5 fois si la connexion à MySQL
        //a été interrompu du à un manque d'activité de la connexion
        while (count < 5) {
            try {
                synchronized (DB_LOCK) {
                    ResultSet rs = requete.executeQuery("SELECT username FROM jos_users WHERE username = '" + nomUtilisateur + "' AND password = '" + motDePasse + "';");
                    if (rs.next() == false)
                    {
                    	rs.close();
                    	return null;
                    }
                    name = rs.getString("username");
                    rs.close();  
                    return name;
                }
            } catch (SQLException e) {
                //on vérifie l'état de l'exception
                //si l'état est égal au codeErreur on peut réesayer la connexion
                if (e.getSQLState().equals(codeErreur))
                    count++;
                else {
                    // Une erreur est survenue lors de l'exécution de la requête
                    objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
                    objLogger.error(GestionnaireMessages.message("bd.trace"));
                    objLogger.error(e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return "";
    }
   
    /**
     * Cette fonction permet de chercher dans la BD le joueur et de remplir  ***
     * les champs restants du joueur.
     *
     * @param joueur Le joueur duquel il faut trouver les informations et les
     *        définir dans l'objet
     */
    public void remplirInformationsJoueur(JoueurHumain joueur) {
        int cle = -1;

        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery(
                        "SELECT jos_users.id, jos_comprofiler.lastname, jos_comprofiler.firstname, gid," +
                        "  jos_comprofiler_field_values.ordering, jos_users.username " +
                        "FROM jos_users " +
                        "LEFT JOIN jos_comprofiler ON jos_users.id = jos_comprofiler.user_id " +
                        "LEFT JOIN jos_comprofiler_field_values ON jos_comprofiler_field_values.fieldtitle = jos_comprofiler.cb_gradelevel " +
                        "WHERE jos_users.username =  '" + joueur.obtenirNomUtilisateur() + "'");
                
                if (rs.next()) {
                    String prenom = rs.getString("lastname");
                    String nom = rs.getString("firstname");
                    cle = rs.getInt("id");
                    int gid = rs.getInt("gid");
                    int niveau = rs.getInt("ordering");
                    //xxString langue = rs.getString("short_name");

                    joueur.definirPrenom(prenom);
                    joueur.definirNomFamille(nom);
                    joueur.definirCleJoueur(cle);
                    //joueur.definirLangue(langue);
                    joueur.setRole(convertGIDToRole(gid));
                    joueur.definirCleNiveau(niveau);
                }
                rs.close();
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requête
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_remplir_info_joueur"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
       
    }//end methode

    private int convertGIDToRole(int gid)
    {
        switch(gid)
        {
            case 18: return 1; //normal users
            case 25: return 2; //admin
            case 31: return 3; //teacher
            default: return 1;
	}
    }
    /** This function queries the DB to find the player's musical preferences  ***
     * and returns a Vector containing URLs of MP3s the player might like
     * @param player
     * @return
     */
    public ArrayList<Object> obtenirListeURLsMusique(JoueurHumain player) {
        ArrayList<Object> liste = new ArrayList<Object>();

        String URLMusique = GestionnaireConfiguration.obtenirInstance().obtenirString("musique.url");
        String strRequeteSQL = "SELECT music_file.filename FROM music_file  WHERE  music_file.level_id = ";
        strRequeteSQL += player.obtenirCleJoueur();
        strRequeteSQL += ";";

        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery(strRequeteSQL);
                while (rs.next()) {
                    liste.add(URLMusique + rs.getString("filename"));
                }
                rs.close();
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requête
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
    public int ajouterInfosPartieTerminee(int room_id, String gameType, Date dateDebut, int dureePartie, int cleJoueurGagnant) {
        String strDate = mejFormatDate.format(dateDebut);
        String strHeure = mejFormatHeure.format(dateDebut);
        // Création du SQL pour l'ajout
        //System.out.println("Game type: " + gameType);
        int gameTypeId;
        if (gameType.equalsIgnoreCase("mathEnJeu"))
            gameTypeId = 1;
        else if (gameType.equalsIgnoreCase("Tournament"))
            gameTypeId = 2;
        else
            gameTypeId = 3;
        String strSQL = "INSERT INTO game(room_id, game_type_id, date, hour, duration,winner_id) " +
                "VALUES (" + room_id + "," + gameTypeId + ",'" + strDate + "','" + strHeure + "'," + dureePartie + "," + cleJoueurGagnant + ")";

        try {
            synchronized (DB_LOCK) {
                requete.executeUpdate(strSQL, Statement.RETURN_GENERATED_KEYS);
                // Aller chercher la clé de partie qu'on vient d'ajouter
                ResultSet rs = requete.getGeneratedKeys();
                rs.next();
                int gameId = rs.getInt("GENERATED_KEY");
                rs.close();
                return gameId;
            }
        } catch (Exception e) {
        	objLogger.error(GestionnaireMessages.message("bd.erreur_ajout_infos") + e.getMessage());
        }

        // Au cas où il y aurait erreur, on retourne -1
        return -1;
    }

    /**
     * Cette fonction permet d'ajouter les statisques concernant la partie dans
     * la BD.
     *
     * @param gameId Le game_id de la partie dont les statistiques seront contabilisées.
     * @param joueur L'objet contenant le joueur pour qui les statistique seront sauvegardees
     * @param estGagnant true si le joueur à gagné la partie, false sinon.
     */
    public void ajouterInfosJoueurPartieTerminee(int gameId, JoueurHumain joueur, boolean estGagnant) {
        boolean requiresFullStats = false;

        try {
            int roomId = joueur.obtenirPartieCourante().obtenirTable().getObjSalle().getRoomId();
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT requires_full_stats FROM room WHERE room_id=" + roomId);
                if (rs.next())
                    requiresFullStats = rs.getBoolean("requires_full_stats");
                rs.close();
            }
        } catch (Exception e) {
        	objLogger.error(GestionnaireMessages.message("bd.erreur_ajout_infos_update") + e.getMessage());
        }

        addSummaryStats(joueur, estGagnant);
        if (requiresFullStats)
            addFullStats(gameId, joueur);

        //updatePlayerLastAccessDate(joueur.obtenirCleJoueur());
        addInfoGameStatistics(gameId, joueur, estGagnant);

    }// end methode

    private void addFullStats(int gameId, JoueurHumain joueur) {
    	try {
    		InformationPartie infoPartie = joueur.obtenirPartieCourante();
    		LinkedList<InformationQuestion> questionsRepondues = infoPartie.obtenirListeQuestionsRepondues();
    		int userId = joueur.obtenirCleJoueur();
    		int pointage = infoPartie.obtenirPointage();
    		synchronized (DB_LOCK) {
    			//Remplir la table 'stats_game' une ligne par joueur pour chaque partie
    			//Ça peut faire beaucoup de lignes, donc on sauve ces lignes seulement
    			//pour les parties dans les salles de prof.
    			requete.executeUpdate(
    					"INSERT INTO gamestats_scores(game_id, user_id, score) " +
    					"VALUES (" + gameId + "," + userId + "," + pointage + ")");
    			//Remplir la table 'gamestats_questions' une ligne par question posee.
    			//Ça peut faire ÉNORMÉMENT de lignes, donc on sauve ces lignes seulement
    			//pour les parties dans les salles de prof.
    			PreparedStatement prepStatement = connexion.prepareStatement(
    					"INSERT INTO gamestats_questions (game_id,user_id,question_id,answer_status,time_taken) " +
    			"VALUES (?,?,?,?,?)");
    			prepStatement.setInt(1, gameId);
    			prepStatement.setInt(2, userId);
    			for (InformationQuestion iq: questionsRepondues) {
    				prepStatement.setInt(3, iq.obtenirQuestionId());
    				prepStatement.setShort(4, iq.obtenirValiditee());
    				prepStatement.setInt(5, iq.obtenirTempsRequis());
    				prepStatement.addBatch();
    			}
    			prepStatement.executeBatch();
    			prepStatement.close();
    		}
    	} catch (Exception e) {
        	objLogger.error(GestionnaireMessages.message("bd.erreur_ajout_infos_update") + e.getMessage());
        }
    }

    private void addSummaryStats(JoueurHumain joueur, boolean estGagnant) {
        InformationPartie infoPartie = joueur.obtenirPartieCourante();
        LinkedList<InformationQuestion> questionsRepondues = infoPartie.obtenirListeQuestionsRepondues();
        int roomId = infoPartie.obtenirTable().getObjSalle().getRoomId();

        try {
            synchronized (DB_LOCK) {
                //Remplir la table 'gamestatssummary_questions'.
                //Cette table contient un maximum de 3 lignes par questions (donc si la BD contient 1000
                //questions, cette table contient au plus 3000 lignes).  Si une question n'a jamais été
                //posée, elle contribue 0 ligne à la table, si elle a déjà été posée elle contribue entre
                //1 et 3 lignes dépendemment de la dernière fois qu'elle a été posée.  Les lignes
                //pour la question 'q' sont (q,1,...) (q,2,...) et (q,3,....)
                //        Chaque dimanche on enlève toutes les lignes (.,1,...)
                //        Chaque premier du mois on enlève toutes les lignes (.,2,....)
                //        Les lignes (.,3,....) ne sont jamais enlever
                //Cette technique permet d'avoir des statistiques pour la semaine, le mois et global
                //sans utiliser trop d'espace.
                PreparedStatement prepStatement = connexion.prepareStatement(
                        "INSERT INTO gamestats_summary_questions VALUES(?,?,?,1,?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "frequency=frequency+1," +
                        "freq_right=freq_right+?," +
                        "freq_wrong=freq_wrong+?," +
                        "time_taken=time_taken+?," +
                        "time_taken_right=time_taken_right+?," +
                        "time_taken_wrong=time_taken_wrong+?");
                int numR = 0;
                int numW = 0;
                int numQ = questionsRepondues.size();
                prepStatement.setInt(3, roomId);
                for (InformationQuestion iq: questionsRepondues) {
                    int right = iq.obtenirValiditee() == InformationQuestion.RIGHT_ANSWER ? 1 : 0;
                    int wrong = iq.obtenirValiditee() == InformationQuestion.WRONG_ANSWER ? 1 : 0;
                    numR += right;
                    numW += wrong;
                    int time = iq.obtenirTempsRequis();
                    int timeRight = right * time;
                    int timeWrong = wrong * time;
                    prepStatement.setInt(1, iq.obtenirQuestionId());
                    prepStatement.setInt(4, right);
                    prepStatement.setInt(5, wrong);
                    prepStatement.setInt(6, time);
                    prepStatement.setInt(7, timeRight);
                    prepStatement.setInt(8, timeWrong);
                    prepStatement.setInt(9, right);
                    prepStatement.setInt(10, wrong);
                    prepStatement.setInt(11, time);
                    prepStatement.setInt(12, timeRight);
                    prepStatement.setInt(13, timeWrong);
                    for (int i = 0; i < 3; i++) {
                        prepStatement.setInt(2, i);
                        prepStatement.addBatch();
                    }
                }
                prepStatement.executeBatch();
                prepStatement.close();

                //Remplir la table 'gamestatssummary_users'.
                //Cette table contient un maximum de 3 lignes par utilisateur (donc si la BD contient 1000
                //utilisateurs, cette table contient au plus 3000 lignes).  Si un utilisateur n'a jamais
                //joué, il contribue 0 ligne à la table, s'il a déjà joué il contribuera entre 1 et 3 lignes
                //dépendemment de la dernière fois qu'il a jouée.  Les lignes pour l'utilisateur 'u' sont
                //(u,1,...) (u,2,...) et (u,3,....)
                //        Chaque dimanche on enlève toutes les lignes (.,1,...)
                //        Chaque premier du mois on enlève toutes les lignes (.,2,....)
                //        Les lignes (.,3,....) ne sont jamais enlever
                //Cette technique permet d'avoir des statistiques pour la semaine, le mois et globale
                //sans utiliser trop d'espace.
                prepStatement = connexion.prepareStatement(
                        "INSERT INTO gamestats_summary_users VALUES(?,?,?,1,?,?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "games_played=games_played+1," +
                        "max_score=(max_score+?+abs(max_score-?))/2," +
                        "sum_score=sum_score+?," +
                        "num_won=num_won+?, " +
                        "num_questions=num_questions+?," +
                        "num_right=num_right+?," +
                        "num_wrong=num_wrong+?");
                int intGagnant = estGagnant ? 1 : 0;
                int userId = joueur.obtenirCleJoueur();
                int pointage = infoPartie.obtenirPointage();
                prepStatement.setInt(1, userId);
                prepStatement.setInt(3, roomId);
                prepStatement.setInt(4, pointage);
                prepStatement.setInt(5, pointage);
                prepStatement.setInt(6, intGagnant);
                prepStatement.setInt(7, numQ);
                prepStatement.setInt(8, numR);
                prepStatement.setInt(9, numW);
                prepStatement.setInt(10, pointage);
                prepStatement.setInt(11, pointage);
                prepStatement.setInt(12, pointage);
                prepStatement.setInt(13, intGagnant);
                prepStatement.setInt(14, numQ);
                prepStatement.setInt(15, numR);
                prepStatement.setInt(16, numW);
                for (int i = 0; i < 3; i++) {
                    prepStatement.setInt(2, i);
                    prepStatement.addBatch();
                }
                prepStatement.executeBatch();
                prepStatement.close();
            }

        } catch (Exception e) {
        	objLogger.error(GestionnaireMessages.message("bd.erreur_ajout_infos_update") + e.getMessage());
        }
    }
    

    /**
     * Cette fonction permet d'ajouter les informations sur une partie pour  ***
     * un joueur dans la table game_user;
     *
     */
    private void addInfoGameStatistics(int clePartie, JoueurHumain joueur, boolean gagner)
    {
    	int intGagner = 0;
    	if (gagner == true)
    	{
    		intGagner = 1;
    	}

    	int cleJoueur = joueur.obtenirCleJoueur();
    	int pointage = joueur.obtenirPartieCourante().obtenirPointage();
    	int room_id = joueur.obtenirPartieCourante().obtenirTable().getObjSalle().getRoomId();
    	String statistics = "";

    	double percents = 0.0;
		percents = joueur.obtenirPartieCourante().getRightAnswersStats();
		//System.out.println("percents " + percents);
    	
		LinkedList<InformationQuestion> questionsRepondues = joueur.obtenirPartieCourante().obtenirListeQuestionsRepondues();
		StringBuffer stats = new StringBuffer();
		for(InformationQuestion info: questionsRepondues)
		{
			// we don't put not answered questions - they will be used again in the future games
			if(info.answerStatus == info.RIGHT_ANSWER || info.answerStatus == info.WRONG_ANSWER)
			   stats.append(info.obtenirQuestionId() + ",");
		}
		if(stats.length() > 0)
		   stats.deleteCharAt(stats.length() - 1);		
		statistics = stats.toString();
		
		try
    	{

    		synchronized(DB_LOCK)
    		{
    			// Création du SQL pour l'ajout
    			String strSQL = "INSERT INTO game_user(game_id, user_id, score, has_won, questions_answers, room_id, stats) VALUES " +
    			"(" + clePartie + "," + cleJoueur + "," + pointage + "," + intGagner + ",'" + statistics + "'," + room_id + "," + percents + ");"; 
    			// Ajouter l'information pour ce joueur
    			requete.executeUpdate(strSQL);
       		}
    	}
    	catch (Exception e)
    	{
    		objLogger.error(GestionnaireMessages.message("bd.erreur_ajout_infos_game_user") + e.getMessage());
    	}


    }// end methode

    /**
     * Used to get from DB the user role_Id
     *
     * @param username
     * @param password
     * @return
     */
    public int getUserRole(String username, String password) {
        int gid = 0;
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT gid FROM jos_users WHERE username='" + username + "' AND password='" + password + "'");
                if (rs.next())
                    gid = rs.getInt("gid");
                rs.close();
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requête
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
        return convertGIDToRole(gid);
    }// end method

    public Map<String, Object> getRoomInfo(int roomId) throws SQLException {
        Map<String, Object> roomData = new TreeMap<String, Object>();
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery(
                        "SELECT room.password, jos_users.username, jos_users.gid, beginDate, endDate, masterTime, room_info.language_id,room_info.name,room_info.description " +
                        "FROM room_info, room, jos_users " +
                        "WHERE room.room_id = " + roomId + " " +
                        "AND room.room_id = room_info.room_id " +
                        "AND jos_users.id = room.user_id");
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
                        int gid = rs.getInt("gid");
                        roomData.put("roomType", convertGIDToRole(gid) == 3 ? "profsType" : "General");
                        roomData.put("names", names);
                        roomData.put("descriptions", descriptions);
                    }
                    int language_id = rs.getInt("language_id");
                    names.put(language_id, rs.getString("room_info.name"));
                    descriptions.put(language_id, rs.getString("room_info.description"));
                    row++;
                }
                rs.close();
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

    public Set<Integer> getRoomKeywordIds(int roomId) throws SQLException {
        Set<Integer> ids = new TreeSet<Integer>();
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT keyword_id FROM rooms_keywords WHERE room_id=" + roomId);
                while (rs.next()) {
                    ids.add(rs.getInt("keyword_id"));

                }
                rs.close();
            }
        } catch (SQLException e) {
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_room_keywordss"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
            throw (e);
        }
        return ids;
    }

    public Set<Integer> getRoomGameTypeIds(int roomId) throws SQLException {
        Set<Integer> types = new TreeSet<Integer>();
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT game_type_id " +
                        "FROM room_game_types " +
                        "WHERE room_id=" + roomId);
                while (rs.next()) {
                    types.add(rs.getInt("game_type_id"));

                }
                rs.close();
            }
        } catch (SQLException e) {
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_game_types"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
            throw (e);
        }
        return types;
    }

    /**
     * Méthode utilisé pour charger les salles
     */
    public void fillsRooms() {
        ArrayList<Integer> rooms = new ArrayList<Integer>();
        //find all rooms  and fill in ArrayList
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT room.room_id FROM room where (beginDate < NOW() AND endDate > NOW()) OR (beginDate is NULL AND endDate is NULL) OR (beginDate is NULL AND endDate > NOW()) OR (beginDate < NOW() AND endDate is NULL);");
                while (rs.next()) {
                    rooms.add(rs.getInt("room.room_id"));

                }
                rs.close();
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requête
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
    public void fillRoomList(ArrayList<Integer> roomIds) {
        synchronized (DB_LOCK) {
            for (int roomId: roomIds) {
                Salle objSalle;
                try {
                    Map<String, Object> roomData = getRoomInfo(roomId);
                    String strPassword = (String)roomData.get("password");
                    String strCreatorUsername = (String)roomData.get("username");
                    Date dateBeginDate = (Date)roomData.get("beginDate");
                    Date dateEndDate = (Date)roomData.get("endDate");
                    int intMasterTime = (Integer)roomData.get("masterTime");
                    Map<Integer, String> names = (Map<Integer, String>)roomData.get("names");
                    Map<Integer, String> descriptions = (Map<Integer, String>)roomData.get("descriptions");
                    String strRoomType = (String)roomData.get("roomType");
                    Set<Integer> kIds = getRoomKeywordIds(roomId);
                    Set<Integer> gtIds = getRoomGameTypeIds(roomId);
                    objSalle = new Salle(objControleurJeu, roomId, strPassword,
                            strCreatorUsername, strRoomType, dateBeginDate,
                            dateEndDate, intMasterTime, names, descriptions,
                            kIds, gtIds);
                    objControleurJeu.ajouterNouvelleSalle(objSalle);
                    objControleurJeu.preparerEvenementNouvelleSalle(objSalle);
                } catch (SQLException e) {
                    // Une erreur est survenue lors de la construction de la
                    // salle avec id 'roomId'
                    objLogger.error(GestionnaireMessages.message("bd.erreur_construction_salle"));
                    objLogger.error(GestionnaireMessages.message("bd.trace"));
                    objLogger.error(e.getMessage());
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    // Une erreur est survenue lors de la recherche de la
                    // prochaine salle
                    objLogger.error(GestionnaireMessages.message("bd.erreur_prochaine_salle"));
                    objLogger.error(GestionnaireMessages.message("bd.trace"));
                    objLogger.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    public Map<Integer,Integer> getNbTracks(Set<Integer> iDs)
    {
    	Map<Integer, Integer> nbTracksMap = new TreeMap<Integer, Integer>();
    	
    	for(Integer Id: iDs){
    		 try {
    	            synchronized (DB_LOCK) {
    	                ResultSet rs = requete.executeQuery("SELECT nbTracks FROM rule WHERE rule_id=" + Id);
    	                if (rs.next())
                        {
    	                    int nbTracks = rs.getInt("nbTracks");
    	                    nbTracksMap.put(Id, nbTracks);
    	                }
    	                rs.close();
    	            }
    	        } catch (SQLException e) {
    	            // Une erreur est survenue lors de l'exécution de la requête
    	            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_rules_NbTracks"));
    	            objLogger.error(GestionnaireMessages.message("bd.trace"));
    	            objLogger.error(e.getMessage());
    	            e.printStackTrace();
    	        }

    		
    	}
    	
		return nbTracksMap;
    	
    }

    /**
     * @param objReglesTable
     * @param gameType
     * @param roomId
     */
    public void chargerReglesTable(Regles objReglesTable, String gameType, int roomId) {

        int gameTypeID = 1; // default type - mathEnJeu
        if (gameType.equals("Tournament"))
            gameTypeID = 2;
        if (gameType.equals("Course"))
            gameTypeID = 3;
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
                rs.close();
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requête
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_rules_charging"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }

        // charger autres regles
        TreeSet<ReglesMagasin> magasins = objReglesTable.obtenirListeMagasinsPossibles();
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
                rst.close();
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requête
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
                rst.close();
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requête
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
                rst.close();
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requête
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
    }// fin méthode

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
                rst.close();
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requête
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
    // Une erreur est survenue lors de l'exécution de la requête
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
     * @param room_id The room_id of the room to update
     * @param password The password to access the room
     * @param names the name of the room in languages for which the room is available
     * @param descriptions the description of the room in languages for which the room is available
     * @param beginDate the date at which the room becomes available
     * @param endDate the date at which the room stops being available
     * @param masterTime the length of games in minutes for the room (0 means player's choice)
     * @param keywordIds the list of keywords associated with the room, the keywords tells what kind of questions will be asked in the room
     * @param gameTypeIds the type of games allowed in the room (classic,race,tournament)
     */
    public void updateRoom(int room_id, String password,
                           TreeMap<Integer, String> names, TreeMap<Integer, String> descriptions,
                           String beginDate, String endDate,
                           int masterTime,
                           String keywordIds, String gameTypeIds) {

        String strBeginDate = (beginDate == null || beginDate.isEmpty()) ? "NULL" : "'" + beginDate + "'";
        String strEndDate = (endDate == null || endDate.isEmpty()) ? "NULL" : "'" + endDate + "'";
        String strSQL = "UPDATE room SET " +
                "password='" + password + "'," +
                "beginDate=" + strBeginDate + "," +
                "endDate=" + strEndDate + "," +
                "masterTime=" + masterTime + " " +
                "WHERE room_id=" + room_id;
        try {
            synchronized (DB_LOCK) {
                // Ajouter l'information pour cette partie
                requete.executeUpdate(strSQL);
            }
        } catch (Exception e) {
        	objLogger.error(GestionnaireMessages.message("bd.erreur_adding_rooms_modProf") + e.getMessage());
        }

        //add information of the room to other tables of DB
        deleteAllAssociatedRoomInfo(room_id);
        for (Integer language_id: names.keySet())
            putNewRoomInfo(room_id, language_id, names.get(language_id), descriptions.get(language_id));
        putNewRoomGameTypes(room_id, gameTypeIds);
        putNewRoomKeywords(room_id, keywordIds);
        putNewRoomObjects(room_id, "1,2,3,7");
        putNewRoomShops(room_id, "1,2,3");

    }// end methode

    public void deleteAllAssociatedRoomInfo(int room_id) {
        try {
            synchronized (DB_LOCK) {
                // Ajouter l'information pour cette partie
                requete.executeUpdate("DELETE FROM room_info WHERE room_id=" + room_id);
                requete.executeUpdate("DELETE FROM rooms_keywords WHERE room_id=" + room_id);
                requete.executeUpdate("DELETE FROM room_game_types WHERE room_id=" + room_id);
                requete.executeUpdate("DELETE FROM room_shop WHERE room_id=" + room_id);
                requete.executeUpdate("DELETE FROM room_object WHERE room_id=" + room_id);
            }
        } catch (Exception e) {
        	objLogger.error(GestionnaireMessages.message("bd.erreur_delete_rooms_modProf") + e.getMessage());
        }
    }

    /**
     * Method used to put new room in DB from room created in profModule
     * put it in room table
     * @param password The password to access the room
     * @param user_id the user_id for the creator of the room
     * @param names the name of the room in languages for which the room is available
     * @param descriptions the description of the room in languages for which the room is available
     * @param beginDate the date at which the room becomes available
     * @param endDate the date at which the room stops being available
     * @param masterTime the length of games in minutes for the room (0 means player's choice)
     * @param fullStats whether to record full stats (for profs) or just a summary (for regular rooms)
     * @param keywordIds the list of keywords associated with the room, the keywords tells what kind of questions will be asked in the room
     * @param gameTypeIds the type of games allowed in the room (classic,race,tournament)
     * @return the room_id of the newly created room
     */
    public int putNewRoom(String password, int user_id,
                          TreeMap<Integer, String> names, TreeMap<Integer, String> descriptions,
                          String beginDate, String endDate,
                          int masterTime, boolean fullStats,
                          String keywordIds, String gameTypeIds) {

        int room_id = 0;
        String strBeginDate = (beginDate == null || beginDate.isEmpty()) ? "NULL" : "'" + beginDate + "'";
        String strEndDate = (endDate == null || endDate.isEmpty()) ? "NULL" : "'" + endDate + "'";

        String strSQL = "INSERT INTO room (password, user_id, rule_id, beginDate, endDate, masterTime, requires_full_stats) VALUES ('" +
                password + "'," +
                user_id + ",1," +
                strBeginDate + "," +
                strEndDate + "," +
                masterTime + "," +
                (fullStats?"1":"0") + ")";
        try {
            synchronized (DB_LOCK) {
                // Ajouter l'information pour cette partie
                requete.executeUpdate(strSQL, Statement.RETURN_GENERATED_KEYS);

                // Aller chercher la clé de la salle qu'on vient d'ajouter
                ResultSet rs = requete.getGeneratedKeys();

                // On retourne la clé de partie
                rs.next();
                room_id = rs.getInt("GENERATED_KEY");
                rs.close();
            }
        } catch (Exception e) {
        	objLogger.error(GestionnaireMessages.message("bd.erreur_adding_rooms_modProf") + e.getMessage());
        }

        //add information of the room to other tables of DB
        for (Integer language_id: names.keySet())
            putNewRoomInfo(room_id, language_id, names.get(language_id), descriptions.get(language_id));
        putNewRoomGameTypes(room_id, gameTypeIds);
        putNewRoomKeywords(room_id, keywordIds);
        putNewRoomObjects(room_id, "1,2,3,7");
        putNewRoomShops(room_id, "1,2,3");

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
            prepStatement.close();

        } catch (Exception e) {
        	objLogger.error(GestionnaireMessages.message("bd.erreur_adding_rooms_Keywords") + e.getMessage());
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
            prepStatement.close();

        } catch (Exception e) {
        	objLogger.error(GestionnaireMessages.message("bd.erreur_adding_rooms_gameTypes") + e.getMessage());
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
        	objLogger.error(GestionnaireMessages.message("bd.erreur_adding_rooms_infosTable") + e.getMessage());
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
            prepStatement.close();

        } catch (Exception e) {
        	objLogger.error(GestionnaireMessages.message("bd.erreur_adding_rooms_specialSquare") + e.getMessage());
        }


    }// end methode

    /**
     * Method satellite to putNewRoom() used to put new room in DB from room created in profModule
     * put infos in room_object table
     * @throws SQLException
     */
    private void putNewRoomObjects(int room_id, String objectIds) {
        ArrayList<Integer> objects = new ArrayList<Integer>();
        StringTokenizer objectsST = new StringTokenizer(objectIds, ",");

        while (objectsST.hasMoreTokens()) {
            objects.add(Integer.parseInt(objectsST.nextToken()));
        }

        int length = objects.size();
        // Création du SQL pour l'ajout
        PreparedStatement prepStatement = null;
        try {
            if (length > 0) {
                prepStatement = connexion.prepareStatement("INSERT INTO room_object (room_id, object_id, priority) VALUES ( ? , ?, ?)");
                for (int i = 0; i < length; i++) {
                    // Ajouter l'information pour cette salle
                    prepStatement.setInt(1, room_id);
                    prepStatement.setInt(2, objects.get(i));
                    prepStatement.setInt(3, (i + 1));
                    prepStatement.addBatch();
                }
                prepStatement.executeBatch();
                prepStatement.close();
            }
        } catch (Exception e) {
        	objLogger.error(GestionnaireMessages.message("bd.erreur_adding_rooms_objects") + e.getMessage());
        }
    }// end methode

    /**
     * Method satellite to putNewRoom() used to put new room in DB from room created in profModule
     * put infos in room_shop table
     * @throws SQLException
     */
    private void putNewRoomShops(int room_id, String shopIds) {
        ArrayList<Integer> shops = new ArrayList<Integer>();
        StringTokenizer shopsST = new StringTokenizer(shopIds, ",");

        while (shopsST.hasMoreTokens()) {
            shops.add(Integer.parseInt(shopsST.nextToken()));
        }

        int length = shops.size();
        // Création du SQL pour l'ajout
        PreparedStatement prepStatement = null;
        try {
            if (length > 0) {
                prepStatement = connexion.prepareStatement("INSERT INTO room_shop (room_id, shop_id, priority) VALUES ( ? , ?, ?)");
                for (int i = 0; i < length; i++) {
                    // Ajouter l'information pour cette salle
                    prepStatement.setInt(1, room_id);
                    prepStatement.setInt(2, shops.get(i));
                    prepStatement.setInt(3, (i + 1));
                    prepStatement.addBatch();//executeUpdate();
                }
                prepStatement.executeBatch();
                prepStatement.close();
            }
        } catch (Exception e) {
        	objLogger.error(GestionnaireMessages.message("bd.erreur_adding_rooms_shops") + e.getMessage());
        }
    }// end methode

    //Delete the room with the specified id from the DB.  We return true if the operation deleted a
    //row from the DB, false otherwise.
    //NOTE:  Because we use InnoDB tables the information associated with the room will be automatically
    //       deleted from the DB so we don't have to worry about deleting the relevant entries in other
    //       tables like room_object, room_info, etc.  This is because we have FOREIGN KEYS in the DB
    //       and we specified ON DELETE CASCADE.
    public boolean deleteRoom(int room_id) {
        int numDeleted = 0;
        try {
            synchronized (DB_LOCK) {
                numDeleted = requete.executeUpdate("DELETE FROM room WHERE room_id=" + room_id);
            }
        } catch (SQLException e) {
            System.out.println(GestionnaireMessages.message("bd.erreur_deleting_room") + e.getMessage());
        }
        return numDeleted != 0;
    }
    //******************************************************************

    public RapportDeSalle createRoomReport(int room_id) {
        boolean requiresFullStats = false;
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT requires_full_stats FROM room WHERE room_id=" + room_id);
                if (rs.next())
                    requiresFullStats = rs.getBoolean("requires_full_stats");
                rs.close();
            }
        } catch (Exception e) {
        	objLogger.error(GestionnaireMessages.message("bd.erreur_ajout_infos_update") + e.getMessage());
        }
        if (requiresFullStats)
            return createRoomFullReport(room_id);
        return createRoomSummaryReport(room_id);
    }
    /**
     * Methode used to create the report for a room
     * used for the moduleProf
     * @param room_id the room_id for the room asking for a report
     * @return An object filled with the data required to build the report.  This
     *         object also contains method that will produce the XML code to
     *         send to the client.
     */
    public RapportDeSalle createRoomFullReport(int room_id) {
        RapportDeSalle rapport = new RapportDeSalle(room_id, RapportDeSalle.ReportType.FULL);
        try {
            synchronized (DB_LOCK) {
                String strSQL = "SELECT game.date,game.game_type_id,game.duration,game.winner_id,gamestats_questions.*,gamestats_scores.score, jos_comprofiler.lastname, jos_comprofiler.firstname, question_info.question_flash_file, question_info.feedback_flash_file, question_info.language_id " +
                        "FROM game, gamestats_questions " +
                        "LEFT JOIN jos_comprofiler ON jos_comprofiler.user_id = gamestats_questions.user_id " +
                        "LEFT JOIN question_info ON question_info.question_id=gamestats_questions.question_id " +
                        "LEFT JOIN gamestats_scores ON gamestats_scores.game_id=gamestats_questions.game_id AND gamestats_scores.user_id=gamestats_questions.user_id " +
                        "WHERE game.room_id=" + room_id + " " +
                        "AND gamestats_questions.game_id=game.game_id";
                ResultSet rs = requete.executeQuery(strSQL);
                while (rs.next()) {
                    String date = mejFormatDate.format(rs.getDate("game.date"));
                    int game_type_id = rs.getInt("game.game_type_id");
                    int duration = rs.getInt("game.duration");
                    int winner_id = rs.getInt("game.winner_id");
                    int game_id = rs.getInt("gamestats_questions.game_id");
                    int user_id = rs.getInt("gamestats_questions.user_id");
                    int question_id = rs.getInt("gamestats_questions.question_id");
                    short answer_status = rs.getShort("gamestats_questions.answer_status");
                    int time_taken = rs.getInt("gamestats_questions.time_taken");
                    int score = rs.getInt("gamestats_scores.score");
                    String lastname = rs.getString("lastname");
                    String firstname = rs.getString("firstname");
                    String q_swf = rs.getString("question_info.question_flash_file");
                    String f_swf = rs.getString("question_info.feedback_falsh_file");
                    int language_id = rs.getInt("question_info.language_id");
                    
                    rapport.addPlayerInfo(user_id, firstname, lastname);
                    rapport.addQuestionSWF(question_id, language_id, q_swf, f_swf);
                    rapport.addGameInfo(game_id, date, game_type_id, winner_id, duration, user_id, question_id, answer_status, time_taken, score);
                }
                rs.close();
            }
        } catch (Exception e) {
        	objLogger.error(GestionnaireMessages.message("bd.erreur_create_report") + e.getMessage());
        }
        return rapport;

    }// end methode

    public RapportDeSalle createRoomSummaryReport(int room_id) {
        RapportDeSalle rapport = new RapportDeSalle(room_id, RapportDeSalle.ReportType.SUMMARY);
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT * FROM gamestats_summary_questions WHERE room_id="+room_id);
                while (rs.next()) {
                    int question_id = rs.getInt("question_id");
                    int time_period = rs.getInt("time_period");
                    int frequency = rs.getInt("frequency");
                    int freq_right = rs.getInt("freq_right");
                    int freq_wrong = rs.getInt("freq_wrong");
                    int time_taken = rs.getInt("time_taken");
                    int time_taken_right = rs.getInt("time_taken_right");
                    int time_taken_wrong = rs.getInt("time_taken_wrong");
                    rapport.addQuestionSummary(question_id, time_period, frequency, freq_right, freq_wrong, time_taken, time_taken_right, time_taken_wrong);
                }
                rs.close();
                rs = requete.executeQuery("SELECT question_info.question_id, question_info.language_id, question_info.question_flash_file, question_info.feedback_flash_file " +
                        "FROM question_info,gamestats_summary_questions " +
                        "WHERE question_info.question_id = gamestats_summary_questions.question_id " +
                        "AND gamestats_summary_questions.room_id="+room_id);
                while (rs.next()) {
                    int question_id = rs.getInt("question_info.question_id");
                    int language_id = rs.getInt("question_info.language_id");
                    String q_swf = rs.getString("question_info.question_flash_file");
                    String fb_swf = rs.getString("question_info.feedback_flash_file");
                    rapport.addQuestionSWF(question_id, language_id, q_swf, fb_swf);
                }
                rs.close();
                rs = requete.executeQuery("SELECT gamestats_summary_users.*,jos_comprofiler.firstname,jos_comprofiler.lastname " +
                        "FROM gamestats_summary_users " +
                        "LEFT JOIN jos_comprofiler ON jos_comprofiler.user_id = gamestats_summary_users.user_id " +
                        "WHERE room_id="+room_id);
                while (rs.next()) {
                    int user_id = rs.getInt("gamestats_summary_users.user_id");
                    int time_period = rs.getInt("gamestats_summary_users.time_period");
                    int games_played = rs.getInt("gamestats_summary_users.games_played");
                    int max_score = rs.getInt("gamestats_summary_users.max_score");
                    int sum_score = rs.getInt("gamestats_summary_users.sum_score");
                    int num_won = rs.getInt("gamestats_summary_users.num_won");
                    int num_questions = rs.getInt("gamestats_summary_users.num_questions");
                    int num_right = rs.getInt("gamestats_summary_users.num_right");
                    int num_wrong = rs.getInt("gamestats_summary_users.num_wrong");
                    String firstname = rs.getString("jos_comprofiler.firstname");
                    String lastname = rs.getString("jos_comprofiler.lastname");
                    rapport.addPlayerInfo(user_id, firstname, lastname);
                    rapport.addPlayerSummary(user_id, time_period, games_played, max_score, sum_score, num_won, num_questions, num_right, num_wrong);
                }
                rs.close();
            }
        } catch (Exception e) {
        	objLogger.error(GestionnaireMessages.message("bd.erreur_create_report") + e.getMessage());

        }
        return rapport;
    }
    
    public String controlPWD(String clientPWD) {
        String encodedPWD = "";
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT PASSWORD('" + clientPWD + "') AS password;");

                if (rs.next())
                    encodedPWD = rs.getString("password");
                rs.close();
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requête
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
        	objLogger.error(GestionnaireMessages.message("bd.erreur_adding_questions_errors") + e.getMessage());
        }


    }// end methode

    /**
     * Generates a map of keywords
     *        language_id --> [keyword_id->keyword_name,group_id,group_name]
     * @return the map described in the method's description
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
                    kmap.put(kid, kname + "," + gid + "," + gname);
                }
                
                rs.close();
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requête
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
     * @return The map described in the method's description.
     */
    public TreeMap<Integer, TreeMap<Integer, String>> getLanguagesMap() {
        TreeMap<Integer, TreeMap<Integer, String>> languages = new TreeMap<Integer, TreeMap<Integer, String>>();
        try {
            synchronized (DB_LOCK) {
                ResultSet rs = requete.executeQuery("SELECT language.short_name,language_info.* " +
                        "FROM language LEFT JOIN language_info ON language.language_id=language_info.language_id");
                languages.put(0, new TreeMap<Integer, String>());
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
                rs.close();
           }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requête
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
     * @return the map of game_type_id to name for all game types in the DB.
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
                
                rs.close();                
            }
        } catch (SQLException e) {
            // Une erreur est survenue lors de l'exécution de la requête
            objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_creer_gameType_map"));
            objLogger.error(GestionnaireMessages.message("bd.trace"));
            objLogger.error(e.getMessage());
            e.printStackTrace();
        }
        return gameTypes;
    } // end method
    
    
    
}// end class

