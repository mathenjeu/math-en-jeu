/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ServeurJeu.ComposantesJeu;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author David
 */
public class RapportDeSalle
{
    public enum ReportType {FULL, SUMMARY};

    private final int roomId;
    private final ReportType type;
    private Map<Integer, PlayerInfo> players;
    private Map<Integer, PlayerSummary> playerSummaries;
    private Map<Integer, Set<String>> questionSWFs;
    private Map<Integer, QuestionSummary> questionSummaries;
    private Map<Integer, GameInfo> games;

    public RapportDeSalle(int roomId, ReportType type) {
        this.roomId = roomId;
        this.type = type;
        players = new HashMap<Integer, PlayerInfo>();
        questionSWFs = new HashMap<Integer, Set<String>>();
        if (type == ReportType.SUMMARY) {
            playerSummaries = new HashMap<Integer, PlayerSummary>();
            questionSummaries = new HashMap<Integer, QuestionSummary>();
        }
        else
            games = new HashMap<Integer, GameInfo>();
    }

    public ReportType obtenirType() {
        return type;
    }
    public void addPlayerInfo(int user_id, String firstname, String lastname) {
        PlayerInfo player = players.get(user_id);
        if (player != null) return;
        players.put(user_id, new PlayerInfo(firstname, lastname));
    }

    public void addPlayerSummary(int user_id, int time_period, int games_played,
            int max_score, int sum_score, int num_won,
            int num_questions, int num_right, int num_wrong) {
        PlayerSummary ps = playerSummaries.get(user_id);
        if (ps == null) {
            ps = new PlayerSummary();
            playerSummaries.put(user_id, ps);
        }
        ps.gamesPlayed[time_period] = games_played;
        ps.maxScore[time_period] = max_score;
        ps.sumScores[time_period] = sum_score;
        ps.numberOfWins[time_period] = num_won;
        ps.questionsAnswered[time_period] = num_questions;
        ps.questionsAnsweredRight[time_period] = num_right;
        ps.questionsAnsweredWrong[time_period] = num_wrong;

    }

    public void addQuestionSWF(int question_id, String swf) {
        Set<String> swfs = questionSWFs.get(question_id);
        if (swfs == null) {
            swfs = new TreeSet<String>();
            questionSWFs.put(question_id, swfs);
        }
        swfs.add(swf);
    }

    public void addQuestionSummary(int question_id, int time_period,
            int frequency, int freq_right, int freq_wrong,
            int time_taken, int time_taken_right, int time_taken_wrong) {
        QuestionSummary qs = questionSummaries.get(question_id);
        if (qs == null) {
            qs = new QuestionSummary();
            questionSummaries.put(question_id, qs);
        }
        qs.frequency[time_period] = frequency;
        qs.frequencyRight[time_period] = freq_right;
        qs.frequencyWrong[time_period] = freq_wrong;
        qs.timeTaken[time_period] = time_taken;
        qs.timeTakenRight[time_period] = time_taken_right;
        qs.timeTakenWrong[time_period] = time_taken_wrong;
    }

    public void addGameInfo(int game_id, String date, int game_type_id, int winner_id, int duration,
            int user_id, int question_id, short answer_status, int time_taken, int score) {
        GameInfo game = games.get(game_id);
        if (game == null) {
            game = new GameInfo(date, game_type_id, winner_id, duration);
            games.put(game_id, game);
        }
        Set<InformationQuestion> infoQuestions = game.userIdToInformationQuestions.get(user_id);
        if (infoQuestions == null) {
            infoQuestions = new TreeSet<InformationQuestion>();
            game.userIdToInformationQuestions.put(user_id, infoQuestions);
            game.scores.put(user_id, score);
        }
        InformationQuestion iq = new InformationQuestion(question_id,time_taken);
        iq.definirValiditee(answer_status);
        infoQuestions.add(iq);
    }
    public Element createXML(Document doc) {
        Element objNoeudRapport = doc.createElement("parametre");
        objNoeudRapport.setAttribute("type", "Rapport");
        
        objNoeudRapport.appendChild(createPlayerXML(doc));
        objNoeudRapport.appendChild(createQuestionXML(doc));
        switch (type) {
            case FULL:
                objNoeudRapport.appendChild(createGameXML(doc));
                break;
            case SUMMARY:
                objNoeudRapport.appendChild(createPlayerSummaryXML(doc));
                objNoeudRapport.appendChild(createQuestionSummaryXML(doc));
                break;
        }
        return objNoeudRapport;
    }

    public Element createGameXML(Document doc) {
        Element objNoeudParametreListeParties = doc.createElement("parametre");
        objNoeudParametreListeParties.setAttribute("type", "ListeParties");
        for (Integer gid: games.keySet()) {
            GameInfo game = games.get(gid);
            Element objNoeudPartie = doc.createElement("game");
            objNoeudPartie.setAttribute("id", gid.toString());
            objNoeudPartie.setAttribute("d", game.strDate);
            objNoeudPartie.setAttribute("gt", ""+game.gameTypeId);
            objNoeudPartie.setAttribute("w", ""+game.winnerId);
            objNoeudPartie.setAttribute("t", ""+game.duration);
            for (Integer uid : game.userIdToInformationQuestions.keySet()) {
                Element objNoeudUser = doc.createElement("user");
                objNoeudUser.setAttribute("id", uid.toString());
                objNoeudUser.setAttribute("s", ""+game.scores.get(uid));
                for (InformationQuestion iq : game.userIdToInformationQuestions.get(uid)) {
                    Element objNoeudQuestion = doc.createElement("question");
                    objNoeudQuestion.setAttribute("id", ""+iq.obtenirQuestionId());
                    objNoeudQuestion.setAttribute("s", ""+iq.obtenirValiditee());
                    objNoeudQuestion.setAttribute("t", ""+iq.obtenirTempsRequis());
                    objNoeudUser.appendChild(objNoeudQuestion);
                }
                objNoeudPartie.appendChild(objNoeudUser);
            }
            objNoeudParametreListeParties.appendChild(objNoeudPartie);
        }
        return objNoeudParametreListeParties;
    }

    public Element createPlayerXML(Document doc) {
        Element objNoeudParametreListeJoueurs = doc.createElement("parametre");
        objNoeudParametreListeJoueurs.setAttribute("type", "ListeJoueurs");
        for (Integer uid: players.keySet()) {
            PlayerInfo player = players.get(uid);
            Element objNoeudJoueur = doc.createElement("user");
            objNoeudJoueur.setAttribute("id", uid.toString());
            objNoeudJoueur.setAttribute("n", player.lastname + ',' + player.firstname);
            objNoeudParametreListeJoueurs.appendChild(objNoeudJoueur);
        }
        return objNoeudParametreListeJoueurs;

    }

    public Element createPlayerSummaryXML(Document doc) {
        Element objNoeudParametreListeSommaireJoueurs = doc.createElement("parametre");
        objNoeudParametreListeSommaireJoueurs.setAttribute("type", "ListeSommaireJoueurs");
        for (Integer uid: playerSummaries.keySet()) {
            PlayerSummary ps = playerSummaries.get(uid);
            Element objNoeudStatsJoueur = doc.createElement("user");
            objNoeudStatsJoueur.setAttribute("id", uid.toString());
            for (int time_period=0; time_period<ps.gamesPlayed.length; time_period++) {
                Element objNoeudTimePeriod = doc.createElement("period");
                objNoeudTimePeriod.setAttribute("gp", ""+ps.gamesPlayed[time_period]);
                objNoeudTimePeriod.setAttribute("ms", ""+ps.maxScore[time_period]);
                objNoeudTimePeriod.setAttribute("ss", ""+ps.sumScores[time_period]);
                objNoeudTimePeriod.setAttribute("w", ""+ps.numberOfWins[time_period]);
                objNoeudTimePeriod.setAttribute("nq", ""+ps.questionsAnswered[time_period]);
                objNoeudTimePeriod.setAttribute("nr", ""+ps.questionsAnsweredRight[time_period]);
                objNoeudTimePeriod.setAttribute("nw", ""+ps.questionsAnsweredWrong[time_period]);
                objNoeudStatsJoueur.appendChild(objNoeudTimePeriod);
            }
            objNoeudParametreListeSommaireJoueurs.appendChild(objNoeudStatsJoueur);
        }
        return objNoeudParametreListeSommaireJoueurs;
    }

    public Element createQuestionXML(Document doc) {
        Element objNoeudParametreListeSWFs = doc.createElement("parametre");
        objNoeudParametreListeSWFs.setAttribute("type", "ListeSWFs");
        for (Integer qid: questionSWFs.keySet()) {
            Element objNoeudSWF = doc.createElement("swf");
            objNoeudSWF.setAttribute("qid", qid.toString());
            for (String filename : questionSWFs.get(qid))
                objNoeudSWF.appendChild(doc.createTextNode(filename));
            objNoeudParametreListeSWFs.appendChild(objNoeudSWF);
        }
        return objNoeudParametreListeSWFs;
    }

    public Element createQuestionSummaryXML(Document doc) {
        Element objNoeudParametreListeSommaireJoueurs = doc.createElement("parametre");
        objNoeudParametreListeSommaireJoueurs.setAttribute("type", "ListeSommaireQuestions");
        for (Integer qid: questionSummaries.keySet()) {
            QuestionSummary qs = questionSummaries.get(qid);
            Element objNoeudStatsQuestion = doc.createElement("question");
            objNoeudStatsQuestion.setAttribute("id", qid.toString());
            for (int time_period=0; time_period<qs.frequency.length; time_period++) {
                Element objNoeudTimePeriod = doc.createElement("period");
                objNoeudTimePeriod.setAttribute("f", ""+qs.frequency[time_period]);
                objNoeudTimePeriod.setAttribute("fr", ""+qs.frequencyRight[time_period]);
                objNoeudTimePeriod.setAttribute("fw", ""+qs.frequencyWrong[time_period]);
                objNoeudTimePeriod.setAttribute("t", ""+qs.timeTaken[time_period]);
                objNoeudTimePeriod.setAttribute("tr", ""+qs.timeTakenRight[time_period]);
                objNoeudTimePeriod.setAttribute("tw", ""+qs.timeTakenWrong[time_period]);
                objNoeudStatsQuestion.appendChild(objNoeudTimePeriod);
            }
            objNoeudParametreListeSommaireJoueurs.appendChild(objNoeudStatsQuestion);
        }
        return objNoeudParametreListeSommaireJoueurs;
    }

    class GameInfo
    {
        private String strDate;
        private int gameTypeId;
        private int winnerId;
        private int duration;
        private Map<Integer, Set<InformationQuestion>> userIdToInformationQuestions;
        private Map<Integer, Integer> scores;

        private GameInfo(String date, int game_type_id,  int winner_id, int durationInMinutes) {
            strDate = date;
            gameTypeId = game_type_id;
            winnerId = winner_id;
            duration = durationInMinutes;
            scores = new HashMap<Integer,Integer>();
            userIdToInformationQuestions = new HashMap<Integer, Set<InformationQuestion>>();
        }
    }

    class PlayerInfo
    {
        private String firstname;
        private String lastname;

        private PlayerInfo(String firstname, String lastname) {
            this.firstname = firstname;
            this.lastname = lastname;
        }
    }

    class PlayerSummary
    {
        private int[] gamesPlayed;
        private int[] questionsAnswered;
        private int[] questionsAnsweredRight;
        private int[] questionsAnsweredWrong;
        private int[] numberOfWins;
        private int[] maxScore;
        private int[] sumScores;
        
        private PlayerSummary() {
            gamesPlayed = new int[]{0,0,0};
            questionsAnswered = new int[]{0,0,0};
            questionsAnsweredRight = new int[]{0,0,0};
            questionsAnsweredWrong = new int[]{0,0,0};
            numberOfWins = new int[]{0,0,0};
            maxScore = new int[]{0,0,0};
            sumScores = new int[]{0,0,0};
        }
    }

    class QuestionSummary
    {
        private int[] frequency;
        private int[] frequencyRight;
        private int[] frequencyWrong;
        private int[] timeTaken;
        private int[] timeTakenRight;
        private int[] timeTakenWrong;

        private QuestionSummary() {
            frequency = new int[]{0,0,0};
            frequencyRight = new int[]{0,0,0};
            frequencyWrong = new int[]{0,0,0};
            timeTaken = new int[]{0,0,0};
            timeTakenRight = new int[]{0,0,0};
            timeTakenWrong = new int[]{0,0,0};
        }
    }



}
