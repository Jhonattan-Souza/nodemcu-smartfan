import groovy.json.JsonSlurper

metadata {
	definition (name: "NodeMcu Fan", namespace: "jsp", author: "Jhonattan", ocfDeviceType: "oic.d.fan") {
		capability "Fan Speed"
        capability "Refresh"
        capability "Switch"
        capability "Health Check"
        capability "Switch Level"
	}
    
    preferences {
        input("ip", "text", title: "IP Address", description: "IP Server Address", defaultValue: "192.168.0.1", required: true, displayDuringSetup: true)
        input("port", "number", title: "Port Number", description: "Port Number (Default:80)", defaultValue: "80", required: true, displayDuringSetup: true)
    }
}

def initialize()
{
	setup()
}

def ping()
{
	refresh()
}

def setFanSpeed(speed) {
	if (speed == 4)
    	speed = 3;
        
	sendHubCommand(speed)
    
    String switchValue = ""
    
    if (speed == 0)
    	switchValue = "off"
    else
    	switchValue = "on"
    
    return [sendEvent(name: "switch", value: switchValue), sendEvent(name: "fanSpeed", value: "$speed")] 
}

def sendHubCommand(speed)
{
  def hubAction = new physicalgraph.device.HubAction(
        method: "POST", 
		headers: [HOST: "${settings.ip}:${settings.port}", contentType: "application/json"],
        path: "/speed/",
        body: "{\"value\":$speed}"
    )    
    
	sendHubCommand(hubAction)
}

def sendGetCommand()
{
  def hubAction = new physicalgraph.device.HubAction(
        method: "GET", 
		headers: [HOST: "${settings.ip}:${settings.port}", Accept: "application/json"],
        path: "/status/"
    )    
    
	sendHubCommand(hubAction)
}

def on()
{
	sendHubCommand(2)
	return [createEvent(name: "sendEvent", value: "2"), sendEvent(name: "switch", value: "on")]
}

def off()
{
	sendHubCommand(0)
	return [sendEvent(name: "fanSpeed", value: "0"), sendEvent(name: "switch", value: "off")]
}

def setLevel(val, rate = null) {
	log.debug "setLevel(val=${val})"
}

def refresh()
{
	setup()	
	sendGetCommand()
}

def parse(String description) {
	def map = stringToMap(description)
    
	def headerString = new String(map.headers.decodeBase64())

	if (headerString.contains("200 OK")) 
    {
    	def bodyString = new String(map.body.decodeBase64())
       
        def slurper = new JsonSlurper()
        
		def result = slurper.parseText(bodyString)
        
        log.debug "$result"
        
        def cmd = [createEvent(name: "fanSpeed", value: result.speed), createEvent(name: "switch", value: "off")]
        log.debug "$cmd"
        if (result.speed == 0)
			return [createEvent(name: "fanSpeed", value: result.speed), createEvent(name: "switch", value: "off")]
		
        return [createEvent(name: "fanSpeed", value: result.speed), createEvent(name: "switch", value: "on")]
    }
}

def setup() {
    def hosthex = convertIPtoHex(settings.ip)

    def porthex = convertPortToHex(settings.port)

    device.deviceNetworkId = hosthex + ":" + porthex
}

private String convertIPtoHex(ip) { 
	String hexip = ip.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
	return hexip
}
private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
	return hexport
}


