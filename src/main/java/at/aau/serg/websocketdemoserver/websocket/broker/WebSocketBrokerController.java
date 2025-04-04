package at.aau.serg.websocketdemoserver.websocket.broker;

import at.aau.serg.websocketdemoserver.messaging.dtos.StompMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

import org.springframework.stereotype.Controller;


@Controller
public class WebSocketBrokerController {

    @MessageMapping("/login")
    @SendTo("/topic/login-response")
    public String handleLogin(String username) {
        // TODO handle login here
        return "";
    }

    @MessageMapping("/disconnect")
    @SendTo("/topic/disconnect-response")
    public String handleDisconnect(String username) {
        // TODO handle disconnect here
        return "";
    }


    //sample methods for exchange of String-messages or StompMessages / Json Objects
    @MessageMapping("/hello")
    @SendTo("/topic/hello-response")
    public String handleHello(String text) {
        // TODO handle the messages here
        return "echo from broker: "+text;
    }

    @MessageMapping("/object")
    @SendTo("/topic/rcv-object")
    public StompMessage handleObject(StompMessage msg) {

       return msg;
    }

}
