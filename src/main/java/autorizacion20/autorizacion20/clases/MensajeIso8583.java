/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package autorizacion20.autorizacion20.clases;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author XPC
 */
public class MensajeIso8583 {
    private String mti;
    private BitSet bitMap = new BitSet(64);
    private Map<Integer, String> datos = new HashMap<>();

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public BitSet getBitMap() {
        return bitMap;
    }

    public void setBitMap(BitSet bitMap) {
        this.bitMap = bitMap;
    }

    public Map<Integer, String> getDatos() {
        return datos;
    }

    public void setDatos(Map<Integer, String> datos) {
        this.datos = datos;
    }

    public void establecerBitsEnBitMap() {
        for (Integer campo : datos.keySet()) {
            bitMap.set(campo - 1);
        }
    }

    public String obtenerBitMapHex() {
        byte[] bytes = bitMap.toByteArray();
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

    public String mensaje() {
        StringBuilder mensaje = new StringBuilder();
        establecerBitsEnBitMap();
        mensaje.append(mti);
        mensaje.append(obtenerBitMapHex());
        TreeMap<Integer, String> ordenCampos = new TreeMap<>(datos);
        for (Map.Entry<Integer, String> campo : ordenCampos.entrySet()) {
            mensaje.append(campo.getValue());
        }
        return mensaje.toString();
    }

    public byte[] mensajeBytes() {
        establecerBitsEnBitMap();
        byte[] mtiBytes = mti.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        byte[] bitMapBytes = mensajeBytes(obtenerBitMapHex());
        TreeMap<Integer, String> ordenCampos = new TreeMap<>(datos);

        byte[] result = new byte[mtiBytes.length + bitMapBytes.length + ordenCampos.values().stream().mapToInt(String::length).sum()];
        System.arraycopy(mtiBytes, 0, result, 0, mtiBytes.length);
        System.arraycopy(bitMapBytes, 0, result, mtiBytes.length, bitMapBytes.length);

        int offset = mtiBytes.length + bitMapBytes.length;
        for (String campo : ordenCampos.values()) {
            byte[] campoBytes = campo.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
            System.arraycopy(campoBytes, 0, result, offset, campoBytes.length);
            offset += campoBytes.length;
        }

        return result;
    }

    public byte[] mensajeBytes(String bitMap) {
        if (bitMap.length() % 2 != 0) {
            throw new IllegalArgumentException("La longitud del BitMap debe ser par.");
        }
        int cantidad = bitMap.length();
        byte[] bytes = new byte[cantidad / 2];
        for (int i = 0; i < cantidad; i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(bitMap.substring(i, 2), 16);
        }
        return bytes;
    }

    public static MensajeIso8583 parse(byte[] mensaje) {
        MensajeIso8583 mensajeIso = new MensajeIso8583();
        mensajeIso.mti = new String(mensaje, 0, 4, java.nio.charset.StandardCharsets.US_ASCII);

        byte[] bitMapBytes = new byte[8];
        System.arraycopy(mensaje, 4, bitMapBytes, 0, 8);
        mensajeIso.bitMap = BitSet.valueOf(bitMapBytes);

        int index = 4 + 8;
        for (int i = 1; i <= 64; i++) {
            if (mensajeIso.bitMap.get(i - 1)) {
                int fieldLength = getFieldLength(i);
                String data = new String(mensaje, index, fieldLength, java.nio.charset.StandardCharsets.US_ASCII);
                mensajeIso.datos.put(i, data);
                index += fieldLength;
            }
        }

        return mensajeIso;
    }

    private static int getFieldLength(int fieldNumber) {
        Map<Integer, Integer> fieldLengths = new HashMap<>();
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

        return fieldLengths.getOrDefault(fieldNumber, 0);
    }
}