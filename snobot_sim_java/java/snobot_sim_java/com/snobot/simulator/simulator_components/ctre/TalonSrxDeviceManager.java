package com.snobot.simulator.simulator_components.ctre;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.snobot.simulator.SensorActuatorRegistry;
import com.snobot.simulator.jni.SensorFeedbackJni;

public class TalonSrxDeviceManager implements ICanDeviceManager
{
    private static final Logger sLOGGER = Logger.getLogger(TalonSrxDeviceManager.class);
    private static final int sCAN_OFFSET = 100;

    @Override
    public void handleSend(int aMessageId)
    {
        int port = aMessageId & 0x3F;

        ByteBuffer buffer = ByteBuffer.allocateDirect(19);
        SensorFeedbackJni.getCanLastSentMessageData(buffer, 8);

        sLOGGER.log(Level.DEBUG,
                "@SendingMessage: MID: " + Integer.toHexString(aMessageId) + ", Data: (0x" + String.format("%016X", buffer.getLong(0)) + ")");

        int messageId = aMessageId & 0xFFFFFFC0;

        if (messageId == 0x02040000)
        {
            handleTx1(buffer, port);
        }
        else if (messageId == 0x02041880)
        {
            handleSetParamCommand(buffer, port);
        }
        else if (messageId == 0x02041800)
        {
            handleParamRequest(buffer, port);
        }
        else
        {
            unsupportedWrite(messageId);
        }
    }

    @Override
    public void handleReceive(int aMessageId)
    {
        int port = aMessageId & 0x3F;

        sLOGGER.log(Level.DEBUG, "@ReceiveMessage: MID: " + Integer.toHexString(aMessageId));

        int messageId = aMessageId & 0xFFFFFFC0;

        if (messageId == 0x02041400)
        {
            populateStatus1(port);
        }
        else if (messageId == 0x02041440)
        {
            populateStatus2(port);
        }
        else if (messageId == 0x02041480)
        {
            populateStatus3(port);
        }
        else if (messageId == 0x020414C0)
        {
            populateStatus4(port);
        }
        else
        {
            unsupportedRead(messageId);
        }
    }

    @Override
    public void openStreamSession(int aMessageId)
    {
        // Handled Elsewhere
    }

    @Override
    public void readStreamSession(int aMessageId)
    {
        // Handled Elsewhere
    }

    private void handleTx1(ByteBuffer aBuffer, int aPort)
    {

        byte command = (byte) (aBuffer.get(5) & 0xF0);
        if (command == 0x00)
        {
            CanTalonSpeedControllerSim wrapper = new CanTalonSpeedControllerSim(aPort);
            SensorActuatorRegistry.get().register(wrapper, aPort + sCAN_OFFSET);
            sLOGGER.log(Level.DEBUG, "Creating " + aPort);
        }
        else if (command == 0x20)
        {
            handleSetDemandCommand(aBuffer, aPort);
        }
        else
        {
            sLOGGER.log(Level.ERROR, "Unknown command: " + command);
        }
    }

    private void handleSetParamCommand(ByteBuffer aBuffer, int aPort)
    {
        CanTalonSpeedControllerSim wrapper = getWrapperHelper(aPort);
        aBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byte commandType = aBuffer.get(0);

        int rawValue = aBuffer.getInt(1);
        double floatValue = rawValue * 0.0000002384185791015625;

        // P gain
        if (commandType == 1)
        {
            wrapper.setPGain(floatValue);
        }
        // I gain
        else if (commandType == 2)
        {
            wrapper.setIGain(floatValue);
        }
        // D gain
        else if (commandType == 3)
        {
            wrapper.setDGain(floatValue);
        }
        // F gain
        else if (commandType == 4)
        {
            wrapper.setFGain(floatValue);
        }
        // I Zone
        else if (commandType == 5)
        {
            wrapper.setIZone(rawValue);
        }
        else
        {
            sLOGGER.log(Level.ERROR, "Unknown SetParam command: " + commandType);
        }
    }

    private void handleParamRequest(ByteBuffer aBuffer, int aPort)
    {
        sLOGGER.log(Level.DEBUG, "Getting parameters...");

        CanTalonSpeedControllerSim wrapper = getWrapperHelper(aPort);
        aBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byte commandType = aBuffer.get(0);

        double floatValue = 0;
        boolean isFloat = true;

        // P gain
        if (commandType == 1)
        {
            floatValue = wrapper.getPidConstants().mP;
        }
        // I gain
        else if (commandType == 2)
        {
            floatValue = wrapper.getPidConstants().mI;
        }
        // D gain
        else if (commandType == 3)
        {
            floatValue = wrapper.getPidConstants().mD;
        }
        // F gain
        else if (commandType == 4)
        {
            floatValue = wrapper.getPidConstants().mF;
        }
        // I Zone
        else if (commandType == 5)
        {
            floatValue = wrapper.getPidConstants().mIZone;
            isFloat = false;
        }
        else
        {
            sLOGGER.log(Level.ERROR, "Unknown GetParam command: " + commandType);
        }
        int rawValue;
        if(isFloat)
        {
            rawValue = (int) (floatValue / 0.0000002384185791015625);
        }
        else
        {
            rawValue = (int) floatValue;
        }
        
        int messageId = 0x2041840;
        messageId |= aPort;

        ByteBuffer outputBuffer = ByteBuffer.allocateDirect(40);
        outputBuffer.order(ByteOrder.LITTLE_ENDIAN);
        outputBuffer.putInt(messageId);
        outputBuffer.putInt(0xDEADBEEF); // Timestamp
        outputBuffer.put(commandType);
        outputBuffer.putInt(rawValue);

        SensorFeedbackJni.setCanSetValueForReadStream(outputBuffer, 1);

    }

    private void handleSetDemandCommand(ByteBuffer aBuffer, int aPort)
    {
        byte commandType = (byte) ((aBuffer.get(6) >> 4) & 0xF);
        CanTalonSpeedControllerSim wrapper = getWrapperHelper(aPort);
        int demand = (aBuffer.getInt(2)) >> 8;
        // int demand2 =
        sLOGGER.log(Level.DEBUG, String.format(" Demand: %d, Command: %d", demand, commandType));

        if (commandType == 0x00)
        {
            double appliedVoltageDemand = demand / 1023.0;
            wrapper.set(appliedVoltageDemand);
            sLOGGER.log(Level.DEBUG, " Setting by applied throttle.. " + appliedVoltageDemand);
        }
        else if (commandType == (byte) 0x01)
        {
            double position = demand / 4096.0;
            wrapper.setPositionGoal(position);
            sLOGGER.log(Level.DEBUG, "  Setting by position." + position);
        }
        else if (commandType == (byte) 0x02)
        {
            double speed = demand * 600.0 / 4096.0;
            wrapper.setSpeedGoal(speed);
            sLOGGER.log(Level.DEBUG, " Setting by speed. " + speed);
        }
        else if (commandType == (byte) 0x03)
        {
            sLOGGER.log(Level.DEBUG, "  Setting by current." + demand);
        }
        else if (commandType == (byte) 0x04)
        {
            double voltageDemand = demand / 256.0;
            wrapper.set(voltageDemand / 12.0);
            sLOGGER.log(Level.DEBUG, "  Setting by voltage. " + voltageDemand);
        }
        else if (commandType == (byte) 0x05)
        {
            sLOGGER.log(Level.DEBUG, "  Setting by FOLLOWER.");
        }
        else if (commandType == (byte) 0x06)
        {
            sLOGGER.log(Level.DEBUG, "  Setting by Motion Profile.");
        }
        else if (commandType == (byte) 0x07)
        {
            sLOGGER.log(Level.DEBUG, "  Setting by Motion Magic.");
        }
        else if (commandType == (byte) 0x0F)
        {
            // Nothing to do, but don't print error
        }
        else
        {
            sLOGGER.log(Level.ERROR, String.format("Unknown set command type 0x%02X", commandType));
        }
    }

    private void populateStatus1(int aPort)
    {
        CanTalonSpeedControllerSim wrapper = getWrapperHelper(aPort);
        sLOGGER.log(Level.DEBUG, " Getting STATUS1 " + wrapper.get());

        ByteBuffer buffer = ByteBuffer.allocateDirect(19);
        buffer.putShort(3, (short) (wrapper.get() * 1023));
        SensorFeedbackJni.setCanSetValueForRead(buffer, 8);
    }

    private void populateStatus2(int aPort)
    {
        CanTalonSpeedControllerSim wrapper = getWrapperHelper(aPort);
        int binnedPosition = (int) (wrapper.getPosition() * 4096);
        int binnedVelocity = (int) (wrapper.getVelocity() * 6.9);

        ByteBuffer buffer = ByteBuffer.allocateDirect(19);
        putNumber(buffer, binnedPosition, 3);
        buffer.putShort((short) binnedVelocity);

        SensorFeedbackJni.setCanSetValueForRead(buffer, 8);
    }

    private void populateStatus3(int aPort)
    {
        CanTalonSpeedControllerSim wrapper = getWrapperHelper(aPort);
        int binnedPosition = (int) wrapper.getPosition();
        int binnedVelocity = (int) wrapper.getVelocity();

        ByteBuffer buffer = ByteBuffer.allocateDirect(19);
        putNumber(buffer, binnedPosition, 3);
        buffer.putShort((short) binnedVelocity);

        SensorFeedbackJni.setCanSetValueForRead(buffer, 8);
    }

    private void populateStatus4(int aPort)
    {
        sLOGGER.log(Level.DEBUG, "POPULATE STATUS 4");
        double temperature = 30;
        double batteryVoltage = 12;
        CanTalonSpeedControllerSim wrapper = getWrapperHelper(aPort);
        int binnedPosition = (int) wrapper.getPosition();
        int binnedVelocity = (int) wrapper.getVelocity();

        ByteBuffer buffer = ByteBuffer.allocateDirect(19);
        putNumber(buffer, binnedPosition, 3);
        buffer.putShort((short) binnedVelocity);
        buffer.put((byte) ((temperature + 50) / 0.6451612903));
        buffer.put((byte) ((batteryVoltage - 4) / 0.05));

        SensorFeedbackJni.setCanSetValueForRead(buffer, 8);
    }

    private void putNumber(ByteBuffer aOutput, int aNumber, int aBytes)
    {
        for (int i = aBytes; i > 0; --i)
        {
            int shift = 8 * (i - 1);
            int mask = 0xFF << shift;
            aOutput.put((byte) ((aNumber & mask) >> shift));
        }
    }

    private void unsupportedRead(int aStatusType)
    {
        sLOGGER.log(Level.ERROR, "Status request " + aStatusType + " is not supported.");

        ByteBuffer buffer = ByteBuffer.allocateDirect(19);
        SensorFeedbackJni.setCanSetValueForRead(buffer, 8);
    }

    private void unsupportedWrite(int aStatusType)
    {
        sLOGGER.log(Level.ERROR, "TX Request " + aStatusType + " is not supported.");

        ByteBuffer buffer = ByteBuffer.allocateDirect(19);
        SensorFeedbackJni.setCanSetValueForRead(buffer, 8);
    }

    private CanTalonSpeedControllerSim getWrapperHelper(int aPort)
    {
        return (CanTalonSpeedControllerSim) SensorActuatorRegistry.get().getSpeedControllers().get(aPort + sCAN_OFFSET);
    }
}
