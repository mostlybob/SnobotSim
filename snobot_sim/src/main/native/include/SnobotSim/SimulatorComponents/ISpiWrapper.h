/*
 * ISpiWrapper.hpp
 *
 *  Created on: May 7, 2017
 *      Author: PJ
 */

#ifndef ISPIWRAPPER_HPP_
#define ISPIWRAPPER_HPP_

#include <stdint.h>
#include "SnobotSim/ExportHelper.h"

class ISpiWrapper
{
public:

    virtual void HandleRead() = 0;
};


class EXPORT_ NullSpiWrapper : public ISpiWrapper
{
public:

    virtual void HandleRead() override;
};


#endif /* ISPIWRAPPER_HPP_ */
