/*
 * AnalogInFactory.cpp
 *
 *  Created on: Jun 30, 2018
 *      Author: PJ
 */

#include "SnobotSim/ModuleWrapper/Factories/AnalogInFactory.h"

#include "SnobotSim/Logging/SnobotLogger.h"
#include "SnobotSim/ModuleWrapper/WpiWrappers/WpiAnalogInWrapper.h"
#include "SnobotSim/SensorActuatorRegistry.h"

AnalogInFactory::AnalogInFactory()
{
    // TODO Auto-generated constructor stub
}

AnalogInFactory::~AnalogInFactory()
{
    // TODO Auto-generated destructor stub
}

bool AnalogInFactory::Create(int aHandle, const std::string& aType)
{
    bool success = true;

    if (aType == "WpiAnalogInWrapper")
    {
        SensorActuatorRegistry::Get().Register(aHandle,
                std::shared_ptr<IAnalogInWrapper>(new WpiAnalogInWrapper(aHandle)));
    }
    else
    {
        SNOBOT_LOG(SnobotLogging::WARN, "Unknown type " << aType);
        success = false;
    }

    return success;
}
