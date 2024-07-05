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
    private String externalApiUrl;
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
        
        
        System.out.println("ISO message as JSON: " + json);

        // Log the JSON
        logger.info("ISO message as JSON: {}", json);

        
        this.lastJsonMessage = json;
        this.jsonAEmisorMessage = jsonAEmisor;

        // Return a simple confirmation response (if needed)
        return ResponseEntity.ok(isoMessageBytes);
    }

    // Método para reconstruir el mensaje ISO con los datos actualizados
    private byte[] reconstructIsoMessage(MensajeIso8583 mensajeIso, Map<Integer, String> isoData) {
        // Obtener los bytes del mensaje ISO original
        byte[] originalIsoBytes = mensajeIso.mensajeBytes();

        // Reconstruir el mensaje ISO con los datos actualizados
        for (Map.Entry<Integer, String> entry : isoData.entrySet()) {
            Integer fieldNumber = entry.getKey();
            String fieldValue = entry.getValue();

            // Verificar y ajustar la longitud del campo según fieldLengths si es necesario
            if (fieldValue != null && fieldValue.length() > 0) {
                int expectedLength = fieldLengths.getOrDefault(fieldNumber, fieldValue.length());
                if (fieldValue.length() < expectedLength) {
                    // Rellenar con espacios si es menor que la longitud esperada
                    fieldValue = String.format("%-" + expectedLength + "s", fieldValue);
                } else if (fieldValue.length() > expectedLength) {
                    // Truncar si es mayor que la longitud esperada
                    fieldValue = fieldValue.substring(0, expectedLength);
                }
                isoData.put(fieldNumber, fieldValue);
            }
        }

        // Reconstruir los bytes del mensaje ISO con los datos actualizados
        return mensajeIso.mensajeBytes();
    }

    // Método para convertir byte[] a representación hexadecimal de cadena
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }

    // Longitudes esperadas de los campos ISO
    private static final Map<Integer, Integer> fieldLengths = new HashMap<>();
    static {
        fieldLengths.put(2, 16);
        fieldLengths.put(3, 6);
        fieldLengths.put(4, 12);
        fieldLengths.put(7, 10);
        fieldLengths.put(11, 6);
        fieldLengths.put(12, 6);
        fieldLengths.put(14, 4);
        fieldLengths.put(37, 12);
        fieldLengths.put(38, 6);
        fieldLengths.put(41, 8);
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
