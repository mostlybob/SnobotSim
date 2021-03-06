package com.snobot.simulator.gui.module_widget.settings;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.snobot.simulator.wrapper_accessors.DataAccessorFactory;

public class EncoderSettingsDialog extends SimpleSettingsDialog
{
    protected JComboBox<SensorHandleOption> mSpeedControllerSelection;

    public EncoderSettingsDialog(String aTitle, int aKey, String aName)
    {
        super(aTitle, aKey, aName);

        mSpeedControllerSelection = new JComboBox<>();
        mSpeedControllerSelection.addItem(new SensorHandleOption(-1, "None"));

        List<Integer> speedControllers = DataAccessorFactory.getInstance().getSpeedControllerAccessor().getPortList();
        for (int handle : speedControllers)
        {
            SensorHandleOption option = new SensorHandleOption(handle,
                    DataAccessorFactory.getInstance().getSpeedControllerAccessor().getName(handle));
            mSpeedControllerSelection.addItem(option);
        }

        selectAttachedControllerAndRefreshNames();

        JPanel scSelectionPanel = new JPanel();
        scSelectionPanel.add(new JLabel("Connected SC"));
        scSelectionPanel.add(mSpeedControllerSelection);
        getContentPane().add(scSelectionPanel, BorderLayout.CENTER);
    }

    @Override
    public void setVisible(boolean aVisible)
    {
        selectAttachedControllerAndRefreshNames();
        super.setVisible(aVisible);
    }

    private void selectAttachedControllerAndRefreshNames()
    {
        int connectedSc = -1;
        if (DataAccessorFactory.getInstance().getEncoderAccessor().isHookedUp(mHandle))
        {
            connectedSc = DataAccessorFactory.getInstance().getEncoderAccessor().getHookedUpId(mHandle);
        }

        for (int i = 0; i < mSpeedControllerSelection.getItemCount(); ++i)
        {
            SensorHandleOption option = mSpeedControllerSelection.getItemAt(i);

            if (option.mHandle == connectedSc)
            {
                mSpeedControllerSelection.setSelectedIndex(i);
            }
            if (option.mHandle != -1)
            {
                option.mName = DataAccessorFactory.getInstance().getSpeedControllerAccessor().getName(option.mHandle);
            }
        }
    }

    @Override
    protected void onSubmit()
    {
        SensorHandleOption option = (SensorHandleOption) mSpeedControllerSelection.getSelectedItem();
        int scId = option == null ? -1 : option.mHandle;

        DataAccessorFactory.getInstance().getEncoderAccessor().connectSpeedController(mHandle, scId);
    }

}
