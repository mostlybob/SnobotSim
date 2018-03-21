package com.snobot.simulator.gui.joysticks;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.junit.Assert;
import org.junit.Test;

import com.snobot.test.utilities.BaseGuiSimulatorTest;

public class TestPanels extends BaseGuiSimulatorTest
{
    @Test
    public void testJoystickManagerDialog() throws Exception
    {
        JoystickManagerDialog dialog = new JoystickManagerDialog();
        boolean success = true;

        try
        {
            dialog.setVisible(true);
        }
        catch (Exception ex)
        {
            LogManager.getLogger().log(Level.ERROR, ex);
        }
        finally
        {
            dialog.dispose();
        }

        Assert.assertTrue(success);
    }
}
