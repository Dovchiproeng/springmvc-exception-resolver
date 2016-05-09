package com.vdp.spring.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.vdp.spring.exception.BusinessException;
import com.vdp.spring.exception.InvalidArgumentsException;
import com.vdp.spring.exception.ObjectNotFoundException;
import com.vdp.spring.json.Customer;

@Controller
@RequestMapping("/demo")
public class TestController {
    private static final Logger LOG = LoggerFactory.getLogger(TestController.class);

    private List<Customer> customerList;

    public TestController() {
        customerList = new ArrayList<Customer>();
        Customer c1 = new Customer();
        c1.setName("Vandet PIN");
        c1.setAge(24);
        c1.setGender('M');
        customerList.add(c1);

        Customer c2 = new Customer();
        c2.setName("Mali Ada");
        c2.setAge(20);
        c2.setGender('M');
        customerList.add(c2);
    }

    @RequestMapping(value = "/v1/customers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Customer>> getAllCustomer() {

        LOG.info(">>>>>>>>>>>>>>> get all customer >>>>>>>>>>");
        return new ResponseEntity<>(customerList, HttpStatus.OK);
    }

    @RequestMapping(value = "/v1/customers/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Customer> getCustomer(@RequestParam String name) {

        if(StringUtils.equalsIgnoreCase(name, "error")) {
            throw new BusinessException("Server error, please contact your admin", 200);
        }

        if(NumberUtils.isNumber(name)) {
            throw new InvalidArgumentsException(String.format("[Name=%s] must be string", name));
        }

        Customer c = searchName(name);
        if(c == null) {
            throw new ObjectNotFoundException(String.format("[Name=%s] search not found.", name));
        }

        LOG.info(">>>>>>>>>>>>>>> say name you pass: " + c.getName());
        return new ResponseEntity<Customer>(c, HttpStatus.OK);
    }

    private Customer searchName(String name) {
        for(Customer c : customerList) {
            if(StringUtils.equalsIgnoreCase(name, c.getName())) {
                return c;
            }
        }

        return null;
    }

}