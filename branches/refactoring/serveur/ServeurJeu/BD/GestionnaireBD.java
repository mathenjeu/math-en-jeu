package ServeurJeu.BD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import Enumerations.Visibilite;
import ServeurJeu.ControleurJeu;
import ServeurJeu.ComposantesJeu.BoiteQuestions;
import ServeurJeu.ComposantesJeu.Langue;
import ServeurJeu.ComposantesJeu.Question;
import ServeurJeu.ComposantesJeu.Salle;
import ServeurJeu.ComposantesJeu.Joueurs.Joueur;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesCaseCouleur;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesCaseSpeciale;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesComparator;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesMagasin;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesObjetUtilisable;
import ServeurJeu.Configuration.GestionnaireConfiguration;
import ServeurJeu.Configuration.GestionnaireMessages;


/**
 * @author Jean-François Brind'Amour
 */
public class GestionnaireBD 
{


  static private Logger objLogger = Logger.getLogger( GestionnaireBD.class );

  private static final String strValeurGroupeAge = "valeurGroupeAge";

  private static Connection mConnection;
  
  public GestionnaireBD(Connection pConnection) {
    mConnection = pConnection;
  }

  


  /**
   * Cette fonction permet de chercher dans la BD si le joueur dont le nom
   * d'utilisateur et le mot de passe sont passés en paramètres existe.
   * 
   * @param String nomUtilisateur : Le nom d'utilisateur du joueur
   * @param String motDePasse : Le mot de passe du joueur
   * @return true  : si le joueur existe et que son mot de passe est correct
   *       false : si le joueur n'existe pas ou que son mot de passe n'est 
   *           pas correct
   */
  public boolean joueurExiste(String nomUtilisateur, String motDePasse)
  {

    GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
    String codeErreur = config.obtenirString( "gestionnairebd.code_erreur_inactivite" );

    int count=0;  //compteur du nombre d'essai de la requête

    //boucler la requête jusqu'à 5 fois si la connexion à MySQL
    //a été interrompu du à un manque d'activité de la connexion
    while(count<5)
    {
      try
      {
        if(count!=0)
        {
          //connexionDB();
        }
        //synchronized( requete )
        //{
          Statement stmt = mConnection.createStatement();
          ResultSet rs = stmt.executeQuery("SELECT * FROM joueur WHERE alias = '" + nomUtilisateur + "' AND motDePasse = '" + motDePasse + "';");
          return rs.next();
        //}
      }
      catch (SQLException e)
      {
        //on vérifie l'état de l'exception 
        //si l'état est égal au codeErreur
        //on peut réesayer la connexion
        if(e.getSQLState().equals(codeErreur))
        {
          count++;
        }
        else
        {
          // Une erreur est survenue lors de l'exécution de la requête
          objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
          objLogger.error(GestionnaireMessages.message("bd.trace"));
          objLogger.error( e.getMessage() );
          e.printStackTrace();
          return false; 
        }
      }
    }
    return false;
  }

  /**
   * Cette fonction permet de chercher dans la BD le joueur et de remplir
   * les champs restants du joueur.
   * 
   * @param JoueurHumain joueur : Le joueur duquel il faut trouver les
   *                informations et les définir dans l'objet
   */
  public void remplirInformationsJoueur(JoueurHumain joueur)
  {

    String lSql = "SELECT l.id as langue_id, l.nom as langue_nom, l.nom_court, cleJoueur, prenom, j.nom, cleNiveau, peutCreerSalles " +
    " FROM joueur j, langues l " +
    " WHERE alias = ? and j.cleLangue = l.id;";

    try {

      PreparedStatement requete = mConnection.prepareStatement(lSql);
      requete.setString(1, joueur.obtenirNomUtilisateur());

      ResultSet rs = requete.executeQuery();
      if (rs.next())
      {
        if (rs.getInt("peutCreerSalles") != 0)
        {
          joueur.definirPeutCreerSalles(true);
        }
        String prenom = rs.getString("prenom");
        String nom = rs.getString("nom");
        Langue lLangue = new Langue(rs.getInt("langue_id"),rs.getString("langue_nom"),rs.getString("nom_court"));
        int cle = Integer.parseInt(rs.getString("cleJoueur"));
        String cleNiveau = rs.getString( "cleNiveau" );
        joueur.definirPrenom(prenom);
        joueur.definirNomFamille(nom);
        joueur.definirCleJoueur(cle);
        joueur.definirCleNiveau( cleNiveau );
        joueur.setLangue(lLangue);
      }

    } catch (SQLException e) {
      // Une erreur est survenue lors de l'exécution de la requête
      objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
      objLogger.error(GestionnaireMessages.message("bd.trace"));
      objLogger.error( e.getMessage() );
      e.printStackTrace();      
    }
  }

  
  /**
   * Load all the question for this player questions box.
   * @param pJoueur the current player
   * @param boiteQuestions the questions box.
   */
  private void remplirBoiteQuestionAll(JoueurHumain pJoueur, BoiteQuestions boiteQuestions) {
    String lUrl = GestionnaireConfiguration.obtenirInstance().obtenirString("controleurjeu.url-question");
    
    String lSql = "SELECT distinct q.*, qd.*, tr.nomType FROM question q, question_details qd, langues l, typereponse tr " +
      "where q.cleQuestion = qd.question_id and qd.langue_id = l.id and l.nom_court = ? " +
      "and tr.cleType = q.typeReponse and qd.valide = 1 " +
      "and qd.FichierFlashQuestion is not NULL and qd.FichierFlashReponse is not NULL " +
      "and q.cleQuestion = qd.id " +
      "and q.valeurGroupeAge" + pJoueur.obtenirCleNiveau() + " > 0";
    
    PreparedStatement lStatement = null;
    
    try {

      lStatement = mConnection.prepareStatement(lSql);
      lStatement.setString(1, pJoueur.getLangue().getNomCourt());
      ResultSet rs = lStatement.executeQuery();
      
      while(rs.next()) {
        int codeQuestion = rs.getInt("cleQuestion");
        String typeQuestion = rs.getString( "nomType" );
        String question = rs.getString( "FichierFlashQuestion" );
        String reponse = rs.getString("bonneReponse");
        String explication = rs.getString("FichierFlashReponse");
        int difficulte = rs.getInt( strValeurGroupeAge + pJoueur.obtenirCleNiveau() );
        boiteQuestions.ajouterQuestion(new Question(codeQuestion, typeQuestion, difficulte, lUrl+question, reponse, lUrl+explication));
      }
      
      
    } catch (SQLException e) {
      // Une erreur est survenue lors de l'exécution de la requête
      objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
      objLogger.error(GestionnaireMessages.message("bd.trace"));
      objLogger.error( e.getMessage() );  
    } finally {
      if (lStatement != null) {
        try {
          lStatement.close();
        } catch (SQLException e) {
          objLogger.log(Level.FATAL, e.getMessage(), e);
        }
      }
    }
  }
  
  /**
   * Load a question box for the player
   * @param pJoueur the player to load the questions for
   * @param boiteQuestions the box to load the questions into
   */
  public void remplirBoiteQuestions( JoueurHumain pJoueur, BoiteQuestions boiteQuestions) {
    
    String lUrl = GestionnaireConfiguration.obtenirInstance().obtenirString("controleurjeu.url-question");
    
    String lSql = "SELECT distinct q.*, qd.*, tr.nomType FROM question q, question_details qd, langues l, typereponse tr " +
                  "where " +
                     "(q.cleQuestion in (select qgq.question_id " +
                           "from questions_groups_questions qgq, questions_groups qg, questions_groups_salles qgs, salles s " +
                           "where qgq.questions_group_id = qg.id and qgs.questions_group_id = qg.id and qgs.salle_id = s.id and s.id = ?) " +
                  "or q.cleQuestion in (select sq.question_id " +
                           "from salles s, salles_question sq " +
                           "where s.id = sq.salle_id and s.id = ?)) " +
                  "and q.cleQuestion = qd.question_id and qd.langue_id = l.id and l.nom_court = ? " +
                  "and tr.cleType = q.typeReponse and qd.valide = 1 " +
                  "and qd.FichierFlashQuestion is not NULL and qd.FichierFlashReponse is not NULL " +
                  "and q.cleQuestion = qd.id " +
                  "and q.valeurGroupeAge" + pJoueur.obtenirCleNiveau() + " > 0";
    
    PreparedStatement lStatement = null;
    
    try {

      lStatement = mConnection.prepareStatement(lSql);
      lStatement.setInt(1, pJoueur.obtenirSalleCourante().getId());
      lStatement.setInt(2, pJoueur.obtenirSalleCourante().getId());
      lStatement.setString(3, pJoueur.getLangue().getNomCourt());


      ResultSet rs = lStatement.executeQuery();
      boolean lHasSome = false;
      while(rs.next())
      {
        int codeQuestion = rs.getInt("cleQuestion");
        String typeQuestion = rs.getString( "nomType" );
        String question = rs.getString( "FichierFlashQuestion" );
        String reponse = rs.getString("bonneReponse");
        String explication = rs.getString("FichierFlashReponse");
        int difficulte = rs.getInt( strValeurGroupeAge + pJoueur.obtenirCleNiveau() );
        lHasSome = true;
        boiteQuestions.ajouterQuestion(new Question(codeQuestion, typeQuestion, difficulte, lUrl+question, reponse, lUrl+explication));
      }

      if (!lHasSome) {
        remplirBoiteQuestionAll(pJoueur,boiteQuestions);
      }

    } catch (SQLException e) {
      // Une erreur est survenue lors de l'exécution de la requête
      objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
      objLogger.error(GestionnaireMessages.message("bd.trace"));
      objLogger.error( e.getMessage() );
      e.printStackTrace();      
    } catch( RuntimeException e) {
      //Une erreur est survenue lors de la recherche de la prochaine question
      objLogger.error(GestionnaireMessages.message("bd.erreur_prochaine_question"));
      objLogger.error(GestionnaireMessages.message("bd.trace"));
      objLogger.error( e.getMessage() );
      e.printStackTrace();
    } finally {
      if (lStatement != null) {
        try {
          lStatement.close();
        } catch (SQLException e) {
          objLogger.log(Level.FATAL, e.getMessage(), e);
        }
      }
    }

  }



  /**
   * This function queries the DB to find the player's musical preferences
   * and returns a Vector containing URLs of MP3s the player might like
   * @param cleJoueur the user id
   * @return a list of url to mp3 files
   */
  public Vector obtenirListeURLsMusique(int cleJoueur)
  {
    

    Vector<String> liste = new Vector<String>();
    
    String URLMusique = GestionnaireConfiguration.obtenirInstance().obtenirString("musique.url");
    String strRequeteSQL = "SELECT musique_Fichiers.nomFichier FROM musique_Fichiers,musique_Fichiers_Categories,musique_Categories,musique_Categorie_Joueur WHERE ";
    strRequeteSQL       += "musique_Fichiers.cleFichier = musique_Fichiers_Categories.cleFichier AND ";
    strRequeteSQL       += "musique_Fichiers_Categories.cleCategorie = musique_Categories.cleCategorie AND ";
    strRequeteSQL       += "musique_Categories.cleCategorie = musique_Categorie_Joueur.cleCategorie AND ";
    strRequeteSQL       += "musique_Categorie_Joueur.cleJoueur = " + Integer.toString(cleJoueur);
    try
    {
      Statement requete = mConnection.createStatement();

        ResultSet rs = requete.executeQuery(strRequeteSQL);
        while(rs.next())
        {
          liste.add(URLMusique + rs.getString("nomFichier"));
        }
      
    }
    catch (SQLException e)
    {
      // Une erreur est survenue lors de l'exécution de la requête
      objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
      objLogger.error(GestionnaireMessages.message("bd.trace"));
      objLogger.error( e.getMessage() );
      e.printStackTrace();      
    }
    catch( RuntimeException e)
    {
      // Ce n'est pas le bon message d'erreur mais ce n'est pas grave
      objLogger.error(GestionnaireMessages.message("bd.error_music"));
      objLogger.error(GestionnaireMessages.message("bd.trace"));
      objLogger.error( e.getMessage() );
      e.printStackTrace();
    }
    return liste;
  }

  // This method updates a player's information in the DB
  public void mettreAJourJoueur( JoueurHumain joueur, int tempsTotal )
  {
    try
    {
      Statement requete = mConnection.createStatement();

        ResultSet rs = requete.executeQuery("SELECT partiesCompletes, meilleurPointage, tempsPartie FROM joueur WHERE alias = '" + joueur.obtenirNomUtilisateur() + "';");
        if (rs.next())
        {
          int partiesCompletes = rs.getInt( "partiesCompletes" ) + 1;
          int meilleurPointage = rs.getInt( "meilleurPointage" );
          int pointageActuel = joueur.obtenirPartieCourante().obtenirPointage();
          if( meilleurPointage < pointageActuel )
          {
            meilleurPointage = pointageActuel;
          }

          int tempsPartie = tempsTotal + rs.getInt("tempsPartie");

          //mise-a-jour
          int result = requete.executeUpdate( "UPDATE joueur SET partiesCompletes=" + partiesCompletes + ",meilleurPointage=" + meilleurPointage + ",tempsPartie=" + tempsPartie + " WHERE alias = '" + joueur.obtenirNomUtilisateur() + "';");
        }
    }
    catch (SQLException e)
    {
      // Une erreur est survenue lors de l'exécution de la requête
      objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
      objLogger.error(GestionnaireMessages.message("bd.trace"));
      objLogger.error( e.getMessage() );
      e.printStackTrace();      
    }
  }


  /* Cette fonction permet d'ajouter les information sur une partie dans 
   * la base de données dans la table partie. 
   *
   * Retour: la clé de partie qui servira pour la table partieJoueur
   */
  public int ajouterInfosPartiePartieTerminee(Date dateDebut, int dureePartie)
  {

    SimpleDateFormat objFormatDate = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat objFormatHeure = new SimpleDateFormat("HH:mm:ss");

    String strDate = objFormatDate.format(dateDebut);
    String strHeure = objFormatHeure.format(dateDebut);

    // Création du SQL pour l'ajout
    String strSQL = "INSERT INTO partie(datePartie, heurePartie, dureePartie) VALUES ('" + 
    strDate + "','" + strHeure + "'," + dureePartie + ")";

    try
    {
      Statement requete = mConnection.createStatement();


        // Ajouter l'information pour cette partie
        requete.executeUpdate(strSQL, Statement.RETURN_GENERATED_KEYS);

        // Aller chercher la clé de partie qu'on vient d'ajouter
        ResultSet  rs = requete.getGeneratedKeys();

        // On retourne la clé de partie
        rs.next();
        return Integer.parseInt(rs.getString("GENERATED_KEY"));
      
    }
    catch (Exception e)
    {
      System.out.println(GestionnaireMessages.message("bd.erreur_ajout_infos") + e.getMessage());
    }

    // Au cas où il y aurait erreur, on retourne -1
    return -1;
  }

  /* Cette fonction permet d'ajouter les informations sur une partie pour
   * un joueur dans la table partieJoueur;
   *
   */
  public void ajouterInfosJoueurPartieTerminee(int clePartie, int cleJoueur, int pointage, boolean gagner)
  {
    int intGagner = 0;
    if (gagner == true)
    {
      intGagner = 1;
    }

    // Création du SQL pour l'ajout
    String strSQL = "INSERT INTO partiejoueur(clePartie, cleJoueur, pointage, gagner) VALUES " +
    "(" + clePartie + "," + cleJoueur + "," + pointage + "," + intGagner + ");";

    try
    {

      Statement requete = mConnection.createStatement();

        // Ajouter l'information pour ce joueur
        requete.executeUpdate(strSQL);
    }
    catch (Exception e)
    {
    }
  }
  
  /**
   * Load a langue given its short name
   * @param pShortName the short name of a langage (ex : fr,en,jp
   * @return a langue object
   */
  public Langue loadLangue(String pShortName) {
    Langue lResult = null;
    
    String lSql = "select * from langues where nom_court = '" + pShortName + "'";
    
    try {
      Statement requete = mConnection.createStatement();

        ResultSet lRs = requete.executeQuery(lSql);
        
        if (lRs.next()) {
          lResult = new Langue(lRs.getInt("id"), lRs.getString("nom"),lRs.getString("nom_court"));
          
        }

    } catch (SQLException e) {
      objLogger.log(Level.FATAL, e.getMessage(), e);
    }
    
    return lResult;
  }
    
  
  /**
   * Add the rules for the color squares and return the modified object
   * @param pRules
   * @return the modified TreeSet containing the rules for the color squares
   */
  private TreeSet<ReglesCaseCouleur> loadRuleColorSquare(Regles pRules) {
    
    TreeSet<ReglesCaseCouleur> lResult = pRules.obtenirListeCasesCouleurPossibles();
    
    Statement lStatement = null;
    String lSql = "select * from regles_case_couleur";
    try {
      
      lStatement = mConnection.createStatement();
      ResultSet lRs = lStatement.executeQuery(lSql);
        
      while (lRs.next()) {
        lResult.add(new ReglesCaseCouleur(lRs.getInt("priorite"), lRs.getInt("type")));
      }

    } catch (SQLException e) {
      objLogger.log(Level.FATAL, e.getMessage(), e);
    } finally {
      if (lStatement != null) {
        try {
          lStatement.close();
        } catch (SQLException e) {
          objLogger.log(Level.FATAL, e.getMessage(), e);
        }
      }
    }
    
    return lResult;
  }
  
  /**
   * Add the rules for the special squares and return the modified object
   * @param pRules
   * @return the modified TreeSet containing the rules for the special squares
   */
  private TreeSet<ReglesCaseSpeciale> loadRuleSpecialSquare(Regles pRules) {
        
    TreeSet<ReglesCaseSpeciale> lResult = pRules.obtenirListeCasesSpecialesPossibles();
    
    Statement lStatement = null;
    String lSql = "select * from regles_case_special";
    try {
      lStatement = mConnection.createStatement();
      ResultSet lRs = lStatement.executeQuery(lSql);

      while (lRs.next()) {
        lResult.add(new ReglesCaseSpeciale(lRs.getInt("priorite"), lRs.getInt("type")));
      }
      
    } catch (SQLException e) {
      objLogger.log(Level.FATAL, e.getMessage(), e);
    } finally {
      if (lStatement != null) {
        try {
          lStatement.close();
        } catch (SQLException e) {
          objLogger.log(Level.FATAL, e.getMessage(), e);
        }
      }
    }
    
    return lResult;
  }
  
  
  private void loadShopObjects(ReglesMagasin lReglesMagasin, int pShopId) {
    
    PreparedStatement lStatement = null;
    
    String lSql = "select o.tag from objets o, magasins_objets_utilisable mo, magasins m " +
      " where o.id = mo.id and mo.magasin_id = m.id and m.id = ?";
  
    
    try {
      lStatement = mConnection.prepareStatement(lSql);
      lStatement.setInt(1, pShopId);

      ResultSet lResultSet = lStatement.executeQuery();
      while (lResultSet.next()) {
        lReglesMagasin.ajouterReglesObjetUtilisable(new ReglesObjetUtilisable(lResultSet.getString("tag"), Visibilite.Aleatoire));
      }
      
    } catch (SQLException e) {
      objLogger.log(Level.FATAL, e.getMessage(), e);
    } finally {
      if (lStatement != null) {
        try {
          lStatement.close();
        } catch (SQLException e) {
          objLogger.log(Level.FATAL, e.getMessage(), e);
        }
      }
    }   
    
  }
  
  
  
  private void loadShop(TreeSet<ReglesMagasin> pMagasin, int pRoomId) {
    PreparedStatement lStatement = null;

    String lSql = "select m.id as magasin_id, m.nom, sm.priorite from salles s, magasins m, salles_magasins sm where "
      + "m.id = sm.magasin_id and sm.salle_id = s.id and s.id=?";
    
    try {
      
      lStatement = mConnection.prepareStatement(lSql);
      lStatement.setInt(1, pRoomId);

      ResultSet lResultSet = lStatement.executeQuery();
      
      while (lResultSet.next()) {
        ReglesMagasin lReglesMagasin = new ReglesMagasin(lResultSet.getInt("priorite"), lResultSet.getString("nom"));
        
        //load the usable objet for this room
        loadShopObjects(lReglesMagasin, lResultSet.getInt("magasin_id"));

        pMagasin.add(lReglesMagasin);
      }
      
      
    } catch (SQLException e) {
      objLogger.log(Level.FATAL, e.getMessage(), e);
    } finally {
      if (lStatement != null) {
        try {
          lStatement.close();
        } catch (SQLException e) {
          objLogger.log(Level.FATAL, e.getMessage(), e);
        }
      }
    }
    
  }
  
  
  private void loadUsableObjet(TreeSet<ReglesObjetUtilisable> pObject, int pRoomId) {
    
    Statement lStatement = null;
    
    //load the usable object for this room
    String lSql = "select o.tag, so.priorite from objets o, salles_objets so, salles s where " 
      + "o.id = so.objet_id and so.salle_id = s.id and s.id=" + pRoomId;
    
    try {
      lStatement = mConnection.createStatement();
      
      ResultSet lRsObjet = lStatement.executeQuery(lSql);
      while (lRsObjet.next()) {
        pObject.add(new ReglesObjetUtilisable(lRsObjet.getInt("priorite"), lRsObjet.getString("tag"), Visibilite.Aleatoire));
      }
    } catch (SQLException e) {
      objLogger.log(Level.FATAL, e.getMessage(), e);
    } finally {
      if (lStatement != null) {
        try {
          lStatement.close();
        } catch (SQLException e) {
          objLogger.log(Level.FATAL, e.getMessage(), e);
        }
      }
    }
    
  }
  
  
  private Map<String, String> loadDescription(int pRoomId) {
    
    Statement lStatement = null;
    
    Map<String, String> lResult = new TreeMap<String, String>();
    
    String lSql = "select sd.description, l.nom_court from langues l, salles s, salle_detail sd "
      + " where sd.salle_id = s.id and l.id = sd.langue_id and s.id =" + pRoomId;

    try {
      lStatement = mConnection.createStatement();
      
      ResultSet lRsDescription = lStatement.executeQuery(lSql);
      
      while (lRsDescription.next()) {
        lResult.put(lRsDescription.getString("nom_court"), lRsDescription.getString("description"));
      }
    } catch (SQLException e) {
      objLogger.log(Level.FATAL, e.getMessage(), e);
    } finally {
      if (lStatement != null) {
        try {
          lStatement.close();
        } catch (SQLException e) {
          objLogger.log(Level.FATAL, e.getMessage(), e);
        }
      }
    }
    
    return lResult;
    
  }
  
  /**
   * Rooms are loaded directly in the ControleurJeu list of rooms 
   * @param pGameType the type of game to load room for
   */
  public void loadRooms(String pGameType) {
    
    String lSql = "select r.*, s.id as salle_id, s.nom as table_name, s.password,j.alias "
                  + " from salles s, type_jeu tj, joueur j, regles r " 
                  + " where s.type_jeu_id = tj.id and j.cleJoueur = s.joueur_id and s.regle_id = r.id and tj.nom = '" + pGameType + "'"
                  + " order by s.nom";
    
    Statement lStatement = null;    

    try {

      lStatement = mConnection.createStatement();
      
      ResultSet lRs = lStatement.executeQuery(lSql);
      
      while (lRs.next()) {
        
        //check if the room is not already loaded
        if (!ControleurJeu.getInstance().salleExiste(lRs.getString("table_name"))) {
        
          Regles objReglesSalle = new Regles();
          TreeSet<ReglesObjetUtilisable> objetsUtilisables = objReglesSalle.obtenirListeObjetsUtilisablesPossibles();
          TreeSet<ReglesMagasin> magasins = objReglesSalle.obtenirListeMagasinsPossibles();
  
          loadRuleColorSquare(objReglesSalle);
          loadRuleSpecialSquare(objReglesSalle);
          
          objReglesSalle.definirPermetChat(lRs.getInt("chat") == 1 ? true : false);
          objReglesSalle.definirRatioTrous(lRs.getFloat("ratio_trou"));
          objReglesSalle.definirRatioMagasins(lRs.getFloat("ratio_magasin"));
          objReglesSalle.definirRatioCasesSpeciales(lRs.getFloat("ratio_case_special"));
          objReglesSalle.definirRatioPieces(lRs.getFloat("ratio_piece"));
          objReglesSalle.definirRatioObjetsUtilisables(lRs.getFloat("ratio_objet_utilisable"));
          objReglesSalle.definirValeurPieceMaximale(lRs.getInt("valeur_maximal_piece"));
          objReglesSalle.definirTempsMinimal(lRs.getInt("temps_minimal"));
          objReglesSalle.definirTempsMaximal(lRs.getInt("temps_maximal"));
          objReglesSalle.definirDeplacementMaximal(lRs.getInt("deplacement_maximal"));
          objReglesSalle.definirMaxObjet(lRs.getInt("max_possession_objets_pieces"));
          

          //load the object
          loadUsableObjet(objetsUtilisables, lRs.getInt("salle_id"));
  
          //load the shops
          loadShop(magasins, lRs.getInt("salle_id"));
          
          //load the description for this room
          Map<String, String> lDescriptions = loadDescription(lRs.getInt("salle_id"));
          
          Salle lSalle = new Salle(lRs.getInt("salle_id"),
                                   lRs.getString("table_name"), 
                                   lRs.getString("alias"), 
                                   lRs.getString("password"),
                                   lDescriptions,
                                   objReglesSalle,
                                   pGameType);
          
          ControleurJeu.getInstance().ajouterNouvelleSalle(lSalle);
          
        }
      }
    } catch (SQLException e) {
      objLogger.log(Level.FATAL, e.getMessage(), e);
    } finally {
      if (lStatement != null) {
        try {
          lStatement.close();
        } catch (SQLException e) {
          objLogger.log(Level.FATAL, e.getMessage(), e);
        }
      }
    }
    
  }
  

}

