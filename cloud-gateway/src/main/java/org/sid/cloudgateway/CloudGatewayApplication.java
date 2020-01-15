package org.sid.cloudgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
class FallBackRestController{
	@GetMapping("/restCountriesFallback")
	public Map<String,String> restCountriesFallback(){
		Map<String,String> map=new HashMap<>();
		map.put("message","Default Rest Countries Fallback service");
		map.put("countries","Algeria, Morocco");
		return map;
	}
	@GetMapping("/muslimsalatFallback")
	public Map<String,String> muslimsalatback(){
		Map<String,String> map=new HashMap<>();
		map.put("message","Default Muslim Fallback service");
		map.put("Fajr","07:00");
		map.put("DOHR","14:00");
		return map;
	}
}

@SpringBootApplication
@EnableHystrix
public class CloudGatewayApplication {
	@Bean
	RouteLocator routes(RouteLocatorBuilder builder){
		return builder.routes()
				.route(r->r.path("/customers/**").uri("lb://CUSTOMER-SERVICE").id("r1"))
				.route(r->r.path("/products/**").uri("lb://INVENTORY-SERVICE").id("r2"))
				.route(r->r.path("/restcountries/**")
						.filters(f->f
								.addRequestHeader("x-rapidapi-host","restcountries-v1.p.rapidapi.com")
								.addRequestHeader("x-rapidapi-key", "fe5e774996msh4eb6e863d457420p1d2ffbjsnee0617ac5078")
								.rewritePath("/restcountries/(?<segment>.*)","/${segment}")
								.hystrix(h->h.setName("rest-countries")
										.setFallbackUri("forward:/restCountriesFallback"))
						)
						.uri("https://restcountries-v1.p.rapidapi.com").id("countries"))
				.route(r->r.path("/muslimsalat/**")
						.filters(f->f
								.addRequestHeader("x-rapidapi-host","muslimsalat.p.rapidapi.com")
								.addRequestHeader("x-rapidapi-key", "fe5e774996msh4eb6e863d457420p1d2ffbjsnee0617ac5078")
								.rewritePath("/muslimsalat/(?<segment>.*)","/${segment}")
								.hystrix(h->h.setName("muslimsalat")
										.setFallbackUri("forward:/muslimsalatFallback"))
						)
						.uri("https://muslimsalat.p.rapidapi.com").id("countries")
				)
				.build();

	}
	@Bean
	DiscoveryClientRouteDefinitionLocator dynamicRoutes(ReactiveDiscoveryClient rdc, DiscoveryLocatorProperties dlp){
		return new DiscoveryClientRouteDefinitionLocator(rdc,dlp);
	}
	public static void main(String[] args) {
		SpringApplication.run(CloudGatewayApplication.class, args);
	}

}
