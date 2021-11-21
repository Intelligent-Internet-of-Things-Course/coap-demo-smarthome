package it.unimore.dipi.iot.client;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project coap-demo-smarthome
 * @created 21/11/2021 - 21:53
 */
public class TargetCoapResourceDescriptor {

    private String targetUrl;

    private boolean senmlSupport;

    public TargetCoapResourceDescriptor() {
    }

    public TargetCoapResourceDescriptor(String targetUrl, boolean senmlSupport) {
        this.targetUrl = targetUrl;
        this.senmlSupport = senmlSupport;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public boolean getSenmlSupport() {
        return senmlSupport;
    }

    public void setSenmlSupport(boolean senmlSupport) {
        this.senmlSupport = senmlSupport;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TargetResourceDescriptor{");
        sb.append("targetUrl='").append(targetUrl).append('\'');
        sb.append(", senmlSupport=").append(senmlSupport);
        sb.append('}');
        return sb.toString();
    }
}
