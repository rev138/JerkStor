package org.hzsogood.jerkstor.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/admin")
public class AdminController {

    @RequestMapping(method = RequestMethod.GET)
    public String index(ModelMap model) {

        model.addAttribute("message", "Spring 3 MVC Hello World");
        return "index";
    }
}
