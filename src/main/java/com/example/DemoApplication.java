package com.example;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URL;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class DemoApplication {

	public static void main(String args[]) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	GraphiteReporter graphiteReporter(@Value("${HOSTEDGRAPHITE_APIKEY}") String prefix,
	                                  @Value("http://${HOSTEDGRAPHITE_URL}") URL url,
	                                  @Value("${HOSTEDGRAPHITE_PORT}") int port,
	                                  MetricRegistry registry) {

		java.security.Security.setProperty("networkaddress.cache.ttl", "60");

		GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
				.prefixedWith(prefix)
				.build(new Graphite(url.getHost(), port));
		reporter.start(1, TimeUnit.SECONDS);
		return reporter;
	}
}

@Controller
class RussianRouletteController {

	@Autowired
	private CounterService counterService;

	@Autowired
	private GaugeService gaugeService ;

	@RequestMapping("/rr.php")
	String rr(Model model) {
		StopWatch  sw  = new StopWatch() ;
		sw.start();
		boolean green = new Random().nextInt(100) > 50;
		String color = green ? "green" : "red";
		model.addAttribute("rr", color);
		this.counterService.increment("meter." + color + "");
		sw.stop();
		this.gaugeService.submit("timer.rr." + color , sw.getLastTaskTimeMillis());
		return "rr";
	}
}

@Component
class LoggingCLR implements CommandLineRunner {

	private Log log = LogFactory.getLog(getClass());

	@Override
	public void run(String... args) throws Exception {

		String greeting = "Hello, world!";

		this.log.info("INFO: " + greeting);
		this.log.warn("WARN: " + greeting);  // you can change logging levels dynamically
		this.log.debug("DEBUG: " + greeting);
	}
}