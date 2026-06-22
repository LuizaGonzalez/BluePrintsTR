package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class BlueprintWSController {
    private final SimpMessagingTemplate messaging;
    private final BlueprintsServices services;

    public BlueprintWSController(SimpMessagingTemplate messaging, BlueprintsServices services) {
        this.messaging = messaging;
        this.services = services;
    }

    @MessageMapping("/draw")
    public void drawPoint(@Payload DrawEvent event) throws BlueprintNotFoundException {
        services.addPoint(event.author(), event.name(), event.point().x(), event.point().y());
        messaging.convertAndSend("/topic/blueprints."+ event.author() + "."+ event.name(),event);
    }
    public record DrawEvent(String author, String name, Point point) { }
}
