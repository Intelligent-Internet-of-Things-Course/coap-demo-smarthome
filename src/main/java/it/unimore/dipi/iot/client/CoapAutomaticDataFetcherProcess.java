package it.unimore.dipi.iot.client;

import it.unimore.dipi.iot.utils.CoreInterfaces;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;


/**
 * A simple CoAP automatic data fetcher.
 * Discover available resource on a target CoAP Smart Object and then start observing core.a and core.s available and
 * observable resources.
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project coap-demo-smartobject
 * @created 20/10/2020 - 09:19
 */
public class CoapAutomaticDataFetcherProcess {

	private final static Logger logger = LoggerFactory.getLogger(CoapAutomaticDataFetcherProcess.class);

	private static final String TARGET_SMART_OBJECT_ADDRESS = "127.0.0.1";

	private static final int TARGET_SMART_OBJECT_IP = 5683;

	private static final String WELL_KNOWN_CORE_URI = "/.well-known/core";

	private static final String OBSERVABLE_CORE_ATTRIBUTE = "obs";

	private static final String INTERFACE_CORE_ATTRIBUTE = "if";

	private static List<String> targetObservableResourceList = null;

	private static Map<String, CoapObserveRelation> observingRelationMap = null;

	public static void main(String[] args) {

		//Init target resource list array and observing relations
		targetObservableResourceList = new ArrayList<>();
		observingRelationMap = new HashMap<>();

		//Initialize coapClient
		CoapClient coapClient = new CoapClient();

		//Discover available observable core.a and core.s resources on the target node
		discoverTargetObservableResources(coapClient);

		//Start observing each resource
		targetObservableResourceList.forEach(targetResourceUrl -> {
			startObservingTargetResource(coapClient, targetResourceUrl);
		});

		//Sleep and then cancel registrations
		try {
			Thread.sleep(60*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		observingRelationMap.forEach((key, value) -> {
			logger.info("Canceling Observation for target Url: {}", key);
			value.proactiveCancel();
		});

	}

	private static void startObservingTargetResource(CoapClient coapClient, String targetUrl) {

		logger.info("OBSERVING ... {}", targetUrl);

		Request request = Request.newGet().setURI(targetUrl).setObserve();
		request.setConfirmable(true);

		CoapObserveRelation relation = coapClient.observe(request, new CoapHandler() {

			public void onLoad(CoapResponse response) {
				String content = response.getResponseText();
				logger.info("Notification -> Resource Target: {} -> Body: {}", targetUrl, content);
			}

			public void onError() {
				logger.error("OBSERVING {} FAILED", targetUrl);
			}
		});

		observingRelationMap.put(targetUrl, relation);

	}

	private static void discoverTargetObservableResources(CoapClient coapClient){

		//Request Class is a generic CoAP message: in this case we want a GET.
		//"Message ID", "Token" and other header's fields can be set
		Request request = new Request(Code.GET);

		//coap://127.0.0.1:5683/.well-known/core
		request.setURI(String.format("coap://%s:%d%s",
				TARGET_SMART_OBJECT_ADDRESS,
				TARGET_SMART_OBJECT_IP,
				WELL_KNOWN_CORE_URI));

		//Set Request as Confirmable
		request.setConfirmable(true);

		logger.info("Request Pretty Print: \n{}", Utils.prettyPrint(request));

		//Synchronously send the GET message (blocking call)
		CoapResponse coapResp = null;

		try {

			coapResp = coapClient.advanced(request);

			if(coapResp != null) {

				//Pretty print for the received response
				logger.info("Response Pretty Print: \n{}", Utils.prettyPrint(coapResp));

				if (coapResp.getOptions().getContentFormat() == MediaTypeRegistry.APPLICATION_LINK_FORMAT) {

					Set<WebLink> links = LinkFormat.parse(coapResp.getResponseText());

					links.forEach(link -> {

						if(link.getURI() != null && !link.getURI().equals(WELL_KNOWN_CORE_URI) && link.getAttributes() != null && link.getAttributes().getCount() > 0){

							//If the resource is a core.s or core.a and it is observable save the target url reference
							if(link.getAttributes().containsAttribute(OBSERVABLE_CORE_ATTRIBUTE) &&
									link.getAttributes().containsAttribute(INTERFACE_CORE_ATTRIBUTE) &&
									(link.getAttributes().getAttributeValues(INTERFACE_CORE_ATTRIBUTE).get(0).equals(CoreInterfaces.CORE_S.getValue()) || link.getAttributes().getAttributeValues(INTERFACE_CORE_ATTRIBUTE).get(0).equals(CoreInterfaces.CORE_A.getValue()))){

								logger.info("Target resource found ! URI: {}}", link.getURI());

								//E.g. coap://<node_ip>:<node_port>/<resource_uri>
								String targetResourceUrl = String.format("coap://%s:%d%s",
										TARGET_SMART_OBJECT_ADDRESS,
										TARGET_SMART_OBJECT_IP,
										link.getURI());

								targetObservableResourceList.add(targetResourceUrl);

								logger.info("Target Resource URL: {} correctly saved !", targetResourceUrl);

							}
							else
								logger.info("Resource {} does not match filtering parameters ....", link.getURI());

						}
					});

				} else {
					logger.error("CoRE Link Format Response not found !");
				}
			}
		} catch (ConnectorException | IOException e) {
			e.printStackTrace();
		}

	}
}