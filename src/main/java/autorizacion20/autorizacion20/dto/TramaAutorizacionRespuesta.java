/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package autorizacion20.autorizacion20.dto;

import java.math.BigDecimal;

/**
 *
 * @author XPC
 */
public class TramaAutorizacionRespuesta {
    
    private String PANEncriptado;
    private String numeroReferencia;
    private int numeroAutorizacion;
    private int secuenciaSistema;
    private BigDecimal montoCompra;
    private String estado;

    public String getPANEncriptado() {
        return PANEncriptado;
    }

    public void setPANEncriptado(String PANEncriptado) {
        this.PANEncriptado = PANEncriptado;
    }

    public String getNumeroReferencia() {
        return numeroReferencia;
    }

    public void setNumeroReferencia(String numeroReferencia) {
        this.numeroReferencia = numeroReferencia;
    }

    public int getNumeroAutorizacion() {
        return numeroAutorizacion;
    }

    public void setNumeroAutorizacion(int numeroAutorizacion) {
        this.numeroAutorizacion = numeroAutorizacion;
    }

    public int getSecuenciaSistema() {
        return secuenciaSistema;
    }

    public void setSecuenciaSistema(int secuenciaSistema) {
        this.secuenciaSistema = secuenciaSistema;
    }

    public BigDecimal getMontoCompra() {
        return montoCompra;
    }

    public void setMontoCompra(BigDecimal montoCompra) {
        this.montoCompra = montoCompra;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
     
}
