package com.example.floodmonitor.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "FloodMonitor REST API",
                version = "1.0",
                description = """
                        REST-Schnittstelle eines Hochwasser-Frühwarnsystems (DEZSYS GK71).

                        **Formate:** Alle Endpunkte liefern JSON (Standard) oder XML.
                        Das Format wird über den Accept-Header gewählt
                        (`application/json` oder `application/xml`). Andere Formate → 406.

                        **Fehler:** Alle Fehler haben das einheitliche Format `ApiError`
                        (timestamp, status, error, message, path).

                        **Authentifizierung:** Keine. Alle Endpunkte sind lesend (GET)
                        und öffentlich zugänglich.
                        """
        ),
        servers = @Server(url = "http://localhost:8080", description = "Lokale Entwicklung")
)
public class OpenApiConfig {
}