package com.example.config;

import com.google.maps.GeoApiContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleMapsConfig {
    
    @Bean
    public GeoApiContext buildGeoApiContext() {
        return new GeoApiContext.Builder().apiKey("AIzaSyBOxtFqpTl2-pNraBJFRc8GUVneP7MOmSI").build();
    }

}
