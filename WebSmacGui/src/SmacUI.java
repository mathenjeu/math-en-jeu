import java.util.ArrayList;

public interface SmacUI
{
	public int selectOption(String msg, ArrayList<SmacOptionData> options);
	public void outputMessage(String message);
	//public String encodeQuestionNumber(int nm);
}