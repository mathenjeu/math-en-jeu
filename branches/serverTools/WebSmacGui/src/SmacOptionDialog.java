import javax.swing.*;
import java.awt.event.*;

public class SmacOptionDialog extends JOptionPane implements ActionListener
{

	private int selection;

	public SmacOptionDialog()
	{
		super();
		setOptions(new Object[]{"Okay"});
	}

	public void actionPerformed(ActionEvent ae)
	{
		String command = ae.getActionCommand();
		selection = Integer.parseInt(command);
	}

	public int getSelection()
	{
		return selection;
	}
	public void setSelection(int i)
	{
		selection = i;
	}


}