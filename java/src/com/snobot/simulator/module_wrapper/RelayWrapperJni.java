
package com.snobot.simulator.module_wrapper;

public class RelayWrapperJni {
    
    public static native void setName(int aPort, String aName);
    
    public static native String getName(int aPort);

    public static native boolean getWantsHidden(int aPort);

    public static native boolean getFowardValue(int aPort);
    
    public static native boolean getReverseValue(int aPort);
    
    public static native int[] getPortList();
}
