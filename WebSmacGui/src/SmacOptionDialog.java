import javax.swing.*;

import java.awt.Dimension;
import java.awt.event.*;

public class SmacOptionDialog extends JOptionPane implements ActionListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3144720063897006694L;
	private int selection;

	public SmacOptionDialog()
	{
		super();
		setOptions(new Object[]{"Okay"});
		this.setSize(new Dimension(800,600));
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