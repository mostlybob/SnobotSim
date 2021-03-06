/*
 * SimpleMotorSimulator.cpp
 *
 *  Created on: May 5, 2017
 *      Author: PJ
 */

#include "SnobotSim/MotorSim/SimpleMotorSimulator.h"

const std::string SimpleMotorSimulator::SIMULATOR_TYPE = "Simple";

SimpleMotorSimulator::SimpleMotorSimulator(double aMaxSpeed) :
        mMaxSpeed(aMaxSpeed),
        mVoltagePercent(0),
        mVelocity(0),
        mPosition(0)
{
}

SimpleMotorSimulator::~SimpleMotorSimulator()
{
}

const std::string& SimpleMotorSimulator::GetSimulatorType()
{
    return SIMULATOR_TYPE;
}

double SimpleMotorSimulator::GetMaxSpeed()
{
    return mMaxSpeed;
}

void SimpleMotorSimulator::SetVoltagePercentage(double aSpeed)
{
    mVoltagePercent = aSpeed;
}

double SimpleMotorSimulator::GetVoltagePercentage()
{
    return mVoltagePercent;
}

double SimpleMotorSimulator::GetAcceleration()
{
    return 0;
}

double SimpleMotorSimulator::GetCurrent()
{
    return 0;
}

double SimpleMotorSimulator::GetVelocity()
{
    return mVelocity;
}

double SimpleMotorSimulator::GetPosition()
{
    return mPosition;
}

void SimpleMotorSimulator::Reset()
{
    Reset(0, 0, 0);
}

void SimpleMotorSimulator::Reset(double aPosition, double aVelocity, double aCurrent)
{
    mPosition = aPosition;
    mVelocity = aVelocity;
}

void SimpleMotorSimulator::Update(double aUpdateTime)
{
    mVelocity = mMaxSpeed * mVoltagePercent;
    mPosition += mVelocity * aUpdateTime;
}
