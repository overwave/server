package dev.overwave.server;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class HelloController {

	@RequestMapping("/test")
	public String test() {
		return "Greetings from Spring Boot!";
	}

	@RequestMapping("/bot")
	public String test2() {
		return "5d7c0808";
	}
}