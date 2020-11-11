package it.unimore.dipi.iot.server.resource.raw;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project coap-demo-smarthome
 * @created 11/11/2020 - 15:39
 */
public interface ResourceDataListener<T> {

    public void onDataChanged(SmartObjectResource<T> resource, T updatedValue);

}
