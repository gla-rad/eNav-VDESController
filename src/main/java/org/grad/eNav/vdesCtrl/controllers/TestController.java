package org.grad.eNav.vdesCtrl.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Path;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/tests")
public class TestController {

    @GetMapping(path = "/testInt")
    @ResponseBody
    public ResponseEntity<Integer> getTestInt() {

        return new ResponseEntity<>(123, HttpStatus.OK);
    }

    @GetMapping(path = "/testString")
    @ResponseBody
    public ResponseEntity<String> getTestString() {

        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

}
