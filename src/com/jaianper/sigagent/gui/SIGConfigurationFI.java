package com.jaianper.sigagent.gui;

import javax.swing.JTextField;

/**
 *
 * @author jaianper
 */
public class SIGConfigurationFI
{
    private String name;
    private JTextField textField;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public JTextField getTextField()
    {
        return textField;
    }

    public void setTextField(JTextField textField)
    {
        this.textField = textField;
    }
    
    public String getValue()
    {
        return textField.getText();
    }
}
