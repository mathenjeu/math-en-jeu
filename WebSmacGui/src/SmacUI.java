import java.util.ArrayList;

public interface SmacUI
{
	int selectOption(String msg, ArrayList<SmacOptionData> options);
	void outputMessage(String message);
	//public String encodeQuestionNumber(int nm);
}