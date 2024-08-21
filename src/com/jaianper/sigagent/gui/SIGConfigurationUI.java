package com.jaianper.sigagent.gui;

import com.jaianper.common.error.GenericException;
import com.jaianper.sigagent.controller.SIGConfigurationController;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import org.apache.log4j.Logger;

/**
 *
 * @author jaianper
 */
public class SIGConfigurationUI extends JFrame implements ActionListener
{
    private static final String CB_MESSAGEBROKER = "CBMessageBroker";
    private static final String BTN_SAVECONFIGURATION = "BTNSaveConfiguration";
    
    private final SIGConfigurationController sigCController;
    private JPanel panelBody;
    
    /**
     * Creates new form SIGAgentConfiguration
     * @param sigCController
     */
    public SIGConfigurationUI(SIGConfigurationController sigCController)
    {
        super();
        this.sigCController = sigCController;
        setTitle(sigCController.getApplicationName());
        setLookAndFeel();
        initComponents();
    }

    /**
     * 
     */                     
    private void initComponents()
    {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 1));
        
        try
        {
            Map<String, String> mParameters = sigCController.getConfigurationParameters("default");
            
            getContentPane().add("CTopContent", getTopContent());
            getContentPane().add("CBodyContent", getBodyContent(mParameters));
            getContentPane().add("CBottomContent", getBottomContent());
        }
        catch (GenericException ex)
        {
            showError("Error getting default configuration parameters.", ex);
        }

        pack();
    }
    
    private JPanel getTopContent()
    {
        JLabel lblMessageBroker = new JLabel("Select message broker");
        
        JComboBox cb = new JComboBox();
        cb.setName(CB_MESSAGEBROKER);
        
        for(String item : sigCController.getMessageBrokerNames())
        {
            cb.addItem(item);
        }
        
        cb.addActionListener(this);
        
        JPanel panelTop = new JPanel();
        panelTop.add(lblMessageBroker);
        panelTop.add(cb);
        panelTop.setBackground(Color.red);
        
        return panelTop;
    }
    
    private JPanel getBodyContent(Map<String, String> mParameters)
    {
        if(panelBody == null)
        {
            panelBody = new JPanel();
            panelBody.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panelBody.setBackground(Color.blue);
        }
        
        panelBody.removeAll();
        panelBody.setLayout(new GridLayout(mParameters.size(), 2));
        
        for(Map.Entry<String, String> entry : mParameters.entrySet())
        {
            JTextField tfLabel = new JTextField(entry.getKey());
            tfLabel.setEditable(false);
            tfLabel.setFocusable(false);
            tfLabel.setBackground(Color.LIGHT_GRAY);
            
            JTextField tfInput = new JTextField();
            tfInput.setText(entry.getValue());
            
            SIGConfigurationFI cfi = new SIGConfigurationFI();
            cfi.setName(entry.getKey());
            cfi.setTextField(tfInput);
            
            sigCController.addConfigurationField(cfi);
            
            panelBody.add(tfLabel);
            panelBody.add(tfInput);
        }
        
        return panelBody;
    }
    
    private JPanel getBottomContent()
    {
        JButton btnSaveConfiguration = new JButton();
        btnSaveConfiguration.setText("Save configuration");
        btnSaveConfiguration.setName(BTN_SAVECONFIGURATION);
        btnSaveConfiguration.addActionListener(this);
        
        JPanel panelBottom = new JPanel();
        panelBottom.add(btnSaveConfiguration);
        panelBottom.setBackground(Color.green);
        
        return panelBottom;
    }
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        if(source instanceof JComponent)
        {
            String componentName = ((JComponent)source).getName();
            if(CB_MESSAGEBROKER.equals(componentName))
            {
                String mbSelected = ((JComboBox)source).getSelectedItem()+"";
                try
                {
                    Map<String, String> mParameters = sigCController.getConfigurationParameters(mbSelected);
                    getBodyContent(mParameters);
                    //getContentPane().add("CBodyContent", getBodyContent(cParameterKeys));
                    pack();
                }
                catch (GenericException ex)
                {
                    showError("Error getting configuration parameters from \""+mbSelected+"\".", ex);
                }
            }
            else if(BTN_SAVECONFIGURATION.equals(componentName))
            {
                try
                {
                    sigCController.saveConfiguration();
                    dispose();
                }
                catch (GenericException ex)
                {
                    showError("Error saving configuration parameters.", ex);
                }
            }
        }
    }
    
    private void setLookAndFeel()
    {
        try
        {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ClassNotFoundException ex)
        {
            Logger.getLogger(SIGConfigurationUI.class).error("", ex);
        }
        catch (InstantiationException ex)
        {
            Logger.getLogger(SIGConfigurationUI.class).error("", ex);
        }
        catch (IllegalAccessException ex)
        {
            Logger.getLogger(SIGConfigurationUI.class).error("", ex);
        }
        catch (UnsupportedLookAndFeelException ex)
        {
            Logger.getLogger(SIGConfigurationUI.class).error("", ex);
        }
    }
    
    private void showError(String message, Throwable throwable)
    {
        Logger.getLogger(SIGConfigurationUI.class).error(message, throwable);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}

