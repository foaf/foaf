/*
 * Created on Aug 17, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.rdfweb.foafcon;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author pldms
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class InfoDialog extends Dialog
{
	private Text textField;
	private String infoText;
	
	/**
	 * @param arg0
	 */
	public InfoDialog(Shell arg0)
	{
		super(arg0);
	}
	
	public void setInfoText(String infoText)
	{
		this.infoText = infoText;
	}
	
	protected Control createDialogArea(Composite parent)
	{
		getShell().setText("More Information");
		
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 150;
		layoutData.widthHint = 250;
		
		textField = new Text(parent, 
				     SWT.MULTI | SWT.V_SCROLL | 
				     SWT.H_SCROLL | SWT.READ_ONLY);
		if (infoText == null)
			textField.setText("NOWT");
		else
			textField.setText(infoText);
		
		textField.setLayoutData(layoutData);
		
		return textField;
	}

}
