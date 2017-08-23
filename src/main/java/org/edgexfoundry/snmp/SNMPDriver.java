/*******************************************************************************
 * Copyright 2016-2017 Dell Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @microservice:  device-modbus
 * @author: Anantha Boyapalle, Dell
 * @version: 1.0.0
 *******************************************************************************/
package org.edgexfoundry.snmp;

import java.io.IOException;

import org.edgexfoundry.data.DeviceStore;
import org.edgexfoundry.data.ObjectStore;
import org.edgexfoundry.data.ProfileStore;
import org.edgexfoundry.domain.SNMPAttribute;
import org.edgexfoundry.domain.SNMPDevice;
import org.edgexfoundry.domain.SNMPObject;
import org.edgexfoundry.domain.ScanList;
import org.edgexfoundry.domain.meta.Addressable;
import org.edgexfoundry.domain.meta.ProfileProperty;
import org.edgexfoundry.domain.meta.PropertyValue;
import org.edgexfoundry.domain.meta.ResourceOperation;
import org.edgexfoundry.exception.BadCommandRequestException;
import org.edgexfoundry.exception.DeviceNotFoundException;
import org.edgexfoundry.exception.controller.ServiceException;
import org.edgexfoundry.handler.SNMPHandler;
import org.edgexfoundry.support.logging.client.EdgeXLogger;
import org.edgexfoundry.support.logging.client.EdgeXLoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class SNMPDriver {

	private final static EdgeXLogger logger = EdgeXLoggerFactory.getEdgeXLogger(SNMPDriver.class);

	@Autowired
	ProfileStore profiles;

	@Autowired
	DeviceStore devices;

	@Autowired
	ObjectStore objectCache;

	@Autowired
	SNMPHandler handler;

	@Value("${snmp.version}")
	private int m_snmpVersion;

	@Value("${snmp.retries}")
	private int m_snmpRetries;

	@Value("${snmp.timeout}")
	private int m_snmpTimeout;




	@SuppressWarnings("rawtypes")
	private TransportMapping snmpTransportMapping;
	private Snmp snmpInstance;

	public ScanList discover() {
		ScanList scan = new ScanList();
		// Fill with SNMP specific discovery mechanism
		return scan;
	}

	// operation is get or set
	// Device to be written to
	// SNMP Object to be written to
	// value is string to be written or null
	public void process(ResourceOperation operation, SNMPDevice device, SNMPObject object, String value, String transactionId, String opId) {
		String result = "";
		result = processCommand(operation.getOperation(), device.getAddressable(), object, value);
		objectCache.put(device, operation, result);
		handler.completeTransaction(transactionId, opId, objectCache.getResponses(device, operation));
	}

	public String processCommand(String operation, Addressable addressable, SNMPObject object, String value) {
		logger.info("ProcessCommand: " + operation + ", interface: " + addressable.getProtocol() + ", address: " + addressable.getAddress() + ", attributes: " + object.getAttributes() + ", value: " + value );
		String result = "";
		// TODO SNMP stack goes here, return the raw value from the device as a string for processing
		CommunityTarget comTarget = createCommunityTarget(object.getAttributes(), addressable);
		if (operation.toLowerCase().equals("get")) {
			result = getValue(comTarget, object.getAttributes(), addressable);
		} else {
			result = setValue(comTarget, object, addressable, value);
		}
		return result;
	}

	@SuppressWarnings("null")
	private String setValue(CommunityTarget comTarget, SNMPObject object, Addressable addressable, String value) {
		String result = "";
		try
		{
			// Create the PDU object
			PDU pdu = new PDU();
			OID oid = new OID(object.getAttributes().getOid());
			ProfileProperty properties = object.getProperties();
			PropertyValue val = properties.getValue();
			String type = val.getType();
			Variable variable = null;
			if(type.equalsIgnoreCase("Integer"))
			{
				if(value != null)
					variable = new Integer32(Integer.parseInt(value));
			}
			
			VariableBinding varBind = new VariableBinding(oid, variable);

			pdu.add(varBind);
			pdu.setType(PDU.SET);
			ResponseEvent response = snmpInstance.set(pdu, comTarget);

			// Process Agent Response
			if (response != null)
			{
				PDU responsePDU = response.getResponse();
				if (responsePDU != null)
				{
					int errorStatus = responsePDU.getErrorStatus();
					
					if (errorStatus == PDU.noError)
					{
						result = responsePDU.getVariable(oid).toString();
					}
					else
					{
						
						result = responsePDU.getErrorStatusText();
					}
				}
				else
				{
					throw new BadCommandRequestException(responsePDU.getErrorStatusText());
				}
			}
			else
			{
				result = "Connection timed out";
				throw new DeviceNotFoundException(result);
			}
			

		}catch(Exception e){
			logger.error("Exception in setValue:" + e);
		}
		return result;
	}
	
	private String getValue(CommunityTarget comTarget, SNMPAttribute attributes, Addressable addressable) {
		String result = "";
		try
		{
			PDU pdu = new PDU();
			OID oid = new OID (attributes.getOid());
			pdu.add(new VariableBinding(oid));
			pdu.setType(PDU.GET);
			ResponseEvent response = snmpInstance.get(pdu, comTarget);
			PDU responsePDU = response.getResponse();
	
			if (responsePDU != null)
			{
				int errorStatus = responsePDU.getErrorStatus();
				if (errorStatus == PDU.noError)
				{
					Variable var = responsePDU.getVariable(oid);
					result = var.toString();
				}
				else
				{
					String errorStatusText = responsePDU.getErrorStatusText();
					throw new BadCommandRequestException(errorStatusText);
				}
			}
			else
			{
				result = "Connection timed out";
				throw new DeviceNotFoundException(result);
			}
		}catch(Exception e){
			logger.error("Exception in getValue():" + e);
			throw new ServiceException(e);
			
		}

		return result;
	}

	private CommunityTarget createCommunityTarget(SNMPAttribute attributes, Addressable addressable) {
		CommunityTarget comTarget = new CommunityTarget();
		comTarget.setCommunity(new OctetString(attributes.getCommunity()));
		switch(m_snmpVersion)
		{
			case 1:
				comTarget.setVersion(SnmpConstants.version1);
				break;
			case 2:
				comTarget.setVersion(SnmpConstants.version2c);
				break;
			case 3:
				comTarget.setVersion(SnmpConstants.version3);
				break;
	
			default:
				comTarget.setVersion(SnmpConstants.version1);
				break;
		}
		comTarget.setAddress(new UdpAddress(addressable.getAddress() + "/" + addressable.getPort()));
		comTarget.setRetries(m_snmpRetries);
		comTarget.setTimeout(m_snmpTimeout);
		return comTarget;
	}

	@SuppressWarnings("unchecked")
	public void initialize() {
		try
		{
			snmpTransportMapping = new DefaultUdpTransportMapping();
			if(!snmpTransportMapping.isListening()){
				snmpTransportMapping.listen();
			}
			snmpInstance = new  Snmp(snmpTransportMapping);
		} catch (IOException e) {
			logger.error("Unable to initialize SNMP Transport");

		}

	}

	public void disconnectDevice(String path) {
		try{
			snmpTransportMapping.close();
			snmpInstance.close();
		}catch(Exception e)
		{
			logger.error("Error while disconnecting SNMP transport and instance");
		}
	}

}
