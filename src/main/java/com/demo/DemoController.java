package com.demo;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableRunnable;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/api/demo")
public class DemoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoController.class);

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    public ResponseEntity<Object> demo() {
        Object ob = getData();
        return new ResponseEntity<>(ob, HttpStatus.OK);
    }

    private Object getData() {
        Channel<Object> outChannel = Channels.newChannel(0);
        new Fiber<Void>((SuspendableRunnable) () -> {
            try {
                Object result = restTemplate.getForEntity("http://139.196.171.215:6060/?key=中国", Map.class).getBody();
                outChannel.send(result);
            } catch (Exception e) {
                LOGGER.error("", e);
            } finally {
                outChannel.close();
            }
        }).inheritThreadLocals().start();
        try {
            return outChannel.receive(5, TimeUnit.SECONDS);
        } catch (SuspendExecution | InterruptedException suspendExecution) {
            suspendExecution.printStackTrace();
        }
        return null;
    }
}
