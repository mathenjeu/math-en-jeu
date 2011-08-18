/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ServeurJeu.ComposantesJeu.Questions;

/**
 *
 * @author David
 */
public class InformationQuestion implements Comparable<InformationQuestion> {
    public static final short RIGHT_ANSWER=(short)0;
    public static final short WRONG_ANSWER=(short)1;
    public static final short NOT_ANSWERED=(short)2;
    private int questionId;
    public short answerStatus;
    private int answerTimeTaken;

    public InformationQuestion(int qid, int timeLeft) {
        questionId = qid;
        answerStatus = NOT_ANSWERED;
        answerTimeTaken = timeLeft;
    }

    public void definirValiditee(short validitee) {
        switch (validitee) {
            case RIGHT_ANSWER:
            case WRONG_ANSWER:
            case NOT_ANSWERED: answerStatus = validitee; break;
            default: answerStatus = NOT_ANSWERED;
        }
    }
    public void definirTempsRequis(int time) {
        answerTimeTaken = Math.max(0,time);
    }
    
    public int obtenirQuestionId() {
        return questionId;
    }
    public short obtenirValiditee() {
        return answerStatus;
    }
    public int obtenirTempsRequis() {
        return answerTimeTaken;
    }
    
    @Override
    public int compareTo(InformationQuestion iq)
    {
        if (questionId == iq.questionId) return 0;
        if (questionId < iq.questionId) return -1;
        return 1;
    }

}
