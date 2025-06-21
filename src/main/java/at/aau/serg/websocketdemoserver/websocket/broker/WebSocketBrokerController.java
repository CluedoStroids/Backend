package at.aau.serg.websocketdemoserver.websocket.broker;

//import at.aau.serg.websocketdemoserver.messaging.dtos.StompMessage;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;


@Controller
public class WebSocketBrokerController {

    @MessageMapping("/login")
    @SendTo("/topic/login-response")
    public String handleLogin(String username) {
        // TODO handle login here
        return "login-response";
    }

    @MessageMapping("/disconnect")
    @SendTo("/topic/disconnect-response")
    public String handleDisconnect(String username) {
        // TODO handle disconnect here
        return "disconnect-response";
    }

    //sample methods for exchange of String-messages or StompMessages / Json Objects
    @MessageMapping("/message")
    @SendTo("/topic/message-response")
    public String handleMessage(String text) {
        // TODO handle the messages here
        return "echo from broker: "+text;
    }
/*
    @MessageMapping("/object")
    @SendTo("/topic/rcv-object")
    public StompMessage handleObject(StompMessage msg) {

       return msg;
    }*/

}
