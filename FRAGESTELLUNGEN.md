# Fragestellungen

## 1. Was sind die Hauptmerkmale von JSON?

- **JSON (JavaScript Object Notation)** ist ein textbasiertes, leichtgewichtiges Datenaustauschformat.
- Es kennt nur wenige Datentypen: **Objekte** `{ }` (Schlüssel-Wert-Paare), **Arrays** `[ ]`, **Strings**,
  **Zahlen**, **true/false** und **null**.
- Schlüssel sind immer Strings in doppelten Anführungszeichen.
- Es ist **für Menschen gut lesbar** und für Maschinen einfach zu parsen; die Syntax ist an JavaScript angelehnt,
  JSON ist aber **sprachunabhängig** (Parser gibt es für praktisch jede Sprache).
- **Kompakt** – weniger Overhead als XML, deshalb Standardformat für Web-APIs.
- Kein Schema, keine Kommentare, keine Attribute, keine Namespaces im Standard.
- Zeichenkodierung bei Datenaustausch: UTF-8.

**Im Projekt:** Standardformat der API, z. B. `{"stationId":"ST-001","waterLevel":248.6, ...}`.

**Quellen:**
- T. Bray (Hrsg.): RFC 8259 – The JavaScript Object Notation (JSON) Data Interchange Format, IETF, 2017. https://www.rfc-editor.org/rfc/rfc8259
- json.org – Einführung in JSON. https://www.json.org/json-de.html

---

## 2. Was sind die Hauptmerkmale von XML?

- **XML (Extensible Markup Language)** ist eine textbasierte **Auszeichnungssprache** für strukturierte Daten.
- Daten stehen in **Elementen** mit Start- und End-Tag (`<waterLevel>248.6</waterLevel>`), Elemente können
  **Attribute** haben und beliebig verschachtelt werden (**Baumstruktur**).
- Es gibt genau **ein Wurzelelement**; das Dokument muss **wohlgeformt** sein (jedes Tag geschlossen, korrekt verschachtelt).
- **Erweiterbar:** Tag-Namen sind frei wählbar.
- **Validierbar** über Schemas (DTD, XML Schema/XSD), unterstützt **Namespaces**, Kommentare und eine
  Deklaration (`<?xml version="1.0" encoding="UTF-8"?>`).
- Umfangreiches Ökosystem: XPath, XSLT, DOM-/SAX-Parser.
- Nachteil gegenüber JSON: **mehr Overhead** (doppelte Tag-Namen), dadurch größer und aufwendiger zu verarbeiten.

**Im Projekt:** alternatives Format, z. B. `<measurement><stationId>ST-001</stationId>...</measurement>`.

**Quellen:**
- W3C: Extensible Markup Language (XML) 1.0 (Fifth Edition), W3C Recommendation, 2008. https://www.w3.org/TR/xml/
- MDN Web Docs: XML introduction. https://developer.mozilla.org/en-US/docs/Web/XML/Guides/XML_introduction

---

## 3. Wie unterscheidet die Applikation, ob JSON oder XML zurückgegeben wird?

Über **Content Negotiation** anhand des HTTP-Headers **`Accept`**:

1. Der Client schickt z. B. `Accept: application/json` oder `Accept: application/xml`.
2. Der Controller gibt nur ein Java-Objekt zurück (z. B. `Measurement`). Spring MVC vergleicht den `Accept`-Header mit
   den verfügbaren **HttpMessageConvertern**:
    - `MappingJackson2HttpMessageConverter` → JSON (immer vorhanden durch `spring-boot-starter-web`)
    - `MappingJackson2XmlHttpMessageConverter` → XML (vorhanden, weil `jackson-dataformat-xml` in `build.gradle` eingebunden ist)
3. Der passende Converter serialisiert das Objekt; die Antwort bekommt den passenden `Content-Type`.
4. **Ohne Accept-Header** (oder `*/*`) liefert Spring Boot **JSON**, weil der JSON-Converter vor dem XML-Converter steht.
5. Wird ein Format verlangt, das kein Converter kann (z. B. `text/plain`), wirft Spring eine
   `HttpMediaTypeNotAcceptableException` → unser `GlobalExceptionHandler` antwortet mit **406 Not Acceptable**.

Die XML-Elementnamen werden mit `@JacksonXmlRootElement` und `@JacksonXmlProperty` festgelegt.
Der Webclient setzt beim Umschalten einfach den Accept-Header neu.

**Quellen:**
- Spring Framework Reference – Content Types / Content Negotiation. https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-config/content-negotiation.html
- MDN Web Docs: Content negotiation. https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Content_negotiation
- R. Fielding u. a.: RFC 9110 – HTTP Semantics, Abschnitt 12.5.1 „Accept“. https://www.rfc-editor.org/rfc/rfc9110#name-accept

---

## 4. Was zeichnet ein REST-Interface aus und warum wird es so oft als Schnittstelle verwendet?

REST (**Representational State Transfer**) ist ein Architekturstil, den Roy Fielding 2000 in seiner Dissertation beschrieben hat.
Merkmale (Constraints):

- **Client-Server:** Trennung von Oberfläche (Webclient) und Datenhaltung/Logik (Spring-Boot-Server).
- **Zustandslosigkeit (stateless):** Jede Anfrage enthält alle nötigen Informationen; der Server speichert keinen Sitzungszustand.
- **Cachebarkeit:** Antworten können als cachebar gekennzeichnet werden.
- **Einheitliche Schnittstelle (uniform interface):**
    - **Ressourcen** werden über **URIs** identifiziert (`/api/v1/stations/ST-001`) – Nomen statt Verben
    - Manipulation über **Repräsentationen** (JSON, XML)
    - **Standard-HTTP-Methoden** (GET, POST, PUT, PATCH, DELETE) und **Statuscodes** (200, 404, 400 …)
    - selbstbeschreibende Nachrichten (`Content-Type`, `Accept`)
- **Geschichtetes System:** Proxies, Load Balancer, Gateways können dazwischen liegen.

**Warum so verbreitet?**
- baut auf **HTTP** auf – überall verfügbar, Firewall-freundlich, keine Spezialsoftware nötig
- **sprach- und plattformunabhängig** (Java-Server, JavaScript-Client, Mobile Apps …)
- **einfach** zu verstehen und zu testen (Browser, curl, Postman)
- **skalierbar** durch Zustandslosigkeit und Caching
- flexible Formate (JSON, XML), gute Werkzeuge zur Dokumentation (OpenAPI)

**Quellen:**
- R. T. Fielding: Architectural Styles and the Design of Network-based Software Architectures, Dissertation, UC Irvine, 2000, Kapitel 5. https://ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm
- MDN Web Docs: REST (Glossar). https://developer.mozilla.org/en-US/docs/Glossary/REST

---

## 5. Unterschied zwischen GET, POST, PUT und PATCH

| Methode | Zweck | Request-Body | Sicher (safe) | Idempotent | Typischer Status |
|---|---|---|---|---|---|
| **GET** | Ressource **lesen** | nein | ja | ja | 200 OK |
| **POST** | neue Ressource **anlegen** bzw. Daten verarbeiten | ja | nein | **nein** | 201 Created |
| **PUT** | Ressource **vollständig ersetzen** (oder unter bekannter URI anlegen) | ja (komplette Ressource) | nein | ja | 200 / 204 |
| **PATCH** | Ressource **teilweise ändern** | ja (nur Änderungen) | nein | nicht garantiert | 200 / 204 |

- **Sicher** = verändert keinen Zustand am Server.
- **Idempotent** = mehrfaches Ausführen hat dieselbe Wirkung wie einmaliges (zweimal `PUT` mit denselben
  Daten → gleicher Zustand; zweimal `POST` → zwei Ressourcen).
- **PUT vs. PATCH:** `PUT /stations/ST-001` schickt die ganze Station, fehlende Felder würden überschrieben;
  `PATCH /stations/ST-001` mit `{"isActive": false}` ändert nur dieses eine Feld.

**Im Projekt:** alle Endpunkte sind GET, weil sie nur lesen.

**Quellen:**
- RFC 9110 – HTTP Semantics, Abschnitt 9 „Methods“ (GET, POST, PUT; safe/idempotent). https://www.rfc-editor.org/rfc/rfc9110#name-methods
- RFC 5789 – PATCH Method for HTTP. https://www.rfc-editor.org/rfc/rfc5789
- MDN Web Docs: HTTP request methods. https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Methods

---

## 6. Neue Request-Methode / neuer Pfad – welche Stellen im Code muss ich ändern?

Am Beispiel eines neuen Endpunkts `GET /api/v1/stations/{id}/measurements/count`:

1. **Controller** (`controller/StationController.java`): neue Methode mit Mapping-Annotation
   (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping`) und Pfad, Parameter mit
   `@PathVariable` / `@RequestParam` / `@RequestBody`. Für einen ganz neuen Bereich: neue Controller-Klasse mit
   `@RestController` und `@RequestMapping`.
2. **Service** (`service/...`): die eigentliche Logik als neue Methode (Controller bleibt schlank).
3. **Repository** (`repository/...`): nur falls ein neuer Datenzugriff nötig ist.
4. **Model / DTO** (`model/`, `dto/`): falls ein neues Antwort- oder Request-Format gebraucht wird
   (für XML ggf. `@JacksonXmlRootElement`).
5. **Fehlerbehandlung** (`exception/GlobalExceptionHandler`): nur falls neue Fehlerfälle/Exceptions dazukommen.
6. **Validierung** (`controller/QueryParams`): falls neue Parameter geprüft werden müssen.
7. **OpenAPI-Doku**: `@Operation`, `@Parameter`, `@ApiResponse` an der neuen Methode; `docs/openapi.yaml` neu exportieren.
8. **Tests** (`src/test/java`): neuen Testfall schreiben.
9. Optional **Webclient** (`static/app.js`), falls der Client den Endpunkt nutzen soll, und **README**.

**Quellen:**
- Spring Guide: Building a RESTful Web Service. https://spring.io/guides/gs/rest-service/
- Spring Framework Reference – Annotated Controllers / Request Mapping. https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html

---

## 7. Kann man OpenAPI und Swagger vergleichen?

**Nur eingeschränkt** – sie sind keine Konkurrenten, sondern gehören zusammen:

| | OpenAPI | Swagger |
|---|---|---|
| Was | **Spezifikation** (Standard), wie eine REST-API beschrieben wird | **Werkzeuge**, die diese Spezifikation umsetzen |
| Verantwortlich | OpenAPI Initiative (Linux Foundation) | SmartBear Software |
| Form | YAML- oder JSON-Dokument (z. B. `docs/openapi.yaml`) | Swagger UI, Swagger Editor, Swagger Codegen … |
| Geschichte | 2015 wurde die „Swagger Specification“ an die OpenAPI Initiative übergeben und in „OpenAPI Specification“ umbenannt; Swagger 2.0 = OpenAPI 2.0, danach OpenAPI 3.x | Name blieb für die Tools erhalten |

Kurz gesagt: **OpenAPI = Spezifikation, Swagger = Tools zur Umsetzung der Spezifikation.**
Deshalb ist „Swagger-Spezifikation“ nur für Version 2.0 und älter korrekt.

**Im Projekt:** springdoc erzeugt aus den Annotationen ein **OpenAPI**-3-Dokument (`/v3/api-docs`), und
**Swagger UI** (`/swagger-ui.html`) stellt es grafisch dar.

**Quellen:**
- SmartBear / swagger.io: What Is the Difference Between Swagger and OpenAPI? (2017). https://swagger.io/difference-between-swagger-and-openapi
- OpenAPI Initiative: OpenAPI Specification. https://spec.openapis.org/oas/latest.html

---

## 8. Wo liegt im Code-Snippet der Fehler?

```java
@GetMapping("/stations/add")
public String createStation(@RequestParam String name, @RequestParam double warning) {
    stationService.save(new Station(name, warning));
    return "Station successfully created!";
}
```

**Hauptfehler: Für eine Aktion, die Daten verändert (eine Station anlegen), wird `GET` verwendet.**
`GET` ist laut HTTP-Standard **sicher** und **idempotent**, darf also keinen Zustand am Server ändern. Folgen:
- Browser, Proxies oder Suchmaschinen-Crawler können GET-Anfragen **cachen, vorausladen oder wiederholen** → ungewollt mehrfach angelegte Stationen.
- Ein einfacher Link oder `<img src="...">` könnte Stationen anlegen (**CSRF**-Gefahr).
- Die Daten stehen in der **URL** und landen in Logs und im Browserverlauf.

**Weitere Fehler (REST-Prinzipien):**
- **Verb in der URL** (`/add`): Ressourcen-URIs sollten Nomen sein; die Aktion ergibt sich aus der Methode → `POST /stations`.
- **Daten als Query-Parameter** statt als **Request-Body** (JSON/XML).
- Rückgabe ist ein **Text** statt der angelegten Ressource; Status ist **200 statt 201 Created**, kein `Location`-Header.
- **Keine Validierung:** kritischer Wasserstand, ID, Fluss usw. fehlen; ungültige Werte (z. B. negativ) werden nicht abgefangen.
- Fehlende API-Version im Pfad (`/api/v1/...`).

**Korrigierte Variante:**
```java
@PostMapping("/api/v1/stations")
public ResponseEntity<Station> createStation(@Valid @RequestBody StationCreateRequest request) {
    Station created = stationService.create(request);
    URI location = URI.create("/api/v1/stations/" + created.getId());
    return ResponseEntity.created(location).body(created);   // 201 Created
}
```

**Quellen:**
- RFC 9110 – HTTP Semantics, Abschnitt 9.2.1 „Safe Methods“ und 9.3.3 „POST“. https://www.rfc-editor.org/rfc/rfc9110#name-safe-methods
- MDN Web Docs: GET / POST. https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Methods/GET
- R. T. Fielding: Dissertation, Kapitel 5.2.1 (Ressourcen und Ressourcen-Identifier). https://ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm