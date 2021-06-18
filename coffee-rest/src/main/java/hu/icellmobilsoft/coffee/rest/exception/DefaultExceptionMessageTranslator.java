/*-
 * #%L
 * Coffee
 * %%
 * Copyright (C) 2020 i-Cell Mobilsoft Zrt.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package hu.icellmobilsoft.coffee.rest.exception;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBException;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;

import hu.icellmobilsoft.coffee.cdi.logger.AppLogger;
import hu.icellmobilsoft.coffee.cdi.logger.LogProducer;
import hu.icellmobilsoft.coffee.cdi.logger.ThisLogger;
import hu.icellmobilsoft.coffee.dto.common.commonservice.BaseExceptionResultType;
import hu.icellmobilsoft.coffee.dto.common.commonservice.FunctionCodeType;
import hu.icellmobilsoft.coffee.dto.exception.BaseException;
import hu.icellmobilsoft.coffee.dto.exception.enums.CoffeeFaultType;
import hu.icellmobilsoft.coffee.rest.locale.LocalizedMessage;

/**
 * Default exception translator implementation for exception throwing
 *
 * @author imre.scheffer
 * @since 1.0.0
 */
public class DefaultExceptionMessageTranslator implements IExceptionMessageTranslator {

    @Inject
    @ThisLogger
    private AppLogger log;

    /** Constant <code>HTTP_STATUS_I_AM_A_TEAPOT=418</code> */
    public static final int HTTP_STATUS_I_AM_A_TEAPOT = 418;

    @Inject
    private LocalizedMessage localizedMessage;

    @Inject
    private ProjectStage projectStage;

    /** {@inheritDoc} */
    @Override
    public void addCommonInfo(BaseExceptionResultType dto, BaseException e) {
        addCommonInfo(dto, e, e.getFaultTypeEnum());
    }

    /** {@inheritDoc} */
    @Override
    public void addCommonInfo(BaseExceptionResultType dto, Throwable t, Enum<?> faultType) {
        boolean putExceptionToResponse = !ProjectStage.Production.equals(projectStage);
        if (putExceptionToResponse) {
            if (t instanceof JAXBException) {
                dto.setMessage(getLinkedExceptionLocalizedMessage((JAXBException) t));
            } else {
                dto.setMessage(t.getLocalizedMessage());
            }
            dto.setClassName(t.getClass().getName());
            dto.setException(t.getMessage());

            if (t.getCause() != null) {
                BaseExceptionResultType causedBy = new BaseExceptionResultType();
                addCommonInfo(causedBy, t.getCause(), faultType);
                dto.setCausedBy(causedBy);
            }
        }
        dto.setFuncCode(FunctionCodeType.ERROR);
        dto.setFaultType(faultType.name());
        // nyelvesitett valasz kell a faultype szerint
        dto.setMessage(getLocalizedMessage(faultType));

        try {
            String applicationName = InitialContext.doLookup("java:app/AppName");
            dto.setService(applicationName);
        } catch (NamingException e) {
            log.debug("Error in getting JNDI value of 'java:app/AppName'", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getLocalizedMessage(Enum<?> faultType) {
        if (faultType == null) {
            LogProducer.getStaticDefaultLogger(DefaultExceptionMessageTranslator.class)
                    .warn("FaultType is null, proceeding with faultType: [" + CoffeeFaultType.OPERATION_FAILED + "]");
            return localizedMessage.message(CoffeeFaultType.OPERATION_FAILED);
        }
        return localizedMessage.message(faultType);
    }

}
