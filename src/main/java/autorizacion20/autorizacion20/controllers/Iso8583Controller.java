/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package autorizacion20.autorizacion20.controllers;

import autorizacion20.autorizacion20.clases.MensajeIso8583;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author XPC
 */
@RestController
public class Iso8583Controller {

    private static final Logger logger = LoggerFactory.getLogger(Iso8583Controller.class);
    private String lastJsonMessage;
    private String jsonAEmisorMessage;
    private String urlEmisor1 = "https://accountservicese1-2srxjcdrq-josue19-08s-projects.vercel.app"; // Cambia esta URL por la real
    private String urlEmisor2 = "https://accountservicedos-n67mesvus-josue19-08s-projects.vercel.app"; // Cambia esta URL por la real
    private byte[] lastIsoMessage; // Variable para guardar el último mensaje ISO recibido

    @PostMapping("/api/iso8583/receive")
    public ResponseEntity<byte[]> receiveIsoMessage(@RequestBody byte[] isoMessageBytes) {
        logger.info("Received ISO message bytes: {}", new String(isoMessageBytes));

        // Parse and process the ISO message
        MensajeIso8583 mensajeIso = MensajeIso8583.parse(isoMessageBytes);
        Map<Integer, String> isoData = mensajeIso.getDatos();

        // Convert ISO data to JSON manually
        String json = buildJson(mensajeIso, isoData);
        String jsonAEmisor = buildJsonAEmisor(mensajeIso);
        String jsonComprobarPan = buildJsonAEmisorVerificarPAN(mensajeIso);

        
        //mensajeIso.getDatos().put(2, "4380989406150991");
        //mensajeIso.getDatos().put(3, "123445"); 
        //mensajeIso.getDatos().put(4, "000000012000"); 
        //mensajeIso.getDatos().put(7, "2406241801"); 
        //mensajeIso.getDatos().put(11, "000000"); 
        //mensajeIso.getDatos().put(12, "150000"); 
        //mensajeIso.getDatos().put(14, "2406"); 
        //mensajeIso.getDatos().put(37, "123456789012"); 
        //mensajeIso.getDatos().put(38, "000145"); 
        //mensajeIso.getDatos().put(41, "12345678"); 
        // Actualizar el mapa de datos en mensajeIso
        
        byte[] envio = mensajeIso.mensajeBytes();
        //  mensajeIso.setCampo(11, "000051");  
        //  byte[] modifiedIsoMessageBytes = mensajeIso.mensajeBytes();

        manejarRespuesta(jsonComprobarPan,jsonAEmisor);
        System.out.println("ISO message as JSON: " + json);

        // Log the JSON
        logger.info("ISO message as JSON: {}", json);

        this.lastJsonMessage = json;
        this.jsonAEmisorMessage = jsonAEmisor;

        // Return a simple confirmation response (if needed)
        return ResponseEntity.ok(isoMessageBytes);
    }

    private void manejarRespuesta(String jsonComprobarPan, String jsonAEmisor) {
        String urlEmisor1 = "https://accountservicese1-i101vvzy8-josue19-08s-projects.vercel.app/account/check-pan";
        String urlEmisor2 = "https://accountservicedos-n67mesvus-josue19-08s-projects.vercel.app/account/check-pan";

        boolean exitoEmisor1 = enviarJsonAUrlExternaEmisor1(urlEmisor1, jsonComprobarPan);
        boolean exitoEmisor2 = enviarJsonAUrlExternaEmisor2(urlEmisor2, jsonComprobarPan);

        // Determinar cuál emisor dio éxito y enviar el JSON nuevamente a una tercera URL
        if (exitoEmisor1) {
            // Acción si la solicitud fue exitosa para el emisor 1
            logger.info("La solicitud fue exitosa para el emisor 1");
            enviarJsonAutorizonEmisor1(jsonAEmisor); // Envía el JSON a una tercera URL
        } else {
            // Acción si la solicitud falló para el emisor 1
            logger.error("La solicitud falló para el emisor 1");
            // Otras acciones...
        }

        if (exitoEmisor2) {
            // Acción si la solicitud fue exitosa para el emisor 2
            logger.info("La solicitud fue exitosa para el emisor 2");
            enviarJsonAutorizonEmisor2(jsonAEmisor); // Envía el JSON a una tercera URL
        } else {
            // Acción si la solicitud falló para el emisor 2
            logger.error("La solicitud falló para el emisor 2");
            // Otras acciones...
        }
    }

    private void enviarJsonAutorizonEmisor1(String json) {
        String urlTercera = "https://transactionserviceuno-u5bdj7yns-josue19-08s-projects.vercel.app/account/debit";

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);

            ResponseEntity<String> response = restTemplate.exchange(urlTercera, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String jsonResponse = response.getBody();
                logger.info("JSON recibido de la tercera URL: " + jsonResponse);
                System.out.println("JSON recibido de la tercera URL: " + jsonResponse);
                // Aquí puedes procesar el JSON recibido según tus necesidades
            } else {
                logger.error("Error al enviar JSON a la tercera URL: " + response.getStatusCode() + " - " + response.getBody());
                // Manejar el error según sea necesario
            }
        } catch (Exception e) {
            logger.error("Exception occurred while sending JSON to the third URL: ", e);
            // Manejar la excepción según sea necesario
        }
    }

    private void enviarJsonAutorizonEmisor2(String json) {
        String urlTercera = "https://accountservicedos-n67mesvus-josue19-08s-projects.vercel.app/account/debit";

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);

            ResponseEntity<String> response = restTemplate.exchange(urlTercera, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String jsonResponse = response.getBody();
                logger.info("JSON recibido de la tercera URL: " + jsonResponse);
                System.out.println("JSON recibido de la tercera URL: " + jsonResponse);
                // Aquí puedes procesar el JSON recibido según tus necesidades
            } else {
                logger.error("Error al enviar JSON a la tercera URL: " + response.getStatusCode() + " - " + response.getBody());
                // Manejar el error según sea necesario
            }
        } catch (Exception e) {
            logger.error("Exception occurred while sending JSON to the third URL: ", e);
            // Manejar la excepción según sea necesario
        }
    }

    private boolean enviarJsonAUrlExternaEmisor1(String url, String json) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("JSON enviado exitosamente: " + response.getBody());
                return true;
            } else {
                logger.error("Error al enviar JSON: " + response.getStatusCode() + " - " + response.getBody());
                return false;
            }
        } catch (Exception e) {
            logger.error("Exception occurred while sending JSON to external URL: ", e);
            return false;
        }
    }

    private boolean enviarJsonAUrlExternaEmisor2(String url, String json) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("JSON enviado exitosamente: " + response.getBody());
                return true;
            } else {
                logger.error("Error al enviar JSON: " + response.getStatusCode() + " - " + response.getBody());
                return false;
            }
        } catch (Exception e) {
            logger.error("Exception occurred while sending JSON to external URL: ", e);
            return false;
        }
    }

    @GetMapping("/api/iso8583/lastJson")
    public ResponseEntity<String> getLastJsonMessage() {
        return ResponseEntity.ok(lastJsonMessage);
    }

    @GetMapping("/api/iso8583/emisor")
    public ResponseEntity<String> getJsonAEmisor() {
        // Obtener el último mensaje ISO procesado (asumiendoS que lastJsonMessage está actualizado correctamente)
        if (jsonAEmisorMessage == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(jsonAEmisorMessage);
    }

    @PostMapping("/api/iso8583/emisor")
    public ResponseEntity<String> postJsonAEmisor() {
        if (jsonAEmisorMessage == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(jsonAEmisorMessage);
    }

    private String buildJsonAEmisor(MensajeIso8583 mensajeIso) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");

        // Campo 4
        if (mensajeIso.getDatos().containsKey(4)) {
            jsonBuilder.append("\"campo4\":\"").append(mensajeIso.getDatos().get(4)).append("\",");
        }

        // Campo 2
        if (mensajeIso.getDatos().containsKey(2)) {
            jsonBuilder.append("\"campo2\":\"").append(mensajeIso.getDatos().get(2)).append("\",");
        }

        // Campo 11
        if (mensajeIso.getDatos().containsKey(11)) {
            jsonBuilder.append("\"campo11\":\"").append(mensajeIso.getDatos().get(11)).append("\",");
        }

        // Campo 37
        if (mensajeIso.getDatos().containsKey(37)) {
            jsonBuilder.append("\"campo37\":\"").append(mensajeIso.getDatos().get(37)).append("\",");
        }

        // Campo 38
        if (mensajeIso.getDatos().containsKey(38)) {
            jsonBuilder.append("\"campo38\":\"").append(mensajeIso.getDatos().get(38)).append("\",");
        }

        // Campo 41
        if (mensajeIso.getDatos().containsKey(41)) {
            jsonBuilder.append("\"campo41\":\"").append(mensajeIso.getDatos().get(41)).append("\",");
        }

        // Eliminar la última coma si hay campos
        if (jsonBuilder.charAt(jsonBuilder.length() - 1) == ',') {
            jsonBuilder.setLength(jsonBuilder.length() - 1);
        }

        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    private String buildJsonAEmisorVerificarPAN(MensajeIso8583 mensajeIso) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");

        // Campo 2
        if (mensajeIso.getDatos().containsKey(2)) {
            jsonBuilder.append("\"pan\":\"").append(mensajeIso.getDatos().get(2)).append("\",");
        }

        // Eliminar la última coma si hay campos
        if (jsonBuilder.charAt(jsonBuilder.length() - 1) == ',') {
            jsonBuilder.setLength(jsonBuilder.length() - 1);
        }

        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    private String buildJson(MensajeIso8583 mensajeIso, Map<Integer, String> isoData) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"mti\":\"").append(mensajeIso.getMti()).append("\",");
        jsonBuilder.append("\"bitmap\":\"").append(mensajeIso.obtenerBitMapHex()).append("\",");
        jsonBuilder.append("\"data\":{");
        TreeMap<Integer, String> sortedData = new TreeMap<>(isoData);
        for (Map.Entry<Integer, String> entry : sortedData.entrySet()) {
            jsonBuilder.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue()).append("\",");
        }
        if (!sortedData.isEmpty()) {
            jsonBuilder.setLength(jsonBuilder.length() - 1); // Cambiar el ultimo
        }
        jsonBuilder.append("}");
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    public String jsonAEmisorMessage() {
        return lastJsonMessage;
    }

    public String getLastJsonMessages() {
        return jsonAEmisorMessage;
    }

    public byte[] getLastIsoMessage() {
        return lastIsoMessage;
    }
}
